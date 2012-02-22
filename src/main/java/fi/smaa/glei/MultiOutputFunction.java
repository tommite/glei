package fi.smaa.glei;

public interface MultiOutputFunction {
	
	double[] value(double[] at);
	int dimension();
	int returnDimension();
	
}
