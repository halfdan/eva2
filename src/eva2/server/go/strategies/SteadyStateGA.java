package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectTournament;
import eva2.server.go.operators.selection.replacement.InterfaceReplacement;
import eva2.server.go.operators.selection.replacement.ReplaceWorst;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** A simple implementation of the steady-state GA with variable
 * replacement schemes. To reduce the logging effort population.size()
 * optimization steps are performed each time optimize() is called.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 14:30:20
 * To change this template use File | Settings | File Templates.
 */
public class SteadyStateGA implements InterfaceOptimizer, java.io.Serializable {

    private Population                      m_Population        = new Population();
    private InterfaceOptimizationProblem    m_Problem           = new B1Problem();
    private InterfaceSelection              m_ParentSelection   = new SelectTournament();
    private InterfaceSelection              m_PartnerSelection  = new SelectTournament();
    private InterfaceReplacement            m_ReplacementSelection  = new ReplaceWorst();
    private int                             m_NumberOfPartners  = 1;

        private String                m_Identifier = "";
        transient private InterfacePopulationChangedEventListener m_Listener;

        public SteadyStateGA() {
        }

        public SteadyStateGA(SteadyStateGA a) {
            this.m_Population                   = (Population)a.m_Population.clone();
            this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
            this.m_Identifier                   = a.m_Identifier;
            this.m_NumberOfPartners             = a.m_NumberOfPartners;
            this.m_ParentSelection              = (InterfaceSelection)a.m_ParentSelection.clone();
            this.m_PartnerSelection             = (InterfaceSelection)a.m_PartnerSelection.clone();
            this.m_ReplacementSelection         = (InterfaceReplacement)a.m_ReplacementSelection.clone();
        }

    @Override
        public Object clone() {
            return (Object) new SteadyStateGA(this);
        }

    @Override
        public void init() {
            this.m_Problem.initPopulation(this.m_Population);
            this.evaluatePopulation(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }

        /** This method will init the optimizer with a given population
         * @param reset     If true the population is reset.
         */
    @Override
        public void initByPopulation(Population pop, boolean reset) {
            this.m_Population = (Population)pop.clone();
            if (reset) {
            	this.m_Population.init();
                this.evaluatePopulation(this.m_Population);
                this.firePropertyChangedEvent(Population.nextGenerationPerformed);
            }
        }

        /** This method will evaluate the current population using the
         * given problem.
         * @param population The population that is to be evaluated
         */
        private void evaluatePopulation(Population population) {
            this.m_Problem.evaluate(population);
            population.incrGeneration();
        }

        /** This method will assign fitness values to all individual in the
         * current population.
         * @param population The population that is to be evaluated
         */
        private void defaultEvaluatePopulation(Population population) {
            GAIndividualBinaryData tmpIndy;
            for (int i = 0; i < population.size(); i++) {
                tmpIndy = (GAIndividualBinaryData) population.get(i);
                tmpIndy.SetFitness(0, tmpIndy.defaultEvaulateAsMiniBits());
                population.incrFunctionCalls();
            }
            population.incrGeneration();
        }

        /** This method will generate the offspring population from the
         * given population of evaluated individuals.
         */
        private void generateChildren() {
            this.m_ParentSelection.prepareSelection(this.m_Population);
            this.m_PartnerSelection.prepareSelection(this.m_Population);
            Population parents                  = this.m_ParentSelection.selectFrom(this.m_Population, 1);
            AbstractEAIndividual mother         = (AbstractEAIndividual)parents.get(0);
            parents                             = this.m_PartnerSelection.findPartnerFor(mother, this.m_Population, this.m_NumberOfPartners);
            AbstractEAIndividual[]  offSprings  = mother.mateWith(parents);
            offSprings[0].mutate();
            this.m_Problem.evaluate(offSprings[0]);
            this.m_ReplacementSelection.insertIndividual(offSprings[0], this.m_Population, parents);
        }

    @Override
        public void optimize() {
            for (int i = 0; i < this.m_Population.size(); i++) this.generateChildren();
            this.m_Population.incrFunctionCallsBy(this.m_Population.size());
            this.m_Population.incrGeneration();
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
    @Override
        public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
            this.m_Listener = ea;
        }
    @Override
    	public boolean removePopulationChangedEventListener(
    			InterfacePopulationChangedEventListener ea) {
    		if (m_Listener==ea) {
    			m_Listener=null;
    			return true;
    		} else return false;
    	}
        protected void firePropertyChangedEvent (String name) {
            if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
        }

        /** This method will set the problem that is to be optimized
         * @param problem
         */
    @Override
        public void setProblem (InterfaceOptimizationProblem problem) {
            this.m_Problem = problem;
        }
    @Override
        public InterfaceOptimizationProblem getProblem () {
            return this.m_Problem;
        }

        /** This method will return a string describing all properties of the optimizer
         * and the applied methods.
         * @return A descriptive string
         */
    @Override
        public String getStringRepresentation() {
            String result = "";
            result += "Genetic Algorithm:\n";
            result += "Optimization Problem: ";
            result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
            result += this.m_Population.getStringRepresentation();
            return result;
        }
        /** This method allows you to set an identifier for the algorithm
         * @param name      The indenifier
         */
    @Override
         public void setIdentifier(String name) {
            this.m_Identifier = name;
        }
    @Override
         public String getIdentifier() {
             return this.m_Identifier;
         }

        /** This method is required to free the memory on a RMIServer,
         * but there is nothing to implement.
         */
    @Override
        public void freeWilly() {

        }
    /**********************************************************************************************************************
     * These are for GUI
     */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a Steady-State Genetic Algorithm.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "SS-GA";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.m_Population;
    }
    @Override
    public void setPopulation(Population pop){
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
    
    @Override
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
    /** This method will set the parent selection method that is to be used
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

    /** This method will set the number of partners that are needed to create
     * offsprings by mating
     * @param partners
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) partners = 0;
        this.m_NumberOfPartners = partners;
    }
    public int getNumberOfPartners() {
        return this.m_NumberOfPartners;
    }
    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }

   /** Choose a selection method for selecting recombination partners for given parents
    * @param selection
    */
    public void setPartnerSelection(InterfaceSelection selection) {
        this.m_PartnerSelection = selection;
    }
    public InterfaceSelection getPartnerSelection() {
        return this.m_PartnerSelection;
    }
    public String partnerSelectionTipText() {
        return "Choose a selection method for selecting recombination partners for given parents.";
    }

    /** Choose a replacement strategy
     * @param selection
     */
    public void setReplacementSelection(InterfaceReplacement selection) {
        this.m_ReplacementSelection = selection;
    }
    public InterfaceReplacement getReplacementSelection() {
        return this.m_ReplacementSelection;
    }
    public String replacementSelectionTipText() {
        return "Choose a replacement strategy.";
    }
}
