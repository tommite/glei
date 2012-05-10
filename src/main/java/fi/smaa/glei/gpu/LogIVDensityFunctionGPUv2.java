/*
 * This file is part of glei.
 * Copyright (C) 2011-12 Tommi Tervonen.
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
	private int blockSize;

	public static final int BLOCK_SIZE = 16;
	private int appendedRows = 0;

	public LogIVDensityFunctionGPUv2(double[] dataY, double[] dataX, double[][] dataZ, OpenCLFacade facade, int blockSize) throws IOException {
		super(dataY, dataX, dataZ);
		this.blockSize = blockSize;
		this.facade = facade;
		if (blockSize > facade.getMaxWorkGroupSize()) {
			throw new IllegalArgumentException("PRECOND violation: warpSize > facade.getMaxWorkGroupSize()");
		}
		// if data is not exactly correct row sizes, append new rows
		appendedRows = dataY.length % blockSize;
		if (appendedRows != 0) {
			appendedRows = blockSize - appendedRows;
		}
		appendEmptyRowsTodata();
		initCLStuff();
	}

	public LogIVDensityFunctionGPUv2(double[] dataY, double[] dataX, double[][] dataZ, OpenCLFacade facade) throws IOException {
		this(dataY, dataX, dataZ, facade, BLOCK_SIZE);
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
		// set kernel arguments
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(yBuf));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(xBuf));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(zBuf));
		clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{z[0].length}));
		clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{z.length}));
	}

	@Override
	public double[] value(double[][] points) {
		int nrPoints = points.length;
		int nrData = y.length;

		if (nrPoints % blockSize != 0) {
			throw new IllegalArgumentException("PRECOND violation: nrPoints % blockSize != 0");
		}
		if (blockSize > facade.getMaxWorkGroupSize()) {
			throw new IllegalArgumentException("PRECOND violation: blockSize > facade.getMaxWorkGroupSize()");
		}

		float[] fPoints = GPUHelper.double2dimToFloat1Dim(points);		

		cl_context context = facade.getContext();

		// allocate buffers
		cl_mem pointsBuf = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
				Sizeof.cl_float * fPoints.length, Pointer.to(fPoints), null);		
		cl_mem resBuf = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
				Sizeof.cl_float * nrPoints * nrData, null, null);

		// set kernel arguments
		clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(pointsBuf));
		clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to(resBuf));

		// Set the work-item dimensions
		long local_work_size[] = new long[]{blockSize, blockSize};
		long global_work_size[] = new long[]{nrData, nrPoints};

		// Execute the kernel
		clEnqueueNDRangeKernel(facade.getCommandQueue(), kernel, 2, null,
				global_work_size, local_work_size, 0, null, null);

		// read result
		float[] fResult = new float[nrPoints*nrData];		
		clEnqueueReadBuffer(facade.getCommandQueue(), resBuf, CL_TRUE, 0,
				nrPoints * nrData * Sizeof.cl_float, Pointer.to(fResult), 0, null, null);

		// deallocate memory
		clReleaseMemObject(pointsBuf);
		clReleaseMemObject(resBuf);
		
		double[] finRes = new double[nrPoints]; 
		for (int i=0;i<nrPoints;i++) {
			double[] point = points[i];
			double omega11 = point[point.length-3];
			double omega22 = point[point.length-2];
			double rho = point[point.length-1];			
			// compute large omega_12 and_21, omega determinant, and omega inverse
			double Om121 =  rho * Math.sqrt(omega11 * omega22);
			double omegaDet = (omega11 * omega22) - (Om121 * Om121);
			double res = (-3.0f / 2.0f) * Math.log(omegaDet);

			if (omega11 <= 0.0f || omega22 <= 0.0f || rho < -1.0f || rho > 1.0f) {
				res = Double.NEGATIVE_INFINITY;
			} else {
				for (int j=0;j<nrData-this.appendedRows;j++) {
					float gpuRes = fResult[i * nrData + j];
					res += gpuRes;
				}
			}
			finRes[i] = res;
		}

		return finRes;
	}

}
