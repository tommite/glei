package fi.smaa.glei;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.math.PlusMult;

public class LogIVDensityFunction extends AbstractDimFunction {

	private int T;
	private double[] x;
	private double[] y;
	private double[][] z;
	private static final int k = 2; // bi-variate normal density
	// TODO: ask Nalan whether only bi-variate densities are allowed (k=2)
	private static final double BIVARDEN1STTERM = -(k / 2.0) * Math.log(2.0 * Math.PI);

	/**
	 * 
	 * @param dataY
	 * @param dataX
	 * @param dataZ has to be a T x 2 matrix (bi-variate?) 
	 */
	public LogIVDensityFunction(double[] dataY, double[] dataX, double[][] dataZ) {
		super(6); // 6 parameters in the points to be estimated: beta, Phi, omega11, omega22, rho
		T = dataY.length;
		if (dataX.length != T || dataZ.length != T) {
			throw new IllegalArgumentException("dataY, dataX, and dataZ have to be of same dimensionality");
		}
		if (dataZ[0].length != 2) {
			throw new IllegalArgumentException("cannot handle other than bi-variate normal densities");
		}
		this.x = dataX;
		this.y = dataY;
		this.z = dataZ;
	}

	/**
	 * Evaluates in the given point, that has all the components concatenated.
	 * 
	 * @param point to evaluate in (beta, Phi1, Phi2, omega11, omega22, rho)
	 */
	@Override	
	protected double evaluateSingle(double[] point) {
		double beta = point[0];
		double Phi1 = point[1];
		double Phi2 = point[2];
		double omega11 = point[3];
		double omega22 = point[4];
		double rho = point[5];
		
		if (omega11 <= 0.0 || omega22 <= 0.0 || rho < -1.0 || rho > 1.0) {
			return Double.NEGATIVE_INFINITY;
		}
		
		// compute large omega_12 and_21, omega determinant, and omega inverse
		double Om121 =  rho * Math.sqrt(omega11 * omega22);
		double omegaDet = (omega11 * omega22) - (Om121 * Om121);
		double omegaInv11 = omega22 / omegaDet;
		double omegaInv121 = -Om121 / omegaDet;
		double omegaInv22 = omega11 / omegaDet;
		
		double res = (-3.0 / 2.0) * Math.log(omegaDet);
		
		for (int i=0;i<T;i++) {
			assert(z[i].length == k); // sanity check
			
			double mean1 = y[i] - x[i] * beta;
			double mean2 = x[i] - (z[i][0] * Phi1 + z[i][1] * Phi2);
			
			double toAdd = bivariateLogNormalDensity(omegaDet,
					DoubleFactory2D.dense.make(new double[][]{
						{omegaInv11, omegaInv121},
						{omegaInv121, omegaInv22}
					}),
					DoubleFactory2D.dense.make(new double[][]{{mean1, mean2}}),
					DoubleFactory2D.dense.make(new double[][]{{0.0, 0.0}}));
			res += toAdd;
		}
		
		return res;
	}

	public static double bivariateLogNormalDensity(double omegaDet, DoubleMatrix2D omegaInv,
			DoubleMatrix2D mean, DoubleMatrix2D y) {
				
		DoubleMatrix2D ym = y.assign(mean, PlusMult.minusMult(1.0));
		
		double bivarfirst2terms = BIVARDEN1STTERM - 0.5 * Math.log(omegaDet);
		DoubleMatrix2D mults = ym.zMult(omegaInv, null, 1.0, 1.0, false, false);
		DoubleMatrix2D matres = mults.zMult(ym, null, 1.0, 1.0, false, true);
		
		return bivarfirst2terms - 0.5 * matres.get(0, 0);
	}

}
