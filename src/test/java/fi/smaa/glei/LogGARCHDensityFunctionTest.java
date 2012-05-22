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

package fi.smaa.glei;

import org.junit.Assert;
import org.junit.Test;


public class LogGARCHDensityFunctionTest {

	@Test
	public void testValueWithNalansExample() {
		int p = 3;
		int q = 2;
		double[] data = new double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[] pt = new double[] {0.1, 0.2, 0.3, 0.0, 0.1};
		
		
		LogGARCHDensityFunction fn = new LogGARCHDensityFunction(p, q, data);
		Assert.assertArrayEquals(new double[]{-3.730027}, fn.value(new double[][]{pt}), 0.0001);
	}
	
	@Test
	public void testInvalidAlphaBeta() {
		int p = 1;
		int q = 1;
		double[] data = new double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[] pt = new double[] {-1.0, 0.0};
		
		LogGARCHDensityFunction fn = new LogGARCHDensityFunction(p, q, data);
		Assert.assertArrayEquals(new double[]{Double.NEGATIVE_INFINITY}, fn.value(new double[][]{pt}), 0.0000001);
	}
}
