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

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogIVDensityFunctionGPUTest {
	
	private static OpenCLFacade fac;

	@BeforeClass
	public static void setUpSuite() {
		fac = OpenCLFacade.getInstance();
	}
	
	@Test
	public void testNalansExample() throws IOException {
		double[] y = new double[] {1.0, 2.0, 3.0};
		double[] x = new double[] {0.0, 1.0, 2.0};
		double[][] z = new double[][] {
				{ 1.0, 2.0 },
				{ 2.0, 1.0 },
				{ 3.0, 3.0 }};
		
		// evaluate at (beta, Phi1, Phi2, omega11, omega22, rho)
		double[][] point = new double[][] {{1.0, 0.0, 2.0, 1.0, 2.0, 0.5}};
		
		LogIVDensityFunctionGPU iv = new LogIVDensityFunctionGPU(y, x, z, fac, 1);
		Assert.assertArrayEquals(new double[]{-23.9727}, iv.value(point), 0.001);
	}
}
