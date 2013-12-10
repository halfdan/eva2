package eva2.optimization.problems;

import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * Galar Function
 */
@Description("Galar Function")
public class F12Problem extends AbstractProblemDoubleOffset implements Serializable {
    private final static double f12range = 5.;

    public F12Problem() {
        setDefaultRange(f12range);
    }

    public F12Problem(F12Problem b) {
        super(b);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new F12Problem(this);
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
        double tmp = 0;//-5;
        for (int i = 1; i < x.length - 1; i++) {
            tmp += Math.pow(x[i] - xOffset, 2);
        }
        double x0 = x[0] - xOffset;
        result[0] = yOffset + ((Math.exp(-5 * x0 * x0) + 2 * Math.exp(-5 * Math.pow(1 - x0, 2))) * Math.exp(-5 * tmp));
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F12 Galar:\n";
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
        return "F12 Problem";
    }
}