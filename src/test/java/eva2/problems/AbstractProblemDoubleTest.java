package eva2.problems;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractProblemDoubleTest {

    private class Mock extends AbstractProblemDouble {
        @Override
        public double[] evaluate(double[] x) {
            return null;
        }

        @Override
        public Object clone() {
            return null;
        }
    }

    private AbstractProblemDouble instance;

    @Before
    public void setUp() {
        instance = new Mock();
    }

    @Test
    public void testMakeRange() throws Exception {
        double[][] range = instance.makeRange();
        assertEquals(instance.getProblemDimension(), range.length);
    }

    @Test
    public void testSetDefaultRange() throws Exception {
        instance.setDefaultRange(150.0);
        assertEquals(150.0, instance.getDefaultRange(), 0.0);
    }

    @Test
    public void testGetRangeLowerBound() throws Exception {
        instance.setDefaultRange(42.0);
        for(int i = 0; i < instance.getProblemDimension(); i++) {
            assertEquals(-42.0, instance.getRangeLowerBound(i), 0.0);
        }
    }

    @Test
    public void testGetRangeUpperBound() throws Exception {
        instance.setDefaultRange(42.0);
        for(int i = 0; i < instance.getProblemDimension(); i++) {
            assertEquals(42.0, instance.getRangeUpperBound(i), 0.0);
        }
    }
}