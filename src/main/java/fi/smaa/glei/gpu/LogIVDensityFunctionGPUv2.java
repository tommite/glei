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

import fi.smaa.glei.LogIVDensityFunction;

public class LogIVDensityFunctionGPUv2 extends LogIVDensityFunction {
	
	public static final String KERNEL_FILENAME = "log_iv_density_v2.cl";
	public static final String KERNEL_FUNCNAME = "log_iv_density_v2";
	private OpenCLFacade facade;
	private cl_program program;
	private cl_kernel kernel;
	private cl_mem yBuf;
	private cl_mem xBuf;
	private cl_mem zBuf;
	private int warpSize;
	private cl_mem resBuf;
	
	public static final int BLOCK_SIZE = 1;
	private int appendedRows = 0;

	public LogIVDensityFunctionGPUv2(double[] dataY, double[] dataX, double[][] dataZ, OpenCLFacade facade, int warpSize) throws IOException {
		super(dataY, dataX, dataZ);
		this.warpSize = warpSize;
		this.facade = facade;
		if (dataY.length % warpSize != 0) {
			throw new IllegalArgumentException("PRECOND violation: dataY.length % warpSize != 0");
		}
		if (warpSize > facade.getMaxWorkGroupSize()) {
			throw new IllegalArgumentException("PRECOND violation: warpSize > facade.getMaxWorkGroupSize()");
		}
		initCLStuff();
	}
	
	public LogIVDensityFunctionGPUv2(double[] dataY, double[] dataX, double[][] dataZ, OpenCLFacade facade) throws IOException {
		super(dataY, dataX, dataZ);
		this.facade = facade;		
		warpSize = facade.getMaxWorkGroupSize();
		// if data is not exactly correct row sizes, append new rows
		appendedRows = dataY.length % warpSize;
		if (appendedRows != 0) {
			appendedRows = warpSize - appendedRows;
		}
		appendEmptyRowsTodata();		
		initCLStuff();
	}
	
	public int getAppendedRows() {
		return appendedRows;
	}
		
	private void appendEmptyRowsTodata() {
		x = appendRows(x);
		y = appendRows(y);
		double[][] newZ = new double[z.length + appendedRows][z[0].length];
		for (int i=0;i<z.length;i++) {
			for (int j=0;j<z[0].length;j++) {
				newZ[i][j] = z[i][j];
			}
		}
		z = newZ;
	}

	private double[] appendRows(double[] dataY) {
		double[] newY = new double[dataY.length + appendedRows];
		for (int i=0;i<dataY.length;i++) {
			newY[i] = dataY[i];
		}
		return newY;
	}

	public void finalize() {
		clReleaseMemObject(xBuf);
		clReleaseMemObject(yBuf);
		clReleaseMemObject(zBuf);
		clReleaseMemObject(resBuf);		
		clReleaseProgram(program);
		clReleaseKernel(kernel);
	}
	
	private void initCLStuff() throws IOException {
		program = facade.buildProgram(KERNEL_FILENAME);
		kernel = clCreateKernel(program, KERNEL_FUNCNAME, null);		
		float[] fY = GPUHelper.double1dimToFloat1Dim(y, y.length);		
		float[] fX = GPUHelper.double1dimToFloat1Dim(x, x.length);
		float[] fZ = GPUHelper.double2dimToFloat1Dim(z);

		cl_context context = facade.getContext();
		yBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fY.length, Pointer.to(fY), null);
		xBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fX.length, Pointer.to(fX), null);
		zBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fZ.length, Pointer.to(fZ), null);
		resBuf = clCreateBuffer(facade.getContext(), CL_MEM_WRITE_ONLY,
	            Sizeof.cl_float * fY.length, null, null);		
		// set kernel arguments
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(yBuf));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(xBuf));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(zBuf));
		clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{z[0].length}));
		clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(resBuf));		
	}

	@Override
	protected double iterateOverData(double[] point,
			double omegaInv11, double omegaInv121, double omegaInv22,
			double bivarfirst2terms) {
		int T = y.length;
		// alloc point buffer
		float[] fPoint = GPUHelper.double1dimToFloat1Dim(point, point.length);
		cl_mem pointBuf = clCreateBuffer(facade.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fPoint.length, Pointer.to(fPoint), null);
		
		// set changing arguments
		clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(pointBuf));
		clSetKernelArg(kernel, 6, Sizeof.cl_float, Pointer.to(new float[]{(float) omegaInv11}));		
		clSetKernelArg(kernel, 7, Sizeof.cl_float, Pointer.to(new float[]{(float) omegaInv121}));		
		clSetKernelArg(kernel, 8, Sizeof.cl_float, Pointer.to(new float[]{(float) omegaInv22}));
		
		// Set the work-item dimensions
		long local_work_size[] = new long[]{warpSize};
		long global_work_size[] = new long[]{T};
				
		// Execute the kernel
		clEnqueueNDRangeKernel(facade.getCommandQueue(), kernel, 1, null,
				global_work_size, local_work_size, 0, null, null);

		// read result
		float[] fResult = new float[T];
		clEnqueueReadBuffer(facade.getCommandQueue(), resBuf, CL_TRUE, 0,
				T * Sizeof.cl_float, Pointer.to(fResult), 0, null, null);
						
		double result = 0.0;
		for (int i=0;i<(fResult.length-appendedRows);i++) {
			result += ((double) fResult[i] + bivarfirst2terms);
		}
		
		// dealloc buffers
		clReleaseMemObject(pointBuf);		
		
		return result;
	}	

}
