package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.06.2005
 * Time: 14:59:33
 * To change this template use File | Settings | File Templates.
 */
public class F12Problem extends AbstractProblemDoubleOffset implements java.io.Serializable {
	private final static double f12range = 5.;

    public F12Problem() {
        setDefaultRange(f12range);
    }
    public F12Problem(F12Problem b) {
        super(b);
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F12Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
    	x = rotateMaybe(x);
        double[] result = new double[1];
        double tmp = 0;//-5;
        for (int i = 1; i < x.length-1; i++) {
            tmp += Math.pow(x[i]-m_XOffset, 2);
        }
        double x0 = x[0]-m_XOffset;
        result[0] = m_YOffset+((Math.exp(-5*x0*x0)+2*Math.exp(-5*Math.pow(1-x0, 2)))*Math.exp(-5*tmp));
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F12 Galar:\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension +"\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.m_Template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "F12 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Galar Function";
    }

}