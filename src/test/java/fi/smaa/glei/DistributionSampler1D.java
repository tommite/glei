package fi.smaa.glei;

import cern.jet.random.AbstractDistribution;

public class DistributionSampler1D implements MultiDimensionalSampler {

	private AbstractDistribution dist;

	public DistributionSampler1D(AbstractDistribution dist) {
		this.dist = dist;
	}

	public double[][] sample(int nrDraws) throws SamplingException {
		double[][] res = new double[nrDraws][1];
		for (int i=0;i<nrDraws;i++) {
			res[i][0] = dist.nextDouble();
		}
		return res;
	}

}
