package fi.smaa.glei;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import fi.smaa.glei.gpu.LogGARCHDensityFunctionGPU;
import fi.smaa.glei.gpu.OpenCLFacade;

public class GARCHImportanceSamplingTest {

	@Test
	public void testNalansTest() throws SamplingException {
		RandomEngine engine = new MersenneTwister(0x667);
		double alpha = 0.8, beta = 0.0;
		double sigma2 = 1.0 - alpha - beta;
		int p = 1, q = 1;
		int T = 300;
		int k = p + q;
		int M = (int) 1e6;
		double y0 = 0.0;
		double h0 = sigma2;
		GARCH11DataSimulator sim = new GARCH11DataSimulator(y0, h0, sigma2, alpha,
				beta, engine, T, 100);
		
		DoubleMatrix1D mode = DoubleFactory1D.dense.make(new double[]{0.5, 0.5});
		DoubleMatrix2D sigma = DoubleFactory2D.dense.make(new double[][]{
				{1.0, 0.0},
				{0.0, 1.0}});
		int df = 10;
		MultivariateStudentTSampler Theta = new MultivariateStudentTSampler(mode, sigma, df, engine);
		StudentTLogDensityFunction lngtheta = new StudentTLogDensityFunction(mode, sigma, df);
		LogGARCHDensityFunction lnftheta = new LogGARCHDensityFunction(p, q, sim.getY());
		EqualFunction H = new EqualFunction(k);
		ImportanceSampler sampler = new ImportanceSampler(lngtheta, lnftheta, H, M, Theta);
		
		double[][] res = sampler.sample(M, true);
		Assert.assertArrayEquals(res[0], new double[]{0.6920489}, 0.2);
		Assert.assertArrayEquals(res[1], new double[]{0.1108320}, 0.2);		
	}
	
	@Test
	public void testNalansTestWithGPU() throws SamplingException, IOException {
		OpenCLFacade facade = new OpenCLFacade();		
		RandomEngine engine = new MersenneTwister(0x667);
		double alpha = 0.8, beta = 0.0;
		double sigma2 = 1.0 - alpha - beta;
		int p = 1, q = 1;
		int T = 300;
		int k = p + q;
		double y0 = 0.0;
		double h0 = sigma2;
		GARCH11DataSimulator sim = new GARCH11DataSimulator(y0, h0, sigma2, alpha,
				beta, engine, T, 100);
		
		DoubleMatrix1D mode = DoubleFactory1D.dense.make(new double[]{0.5, 0.5});
		DoubleMatrix2D sigma = DoubleFactory2D.dense.make(new double[][]{
				{1.0, 0.0},
				{0.0, 1.0}});
		int df = 10;
		MultivariateStudentTSampler Theta = new MultivariateStudentTSampler(mode, sigma, df, engine);
		StudentTLogDensityFunction lngtheta = new StudentTLogDensityFunction(mode, sigma, df);
		LogGARCHDensityFunctionGPU lnftheta = new LogGARCHDensityFunctionGPU(p, q, sim.getY(), facade);
		EqualFunction H = new EqualFunction(k);
		int Mclose = (int) 1e6;
		int M = ((Mclose / LogGARCHDensityFunctionGPU.DEFAULT_WARP_SIZE)+1) * LogGARCHDensityFunctionGPU.DEFAULT_WARP_SIZE;
		ImportanceSampler sampler = new ImportanceSampler(lngtheta, lnftheta, H, M, Theta);
		
		double[][] res = sampler.sample(M, true);
		Assert.assertArrayEquals(res[0], new double[]{0.6920489}, 0.2);
		Assert.assertArrayEquals(res[1], new double[]{0.1108320}, 0.2);		
	}
}
