/*
 * This file is part of glei.
 * Copyright (C) 2012 Tommi Tervonen.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
