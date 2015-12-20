package eva2.problems;

import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.math.Mathematics;
import eva2.util.annotation.Description;

/**
 * Sphere Problem
 */
@Description(value = "Sphere Problem")
public class F1Problem extends AbstractProblemDoubleOffset implements Interface2DBorderProblem, InterfaceHasInitRange, java.io.Serializable, InterfaceFirstOrderDerivableProblem {
    private double initialRangeRatio = 1.; // reduce to initialize in a smaller subrange of the original range (in the corner box)

    public F1Problem() {
        super();
        setDefaultRange(10);
    }

    public F1Problem(F1Problem b) {
        super();
        super.cloneObjects(b);
    }

    public F1Problem(int dim) {
        super(dim);
    }

    public F1Problem(int dim, double defRange) {
        this(dim);
        setDefaultRange(defRange);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new F1Problem(this);
    }

    /**
     * This method allows you to evaluate a simple bit string to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        result[0] = yOffset;
        // add an offset in solution space
        for (int i = 0; i < x.length; i++) {
            result[0] += Math.pow(x[i] - this.xOffset, 2);
        }
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
        StringBuilder sb = new StringBuilder(200);
        sb.append("F1 Sphere model:\n");
        sb.append("Here the individual codes a vector of real number x and F1(x)= x^2 is to be minimized.\nParameters:\n");
        sb.append("Dimension   : ");
        sb.append(this.problemDimension);
        sb.append("\nNoise level : ");
        sb.append(this.getNoise());
        return sb.toString();
    }

    /**
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
        return "Sphere";
    }

    @Override
    public double[] getFirstOrderGradients(double[] x) {
        x = rotateMaybe(x);
        // first order partial derivation in direction x_i is 2*x_i
        double[] grads = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            grads[i] = (2. * (x[i] - this.xOffset));
        }
        return grads;
    }

    /**
     * If initialRangeRatio &lt; 1, produce a reduced initial range in the negative corner of the range.
     */
    @Override
    public Object getInitializationRange() {
        if (initialRangeRatio < 1.) {
            double[][] gR = makeRange();
            double[][] initR = makeRange();
            Mathematics.scaleRange(initialRangeRatio, initR);
            for (int i = 0; i < getProblemDimension(); i++) {
                double d = gR[i][0] - initR[i][0];
                initR[i][0] += d; // shift back by original offsets
                initR[i][1] += d;
            }
            return initR;
        } else {
            return makeRange();
        }
    }
}
