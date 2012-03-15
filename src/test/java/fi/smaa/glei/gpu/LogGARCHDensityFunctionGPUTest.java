package fi.smaa.glei.gpu;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class LogGARCHDensityFunctionGPUTest {
	
	private static OpenCLFacade fac;

	@BeforeClass
	public static void setUpSuite() {
		fac = new OpenCLFacade();
	}

	@Test
	public void testValueWithNalansExample() throws IOException {
		int p = 3;
		int q = 2;
		double[] data = new double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[][] pt = new double[][] {{0.1, 0.2, 0.3, 0.0, 0.1}};
		
		
		LogGARCHDensityFunctionGPU fn = new LogGARCHDensityFunctionGPU(p, q, data, fac, 1);
		double[] res = fn.value(pt);
		Assert.assertArrayEquals(new double[]{-3.730027}, res, 0.0001);
	}
	
	@Test
	public void testInvalidAlphaBeta() throws IOException {
		int p = 1;
		int q = 1;
		double[] data = new double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[] pt = new double[] {-1.0, 0.0};
		
		LogGARCHDensityFunctionGPU fn = new LogGARCHDensityFunctionGPU(p, q, data, fac, 1);
		Assert.assertArrayEquals(new double[]{Double.NEGATIVE_INFINITY}, fn.value(new double[][]{pt}), 0.0000001);
	}
}
