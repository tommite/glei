package fi.smaa.glei;



public class ImportanceSampler {

	public static final double EPSILON = 1E-15; // appropriate?
	
	private Function lnG;
	private Function lnF;
	private Function H;
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
	public ImportanceSampler(Function lnG, Function lnF, Function H, int nrDraws, MultiDimensionalSampler gSampler) {
		if (lnG.dimension() != lnF.dimension() || lnF.dimension() != H.dimension()) {
			throw new IllegalArgumentException("PRECOND violation: functions with differing dimensions");
		}
		this.lnG = lnG;
		this.lnF = lnF;
		this.H = H;
		this.nrDraws = nrDraws;
		this.gSampler = gSampler;
	}
			
	public double sample() throws SamplingException {
		double hTheta = 0.0;
		double w = 0.0;

		int M = getNrDraws();
		boolean nonZeroFound = false;

		for (int i=0;i<M;i++) {
			double[] thetaI = gSampler.sample();
			double lnGi = lnG.value(thetaI);
			double lnFi = lnF.value(thetaI);
			double wi = Math.exp(lnFi - lnGi);
			double hi = H.value(thetaI);
			hTheta += (hi * wi);
			w += wi;
			if (!nonZeroFound && !withinEpsilon(wi, 0.0)) {
				nonZeroFound = true;
			}
		}
		if (!nonZeroFound) {
			throw new SamplingException("Need more draws or a better candidate logG");
		}
		return hTheta / w;
	}

	public int getNrDraws() {
		return nrDraws;
	}

	private boolean withinEpsilon(double n1, double n2) {
		return Math.abs(n1 - n2) < EPSILON; 
	}
}
