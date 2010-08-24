package eva2.server.go.strategies;

import java.io.Serializable;
import java.util.HashMap;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.archiving.ArchivingNSGAII;
import eva2.server.go.operators.archiving.ArchivingNSGAIISMeasure;
import eva2.server.go.operators.mutation.MutateESCovarianceMatrixAdaptionPlus;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/**
 * 
 * @author mkron
 *
 */
public class MultiObjectiveCMAES implements InterfaceOptimizer, Serializable {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @author mkron
	 *
	 */
	class CounterClass {
		public CounterClass(int i) {
			value = i;
		}

		public int value;
		public boolean seen = false;
	}

	private String m_Identifier = "MOCMAES";

	private Population m_Population;
	private AbstractOptimizationProblem m_Problem;

	transient private InterfacePopulationChangedEventListener m_Listener;

	private int m_lambda = 1;
	private int m_lambdamo = 1;

	public MultiObjectiveCMAES() {
		m_Population = new Population(m_lambdamo);
	}

	public MultiObjectiveCMAES(MultiObjectiveCMAES a) {
		m_Problem = (AbstractOptimizationProblem) a.m_Problem.clone();
		setPopulation((Population) a.m_Population.clone());
		m_lambda = a.m_lambda;
	}

	public MultiObjectiveCMAES clone() {
		return new MultiObjectiveCMAES(this);
	}

	public void hideHideable() {
		GenericObjectEditor
				.setHideProperty(this.getClass(), "population", true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.server.go.strategies.InterfaceOptimizer#SetIdentifier(java.lang.
	 * String)
	 */
	public void SetIdentifier(String name) {
		m_Identifier = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.server.go.strategies.InterfaceOptimizer#SetProblem(eva2.server.go
	 * .problems.InterfaceOptimizationProblem)
	 */
	public void SetProblem(InterfaceOptimizationProblem problem) {
		m_Problem = (AbstractOptimizationProblem) problem;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#freeWilly()
	 */
	public void freeWilly() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#getAllSolutions()
	 */
	public InterfaceSolutionSet getAllSolutions() {
		Population pop = getPopulation();
		return new SolutionSet(pop, pop);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#getIdentifier()
	 */
	public String getIdentifier() {
		return m_Identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#getName()
	 */
	public String getName() {
		return "(1+" + m_lambda + ") MO-CMA-ES";
	}

	public static String globalInfo() {
		return "A multi-objective CMA-ES variant after Igel, Hansen and Roth 2007 (EC 15(1),1-28).";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#getPopulation()
	 */
	public Population getPopulation() {
		return m_Population;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#getProblem()
	 */
	public InterfaceOptimizationProblem getProblem() {
		return m_Problem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.server.go.strategies.InterfaceOptimizer#getStringRepresentation()
	 */
	public String getStringRepresentation() {
		StringBuilder strB = new StringBuilder(200);
		strB.append("(1+" + m_lambda + ") MO-CMA-ES:\nOptimization Problem: ");
		strB.append(this.m_Problem.getStringRepresentationForProblem(this));
		strB.append("\n");
		strB.append(this.m_Population.getStringRepresentation());
		return strB.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#init()
	 */
	public void init() {
		// initByPopulation(m_Population, true);
		this.m_Population.setTargetSize(m_lambdamo);
		this.m_Problem.initPopulation(this.m_Population);
		// children = new Population(m_Population.size());
		this.evaluatePopulation(this.m_Population);
		this.firePropertyChangedEvent(Population.nextGenerationPerformed);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.server.go.strategies.InterfaceOptimizer#initByPopulation(eva2.server
	 * .go.populations.Population, boolean)
	 */
	public void initByPopulation(Population pop, boolean reset) {
		setPopulation(pop);
		if (reset) {
			m_Problem.initPopulation(m_Population);
			m_Problem.evaluate(m_Population);

		}
	}

	/**
	 * This method will evaluate the current population using the given problem.
	 * 
	 * @param population
	 *            The population that is to be evaluated
	 */
	private void evaluatePopulation(Population population) {
		this.m_Problem.evaluate(population);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.strategies.InterfaceOptimizer#optimize()
	 */
	public void optimize() {

		HashMap<Long, CounterClass> SuccessCounterMap = new HashMap<Long, CounterClass>();

		// Eltern markieren und f�r die Z�hlung vorbereiten
		for (int j = 0; j < m_lambdamo && j < m_Population.size(); j++) {
			m_Population.getEAIndividual(j).putData("Parent",
					m_Population.getEAIndividual(j));
			SuccessCounterMap.put(m_Population.getEAIndividual(j).getIndyID(),
					new CounterClass(0));
		}

		// Kinder erzeugen
		Population children = new Population(m_lambdamo * m_lambda);
		children.setGenerationTo(m_Population.getGeneration());

		for (int j = 0; j < children.getTargetSize(); j++) {
			AbstractEAIndividual parent = m_Population.getEAIndividual(j
					% m_lambdamo);
			AbstractEAIndividual indy = (AbstractEAIndividual) parent.clone();
			indy.mutate();
			indy.putData("Parent", parent);
			children.add(indy);
		}
		evaluatePopulation(children);

		m_Population.addPopulation(children);
		// Ranking
		ArchivingNSGAII dummyArchive = new ArchivingNSGAIISMeasure();
		Population[] store = dummyArchive
				.getNonDomiatedSortedFronts(m_Population);
		store = dummyArchive.getNonDomiatedSortedFronts(m_Population);
		dummyArchive.calculateCrowdingDistance(store);

		// Vergleichen und den Successcounter hochz�hlen wenn wir besser als
		// unser Elter sind
		for (int j = 0; j < m_Population.size(); j++) {
			AbstractEAIndividual parent = (AbstractEAIndividual) m_Population
					.getEAIndividual(j).getData("Parent");
			if (m_Population.getEAIndividual(j) != parent) { // Eltern nicht mit
				// sich selber
				// vergleichen
				int parentParetoLevel = ((Integer) parent
						.getData("ParetoLevel")).intValue();
				double parentSMeasure = ((Double) parent.getData("HyperCube"))
						.doubleValue();
				int childParetoLevel = ((Integer) m_Population.getEAIndividual(
						j).getData("ParetoLevel")).intValue();
				double childSMeasure = ((Double) m_Population
						.getEAIndividual(j).getData("HyperCube")).doubleValue();
				if (childParetoLevel < parentParetoLevel
						|| ((childParetoLevel == parentParetoLevel) && childSMeasure > parentSMeasure)) {
					SuccessCounterMap.get(parent.getIndyID()).value++;
				}
			} else { // Debug

				SuccessCounterMap.get(parent.getIndyID()).seen = true;
			}
		}

		// Selection
		m_Population.clear();
		for (int i = 0; i < store.length; i++) {
			if (m_Population.size() + store[i].size() <= m_lambdamo) { // Die
				// Front
				// passt
				// noch
				// komplett
				m_Population.addPopulation(store[i]);

			} else { // die besten aus der aktuellen Front heraussuchen bis voll
				while (store[i].size() > 0 && m_Population.size() < m_lambdamo) {
					AbstractEAIndividual indy = store[i].getEAIndividual(0);
					double bestMeasure = ((Double) indy.getData("HyperCube"))
							.doubleValue(); // TODO mal noch effizient machen
					// (sortieren und die besten n
					// herausholen)
					for (int j = 1; j < store[i].size(); j++) {
						if (bestMeasure < ((Double) store[i].getEAIndividual(j)
								.getData("HyperCube")).doubleValue()) {
							bestMeasure = ((Double) store[i].getEAIndividual(j)
									.getData("HyperCube")).doubleValue();
							indy = store[i].getEAIndividual(j);
						}
					}
					m_Population.add(indy);
					store[i].removeMember(indy);
				}
			}

		}

		// Strategieparemeter updaten
		for (int j = 0; j < m_Population.size(); j++) {

			AbstractEAIndividual indy = m_Population.getEAIndividual(j);
			if (indy.getMutationOperator() instanceof MutateESCovarianceMatrixAdaptionPlus) { // Das
				// geht
				// nur
				// wenn
				// wir
				// auch
				// die
				// richtige
				// Mutation
				// haben
				AbstractEAIndividual parent = (AbstractEAIndividual) indy
						.getData("Parent");
				MutateESCovarianceMatrixAdaptionPlus muta = (MutateESCovarianceMatrixAdaptionPlus) indy
						.getMutationOperator();
				double rate = ((double) SuccessCounterMap.get(parent
						.getIndyID()).value)
						/ ((double) m_lambda);

				if (indy != parent) {
					muta.updateCovariance();
				}
				muta.updateStepSize(rate);
			}
		}

		for (int j = 0; j < children.size(); j++) {
			children.getEAIndividual(j).putData("Parent", null);
		}

		m_Population.incrFunctionCallsBy(children.size());
		m_Population.incrGeneration();
		this.firePropertyChangedEvent(Population.nextGenerationPerformed);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeeva2.server.go.strategies.InterfaceOptimizer#
	 * removePopulationChangedEventListener
	 * (eva2.server.go.InterfacePopulationChangedEventListener)
	 */
	public boolean removePopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.server.go.strategies.InterfaceOptimizer#setPopulation(eva2.server
	 * .go.populations.Population)
	 */
	public void setPopulation(Population pop) {
		m_Population = pop;
		m_Population.setNotifyEvalInterval(1);
		m_lambdamo = m_Population.getTargetSize();

	}

	/**
	 * Something has changed
	 * 
	 * @param name
	 */
	protected void firePropertyChangedEvent(String name) {
		if (this.m_Listener != null)
			this.m_Listener.registerPopulationStateChanged(this, name);
	}

	public int getLambda() {
		return m_lambda;
	}

	public void setLambda(int mLambda) {
		m_lambda = mLambda;
	}

	/*
	 * public int getLambdaMo() { return m_lambdamo; }
	 * 
	 * public void setLambdaMo(int mLambda) { m_lambdamo = mLambda; }
	 */

}
