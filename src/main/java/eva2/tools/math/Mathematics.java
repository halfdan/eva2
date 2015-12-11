package eva2.tools.math;

import Jama.Matrix;
import eva2.optimization.tools.DoubleArrayComparator;
import eva2.tools.EVAERROR;
import eva2.tools.Pair;
import eva2.tools.math.interpolation.BasicDataSet;
import eva2.tools.math.interpolation.InterpolationException;
import eva2.tools.math.interpolation.SplineInterpolation;

import java.util.Arrays;
import java.util.List;

public final class Mathematics {
    private Mathematics() {}

    /**
     * Computes the full adjoint matrix.
     *
     * @param a
     * @return
     */
    public static double[][] adjoint(double[][] a) {
        if (a == null) {
            return null;
        }
        if (a.length != a[0].length) {
            return null;
        }
        double[][] b = new double[a.length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                b[i][j] = adjoint(a, i, j);
            }
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
     * This computes the determinant of the given matrix
     *
     * @param matrix
     * @return The determinant or null if there is no determinant (if the matrix
     *         is not square).
     */
    public static double determinant(double[][] matrix) {
        if (matrix == null) {
            return 0;
        }
        if (matrix.length != matrix[0].length) {
            return 0;
        }
        if (matrix.length == 1) {
            return matrix[0][0];
        }
        if (matrix.length == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }
        if (matrix.length == 3) {
            return matrix[0][0] * matrix[1][1] * matrix[2][2] + matrix[0][1]
                    * matrix[1][2] * matrix[2][0] + matrix[0][2] * matrix[1][0]
                    * matrix[2][1] - matrix[2][0] * matrix[1][1] * matrix[0][2]
                    - matrix[2][1] * matrix[1][2] * matrix[0][0] - matrix[2][2]
                    * matrix[1][0] * matrix[0][1];
        }

        double det = 0;
        for (int k = 0; k < matrix.length; k++) {
            if (matrix[0][k] != 0) {
                det += matrix[0][k] * adjoint(matrix, 0, k);
            }
        }
        return det;
    }

    /**
     * Computes the root-Distance function. For example root = 2 gives the
     * Euclidian Distance.
     *
     * @param x    a vector
     * @param y    another vector
     * @param root what kind of distance function
     * @return the distance of x and y
     * @throws Exception if x and y have different dimensions an exception is thrown.
     */
    public static double dist(double[] x, double[] y, int root) {
        if (x.length != y.length) {
            throw new RuntimeException(
                    "The vectors x and y must have the same dimension");
        }
        if (root == 0) {
            throw new RuntimeException("There is no 0-root!");
        }
        double d = 0;
        for (int i = 0; i < x.length; i++) {
            d += Math.pow(Math.abs(x[i] - y[i]), root);
        }
        return Math.pow(d, (double) 1 / root);
    }

    /**
     * Computes the euclidian distance function.
     *
     * @param x    a vector
     * @param y    another vector
     * @return the distance of x and y
     * @throws Exception if x and y have different dimensions an exception is thrown.
     */
    public static double euclideanDist(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("The vectors x and y must have the same dimension");
        }
        double d = 0;
        for (int i = 0; i < x.length; i++) {
            d += Math.pow(Math.abs(x[i] - y[i]), 2);
        }
        return Math.sqrt(d);
    }

    /**
     * Expand a vector to a higher dimension (len) by filling it up with a
     * constant value.
     *
     * @param x
     * @param len
     * @param v
     * @return
     */
    public static double[] expandVector(double[] x, int len, double v) {
        if (len <= x.length) {// not really an error, just perform identity
            return x;
        } else {
            double[] expanded = new double[len];
            System.arraycopy(x, 0, expanded, 0, x.length);
            for (int i = x.length; i < expanded.length; i++) {
                expanded[i] = v;
            }
            return expanded;
        }
    }

    /**
     * Fill the front of an array with data from a given source array.
     *
     * @param dest
     * @param src
     */
    public static void fillFront(double[] dest, double[] src) {
        System.arraycopy(src, 0, dest, 0, Math.min(dest.length, src.length));
    }

    /**
     * Return first multiple of interval which is larger than len.
     *
     * @param len
     * @param interval
     * @return
     */
    public static double firstMultipleAbove(double len, double interval) {
        double startVal, dn = (len / interval);
        startVal = Math.round(dn - 0.5) * interval;

        if (startVal < len || (len == 0)) {
            startVal += interval;
        }
        return startVal;
    }

    /**
     * Return the vector of interval length values in any dimension.
     * ret[i]=range[i][1]-range[i][0];
     *
     * @param range
     * @return
     */
    public static double[] getAbsRange(double[][] range) {
        double[] ret = new double[range.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = range[i][1] - range[i][0];
        }
        return ret;
    }

    /**
     * Calculate the average length of the range intervals over all dimensions.
     *
     * @param range
     * @return the average length of the range intervals
     */
    public static double getAvgRange(double[][] range) {
        double sum = 0.;
        for (int i = 0; i < range.length; i++) {
            sum += (range[i][1] - range[i][0]);
        }
        return sum / range.length;
    }

    /**
     * Calculates the norm of the given vector relative to the problem range.
     *
     * @param vector a double vector within the range
     * @param range  the range in each dimension
     * @return measure of the length relative to the problem range
     */
    public static double getRelativeLength(double[] vector, double[][] range) {
        double sumV = 0;
        double sumR = 0;
        for (int i = 0; i < range.length; i++) {
            sumV += Math.pow(vector[i], 2);
            sumR += Math.pow(range[i][1] - range[i][0], 2);
        }
        sumV = Math.sqrt(sumV);
        sumR = Math.sqrt(sumR);
        return sumV / sumR;
    }

    /**
     * Set rotation matrix entries along i/j axis. w is expected in radians.
     *
     * @param tmp
     * @param i
     * @param j
     * @param w
     */
    public static void getRotationEntriesSingleAxis(Matrix tmp, int i, int j,
                                                    double w) {
        double cosw = Math.cos(w);
        double sinw = Math.sin(w);
        tmp.set(i, i, cosw);
        tmp.set(i, j, sinw);
        tmp.set(j, i, -sinw);
        tmp.set(j, j, cosw);
    }

    public static Matrix getRotationMatrix(double w, int dim) {
        Matrix A = Matrix.identity(dim, dim);
        Matrix tmp = Matrix.identity(dim, dim);

        for (int i = 1; i < dim; i++) {
            // System.out.println("deg: "+(w/Math.PI)*180);
            // make partial rotation matrix
            getRotationEntriesSingleAxis(tmp, i - 1, i, w);
            A = tmp.times(A); // add to resulting rotation
            // reset tmp matrix to unity
            resetRotationEntriesSingleAxis(tmp, i - 1, i);
        }
        // Matrix vec = new Matrix(dim, 1);
        // for (int i=0; i<dim; i++) vec.set(i,0, 1);
        // vec = A.times(vec);
        // vec = A.times(vec);
        return A;
    }

    /**
     * Given two vectors, ``[a0, a1, ..., aM]`` and ``[b0, b1, ..., bN]``,
     * the outer product becomes::
     * <p>
     * [[a0*b0  a0*b1 ... a0*bN ]
     * [a1*b0    .
     * [ ...          .
     * [aM*b0            aM*bN ]]
     */
    public static Matrix outer(double[] a, double[] b) {
        double[][] M = new double[a.length][b.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                M[i][j] = a[i] * b[j];
            }
        }
        return new Matrix(M);
    }

    /**
     * Return the minimum and maximum value on the diagonal
     * as a pair.
     *
     * @return
     */
    public static Pair<Double, Double> getMinMaxDiag(Matrix m) {
        if (m.getRowDimension() < 1 || m.getColumnDimension() < 1) {
            return null;
        }

        double v = m.get(0, 0);
        Pair<Double, Double> ret = new Pair<>(v, v);
        for (int i = 1; i < Math.min(m.getRowDimension(), m.getColumnDimension()); i++) {
            v = m.get(i, i);
            ret.head = Math.min(ret.head, v);
            ret.tail = Math.max(ret.tail, v);
        }
        return ret;
    }

    /**
     * Copy a column from the matrix.
     *
     * @return Matrix elements packed in a one-dimensional array by columns.
     */
    public static double[] getColumn(Matrix m, int k) {
        double[] vals = new double[m.getRowDimension()];
        for (int i = 0; i < m.getRowDimension(); i++) {
            vals[i] = m.get(i, k);
        }
        return vals;
    }

    /**
     * Return a matrix A which performs the rotation of vec to (1,0,0,...0) if
     * forward is true, else return a matrix B which performs the reverted
     * rotation, where B=A' (transposition).
     *
     * @param vec
     * @return
     */
    public static Matrix getRotationMatrix(Matrix vec) {
        Matrix A = Matrix
                .identity(vec.getRowDimension(), vec.getRowDimension());
        Matrix tmp = Matrix.identity(vec.getRowDimension(), vec
                .getRowDimension());
        Matrix z = (Matrix) vec.clone();

        z.times(1. / z.norm2()); // normalize

        for (int i = 1; i < vec.getRowDimension(); i++) {
            double w = Math.atan2(z.get(i, 0), z.get(0, 0));// calc angle
            // between the
            // projection of x
            // and x0 in
            // x0-xi-plane
            // System.out.println("deg: "+(w/Math.PI)*180);

            // make partial rotation matrix
            getRotationEntriesSingleAxis(tmp, 0, i, w);

            A = tmp.times(A); // add to resulting rotation
            z = tmp.times(z); // z is now 0 in i-th component

            // reset tmp matrix to unity
            resetRotationEntriesSingleAxis(tmp, 0, i);
        }
        return A;
    }

    /**
     * This method return a vector from a to b
     *
     * @param a first vector
     * @param b second vectors
     * @return the vector from a to b
     */
    public static double[] getVectorFromTo(double[] a, double[] b) {
        return vvSub(b, a);
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
    public static double hyperbolicInterpolation(double x, double x0,
                                                 double x1, double f0, double f1) {
        if (x1 == 0) {
            return lerp(f0, f1, (x - x0) / (-x0));
        }
        double l = lerp(x0 / x1, 1, x);
        if (l == 0) {
            return linearInterpolation(x, x0, x1, f0, f1);
        }
        return lerp(f0, f1, x / l);
    }

    /**
     * Intersect two ranges resulting in the maximum range contained in both.
     *
     * @param r1
     * @param r2
     * @param destRange
     */
    public static void intersectRange(double[][] r1, double[][] r2,
                                      double[][] destRange) {
        for (int i = 0; i < r1.length && i < r2.length; i++) {
            destRange[i][0] = Math.max(r1[i][0], r2[i][0]);
            destRange[i][1] = Math.min(r1[i][1], r2[i][1]);
        }
    }

    /**
     * Computes the inverse of the given matrix or returns null if there is no
     * inverse (if the determinant is 0).
     *
     * @param a
     * @return
     */
    public static double[][] inverse(double[][] a) {
        if (a == null) {
            return null;
        }
        if (a.length != a[0].length) {
            return null;
        }
        double det = determinant(a);

        if (det == 0) {
            return null;
        }
        double[][] b = adjoint(a);
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                b[i][j] /= det;
            }
        }
        return b;
    }

    /**
     * Check if a number is valid (not NaN) and finite.
     *
     * @param v
     * @return
     */
    public static boolean isFinite(double v) {
        return (!Double.isInfinite(v) && !Double.isNaN(v));
    }

    /**
     * Check if all numbers are valid (not NaN) and finite. Returns
     * -1 if this is the case or the index of the first row with an invalid number.
     *
     * @param v
     * @return
     */
    public static int areFinite(double[][] v) {
        for (int i = 0; i < v.length; i++) {
            if (areFinite(v[i]) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if all numbers are valid (not NaN) and finite. Returns
     * -1 if this is the case or the index of the first invalid number.
     *
     * @param v
     * @return
     */
    public static int areFinite(double... v) {
        for (int i = 0; i < v.length; i++) {
            if (Double.isInfinite(v[i]) || Double.isNaN(v[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check whether the given value lies within the interval in every
     * dimension.
     *
     * @param v     Value
     * @param lower Lower bound
     * @param upper Upper bound
     * @return true if the vector lies within the range, else false
     */
    public static boolean isInRange(double v, double lower, double upper) {
        return !(v < lower || (v > upper));
    }

    /**
     * Check whether the given vector lies within the range in every dimension.
     *
     * @param x
     * @param range
     * @return true if the vector lies within the range, else false
     */
    public static boolean isInRange(double[] x, double[][] range) {
        for (int i = 0; i < x.length; i++) {
            if (x[i] < range[i][0] || (x[i] > range[i][1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns false if a column vector contains NaN, its squared sum is NaN or the
     * absolute sum is smaller than 10^-18.
     *
     * @param d
     * @return
     */
    public static boolean isValidVec(double[][] d) {
        for (double[] vec : d) {
            if (!isValidVector(vec)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns false if a vector contains NaN, its squared sum is NaN or the
     * absolute sum is smaller than 10^-18.
     *
     * @param d
     * @return
     */
    public static boolean isValidVector(double[] d) {
        double sum = 0;
        for (int i = 0; i < d.length; i++) {
            if (Double.isNaN(d[i])) {
                return false;
            }
            sum += Math.pow(d[i], 2);
        }
        if (Double.isNaN(sum)) {
            return false;
        }
        return Math.abs(sum) >= 10E-18;
    }

    /**
     * Linear interpolation between two points
     *
     * @param f0
     * @param f1
     * @param t
     * @return
     */
    private static double lerp(double f0, double f1, double t) {
        return f0 + (f1 - f0) * t;
    }

    /**
     * This method gives a linear interpolation of the function values of the
     * given argument/function value pairs.
     *
     * @param x  The argument at the point with unknown function value
     * @param x0 The argument at the last position with a function value
     * @param x1 The argument at the next known function value
     * @param f0 The function value at the position x0
     * @param f1 The function value at the position x1
     * @return The function value at position x given by linear interpolation.
     */
    public static double linearInterpolation(double x, double x0, double x1,
                                             double f0, double f1) {
        if (x1 == x0) {
            return f0;
        }
        return lerp(f0, f1, (x - x0) / (x1 - x0));
    }

    public static double max(double[] vals) {
        double maxVal = vals[0];
        for (int i = 1; i < vals.length; i++) {
            maxVal = Math.max(maxVal, vals[i]);
        }
        return maxVal;
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
        double sum = sum(vector);
        return sum / (double) vector.length;
    }

    /**
     * This method returns a mean vector from a whole array of vectors.
     *
     * @param d d[i] the vectors, d[i][j] the jth coordinate of the ith vector
     * @return The mean vector.
     */
    public static double[] meanVect(double[][] d) {
        double[] result = new double[d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                result[j] += d[i][j];
            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i] /= ((double) d.length);
        }
        return result;
    }

    /**
     * Computes the median of a given double vector by sorting x.
     *
     * @param x      a vector of doubles
     * @param cloneX flag whether x should be cloned before sorting.
     * @return the median
     */
    public static double median(double[] x, boolean cloneX) {
        double[] in;
        if (cloneX) {
            in = x.clone();
        } else {
            in = x;
        }

        Arrays.sort(in);
        if (in.length == 0) {
            return Double.NaN;
        } else if (in.length == 1) {
            return in[0];
        } else if (in.length == 2) {
            return (in[0] + in[1]) / 2.;
        } else {
            if (in.length % 2 == 1) {
                return in[in.length / 2];
            } else {
                return (in[in.length / 2 - 1] + in[in.length / 2]) / 2.;
            }
        }
    }

    /**
     * Computes the median of a given list of double vectors by sorting it. If
     * the size is even, no direct median is defined - in that case it may be
     * interpolated by the two closest elements or one of them may be selected
     * (always the smaller one depending on the comparator.
     *
     * @param dblArrList  a list of double vectors
     * @param interpolate flag whether, for even size, the median is interpolated
     * @return the median
     * @see DoubleArrayComparator
     */
    public static double[] median(List<double[]> dblArrList, boolean interpolate) {
        // by default the comparator uses pareto dominance
        java.util.Collections.sort(dblArrList, new DoubleArrayComparator());

        int len = dblArrList.size();
        if (len % 2 != 0) {
            return dblArrList.get((len - 1) / 2);
        } else {
            double[] med = dblArrList.get(len / 2).clone();
            if (interpolate) {
                vvAdd(med, dblArrList.get((len / 2) + 1), med);
                svDiv(2, med, med);
            }
            return med;
        }
    }

    public static double variance(double[] vector) {
        double mean = Mathematics.mean(vector);
        double result = 0.0;
        for (int i = 0; i < vector.length; i++) {
            result += Math.pow(vector[i] - mean, 2);
        }
        return result / (vector.length - 1);
    }

    /**
     * Computes the sample standard deviation.
     *
     * @param vector
     * @return
     */
    public static double stdDev(double[] vector) {
        double result = variance(vector);
        result = Math.sqrt(result);
        return result;
    }

    /**
     * Performs two-sample unpaired t test and returns t value.
     *
     * Both samples have to have the same size and equal variance.
     *
     * @param vector1
     * @param vector2
     * @return
     */
    public static double tTestEqSizeEqVar(double[] vector1, double[] vector2) {
        double n = (double) vector1.length;
        double mean1 = mean(vector1);
        double mean2 = mean(vector2);
        double stdDev1 = stdDev(vector1);
        double stdDev2 = stdDev(vector2);
        double sX1X2 = Math.sqrt((Math.pow(stdDev1, 2) + Math.pow(stdDev2, 2)) / 2);
        return (mean1 - mean2) / (Math.sqrt(2 / n) * sX1X2);
    }

    public static double tTestUnEqSizeEqVar(double[] vector1, double[] vector2) {
        double n1 = (double) vector1.length;
        double n2 = (double) vector2.length;
        double mean1 = mean(vector1);
        double mean2 = mean(vector2);
        double stdDev1 = stdDev(vector1);
        double stdDev2 = stdDev(vector2);
        double sX1X2 = Math.sqrt(((n1 - 1) * Math.pow(stdDev1, 2) + (n2 - 1) * Math.pow(stdDev2, 2)) / (n1 + n2 - 2));
        return (mean1 - mean2) / (sX1X2 * Math.sqrt(1 / n1 + 1 / n2));
    }

    public static double tTestUnEqSizeUnEqVar(double[] vector1, double[] vector2) {
        double n1 = (double) vector1.length;
        double n2 = (double) vector2.length;
        double mean1 = mean(vector1);
        double mean2 = mean(vector2);
        double stdDev1 = stdDev(vector1);
        double stdDev2 = stdDev(vector2);
        double sX1X2 = Math.sqrt((Math.pow(stdDev1, 2) / n1) + (Math.pow(stdDev2, 2) / n2));
        return (mean1 - mean2) / sX1X2;
    }

    public static double min(double[] vals) {
        double minVal = vals[0];
        for (int i = 1; i < vals.length; i++) {
            minVal = Math.min(minVal, vals[i]);
        }
        return minVal;
    }

    /**
     * Computes the 2-norm of an array of doubles.
     *
     * @param d the array of double
     * @return the 2-norm of the elements
     */
    public static double norm(double[] d) {
        double sqSum = 0;
        for (double value : d) {
            sqSum += value * value;
        }
        return Math.sqrt(sqSum);
    }

    /**
     * Normalizes the doubles in the array by their sum, so that they add up to
     * one.
     *
     * @param v the array of double
     * @throws IllegalArgumentException if sum is Zero or NaN
     */
    public static double[] normalizeSum(double[] v) {
        double[] res = new double[v.length];
        svMult(1. / sum(v), v, res);
        return res;
    }

    /**
     * Normalizes the doubles in the array by their sum, so that they add up to
     * one.
     *
     * @param v the array of double
     * @throws IllegalArgumentException if sum is Zero or NaN
     */
    public static void normalizeSum(double[] v, double[] res) {
        svMult(1. / sum(v), v, res);
    }

    /**
     * Normalize the given vector to a euclidean length of 1.
     *
     * @param v
     * @return
     */
    public static double[] normVect(double[] v) {
        return svDiv(norm(v), v);
    }

    /**
     * Normalize the given vector to a euclidean length of 1.
     *
     * @param v
     * @return
     */
    public static void normVect(double[] v, double[] res) {
        svDiv(norm(v), v, res);
    }

    /**
     * Return the product over a double vector.
     *
     * @param vals
     * @return
     */
    public static double product(double[] vals) {
        double prod = 1.;
        for (double val : vals) {
            prod *= val;
        }
        return prod;
    }

    /**
     * Project the values in x to the range given. The range must be an vector
     * of 2d-arrays each of which containing lower and upper bound in the i-th
     * dimension. x must not be longer than the available ranges. Values
     * exceeding the bounds are set on the bound. The number of bound violations
     * is returned.
     *
     * @param x
     * @param range
     * @return
     */
    public static int projectToRange(double[] x, double[][] range) {
        int viols = 0;
        if (x.length > range.length) {
            System.err.println("Invalid vector length, x is longer than range! (Mathematics.projectToRange)");
        }
        for (int i = 0; i < x.length; i++) {
            if (x[i] < range[i][0]) {
                viols++;
                x[i] = range[i][0];
            } else if (x[i] > range[i][1]) {
                viols++;
                x[i] = range[i][1];
            }
        }
        return viols;
    }

    /**
     * Project the value to the range given.
     *
     * @param v
     * @param min
     * @param max
     * @return the closest value to v within [min,max]
     */
    public static double projectValue(double v, double min, double max) {
        double value;
        if (v < min) {
            value = min;
        } else if (v > max) {
            value = max;
        } else {
            value = v;
        }
        return value;
    }

    /**
     * Create a random vector, the components will be set to Gaussian
     * distributed values with mean zero and the given standard deviation.
     *
     * @param dim    The desired dimension
     * @param stdDev The Gaussian standard deviation
     * @return random vector
     */
    public static double[] randomVector(final int dim, final double stdDev) {
        double[] vect = new double[dim];
        for (int j = 0; j < vect.length; j++) {
            vect[j] = RNG.gaussianDouble(stdDev);
        }
        return vect;
    }

    /**
     * Reflect the entries of x which violate the bounds to within the range.
     * Return the number of violating dimensions.
     *
     * @param x
     * @param range
     * @return The number of violating dimensions
     */
    public static int reflectBounds(double[] x, double[][] range) {
        int viols = 0;
        double d;
        for (int i = 0; i < x.length; i++) {
            double dimLen = range[i][1] - range[i][0];
            if (dimLen <= 0.) {
                EVAERROR.errorMsgOnce("Error in reflectBounds: empty range! (possibly multiple errors)");
            } else {
                if (x[i] < range[i][0]) {
                    viols++;
                    d = range[i][0] - x[i];
                    while (d > dimLen)
                        d -= dimLen; // avoid violating the other bound
                    // immediately
                    x[i] = range[i][0] + d;
                } else if (x[i] > range[i][1]) {
                    viols++;
                    d = x[i] - range[i][1];
                    while (d > dimLen) {
                        d -= dimLen; // avoid violating the other bound
                    }
                    // immediately
                    x[i] = range[i][1] - d;
                }
            }
        }
        return viols;
    }

    /**
     * Simple version of reflection of a value moving by a step and bouncing of
     * min and max values like a pool ball. Precondition is min <= val <= max,
     * post condition is min <= retVal <= max.
     *
     * @param val
     * @param step
     * @param min
     * @param max
     * @return
     */
    public static double reflectValue(double val, double step, double min,
                                      double max) {
        while (step > (max - min)) {
            step -= (max - min);
        }
        if ((val + step) > max) {
            return (2 * max - val - step);
        }
        if ((val + step) < min) {
            return (2 * min - val - step);
        }
        return val + step;
    }

    /**
     * Computes the relative distance of vector x to vector y. Therefore the
     * difference of x[i] and y[i] is divided by y[i] for every i. If y[i] is
     * zero, the default value def is used instead. The sum of these differences
     * gives the distance function.
     *
     * @param x   A vector
     * @param y   The reference vector
     * @param def The default value to be used to avoid division by zero.
     * @return The relative distance of x to y.
     * @throws Exception
     */
    public static double relDist(double[] x, double[] y, double def)
            throws Exception {
        if (x.length != y.length) {
            throw new Exception("The vectors x and y must have the same dimension");
        }
        double d = 0;
        for (int i = 0; i < x.length; i++) {
            if (y[i] != 0) {
                d += Math.pow(((x[i] - y[i]) / y[i]), 2);
            } else {
                d += def;
            }
        }
        return d;
    }

    /**
     * Reset single axis rotation matrix to unity.
     */
    public static void resetRotationEntriesSingleAxis(Matrix tmp, int i, int j) {
        tmp.set(i, i, 1);
        tmp.set(i, j, 0);
        tmp.set(j, i, 0);
        tmp.set(j, j, 1);
    }

    public static void revertArray(Object[] src, Object[] dst) {
        if (dst.length >= src.length) {
            for (int i = 0; i < src.length; i++) {
                dst[src.length - i - 1] = src[i];
            }
        } else {
            System.err.println("Mismatching array lengths!");
        }
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
        vect[i] = (xi * Math.cos(alpha)) - (xj * Math.sin(alpha));
        vect[j] = (xi * Math.sin(alpha)) + (xj * Math.cos(alpha));
    }

    /**
     * Rotate a given double vector using a rotation matrix. If the matrix is
     * null, x will be returned unchanged. Matrix dimensions must fit.
     *
     * @param x
     * @param rotMatrix
     * @return the rotated vector
     */
    public static double[] rotate(double[] x, Matrix rotMatrix) {
        if (rotMatrix != null) {
            Matrix resVec = rotMatrix.times(new Matrix(x, x.length));
            x = resVec.getColumnPackedCopy();
            return x;
        } else {
            return x;
        }
    }

    /**
     * Rotate the vector along all axes by angle alpha or a uniform random value
     * in [-alpha, alpha] if randomize is true.
     *
     * @param vect
     * @param alpha
     * @param randomize
     */
    public static void rotateAllAxes(double[] vect, double alpha,
                                     boolean randomize) {
        for (int i = 0; i < vect.length - 1; i++) {
            for (int j = i + 1; j < vect.length; j++) {
                if (randomize) {
                    rotate(vect, RNG.randomDouble(-alpha, alpha), i, j);
                } else {
                    rotate(vect, alpha, i, j);
                }
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
        for (int i = 0; i < vect.length - 1; i++) {
            for (int j = i + 1; j < vect.length; j++) {
                rotate(vect, alphas[i][j], i, j);
            }
        }
    }

    /**
     * Scale a range by the given factor, meaning that the interval in each
     * dimension is extended (fact>1) or reduced (fact < 1) by the defined ratio
     * around the center.
     *
     * @param rangeScaleFact
     * @param range
     */
    public static void scaleRange(double rangeScaleFact, double[][] range) {
        double[] intervalLengths = Mathematics.getAbsRange(range);
        double[] tmpInts = Mathematics.svMult(rangeScaleFact, intervalLengths);
        Mathematics.vvSub(tmpInts, intervalLengths, tmpInts); // this is what
        // must be added
        // to range
        // interval
        for (int i = 0; i < range.length; i++) {
            range[i][0] -= tmpInts[i] / 2;
            range[i][1] += tmpInts[i] / 2;
        }
    }

    /**
     * Shift bounds by a constant value in every dimension.
     *
     * @param range
     * @return
     */
    public static void shiftRange(double[][] range, double dist) {
        for (int i = 0; i < range.length; i++) {
            svAdd(dist, range[i]);
        }
    }

    /**
     * Shift bounds by a constant value in every dimension. The dists must be of
     * dimensions as the range.
     *
     * @param range
     * @return
     */
    public static void shiftRange(double[][] range, double[] dists) {
        for (int i = 0; i < range.length; i++) {
            svAdd(dists[i], range[i]);
        }
    }

    /**
     * Computes a spline interpolation of the two point (x0,f0) and (x1,f1).
     *
     * @param x
     * @param x0
     * @param x1
     * @param f0
     * @param f1
     * @return If an error with the spline occurs, a linear interpolation will
     *         be returned.
     */
    public static double splineInterpolation(double x, double x0, double x1,
                                             double f0, double f1) {
        try {
            double[] t = {x0, x1}, f = {f0, f1};
            SplineInterpolation spline = new SplineInterpolation(
                    new BasicDataSet(t, f, 1));
            return spline.getY(x);
        } catch (InterpolationException e) {
            e.printStackTrace();
        }
        return linearInterpolation(x, x0, x1, f0, f1);
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
            if (i == k) {
                continue;
            }
            for (j = 0; j < a[0].length; j++) {
                if (j == l) {
                    continue;
                }
                b[m][n++] = a[i][j];
            }
            m++;
            n = 0;
        }

        return b;
    }

    /**
     * Computes the sum of the elements of an array of doubles.
     *
     * @param doubles the array of double
     * @return the sum of the elements
     */
    public static double sum(double[] doubles) {
        double sum = 0;
        for (double value : doubles) {
            sum += value;
        }
        return sum;
    }

    /**
     * Computes the sum of the elements of an array of integers.
     *
     * @param ints the array of integers
     * @return the sum of the elements
     */
    public static int sum(int[] ints) {
        int sum = 0;

        for (int value : ints) {
            sum += value;
        }
        return sum;
    }

    /**
     * Add each entry of a vector with a scalar in a new vector.
     *
     * @param s
     * @param v
     * @return
     */
    public static double[] svAdd(double s, double[] v) {
        double[] res = new double[v.length];
        svAdd(s, v, res);
        return res;
    }

    /**
     * Add each entry of a vector with a scalar in a result vector.
     *
     * @param s Scalar
     * @param v Vector
     * @return
     */
    public static void svAdd(double s, double[] v, double[] res) {
        for (int i = 0; i < v.length; i++) {
            res[i] = v[i] + s;
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
        svDiv(s, v, res);
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
     * Multiplies (scales) every element of the array v with s returning a new
     * vector.
     *
     * @param s a scalar
     * @param v an array to be multiplied with s.
     * @return a scaled array.
     */
    public static double[] svMult(double s, double[] v) {
        double[] res = new double[v.length];
        svMult(s, v, res);
        return res;
    }

    /**
     * Multiplies (scales) every element of the array v with s in place.
     *
     * @param s a scalar
     * @param v an array to be multiplied with s.
     */
    public static void svMult(double s, double[] v, double[] res) {
        for (int i = 0; i < v.length; i++) {
            res[i] = v[i] * s;
        }
    }

    /**
     * Add vectors scaled: res[i] = s*v[i] + w[i]
     *
     * @param s Scaling factor
     * @param v
     * @param w
     * @return
     */
    public static void svvAddScaled(double s, double[] v, double[] w,
                                    double[] res) {
        for (int i = 0; i < v.length; i++) {
            res[i] = s * v[i] + w[i];
        }
    }

    /**
     * Add vectors scaled: res[i] = s*(v[i] + w[i])
     *
     * @param s
     * @param v
     * @param w
     * @return
     */
    public static void svvAddAndScale(double s, double[] v, double[] w,
                                      double[] res) {
        for (int i = 0; i < v.length; i++) {
            res[i] = s * (v[i] + w[i]);
        }
    }

    /**
     * Add vectors returning a new vector c = a + b;
     *
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
     * Calculate r=1/2 * sqrt(sum(sqr(upperBound_i - lowerBound_i))).
     *
     * @param range
     * @return the average length of the range intervals
     */
    public static double getAvgRangeL2(double[][] range) {
        double sum = 0.;
        for (int i = 0; i < range.length; i++) {
            double d = (range[i][1] - range[i][0]);
            sum += (d * d);
        }
        return Math.sqrt(sum) / 2.;
    }

    /**
     * Add vectors in place setting with an offset within the target vector,
     * meaning that res[resOffs+i]=v1[v1Offs+i]+v2[v2Offs+i] for i in length.
     *
     * @param v1
     * @param v2
     * @return vector addition
     */
    public static void vvAddOffs(double[] v1, int v1Offs, double[] v2,
                                 int v2Offs, double[] res, int resOffs, int len) {
        for (int i = 0; i < len; i++) {
            res[resOffs + i] = v1[v1Offs + i] + v2[v2Offs + i];
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
            result += a[i] * b[i];
        }
        return result;
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
            res[i] = u[i] * v[i];
        }
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
     * Return a vector of given length containing zeroes.
     *
     * @param n
     * @return
     */
    public static double[] zeroes(int n) {
        return makeVector(0, n);
    }

    /**
     * Create a double vector of length dim filled with value d.
     *
     * @param d
     * @param dim
     * @return a double vector of length dim filled with value d
     */
    public static double[] makeVector(double d, int dim) {
        double[] ret = new double[dim];
        Arrays.fill(ret, d);
        return ret;
    }

    /**
     * Scales a vector with the given scalar.
     *
     * @param scale
     * @param vec
     */
    public static void scale(double scale, double[] vec) {
        for (int i = 0; i < vec.length; i++) {
            vec[i] *= scale;
        }
    }

    /**
     * Return true if an integer is contained in an integer list, otherwise false.
     *
     * @param list
     * @param i
     * @return
     */
    public static boolean contains(int[] list, int i) {
        for (int k : list) {
            if (k == i) {
                return true;
            }
        }
        return false;
    }
}
