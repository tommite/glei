package fi.smaa.glei;


public interface Function {
	
	/**
	 * Evaluates the function at point at.
	 * 
	 * @param at Point to evaluate the function at. PRECOND: at.length == dimension()
	 * @return Function evaluation
	 */
	double value(double[] at);
	
	/**
	 * Gives dimension of this function
	 * 
	 * @return Function dimension, i.e. the number of input parameters
	 */
	int dimension();
}
