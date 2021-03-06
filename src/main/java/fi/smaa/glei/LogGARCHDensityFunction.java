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

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;



public class LogGARCHDensityFunction extends AbstractDimFunction {

	protected int p;
	protected int q;
	protected double[] data;
	protected int tStar;
	protected double[] h;

	/**
	 * 
	 * @param p length of the alpha-vector. PRECOND: > 0
	 * @param q length of the beta-vector. PRECOND: > 0
	 * @param data
	 */
	public LogGARCHDensityFunction(int p, int q, double[] data) {
		super(p+q);
		if (p < 1 || q < 1) {
			throw new IllegalArgumentException("PRECOND violation: p or q not positive");
		}
		this.p = p;
		this.q = q;
		this.data = data;
		tStar = Math.max(p, q);
		initHt();
	}

	private void initHt() {
		h = new double[data.length];
		
		DoubleArrayList datalist = new DoubleArrayList(data);
		double sampleVariance = Descriptive.sampleVariance(datalist, Descriptive.mean(datalist));
		for (int i=0;i<tStar;i++) {
			h[i] = sampleVariance;
		}
	}

	/**
	 * Evaluate s.t. point has first the alpha-components and then the beta-ones (concatenated).
	 */
	protected double evaluateSingle(double[] point) {
		double pSum = 0.0;
		for (int i=0;i<point.length;i++) {
			if (point[i] < 0.0) {
				return Double.NEGATIVE_INFINITY;
			}
			pSum += point[i];
		}
		if (pSum >= 1.0) {
			return Double.NEGATIVE_INFINITY;
		}
		double sigmaSq = 1.0;
		for (int i=0;i<point.length;i++) {
			sigmaSq -= point[i];
		}
		for (int t=tStar;t<data.length;t++) {
			double alphaSum = 0.0;
			for (int i=0;i<p;i++) {
				alphaSum += point[i] * Math.pow(data[t-i-1], 2.0); 
			}
			double betaSum = 0.0;
			for (int j=0;j<q;j++) {
				betaSum += point[p+j] * h[t-j-1];
			}
			h[t] = sigmaSq + alphaSum + betaSum;
		}
		
		double lnF = 0.0;
		for (int t=tStar;t<data.length;t++) {
			lnF -= 0.5 * Math.log(2.0 * Math.PI * h[t]);
			lnF -= 0.5 * (Math.pow(data[t], 2.0) / h[t]);
		}
		return lnF;
	}

}
