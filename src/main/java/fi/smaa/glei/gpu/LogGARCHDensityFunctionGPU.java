/*
 * This file is part of glei.
 * Copyright (C) 2012 Tommi Tervonen.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	
	protected LogGARCHDensityFunctionGPU(int p, int q, double[] data, OpenCLFacade facade, long warpSize) throws IOException {
		super(p, q, data);
		this.facade = facade;
		program = facade.buildProgram(KERNEL_FILENAME);
		kernel = clCreateKernel(program, KERNEL_FUNCNAME, null);
		this.warpSize = warpSize;
	}
	
	public LogGARCHDensityFunctionGPU(int p, int q, double[] data, OpenCLFacade facade) throws IOException {
		this(p, q, data, facade, OpenCLFacade.DEFAULT_WARP_SIZE);
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
		
		float[] fPoints = GPUHelper.double2dimToFloat1Dim(points);
		float[] fData = GPUHelper.double1dimToFloat1Dim(data, data.length);
		float[] fH = GPUHelper.double1dimToFloat1Dim(h, tStar);		
		float[] fResult = new float[nrPoints];
		
		cl_context context = facade.getContext();
						
		// allocate buffers
		cl_mem pointsBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fPoints.length, Pointer.to(fPoints), null);		
		cl_mem dataBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fData.length, Pointer.to(fData), null);
		cl_mem hBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * fH.length, Pointer.to(fH), null);		
		cl_mem resBuf = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
	            Sizeof.cl_float * nrPoints, null, null);
		
		// set kernel arguments
		clSetKernelArg(kernel, 0, Sizeof.cl_int, Pointer.to(new int[]{p}));
		clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{q}));
		clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{data.length}));
		clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(dataBuf));
		clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{pointsDim}));
		clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(pointsBuf));
		clSetKernelArg(kernel, 6, Sizeof.cl_int, Pointer.to(new int[]{tStar}));
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
		clReleaseMemObject(dataBuf);
		clReleaseMemObject(pointsBuf);
		clReleaseMemObject(hBuf);
		clReleaseMemObject(resBuf);
			
		return GPUHelper.float1dimToDouble1Dim(fResult);
	}

}
