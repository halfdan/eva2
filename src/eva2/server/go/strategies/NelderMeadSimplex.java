package eva2.server.go.strategies;

import java.io.Serializable;
import java.util.Vector;

import com.sun.org.apache.bcel.internal.generic.CHECKCAST;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.AbstractProblemDouble;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.stat.StatsParameter;
import eva2.tools.Mathematics;

/**
 * Nelder-Mead-Simplex does not guarantee an equal number of evaluations within each optimize call
 * because of the different step types.
 * Range check is now available by projection at the bounds.
 *  
 * @author mkron
 *
 */
public class NelderMeadSimplex implements InterfaceOptimizer, Serializable, InterfacePopulationChangedEventListener {

	private int populationSize = 100;
	// simulating the generational cycle. Set rather small (eg 5) for use as local search, higher for global search (eg 50)
	private int	generationCycle = 50;
	
	private Population m_Population;
	private AbstractOptimizationProblem m_Problem;
	private transient Vector<InterfacePopulationChangedEventListener> m_Listener;
	private String m_Identifier = "NelderMeadSimplex";
	private boolean checkConstraints = true;

	public NelderMeadSimplex() {
		setPopulation(new Population(populationSize));
	}

	public NelderMeadSimplex(NelderMeadSimplex a) {
		m_Problem = (AbstractOptimizationProblem)a.m_Problem.clone();
		setPopulation((Population)a.m_Population.clone());
		populationSize = a.populationSize;
		generationCycle = a.generationCycle;
		m_Identifier = a.m_Identifier;
	}

	public NelderMeadSimplex clone() {
		return new NelderMeadSimplex(this);
	}

	public void SetIdentifier(String name) {
		m_Identifier = name;
	}

	public void SetProblem(InterfaceOptimizationProblem problem) {
		m_Problem = (AbstractOptimizationProblem)problem;
	}
	
	public boolean setProblemAndPopSize(InterfaceOptimizationProblem problem) {
		SetProblem(problem);
		if (m_Problem instanceof AbstractProblemDouble) {
			setPopulationSize(((AbstractProblemDouble)problem).getProblemDimension()+1);
			return true;
		} else {
			Object ret=BeanInspector.callIfAvailable(problem, "getProblemDimension", null);
			if (ret!=null) {
				setPopulationSize(((Integer)ret)+1);
				return true;
			}
		}
		return false;
	}

	public void addPopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		if (m_Listener == null) m_Listener = new Vector<InterfacePopulationChangedEventListener>();
		if (!m_Listener.contains(ea)) m_Listener.add(ea);
	}

	public void freeWilly() {}

	protected double[] calcChallengeVect(double[] centroid, double[] refX) {
		double[] r = new double[centroid.length];
		for (int i=0; i<r.length; i++) r[i] = 2*centroid[i] - refX[i];
//		double alpha = 1.3;
//		for (int i=0; i<r.length; i++) r[i] = centroid[i] + alpha*(centroid[i] - refX[i]);
		return r;
	}
	
	public AbstractEAIndividual simplexStep(Population subpop) {
		// parameter

		// hole die n-1 besten individuen
		Population bestpop = subpop.getBestNIndividuals(subpop.size()-1);
		// und das schlechteste
		AbstractEAIndividual worst = subpop.getWorstEAIndividual();
		AbstractEAIndividual best=subpop.getBestEAIndividual();
		
		double[][] range = ((InterfaceDataTypeDouble) worst).getDoubleRange();
		double[] x_worst = ((InterfaceDataTypeDouble) worst).getDoubleData();
		int dim = x_worst.length;

		// Centroid berechnen
		double[] centroid = new double[dim];
		for (int i=0; i<bestpop.size(); i++) {
			for (int j=0; j<dim; j++) {
				AbstractEAIndividual bestIndi= (AbstractEAIndividual) bestpop.getIndividual(i);
				centroid[j] +=((InterfaceDataTypeDouble)bestIndi).getDoubleData()[j]/bestpop.size(); // bug?
			}
		}

		// Reflection
		double[] r = calcChallengeVect(centroid, x_worst);
		
		if (checkConstraints) {
			if (!Mathematics.isInRange(r, range)) {
				Mathematics.reflectBounds(r, range);
			}
		}
		AbstractEAIndividual r_ind = (AbstractEAIndividual)((AbstractEAIndividual)bestpop.getIndividual(1)).clone(); 
		((InterfaceDataTypeDouble)r_ind).SetDoubleGenotype(r);

		m_Problem.evaluate(r_ind);
		this.m_Population.incrFunctionCalls();

		if ((best.getFitness(0)<r_ind.getFitness(0))&(r_ind.getFitness(0) < bestpop.getWorstEAIndividual().getFitness(0))) {               // Problem: Fitnesswert ist vektor

			return r_ind;
		} else if (best.getFitness(0)>r_ind.getFitness(0)){ //neues besser als bisher bestes => Expansion

			double[] e = new double[dim];
			for (int i=0; i<dim; i++)  e[i] = 3*centroid[i] - 2*x_worst[i];
			if (checkConstraints && !Mathematics.isInRange(e, range)) Mathematics.projectToRange(e, range);
			
			AbstractEAIndividual e_ind = (AbstractEAIndividual)((AbstractEAIndividual)bestpop.getIndividual(1)).clone(); 
			((InterfaceDataTypeDouble)e_ind).SetDoubleGenotype(e);
			m_Problem.evaluate(e_ind);
			this.m_Population.incrFunctionCalls();
			if(e_ind.getFitness(0)<r_ind.getFitness(0)){//expandiertes ist besser als reflektiertes
				return e_ind;
			} else {
				return r_ind;
			}

		} else if(r_ind.getFitness(0) >= bestpop.getWorstEAIndividual().getFitness(0)){//kontrahiere da neues indi keine verbesserung brachte
			double[] c = new double[dim];
			for (int i=0; i<dim; i++)  c[i] = 0.5*centroid[i] + 0.5*x_worst[i];
			if (checkConstraints && !Mathematics.isInRange(c, range)) Mathematics.projectToRange(c, range);
			
			AbstractEAIndividual c_ind = (AbstractEAIndividual)((AbstractEAIndividual)bestpop.getIndividual(1)).clone(); 
			((InterfaceDataTypeDouble)c_ind).SetDoubleGenotype(c);
			m_Problem.evaluate(c_ind);
			this.m_Population.incrFunctionCalls();
			if(c_ind.getFitness(0)<=worst.getFitness(0)){
				return c_ind;
			}
		}
		return null;
	}

	public String getIdentifier() {
		return m_Identifier;
	}

	public String getName() {
		return m_Identifier;
	}

	public String globalInfo() {
		return "The Nelder-Mead simplex search algorithm for local search. Reflection is used as constra";
	}

	public Population getPopulation() {
		return m_Population;
	}

	public InterfaceOptimizationProblem getProblem() {
		return m_Problem;
	}

	public String getStringRepresentation() {
    	StringBuilder strB = new StringBuilder(200);
    	strB.append("Nelder-Mead-Simplex Strategy:\nOptimization Problem: ");
        strB.append(this.m_Problem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.m_Population.getStringRepresentation());
        return strB.toString();
	}

	public void init() {
		initByPopulation(m_Population, true);
	}

	public void initByPopulation(Population pop, boolean reset) {
		setPopulation(pop);
		pop.addPopulationChangedEventListener(this);
		if (reset) {
			m_Problem.initPopulation(m_Population);
			m_Problem.evaluate(m_Population);
		}
//		fireNextGenerationPerformed();
	}

	private void fireNextGenerationPerformed() {
		if (m_Listener != null) {
			for (int i=0; i<m_Listener.size(); i++)
				m_Listener.elementAt(i).registerPopulationStateChanged(this, Population.nextGenerationPerformed);
		}
	}

	public void optimize() {
		// make at least as many calls as there are individuals within the population.
		// this simulates the generational loop expected by some other modules
		int evalCntStart = m_Population.getFunctionCalls();
		int evalsDone = 0;
		m_Problem.evaluatePopulationStart(m_Population);
		do {
			AbstractEAIndividual ind = simplexStep(m_Population);
			if(ind!=null){ //Verbesserung gefunden
				double[] x=((InterfaceDataTypeDouble)ind).getDoubleData();
				double[][] range = ((InterfaceDataTypeDouble)ind).getDoubleRange();
				if (!Mathematics.isInRange(x, range)) {
					System.err.println("WARNING: nelder mead step produced indy out of range!");
//					Mathematics.projectToRange(x, range);
//					((InterfaceDataTypeDouble)ind).SetDoubleGenotype(x);
//					m_Problem.evaluate(ind);
//					this.m_Population.incrFunctionCalls();
				}
				m_Population.set(m_Population.getIndexOfWorstIndividual(), ind);
			}else{//keine Verbesserung gefunden shrink!!
				
				double[] u_1 = ((InterfaceDataTypeDouble) m_Population.getBestEAIndividual()).getDoubleData();
				
				for(int j=0;j<m_Population.size();j++){
					double [] c= ((InterfaceDataTypeDouble) m_Population.getEAIndividual(j)).getDoubleData();
					for (int i=0; i<c.length; i++)  c[i] = 0.5*c[i] + 0.5*u_1[i];
					((InterfaceDataTypeDouble) m_Population.getEAIndividual(j)).SetDoubleGenotype(c);
				}
				m_Problem.evaluate(m_Population);
			}
			evalsDone =  m_Population.getFunctionCalls() - evalCntStart;
		} while (evalsDone < generationCycle);
		m_Problem.evaluatePopulationEnd(m_Population);
	}

	public void setPopulation(Population pop) {
		m_Population = pop;
		m_Population.addPopulationChangedEventListener(this);
		m_Population.setNotifyEvalInterval(populationSize);
	}

	public InterfaceSolutionSet getAllSolutions() {
		Population pop = getPopulation();
		return new SolutionSet(pop, pop);
	}

	/**
	 * @return the populationSize
	 */
	public int getPopulationSize() {
		return populationSize;
	}

	/**
	 * @param populationSize the populationSize to set
	 */
	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
		if (m_Population!=null) {
			m_Population.setPopulationSize(populationSize);
			m_Population.setNotifyEvalInterval(m_Population.getPopulationSize());
		}
	}
	
	public String populationSizeTipText() {
		return "The population size should be adapted to the dimensions of the problem (e.g. n+1)";
	}

	public void registerPopulationStateChanged(Object source, String name) {
		if (name.compareTo(Population.funCallIntervalReached) == 0) {
			fireNextGenerationPerformed();
		}// else System.err.println("unknown event!");
	}
	
	/**
	 * This method creates a Nelder-Mead instance.
	 *
	 * @param pop
	 *          The size of the population
	 * @param problem
	 *          The problem to be optimized
	 * @param listener
	 * @return An optimization procedure that performs nelder mead optimization.
	 */
	public static final NelderMeadSimplex createNelderMeadSimplex(AbstractOptimizationProblem problem,
	    InterfacePopulationChangedEventListener listener) {

		problem.initProblem();
		NelderMeadSimplex nms = new NelderMeadSimplex();
		nms.setProblemAndPopSize(problem);
		
		nms.addPopulationChangedEventListener(listener);
		nms.init();

		if (listener!=null) listener.registerPopulationStateChanged(nms.getPopulation(), "");

		return nms;
	}

	/**
	 * This method creates a Nelder-Mead instance with an initial population 
	 * around a given candidate solution. The population is created as a simplex with given 
	 * perturbation ratio or randomly across the search range if the perturbation ratio is 
	 * zero or below zero.
	 * 
	 *
	 * @param problem
	 *          The problem to be optimized
	 * @param candidate starting point of the search
	 * @param perturbationRatio perturbation ratio relative to the problem range for the initial simplex creation
	 * @param listener
	 * @return An optimization procedure that performs nelder mead optimization.
	 */
	public static final NelderMeadSimplex createNelderMeadSimplexLocal(AbstractOptimizationProblem problem,
			AbstractEAIndividual candidate, double perturbationRatio,
	    InterfacePopulationChangedEventListener listener) {

		// TODO this method might be superfluous when using PostProcess
		problem.initProblem();
		NelderMeadSimplex nms = new NelderMeadSimplex();
		nms.setProblemAndPopSize(problem);
		
		Population initialPop;
		if (perturbationRatio <= 0) { // random case
			initialPop = new Population(nms.getPopulationSize());
			problem.initPopulation(initialPop);
			initialPop.set(0, candidate);
		} else { 
			double[][] range = ((InterfaceDataTypeDouble)candidate).getDoubleRange();
			if (range.length != nms.getPopulationSize()-1) System.err.println("Unexpected population size for nelder mead!");
			initialPop = createNMSPopulation(candidate, perturbationRatio, range, true);
		}
		if (listener != null) nms.addPopulationChangedEventListener(listener);
		nms.initByPopulation(initialPop, false);
		//nms.setPopulation(initialPop);

		return nms;
	}
	
	/**
	 * From a given candidate solution, create n solutions around the candidate, where every i-th
	 * new candidate differs in i dimensions by a distance of perturbRatio relative to the range in
	 * that dimension (respecting the range). 
	 * The new solutions are returned as a population, which, if includeCand is true,
	 * also contains the initial candidate. However, the new candidates have not been evaluated.
	 * 
	 * @param candidate
	 * @param perturbRatio
	 * @param range
	 * @param includeCand
	 * @return
	 */
	public static Population createNMSPopulation(AbstractEAIndividual candidate, double perturbRatio, double[][] range, boolean includeCand) {
		Population initPop = new Population();
		if (includeCand) initPop.add(candidate);
		if (perturbRatio >= 1. || (perturbRatio <= 0.)) {
			System.err.println("Warning: perturbation ratio should lie between 0 and 1! (NelderMeadSimplex:createNMSPopulation)");
		}
		addPerturbedPopulation(perturbRatio, initPop, range, candidate);
		return initPop;
	}

	private static void addPerturbedPopulation(double perturbationRatio,
			Population initialPop, double[][] range, AbstractEAIndividual candidate) {
		AbstractEAIndividual indy = (AbstractEAIndividual)candidate.clone();
		// span by perturbation, every new individual i is modified in dimension i by
		// a value of perturbRatio*range_i such that a simplex of relative side length perturbRatio is created. 
		for (int i=0; i<range.length; i+=1) {
			double curPerturb = ((range[i][1]-range[i][0])*perturbationRatio);
			double[] dat = ((InterfaceDataTypeDouble)indy).getDoubleData();
			if (dat[i]==range[i][1]) { // in this case the bound is said to be too close 
				dat[i]=Math.max(dat[i]-curPerturb, range[i][0]);
			} else dat[i] = Math.min(dat[i]+curPerturb, range[i][1]);
			((InterfaceDataTypeDouble)indy).SetDoubleGenotype(dat);
			initialPop.add((AbstractEAIndividual)indy.clone());
		}
		initialPop.setPopulationSize(initialPop.size());
	}

	/**
	 * @param generationCycle the generationCycle to set
	 */
	public void setGenerationCycle(int generationCycle) {
		this.generationCycle = generationCycle;
	}

	public boolean isCheckRange() {
		return checkConstraints;
	}

	public void setCheckRange(boolean checkRange) {
		this.checkConstraints = checkRange;
	}
	
	public String checkRangeTipText() {
		return "Mark to check range constraints by reflection/projection";
	}

}
