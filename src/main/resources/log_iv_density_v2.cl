__kernel void log_iv_density_v2(
		__global const float *y,
		__global const float *x,
		__global const float *z,
		int sizeZcols,
		__global float *dst,		
		__global const float *point,
		float omegaInv11,
		float omegaInv121,
		float omegaInv22)
{
	int i = get_global_id(0);
	
	float beta = point[0];
	float mean1 = y[i] - x[i] * beta;
	float mean2 = x[i];
	
	for (int j=0;j<sizeZcols;j++) {
		mean2 -= point[j+1] * z[i*sizeZcols+j];
	}
	/* write open the 2 matrix multiplications */
	float mults = ((mean1  * omegaInv11 + mean2 * omegaInv121) * mean1) +
			((mean1 * omegaInv121 + mean2 * omegaInv22) * mean2);
	dst[i] = -0.5 * mults;
}
