package javaeva.server.go.strategies;

import javaeva.gui.GenericObjectEditor;
import javaeva.gui.Plot;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.individuals.InterfaceESIndividual;
import javaeva.server.go.operators.mutation.MutateESFixedStepSize;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.selection.SelectXProbRouletteWheel;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;

import java.util.ArrayList;

/** This is a Particel Filter implemented by Frank Senke, only some documentation
 * here and not throughfully checked, whether this works on arbitrary problem
 * instances.
 * This is a implementation of Genetic Algorithms.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class ParticleFilterOptimization implements InterfaceOptimizer, java.io.Serializable {

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	private Population                      m_Population        = new Population();
    private InterfaceOptimizationProblem    m_Problem           = new F1Problem();
    private InterfaceSelection              m_ParentSelection   = new SelectXProbRouletteWheel();//new SelectParticleWheel();
    private ArrayList                       tmpA;
    //private boolean							m_UseElitism		= true;

    private String					m_Identifier = "";
    private boolean 				withShow = false;
    private double					mutationSigma = 0.05;

    private int 					sleepTime = 0;
    
    transient private int indCount = 0;
    transient private InterfacePopulationChangedEventListener m_Listener;
    transient Plot myPlot = null;

    public ParticleFilterOptimization() {
        tmpA = new ArrayList(100);
        for (int i = 0; i < 100; i++) tmpA.add(new EvolutionStrategies());
        if (withShow) setWithShow(true);
    }

    public ParticleFilterOptimization(ParticleFilterOptimization a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Identifier                   = a.m_Identifier;
        //this.m_Plague                       = a.m_Plague;
        //this.m_NumberOfPartners             = a.m_NumberOfPartners;
        //this.m_UseElitism                   = a.m_UseElitism;
        this.m_ParentSelection              = (InterfaceSelection)a.m_ParentSelection.clone();
        if (a.withShow) setWithShow(true);
    }

    public Object clone() {
        return (Object) new ParticleFilterOptimization(this);
    }

    public void init() {
        this.m_Problem.initPopulation(this.m_Population);    	
        for (int i=0; i<m_Population.size(); i++) {
        	((AbstractEAIndividual)m_Population.getIndividual(i)).setMutationOperator(new MutateESFixedStepSize(mutationSigma));
        }

        setWithShow(withShow);
        this.evaluatePopulation(this.m_Population);  
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        if (reset) this.m_Population.init();
        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
    private Population evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
        return population;
    }

	/**
	 * This method will resample the given population using EA parent selection.
	 * 
     */
    protected Population resample(Population pop) {
        Population parents;
        
        this.m_ParentSelection.prepareSelection(pop);
        
        // Generate a Population of Parents with Parantselectionmethod.
        // DONT forget cloning -> selection does only shallow copies!
        parents = (Population)(this.m_ParentSelection.selectFrom(pop, this.m_Population.getPopulationSize())).clone();
        parents.SetFunctionCalls(pop.getFunctionCalls());
        parents.setGenerationTo(pop.getGeneration());
        
        return parents;
    }
    
    protected void predict(Population pop) {
    	indCount = 0;
        for (int i = 0; i < pop.getPopulationSize(); i++) {
        	applyMotionModel((AbstractEAIndividual)((AbstractEAIndividual)pop.get(i)), 0.);
        	indCount++;
        }
    }

    protected void applyMotionModel(AbstractEAIndividual indy, double noise) {
    	// this currently only performs a mutation
    	indy.mutate();
    	indy.SetFitness(0, 0);
    	
    	if (this.withShow) {
    		InterfaceESIndividual endy = (InterfaceESIndividual) indy;
	        double[] curPosition    = endy.getDGenotype();

	        myPlot.setUnconnectedPoint(curPosition[0], curPosition[1], indCount);
	        myPlot.setConnectedPoint(curPosition[0], curPosition[1], indCount);
  
//            this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index+1);
//            this.m_Plot.setConnectedPoint(localBestPosition[0], localBestPosition[1], index+1);
//            this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index+1);
//            this.m_Plot.setConnectedPoint(bestPosition[0], bestPosition[1], index+1);
//            this.m_Plot.setUnconnectedPoint(curPosition[0], curPosition[1], 100*index+1);
        }
    }
    
    /**
     * Optimization loop of a resampling particle filter, restructured by MK.
     * 
     */
    public void optimize() {
        Population nextGeneration;
        //AbstractEAIndividual   elite;
        
        // resample using selection
        nextGeneration = resample(m_Population);

        // predict step
        predict(nextGeneration);
        
        m_Population = evaluatePopulation(nextGeneration);
        
        collectStatistics(m_Population);
        
        this.firePropertyChangedEvent("NextGenerationPerformed");
        
		if (sleepTime > 0 ) try { Thread.sleep(sleepTime); } catch(Exception e) {}
    }

    protected void collectStatistics(Population population) {
		// TODO Auto-generated method stub
		int tMax = 5;
		
	}

	/** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
    /** Something has changed
     */
    protected void firePropertyChangedEvent (String name) {
        if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
    }

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void SetProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
    }
    public InterfaceOptimizationProblem getProblem () {
        return this.m_Problem;
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
    	StringBuilder strB=new StringBuilder(200);
        strB.append("Particle Filter:\nOptimization Problem: ");
        strB.append(this.m_Problem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.m_Population.getStringRepresentation());
        return strB.toString();
    }
    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
     public void SetIdentifier(String name) {
        this.m_Identifier = name;
    }
     public String getIdentifier() {
         return this.m_Identifier;
     }

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    public void freeWilly() {

    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a Particle Filter Algorithm.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "PF";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return this.m_Population;
    }
    public void setPopulation(Population pop){
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
    
    public Population getAllSolutions() {
    	return getPopulation();
    }
    /** This method will set the selection method that is to be used
     * @param selection
     */
    public void setParentSelection(InterfaceSelection selection) {
        this.m_ParentSelection = selection;
    }
    public InterfaceSelection getParentSelection() {
        return this.m_ParentSelection;
    }
    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
    }

	/**
	 * @return the withShow
	 **/
	public boolean isWithShow() {
		return withShow;
	}

	/**
	 * @param withShow the withShow to set
	 **/
	public void setWithShow(boolean wShow) {
		this.withShow = wShow;
		if (!wShow) {
			GenericObjectEditor.setHideProperty(this.getClass(), "mutationSigma", true);
		} else {
			GenericObjectEditor.setHideProperty(this.getClass(), "mutationSigma", false);
		}
		if (!withShow) myPlot = null;
		else {
		    double[][] range;
			if ((m_Population != null) && (m_Population.size() > 0)) range = ((InterfaceDataTypeDouble)this.m_Population.get(0)).getDoubleRange();
			else {
				range = new double[2][];
				range[0] = new double[2];
				range[0][0] = 0;
				range[0][1] = 0;
				range[1] = range[0]; // this is evil
			}
		    myPlot = new javaeva.gui.Plot("PF", "x1", "x2", true);
		}
	}

	/**
	 * @return the sleepTime
	 **/
	public int getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param sleepTime the sleepTime to set
	 **/
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * @return the mutationSigma
	 **/
	public double getMutationSigma() {
		return mutationSigma;
	}

	/**
	 * @param mutationSigma the mutationSigma to set
	 **/
	public void setMutationSigma(double mutationSigma) {
		this.mutationSigma = mutationSigma;
	}
    
//    /** This method will set the problem that is to be optimized
//     * @param elitism
//     */
//    public void setElitism (boolean elitism) {
//        this.m_UseElitism = elitism;
//    }
//    public boolean getElitism() {
//        return this.m_UseElitism;
//    }
//    public String elitismTipText() {
//        return "Enable/disable elitism.";
//    }
 
}
