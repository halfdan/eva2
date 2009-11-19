package eva2.server.go.problems;

import java.awt.BorderLayout;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.io.Serializable;


import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.server.go.enums.PostProcessMethod;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.moso.MOSONoConvert;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESFixedStepSize;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.operators.terminators.CombinedTerminator;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.operators.terminators.PhenotypeConvergenceTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.Mathematics;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 29.08.2003
 * Time: 13:40:12
 * To change this template use Options | File Templates.
 */
public abstract class AbstractOptimizationProblem implements InterfaceOptimizationProblem /*, InterfaceParamControllable*/, Serializable {
	class EvalThread extends Thread {
		AbstractOptimizationProblem prob;
		AbstractEAIndividual ind;
		Vector<AbstractEAIndividual> resultrep;
		Population pop;
		Semaphore m_Semaphore;
		
		public EvalThread(AbstractOptimizationProblem prob, AbstractEAIndividual ind, Vector<AbstractEAIndividual> resultrep, Population pop,Semaphore sema) {
			this.ind = ind;
			this.prob = prob;
			this.resultrep = resultrep;
			this.pop = pop;
			this.m_Semaphore=sema;
		}
		
		public void run() {
//			System.out.println("Running ET " + this);
//			long time=System.nanoTime();
			prob.evaluate(ind);
			resultrep.add(ind);
			pop.incrFunctionCalls();
			m_Semaphore.release();
//			long duration=System.nanoTime()-time;
//			System.out.println("Finished ET" + this +  ", time was " + duration);
		}
		
	}
	
	int parallelthreads = 1;
	
    protected 	AbstractEAIndividual      m_Template;
//    private transient ArrayList<ParamChangeListener> changeListeners = null;

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public abstract Object clone();

    public int getParallelThreads() {
		return parallelthreads;
	}

	public void setParallelThreads(int parallelthreads) {
		this.parallelthreads = parallelthreads;
	}
	
	public String parallelThreadsTipText() {
		return "Set the number of threaded parallel function evaluations - interesting for slow functions and generational optimizers.";
	}

	/** This method inits the Problem to log multiruns
     */
    public abstract void initProblem();

    /******************** The most important methods ****************************************/

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public abstract void initPopulation(Population population);

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population) {
        AbstractEAIndividual    tmpIndy;
        evaluatePopulationStart(population);
        
        if (this.parallelthreads > 1) {
        	Vector<AbstractEAIndividual> queue = new Vector<AbstractEAIndividual>(population.size());
        	Vector<AbstractEAIndividual> finished =  new Vector<AbstractEAIndividual>(population.size());
        	/* 	queue.addAll(population);
        	Semaphore sema=new Semaphore(parallelthreads);
        	while (finished.size() < population.size()) {
        		try {
					sema.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		if ((population.size()-(queue.size() + finished.size())) < parallelthreads) {
        			if (queue.size() > 0) {
	        			AbstractEAIndividual tmpindy = queue.get(0);
		        		queue.remove(0);
		        		tmpindy.resetConstraintViolation();
		        		EvalThread evalthread = new EvalThread(this,tmpindy,finished,population,sema);
		        		evalthread.start();

        			} 
        		}
        	}*/
        	Semaphore sema=new Semaphore(0);
        	ExecutorService pool = Executors.newFixedThreadPool(parallelthreads);     
        	for (int i = 0; i < population.size(); i++){
        		AbstractEAIndividual tmpindy =  (AbstractEAIndividual)population.get(i);   		
        		tmpindy.resetConstraintViolation();
        		EvalThread evalthread = new EvalThread(this,tmpindy,finished,population,sema);
        		pool.execute(evalthread);

        	}
        	try {
        		sema.acquire(population.size());
        	} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	pool.shutdownNow();
        } else {

	        for (int i = 0; i < population.size(); i++) {
	        	tmpIndy = (AbstractEAIndividual) population.get(i);
	        	synchronized (tmpIndy) {
	            	tmpIndy.resetConstraintViolation();
	            	this.evaluate(tmpIndy);
				}
	        	population.incrFunctionCalls();
	        }
        }
        
        evaluatePopulationEnd(population);
    }
    
    /**
     * Empty thunk for implementation in subclasses. This is called right before a population is evaluated.
     * Made public because some steady-state optimizers do not call evaluate(Population).
     *
     * @param population
     */   
    
    public void evaluatePopulationStart(Population population) {
	}

	/**
     * Empty thunk for implementation in subclasses. This is called after a population was evaluated.
     *
     * @param population
     */
    public void evaluatePopulationEnd(Population population) {
    }
    
    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evaluated
     */
    public abstract void evaluate(AbstractEAIndividual individual);

    /******************** Some output methods *******************************************/

    /** This method allows you to output a string that describes a found solution
     * in a way that is most suitable for a given problem.
     * @param individual    The individual that is to be shown.
     * @return The description.
     */
    public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
    	return AbstractEAIndividual.getDefaultStringRepresentation(individual);
    }

    /** This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     * @param pop   The population that is to be refined.
     * @return Double value
     */
    public Double getDoublePlotValue(Population pop) {
        return new Double(pop.getBestEAIndividual().getFitness(0));
    }

    /** This method returns the header for the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringHeader(PopulationInterface pop) {
        return "Solution";
//    	return "";
    }

    /** This method returns the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringValue(PopulationInterface pop) {
//    	return "";
        return AbstractEAIndividual.getDefaultDataString(pop.getBestIndividual());
    }

    /**
     * Convenience method, draws with undefined generation and function evaluation count.
     * 
     * @see #drawIndividual(int, int, AbstractEAIndividual)
     * @param indy
     * @return
     */
    public JComponent drawIndividual(AbstractEAIndividual indy) {
    	return drawIndividual(-1, -1, indy);
    }

    /** 
	 * This method allows you to request a graphical represenation for a given individual.
	 * The additional informations generation and funCalls are shown if they are >= 0.
     * individual.
     * @param generation	generation of the individual or -1
     * @param funCalls	function calls performed or -1
     * @param indy	the individual to display
     * @return JComponent
     */
    public JComponent drawIndividual(int generation, int funCalls, AbstractEAIndividual indy) {
        JPanel      result  = new JPanel();
        result.setLayout(new BorderLayout());
        JTextArea   area    = new JTextArea();
        JScrollPane scroll  = new JScrollPane(area);
        String text = "Best Solution:\n";
        if (generation >= 0) text+=" Generation: " + generation + "\n";
        if (funCalls >= 0) text+=" Evaluations: " + funCalls + "\n";
        text += this.getSolutionRepresentationFor(indy);
        area.setLineWrap(true);
        area.setText(text);
        area.setEditable(false);
        result.add(scroll, BorderLayout.CENTER);
        return result;
    }

    /** This method will report whether or not this optimization problem is truly
     * multi-objective
     * @return True if multi-objective, else false.
     */
    public boolean isMultiObjective() {
        if (this instanceof AbstractMultiObjectiveOptimizationProblem) {
            if (((AbstractMultiObjectiveOptimizationProblem)this).getMOSOConverter() instanceof MOSONoConvert) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     * For some evaluation cases it may be necessary to inform the problem class about the optimizer in use.
     *  
     * @param opt
     */
    public void informAboutOptimizer(InterfaceOptimizer opt) {
    	
    }

    /**
     * This allows "anyone" to access the problem's individual template and set operators etc.
     * Subclasses may have a method getEAIndividual additionally with a more
     * specific interface signature, which makes sense for the GUI which decides
     * on what classes to present to the user based on the interface signature. 
     * 
     * @return the problem's individual template
     */
    public AbstractEAIndividual getIndividualTemplate() {
    	return m_Template;
    }
    
    public String individualTemplateTipText() {
    	return "Choose the individual representation to use.";
    }
    
    /**
     * This method extracts the individuals from a given population that are assumed to correspond to local or global optima.
     * Similar individuals are clustered together with a density based clustering method
     * @param pop
     * @param epsilonPhenoSpace maximal allowed improvement of an individual before considered premature (given as distance in the search space)
     * @param epsilonFitConv if positive: additional absolute convergence criterion (fitness space) as termination criterion of the local search  
     * @param clusterSigma minimum cluster distance
     * @param numOfFailures 
     * @see #isPotentialOptimumNMS(AbstractEAIndividual, double, double, int)
     * @return 
     */
    public Population extractPotentialOptima(Population pop, double epsilonPhenoSpace, double epsilonFitConv, double clusterSigma, int numOfFailures) {
    	Population potOptima = new Population();
    	for (int i = 0; i < pop.size(); ++i){
    		AbstractEAIndividual indy = pop.getEAIndividual(i);
//    		System.out.println("bef: " + indy.toString());
    		if (isPotentialOptimumNMS(indy, epsilonPhenoSpace, epsilonFitConv, numOfFailures)){ 
//       	if (isPotentialOptimum(indy, epsilon,-1,-1)){ 
    			potOptima.addIndividual(indy);
    		}
    	}
    	if (clusterSigma > 0) return (Population)PostProcess.clusterBest(potOptima, clusterSigma, 0, PostProcess.KEEP_LONERS, PostProcess.BEST_ONLY).clone();
    	else return potOptima;
    }
   
  /**
     * This method estimates if the given individual is within epsilon of an optimum (local or global).
     * The algorithm tries to improve the given individual locally. 
     * If it is possible to improve the individual such that its changed position is further than epsilon,
     * the individual is considered premature. 
     * If not, the particle is assumed to correspond to a local or global optimum.
     * 
     * @param orig individual that is to be tested
     * @param epsilon maximal allowed improvement before considered premature (given as distance in the search space) 
     * @param mutationStepSize step size used to mutate the individual in one step 
     * (if < 0 a default value of 0.0001 is used)
     * @param numOfFailures number of unsuccessful improvement steps in a row before an individual is considered to be locally unimproveable
     * (if < 0 a default value of 100*problem dimensions is used ) 
     * @return estimation if the given individual is within epsilon of an optimum (local or global)
     */
    public boolean isPotentialOptimum(AbstractEAIndividual orig, double epsilon, double mutationStepSize, int numOfFailures){
    	int stepsCounter = 0; // could be used to limit the maximal number of steps overall
    	
    	// if not provided reasonable values use defaults:
    	if (mutationStepSize<0) mutationStepSize = 0.0001; 
    	if (numOfFailures<0) numOfFailures = 100*AbstractEAIndividual.getDoublePositionShallow(this.m_Template).length; // scales the effort with the number of problem dimensions
    	
    	AbstractEAIndividual indy = (AbstractEAIndividual)orig.clone();
    	this.evaluate(indy); // indy may be evaluated in a normalised way...
    	
    	InterfaceDistanceMetric metric = new PhenotypeMetric();
    	double overallDist = 0;
    	
    	InterfaceMutation mutator = new MutateESFixedStepSize(mutationStepSize);
    	
    	for (int i = 0; i < numOfFailures; ++i){
    		// backup
    		AbstractEAIndividual old = (AbstractEAIndividual)indy.clone();
    		// mutate
    		double  tmpD = indy.getMutationProbability();
    		indy.setMutationProbability(1.0);
    		mutator.mutate(indy);
    		++stepsCounter;
    		indy.setMutationProbability(tmpD); 
    		// evaluate
    		this.evaluate(indy);
    		
    		if (old.isDominatingDebConstraints(indy)) {// indy could not be improved
    			indy = (AbstractEAIndividual)old.clone();
    		} else { // indy could be improved
    			i = 0; // the given number of unsuccessful improvement steps should occur in a row
    			overallDist = metric.distance(orig, indy);
    			//System.out.println(overallDist);
    		}
    		if (overallDist > epsilon) {
    			return false; // dont waste any more evaluations on this candidate
    		}
    	}
        if (overallDist < epsilon) {
        	return true;
        }
        else return false; 
    }
    
    /**
     * Refine a given individual using Nelder-Mead-Simplex local search. Return true, if the refined result is within a given
     * distance from the original individual in phenotype space. The numOfFailures parameter gives the maximum evaluations
     * for the local search Using the epsilonFitConv parameter may define a convergence criterion as PhenotypeConvergenceTerminator 
     * which is combined (using OR) with the evaluation counter.
     * If numOfFailures is smaller than zero, 100*dim is used. Be aware that this may cost quite some runtime depending on the target
     * function.
     * 
     * @param orig
     * @param epsilonPhenoSpace
     * @param epsilonFitConv
     * @param numOfFailures
     * @return
     */
    public boolean isPotentialOptimumNMS(AbstractEAIndividual orig, double epsilonPhenoSpace, double epsilonFitConv, int numOfFailures){
    	
    	AbstractEAIndividual indy = (AbstractEAIndividual)orig.clone();
    	this.evaluate(indy); // indy may be evaluated in a normalised way...
    	
    	InterfaceDistanceMetric metric = new PhenotypeMetric();
    	double overallDist = 0;
    	double initPerturb = -1;
    	int dim = -1;
    	if (orig instanceof InterfaceDataTypeDouble) {
    		initPerturb = epsilonPhenoSpace/(2*(Mathematics.getAvgRange(((InterfaceDataTypeDouble)orig).getDoubleRange())));
    		dim=((InterfaceDataTypeDouble)orig).getDoubleRange().length;
        	if (numOfFailures<0) numOfFailures = 100*AbstractEAIndividual.getDoublePositionShallow(this.m_Template).length; // scales the effort with the number of problem dimensions
    	} else {
    		System.err.println("Cannot initialize NMS on non-double valued individuals!");
    		return false;
    	}
    	
    	Population pop = new Population(1);
    	pop.add(orig);
    	InterfaceTerminator term = new EvaluationTerminator(numOfFailures); 
    	if (epsilonFitConv > 0) term = new CombinedTerminator(new PhenotypeConvergenceTerminator(epsilonFitConv, 10*dim, true, true), term, false);
    	int evalsPerf = PostProcess.processSingleCandidatesNMCMA(PostProcessMethod.nelderMead, pop, term, initPerturb, this);
    	overallDist = metric.distance(indy, pop.getBestEAIndividual());
//    	System.out.println("aft: " + pop.getBestEAIndividual().toString() + ", evals performed: " + evalsPerf + ", opt moved by " + overallDist);
//    	System.out.println("terminated because: " + term.lastTerminationMessage());
        if (overallDist < epsilonPhenoSpace) return true;
        else return false; 
    }
    
//    /**********************************************************************************************************************
//     * These are for InterfaceParamControllable
//     */
//	public Object[] getParamControl() {
//		return null;
//	}
//	
//	public void notifyParamChanged(String member, Object oldVal, Object newVal) {
//		if (changeListeners != null) for (ParamChangeListener l : changeListeners) {
//			l.notifyChange(this, oldVal, newVal, null);
//		}
//	}
//	
//	public void addChangeListener(ParamChangeListener l) {
//		if (changeListeners==null) changeListeners = new ArrayList<ParamChangeListener>();
//		if (!changeListeners.contains(l)) changeListeners.add(l);
//	}
//	
//	public void removeChangeListener(ParamChangeListener l) {
//		if (changeListeners!=null) changeListeners.remove(l);
//	}
	
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "AbstractOptimizationProblem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The programmer failed to give further details.";
    }
}
