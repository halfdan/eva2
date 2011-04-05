package eva2.server.go.problems;

import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.GradientDescentAlgorithm;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:03:09
 * To change this template use File | Settings | File Templates.
 */
public class F2Problem extends AbstractProblemDoubleOffset implements InterfaceLocalSearchable, InterfaceMultimodalProblem, java.io.Serializable, InterfaceFirstOrderDerivableProblem {

    private transient GradientDescentAlgorithm localSearchOptimizer=null;


	public F2Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F2Problem(F2Problem b) {
        super(b);     
    }
    public F2Problem(int dim) {
    	super(dim);
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
    	x = rotateMaybe(x);
        double[] result = new double[1];
        result[0]     = m_YOffset;
        double xi, xii;
        for (int i = 0; i < x.length-1; i++) {
        	xi=x[i]-m_XOffset;
        	xii=x[i+1]-m_XOffset;
            result[0]  += (100*(xii-xi*xi)*(xii-xi*xi)+(xi-1)*(xi-1));
        }
        if (m_YOffset==0 && (result[0]<=0)) result[0]=Math.sqrt(Double.MIN_VALUE); // guard for plots in log scale
        return result;
    }
    
	public double[] getFirstOrderGradients(double[] x) {
		x = rotateMaybe(x);
        int dim = x.length;
        double[] result = new double[dim];
        double xi, xii;
        
        for (int i = 0; i < dim-1; i++) {
        	xi=x[i]-m_XOffset;
        	xii=x[i+1]-m_XOffset;

        	result[i] += 400*xi*(xi*xi-xii) + 2*xi-2;
        	result[i+1] += -200 * (xi*xi - xii);
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
    public static String globalInfo() {
        return "Generalized Rosenbrock's function.";
    }

    public void doLocalSearch(Population pop) {
    	if (localSearchOptimizer == null) {
    		initLS();
    	}
    	localSearchOptimizer.setPopulation(pop);
    	localSearchOptimizer.optimize();
    }

    private void initLS() {
		localSearchOptimizer = new GradientDescentAlgorithm();
	    localSearchOptimizer.SetProblem(this);
	    localSearchOptimizer.init();
    }

    public double getLocalSearchStepFunctionCallEquivalent() {
    	double cost = 1;
    	if (this.localSearchOptimizer instanceof GradientDescentAlgorithm) {
    		cost = ((GradientDescentAlgorithm) localSearchOptimizer).getIterations();
    	}
    	return cost;
    }
}
