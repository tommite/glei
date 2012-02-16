package fi.smaa.glei;


public class EqualFunction1D extends AbstractDimFunction {
	
	public EqualFunction1D() {
		super(1);
	}

	public double evaluate(double[] point) {
		return point[0];
	}

}
