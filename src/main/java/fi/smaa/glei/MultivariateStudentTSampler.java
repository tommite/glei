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

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.jet.random.ChiSquare;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class MultivariateStudentTSampler implements MultiDimensionalSampler {

	private DoubleMatrix2D sigmaChol;
	private DoubleMatrix1D thetaStdnorm;
	private int p;
	private DoubleMatrix1D mu;
	private Normal normal01Sampler;
	private ChiSquare chisqSampler; 
	private double n;

	/**
	 * 
	 * @param mu p length vector
	 * @param sigma p x p matrix. PRECOND: positive definite
	 * @param n degrees of freedom. PRECOND: > 0
	 * @param rnd the random number generator engine
	 */
	public MultivariateStudentTSampler(DoubleMatrix1D mu, DoubleMatrix2D sigma, int n, RandomEngine rnd) {
		p = mu.size();
		if (n < 1) {
			throw new IllegalArgumentException("PRECOND violation: n < 1");
		}
		if (sigma.rows() != p || sigma.columns() != p) {
			throw new IllegalArgumentException("PRECOND violation: sigma not square and size of mu");
		}
		CholeskyDecomposition chol = new CholeskyDecomposition(sigma);
		if (!chol.isSymmetricPositiveDefinite()) {
			throw new IllegalArgumentException("PRECOND violation: sigma not symmetric, positive definite");
		}
		this.sigmaChol = chol.getL();
		this.mu = mu;
		this.n = n;
		thetaStdnorm = DoubleFactory1D.dense.make(p);
		normal01Sampler = new Normal(0.0, 1.0, rnd);
		chisqSampler = new ChiSquare((double) n, rnd);
	}
	
	public double[][] sample(int nrDraws) {
		double[][] res = new double[nrDraws][p];
		for (int i=0;i<nrDraws;i++) {
			sampleSingle(res[i]);
		}
		return res;
	}
	
	private void sampleSingle(double[] dest) {
		for (int i=0;i<p;i++) {
			thetaStdnorm.set(i, normal01Sampler.nextDouble());
		}
		DoubleMatrix1D thetaNorm = sigmaChol.zMult(thetaStdnorm, null, 1.0, 1.0, true);
		
		for (int i=0;i<p;i++) {
			double thetaChi = chisqSampler.nextDouble();
			dest[i] = thetaNorm.get(i) / Math.sqrt(thetaChi / n) + mu.get(i);
		}
	}
}
