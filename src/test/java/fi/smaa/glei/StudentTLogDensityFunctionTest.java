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

package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class StudentTLogDensityFunctionTest {

	@Test
	public void testEvaluateWithNalansAwesomeExample() {		
		DoubleMatrix2D sigma = DoubleFactory2D.dense.make(new double[][] {
				{2., 1.},
				{1., 1.}
		});
		
		DoubleMatrix1D mu = DoubleFactory1D.dense.make(new double[]{-3., 1.});
		int n = 10;

		StudentTLogDensityFunction fnc = new StudentTLogDensityFunction(mu, sigma, n);
		
		double[] theta1 = new double[]{0., 0.};
		double[] theta2 = new double[]{1., 2.};
		
		assertEquals(-1.800628, fnc.value(new double[][]{theta1})[0] - fnc.value(new double[][]{theta2})[0], 0.00001);
	}
}
