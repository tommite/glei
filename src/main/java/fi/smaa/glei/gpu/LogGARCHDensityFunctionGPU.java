package fi.smaa.glei.gpu;

import java.io.IOException;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

import static org.jocl.CL.*;

import fi.smaa.glei.LogGARCHDensityFunction;

public class LogGARCHDensityFunctionGPU extends LogGARCHDensityFunction {
	
	private static final String KERNEL_FILENAME = "log_garch_density.cl";
	private static final String KERNEL_FUNCNAME = "log_garch_density";
	private OpenCLFacade facade;
	private cl_kernel kernel;

	public LogGARCHDensityFunctionGPU(int p, int q, double[] data, OpenCLFacade facade) throws IOException {
		super(p, q, data);
		this.facade = facade;
		kernel = facade.buildKernel(KERNEL_FILENAME, KERNEL_FUNCNAME);
	}
	
	@Override
	public double[] value(double[][] points) {
		int nrPoints = points.length;
		int pointsDim = points[0].length;
		float[] fPoints = double2dimToFloat1Dim(points);
		float[] fData = double1dimToFloat1Dim(data);
		float[] fResult = new float[nrPoints];
		
		cl_context context = facade.getContext();
				
		// allocate buffers
		cl_mem pointsBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fPoints.length, Pointer.to(fPoints), null);		
		cl_mem dataBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fData.length, Pointer.to(fData), null);
		cl_mem resBuf = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
	            Sizeof.cl_float * nrPoints, null, null);		

		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(facade.createIntArgBuffer(data.length)));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(dataBuf));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(facade.createIntArgBuffer(pointsDim)));
		clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(pointsBuf));
		clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(resBuf));

		// Set the work-item dimensions
		long global_work_size[] = new long[]{nrPoints};
		long local_work_size[] = new long[]{1};

		// Execute the kernel
		clEnqueueNDRangeKernel(facade.getCommandQueue(), kernel, 1, null,
				global_work_size, local_work_size, 0, null, null);

		// read result
		clEnqueueReadBuffer(facade.getCommandQueue(), resBuf, CL_TRUE, 0,
				nrPoints * Sizeof.cl_float, Pointer.to(fResult), 0, null, null);
		
		return float1dimToDouble1Dim(fResult);
	}
	
	private double[] float1dimToDouble1Dim(float[] fResult) {
		double[] res = new double[fResult.length];
		for (int i=0;i<fResult.length;i++) {
			res[i] = fResult[i];
		}
		return res;
	}

	private float[] double1dimToFloat1Dim(double[] points) {
		int nrPoints = points.length;

		float[] res = new float[nrPoints];
		for (int i=0;i<nrPoints;i++) {
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
