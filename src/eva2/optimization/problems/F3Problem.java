package eva2.optimization.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:15:03
 * To change this template use File | Settings | File Templates.
 */
public class F3Problem extends AbstractProblemDoubleOffset implements java.io.Serializable {

    public F3Problem() {
        this.template = new ESIndividualDoubleData();
    }

    public F3Problem(F3Problem b) {
        super(b);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new F3Problem(this);
    }

    /**
     * Ths method allows you to evaluate a double[] to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] eval(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        result[0] = yOffset + 6 * x.length;
        for (int i = 0; i < x.length - 1; i++) {
            result[0] += Math.floor(x[i] - this.xOffset);
        }
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F3 Step function:\n";
        result += "This problem is discontinuos.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "F3 Problem";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Step function.";
    }
}
