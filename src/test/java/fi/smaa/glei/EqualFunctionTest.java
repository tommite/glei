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
