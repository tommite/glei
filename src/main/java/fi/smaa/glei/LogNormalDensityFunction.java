package fi.smaa.glei;


public class LogNormalDensityFunction extends AbstractDimFunction {
	
	private double mean;
	private double varsq;

	public LogNormalDensityFunction(double mean, double var) {
		super(1);
		this.mean = mean;
		this.varsq = var * var;
	}


	public double evaluate(double[] point) {
		double res = point[0] - mean;
		res *= res;
		res /= varsq;
		
		return -0.5 * res;
	}
}
