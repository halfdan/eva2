package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;

public class F9Problem extends AbstractProblemDoubleOffset implements java.io.Serializable {

    public F9Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F9Problem(F9Problem b) {
        super(b);
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F9Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
    	x = rotateMaybe(x);
        double[] result = new double[1];
        result[0]     = m_YOffSet;
        for (int i = 0; i < x.length; i++) {
            result[0]  += (i+1)*Math.pow(x[i]-m_XOffSet, 2);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F9 Weighted Sphere Model:\n";
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
        return "F9 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Weighted Sphere Model";
    }
}