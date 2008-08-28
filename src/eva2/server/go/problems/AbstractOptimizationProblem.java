package eva2.server.go.problems;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.moso.MOSONoConvert;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESFixedStepSize;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 29.08.2003
 * Time: 13:40:12
 * To change this template use Options | File Templates.
 */
public abstract class AbstractOptimizationProblem implements InterfaceOptimizationProblem, java.io.Serializable {

    protected 	AbstractEAIndividual      m_Template;

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public abstract Object clone();

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

//        if (population.isEvaluated()) {
//        	System.err.println("Population evaluation seems not required!");
//        } else {
	        // @todo This is the position to implement a granular
	        // @todo paralliziation scheme
	        evaluatePopulationStart(population);
	        for (int i = 0; i < population.size(); i++) {
	            tmpIndy = (AbstractEAIndividual) population.get(i);
	            tmpIndy.resetConstraintViolation();
	            this.evaluate(tmpIndy);
	            population.incrFunctionCalls();
	        }
	        evaluatePopulationEnd(population);
//	        population.setEvaluated();
//        }
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

    /** This method returns a single line representation of the solution
     * @param individual  The individual
     * @return The string
     */
//    public String getSolutionDataFor(IndividualInterface individual) {
//    }

//    /** This method returns a string describing the optimization problem.
//     * @return The description.
//     */
//    public String getStringRepresentationF() {
//        return "AbstractOptimizationProblem: programmer failed to give further details";
//    }

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
    }

    /** This method returns the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringValue(PopulationInterface pop) {
        return AbstractEAIndividual.getDefaultDataString(pop.getBestIndividual());
    }

    /** This method allows you to request a graphical represenation for a given
     * individual.
     * @return JComponent
     */
    public JComponent drawIndividual(AbstractEAIndividual indy) {
        JPanel      result  = new JPanel();
        result.setLayout(new BorderLayout());
        JTextArea   area    = new JTextArea();
        JScrollPane scroll  = new JScrollPane(area);
        area.setText("Best Solution:\n"+this.getSolutionRepresentationFor(indy));
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
    
    /**
     * This method extracts the individuals from a given population that are assumed to correspond to local or global optima.
     * Similar inidviduals are clustered together with a density based clustering method
     * @param pop
     * @param epsilon maximal allowed improvement of an individual before considered premature (given as distance in the search space) 
     * @param clusterSigma minimum cluster distance
     * @return 
     */
    public Population extractPotentialOptima(Population pop, double epsilon, double clusterSigma) {
    	Population potOptima = new Population();
    	for (int i = 0; i < pop.size(); ++i){
    		AbstractEAIndividual indy = pop.getEAIndividual(i);
    		if (isPotentialOptimum(indy, epsilon,-1,-1)){ 
    			potOptima.addIndividual(indy);
    		}
    	}
    	Population clusteredPop = (Population)PostProcess.clusterBest(potOptima, clusterSigma, 0, PostProcess.KEEP_LONERS, PostProcess.BEST_ONLY).clone();
    	return clusteredPop;
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
    	if (numOfFailures<0) numOfFailures = 100*AbstractEAIndividual.getDoublePosition(this.m_Template).length; // scales the effort with the number of problem dimensions
    	
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
