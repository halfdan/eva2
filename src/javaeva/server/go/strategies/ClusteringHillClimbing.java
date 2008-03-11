package javaeva.server.go.strategies;

import java.io.Serializable;

import javaeva.gui.GenericObjectEditor;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.operators.mutation.MutateESFixedStepSize;
import javaeva.server.go.operators.postprocess.PostProcess;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.AbstractOptimizationProblem;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.tools.Pair;

public class ClusteringHillClimbing implements InterfacePopulationChangedEventListener, InterfaceOptimizer, Serializable {
    transient private InterfacePopulationChangedEventListener   m_Listener;
    transient private String 				m_Identifier = "";
    private Population						m_Population = new Population();
    private transient Population			archive = new Population();
    private InterfaceOptimizationProblem	m_Problem	= new F1Problem();
    private int								hcEvalCycle	= 1000;
    private int								initialPopSize = 200;
   	private int								loopCnt = 0;
//   	private int								baseEvalCnt = 0;
   	private int								notifyGuiEvery		= 50;
   	private double							sigma 				= 0.01;
   	private double							minImprovement 		= 0.000001;
   	private double 							reinitForStepSize 	= 0.000001;
   	private double							initialStepSize 	= 0.1;
   	private MutateESFixedStepSize				mutator = new MutateESFixedStepSize(0.1);

	public ClusteringHillClimbing() {
		hideHideable();
	}
	
	public ClusteringHillClimbing(ClusteringHillClimbing other) {
		hideHideable();
		m_Population = (Population)other.m_Population.clone();
		m_Problem = (InterfaceOptimizationProblem)other.m_Problem.clone();
		hcEvalCycle = other.hcEvalCycle;
		initialPopSize = other.initialPopSize;
		loopCnt = 0;		
	}
	
    public Object clone() {
        return (Object) new ClusteringHillClimbing(this);
    }
    
	/**
	 * Hide the population.
	 */
	public void hideHideable() {
		GenericObjectEditor.setHideProperty(getClass(), "population", true);
	}
	
     public void SetIdentifier(String name) {
        this.m_Identifier = name;
    }
     public String getIdentifier() {
         return this.m_Identifier;
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

    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }

    public void init() {
       	loopCnt = 0;
       	mutator = new MutateESFixedStepSize(initialStepSize);
       	archive = new Population();
       	hideHideable();
       	m_Population.setPopulationSize(initialPopSize);
        this.m_Problem.initPopulation(this.m_Population);
        m_Population.addPopulationChangedEventListener(null);
        this.m_Problem.evaluate(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
       	loopCnt = 0;
        this.m_Population = (Population)pop.clone();
        m_Population.addPopulationChangedEventListener(null);
        if (reset) this.m_Population.init();
        this.m_Problem.evaluate(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** Something has changed
     */
    protected void firePropertyChangedEvent (String name) {
        if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
    }
    
	public void optimize() {
		double improvement;
		
		loopCnt++;
		m_Population.addPopulationChangedEventListener(this);
		m_Population.setNotifyEvalInterval(notifyGuiEvery);
		Pair<Population, Double> popD = PostProcess.clusterHC(m_Population, (AbstractOptimizationProblem)m_Problem, sigma, hcEvalCycle - (m_Population.getFunctionCalls() % hcEvalCycle), 0.5, mutator);
		improvement = popD.tail();

		popD.head().setGenerationTo(m_Population.getGeneration()+1);
		m_Population = popD.head();
		
		if (improvement < minImprovement) {
			System.out.println("improvement below " + minImprovement);
			if (mutator.getSigma() < reinitForStepSize) { // reinit!
				System.out.println("REINIT!!");
		        // store results
				mutator.setSigma(initialStepSize);
				archive.SetFunctionCalls(m_Population.getFunctionCalls());
				archive.addPopulation(m_Population);
				
				Population tmpPop = new Population();
				tmpPop.setSameParams(m_Population);
				tmpPop.addPopulationChangedEventListener(null);
				tmpPop.setPopulationSize(initialPopSize);
				this.m_Problem.initPopulation(tmpPop);
				this.m_Problem.evaluate(tmpPop);
				
				// reset population while keeping function calls etc.
				m_Population.clear();
				m_Population.addPopulation(tmpPop);
				m_Population.incrFunctionCallsby(tmpPop.size());
		
			} else  {  // decrease step size
				mutator.setSigma(mutator.getSigma()/2);
				System.out.println("halfed sigma to " + mutator.getSigma());
			}
		}
//		System.out.println("funcalls: " + evalCnt);
//        this.firePropertyChangedEvent("NextGenerationPerformed");

	}

	public void registerPopulationStateChanged(Object source, String name) {
		// The events of the interim hill climbing population will be caught here 
		if (name.compareTo(Population.funCallIntervalReached) == 0) {
//			if ((((Population)source).size() % 50) > 0) {
//				System.out.println("bla");
//			}
			// set funcalls to real value
			m_Population.SetFunctionCalls(((Population)source).getFunctionCalls());
//			System.out.println("FunCallIntervalReached at " + (((Population)source).getFunctionCalls()));
			this.firePropertyChangedEvent("NextGenerationPerformed");
		} 
		// do not react to NextGenerationPerformed
		//else System.err.println("ERROR, event was " + name);
		
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
        return "Change the number of starting individuals stored (Cluster-HC).";
    }

    public Population getAllSolutions() {
    	Population tmp = new Population();
    	tmp.addPopulation(archive);
    	tmp.addPopulation(m_Population);
//    	tmp = PostProcessInterim.clusterBest(tmp, sigma, 0, PostProcessInterim.KEEP_LONERS, PostProcessInterim.BEST_ONLY);
    	return tmp;
    }
    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        StringBuffer sbuf = new StringBuffer("Clustering Hill Climbing");
        sbuf.append(", initial pop size: ");
        sbuf.append(getPopulation().getPopulationSize());
        sbuf.append("Optimization Problem: ");
        sbuf.append(this.m_Problem.getStringRepresentationForProblem(this));
        sbuf.append(this.m_Population.getStringRepresentation());
        return sbuf.toString();
    }
	
	public void freeWilly() {}

	public String getName() {
		return "Cluster-HC";
	}

	/**
	 * @return the hcEvalCycle
	 */
	public int getHcEvalCycle() {
		return hcEvalCycle;
	}

	/**
	 * @param hcEvalCycle the hcEvalCycle to set
	 */
	public void setHcEvalCycle(int hcEvalCycle) {
		this.hcEvalCycle = hcEvalCycle;
	}

	/**
	 * @return the initialPopSize
	 */
	public int getInitialPopSize() {
		return initialPopSize;
	}

	/**
	 * @param initialPopSize the initialPopSize to set
	 */
	public void setInitialPopSize(int initialPopSize) {
		this.initialPopSize = initialPopSize;
	}

	/**
	 * @return the sigma
	 */
	public double getSigma() {
		return sigma;
	}

	/**
	 * @param sigma the sigma to set
	 */
	public void setSigma(double sigma) {
		this.sigma = sigma;
	}
	
	public String sigmaTipText() {
		return "Defines the sigma distance parameter for density based clustering.";
	}

	/**
	 * @return the notifyGuiEvery
	 */
	public int getNotifyGuiEvery() {
		return notifyGuiEvery;
	}

	/**
	 * @param notifyGuiEvery the notifyGuiEvery to set
	 */
	public void setNotifyGuiEvery(int notifyGuiEvery) {
		this.notifyGuiEvery = notifyGuiEvery;
	}

	/**
	 * @return the minImprovement
	 */
	public double getMinImprovement() {
		return minImprovement;
	}

	/**
	 * @param minImprovement the minImprovement to set
	 */
	public void setMinImprovement(double minImprovement) {
		this.minImprovement = minImprovement;
	}

	/**
	 * @return the reinitForStepSize
	 */
	public double getReinitForStepSize() {
		return reinitForStepSize;
	}

	/**
	 * @param reinitForStepSize the reinitForStepSize to set
	 */
	public void setReinitForStepSize(double reinitForStepSize) {
		this.reinitForStepSize = reinitForStepSize;
	}

	/**
	 * @return the initialStepSize
	 */
	public double getInitialStepSize() {
		return initialStepSize;
	}

	/**
	 * @param initialStepSize the initialStepSize to set
	 */
	public void setInitialStepSize(double initialStepSize) {
		this.initialStepSize = initialStepSize;
	}

//	/**
//	 * @return the mutator
//	 */
//	public InterfaceMutation getMutator() {
//		return mutator;
//	}
//
//	/**
//	 * @param mutator the mutator to set
//	 */
//	public void setMutator(InterfaceMutation mutator) {
//		this.mutator = mutator;
//	}
}
