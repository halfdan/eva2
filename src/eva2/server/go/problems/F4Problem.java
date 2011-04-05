package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:28:33
 * To change this template use File | Settings | File Templates.
 */
public class F4Problem extends AbstractProblemDoubleOffset implements java.io.Serializable {
	final static double f4range = 1.28;
	
    public F4Problem() {
        setDefaultRange(f4range);
    }
    public F4Problem(F4Problem b) {
        super(b);
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F4Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
    	x = rotateMaybe(x);
        double[] result = new double[1];
        result[0]     = m_YOffset;
        for (int i = 0; i < x.length-1; i++) {
            result[0]  += (i+1)*Math.pow((x[i]-m_XOffset), 4);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F4 Quadratic Function with noise:\n";
        result += "This problem is noisey.\n";
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
        return "F4 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Quadratic Function with noise.";
    }
}