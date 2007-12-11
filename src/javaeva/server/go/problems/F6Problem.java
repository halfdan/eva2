package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.strategies.ParticleSwarmOptimization;
import wsi.ra.math.Jama.Matrix;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.06.2005
 * Time: 13:09:36
 * To change this template use File | Settings | File Templates.
 */
public class F6Problem extends F1Problem implements java.io.Serializable {

	private boolean 		doRotation = false;
    private double          m_A     = 10;
    private double          m_Omega = 2*Math.PI;
    private Matrix 			rotation;

    public F6Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F6Problem(F6Problem b) {
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
        this.m_A                = b.m_A;
        this.m_Omega            = b.m_Omega;
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
        super.initProblem();
        rotation = new Matrix(m_ProblemDimension, m_ProblemDimension);
        Matrix vec = new Matrix(m_ProblemDimension, 1);
        for (int i=0; i<m_ProblemDimension; i++) vec.set(i,0, i+1);
        rotation = ParticleSwarmOptimization.getRotationMatrix(vec).transpose();
    }
    
    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F6Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
    	
    	if (doRotation) {
	    	Matrix resVec = rotation.times(new Matrix(x, x.length));
	    	x = resVec.getColumnPackedCopy();
    	}
        double[] result = new double[1];
        result[0]     = x.length * this.m_A;
        for (int i = 0; i < x.length; i++) {
            result[0]  += Math.pow(x[i], 2) - this.m_A * Math.cos(this.m_Omega*x[i]);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F6 Generalized Rastrigin's Function:\n";
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
        return "F6-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Generalized Rastrigins's function.";
    }

    /** This method allows you to set/get an offset for decision variables.
     * @param a     The offset for the decision variables.
     */
    public void setA(double a) {
        this.m_A = a;
    }
    public double getA() {
        return this.m_A;
    }
    public String aTipText() {
        return "Choose a value for A.";
    }

    /** This method allows you to set/get the offset for the
     * objective value.
     * @param Omega     The offset for the objective value.
     */
    public void setOmega(double Omega) {
        this.m_Omega = Omega;
    }
    public double getOmega() {
        return this.m_Omega;
    }
    public String omegaTipText() {
        return "Choose Omega.";
    }
	public boolean isDoRotation() {
		return doRotation;
	}
	public void setDoRotation(boolean doRotation) {
		this.doRotation = doRotation;
	}
}