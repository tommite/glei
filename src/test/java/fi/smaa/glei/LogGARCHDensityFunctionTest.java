package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class LogGARCHDensityFunctionTest {

	@Test
	public void testValueWithNalansExample() {
		int p = 3;
		int q = 2;
		double[] data = new double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[] pt = new double[] {0.1, 0.2, 0.3, 0.0, 0.1};
		
		
		LogGARCHDensityFunction fn = new LogGARCHDensityFunction(p, q, data);
		assertEquals(-3.730027, fn.value(pt), 0.0001);
	}
	
	@Test
	public void testInvalidAlphaBeta() {
		int p = 1;
		int q = 1;
		double[] data = new double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[] pt = new double[] {-1.0, 0.0};
		
		LogGARCHDensityFunction fn = new LogGARCHDensityFunction(p, q, data);
		assertEquals(Double.NEGATIVE_INFINITY, new Double(fn.value(pt)), 0.0000001);
	}
}