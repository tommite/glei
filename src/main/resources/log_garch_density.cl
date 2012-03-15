
__kernel void log_garch_density(
		__global int* pnrData, __global const float * data,
		__global int* ppointsDim, __global const float * points,
		__global float * dst)
{
	int nrData = *pnrData;
	int pointsDim = *ppointsDim;
	int gid = get_global_id(0);
}