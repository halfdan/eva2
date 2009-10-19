package eva2.server.go.problems;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:40:28
 * To change this template use File | Settings | File Templates.
 */
public class F8Problem extends F1Problem implements InterfaceMultimodalProblem, java.io.Serializable {

    private double      a = 20;
    private double      b = 0.2;
    private double      c = 2*Math.PI;
    final static double f8Range = 32.768;

    public F8Problem() {
        setDefaultRange(f8Range);
    }
    public F8Problem(F8Problem b) {
        super(b);
        this.a = b.a;
        this.b = b.b;
        this.c = b.c;
    }

    public F8Problem(int dim) {
		super(dim);
		setDefaultRange(f8Range);
	}
	/** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F8Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[]        result = new double[1];
        double          sum1 = 0, sum2 = 0, exp1, exp2;

        for (int i = 0; i < x.length; i++) {
        	double xi = x[i]-m_XOffSet;
        	sum1 += (xi)*(xi);
        	sum2 += Math.cos(c * (xi));
        }
        exp1    = -b*Math.sqrt(sum1/(double)this.m_ProblemDimension);
        exp2    = sum2/(double)this.m_ProblemDimension;
        result[0] = m_YOffSet + a +  Math.E  - a * Math.exp(exp1)- Math.exp(exp2);

        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F8 Ackley's function.\n";
        result += "This problem is multimodal.\n";
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
        return "F8-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Ackley's function.";
    }
}