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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;


public class OpenCLFacadeTest {
	
	private OpenCLFacade f;

	@Before
	public void setUp() {
		f = OpenCLFacade.getInstance();
	}

	@Test
	public void testGetNrDevices() {
		assertTrue(f.getNrDevices() >= 0);
	}
	
	@Test
	public void testLoadProgram() throws IOException {
		assertNotNull(f.loadProgram("log_garch_density.cl"));
	}
	
	@Test
	public void testBuildProgram() throws IOException {
		assertNotNull(f.buildProgram("log_garch_density.cl"));
	}
	
	@Test
	public void testInfos() {
		System.out.println("OpenCL global mem size " + f.getGlobalMemSize());
		System.out.println("OpenCL max mem alloc size " + f.getMaxMemAllocSize());
		System.out.println("OpenCL max work group size " + f.getMaxWorkGroupSize());		
	}
	
}
