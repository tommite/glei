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
