package eva2.optimization.problems;


import eva2.optimization.strategies.InterfaceOptimizer;

/**
 * The integer hyper-sphere problem.
 * <p/>
 * User: streiche, mkron
 */
public class I1Problem extends AbstractProblemInteger implements java.io.Serializable {

    public I1Problem() {
    }

    public I1Problem(I1Problem o) {
        super.cloneObjects(o);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new I1Problem(this);
    }

    /**
     * Ths method allows you to evaluate a simple bit string to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(int[] x) {
        double[] result = new double[1];
        result[0] = 0;
        for (int i = 0; i < x.length; i++) {
            result[0] += Math.pow(x[i], 2);
        }
        result[0] += 1;
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        String result = "";

        result += "I1 Problem:\n";
        result += "Here the individual codes a vector of int numbers x and F1(x)= x^2 is to be minimized.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension + "\n";
        result += "Solution representation:\n";
        return result;
    }

    /**
     * *******************************************************************************************************************
     * These are for GUI
     */

    @Override
    public String getName() {
        return "I1 Problem";
    }

    public static String globalInfo() {
        return "A hyper parabola on integer values x with I1(x) = x^2 is to be minimized.";
    }
}