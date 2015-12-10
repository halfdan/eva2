package eva2.problems;

import eva2.tools.math.Mathematics;
import eva2.util.annotation.Description;

/**
 * Zakharov function
 */
@Description("Zakharov function")
public class F24Problem extends AbstractProblemDoubleOffset implements InterfaceHasInitRange {
    private double initialRangeRatio = 1.; // reduce to initialize in a smaller subrange of the original range (in the corner box)

    public F24Problem() {
        super();
        setDefaultRange(10);
    }

    public F24Problem(F24Problem b) {
        super();
        super.cloneObjects(b);
    }

    public F24Problem(int dim) {
        super(dim);
    }

    public F24Problem(int dim, double defRange) {
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
        return new F24Problem(this);
    }

    @Override
    public double[] evaluate(double[] x) {
        double sum1 = 0.0, sum2 = 0.0, sum3 = 0.0;

        for (int i = 0; i < x.length; i++) {
            sum1 += Math.pow(x[i], 2);
            sum2 += Math.pow(0.5 * i * Math.pow(x[i], 2), 2);
            sum3 += Math.pow(0.5 * i * Math.pow(x[i], 2), 4);
        }

        return new double[] {sum1 + sum2 + sum3};
    }

    @Override
    public String getName() {
        return "Zakharov";
    }

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
