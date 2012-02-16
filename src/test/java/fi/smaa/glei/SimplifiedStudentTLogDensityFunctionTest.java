package fi.smaa.glei;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class SimplifiedStudentTLogDensityFunctionTest {

	@Test
	public void testEvaluateWithNalansAwesomeExample() {
		double[] tv = new double[8];
		
		tv[0] = performTest(0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1);
		tv[1] = performTest(1.0, 2.0, 1.0, 0.0, 0.0, 1.0, 1);
		tv[2] = performTest(0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 2);
		tv[3] = performTest(1.0, 2.0, 1.0, 0.0, 0.0, 1.0, 2);
		tv[4] = performTest(0.0, 0.0, 2.0, 1.0, 1.0, 1.0, 1);
		tv[5] = performTest(1.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1);
		tv[6] = performTest(0.0, 0.0, 2.0, 1.0, 1.0, 1.0, 2);
		tv[7] = performTest(1.0, 2.0, 2.0, 1.0, 1.0, 1.0, 2);
		
		double sum = 0.0;
		for (int i=0;i<tv.length;i++) {
			sum += tv[i];
		}
		
		for (int i=0;i<tv.length;i++) {
			tv[i] /= sum;
		}
		
		System.out.println(Arrays.toString(tv));
		
		Assert.assertArrayEquals(new double[]{0.11, 0.15, 0.10, 0.14, 0.15, 0.11, 0.14, 0.10}, tv, 0.01);
	}

	private double performTest(double mu1, double mu2, double s1, double s2, double s3, double s4, int n) {
		DoubleMatrix2D sigma = DoubleFactory2D.dense.make(new double[][] {
				{s1, s2},
				{s3, s4}
		});
		
		DoubleMatrix1D mu = DoubleFactory1D.dense.make(new double[]{mu1, mu2});
		SimplifiedStudentTLogDensityFunction fnc = new SimplifiedStudentTLogDensityFunction(mu, sigma, n);

		return fnc.value(new double[]{-1.0, 1.0});
		
	}
}
