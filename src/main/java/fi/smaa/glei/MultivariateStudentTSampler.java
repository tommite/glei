package fi.smaa.glei;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.jet.random.ChiSquare;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class MultivariateStudentTSampler implements MultiDimensionalSampler{

	private DoubleMatrix2D sigmaChol;
	private DoubleMatrix1D thetaStdnorm;
	private int p;
	private DoubleMatrix1D mu;
	private Normal normal01Sampler;
	private ChiSquare chisqSampler; 
	private double[] lastSample;
	private double n;

	/**
	 * 
	 * @param sigma p x p matrix. PRECOND: positive definite
	 * @param mu p length vector
	 * @param n degrees of freedom. PRECOND: > 0
	 * @param rnd the random number generator engine
	 */
	public MultivariateStudentTSampler(DoubleMatrix2D sigma, DoubleMatrix1D mu, int n, RandomEngine rnd) {
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
		lastSample = new double[p];
	}
	
	public double[] sample() {
		for (int i=0;i<p;i++) {
			thetaStdnorm.set(i, normal01Sampler.nextDouble());
		}
		DoubleMatrix1D thetaNorm = sigmaChol.zMult(thetaStdnorm, null, 1.0, 1.0, true);
		
		for (int i=0;i<p;i++) {
			double thetaChi = chisqSampler.nextDouble();
			lastSample[i] = thetaNorm.get(i) / Math.sqrt(thetaChi / n) + mu.get(i);
		}
		return lastSample;
	}
}
