package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.06.2005
 * Time: 13:59:12
 * To change this template use File | Settings | File Templates.
 */
public class F9Problem extends F1Problem implements java.io.Serializable {

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
        double[] result = new double[1];
        result[0]     = 0;
        for (int i = 0; i < x.length; i++) {
            result[0]  += (i+1)*Math.pow(x[i], 2);
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
    public String globalInfo() {
        return "Weighted Sphere Model";
    }
}