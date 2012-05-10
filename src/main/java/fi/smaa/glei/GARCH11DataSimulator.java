/*
 * This file is part of glei.
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

package fi.smaa.glei;

import java.util.Arrays;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class GARCH11DataSimulator {

	private Normal normal;
	private double[] h;
	private double[] y;

	//	##### SIMULATE DATA FROM GARCH(1,1) MODEL
	//	#	for t in 1:T
	//	#		eps_t ~ N(0,1)
	//	#		h_t = alpha0 + alpha1 * y_(t-1)^2 + beta1 * h_{t-1}
	//	#		y_t = sqrt(h_t) eps_t
	//	#		ntrans: number of simulations to throw away - gets rid of the initial value problem, not necessary
	//	#	returns: list(data y , conditional variance h), both vectors size T
	//	fn.simGARCH11<-function(y0,h0,alpha0,alpha1,beta1,T,ntrans = 100){
	//		# simulate eps_t for all t
	//		Tall = ntrans +T
	//		eps <- rnorm(Tall,0,1)
	//		# calculate h_t & y_t  for t=1 
	//		## has to be changed for general GARCH(p,q) model
	//		h  <- alpha0 + alpha1 * y0^2 + beta1 * h0
	//		y  <- sqrt(h[1]) * eps[1]
	//		# calculate h_t for t=2:Tall
	//		if(Tall>1){
	//		for (t in 2:Tall){
	//			h[t]  <- alpha0 + alpha1 * (y[t-1]^2) + beta1 * h[t-1]
	//			y[t]  <- sqrt(h[t]) * eps[t]
	//		}}
	//		return(list(h=h[(ntrans+1):Tall],y=y[(ntrans+1):Tall]))
	//	}

	public GARCH11DataSimulator(double y0, double h0, double alpha0, double alpha1, double beta1, RandomEngine eng, int nrPoints) {
		this.normal = new Normal(0.0, 1.0, eng);
		y = new double[nrPoints];
		h = new double[nrPoints];
		simulate(y0, h0, alpha0, alpha1, beta1, nrPoints);
	}
	
	public GARCH11DataSimulator(double y0, double h0, double alpha0, double alpha1, double beta1, RandomEngine eng, int nrPoints, int burnIn) {
		this(y0, h0, alpha0, alpha1, beta1, eng, nrPoints+burnIn);
		y = Arrays.copyOfRange(y, burnIn, y.length);
		h = Arrays.copyOfRange(h, burnIn, h.length);
	}

	private void simulate(double y0, double h0, double alpha0, double alpha1, double beta1, int nrPoints) {		
		double prevY = y0;
		double prevH = h0;
		for (int i=0;i<nrPoints;i++) {
			h[i] = alpha0 + (alpha1 * Math.pow(prevY, 2.0)) + (beta1 * prevH);
			prevH = h[i];
			y[i] = Math.sqrt(prevH) * normal.nextDouble();
			prevY = y[i];
		}
	}
	
	public double[] getY() {
		return y;
	}
	
	public double[] getH() {
		return h;
	}
}
