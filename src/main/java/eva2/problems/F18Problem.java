package eva2.problems;

import eva2.util.annotation.Description;

/**
 * N-Function from Shir&amp;Baeck, PPSN 2006.
 */
@Description("N-Function from Shir&Baeck, PPSN 2006")
public class F18Problem extends AbstractProblemDouble implements
        InterfaceMultimodalProblem {
    int problemDimension = 10;
    double alpha = 1.;

    public F18Problem() {
        problemDimension = 10;
    }

    public F18Problem(F18Problem other) {
        problemDimension = other.problemDimension;
    }

    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] res = new double[1];
        double sum = 0;
        for (int i = 0; i < getProblemDimension(); i++) {
            sum += Math.pow(Math.sin(5 * Math.PI * x[i]), alpha);
        }
        res[0] = 1. - sum / getProblemDimension();
        return res;
    }

    @Override
    public double getRangeLowerBound(int n) {
        return 0.;
    }

    @Override
    public double getRangeUpperBound(int n) {
        return 1.;
    }

    public void setProblemDimension(int newDim) {
        problemDimension = newDim;
    }

    @Override
    public Object clone() {
        return new F18Problem(this);
    }

    @Override
    public String getName() {
        return "F18-Problem";
    }

    /**
     * @return the alpha
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * @param alpha the alpha to set
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
}
