package fi.smaa.glei;


public abstract class AbstractDimFunction implements Function {

	private int dim;

	public AbstractDimFunction(int dim) {
		this.dim = dim;
	}
	
	public int dimension() {
		return dim;
	}
	
	public double[] value(double[][] points) throws IllegalArgumentException {
		if (dim != points[0].length) {
			throw new IllegalArgumentException("Incorrect number of components in input");
		}		
		double[] res = new double[points.length];
		for (int i=0;i<res.length;i++) {
			res[i] = evaluateSingle(points[i]);
		}
		return res;
	}

	protected abstract double evaluateSingle(double[] point);
}
