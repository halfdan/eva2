package eva2.server.go.problems;

import eva2.server.go.individuals.ESIndividualDoubleData;

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
        result[0]     = m_YOffSet;
        double xi, xii;
        for (int i = 0; i < x.length-1; i++) {
        	xi=x[i]-m_XOffSet;
        	xii=x[i+1]-m_XOffSet;
            result[0]  += (100*(xii-xi*xi)*(xii-xi*xi)+(xi-1)*(xi-1));
        }
        if (m_YOffSet==0 && (result[0]<=0)) result[0]=Math.sqrt(Double.MIN_VALUE); // guard for plots in log scale
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
        return "F2-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Generalized Rosenbrock's function.";
    }
}
