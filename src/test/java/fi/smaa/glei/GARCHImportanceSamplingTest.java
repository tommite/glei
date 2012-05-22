/*
 * This file is part of glei.
 * Copyright (C) 2012 Tommi Tervonen.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
		Assert.assertArrayEquals(new double[]{0.6920489}, res[0], 0.2);
		Assert.assertArrayEquals(new double[]{0.1108320}, res[1], 0.2);		
	}
	
	@Test
	public void testNalansTestWithGPU() throws SamplingException, IOException {
		OpenCLFacade facade = OpenCLFacade.getInstance();		
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
		int M = ((Mclose / OpenCLFacade.DEFAULT_WARP_SIZE)+1) * OpenCLFacade.DEFAULT_WARP_SIZE;
		ImportanceSampler sampler = new ImportanceSampler(lngtheta, lnftheta, H, M, Theta);
		
		double[][] res = sampler.sample(M, true);
		Assert.assertArrayEquals(new double[]{0.6920489}, res[0], 0.2);
		Assert.assertArrayEquals(new double[]{0.1108320}, res[1], 0.2);		
	}
}
