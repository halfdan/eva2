package eva2.optimization.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.populations.Population;
import eva2.optimization.strategies.InterfaceOptimizer;
import java.util.Random;


public abstract class AbstractDynTransProblem extends AbstractSynchronousOptimizationProblem {

	private static final long serialVersionUID = 2361612076300958044L;

	protected InterfaceOptimizationProblem prob;
	protected AbstractEAIndividual bestIndividual = null;

	
	/* for the random number generator, so that every solution is traceable */
	protected int seed 					=   1;
	protected Random rand = new Random(seed);
	protected int problemDimension;
	protected double[][] range;
	
	public AbstractDynTransProblem() {
		super();
		setProblem(new F1Problem());
		setFrequencyRelative(true);
	}

	/** 
	 * Returns the instance of the object.
	 *
	 * @return the problem instance of the object.
	 */
	protected AbstractOptimizationProblem getInstance() {
		return this;
	}
	
	/**
	 * Evaluate the function at the individuals position using an arbitrary translation which may be dynamically changing.
	 * 
	 * @param individual	the individual to be evaluated
	 * @param t				timestamp of the evaluation
	 */
    @Override
	public void evaluateAt(AbstractEAIndividual individual, double time) {
		/* the fitness ist set by the evaluate function */
		AbstractEAIndividual tussy = (AbstractEAIndividual)individual.clone();
		transform(tussy, time);
        getProblem().evaluate(tussy);
        individual.setFitness(tussy.getFitness());       
	}

	/**
	 * Override population evaluation to do some data output.
	 * 
	 */
    @Override
	public void evaluatePopulationEnd(Population population) {
		double delta = transLength(getCurrentProblemTime());
		if (isExtraPlot() == true) {
			if (myplot != null) {
				//	myplot.addGraph(0, 1);
				myplot.jump();
			} else {
				if (TRACE) {
                                System.out.println("creating myplot instance");
                            }
				double[] tmpD = new double[2];
				tmpD[0] = 0;
				tmpD[1] = 0;
				// im not really certain about what tmpD is required for
				this.myplot = new eva2.gui.Plot("population measures", "x1", "x2", tmpD, tmpD);
			}			
			myplot.setConnectedPoint(population.getFunctionCalls(), delta, 0);
			//myplot.setUnconnectedPoint(population.getFunctionCalls(), population.getPopulationMeasures()[2], 2);
		}
		else {
                myplot = null;
            }
	}
	
	private double transLength(double time) {
		double ret = 0.;
		for (int i = 0; i < getProblemDimension(); i++) {
            ret += Math.pow(getTranslation(i, time), 2.);
        }
		return Math.sqrt(ret);
	}
	
	/**
	 * Returns the translation in the given dimension at the given time.
	 * 
	 * @param dim	the dimension
	 * @param time	the simulation time
	 * @return the translation in the given dimension at the given time
	 */
	protected abstract double getTranslation(int dim, double time);
	
	protected void transform(AbstractEAIndividual individual, double time) {
		double[] indyData = ((InterfaceDataTypeDouble)individual).getDoubleData();
		for (int i = 0; i < indyData.length; ++i) {
			/* individuum moves towords untranslated problem */
			indyData[i] -= getTranslation(i, time);
		}
		((InterfaceDataTypeDouble)individual).SetDoubleGenotype(indyData);
	}

	/*
	 * Initializes the underlying problem in the problem class 
	 */
    @Override
	public void initializeProblem() {
		super.initializeProblem();
		bestIndividual = null;
		getProblem().initializeProblem();
	}


    /**
     * Whenever the environment (or the time, primarily) has changed, some problem
     * properties (like stored individual fitness) may require updating.
     * 
     * @param severity the severity of the change (time measure)
     */
    @Override
	public void resetProblem(double severity) {
		if ((prob != null) && (bestIndividual != null)) {
                this.evaluateAt(bestIndividual, getCurrentProblemTime());
            }
	}
	
	/* inits the population in the problem itself
	 * 
	 */
    @Override
	public void initPopulationAt(Population population, double time) {
		if (TRACE) {
                System.out.println("DynTransProblem at " + this + " initPop, problem is " + getProblem());
            }
		getProblem().initializePopulation(population);
		for (int i = 0; i < population.size(); i++) {
            ((AbstractEAIndividual)population.get(i)).SetAge(0);
        }
	}
	
	public int getProblemDimension() {
		return problemDimension;
	}

/******************************************************************************
 * These are for the GUI
 */
    @Override
	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "DynTransProblem";
	}

	/**
	 * This is for the Scroll Box:
	 * in JParaPanel.java package eva2.gui
	 * in installActions()
	 * PropertyEditorManager.registerEditor(AbstractOptimizationProblem.class, GenericObjectEditor.class);
	 * don't forget the import statement
	 * 
	 */
	public InterfaceOptimizationProblem getProblem() {
		return prob;
	}
	public void setProblem(InterfaceOptimizationProblem prob) {
		this.prob = prob;
		/* to get the right values for problemDimension and Range */
		Population pop = new Population();
		pop.setTargetSize(1);		
		prob.initializePopulation(pop);
		AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(0);
		if (indy instanceof InterfaceDataTypeDouble) {
			problemDimension = ((InterfaceDataTypeDouble)indy).getDoubleRange().length;
			range = ((InterfaceDataTypeDouble)indy).getDoubleRange();
		} else {
			System.out.println("Wrong Problem");
		}
		pop.clear();
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
		this.rand = new Random(seed);
	}
}
