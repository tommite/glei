package fi.smaa.glei;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.PlusMult;

public class StudentTLogDensityFunction extends AbstractDimFunction {

	private DoubleMatrix2D mu;
	private DoubleMatrix2D sigmaPrime;
	private int dof;
	private DoubleFactory2D matFac = DoubleFactory2D.dense;
	private int p;
	private double minusNPlusPby2;

	/**
	 * Constructs a ln(StudentT) density function that is in a simplified format
	 * suitable for importance sampling (Nalan's Algorithm 3).
	 * 
	 * PRECOND: mu.size() == sigma.columns() == sigma.rows() 
	 *  
	 * @param mu sample mode(length p vector)
	 * @param sigma sample scale (p x p matrix)
	 * @param dof degrees of freedom (n). PRECOND: > 0
	 */
	public StudentTLogDensityFunction(DoubleMatrix1D mu, DoubleMatrix2D sigma, int dof) {
		super(mu.size());
		p = mu.size();
		if (p != sigma.columns() || sigma.columns() != sigma.rows()) {
			throw new IllegalArgumentException("Incorrect dimensionality input");
		}
		if (dof < 1) {
			throw new IllegalArgumentException("Non-positive degrees of freedom");
		}
		Algebra alg = new Algebra();
		this.mu = matFac.make(mu.toArray(), mu.size());
		this.sigmaPrime = alg.inverse(sigma);
		this.dof = dof;
		this.minusNPlusPby2 = (-(dof + p)) / 2.0;
	}

	@Override
	protected double evaluateSingle(double[] point) {
		DoubleMatrix2D theta = matFac.make(point, point.length);
		DoubleMatrix2D thetaMinusMu = theta.copy();
		thetaMinusMu.assign(mu, PlusMult.minusMult(1.0));
		DoubleMatrix2D rightSidePart = thetaMinusMu.zMult(sigmaPrime, null, 1.0, 1.0, true, false);
		DoubleMatrix2D rightSide = rightSidePart.zMult(thetaMinusMu, null, 1.0, 1.0, false, false);
				
		assert(rightSide.columns() == 1 && rightSide.rows() == 1); // sanity check
		
		double rsVal = rightSide.get(0, 0);		
		double inLn = 1.0 + ((1.0 / dof) * rsVal);
		
		return minusNPlusPby2 * Math.log(inLn);
	}

}
