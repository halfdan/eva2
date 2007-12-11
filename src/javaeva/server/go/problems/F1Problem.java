package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.tools.RandomNumberGenerator;

import javaeva.server.stat.Statistics;
import javaeva.gui.JEFrame;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 17:58:55
 * To change this template use Options | File Templates.
 */
public class F1Problem extends AbstractOptimizationProblem implements Interface2DBorderProblem, java.io.Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4870484001737601464L;
	protected AbstractEAIndividual      m_OverallBest       = null;
    protected int                       m_ProblemDimension  = 10;
    protected double                    m_Noise             = 0.0;
    protected double                    m_XOffSet           = 0.0;
    protected double                    m_YOffSet           = 0.0;
    protected boolean                   m_UseTestConstraint = false;
    protected double					defaultRange		= 5.12;

    public F1Problem() {
        this.m_Template         = new ESIndividualDoubleData();
        ((ESIndividualDoubleData)this.m_Template).setDoubleDataLength(m_ProblemDimension);
        ((ESIndividualDoubleData)this.m_Template).SetDoubleRange(makeRange());
    }
    public F1Problem(F1Problem b) {
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
        return (Object) new F1Problem(this);
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
        this.m_OverallBest = null;
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        this.m_OverallBest = null;

        population.clear();

        ((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());

        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
    }
    
    protected double[][] makeRange() {
	    double[][] range = new double[this.m_ProblemDimension][2];
	    for (int i = 0; i < range.length; i++) {
	        range[i][0] = getRangeLowerBound(i);
	        range[i][1] = getRangeUpperBound(i);
	    }
	    return range;
    }
    
    protected double getRangeLowerBound(int dim) {
    	return -defaultRange;
    }
    
    protected double getRangeUpperBound(int dim) {
    	return defaultRange;
    }
    
    protected double[][] getDoubleRange() {
    	return ((InterfaceDataTypeDouble)this.m_Template).getDoubleRange();                             
    }

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population) {
        AbstractEAIndividual    tmpIndy;
        //System.out.println("Population size: " + population.size());
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual) population.get(i);
            tmpIndy.resetConstraintViolation();
            this.evaluate(tmpIndy);
            population.incrFunctionCalls();
        }
    }

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
        double[]        fitness;

        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);

        for (int i = 0; i < x.length; i++) x[i] = x[i] - this.m_XOffSet;
        fitness = this.doEvaluation(x);
        for (int i = 0; i < fitness.length; i++) {
            // add noise to the fitness
            if (m_Noise != 0) fitness[i] += RandomNumberGenerator.gaussianDouble(this.m_Noise);
            fitness[i] += this.m_YOffSet;
            // set the fitness of the individual
            individual.SetFitness(i, fitness[i]);
        }
        if (this.m_UseTestConstraint) {
            if (x[0] < 1) individual.addConstraintViolation(1-x[0]);
        }
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
        }
    }

    /** Ths method allows you to evaluate a simple bit string to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
        double[] result = new double[1];
        result[0]     = 0;
        for (int i = 0; i < x.length; i++) {
            result[0]  += Math.pow(x[i], 2);
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
        sb.append(this.m_Noise);
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
        return "F1 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Sphere model.";
    }

    /** This method allows you to choose how much noise is to be added to the
     * fitness. This can be used to make the optimization problem more difficult.
     * @param noise     The sigma for a gaussian random number.
     */
    public void setNoise(double noise) {
        if (noise < 0) noise = 0;
        this.m_Noise = noise;
    }
    public double getNoise() {
        return this.m_Noise;
    }
    public String noiseTipText() {
        return "Noise level on the fitness value.";
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
        return "Length of the x vector at is to be optimized.";
    }
    /** This method allows you to toggle the application of a simple test constraint.
     * @param b     The mode for the test constraint
     */
    public void setUseTestConstraint(boolean b) {
        this.m_UseTestConstraint = b;
    }
    public boolean getUseTestConstraint() {
        return this.m_UseTestConstraint;
    }
    public String useTestConstraintTipText() {
        return "Just a simple test constraint of x[0] >= 1.";
    }

    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.m_Template = (AbstractEAIndividual)indy;
    }
    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble)this.m_Template;
    }
	public double functionValue(double[] point) {
		double x[] = new double[m_ProblemDimension];
		for (int i=0; i<point.length; i++) x[i]=point[i];
		for (int i=point.length; i<m_ProblemDimension; i++) x[i] = 0;
		return Math.sqrt(doEvaluation(x)[0]);
	}
	public double[][] get2DBorder() {
		return getDoubleRange();
	}
	
	/**
	 * A (symmetric) absolute range limit.
	 * 
	 * @return value of the absolute range limit
	 */
	public double getDefaultRange() {
		return defaultRange;
	}
	
	/**
	 * Set a (symmetric) absolute range limit.
	 * 
	 * @param defaultRange
	 */
	public void setDefaultRange(double defaultRange) {
		this.defaultRange = defaultRange;
		if (((InterfaceDataTypeDouble)this.m_Template).getDoubleData().length != m_ProblemDimension) {
			((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(m_ProblemDimension);
		}
		((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());
	}
	
	public String defaultRangeTipText() {
		return "Absolute limit for the symmetric range in any dimension (not used for all f-problems)";
	}
}
