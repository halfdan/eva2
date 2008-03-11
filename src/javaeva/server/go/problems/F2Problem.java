package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.strategies.InterfaceOptimizer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:03:09
 * To change this template use File | Settings | File Templates.
 */
public class F2Problem extends F1Problem implements InterfaceMultimodalProblem, java.io.Serializable {

    public F2Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F2Problem(F2Problem b) {
        super(b);     
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F2Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[] result = new double[1];
        result[0]     = 0;
        for (int i = 0; i < x.length-1; i++) {
            result[0]  += (100*(x[i+1]-x[i]*x[i])*(x[i+1]-x[i]*x[i])+(x[i]-1)*(x[i]-1));
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F2 Generalized Rosenbrock function:\n";
        result += "This problem has a deceptive optimum at (0,0,..), the true optimum is at (1,1,1,..).\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension +"\n";
        result += "Noise level : " + this.m_Noise + "\n";
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
        return "F2-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Generalized Rosenbrock's function.";
    }
}
