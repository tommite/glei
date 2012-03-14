package fi.smaa.glei;

public interface MultiDimensionalSampler {
	
	public double[][] sample(int nrDraws) throws SamplingException;
	
}
