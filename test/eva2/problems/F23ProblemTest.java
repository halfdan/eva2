package eva2.problems;

import eva2.tools.math.Mathematics;
import junit.framework.TestCase;

public class F23ProblemTest extends TestCase {

    /**
     * The Generalized Schaffer function has its minimum
     * at x_{i} = 0
     *
     * @throws Exception
     */
    public void testEvaluate() throws Exception {
        F23Problem problem = new F23Problem();

        for (int i = 1; i < 100; i++) {
            double[] x = Mathematics.zeroes(i);
            double[] res = problem.evaluate(x);
            assertEquals(1, res.length);
            assertEquals(0.0, res[0]);
        }
    }
}