__kernel void log_iv_density(
		__global const float *y,
		__global const float *x,
		__global const float *z,
		int sizeRows,
		int sizeZcols,
		__global const float *points,
		__global float * dst)
{
	int pIndex = get_global_id(0);
	int pointSize = sizeZcols + 4;

	__global const float *point = points + (pointSize * pIndex);

	float beta = point[0];
	float omega11 = point[pointSize-3];
	float omega22 = point[pointSize-2];
	float rho = point[pointSize-1];

	float BIVARDEN1STTERM = -(2.0f / 2.0f) * native_log(2.0f * 3.141592654f);

	if (omega11 <= 0.0f || omega22 <= 0.0f || rho < -1.0f || rho > 1.0f) {
		dst[pIndex] = -INFINITY;
		return;
	}

	/* compute large omega_12 and_21, omega determinant, and omega inverse */
	float Om121 =  rho * native_sqrt(omega11 * omega22);
	float omegaDet = (omega11 * omega22) - (Om121 * Om121);
	float omegaInv11 = omega22 / omegaDet;
	float omegaInv121 = -Om121 / omegaDet;
	float omegaInv22 = omega11 / omegaDet;

	float res = (-3.0f / 2.0f) * native_log(omegaDet);

	float bivarfirst2terms = BIVARDEN1STTERM - 0.5f * native_log(omegaDet);		

	for (int i=0;i<sizeRows;i++) {

		float mean1 = y[i] - x[i] * beta;
		float mean2 = x[i];
		for (int j=0;j<sizeZcols;j++) {
			mean2 -= point[j+1] * z[i*sizeZcols+j];
		}

		/* write open the 2 matrix multiplications */
		float mults = ((mean1  * omegaInv11 + mean2 * omegaInv121) * mean1) +
				((mean1 * omegaInv121 + mean2 * omegaInv22) * mean2);
		float dens = bivarfirst2terms - 0.5 * mults;
		res += dens;
	}

	dst[pIndex] = res;
}
