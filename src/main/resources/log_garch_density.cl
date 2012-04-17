__kernel void log_garch_density(
		__global int *pp, __global int *pq,
		__global int *pnrData, __global const float * data,
		__global int *ppointsDim, __global const float * points,
		__global int* ptStar,
		__global const float * h,
		__global float * dst)
{
	int p = *pp;
	int q = *pq;
	int nrData = *pnrData;
	int pointsDim = *ppointsDim;
	int tStar = *ptStar;
	
	int pIndex = get_global_id(0);

	__global const float *point = points + (pointsDim * pIndex);

	float pSum = 0.0f;

	for (int i=0;i<pointsDim;i++) {
		if (point[i] < 0.0f) {
			dst[pIndex] = -INFINITY;
			return;
		}
		pSum += point[i];
	}
	if (pSum >= 1.0f) {
		dst[pIndex] = -INFINITY;
		return;
	}

	float sigmaSq = 1.0f;
	for (int i=0;i<pointsDim;i++) {
		sigmaSq -= point[i];
	}

	float lnF = 0.0;

	float hArr[32]; /* maximum q size 32 */
	for (int i=0;i<tStar;i++) {
		hArr[i] = h[i];
	}

	for (int t=tStar;t<nrData;t++) {
		float alphaSum = 0.0f;
		for (int i=0;i<p;i++) {
			alphaSum += point[i] * data[t-i-1] * data[t-i-1];
		}

		float betaSum = 0.0;
		for (int j=0;j<q;j++) {
			betaSum += point[p+j] * hArr[q-j-1];
		}
		
		float ht = sigmaSq+alphaSum+betaSum;

		for (int i=1;i<q;i++) {
			hArr[i-1] = hArr[i];
		}
	
		hArr[q-1] = ht;

		lnF -= 0.5 * native_log(2.0f * 3.141592654f * ht);
		lnF -= 0.5 * (data[t] * data[t]) / ht;
	}

	dst[pIndex] = lnF;
}
