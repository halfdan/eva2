package eva2.tools.math;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MathematicsTest {

    @Test
    public void testEuclideanDist() throws Exception {
        double[] values1 = {6.0, 51.0, 3.0};
        double[] values2 = {1.9, 99.0, 2.9};

        assertEquals(48.174889, Mathematics.euclideanDist(values1, values2), 0.00001);
        assertEquals(48.174889, Mathematics.euclideanDist(values2, values1), 0.00001);
    }

    @Test
    public void testMax() throws Exception {
        double[] vals = {1, 2, 3, 4, 5, 6, 4, 3, 2, 2, 12};
        assertEquals(12.0, Mathematics.max(vals), 0.0);
    }

    @Test
    public void testMean() throws Exception {
        double[] vals = {2.0, 3.05, 4.9, 7.8, 12.7};
        assertEquals(6.09, Mathematics.mean(vals), 0.0);

        // Empty vector
        assertEquals(0.0, Mathematics.mean(new double[]{}), 0.0);
    }

    @Test
    public void testZeroes() throws Exception {
        double[] vals = Mathematics.zeroes(10);
        for (final double val : vals) {
            assertEquals(0.0, val, 0.0);
        }
    }

    @Test
    public void testContains() throws Exception {
        assertTrue(Mathematics.contains(new int[]{1, 2, 3, 4, 5}, 4));

        assertFalse(Mathematics.contains(new int[]{1, 2, 3, 4, 5}, 9));
    }

    @Test
    public void testMakeVector() throws Exception {
        double[] vals = Mathematics.makeVector(42.23, 10);
        assertEquals(10, vals.length);
        for (final double val : vals) {
            assertEquals(42.23, val, 0.0);
        }
    }

    @Test
    public void testMeanVect() throws Exception {

    }

    @Test
    public void testMedian() throws Exception {
        // Handle empty case
        assertEquals(Double.NaN, Mathematics.median(new double[]{}, true), 0.0);

        // Median of single element array
        assertEquals(2.4, Mathematics.median(new double[]{2.4}, true), 10E-6);

        // Median of two element array
        assertEquals(5.0, Mathematics.median(new double[]{2.5, 7.5}, true), 10E-6);

        // Median of even length array
        double[] values = {9.8, 7.8, 8.6, 5.6, 3.2, 10.9};
        assertEquals(8.2, Mathematics.median(values, true), 10E-6);


        // Median of odd length array
        double[] values2 = {9.8, 7.8, 5.6, 3.2, 10.9};
        assertEquals(7.8, Mathematics.median(values2, false), 10E-6);

        // Median while preserving original array
        double[] unsortedValues = {5.2, 3.4};
        double[] unsortedValues2 = {5.2, 3.4};
        Mathematics.median(unsortedValues, true);
        assertTrue(Arrays.equals(unsortedValues, unsortedValues2));
    }

    @Test
    public void testVariance() throws Exception {
        double[] values = {9.8, 9.2, 12.3, 15.7, 3.14};

        assertEquals(21.37892, Mathematics.variance(values), 0.000001);
    }

    @Test
    public void testStdDev() throws Exception {
        double[] values = {12.9, 13.5, 19.8, 12.3, 10.7};

        // Mean
        assertEquals(13.84, Mathematics.mean(values), 0.000001);
        // Variance of sample
        assertEquals(12.188, Mathematics.variance(values), 0.000001);
        // Sample std.dev.
        assertEquals(3.491131, Mathematics.stdDev(values), 0.000001);
    }

    @Test
    public void testMin() throws Exception {
        double[] values = {1.9, 2.8, 3.7, 4.6, 5.5};

        assertEquals(1.9, Mathematics.min(values), 0.0);
    }

    @Test
    public void testProjectToRange() throws Exception {

    }

    @Test
    public void testScaleRange() throws Exception {

    }

    @Test
    public void testSum() throws Exception {
        // Array of doubles
        double[] values = {1.9, 2.8, 3.7, 4.6, 5.5};
        assertEquals(18.5, Mathematics.sum(values), 0.0);

        // Array of ints
        int[] intValues = {1, 9, 2, 8, 3, 7, 4, 6, 5};
        assertEquals(45, Mathematics.sum(intValues));
    }

    @Test
    public void testNorm() throws Exception {
        double[] values = {3.0, 4.0};

        assertEquals(5.0, Mathematics.norm(values), 0.0);
    }


    @Test
    public void testScale() throws Exception {
        double[] values = {1.0, 2.0, 3.0};
        Mathematics.scale(2, values);
        assertArrayEquals(new double[]{2.0, 4.0, 6.0}, values, 0.0);
    }

    @Test
    public void testTTestEqSizeEqVar() {
        double[] values1 = {6, 6, 2, 7, 8, 8, 2, 3, 5, 7, 10, 5, 4, 7, 5, 7, 4, 5, 2, 5, 3, 4, 4, 4, 4};
        double[] values2 = {6, 11, 8, 5, 11, 8, 10, 7, 4, 3, 7, 6, 10, 10, 6, 5, 10, 11, 13, 8, 5, 11, 7, 8, 5};

        assertEquals(-4.05593, Mathematics.tTestEqSizeEqVar(values1, values2), 0.00001);
    }

    @Test
    public void testProduct() throws Exception {
        double[] values = {3.0, 4.0, 5.0};

        assertEquals(60.0, Mathematics.product(values), 0.0);
    }

    @Test
    public void testIsInRange() throws Exception {
        // Single dimension
        assertTrue(Mathematics.isInRange(4.9, 1.2, 7.6));
        assertFalse(Mathematics.isInRange(0.0, 1.2, 3.4));

        // Multidimensional
        double[][] ranges = {{1.2, 7.6}, {1.2, 3.4}};
        assertTrue(Mathematics.isInRange(new double[]{4.9, 2.2}, ranges));
        assertFalse(Mathematics.isInRange(new double[]{9.9, 2.2}, ranges));
    }

    @Test
    public void testInverse() throws Exception {
        double[][] matrix = {{4.0, 3.0}, {3.0, 2.0}};
        double[][] inverseMatrix = {{-2.0, 3.0}, {3.0, -4.0}};

        double[][] result = Mathematics.inverse(matrix);
        for (int i = 0; i < result.length; i++) {
            assertArrayEquals(inverseMatrix[i], result[i], 0.0);
        }

        // Non square matrix
        assertNull(Mathematics.inverse(new double[][]{{1.0, 2.0}}));

        // Null matrix
        assertNull(Mathematics.inverse(null));

        // Matrix with determinant = 0
        assertNull(Mathematics.inverse(new double[][]{
                {1.0, 4.0, 5.0},
                {2.0, 5.0, 7.0},
                {3.0, 6.0, 9.0}
        }));
    }

    @Test
    public void testDeterminant() throws Exception {
        // 3x3 Matrix with determinant 0
        assertEquals(0.0, Mathematics.determinant(new double[][]{
                {1.0, 4.0, 5.0},
                {2.0, 5.0, 7.0},
                {3.0, 6.0, 9.0}
        }), 0.0);
    }

    @Test
    public void testLinearInterpolation() throws Exception {
        // Find x = 2.0 for x0(1,1) and x1(3,3)
        assertEquals(2.0, Mathematics.linearInterpolation(2.0, 1.0, 3.0, 1.0, 3.0), 0.0);

        // If x0 and x1 are the same we can't interpolate (returns f0)
        assertEquals(3.0, Mathematics.linearInterpolation(3.0, 2.0, 2.0, 3.0, 3.0), 0.0);
    }

    @Test
    public void testVvSub() throws Exception {
        double[] v1 = {5.0, 6.0, 7.0}, v2 = {3.0, 4.0, 5.0};

        assertArrayEquals(new double[]{2.0, 2.0, 2.0}, Mathematics.vvSub(v1, v2), 0.0);
    }
}