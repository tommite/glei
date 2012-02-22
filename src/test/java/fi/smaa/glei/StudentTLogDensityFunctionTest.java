package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class StudentTLogDensityFunctionTest {

	@Test
	public void testEvaluateWithNalansAwesomeExample() {		
		DoubleMatrix2D sigma = DoubleFactory2D.dense.make(new double[][] {
				{2., 1.},
				{1., 1.}
		});
		
		DoubleMatrix1D mu = DoubleFactory1D.dense.make(new double[]{-3., 1.});
		int n = 10;

		StudentTLogDensityFunction fnc = new StudentTLogDensityFunction(mu, sigma, n);
		
		double[] theta1 = new double[]{0., 0.};
		double[] theta2 = new double[]{1., 2.};
		
		assertEquals(-1.800628, fnc.value(theta1) - fnc.value(theta2), 0.00001);
	}
}
