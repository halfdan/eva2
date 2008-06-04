package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;

/**
 * Schwefels sine root function (1981) with a minimum at 420.9687^n of value 0.
 * Function f(x) = (418.9829 * n) - sum_n(x_i * sin(sqrt(abs(x_i)))) + (418.9829 * n);
 */
public class F13Problem extends F1Problem implements InterfaceMultimodalProblem {

    public F13Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F13Problem(F13Problem b) {
    	super(b);
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F13Problem(this);
    }
    
    protected double[][] makeRange() {
	    double[][] range = new double[this.m_ProblemDimension][2];
	    for (int i = 0; i < range.length; i++) {
	        range[i][0] = -512.03;
	        range[i][1] = 511.97;
	    }
	    return range;
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[] result = new double[1];
        result[0] = 0;
        
        for (int i=0; i<x.length; i++) {
        	result[0] -= x[i]*Math.sin(Math.sqrt(Math.abs(x[i])));
        }
        result[0] += (418.9829 * m_ProblemDimension);
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F13 Schwefel:\n";
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
        return "F13-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Schwefels sine-root Function (multimodal, 1981). Remember to use range check!";
    }

}