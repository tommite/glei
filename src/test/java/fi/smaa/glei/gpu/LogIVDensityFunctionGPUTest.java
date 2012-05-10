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
