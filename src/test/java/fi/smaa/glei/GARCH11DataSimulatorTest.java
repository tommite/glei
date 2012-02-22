package fi.smaa.glei;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister;

public class GARCH11DataSimulatorTest {

	@Test
	public void testLengthDataAndNonNaN() {
		GARCH11DataSimulator s = new GARCH11DataSimulator(0.0, 0.2, 0.2, 0.8, 0.0, new MersenneTwister(), 300, 100);
		assertEquals(300, s.getY().length);
		assertEquals(300, s.getH().length);
		assertFalse(new Double(s.getY()[0]).equals(Double.NaN));
		assertFalse(new Double(s.getH()[0]).equals(Double.NaN));
	}
}
