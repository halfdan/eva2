package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import wsi.ra.math.RNG;
import eva2.server.go.problems.Interface2DBorderProblem;

/**
 * For a double valued problem, there are two main methods to implement: {@link #getProblemDimension()}
 * must return the problem dimension, while {@link #eval(double[])} is to evaluate a single double
 * vector into the result fitness vector. 
 * 
 * To define the problem range, you may use the default range parameter resulting in a symmetric
 * double range [-defaultRange,defaulRange] in all dimensions.
 * Or you may implement {@link #getRangeLowerBound(int)} and {@link #getRangeUpperBound(int)}
 * to define an arbitrary problem range. In that case, the default range parameter is not used.
 * 
 * Anything you want to do before any optimization is started on the problem should go into
 * {@link #initProblem()}, but remember to call the super-method in your implementation. The 
 * individual template will be initialized to an ESIndividualDoubleData by then.
 * 
 * For the GUI, it is also convenient to implement the {@link #globalInfo()} and {@link #getName()}
 * methods to provide some distinctive information for the user.
 * 
 * 
 * @author mkron
 *
 */
public abstract class AbstractProblemDouble extends AbstractOptimizationProblem implements InterfaceProblemDouble, Interface2DBorderProblem {
	private double m_DefaultRange = 10;
	private double m_Noise = 0;
	
	public AbstractProblemDouble() {
		initTemplate();
	}
	
//	public AbstractProblemDouble(AbstractProblemDouble o) {
//		cloneObjects(o);
//	}
	
	protected void initTemplate() {
		if (m_Template == null) m_Template         = new ESIndividualDoubleData();
		if (getProblemDimension() > 0) { // avoid evil case setting dim to 0 during object init
			((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(getProblemDimension());
			((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());
		}
	}
	
	protected void cloneObjects(AbstractProblemDouble o) {
		this.m_DefaultRange = o.m_DefaultRange;
		this.m_Noise = o.m_Noise;
		if (o.m_Template != null) this.m_Template = (AbstractEAIndividual)o.m_Template.clone();
	}
	
	/**
	 * Retrieve and copy the double solution representation from an individual.
	 * 
	 * @param individual
	 * @return the double solution representation
	 */
	protected double[] getEvalArray(AbstractEAIndividual individual){
		double[] x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        return x;
	}
	
	/**
	 * When implementing a double problem, inheriting classes should not override this method (or only
	 * extend it) and do the fitness calculations in the method eval(double[]).
	 * 
	 * @see eval(double[] x)
	 */
	public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
        double[]        fitness;

        x = getEvalArray(individual);
        // evaluate the vector
        fitness = this.eval(x);
        // if indicated, add Gaussian noise
        if (m_Noise != 0) RNG.addNoise(fitness, m_Noise); 
        // set the fitness
        setEvalFitness(individual, x, fitness);
	}
	
	/**
	 * Write a fitness value back to an individual. May be overridden to add constraints.
	 *  
	 * @param individual
	 * @param x
	 * @param fit
	 */
	protected void setEvalFitness(AbstractEAIndividual individual, double[] x, double[] fit) {
		individual.SetFitness(fit);
	}
	
	/**
	 * Evaluate a double vector, representing the target function.
	 * 
	 * @param x the vector to evaluate
	 * @return	the target function value
	 */
	public abstract double[] eval(double[] x);
	
	/**
	 * Get the problem dimension.
	 * 
	 * @return the problem dimension
	 */
	public abstract int getProblemDimension();
	
	@Override
	public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;
        population.clear();
        initTemplate();
        
        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
	}
	
	/**
	 * Create a new range array by using the getRangeLowerBound and getRangeUpperBound methods.
	 * 
	 * @return a range array
	 */
    public double[][] makeRange() {
	    double[][] range = new double[this.getProblemDimension()][2];
	    for (int i = 0; i < range.length; i++) {
	        range[i][0] = getRangeLowerBound(i);
	        range[i][1] = getRangeUpperBound(i);
	    }
	    return range;
    }
    
    /**
     * Get the lower bound of the double range in the given dimension. Override
     * this to implement non-symmetric ranges. User setDefaultRange for symmetric ranges.
     * 
     * @see makeRange()
     * @see getRangeUpperBound(int dim)
     * @param dim
     * @return the lower bound of the double range in the given dimension
     */
    protected double getRangeLowerBound(int dim) {
    	return -getDefaultRange();
    }
    
    /**
     * Get the upper bound of the double range in the given dimension. Override
     * this to implement non-symmetric ranges. User setDefaultRange for symmetric ranges.
     * 
     * @see makeRange()
     * @see getRangeLowerBound(int dim)
     * @param dim
     * @return the upper bound of the double range in the given dimension
     */
    protected double getRangeUpperBound(int dim) {
    	return getDefaultRange();
    }

	@Override
	public void initProblem() {
		initTemplate();
	}
	
    /** 
     * This method allows you to choose how much noise is to be added to the
     * fitness. This can be used to make the optimization problem more difficult.
     * @param noise     The sigma for a gaussian random number.
     */
    public void setNoise(double noise) {
        if (noise < 0) noise = 0;
        this.m_Noise = noise;
    }
    /**
     * Get the current noise level.
     * @return the current noise level
     */
    public double getNoise() {
        return this.m_Noise;
    }
    public String noiseTipText() {
        return "Gaussian noise level on the fitness value.";
    }

    /** 
     * This method allows you to choose the EA individual used by the problem.
     * 
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.m_Template = (AbstractEAIndividual)indy;
    }
    
    /**
     * Get the EA individual template currently used by the problem.
     * 
     * @return the EA individual template currently used
     */
    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble)this.m_Template;
    }
    
    public String EAIndividualTipText() {
    	return "Set the base individual type defining the data representation and mutation/crossover operators";
    }
    
	/**
	 * A (symmetric) absolute range limit.
	 * 
	 * @return value of the absolute range limit
	 */
	public double getDefaultRange() {
		return m_DefaultRange;
	}
	/**
	 * Set a (symmetric) absolute range limit.
	 * 
	 * @param defaultRange
	 */
	public void setDefaultRange(double defaultRange) {
		this.m_DefaultRange = defaultRange;
		initTemplate();
	}
	public String defaultRangeTipText() {
		return "Absolute limit for the symmetric range in any dimension";
	}
    
	
    /**********************************************************************************************************************
     * These are for Interface2DBorderProblem
     */
    public double[][] get2DBorder() {
    	return makeRange();
    }

    
	public double functionValue(double[] point) {
		double x[] = new double[getProblemDimension()];
		for (int i=0; i<point.length; i++) x[i]=point[i];
		for (int i=point.length; i<x.length; i++) x[i] = 0;
		return eval(x)[0];
	}
    /**********************************************************************************************************************
     * These are for GUI
     */
	
    /** 
     * This method allows the GUI to read the
     * name to the current object.
     * @return the name of the object
     */
    public String getName() {
    	return "AbstractProblemDouble";
    }

    /** 
     * This method returns a global info string.
     * @return description
     */
    public String globalInfo() {
    	return "The programmer did not give further details.";
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("A double valued problem: ");
        sb.append(this.getName());
        sb.append("\n");
        sb.append(globalInfo());
        sb.append("Dimension   : "); 
        sb.append(this.getProblemDimension());
        sb.append("\nNoise level : ");
        sb.append(this.m_Noise);
        return sb.toString();
    }
}
