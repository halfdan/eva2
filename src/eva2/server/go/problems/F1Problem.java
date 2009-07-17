package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.constraint.GenericConstraint;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 17:58:55
 * To change this template use Options | File Templates.
 */
public class F1Problem extends AbstractProblemDouble implements Interface2DBorderProblem, java.io.Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4870484001737601464L;
//	protected AbstractEAIndividual      m_OverallBest       = null;
    protected int                       m_ProblemDimension  = 10;
    protected double                    m_XOffSet           = 0.0;
    protected double                    m_YOffSet           = 0.0;
//    protected boolean                   m_UseTestConstraint = false;

    public F1Problem() {
    	super();
    	setDefaultRange(10);
    }
    
    public F1Problem(F1Problem b) {
    	super();
    	super.cloneObjects(b);
    	this.m_ProblemDimension = b.m_ProblemDimension;
    	this.m_XOffSet          = b.m_XOffSet;
    	this.m_YOffSet          = b.m_YOffSet;
//    	this.m_UseTestConstraint = b.m_UseTestConstraint;
    }
    
    public F1Problem(int dim) {
    	this();
    	setProblemDimension(dim);
    }
    
    public F1Problem(int dim, double defRange) {
    	this(dim);
    	setDefaultRange(defRange);
    }
    
    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F1Problem(this);
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
//        this.m_OverallBest = null;
    	initTemplate();
    }

    /** Ths method allows you to evaluate a simple bit string to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[] result = new double[1];
        result[0]     = m_YOffSet;
        // add an offset in solution space
        for (int i = 0; i < x.length; i++) {
            result[0]  += Math.pow(x[i] - this.m_XOffSet, 2);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("F1 Sphere model:\n");
        sb.append("Here the individual codes a vector of real number x and F1(x)= x^2 is to be minimized.\nParameters:\n");
        sb.append("Dimension   : "); 
        sb.append(this.m_ProblemDimension);
        sb.append("\nNoise level : ");
        sb.append(this.getNoise());
//        sb.append("\nSolution representation:\n");
//		  sb.append(this.m_Template.getSolutionRepresentationFor());
        return sb.toString();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "F1-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "F1: multidimensional parabola problem";
    }

    /** This method allows you to set/get an offset for decision variables.
     * @param XOffSet     The offset for the decision variables.
     */
    public void setXOffSet(double XOffSet) {
        this.m_XOffSet = XOffSet;
    }
    public double getXOffSet() {
        return this.m_XOffSet;
    }
    public String xOffSetTipText() {
        return "Choose an offset for the decision variable.";
    }

    /** This method allows you to set/get the offset for the
     * objective value.
     * @param YOffSet     The offset for the objective value.
     */
    public void setYOffSet(double YOffSet) {
        this.m_YOffSet = YOffSet;
    }
    public double getYOffSet() {
        return this.m_YOffSet;
    }
    public String yOffSetTipText() {
        return "Choose an offset for the objective value.";
    }
    /** Length of the x vector at is to be optimized
     * @param t Length of the x vector at is to be optimized
     */
    public void setProblemDimension(int t) {
        this.m_ProblemDimension = t;
    }
    public int getProblemDimension() {
        return this.m_ProblemDimension;
    }
    public String problemDimensionTipText() {
        return "Length of the x vector to be optimized.";
    }
//    /** This method allows you to toggle the application of a simple test constraint.
//     * @param b     The mode for the test constraint
//     */
//    public void setUseTestConstraint(boolean b) {
//        this.m_UseTestConstraint = b;
//    }
//    public boolean getUseTestConstraint() {
//        return this.m_UseTestConstraint;
//    }
//    public String useTestConstraintTipText() {
//        return "Just a simple test constraint of x[0] >= 1.";
//    }
}
