package fi.smaa.glei;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.math.Mult;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.stat.Descriptive;

public class MultivariateStudentTSamplerTest {

	@Test
	public void testSampleWithNalansTest() {
		DoubleMatrix2D sigma = DoubleFactory2D.dense.make(new double[][] {
				{2., 1.},
				{1., 1.}
		});
		sigma.assign(Mult.div(100.0)); // * 1/100
		DoubleMatrix1D mu = DoubleFactory1D.dense.make(new double[]{-3., 1.});
		int n = 10;
		int N = 1000;
		double[][] samples = new double[2][1000];
		
		MultivariateStudentTSampler s = new MultivariateStudentTSampler(sigma, mu, n, new MersenneTwister());
		for (int i=0;i<N;i++) {
			double[] sample = s.sample();
			samples[0][i] = sample[0];
			samples[1][i] = sample[1];
		}
		DoubleArrayList v1 = new DoubleArrayList(samples[0]);
		DoubleArrayList v2 = new DoubleArrayList(samples[1]);
		
		double v1mean = Descriptive.mean(v1);
		assertEquals(-3.0, v1mean, 0.3);
		double v2mean = Descriptive.mean(v2);
		assertEquals(1.0, v2mean, 0.1);
		assertEquals(0.02, Descriptive.sampleVariance(v1, v1mean), 0.01);
		assertEquals(0.01, Descriptive.sampleVariance(v2, v2mean), 0.01);
		assertEquals(0.01, Descriptive.covariance(v1, v2), 0.005);
	}
}
