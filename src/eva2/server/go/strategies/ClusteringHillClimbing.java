package eva2.server.go.strategies;

import java.io.Serializable;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.PopulationInterface;
import eva2.server.go.enums.PostProcessMethod;
import eva2.server.go.operators.mutation.MutateESFixedStepSize;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;


/**
 * The clustering hill climber is similar to a multi-start hill climber. In addition so optimizing
 * a set of individuals in parallel using a (1+1) strategy, the population is clustered in regular
 * intervals. If several individuals have gathered together in the sense that they are interpreted
 * as a cluster, only a subset of representatives of the cluster is taken over to the next HC step
 * while the rest is discarded. This means that the population size may be reduced.
 * 
 * As soon as the improvement by HC lies below a threshold, the mutation step size is decreased.
 * If the step size is decreased below a certain threshold, the current population is stored to 
 * an archive and reinitialized. Thus, the number of optima that may be found and returned by
 * getAllSolutions is higher than the population size.
 * 
 * @author mkron
 *
 */
public class ClusteringHillClimbing implements InterfacePopulationChangedEventListener, 
InterfaceOptimizer, Serializable, InterfaceAdditionalPopulationInformer {
    transient private InterfacePopulationChangedEventListener   m_Listener;
    public static final boolean TRACE = false;
    
    transient private String 				m_Identifier = "";
    private Population						m_Population = new Population();
    private transient Population			archive = new Population();
    private InterfaceOptimizationProblem	m_Problem	= new F1Problem();
    private int								hcEvalCycle	= 1000;
    private int								initialPopSize = 100;
   	private int								loopCnt = 0;
//   	private int								baseEvalCnt = 0;
   	private int								notifyGuiEvery		= 50;
   	private double							sigmaClust 			= 0.01;
   	private double							minImprovement 		= 0.000001;
   	private double 							stepSizeThreshold 	= 0.000001;
   	private double							initialStepSize 	= 0.1;
   	// reduce the step size when there is hardy improvement. 
   	private double							reduceFactor		= 0.2;
   	private MutateESFixedStepSize				mutator = new MutateESFixedStepSize(0.1);
	private PostProcessMethod localSearchMethod = PostProcessMethod.nelderMead;
	private boolean doReinitialization = true;

	public ClusteringHillClimbing() {
		hideHideable();
	}
	
	public ClusteringHillClimbing(int initialPopSize, PostProcessMethod lsMethod) {
		this();
		setInitialPopSize(initialPopSize);
		setLocalSearchMethod(lsMethod);
	}
	
	public ClusteringHillClimbing(ClusteringHillClimbing other) {
		hideHideable();
		m_Population = (Population)other.m_Population.clone();
		m_Problem = (InterfaceOptimizationProblem)other.m_Problem.clone();
		
		hcEvalCycle = other.hcEvalCycle;
		initialPopSize = other.initialPopSize;
		notifyGuiEvery = other.notifyGuiEvery;
		sigmaClust = other.sigmaClust;
		minImprovement = other.minImprovement;
		stepSizeThreshold = other.stepSizeThreshold;
		initialStepSize = other.initialStepSize;
		reduceFactor = other.reduceFactor;
		mutator = (MutateESFixedStepSize)other.mutator.clone();
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
		setDoReinitialization(isDoReinitialization());
		setLocalSearchMethod(getLocalSearchMethod());
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
	public boolean removePopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		if (m_Listener==ea) {
			m_Listener=null;
			return true;
		} else return false;
	}
    public void init() {
       	loopCnt = 0;
       	mutator = new MutateESFixedStepSize(initialStepSize);
       	archive = new Population();
       	hideHideable();
       	m_Population.setTargetSize(initialPopSize);
        this.m_Problem.initPopulation(this.m_Population);
        m_Population.addPopulationChangedEventListener(null); // noone will be notified directly on pop changes
        this.m_Problem.evaluate(this.m_Population);
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
       	loopCnt = 0;
        this.m_Population = (Population)pop.clone();
        m_Population.addPopulationChangedEventListener(null);
        if (reset) {
        	this.m_Population.init();
            this.m_Problem.evaluate(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
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
		Pair<Population, Double> popD;
		int funCallsBefore=m_Population.getFunctionCalls();
		int evalsNow, lastOverhead = (m_Population.getFunctionCalls() % hcEvalCycle); 
		if (lastOverhead>0) evalsNow = (2*hcEvalCycle - (m_Population.getFunctionCalls() % hcEvalCycle));
		else evalsNow = hcEvalCycle;
		do {
			if (TRACE) System.out.println("evalCycle: " + hcEvalCycle + ", evals now: " + evalsNow);
			popD = PostProcess.clusterLocalSearch(localSearchMethod, m_Population, (AbstractOptimizationProblem)m_Problem, sigmaClust, evalsNow, 0.5, mutator);
			//		(m_Population, (AbstractOptimizationProblem)m_Problem, sigmaClust, hcEvalCycle - (m_Population.getFunctionCalls() % hcEvalCycle), 0.5);
			if (popD.head().getFunctionCalls()==funCallsBefore) {
				System.err.println("Bad case, increasing allowed evaluations!");
				evalsNow=Math.max(evalsNow++, (int)(evalsNow*1.2));
			}
		} while (popD.head().getFunctionCalls()==funCallsBefore);
		improvement = popD.tail();
		m_Population = popD.head();
		if (TRACE) System.out.println("num inds after clusterLS: " + m_Population.size());

		popD.head().setGenerationTo(m_Population.getGeneration()+1);
		
		if (doReinitialization  && (improvement < minImprovement)) {
			if (TRACE) System.out.println("improvement below " + minImprovement);
			if ((localSearchMethod != PostProcessMethod.hillClimber) || (mutator.getSigma() < stepSizeThreshold)) { // reinit!
				// is performed for nm and cma, and if hc has too low sigma
				if (TRACE) System.out.println("REINIT!!");
		        
				if (localSearchMethod == PostProcessMethod.hillClimber) mutator.setSigma(initialStepSize);
				
				// store results
				archive.SetFunctionCalls(m_Population.getFunctionCalls());
				archive.addPopulation(m_Population);
				
				Population tmpPop = new Population();
				tmpPop.addPopulationChangedEventListener(null);
				tmpPop.setTargetSize(initialPopSize);
				this.m_Problem.initPopulation(tmpPop);
				tmpPop.setSameParams(m_Population);
				tmpPop.setTargetSize(initialPopSize);
				this.m_Problem.evaluate(tmpPop);
				
				// reset population while keeping function calls etc.
				m_Population.clear();
				m_Population.addPopulation(tmpPop);
				m_Population.incrFunctionCallsBy(tmpPop.size());
		
			} else  {  // decrease step size for hc
				if (localSearchMethod != PostProcessMethod.hillClimber) System.err.println("Invalid case in ClusteringHillClimbing!");
				mutator.setSigma(mutator.getSigma()*reduceFactor);
				if (TRACE) System.out.println("mutation stepsize reduced to " + mutator.getSigma());
			}
		}
//		System.out.println("funcalls: " + evalCnt);
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);

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
			this.firePropertyChangedEvent(Population.nextGenerationPerformed);
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

    public InterfaceSolutionSet getAllSolutions() {
    	Population tmp = new Population();
    	tmp.addPopulation(archive);
    	tmp.addPopulation(m_Population);
    	tmp.SetFunctionCalls(m_Population.getFunctionCalls());
    	tmp.setGenerationTo(m_Population.getGeneration());
//    	tmp = PostProcessInterim.clusterBest(tmp, sigma, 0, PostProcessInterim.KEEP_LONERS, PostProcessInterim.BEST_ONLY);
    	return new SolutionSet(m_Population, tmp);
    }
    
    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        StringBuffer sbuf = new StringBuffer("Clustering Hill Climbing");
        sbuf.append(", initial pop size: ");
        sbuf.append(getPopulation().getTargetSize());
        sbuf.append("Optimization Problem: ");
        sbuf.append(this.m_Problem.getStringRepresentationForProblem(this));
        sbuf.append(this.m_Population.getStringRepresentation());
        return sbuf.toString();
    }
	
	public void freeWilly() {}

	public String getName() {
		return "ClustHC-"+initialPopSize+"-"+localSearchMethod;
	}

	public static String globalInfo() {
		return "Similar to multi-start HC, but clusters the population during optimization to remove redundant individuals for efficiency." +
				"If the local search step does not achieve a minimum improvement, the population may be reinitialized.";
	}
	
	/**
	 * @return the hcEvalCycle
	 */
	public int getEvalCycle() {
		return hcEvalCycle;
	}

	/**
	 * @param hcEvalCycle the hcEvalCycle to set
	 */
	public void setEvalCycle(int hcEvalCycle) {
		this.hcEvalCycle = hcEvalCycle;
	}
	
	public String evalCycleTipText() {
		return "The number of evaluations between two clustering/adaption steps.";
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

	public String initialPopSizeTipText() {
		return "Population size at the start and at reinitialization times.";
	}
	
	/**
	 * @return the sigma
	 */
	public double getSigmaClust() {
		return sigmaClust;
	}

	/**
	 * @param sigma the sigma to set
	 */
	public void setSigmaClust(double sigma) {
		this.sigmaClust = sigma;
	}
	
	public String sigmaClustTipText() {
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

	public String notifyGuiEveryTipText() {
		return "How often to notify the GUI to plot the fitness etc.";
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

	public String minImprovementTipText() {
		return "Improvement threshold below which the mutation step size is reduced or the population reinitialized.";
	}
	
	/**
	 * @return the reinitForStepSize
	 */
	public double getStepSizeThreshold() {
		return stepSizeThreshold;
	}

	/**
	 * @param reinitForStepSize the reinitForStepSize to set
	 */
	public void setStepSizeThreshold(double reinitForStepSize) {
		this.stepSizeThreshold = reinitForStepSize;
	}

	public String stepSizeThresholdTipText() {
		return "Threshold for the mutation step size below which the population is seen as converged and reinitialized.";
	}
	
	/**
	 * @return the initialStepSize
	 */
	public double getStepSizeInitial() {
		return initialStepSize;
	}

	/**
	 * @param initialStepSize the initialStepSize to set
	 */
	public void setStepSizeInitial(double initialStepSize) {
		this.initialStepSize = initialStepSize;
	}
	
	public String stepSizeInitialTipText() {
		return "Initial mutation step size for hill climbing, relative to the problem range.";
	}

	public PostProcessMethod getLocalSearchMethod() {
		return localSearchMethod;
	}

	public void setLocalSearchMethod(PostProcessMethod localSearchMethod) {
		this.localSearchMethod = localSearchMethod;
		GenericObjectEditor.setShowProperty(this.getClass(), "stepSizeInitial", localSearchMethod==PostProcessMethod.hillClimber);
		GenericObjectEditor.setShowProperty(this.getClass(), "stepSizeThreshold", localSearchMethod==PostProcessMethod.hillClimber);
	}
	
	public String localSearchMethodTipText() {
		return "Set the method to be used for the hill climbing as local search";
	}

	public String[] getAdditionalDataHeader() {
		return new String[]{"numIndies", "sigma", "numArchived", "archivedMeanDist"};
	}
	
	public String[] getAdditionalDataInfo() {
		return new String[]{"The current population size", "Current step size in case of stochastic HC", "Number of archived solutions", "Mean distance of archived solutions"};
	}
	
	public Object[] getAdditionalDataValue(PopulationInterface pop) {
		return new Object[]{m_Population.size(), mutator.getSigma(), archive.size(), archive.getPopulationMeasures()[0]};
	}

	public boolean isDoReinitialization() {
		return doReinitialization;
	}

	public void setDoReinitialization(boolean doReinitialization) {
		this.doReinitialization = doReinitialization;
		GenericObjectEditor.setShowProperty(this.getClass(), "minImprovement", doReinitialization); 
	}

	public String doReinitializationTipText() {
		return "Activate reinitialization if no improvement was achieved.";
	}
}
