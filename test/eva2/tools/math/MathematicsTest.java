package eva2.tools.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MathematicsTest {

    @Test
    public void testEuclidianDist() throws Exception {

    }

    @Test
    public void testMax() throws Exception {
        double[] vals = {1,2,3,4,5,6,4,3,2,2,12};
        assertEquals(12.0, Mathematics.max(vals), 0.0);
    }

    @Test
    public void testMean() throws Exception {
        double[] vals = {2.0,3.05,4.9,7.8,12.7};
        assertEquals(6.09, Mathematics.mean(vals), 0.0);
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
        assertTrue(Mathematics.contains(new int[]{1,2,3,4,5}, 4));
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

    }

    @Test
    public void testMedian2() throws Exception {

    }

    @Test
    public void testVariance() throws Exception {
        double[] values = {9.8,9.2,12.3,15.7,3.14};

        assertEquals(21.37892, Mathematics.variance(values), 0.000001);
    }

    @Test
    public void testStdDev() throws Exception {
        double[] values = {12.9,13.5,19.8,12.3,10.7};

        // Mean
        assertEquals(13.84, Mathematics.mean(values), 0.000001);
        // Variance of sample
        assertEquals(12.188, Mathematics.variance(values), 0.000001);
        // Sample std.dev.
        assertEquals(3.491131, Mathematics.stdDev(values), 0.000001);
    }

    @Test
    public void testMin() throws Exception {
        double[] values = {1.9,2.8,3.7,4.6,5.5};

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
        double[] values = {1.9,2.8,3.7,4.6,5.5};

        assertEquals(18.5, Mathematics.sum(values), 0.0);
    }

    @Test
    public void testScale() throws Exception {

    }
}