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
