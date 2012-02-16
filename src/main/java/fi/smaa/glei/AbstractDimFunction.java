package fi.smaa.glei;


public abstract class AbstractDimFunction implements Function {

	private int dim;

	public AbstractDimFunction(int dim) {
		this.dim = dim;
	}
	
	public int dimension() {
		return dim;
	}
	
	public double value(double[] point) throws IllegalArgumentException {
		if (dim != point.length) {
			throw new IllegalArgumentException("Incorrect number of components in input");
		}
		return evaluate(point);
	}

	protected abstract double evaluate(double[] point);
}
