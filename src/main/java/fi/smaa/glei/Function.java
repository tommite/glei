package fi.smaa.glei;


public interface Function {
	
	/**
	 * Evaluates the function at points at.
	 * 
	 * @param at Points to evaluate the function at. PRECOND: at.length == dimension()
	 * @return Function evaluations
	 */
	double[] value(double[][] at);
	
	/**
	 * Gives dimension of this function
	 * 
	 * @return Function dimension, i.e. the number of input parameters
	 */
	int dimension();
}
