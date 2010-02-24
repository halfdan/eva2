package eva2.server.go.strategies;

import java.util.Hashtable;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectBestIndividuals;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceLocalSearchable;
import eva2.server.go.problems.InterfaceOptimizationProblem;


/**
 * A memetic algorithm by hannes planatscher. The local search strategy can only
 * be applied to problems which implement the InterfaceLocalSearchable else the
 * local search will not be activated at all.
 * <p>
 * Title: EvA2
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class MemeticAlgorithm implements InterfaceOptimizer,
    java.io.Serializable {

	/**
   * serial version uid.
   */
  private static final long serialVersionUID = -1730086430763348568L;

	private int	                                              localSearchSteps	= 1;

	private int	                                              subsetsize	      = 5;

	private int	                                              globalSearchIterations	= 1;

	private boolean	                                          lamarckism	      = true;

	// int counter = 0; !?
	// int maxfunctioncalls = 1000; !?

	private boolean	                                          TRACE	            = false;

	private String	                                          m_Identifier	    = "";

	private InterfaceOptimizationProblem	                    m_Problem	        = new F1Problem();

	private InterfaceOptimizer	                              m_GlobalOptimizer	= new GeneticAlgorithm();

	private InterfaceSelection	                              selectorPlug	    = new SelectBestIndividuals();

	transient private InterfacePopulationChangedEventListener	m_Listener;

	public MemeticAlgorithm() {

	}

	public MemeticAlgorithm(MemeticAlgorithm a) {
		// this.m_Population = (Population)a.m_Population.clone();
		this.m_Problem = (InterfaceLocalSearchable) a.m_Problem.clone();
		this.m_GlobalOptimizer = (InterfaceOptimizer) a.m_GlobalOptimizer;
		this.selectorPlug = (InterfaceSelection) a.selectorPlug;
		this.m_Identifier = a.m_Identifier;
		this.localSearchSteps = a.localSearchSteps;
		this.subsetsize = a.subsetsize;
		this.globalSearchIterations = a.globalSearchIterations;
		this.lamarckism = a.lamarckism;
	}

	@Override
	public Object clone() {
		return new MemeticAlgorithm(this);
	}

	public void initByPopulation(Population pop, boolean reset) {
		this.setPopulation((Population) pop.clone());
		if (reset) {
			this.getPopulation().init();
			this.m_Problem.evaluate(this.getPopulation());
			this.firePropertyChangedEvent(Population.nextGenerationPerformed);
		}
	}

	public void init() {
		// counter = 0;
		this.m_GlobalOptimizer.SetProblem(this.m_Problem);
		this.m_GlobalOptimizer.init();
		this.evaluatePopulation(this.m_GlobalOptimizer.getPopulation());
		this.firePropertyChangedEvent(Population.nextGenerationPerformed);
	}

	/**
	 * This method will evaluate the current population using the given problem.
	 *
	 * @param population
	 *          The population that is to be evaluated
	 */
	private void evaluatePopulation(Population population) {
		this.m_Problem.evaluate(population);
		population.incrGeneration();
	}

	public void optimize() {

		if (TRACE) System.out.println("global search");
		this.m_GlobalOptimizer.optimize();

		if ((globalSearchIterations>0) && (((this.m_GlobalOptimizer.getPopulation().getGeneration() % this.globalSearchIterations) == 0))
		    && (this.localSearchSteps > 0)
		    && (this.m_Problem instanceof InterfaceLocalSearchable)) {
			// here the local search is performed
			if (TRACE)
			  System.out.println("Performing local search on " + subsetsize
			      + " individuals.");
			Population gop = this.m_GlobalOptimizer.getPopulation();
			Population subset = selectorPlug.selectFrom(gop, subsetsize);
			Population subsetclone = new Population();
			for (int i = 0; i < subset.size(); i++) {
				subsetclone.add(((AbstractEAIndividual) subset.get(i)).clone());
			}
			if (subset.size() != subsetsize)
			  System.err.println("ALERT! identical individual instances in subset");
			Hashtable antilamarckismcache = new Hashtable();
			if (!this.lamarckism) {
				for (int i = 0; i < subset.size(); i++) {
					AbstractEAIndividual indy = (AbstractEAIndividual) subset.get(i);
					AbstractEAIndividual indyclone = (AbstractEAIndividual) subsetclone
					    .get(i);
					antilamarckismcache.put(indy, indyclone);
				}
			}

			// int dosearchsteps = this.localSearchSteps;
			double cost = ((InterfaceLocalSearchable) this.m_Problem)
			    .getLocalSearchStepFunctionCallEquivalent();
			// int calls = gop.getFunctionCalls() + (int) Math.round(localSearchSteps
			// * cost * subset.size());
			// nett aber total unn�tig-falsch man kann nicht davon ausgehen, dass man
			// einen Fitnesscall Terminator hat..
			// if (calls > this.maxfunctioncalls) {
			// int remainingfunctioncalls = this.maxfunctioncalls -
			// gop.getFunctionCalls();
			// dosearchsteps = (int)Math.floor(((double) remainingfunctioncalls) /
			// (cost * subsetsize));
			// stopit = true;
			// }
			for (int i = 0; i < localSearchSteps; i++) {
				((InterfaceLocalSearchable) this.m_Problem).doLocalSearch(subsetclone);
			}
			this.m_Problem.evaluate(subsetclone);
			if (this.lamarckism) {
				gop.removeAll(subset);
				gop.addPopulation(subsetclone);
			} else {
				for (int i = 0; i < subset.size(); i++) {
					AbstractEAIndividual indy = (AbstractEAIndividual) subset.get(i);
					try {
						AbstractEAIndividual newindy = (AbstractEAIndividual) antilamarckismcache
						    .get(indy);
						indy.SetFitness(newindy.getFitness());
					} catch (Exception ex) {
						System.err.println("individual not found in antilamarckismcache");
					}
				}
			}
			// eigentlich muss hier noch subsetsize drauf, aber lassen wir das
			gop.SetFunctionCalls(gop.getFunctionCalls()
			    + (int) Math.round(localSearchSteps * cost * subset.size()));

			if (TRACE)
			  System.out.println("Population size after local search:" + gop.size());

			this.setPopulation(gop);
		}

		if (TRACE)
		  System.out.println("function calls"
		      + this.m_GlobalOptimizer.getPopulation().getFunctionCalls());
		this.firePropertyChangedEvent(Population.nextGenerationPerformed);
	}

	/**
	 * This method allows you to add the LectureGUI as listener to the Optimizer
	 *
	 * @param ea
	 */
	public void addPopulationChangedEventListener(
	    InterfacePopulationChangedEventListener ea) {
		this.m_Listener = ea;
	}
	public boolean removePopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		if (m_Listener==ea) {
			m_Listener=null;
			return true;
		} else return false;
	}
	/**
	 * Something has changed
	 */
	protected void firePropertyChangedEvent(String name) {
		if (this.m_Listener != null) {
			if (TRACE) System.out.println("firePropertyChangedEvent MA");
			this.m_Listener.registerPopulationStateChanged(this, name);
		}
	}

	/**
	 * This method will set the problem that is to be optimized
	 *
	 * @param problem
	 */
	public void SetProblem(InterfaceOptimizationProblem problem) {
		this.m_Problem = problem;
		this.m_GlobalOptimizer.SetProblem(this.m_Problem);
	}

	public InterfaceOptimizationProblem getProblem() {
		return this.m_Problem;
	}

	/**
	 * This method will return a string describing all properties of the optimizer
	 * and the applied methods.
	 *
	 * @return A descriptive string
	 */
	public String getStringRepresentation() {
		String result = "";
		result += "Memetic Algorithm:\n";
		result += "Optimization Problem: ";
		result += this.m_Problem.getStringRepresentationForProblem(this) + "\n";
		result += this.m_GlobalOptimizer.getStringRepresentation();
		return result;
	}

	/**
	 * This method allows you to set an identifier for the algorithm
	 *
	 * @param name
	 *          The indenifier
	 */
	public void SetIdentifier(String name) {
		this.m_Identifier = name;
	}

	public String getIdentifier() {
		return this.m_Identifier;
	}

	/**
	 * This method is required to free the memory on a RMIServer, but there is
	 * nothing to implement.
	 */
	public void freeWilly() {

	}

	/*
	 * ========================================================================================
	 * These are for GUI
	 */
	/**
	 * This method returns a global info string
	 *
	 * @return description
	 */
	public String globalInfo() {
		return "This is a basic generational Memetic Algorithm. Local search steps are performed on a selected subset " +
				"of individuals after certain numbers of global search iterations. Note " +
				"that the problem class must implement InterfaceLocalSearchable.";
	}

	/**
	 * This method will return a naming String
	 *
	 * @return The name of the algorithm
	 */
	public String getName() {
		return "MemeticAlgorithm";
	}

	/**
	 * Assuming that all optimizer will store thier data in a population we will
	 * allow acess to this population to query to current state of the optimizer.
	 *
	 * @return The population of current solutions to a given problem.
	 */
	public Population getPopulation() {
		return this.m_GlobalOptimizer.getPopulation();
	}

	public void setPopulation(Population pop) {
		this.m_GlobalOptimizer.setPopulation(pop);
	}

	public String populationTipText() {
		return "Edit the properties of the population used.";
	}

	/**
	 * Choose the global optimization strategy to use
	 *
	 * @param m_GlobalOptimizer
	 */
	public void setGlobalOptimizer(InterfaceOptimizer m_GlobalOptimizer) {
		this.m_GlobalOptimizer = m_GlobalOptimizer;
		this.m_GlobalOptimizer.SetProblem(this.getProblem());
		this.init();
	}

	public InterfaceOptimizer getGlobalOptimizer() {
		return m_GlobalOptimizer;
	}

	public String globalOptimizerTipText() {
		return "Choose the global optimization strategy to use.";
	}

	/**
	 * Choose the number of local search steps to perform per selected individual.
	 *
	 * @param localSearchSteps
	 */
	public void setLocalSearchSteps(int localSearchSteps) {
		this.localSearchSteps = localSearchSteps;
	}
	public int getLocalSearchSteps() {
		return localSearchSteps;
	}
	public String localSearchStepsTipText() {
		return "Choose the number of local search steps to perform per selected individual.";
	}

	/**
	 * Choose the interval between the application of the local search
	 *
	 * @param globalSearchSteps
	 */
	public void setGlobalSearchIterations(int globalSearchSteps) {
		this.globalSearchIterations = globalSearchSteps;
	}
	public int getGlobalSearchIterations() {
		return globalSearchIterations;
	}
	public String globalSearchIterationsTipText() {
		return "Choose the interval between the application of the local search.";
	}

    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }

	/**
	 * Choose the number of individual to be locally optimized
	 *
	 * @param subsetsize
	 */
	public void setSubsetsize(int subsetsize) {
		this.subsetsize = subsetsize;
	}
    public int getSubsetsize() {
		return subsetsize;
	}
	public String subsetsizeTipText() {
		return "Choose the number of individuals to be locally optimized.";
	}

	/**
	 * Toggle between Lamarckism and the Baldwin Effect
	 *
	 * @param lamarckism
	 */
	public void setLamarckism(boolean lamarckism) {
		this.lamarckism = lamarckism;
	}
	public String lamarckismTipText() {
		return "Toggle between Lamarckism and the Baldwin Effect.";
	}
	public boolean isLamarckism() {
		return lamarckism;
	}

	public InterfaceSelection getSubSetSelector() {
		return selectorPlug;
	}
	public void setSubSetSelector(InterfaceSelection selectorPlug) {
		this.selectorPlug = selectorPlug;
	}
	public String subSetSelectorTipText() {
		return "Selection method to select the subset on which local search is to be performed.";
	}
}
