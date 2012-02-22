package fi.smaa.glei;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.math.Mult;
import cern.jet.math.PlusMult;



public class ImportanceSampler implements MultiDimensionalSampler {

	public static final double EPSILON = 1E-30;
	
	private Function lnG;
	private Function lnF;
	private MultiOutputFunction H;
	private int nrDraws;
	private MultiDimensionalSampler gSampler;

	private double[] lnFis;
	private double[] lnGis;
	private double[][] thetaIs;

	/**
	 * Constructs a new sampler.
	 * 
	 * PRECOND: lnG.dimension() == lnF.dimension() == H.dimension()
	 * 
	 * @param lnG natural logarithm of the importance density g
	 * @param lnF natural logarithm of the posterior density f
	 * @param H the objective functions
	 * @param nrDraws the number of draws
	 */
	public ImportanceSampler(Function lnG, Function lnF, MultiOutputFunction H, int nrDraws, MultiDimensionalSampler gSampler) {
		if (lnG.dimension() != lnF.dimension() || lnF.dimension() != H.inputDimension()) {
			throw new IllegalArgumentException("PRECOND violation: functions with differing dimensions");
		}
		this.lnG = lnG;
		this.lnF = lnF;
		this.H = H;
		this.nrDraws = nrDraws;
		this.gSampler = gSampler;
		
		lnFis = new double[nrDraws];
		lnGis = new double[nrDraws];
		thetaIs = new double[nrDraws][0];
	}
			
	public double[] sample() throws SamplingException {
		DoubleMatrix1D hTheta = DoubleFactory1D.dense.make(H.returnDimension(), 0.0);
		
		double w = 0.0;

		int M = getNrDraws();
		boolean nonZeroFound = false;

		double maxLnF = Double.NEGATIVE_INFINITY;
		
		for (int i=0;i<M;i++) {
			double[] thetaI = gSampler.sample().clone();
			lnGis[i] = lnG.value(thetaI);
			lnFis[i]= lnF.value(thetaI);
			thetaIs[i] = thetaI;
			if (lnFis[i] > maxLnF) {
				maxLnF = lnFis[i];
			}
		}

		// substract maxLnF from all lnFis
		for (int i=0;i<M;i++) {
			lnFis[i] -= maxLnF;
		}	
		
		for (int i=0;i<M;i++) {
			double lnFi = lnFis[i];
			double lnGi = lnGis[i];
			double[] thetaI = thetaIs[i];
			double wi = Math.exp(lnFi - lnGi);
			DoubleMatrix1D hi = DoubleFactory1D.dense.make(H.value(thetaI));
			hTheta.assign(hi, PlusMult.plusMult(wi));
			w += wi;
			if (!nonZeroFound && !withinEpsilon(wi, 0.0)) {
				nonZeroFound = true;
			}
		}
		if (!nonZeroFound) {
			throw new SamplingException("Need more draws or a better candidate logG");
		}
		return hTheta.assign(Mult.div(w)).toArray();
	}

	public int getNrDraws() {
		return nrDraws;
	}

	private boolean withinEpsilon(double n1, double n2) {
		return Math.abs(n1 - n2) < EPSILON; 
	}
}
