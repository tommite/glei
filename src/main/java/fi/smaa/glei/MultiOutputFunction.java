package fi.smaa.glei;

public interface MultiOutputFunction {
	
	/**
	 * Evaluates the function at point at.
	 * 
	 * @param at Point to evaluate the function at. PRECOND: at.length == dimension()
	 * @return Function evaluation
	 */
	double[] value(double[] at);
	
	/**
	 * Gives input dimension of this function
	 * 
	 * @return Function input dimension, i.e. the number of input parameters
	 */
	int inputDimension();
	
	/**
	 * Gives return value dimension
	 * 
	 * @return return value dimension, i.e. the number of output parameters
	 */
	int returnDimension();
	
}
