package eva2.tools;

import java.util.Arrays;

import eva2.server.go.individuals.InterfaceDataTypeDouble;

import wsi.ra.math.RNG;
import wsi.ra.math.Jama.Matrix;
import wsi.ra.math.interpolation.BasicDataSet;
import wsi.ra.math.interpolation.InterpolationException;
import wsi.ra.math.interpolation.SplineInterpolation;

//created at June 27 2006

/**
 * @author Andreas Dr&auml;ger
 */
public class Mathematics {

	public static void main(String args[]) {
		int i = 0;
		double y[] = new double[args.length];
		for (; i < args.length; i++)
			y[i] = Double.parseDouble(args[i]);
		System.out.println(median(y) / 1000);
	}

	/**
	 * Computes the median of a given double vector.
	 *
	 * @param x
	 *                a vector of doubles
	 * @return the median
	 */
	public static double median(double[] x) {
		if (x.length == 1) return x[0];
		Arrays.sort(x);
		if (x.length % 2 == 0) return x[(x.length + 1) / 2];
		return (x[x.length / 2] + x[x.length / 2 + 1]) / 2;
	}

	/**
	 * This method gives a linear interpolation of the function values of the
	 * given argument/function value pairs.
	 *
	 * @param x
	 *                The argument at the point with unknown function value
	 * @param x0
	 *                The argument at the last position with a function value
	 * @param x1
	 *                The argument at the next known fuction value
	 * @param f0
	 *                The function value at the position x0
	 * @param f1
	 *                The function value at the position x1
	 * @return The function value at position x given by linear interpolation.
	 */
	public static double linearInterpolation(double x, double x0, double x1,
			double f0, double f1) {
		if (x1 == x0) return f0;
		return lerp(f0, f1, (x - x0) / (x1 - x0));
	}

	/**
	 * Computes a hyperbolic interpolation of the two point (x0,f0) and (x1,f1).
	 *
	 * @param x
	 * @param x0
	 * @param x1
	 * @param f0
	 * @param f1
	 * @return
	 */
	public static double hyperbolicInterpolation(double x, double x0, double x1,
			double f0, double f1) {
		if (x1 == 0) return lerp(f0, f1, (x - x0) / (-x0));
		double l = lerp(x0 / x1, 1, x);
		if (l == 0) return linearInterpolation(x, x0, x1, f0, f1);
		return lerp(f0, f1, x / l);
	}

//	<<<<<<< .working
//	/**
//	* Computes a spline interpolation of the two point (x0,f0) and (x1,f1).
//	* 
//	* @param x
//	* @param x0
//	* @param x1
//	* @param f0
//	* @param f1
//	* @return If an error with the spline occurs, a linear interpolation will be
//	*         returned.
//	*/
//	/*	public static double splineInterpolation(double x, double x0, double x1,
//	double f0, double f1) {
//	try {
//	double[] t = { x0, x1 }, f = { f0, f1 };
//	SplineInterpolation spline = new SplineInterpolation(new BasicDataSet(t,
//	f, 1));
//	return spline.getY(x);
//	} catch (InterpolationException e) {
//	e.printStackTrace();
//	}
//	return linearInterpolation(x, x0, x1, f0, f1);
//	}*/
//	=======
	/**
	 * Computes a spline interpolation of the two point (x0,f0) and (x1,f1).
	 *
	 * @param x
	 * @param x0
	 * @param x1
	 * @param f0
	 * @param f1
	 * @return If an error with the spline occurs, a linear interpolation will be
	 *         returned.
	 */
	public static double splineInterpolation(double x, double x0, double x1,
			double f0, double f1) {
		try {
			double[] t = {x0, x1}, f = {f0, f1};
			SplineInterpolation spline = new SplineInterpolation(new BasicDataSet(t,
					f, 1));
			return spline.getY(x);
		} catch (InterpolationException e) {
			e.printStackTrace();
		}
		return linearInterpolation(x, x0, x1, f0, f1);
	}

	/**
	 * @param f0
	 * @param f1
	 * @param t
	 * @return
	 */
	private static double lerp(double f0, double f1, double t) {
		return f0 + (f1 - f0) * t;
	}

	/**
	 * Computes the root-Distance function. For example root = 2 gives the
	 * Euclidian Distance.
	 *
	 * @param x
	 *                a vector
	 * @param y
	 *                another vector
	 * @param root
	 *                what kind of distance funktion
	 * @return the distance of x and y
	 * @throws Exception
	 *                 if x and y have different dimensions an exception is
	 *                 thrown.
	 */
	public static double dist(double[] x, double[] y, int root) {
		if (x.length != y.length)
			throw new RuntimeException("The vectors x and y must have the same dimension");
		if (root == 0) throw new RuntimeException("There is no 0-root!");
		double d = 0;
		for (int i = 0; i < x.length; i++)
			d += Math.pow(Math.abs(x[i] - y[i]), root);
		return Math.pow(d, (double) 1 / root);
	}

	/**
	 * Computes the euclidian distance function.
	 *
	 * @param x
	 *                a vector
	 * @param y
	 *                another vector
	 * @param root
	 *                what kind of distance funktion
	 * @return the distance of x and y
	 * @throws Exception
	 *                 if x and y have different dimensions an exception is
	 *                 thrown.
	 */
	public static double euclidianDist(double[] x, double[] y)  {
		if (x.length != y.length)
			throw new RuntimeException("The vectors x and y must have the same dimension");
		double d = 0;
		for (int i = 0; i < x.length; i++)
			d += Math.pow(Math.abs(x[i] - y[i]), 2);
		return Math.sqrt(d);
	}
	
	/**
	 * Computes the relative distance of vector x to vector y. Therefore the
	 * difference of x[i] and y[i] is divided by y[i] for every i. If y[i] is
	 * zero, the default value def is used instead. The sum of these differences
	 * gives the distance function.
	 *
	 * @param x
	 *                A vector
	 * @param y
	 *                The reference vector
	 * @param def
	 *                The default value to be use to avoid division by zero.
	 * @return The relative distance of x to y.
	 * @throws Exception
	 */
	public static double relDist(double[] x, double[] y, double def)
	throws Exception {
		if (x.length != y.length)
			throw new Exception("The vectors x and y must have the same dimension");
		double d = 0;
		for (int i = 0; i < x.length; i++)
			if (y[i] != 0)
				d += Math.pow(((x[i] - y[i]) / y[i]), 2);
			else d += def;
		return d;
	}

	/**
	 * This computes the determinant of the given matrix
	 *
	 * @param matrix
	 * @return The determinant or null if there is no determinant (if the matrix
	 *         is not square).
	 */
	public static double determinant(double[][] matrix) {
		if (matrix == null) return 0;
		if (matrix.length != matrix[0].length) return 0;
		if (matrix.length == 1) return matrix[0][0];
		if (matrix.length == 2)
			return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
		if (matrix.length == 3)
			return matrix[0][0] * matrix[1][1] * matrix[2][2] + matrix[0][1]
			                                                              * matrix[1][2] * matrix[2][0] + matrix[0][2] * matrix[1][0]
			                                                                                                                       * matrix[2][1] - matrix[2][0] * matrix[1][1] * matrix[0][2]
			                                                                                                                                                                                - matrix[2][1] * matrix[1][2] * matrix[0][0] - matrix[2][2]
			                                                                                                                                                                                                                                         * matrix[1][0] * matrix[0][1];

		double det = 0;
		for (int k = 0; k < matrix.length; k++) {
			if (matrix[0][k] != 0) det += matrix[0][k] * adjoint(matrix, 0, k);
		}
		return det;
	}

	/**
	 * This computes the submatrix of the given matrix as a result by scraching
	 * out the row k and the column l.
	 *
	 * @param a
	 * @param k
	 * @param l
	 * @return
	 */
	public static double[][] submatrix(double[][] a, int k, int l) {
		double b[][] = new double[a.length - 1][a[0].length - 1];
		int i, j, m = 0, n = 0;

		for (i = 0; i < a.length; i++) {
			if (i == k) continue;
			for (j = 0; j < a[0].length; j++) {
				if (j == l) continue;
				b[m][n++] = a[i][j];
			}
			m++;
			n = 0;
		}

		return b;
	}

	/**
	 * Computes the adjoint of the matrix element at the position (k, l).
	 *
	 * @param a
	 * @param k
	 * @param l
	 * @return
	 */
	public static double adjoint(double[][] a, int k, int l) {
		return Math.pow(-1, k + l + 2) * determinant(submatrix(a, k, l));
	}

	/**
	 * Computes the full adjoint matrix.
	 *
	 * @param a
	 * @return
	 */
	public static double[][] adjoint(double[][] a) {
		if (a == null) return null;
		if (a.length != a[0].length) return null;
		double[][] b = new double[a.length][a.length];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a.length; j++)
				b[i][j] = adjoint(a, i, j);
		return b;
	}

	/**
	 * Computes the inverse of the given matrix or returns null if there is no
	 * inverse (if the determinant is 0).
	 *
	 * @param a
	 * @return
	 */
	public static double[][] inverse(double[][] a) {
		if (a == null) return null;
		if (a.length != a[0].length) return null;
		double det = determinant(a);

		if (det == 0) return null;
		double[][] b = adjoint(a);
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a.length; j++)
				b[i][j] /= det;
		return b;
	}

	/** This method returns a mean vector from a whole array of vectors.
	 * @param d     d[i] the vectors, d[i][j] the jth coordinate of the ith vector
	 * @return The mean vector.
	 */
	public static double[] getMeanVector(double[][] d) {
		double[] result = new double[d[0].length];
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[i].length; j++) {
				result[j] += d[i][j];
			}
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/((double)d.length);
		}
		return result;
	}

	/**
	 * Subtract vectors returning a new vector c = a - b.
	 * 
	 * @param a
	 * @param b
	 * @return a new vector c = a - b
	 */
	public static double[] vvSub(double[] a, double[] b) {
		double[] result = new double[a.length];
		vvSub(a, b, result);
		return result;
	}

	/**
	 * Subtract vectors returning a new vector c = a - b.
	 * 
	 * @param a
	 * @param b
	 * @return a new vector c = a - b
	 */
	public static void vvSub(double[] a, double[] b, double[] res) {
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] - b[i];
		}
	}
	
	/** 
	 * This method return a vector from a to b
	 * @param a     first vector
	 * @param b     second vectors
	 * @return the vector from a to b
	 */
	public static double[] getVectorFromTo(double[] a, double[] b) {
		return vvSub(b,a);
	}

	/**
	 * Add vectors returning a new vector c = a + b;
	 * @param a
	 * @param b
	 * @return a new vector c = a + b
	 */
	public static double[] vvAdd(double[] a, double[] b) {
		double[] result = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] + b[i];
		}
		return result;
	}

	/**
	 * Add vectors in place setting res = v1 + v2.
	 *
	 * @param v1
	 * @param v2
	 * @return vector addition
	 */
	public static void vvAdd(double[] v1, double[] v2, double[] res) {
		vvAddOffs(v1, 0, v2, 0, res, 0, v1.length);
	}
	
	/**
	 * Add vectors in place setting with an offset within the target
	 * vector, meaning that res[resOffs+i]=v1[v1Offs+i]+v2[v2Offs+i] for i in length.
	 *
	 * @param v1
	 * @param v2
	 * @return vector addition
	 */
	public static void vvAddOffs(double[] v1, int v1Offs, double[] v2, int v2Offs, double[] res, int resOffs, int len) {
		for (int i = 0; i < len; i++)
			res[resOffs+i] = v1[v1Offs+i] + v2[v2Offs+i];
	}

	/**
	 * Add each entry of a vector with a scalar in a new vector.
	 * 
	 * @param s
	 * @param v
	 * @return 
	 */
	public static double[] svAdd (double s, double[] v) {
		double[] res = new double[v.length];
		for (int i = 0; i < v.length; i++) {
			res[i] = v[i] + s;
		}
		return res;
	}
	
	/**
	 * Add vectors scaled: res[i] = s*v[i] + w[i]
	 * 
	 * @param s
	 * @param v
	 * @param w
	 * @return 
	 */
	public static void svvAddScaled(double s, double[] v, double[] w, double[] res) {
		for (int i = 0; i < v.length; i++) {
			res[i] = s*v[i] + w[i];
		}
	}

	/**
	 * Scalar product of two vectors returning sum_i (a_i * b_i).
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double vvMult(double[] a, double[] b) {
		double result = 0;
		for (int i = 0; i < a.length; i++) {
			result += a[i]*b[i];
		}
		return result;
	}
	
	/**
	 * Multiplies (scales) every element of the array v with s returning a new vector.
	 *
	 * @param s
	 *                a scalar
	 * @param v
	 *                an array to be multiplied with s.
	 * @return a scaled array.
	 */
	public static double[] svMult(double s, double[] v) {
		double[] res = new double[v.length];
		for (int i = 0; i < v.length; i++) {
			res[i] = v[i] * s;
		}
		return res;
	}

	/**
	 * Multiplies (scales) every element of the array v with s in place.
	 *
	 * @param s a scalar
	 * @param v an array to be multiplied with s.
	 * @return a scaled array.
	 */
	public static void svMult(double s, double[] v, double[] res) {
		for (int i = 0; i < v.length; i++) {
			res[i] = v[i] * s;
		}
	}

	/**
	 * Return a new vector which is c = (v_i/s).
	 *
	 * @param s
	 * @param v
	 * @return
	 */
	public static double[] svDiv(double s, double[] v) {
		double[] res = new double[v.length];
		for (int i = 0; i < v.length; i++) {
			res[i] = v[i] / s;
		}
		return res;
	}

	/**
	 * Divide by scalar in place, res_i = v_i/s.
	 *
	 * @param s
	 * @param v
	 * @return
	 */
	public static void svDiv(double s, double[] v, double[] res) {
		for (int i = 0; i < v.length; i++) {
			res[i] = v[i] / s;
		}
	}
	
	/**
	 * Component wise multiplication of vectors: res[i]=u[i]*v[i]
	 *
	 * @param s
	 * @param v
	 * @return
	 */
	public static void vvMultCw(double[] u, double[] v, double[] res) {
		for (int i = 0; i < res.length; i++) {
			res[i] = u[i]*v[i];
		}
	}
	
	/** 
	 * Return a vector of given length containing zeroes.
	 * @param n
	 * @return
	 */
	public static double[] zeroes(int n) {
		double[] result = new double[n];
		Arrays.fill(result, 0, result.length-1, 0.);
		return result;
	}

	/**
	 * Returns false if a vector contains NaN, its squared sum is NaN
	 * or the absolute sum is smaller than 10^-18.
	 * @param d
	 * @return
	 */
	public static boolean isValidVec(double[] d) {
		double sum = 0;
		for (int i = 0; i < d.length; i++) {
			if (Double.isNaN(d[i])) return false;
			sum += Math.pow(d[i],2);
		}
		if (Double.isNaN(sum)) return false;
		if (Math.abs(sum) < 0.000000000000000001) return false;
		return true;
	}

	/**
	 * Computes the sum of the elements of an array of doubles.
	 *
	 * @param doubles the array of double
	 * @return the sum of the elements
	 */
	public static double sum(double[] doubles) {
		double sum = 0;
		for (int i = 0; i < doubles.length; i++) {
			sum += doubles[i];
		}
		return sum;
	}

	/**
	 * Computes the mean for an array of doubles.
	 *
	 * @param vector the array
	 * @return the mean
	 */
	public static double mean(double[] vector) {
		if (vector.length == 0) {
			return 0;
		}
		return sum(vector) / (double)vector.length;
	}

//	/**
//	 * Normalizes the doubles in the array using the given value so that they sum up to 1.
//	 *
//	 * @param doubles the array of double
//	 * @param sum the value by which the doubles are to be normalized
//	 * @exception IllegalArgumentException if sum is zero or NaN
//	 */
//	public static void normalize(double[] v, double sum, double[] res) {
//		if (Double.isNaN(sum)) {
//			throw new IllegalArgumentException("Can't normalize array. Sum is NaN.");
//		}
//		if (sum == 0) {
//			// Maybe this should just be a return.
//			throw new IllegalArgumentException("Can't normalize array. Sum is zero.");
//		}
//		svMult(1/sum, v, res);
//	}

	/**
	 * Computes the sum of the elements of an array of integers.
	 *
	 * @param ints the array of integers
	 * @return the sum of the elements
	 */
	public static int sum(int[] ints) {

		int sum = 0;

		for (int i = 0; i < ints.length; i++) {
			sum += ints[i];
		}
		return sum;
	}

	/**
	 * Computes the 2-norm of an array of doubles.
	 *
	 * @param doubles the array of double
	 * @return the 2-norm of the elements
	 */
	public static double norm(double[] d) {
		double sqSum = 0;
		for (int i = 0; i < d.length; i++) {
			sqSum += d[i]*d[i];
		}
		return Math.sqrt(sqSum);
	}
	
	/**
	 * Normalize the given vector to an euclidian length of 1.
	 * 
	 * @param v
	 * @return
	 */
	public static double[] normVect(double[] v) {
		return svDiv(norm(v), v);
	}
	
	/**
	 * Normalize the given vector to an euclidian length of 1.
	 * 
	 * @param v
	 * @return
	 */
	public static void normVect(double[] v, double[] res) {
		svDiv(norm(v), v, res);
	}
	
	/**
	 * Calculates the norm of the given vector relative to the problem range.
	 *
	 * @param vector	a double vector within the range
	 * @param range		the range in each dimension
	 * @return			measure of the length relative to the problem range
	 */
	public static double getRelativeLength(double[] vector, double[][] range) {
		double sumV        = 0;
		double sumR        = 0;
		for (int i = 0; i < range.length; i++) {
			sumV += Math.pow(vector[i], 2);
			sumR += Math.pow(range[i][1] - range[i][0], 2);
		}
		sumV = Math.sqrt(sumV);
		sumR = Math.sqrt(sumR);
		return sumV/sumR;
	}
	
	/**
	 * Create a random vector, the components will be set to gaussian distributed
	 * values with mean zero and the given standard deviation.
	 *  
	 * @param dim the desired dimension
	 * @param stdDev the gaussian standard deviation
	 * @return	 random vector
	 */
	public static double[] randomVector(int dim, double stdDev) {
		double[] vect   = new double[dim];
		for (int j = 0; j < vect.length; j++) {
			vect[j]    = RNG.gaussianDouble(stdDev);
		}
		return vect;
	}
	
	/**
	 * Normalizes the doubles in the array by their sum,
	 * so that they add up to one.
	 * @param doubles the array of double
	 * @exception IllegalArgumentException if sum is Zero or NaN
	 */
	public static double[] normalizeSum(double[] v) {
		double[] res = new double[v.length];
		svMult(1./sum(v), v,res);
		return res;
	}
	
	/**
	 * Normalizes the doubles in the array by their sum,
	 * so that they add up to one.
	 *
	 * @param doubles the array of double
	 * @exception IllegalArgumentException if sum is Zero or NaN
	 */
	public static void normalizeSum(double[] v, double[] res) {
		svMult(1./sum(v), v, res);
	}

	/**
	 * Return a matrix A which performs the rotation of vec to (1,0,0,...0) if forward is true, else
	 * return a matrix B which performs the reverted rotation, where B=A' (transposition).
	 *   
	 * @param vec
	 * @return
	 */
	public static Matrix getRotationMatrix(Matrix vec) {
		Matrix A = Matrix.identity(vec.getRowDimension(), vec.getRowDimension());
		Matrix tmp = Matrix.identity(vec.getRowDimension(), vec.getRowDimension());
		Matrix z = (Matrix)vec.clone();
	
		double w, cosw, sinw;
	
		z.multi(z.norm2()); // normalize
	
	
		for (int i=1; i<vec.getRowDimension(); i++) {
			w = Math.atan2(z.get(i,0), z.get(0,0));// calc angle between the projection of x and x0 in x0-xi-plane
	
			cosw = Math.cos(w);
			sinw = Math.sin(w);
			tmp.set(0, 0, cosw);	// make partial rotation matrix
			tmp.set(0, i, sinw);
			tmp.set(i, 0, -sinw);
			tmp.set(i, i, cosw);
	
			A = tmp.times(A);			// add to resulting rotation
			z = tmp.times(z);			// z is now 0 in i-th component
	
			tmp.set(0, 0, 1); // reset tmp matrix to unity
			tmp.set(0, i, 0);
			tmp.set(i, 0, 0);
			tmp.set(i, i, 1);
		}
		return A;
	}

	/**
	 * Rotate the vector by angle alpha around axis i/j
	 * 
	 * @param vect
	 * @param alpha
	 * @param i
	 * @param j
	 */
	public static void rotate(double[] vect, double alpha, int i, int j) {
		double xi = vect[i];
		double xj = vect[j];
		vect[i] = (xi*Math.cos(alpha))-(xj*Math.sin(alpha));
		vect[j] = (xi*Math.sin(alpha))+(xj*Math.cos(alpha));
	}
	
	/**
	 * Rotate the vector along all axes by angle alpha or a uniform random value
	 * in [-alpha, alpha] if randomize is true.
	 *  
	 * @param vect
	 * @param alpha
	 * @param randomize
	 */
	public static void rotateAllAxes(double[] vect, double alpha, boolean randomize) {
		for (int i=0; i<vect.length-1; i++) {
			for (int j=i+1; j<vect.length; j++) {
				if (randomize) rotate(vect, RNG.randomDouble(-alpha,alpha), i, j);
				else rotate(vect, alpha, i, j);
			}
		}
	}
	
	/**
	 * Rotate the vector along all axes i/j by angle alphas[i][j].
	 *  
	 * @param vect
	 * @param alphas
	 */
	public static void rotateAllAxes(double[] vect, double[][] alphas) {
		for (int i=0; i<vect.length-1; i++) {
			for (int j=i+1; j<vect.length; j++) {
				rotate(vect, alphas[i][j], i, j);
			}
		}
	}
	
	public static double max(double[] vals) {
		double maxVal = vals[0];
		for (int i=1; i<vals.length; i++) maxVal = Math.max(maxVal, vals[i]);
		return maxVal;
	}
	
	public static double min(double[] vals) {
		double minVal = vals[0];
		for (int i=1; i<vals.length; i++) minVal = Math.min(minVal, vals[i]);
		return minVal;
	}
	
	/**
	 * Check whether the given vector lies within the range in every dimension.
	 * 
	 * @param x
	 * @param range
	 * @return	true if the vector lies within the range, else false
	 */
	public static boolean isInRange(double[] x, double[][] range) {
		for (int i=0; i<x.length; i++) {
			if (x[i]<range[i][0] || (x[i]>range[i][1])) return false;
		}
		return true;
	}
	
	/**
	 * Return the vector of interval length values in any dimension.
	 * ret[i]=range[i][1]-range[i][0]; 
	 *
	 * @param range
	 * @return
	 */
	public static double[] shiftRange(double[][] range) {
		double[] ret = new double[range.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=range[i][1]-range[i][0];
		}
		return ret;
	}
	
	/**
	 * Project the values in x to the range given. The range must be an vector of 2d-arrays
	 * each of which containing lower and upper bound in the i-th dimension.
	 * x must not be longer than the available ranges.
	 * Values exceeding the bounds are set on the bound.
	 * The number of bound violations is returned.
	 * 
	 * @param x
	 * @param range
	 * @return
	 */
	public static int projectToRange(double[] x, double[][] range) {
		int viols = 0;
		if (x.length>range.length) System.err.println("Invalid vector length, x is longer than range! (Mathematics.projectToRange)");
		for (int i=0; i<x.length; i++) {
			if (x[i]<range[i][0]) {
				viols++;
				x[i]=range[i][0];
			} else if (x[i]>range[i][1]) {
				viols++;
				x[i]=range[i][1];
			}
		}
		return viols;
	}
	
	
	/**
	 * Calculate the average length of the range intervals over all dimensions.
	 * 
	 * @param range
	 * @return the average length of the range intervals
	 */
    public static double getAvgRange(double[][] range) {
		double sum = 0.;
		for (int i=0; i<range.length; i++) sum+=(range[i][1]-range[i][0]);
		return sum/range.length;
	}

    /**
     * Reflect the entries of x which violate the bounds to within the range. 
     * Return the number of violating dimensions.
     * 
     * @param x
     * @param range
     * @return the number of violating dimensions
     */
	public static int reflectBounds(double[] x, double[][] range) {
		int viols=0;
		double d = 0.;
		for (int i=0; i<x.length; i++) {
			double dimLen = range[i][1]-range[i][0];
			if (dimLen <= 0.) System.err.println("Error in reflectBounds: empty range!");
			if (x[i]<range[i][0]) {
				viols++;
				d = range[i][0]-x[i];
				while (d > dimLen) d -= dimLen; // avoid violating the other bound immediately
				x[i]=range[i][0]+d;
			} else if (x[i]>range[i][1]) {
				viols++;
				d = x[i]-range[i][1];
				while (d>dimLen) d -= dimLen; // avoid violating the other bound immediately
				x[i]=range[i][1]-d;
			}
		}
		return viols;
	}
	
	/**
	 * Simple version of reflection of a value moving by a step and bouncing
	 * of min and max values like a pool ball. Precondition is min <= val <= max,
	 * post condition is min <= retVal <= max.
	 * 
	 * @param val
	 * @param step
	 * @param min
	 * @param max
	 * @return
	 */
	public static double reflectValue(double val, double step, double min, double max) {
		while (step > (max-min)) step -= (max-min);
		if ((val + step) > max) 
			return (2 * max - val - step);
		if ((val + step) < min) 
			 return (2 * min - val - step);
		return (val += step);
	}
}
