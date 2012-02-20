package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;


public class LogGARCHDensityFunctionTest {

	@Test
	@Ignore
	public void testValueWithNalansExample() {
		int p = 3;
		int q = 2;
		double[] data = new double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[] pt = new double[] {0.1, 0.2, 0.3, 0.0, 0.1};
		
		
		LogGARCHDensityFunction fn = new LogGARCHDensityFunction(p, q, data);
		assertEquals(-3.609036, fn.value(pt), 0.0001);
	}
}
