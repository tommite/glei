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

package fi.smaa.glei.gpu;

public class GPUHelper {

	public static double[] float1dimToDouble1Dim(float[] fResult) {
		double[] res = new double[fResult.length];
		for (int i=0;i<fResult.length;i++) {
			res[i] = fResult[i];
		}
		return res;
	}

	public static float[] double1dimToFloat1Dim(double[] points, int nr) {
		float[] res = new float[nr];
		for (int i=0;i<nr;i++) {
			res[i] = (float) points[i];
		}
		return res;
	}

	public static float[] double2dimToFloat1Dim(double[][] points) {
		int nrPoints = points.length;
		int sizePoint = points[0].length;
	
		float[] res = new float[nrPoints * sizePoint];
		for (int i=0;i<nrPoints;i++) {
			for (int j=0;j<sizePoint;j++) {
				res[i*sizePoint+j] = (float) points[i][j];
			}
		}
		return res;
	}

}
