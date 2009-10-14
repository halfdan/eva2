package eva2.server.go.problems;

import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.tools.Mathematics;
import eva2.tools.math.Jama.Matrix;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.06.2005
 * Time: 13:09:36
 * To change this template use File | Settings | File Templates.
 */
public class F6Problem extends F1Problem implements InterfaceMultimodalProblem, java.io.Serializable {

	private boolean 		doRotation = false;
    private double          m_A     = 10;
    private double          m_Omega = 2*Math.PI;
    private Matrix 			rotation;

    public F6Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F6Problem(F6Problem b) {
        super(b);       
        doRotation 				= b.doRotation;
        this.m_A                = b.m_A;
        this.m_Omega            = b.m_Omega;
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
        super.initProblem();
        if (doRotation) {
        	rotation = new Matrix(m_ProblemDimension, m_ProblemDimension);
        	Matrix vec = new Matrix(m_ProblemDimension, 1);
        	for (int i=0; i<m_ProblemDimension; i++) vec.set(i,0, i+1);
        	rotation = Mathematics.getRotationMatrix(vec).transpose();
        } else rotation = null;
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
    public double[] eval(double[] x) {
    	
    	if (doRotation) {
	    	Matrix resVec = rotation.times(new Matrix(x, x.length));
	    	x = resVec.getColumnPackedCopy();
    	}
        double[] result = new double[1];
        result[0]     = x.length * this.m_A + m_YOffSet;
        for (int i = 0; i < x.length; i++) {
        	double xi = x[i]-m_XOffSet;
            result[0]  += Math.pow(xi, 2) - this.m_A * Math.cos(this.m_Omega*xi);
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