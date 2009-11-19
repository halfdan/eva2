package eva2.server.go.problems;

import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 17:58:55
 * To change this template use Options | File Templates.
 */
public class F1Problem extends AbstractProblemDoubleOffset implements Interface2DBorderProblem, java.io.Serializable, InterfaceFirstOrderDerivableProblem {
    public F1Problem() {
    	super();
    	setDefaultRange(10);
    }
    
    public F1Problem(F1Problem b) {
    	super();
    	super.cloneObjects(b);
    }
    
    public F1Problem(int dim) {
    	super(dim);
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

	public double[] getFirstOrderGradients(double[] x) {
		// first order partial derivation in direction x_i is 2*x_i
		double[] grads=new double[x.length];
		for (int i=0; i<x.length; i++) {
			grads[i]=(2.*(x[i] - this.m_XOffSet));
		}
		return grads;
	}
}
