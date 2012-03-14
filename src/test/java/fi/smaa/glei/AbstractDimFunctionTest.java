package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AbstractDimFunctionTest {
	
	private AbstractDimFunction fnc;

	@Before
	public void setUp() {
		fnc = new AbstractDimFunction(2) {
			@Override
			protected double evaluateSingle(double[] point) {
				return 0;
			}
		};
	}

	@Test
	public void testDimension() {
		assertEquals(2, fnc.dimension());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidValueDim() {
		fnc.value(new double[][]{{1.0}});
	}
}
