package eva2.optimization.problems;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:30:52
 * To change this template use File | Settings | File Templates.
 */
public class F5Problem extends AbstractProblemDoubleOffset implements Serializable {
	final static double f5range = 65.536;
	
    public F5Problem() {
        setDefaultRange(f5range);
    }
    public F5Problem(F5Problem b) {
        super(b);
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    @Override
    public Object clone() {
        return (Object) new F5Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    @Override
    public double[] eval(double[] x) {
    	x = rotateMaybe(x);
        double[]    result = new double[1];
        double      tmp;
        result[0]     = yOffset;
        for (int i = 0; i < x.length; i++) {
            tmp = 0;
            for (int j = 0; j <= i; j++) {
                tmp += x[j]- xOffset;
            }
            result[0] += Math.pow(tmp, 2);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F5 Schwefel's Function:\n";
        result += "This problem is unimodal.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension +"\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
        return "F5 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Schwefel's Function.";
    }
}