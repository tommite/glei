package fi.smaa.glei;

import cern.jet.random.AbstractDistribution;

public class DistributionSampler1D implements MultiDimensionalSampler {

	private AbstractDistribution dist;
	private double[] lastPoint;

	public DistributionSampler1D(AbstractDistribution dist) {
		this.dist = dist;
		lastPoint = new double[1];
	}

	public double[] sample() throws SamplingException {
		lastPoint[0] = dist.nextDouble();
		return lastPoint;
	}

}
