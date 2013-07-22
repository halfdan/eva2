package eva2.optimization.problems;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.postprocess.SolutionHistogram;

/**
 * Schwefels sine root function (1981) with a minimum at 420.9687^n of value 0.
 * Function f(x) = (418.9829 * n) - sum_n(x_i * sin(sqrt(abs(x_i)))) + (418.9829 * n);
 */
public class F13Problem extends AbstractProblemDoubleOffset implements InterfaceMultimodalProblem, InterfaceInterestingHistogram {

    public F13Problem() {
        this.template = new ESIndividualDoubleData();
        setDefaultRange(500);
    }

    public F13Problem(F13Problem b) {
        super(b);
    }

    public F13Problem(int dim) {
        super();
        setProblemDimension(dim);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new F13Problem(this);
    }

    @Override
    public double getRangeLowerBound(int dim) {
        return -500; //-512.03;
    }

    @Override
    public double getRangeUpperBound(int dim) {
        return 500;// 511.97;
    }

    @Override
    public void hideHideable() {
        super.hideHideable();
        GenericObjectEditor.setHideProperty(this.getClass(), "defaultRange", true);
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
        result[0] = yOffset;

        for (int i = 0; i < x.length; i++) {
            double xi = (x[i] - xOffset);
            result[0] -= xi * Math.sin(Math.sqrt(Math.abs(xi)));
        }
        result[0] += (418.9829 * problemDimension);
        // res = cn-sum_i(xi*sin(sqrt(abs(xi))))
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F13 Schwefel:\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

    @Override
    public SolutionHistogram getHistogram() {
//		EVAERROR.errorMsgOnce("Warning: Restore line in getHistogram for evaluation on F13Problem ???");
        if (getProblemDimension() < 15) {
            return new SolutionHistogram(0, 800, 16);
        } else if (getProblemDimension() < 25) {
            return new SolutionHistogram(0, 1600, 16);
        }
//		else if (getProblemDimension() < 35) return new SolutionHistogram(0, 1600, 16);
        else {
            return new SolutionHistogram(0, 3200, 12);
        }
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
        return "F13-Problem";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Schwefels sine-root Function (multimodal, 1981). Remember to use range check! Note that rotating the function may make it easier because new, and better, minima may enter the search space.";
    }

    @Override
    public void setDefaultAccuracy(double v) {
        super.SetDefaultAccuracy(v);
    }
}