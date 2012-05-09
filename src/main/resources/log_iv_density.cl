__kernel void log_iv_density(
		__global const float *pY,
		__global const float *pX,
		__global const float *pZ,
		int sizeRows,
		int sizeZcols,
		__global const float *points,
		__global float * dst)
{	
	int pIndex = get_global_id(0);

	__global const float *point = points + (sizeZcols * pIndex);

	dst[pIndex] = 2.0;
}
