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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EqualFunctionTest {

	@Test
	public void testEqualFunction1D() throws IllegalArgumentException {
		double[] arr = new double[]{2.0};
		
		assertEquals(2.0, new EqualFunction(1).value(arr)[0], 0.000001);
	}
}
