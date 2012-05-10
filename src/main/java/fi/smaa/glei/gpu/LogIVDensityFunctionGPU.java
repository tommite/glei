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
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;

import fi.smaa.glei.LogIVDensityFunction;

public class LogIVDensityFunctionGPU extends LogIVDensityFunction {
	
	public static final String KERNEL_FILENAME = "log_iv_density.cl";
	public static final String KERNEL_FUNCNAME = "log_iv_density";
	private OpenCLFacade facade;
	private cl_program program;
	private long warpSize;
	
	protected LogIVDensityFunctionGPU(double[] y, double[] x, double[][] z, OpenCLFacade facade, long warpSize) throws IOException {
		super(y, x, z);
		this.facade = facade;
		this.warpSize = warpSize;
		
		program = facade.buildProgram(KERNEL_FILENAME);
	}
	
	public LogIVDensityFunctionGPU(double[]y, double[] x, double[][] z, OpenCLFacade facade) throws IOException {
		this(y, x, z, facade, facade.getMaxWorkGroupSize());
	}
	
	public void finalize() {
		clReleaseProgram(program);
	}
	
	@Override
	public double[] value(double[][] points) {
		
		cl_kernel kernel = clCreateKernel(program, KERNEL_FUNCNAME, null);
		
		int nrPoints = points.length;
		
		if (nrPoints % warpSize != 0) {
			throw new IllegalArgumentException("PRECOND violation: nrPoints % warpSize != 0");
		}
		if (warpSize > facade.getMaxWorkGroupSize()) {
			throw new IllegalArgumentException("PRECOND violation: warpSize > facade.getMaxWorkGroupSize()");
		}
		
		float[] fPoints = GPUHelper.double2dimToFloat1Dim(points);
		float[] fX = GPUHelper.double1dimToFloat1Dim(x, x.length);
		float[] fY = GPUHelper.double1dimToFloat1Dim(y, y.length);
		float[] fZ = GPUHelper.double2dimToFloat1Dim(z);
		float[] fResult = new float[nrPoints];		
		
		cl_context context = facade.getContext();
						
		// allocate buffers
		cl_mem yBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fY.length, Pointer.to(fY), null);
		cl_mem xBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fX.length, Pointer.to(fX), null);
		cl_mem zBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fZ.length, Pointer.to(fZ), null);
		cl_mem pointsBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fPoints.length, Pointer.to(fPoints), null);		
		cl_mem resBuf = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
	            Sizeof.cl_float * nrPoints, null, null);
		
		// set kernel arguments
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(yBuf));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(xBuf));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(zBuf));
		clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{y.length}));
		clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{z[0].length}));
		clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(pointsBuf));
		clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to(resBuf));
		
		// Set the work-item dimensions
		long local_work_size[] = new long[]{warpSize};
		long global_work_size[] = new long[]{nrPoints};
		
		cl_command_queue queue = facade.getCommandQueue();
		// Execute the kernel
		clEnqueueNDRangeKernel(queue, kernel, 1, null,
				global_work_size, local_work_size, 0, null, null);

		// read result
		clEnqueueReadBuffer(queue, resBuf, CL_TRUE, 0,
				nrPoints * Sizeof.cl_float, Pointer.to(fResult), 0, null, null);
		
		// deallocate memory
		clReleaseMemObject(xBuf);
		clReleaseMemObject(yBuf);
		clReleaseMemObject(zBuf);				
		clReleaseMemObject(pointsBuf);
		clReleaseMemObject(resBuf);
		
		clReleaseKernel(kernel);
			
		return GPUHelper.float1dimToDouble1Dim(fResult);
	}

}
