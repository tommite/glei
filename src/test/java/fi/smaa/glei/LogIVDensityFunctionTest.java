package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.LUDecomposition;

public class LogIVDensityFunctionTest {

	@Test
	public void testNalansTest1() {
		double[] y = new double[] {1.0};
		double[] x = new double[] {0.0};
		double[][] z = new double[][] {
				{ 1.0, 2.0 }};
		
		// evaluate at (beta, Phi1, Phi2, omega11, omega22, rho)
		double[] point = new double[] {1.0, 0.0, 2.0, 1.0, 1.0, 0.0};
		
		LogIVDensityFunction iv = new LogIVDensityFunction(y, x, z);
		assertEquals(-10.3379, iv.evaluateSingle(point), 0.001);
	}

	@Test
	public void testNalansTest2() {
		double[] y = new double[] {1.0, 2.0, 3.0};
		double[] x = new double[] {0.0, 1.0, 2.0};
		double[][] z = new double[][] {
				{ 1.0, 2.0 },
				{ 2.0, 1.0 },
				{ 3.0, 3.0 }};
		
		// evaluate at (beta, Phi1, Phi2, omega11, omega22, rho)
		double[] point = new double[] {1.0, 0.0, 2.0, 1.0, 2.0, 0.5};
		
		LogIVDensityFunction iv = new LogIVDensityFunction(y, x, z);
		assertEquals(-23.9727, iv.evaluateSingle(point), 0.001);
	}
	
	@Test
	public void testBivariateLognormalDensity() {
		DoubleMatrix2D mu = DoubleFactory2D.dense.make(new double[][]{
				{0.0, 1.0}
		});
		DoubleMatrix2D y = DoubleFactory2D.dense.make(new double[][]{
				{1.0, 2.0}
		});
		
		DoubleMatrix2D omega = DoubleFactory2D.dense.make(new double[][]{
				{1.0, 0.5},
				{0.5, 2.0}
		});
		DoubleMatrix2D omegaInv = new Algebra().inverse(omega);
		double res = LogIVDensityFunction.bivariateLogNormalDensity(new LUDecomposition(omega).det(), omegaInv, mu, y);
		assertEquals(-2.689114, res, 0.00001);
	}
}
