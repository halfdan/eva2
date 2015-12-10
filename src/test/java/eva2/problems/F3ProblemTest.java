package eva2.problems;

import eva2.tools.math.Mathematics;
import junit.framework.TestCase;

public class F3ProblemTest extends TestCase {

    /**
     * The Step function has its minimum
     * at x_{i} = 0
     *
     * @throws Exception
     */
    public void testEvaluate() throws Exception {
        F3Problem problem = new F3Problem();

        for (int i = 1; i < 100; i++) {
            double[] x = Mathematics.zeroes(i);
            double[] res = problem.evaluate(x);
            assertEquals(1, res.length);
            assertEquals(0.0, res[0]);
        }
    }
}