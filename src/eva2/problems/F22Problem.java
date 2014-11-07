package eva2.problems;

import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.util.annotation.Description;

/**
 * F22 Schwefel 2.22 Problem
 */
@Description(value ="Schwefel 2.22")
public class F22Problem extends AbstractProblemDoubleOffset implements InterfaceHasInitRange, java.io.Serializable {

    public F22Problem() {
        super();
        setDefaultRange(10);
    }

    public F22Problem(F22Problem b) {
        super();
        super.cloneObjects(b);
    }

    public F22Problem(int dim) {
        super(dim);
    }

    public F22Problem(int dim, double defRange) {
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
        return new F22Problem(this);
    }

    /**
     * This method allows you to evaluate a eva2.problems.simple bit string to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        result[0] = yOffset;
        double sum = 0.0, product = 1.0;
        // add an offset in solution space
        for (int i = 0; i < x.length; i++) {
            sum += Math.abs(x[i]);
            product *= Math.abs(x[i]);
        }
        result[0] = sum + product;
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
        sb.append("F22 Schwefel 2.22 model:\n");
        sb.append("Here the individual codes a vector of real number x and F22(x) is to be minimized.\nParameters:\n");
        sb.append("Dimension   : ");
        sb.append(this.problemDimension);
        sb.append("\nNoise level : ");
        sb.append(this.getNoise());
        return sb.toString();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Schwefel 2.22";
    }

    /**
     * If initialRangeRatio<1, produce a reduced initial range in the negative corner of the range.
     */
    @Override
    public Object getInitializationRange() {
        return makeRange();
    }
}
