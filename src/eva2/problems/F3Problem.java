package eva2.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("Step function.")
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
        return new F3Problem(this);
    }

    /**
     * Evaluates a double vector according to the Step function.
     *
     * fitness = sum(floor(x_i + 0.5)^2)
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        result[0] = yOffset;
        for (int i = 0; i < x.length - 1; i++) {
            result[0] += Math.pow(Math.floor(x[i] + 0.5 - this.xOffset), 2);
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
        return result;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Step";
    }
}
