package eva2.problems;

import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * Quadratic function with noise.
 */
@SuppressWarnings("unused")
@Description("Quadratic Function with noise.")
public class F4Problem extends AbstractProblemDoubleOffset implements Serializable {
    final static double f4range = 1.28;

    public F4Problem() {
        setDefaultRange(f4range);
    }

    public F4Problem(F4Problem b) {
        super(b);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new F4Problem(this);
    }

    /**
     * Ths method allows you to evaluate a double[] to determine the fitness
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
            result[0] += (i + 1) * Math.pow((x[i] - xOffset), 4);
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

        result += "F4 Quadratic Function with noise:\n";
        result += "This problem is noisy.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
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
        return "Noisy Quaric";
    }
}