package fi.smaa.glei.r;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import fi.smaa.glei.EqualFunction;
import fi.smaa.glei.Function;
import fi.smaa.glei.ImportanceSampler;
import fi.smaa.glei.LogGARCHDensityFunction;
import fi.smaa.glei.MultiDimensionalSampler;
import fi.smaa.glei.MultiOutputFunction;
import fi.smaa.glei.MultivariateStudentTSampler;
import fi.smaa.glei.SamplingException;
import fi.smaa.glei.StudentTLogDensityFunction;


public class GARCHRFacade {

	public static double[] importanceSample(int p, int q, int nrDraws, int dof, double[] data, double[] mu, double[] sigmaVec, int nrowSigma) throws SamplingException {
		RandomEngine rnd = new MersenneTwister(0x667);
		MultiOutputFunction H = new EqualFunction(mu.length);
		DoubleMatrix2D sigmaM = RHelper.rArrayMatrixToMatrix2D(sigmaVec, nrowSigma);
		DoubleMatrix1D muV = DoubleFactory1D.dense.make(mu);
		Function lnF = new LogGARCHDensityFunction(p, q, data);
		Function lnG = new StudentTLogDensityFunction(muV, sigmaM, dof);
		MultiDimensionalSampler gSampler = new MultivariateStudentTSampler(muV, sigmaM, dof, rnd);
		ImportanceSampler sampler = new ImportanceSampler(lnG, lnF, H, nrDraws, gSampler);
		return sampler.sample();
	}
}
