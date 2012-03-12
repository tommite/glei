package fi.smaa.glei;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OpenCLFacadeTest {

	@Test
	public void testGetNrDevices() {
		OpenCLFacade f = new OpenCLFacade();
		assertTrue(f.getNrDevices() >= 0);
	}
	
}
