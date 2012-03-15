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
	}
	
	public double[][] sample(int nrDraws) throws SamplingException {
		return sample(nrDraws, false);
	}
			
	public double[][] sample(int nrDraws, boolean printTimes) throws SamplingException {
		DoubleMatrix1D hTheta = DoubleFactory1D.dense.make(H.returnDimension(), 0.0);
		
		double w = 0.0;

		int M = getNrDraws();
		boolean nonZeroFound = false;

		long t1 = System.currentTimeMillis();
		double[][] thetaIs = gSampler.sample(M);
		long t2 = System.currentTimeMillis();		
		double[] lnGis = lnG.value(thetaIs);
		long t3 = System.currentTimeMillis();		
		double[] lnFis= lnF.value(thetaIs);
		long t4 = System.currentTimeMillis();
		if (printTimes) {
			System.out.println("Times (ms): thetaIs sampling " + (t2-t1) + " lnG " + (t3-t2) + " lnF " + (t4-t3));
		}

		double maxLnF = Double.NEGATIVE_INFINITY;		
		
		for (int i=0;i<M;i++) {
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
		double[] res1Dim = hTheta.assign(Mult.div(w)).toArray();
		double[][] res = new double[res1Dim.length][1];
		for (int i=0;i<res1Dim.length;i++) {
			res[i][0] = res1Dim[i];
		}
		return res;
	}

	public int getNrDraws() {
		return nrDraws;
	}

	private boolean withinEpsilon(double n1, double n2) {
		return Math.abs(n1 - n2) < EPSILON; 
	}
}
