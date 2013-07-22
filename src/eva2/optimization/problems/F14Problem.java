package eva2.optimization.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;

/**
 * Created by IntelliJ IDEA.
 * User: mkron
 * Date: 01.09.2007
 * Time: 19:15:03
 * To change this template use File | Settings | File Templates.
 */
public class F14Problem extends AbstractProblemDoubleOffset implements InterfaceMultimodalProblem, java.io.Serializable {
    double rotation = 0.;
    double rotationDX = 2;

    public F14Problem() {
        this.template = new ESIndividualDoubleData();
        this.problemDimension = 2;
    }

    public F14Problem(F14Problem b) {
        super(b);
        rotation = b.rotation;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new F14Problem(this);
    }

    @Override
    public double[] eval(double[] x) {
        double[] result = new double[1];
        double x0 = x[0] - rotationDX - xOffset;
        double x1 = x[1] - rotationDX - xOffset;
        if (rotation != 0.) {
            double cosw = Math.cos(rotation);
            double sinw = Math.sin(rotation);

            double tmpx0 = cosw * x0 - sinw * x1;
            x1 = sinw * x0 + cosw * x1;
            x0 = tmpx0;
        }
        //matlab: 40 + (- exp(cos(5*X)+cos(3*Y)) .* exp(-X.^2) .* (-.05*Y.^2+5));
        result[0] = yOffset + 36.9452804947;//36.945280494653247;
        result[0] += (-Math.exp(Math.cos(3 * x0) + Math.cos(6 * x1)) * Math.exp(-x0 * x0 / 10) * (-.05 * x1 * x1 + 5));

        return result;
    }

    public double getRotation() {
        return (360.0 / (2 * Math.PI) * rotation);
    }

    public void setRotation(double rotation) {
        this.rotation = 2 * Math.PI * rotation / 360.0;
    }

    public String rotationTipText() {
        return "The rotation angle in degrees.";
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F14 function:\n";
        result += "Several local minima in linear order which may be rotated.\n";
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
        return "F14-Problem";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "F14 function: numerous optima in linear order which may be rotated.";
    }
}
