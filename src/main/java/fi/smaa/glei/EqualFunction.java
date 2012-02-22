package fi.smaa.glei;


public class EqualFunction implements MultiOutputFunction {
	
	private int dim;

	public EqualFunction(int dim) {
		this.dim = dim;
	}

	public double[] value(double[] at) {
		return at;
	}

	public int inputDimension() {
		return dim;
	}

	public int returnDimension() {
		return dim;
	}

}
