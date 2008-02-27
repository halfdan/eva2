package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;

/**
 * Created by IntelliJ IDEA.
 * User: mkron
 * Date: 01.09.2007
 * Time: 19:15:03
 * To change this template use File | Settings | File Templates.
 */
public class F14Problem extends F1Problem implements java.io.Serializable {
	double rotation = 0.;

	public F14Problem() {
        this.m_Template         = new ESIndividualDoubleData();
        this.m_ProblemDimension = 2;
    }
    
    public F14Problem(F14Problem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //F1Problem
        if (b.m_OverallBest != null)
            this.m_OverallBest      = (AbstractEAIndividual)((AbstractEAIndividual)b.m_OverallBest).clone();
        this.m_ProblemDimension = b.m_ProblemDimension;
        this.m_Noise            = b.m_Noise;
        this.m_XOffSet          = b.m_XOffSet;
        this.m_YOffSet          = b.m_YOffSet;
        this.m_UseTestConstraint = b.m_UseTestConstraint;        
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F14Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[] result = new double[1];
        double x0 = x[0]-2;
        double x1 = x[1]-2;
        if (rotation != 0.) {
			double cosw = Math.cos(rotation);
			double sinw = Math.sin(rotation);
			
			double tmpx0=cosw*x0-sinw*x1;
			x1=sinw*x0+cosw*x1;
			x0=tmpx0;
        }
        //matlab: 40 + (- exp(cos(5*X)+cos(3*Y)) .* exp(-X.^2) .* (-.05*Y.^2+5));
        result[0] = 36.9452804947;//36.945280494653247;
        result[0] += (-Math.exp(Math.cos(3*x0)+Math.cos(6*x1)) * Math.exp(-x0*x0/10) * (-.05*x1*x1+5));

        return result;
    }
	
    public double getRotation() {
		return (360.0 / (2 * Math.PI)  * rotation);
	}

	public void setRotation(double rotation) {
		this.rotation = 2 * Math.PI * rotation / 360.0;
	}
    
    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F14 function:\n";
        result += "Several local minima in a straight line\n";
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
        return "F14 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "F14 function.";
    }
}
