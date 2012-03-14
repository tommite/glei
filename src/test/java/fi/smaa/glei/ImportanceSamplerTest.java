package fi.smaa.glei;

import org.junit.Assert;
import org.junit.Test;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

public class ImportanceSamplerTest {

	@Test
	public void testWithUnknownNormalSampling() throws SamplingException {
		RandomEngine engine = new MersenneTwister(); // constant seed
		Normal nd = new Normal(0.0, 1.0, engine);
		
		double[] yi = new double[100];
		for (int i=0;i<100;i++) {
			yi[i] = nd.nextDouble();
		}
		
		Function lnF = new LogF(yi);
		Function lnG = new LogNormalDensityFunction(1.0, 10.0);
		MultiOutputFunction H = new EqualFunction(1);

		MultiDimensionalSampler normalOneTenSampler = new 
				DistributionSampler1D(new Normal(1.0, 10.0, engine));
		
		ImportanceSampler samp = new ImportanceSampler(lnF, lnG, H, 2000, normalOneTenSampler);
		Assert.assertArrayEquals(new double[]{0.0}, samp.sample(2000)[0], 0.1);
	}
	
	
	private class LogF extends AbstractDimFunction {
		
		private double[] data;

		public LogF(double[] data) {
			super(1);
			this.data = data;
		}
		
		public double evaluateSingle(double[] at) {
			double point = at[0];
			
			double res = 0.0;
			for (int i=0;i<data.length;i++) {
				double diff = -0.5 * (data[i] - point);
				res += (diff * diff);
			}

			return res;
		}
	}
}
