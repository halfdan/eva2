package eva2.tools;

import java.util.Arrays;
import wsi.ra.math.interpolation.BasicDataSet;
import wsi.ra.math.interpolation.InterpolationException;
import wsi.ra.math.interpolation.SplineInterpolation;

// created at June 27 2006

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

//<<<<<<< .working
//	/**
//	 * Computes a spline interpolation of the two point (x0,f0) and (x1,f1).
//	 * 
//	 * @param x
//	 * @param x0
//	 * @param x1
//	 * @param f0
//	 * @param f1
//	 * @return If an error with the spline occurs, a linear interpolation will be
//	 *         returned.
//	 */
///*	public static double splineInterpolation(double x, double x0, double x1,
//			double f0, double f1) {
//		try {
//			double[] t = { x0, x1 }, f = { f0, f1 };
//			SplineInterpolation spline = new SplineInterpolation(new BasicDataSet(t,
//					f, 1));
//			return spline.getY(x);
//		} catch (InterpolationException e) {
//			e.printStackTrace();
//		}
//		return linearInterpolation(x, x0, x1, f0, f1);
//	}*/
//=======
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
  public static double dist(double[] x, double[] y, int root) throws Exception {
    if (x.length != y.length)
      throw new Exception("The vecotors x and y must have the same dimension");
    if (root == 0) throw new Exception("There is no 0-root!");
    double d = 0;
    for (int i = 0; i < x.length; i++)
      d += Math.pow(Math.abs(x[i] - y[i]), root);
    return Math.pow(d, (double) 1 / root);
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
  
  /** This method return a vector from a to b
   * @param a     first vector
   * @param b     second vectors
   * @return the vector from a to b
   */
  public static double[] getVectorFromTo(double[] a, double[] b) {
      double[] result = new double[a.length];
      for (int i = 0; i < result.length; i++) {
          result[i] = b[i] - a[i];
      }
      return result;
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
   * Normalize the given vector to a length of 1 returning a deep copy.
   * @param d
   * @return
   */
  public static double[] getNormalizedVector(double[] d) {
      double[] result = new double[d.length];
      System.arraycopy(d, 0, result, 0, d.length);
      normalizeVector(result);
      return result;
  }
  
  /**
   * Normalize the given vector to a length of 1 in place.
   * @param d
   * @return
   */
  public static void normalizeVector(double[] d) {
      double sum = 0;

      for (int i = 0; i < d.length; i++) {
          sum += Math.pow(d[i], 2);
      }
      sum = Math.sqrt(sum);

      for (int i = 0; i < d.length; i++) {
          d[i] = d[i]/sum;
      }
  }
  
  /**
   * Subtract vectors returning a new vector c = a - b.
   * 
   * @param a
   * @param b
   * @return a new vector c = a - b
   */
  public static double[] subVector(double[] a, double[] b) {
      double[] result = new double[a.length];
      for (int i = 0; i < a.length; i++) {
          result[i] = a[i] - b[i];
      }
      return result;
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
    for (int i = 0; i < v1.length; i++)
      res[i] = v1[i] + v2[i];
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
   * Multiply a vector b with scalar a in a new vector c.
   * 
   * @param a
   * @param b
   * @return
   */
  public static double[] scalarMultVector(double a, double[] b) {
      double[] result = new double[b.length];
      for (int i = 0; i < b.length; i++) {
          result[i]= a*b[i];
      }
      return result;
  }

  /** 
   * Return a vector of given length containing zeroes.
   * @param n
   * @return
   */
  public static double[] nullVector(int n) {
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
}
