package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LogNormalDensityFunctionTest {
	
	@Test
	public void testEvaluate() {
		LogNormalDensityFunction density = new LogNormalDensityFunction(2.0, 3.0);
		assertEquals(-0.05555556, density.value(new double[]{1.0}), 0.0001);
	}
}
