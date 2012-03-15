package fi.smaa.glei.gpu;

import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clReleaseProgram;
import static org.jocl.CL.clSetKernelArg;

import java.io.IOException;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;

import fi.smaa.glei.LogGARCHDensityFunction;

public class LogGARCHDensityFunctionGPU extends LogGARCHDensityFunction {
	
	public static final String KERNEL_FILENAME = "log_garch_density.cl";
	public static final String KERNEL_FUNCNAME = "log_garch_density";
	private OpenCLFacade facade;
	private cl_program program;
	private cl_kernel kernel;
	private long warpSize;
	public static final int DEFAULT_WARP_SIZE = 64;

	protected LogGARCHDensityFunctionGPU(int p, int q, double[] data, OpenCLFacade facade, long warpSize) throws IOException {
		super(p, q, data);
		this.facade = facade;
		program = facade.buildProgram(KERNEL_FILENAME);
		kernel = clCreateKernel(program, KERNEL_FUNCNAME, null);
		this.warpSize = warpSize;
	}
	
	public LogGARCHDensityFunctionGPU(int p, int q, double[] data, OpenCLFacade facade) throws IOException {
		this(p, q, data, facade, DEFAULT_WARP_SIZE);
	}
	
	public void finalize() {
		clReleaseProgram(program);
		clReleaseKernel(kernel);	
	}
	
	@Override
	public double[] value(double[][] points) {
		int nrPoints = points.length;
		int pointsDim = points[0].length;
		
		if (nrPoints % warpSize != 0) {
			throw new IllegalArgumentException("PRECOND violation: nrPoints % warpSize != 0");
		}
		if (warpSize > facade.getMaxWorkGroupSize()) {
			throw new IllegalArgumentException("PRECOND violation: warpSize > facade.getMaxWorkGroupSize()");
		}
		
		float[] fPoints = double2dimToFloat1Dim(points);
		float[] fData = double1dimToFloat1Dim(data, data.length);
		float[] fH = double1dimToFloat1Dim(h, tStar);		
		float[] fResult = new float[nrPoints];
		
		cl_context context = facade.getContext();
						
		// allocate buffers
		cl_mem pBuf = facade.createIntArgBuffer(p);
		cl_mem qBuf = facade.createIntArgBuffer(q);
		cl_mem pointsBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fPoints.length, Pointer.to(fPoints), null);		
		cl_mem dataBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fData.length, Pointer.to(fData), null);
		cl_mem hBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fH.length, Pointer.to(fH), null);		
		cl_mem resBuf = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
	            Sizeof.cl_float * nrPoints, null, null);
		cl_mem dataLenBuf = facade.createIntArgBuffer(data.length);
		cl_mem pointsDimBuf = facade.createIntArgBuffer(pointsDim);
		cl_mem tStarBuf = facade.createIntArgBuffer(tStar);
		
		// set kernel arguments
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(pBuf));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(qBuf));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(dataLenBuf));
		clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(dataBuf));
		clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(pointsDimBuf));
		clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(pointsBuf));
		clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to(tStarBuf));
		clSetKernelArg(kernel, 7, Sizeof.cl_mem, Pointer.to(hBuf));
		clSetKernelArg(kernel, 8, Sizeof.cl_mem, Pointer.to(resBuf));

		// Set the work-item dimensions
		long local_work_size[] = new long[]{warpSize};
		long global_work_size[] = new long[]{nrPoints};
		
		// Execute the kernel
		clEnqueueNDRangeKernel(facade.getCommandQueue(), kernel, 1, null,
				global_work_size, local_work_size, 0, null, null);

		// read result
		clEnqueueReadBuffer(facade.getCommandQueue(), resBuf, CL_TRUE, 0,
				nrPoints * Sizeof.cl_float, Pointer.to(fResult), 0, null, null);
		
		// deallocate memory
		clReleaseMemObject(pBuf);
		clReleaseMemObject(qBuf);		
		clReleaseMemObject(dataLenBuf);
		clReleaseMemObject(dataBuf);
		clReleaseMemObject(pointsDimBuf);
		clReleaseMemObject(pointsBuf);
		clReleaseMemObject(tStarBuf);
		clReleaseMemObject(hBuf);
		clReleaseMemObject(resBuf);
		return float1dimToDouble1Dim(fResult);
	}
	
	private double[] float1dimToDouble1Dim(float[] fResult) {
		double[] res = new double[fResult.length];
		for (int i=0;i<fResult.length;i++) {
			res[i] = fResult[i];
		}
		return res;
	}

	private float[] double1dimToFloat1Dim(double[] points, int nr) {
		float[] res = new float[nr];
		for (int i=0;i<nr;i++) {
			res[i] = (float) points[i];
		}
		return res;
	}

	private float[] double2dimToFloat1Dim(double[][] points) {
		int nrPoints = points.length;
		int sizePoint = points[0].length;

		float[] res = new float[nrPoints * sizePoint];
		for (int i=0;i<nrPoints;i++) {
			for (int j=0;j<sizePoint;j++) {
				res[i*sizePoint+j] = (float) points[i][j];
			}
		}
		return res;
	}


}
