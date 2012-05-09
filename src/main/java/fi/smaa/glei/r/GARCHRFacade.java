package fi.smaa.glei.r;

import java.io.IOException;

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
import fi.smaa.glei.gpu.LogGARCHDensityFunctionGPU;
import fi.smaa.glei.gpu.OpenCLFacade;


public class GARCHRFacade {

	public static double[] importanceSample(int p, int q, int nrDraws, int dof, double[] data, double[] mu, double[] sigmaVec, int nrowSigma,
			int useGPU) throws SamplingException {
		RandomEngine rnd = new MersenneTwister(0x667);
		MultiOutputFunction H = new EqualFunction(mu.length);
		DoubleMatrix2D sigmaM = RHelper.rArrayMatrixToMatrix2D(sigmaVec, nrowSigma);
		DoubleMatrix1D muV = DoubleFactory1D.dense.make(mu);
		
		Function lnF = null;
		if (useGPU == 0) {
			lnF = new LogGARCHDensityFunction(p, q, data);
		} else if (useGPU == 1) {
			OpenCLFacade facade = new OpenCLFacade();
			try {
				lnF = new LogGARCHDensityFunctionGPU(p, q, data, facade);
			} catch (IOException e) {
				throw new SamplingException("Cannot load GPU kernel code: " + e.getMessage());
			}
		}
		Function lnG = new StudentTLogDensityFunction(muV, sigmaM, dof);
		MultiDimensionalSampler gSampler = new MultivariateStudentTSampler(muV, sigmaM, dof, rnd);
		ImportanceSampler sampler = new ImportanceSampler(lnG, lnF, H, nrDraws, gSampler);
		double[][] res = sampler.sample(nrDraws);
		
		double[] dim1res = new double[res.length];
		for (int i=0;i<dim1res.length;i++) {
			dim1res[i] = res[i][0];
		}
		return dim1res;
	}
	
	public static int getWarpSize() {
		return new OpenCLFacade().getMaxWorkGroupSize();
	}
}
