/*
 * This file is part of libror.
 * libror is distributed from http://smaa.fi/libror
 * Copyright (C) 2011-12 Tommi Tervonen.
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

package fi.smaa.glei.r;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class RHelper {

	public static DoubleMatrix2D rArrayMatrixToMatrix2D(double[] matrix, int nRows) {
		assert(nRows > 0);
		int nCols = matrix.length / nRows;
		assert(matrix.length == nRows * nCols);
		DoubleMatrix2D perfMatrix = DoubleFactory2D.dense.make(nRows, nCols);
		for (int i=0;i<nRows;i++) {
			for (int j=0;j<nCols;j++) {
				perfMatrix.set(i, j, matrix[i*nCols + j]);
			}
		}
		return perfMatrix;
	}

}
