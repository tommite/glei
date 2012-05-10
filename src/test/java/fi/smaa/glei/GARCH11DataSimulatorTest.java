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
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister;

public class GARCH11DataSimulatorTest {

	@Test
	public void testLengthDataAndNonNaN() {
		GARCH11DataSimulator s = new GARCH11DataSimulator(0.0, 0.2, 0.2, 0.8, 0.0, new MersenneTwister(), 300, 100);
		assertEquals(300, s.getY().length);
		assertEquals(300, s.getH().length);
		assertFalse(new Double(s.getY()[0]).equals(Double.NaN));
		assertFalse(new Double(s.getH()[0]).equals(Double.NaN));
	}
}
