package eva2.problems;

import eva2.optimization.operator.postprocess.SolutionHistogram;
import eva2.util.annotation.Description;

/**
 * The Vincent function: Multiple optima with increasing density near the lower bounds,
 * therefore decreasing attractor size. All have an equal best fitness of zero.
 */
@Description("Vincent function: Multiple optima with increasing densitiy near the lower bounds, therefore decreasing attractor size. All have an equal best fitness of zero")
public class F16Problem extends AbstractProblemDouble implements InterfaceMultimodalProblem, Interface2DBorderProblem, InterfaceInterestingHistogram {
    int dim = 10;

    public F16Problem() {
        dim = 10;
    }

    public F16Problem(F16Problem other) {
        dim = other.dim;
    }

    public F16Problem(int theDim) {
        this.dim = theDim;
    }

    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] res = new double[1];
        double sum = 0;

        for (int i = 0; i < getProblemDimension(); i++) {
            sum += Math.sin(10 * Math.log(x[i]));
        }

        res[0] = 1. - ((1. / getProblemDimension()) * sum);
        return res;
    }

    @Override
    public int getProblemDimension() {
        return dim;
    }

    public void setProblemDimension(int newDim) {
        dim = newDim;
    }

    @Override
    public Object clone() {
        return new F16Problem(this);
    }

    @Override
    public double getRangeLowerBound(int n) {
        return 0.25;
    }

    @Override
    public double getRangeUpperBound(int n) {
        return 10.;
    }

    @Override
    public String getName() {
        return "Vincent";
    }

    @Override
    public SolutionHistogram getHistogram() {
        return new SolutionHistogram(-0.001, 0.599, 15);
    }
}
