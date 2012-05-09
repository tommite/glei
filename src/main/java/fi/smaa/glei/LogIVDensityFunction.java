package fi.smaa.glei;

import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.math.PlusMult;

public class LogIVDensityFunction extends AbstractDimFunction {

	private int T;
	private double[] x;
	private double[] y;
	private double[][] z;
	private static final double BIVARDEN1STTERM = -(2 / 2.0) * Math.log(2.0 * Math.PI);

	/**
	 * 
	 * @param dataY vector of length T
	 * @param dataX vector of length T
	 * @param dataZ matrix with T rows  
	 */
	public LogIVDensityFunction(double[] dataY, double[] dataX, double[][] dataZ) {
		 // 4+z-dimension parameters in the points to be estimated: beta, Phi's, omega11, omega22, rho
		super(dataZ[0].length + 4);
		T = dataY.length;
		if (dataX.length != T || dataZ.length != T) {
			throw new IllegalArgumentException("dataY, dataX, and dataZ have to be of same dimensionality");
		}
		this.x = dataX;
		this.y = dataY;
		this.z = dataZ;
	}

	/**
	 * Evaluates in the given point, that has all the components concatenated.
	 * 
	 * 4 + z-params
	 * 
	 * @param point to evaluate in (beta, Phi1, Phi2, omega11, omega22, rho)
	 */
	@Override	
	protected double evaluateSingle(double[] point) {
		double beta = point[0];
		double omega11 = point[point.length-3];
		double omega22 = point[point.length-2];
		double rho = point[point.length-1];
		
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
		
		double bivarfirst2terms = BIVARDEN1STTERM - 0.5 * Math.log(omegaDet);		
		
		for (int i=0;i<T;i++) {
			
			double mean1 = y[i] - x[i] * beta;
			double mean2 = x[i];
			for (int j=0;j<this.dimension()-4;j++) {
				mean2 -= point[j+1] * z[i][j];
			}

			// write open the 2 matrix multiplications
			double mults = ((mean1  * omegaInv11 + mean2 * omegaInv121) * mean1) +
					((mean1 * omegaInv121 + mean2 * omegaInv22) * mean2);
			double dens = bivarfirst2terms - 0.5 * mults;
			res += dens;			
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
