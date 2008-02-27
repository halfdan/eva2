package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.tools.RandomNumberGenerator;

public abstract class AbstractProblemDouble extends AbstractOptimizationProblem {
	protected double m_DefaultRange = 10;
	protected double m_Noise = 0;
	
	public AbstractProblemDouble() {
		initTemplate();
	}
	
	protected void initTemplate() {
		this.m_Template         = new ESIndividualDoubleData();
		((ESIndividualDoubleData)this.m_Template).setDoubleDataLength(getProblemDimension());
		((ESIndividualDoubleData)this.m_Template).SetDoubleRange(makeRange());
	}
	
	@Override
	public Object clone() {
		try {
			AbstractProblemDouble prob = this.getClass().newInstance();
			prob.m_DefaultRange = m_DefaultRange;
			prob.m_Noise = m_Noise;
			prob.m_Template = (AbstractEAIndividual)m_Template.clone();
			return prob;
		} catch(Exception e) {
			System.err.println("Error: couldnt instantiate "+this.getClass().getName());
			return null;
		}
	}
	
	protected double[] getEvalArray(AbstractEAIndividual individual){
		double[] x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        return x;
	}
	
	@Override
	public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
        double[]        fitness;

        x = getEvalArray(individual);
        // evaluate the vector
        fitness = this.eval(x);
        // if indicated, add Gaussian noise
        if (m_Noise != 0) RandomNumberGenerator.addNoise(fitness, m_Noise); 
        // set the fitness
        setEvalFitness(individual, x, fitness);
	}
	
	protected void setEvalFitness(AbstractEAIndividual individual, double[] x, double[] fit) {
		individual.SetFitness(fit);
	}
	
	/**
	 * Evaluate a double vector 
	 * @param x
	 * @return
	 */
	public abstract double[] eval(double[] x);
	
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
	
    protected double[][] makeRange() {
	    double[][] range = new double[this.getProblemDimension()][2];
	    for (int i = 0; i < range.length; i++) {
	        range[i][0] = getRangeLowerBound(i);
	        range[i][1] = getRangeUpperBound(i);
	    }
	    return range;
    }
    
    protected double getRangeLowerBound(int dim) {
    	return -m_DefaultRange;
    }
    
    protected double getRangeUpperBound(int dim) {
    	return m_DefaultRange;
    }

	@Override
	public void initProblem() {
		initTemplate();
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
        return "Gaussian noise level on the fitness value.";
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
		if (((InterfaceDataTypeDouble)this.m_Template).getDoubleData().length != getProblemDimension()) {
			((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(getProblemDimension());
		}
		((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());
	}
	public String defaultRangeTipText() {
		return "Absolute limit for the symmetric range in any dimension";
	}
    
    /**********************************************************************************************************************
     * These are for GUI
     */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
    	return "SimpleProblemDouble";
    }

    /** This method returns a global info string
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
        sb.append("A double valued problem:\n");
        sb.append(globalInfo());
        sb.append("Dimension   : "); 
        sb.append(this.getProblemDimension());
        sb.append("\nNoise level : ");
        sb.append(this.m_Noise);
        return sb.toString();
    }
}
