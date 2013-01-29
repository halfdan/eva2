package eva2.server.go.strategies;

import eva2.gui.PropertyDoubleArray;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.moso.MOSOWeightedFitness;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.server.go.problems.FM0Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** The winged MOEA was a nice idea, which didn't really work out.
 * Here a standard MOEA is assisted by n additional local searchers, each
 * optimizing just one objective. The idea was that these local optimizers
 * would span the search space and would allow the MOEA to converge faster.
 * But in the end the performance of this algorithm strongly depends on the
 * optimization problem. 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.02.2005
 * Time: 16:34:22
 * To change this template use File | Settings | File Templates.
 */
public class WingedMultiObjectiveEA implements InterfaceOptimizer, java.io.Serializable {

    private InterfaceOptimizer              m_MOOptimizer       = new MultiObjectiveEA();
    private InterfaceOptimizer              m_SOOptimizer       = new GeneticAlgorithm();
    private InterfaceOptimizer[]            m_SOOptimizers;
    private Population                      m_Population        = new Population();
    private int                             m_MigrationRate     = 5;
    private int                             m_OutputDimension   = 2;
    private int                             m_NumberOfLocalOptimizers = 2;
    private InterfaceOptimizationProblem    m_Problem           = new FM0Problem();
    private String                          m_Identifier        = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    public WingedMultiObjectiveEA() {
    }

    public WingedMultiObjectiveEA(WingedMultiObjectiveEA a) {
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_MOOptimizer                  = (InterfaceOptimizer)a.m_MOOptimizer.clone();
        this.m_SOOptimizer                  = (InterfaceOptimizer)a.m_SOOptimizer.clone();
        if (a.m_SOOptimizers != null) {
            this.m_SOOptimizers = new InterfaceOptimizer[a.m_SOOptimizers.length];
            for (int i = 0; i < this.m_SOOptimizers.length; i++) {
                this.m_SOOptimizers[i]      = (InterfaceOptimizer)a.m_SOOptimizers[i].clone();
            }
        }
        this.m_MigrationRate                = a.m_MigrationRate;
        this.m_Population                   = (Population)a.m_Population.clone();
    }

    @Override
    public Object clone() {
        return (Object) new WingedMultiObjectiveEA(this);
    }

    @Override
    public void init() {
        if (this.m_Problem instanceof AbstractMultiObjectiveOptimizationProblem) {
            AbstractMultiObjectiveOptimizationProblem   tmpProb = (AbstractMultiObjectiveOptimizationProblem)this.m_Problem;
            AbstractMultiObjectiveOptimizationProblem   tmpP;
            MOSOWeightedFitness                         tmpWF;
            PropertyDoubleArray                         tmpDA;
            int         dim = this.m_OutputDimension;
            double[]    weights;
            // dim = tmpProb.getOutputDimension();
            this.m_MOOptimizer.setProblem((InterfaceOptimizationProblem)this.m_Problem.clone());
            this.m_MOOptimizer.init();
            this.m_SOOptimizers = new InterfaceOptimizer[dim];
            for (int i = 0; i < dim; i++) {
                tmpP = (AbstractMultiObjectiveOptimizationProblem)this.m_Problem.clone();
                weights = new double[dim];
                for (int j = 0; j < dim; j++) weights[j] = 0;
                weights[i] = 1;
                tmpDA = new PropertyDoubleArray(weights);
                tmpWF = new MOSOWeightedFitness();
                tmpWF.setWeights(tmpDA);
                tmpP.setMOSOConverter(tmpWF);
                this.m_SOOptimizers[i] = (InterfaceOptimizer)this.m_SOOptimizer.clone();
                this.m_SOOptimizers[i].setProblem(tmpP);
                this.m_SOOptimizers[i].init();
            }
        } else {
            this.m_SOOptimizer.setProblem(this.m_Problem);
            this.m_SOOptimizer.init();
        }
        this.communicate();
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }


    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        if (this.m_Problem instanceof AbstractMultiObjectiveOptimizationProblem) {
            AbstractMultiObjectiveOptimizationProblem   tmpProb = (AbstractMultiObjectiveOptimizationProblem)this.m_Problem;
            AbstractMultiObjectiveOptimizationProblem   tmpP;
            MOSOWeightedFitness                         tmpWF;
            PropertyDoubleArray                         tmpDA;
            int         dim = 2;
            double[]    weights;
            // dim = tmpProb.getOutputDimension();
            this.m_MOOptimizer.setProblem((InterfaceOptimizationProblem)this.m_Problem.clone());
            this.m_MOOptimizer.initByPopulation(pop, reset);
            this.m_SOOptimizers = new InterfaceOptimizer[dim];
            for (int i = 0; i < dim; i++) {
                tmpP = (AbstractMultiObjectiveOptimizationProblem)this.m_Problem.clone();
                weights = new double[dim];
                for (int j = 0; j < dim; j++) weights[j] = 0;
                weights[i] = 1;
                tmpDA = new PropertyDoubleArray(weights);
                tmpWF = new MOSOWeightedFitness();
                tmpWF.setWeights(tmpDA);
                tmpP.setMOSOConverter(tmpWF);
                this.m_SOOptimizers[i] = (InterfaceOptimizer)this.m_SOOptimizer.clone();
                this.m_SOOptimizers[i].setProblem(tmpP);
                this.m_SOOptimizers[i].initByPopulation(pop, reset);
            }
        } else {
            this.m_SOOptimizer.setProblem(this.m_Problem);
            this.m_SOOptimizer.initByPopulation(pop, reset);
        }
        this.communicate();
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** The optimize method will compute a 'improved' and evaluated population
     */
    @Override
    public void optimize() {

        this.m_MOOptimizer.optimize();
        for (int i = 0; i < this.m_SOOptimizers.length; i++) {
            this.m_SOOptimizers[i].optimize();
        }
        this.m_Population.incrGeneration();
        if ((this.m_Population.getGeneration() % this.m_MigrationRate) == 0) {
            this.communicate();
        }

        System.gc();

        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** This method will manage comunication between the
     * islands
     */
    private void communicate() {
        int oldFunctionCalls;
        this.m_Population.clear();
        this.m_Population.SetFunctionCalls(0);
        Population pop;
        // first collect all the data
        pop = (Population)this.m_MOOptimizer.getPopulation().clone();
        this.m_Population.addPopulation(pop);
        this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
        for (int i = 0; i < this.m_SOOptimizers.length; i++) {
            pop = (Population)this.m_SOOptimizers[i].getPopulation().clone();
            this.m_Population.addPopulation(pop);
            this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
        oldFunctionCalls = this.m_Population.getFunctionCalls();
        this.m_Problem.evaluate(this.m_Population);
        this.m_Population.SetFunctionCalls(oldFunctionCalls);
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
//        double plotValue = (this.m_Problem.getDoublePlotValue(this.m_Population)).doubleValue();
        // now they are synchronized lets migrate
        this.migrate();
    }

    /** This method implements the migration between the optimzers
     *
     */
    private void migrate() {
        AbstractEAIndividual[] bestIndys = new AbstractEAIndividual[this.m_OutputDimension];
        double                 tmpF1, tmpF2;
        // for each dimension find the best
        for (int i = 0; i < this.m_OutputDimension; i++) {
            bestIndys[i] = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Population.get(0)).clone();
            tmpF1 = bestIndys[i].getFitness(i);
            // for each individual find the best
            for (int j = 0; j < this.m_Population.size(); j++) {
                 if (((AbstractEAIndividual)this.m_Population.get(j)).getFitness(i) < tmpF1) {
                     bestIndys[i] = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Population.get(j)).clone();
                     tmpF1 = bestIndys[i].getFitness(i);
                 }
            }
        }
        // now perform the migration
        AbstractEAIndividual tmpIndy;
        for (int i = 0; i < this.m_OutputDimension; i++) {
            tmpIndy = (AbstractEAIndividual)bestIndys[i].clone();
            this.m_MOOptimizer.getProblem().evaluate(tmpIndy);
            this.m_MOOptimizer.getPopulation().set(i, tmpIndy);
            tmpIndy = (AbstractEAIndividual)bestIndys[i].clone();
            this.m_SOOptimizers[i].getProblem().evaluate(tmpIndy);
            this.m_SOOptimizers[i].getPopulation().set(0, bestIndys[i]);
        }
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
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
    /** Something has changed
     */
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
        result += "EMO:\n";
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
        return "This is Evolutionary Multi-Criteria Optimization Algorithm hybridized with Local Searchers to span the Pareto-Front.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "EMO-LS";
    }

    /** Assuming that all optimizer will store their data in a population
     * we will allow access to this population to query to current state
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
        return "(Defunct)";
    }
    
    @Override
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
    /** This method allows you to set/get the optimizing technique to use.
     * @return The current optimizing method
     */
    public InterfaceOptimizer getMOOptimizer() {
        return this.m_MOOptimizer;
    }
    public void setMOOptimizer(InterfaceOptimizer b){
        this.m_MOOptimizer = b;
    }
    public String mOOptimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /** This method allows you to set/get the optimizing technique to use.
     * @return The current optimizing method
     */
    public InterfaceOptimizer getSOOptimizer() {
        return this.m_SOOptimizer;
    }
    public void setSOOptimizer(InterfaceOptimizer b){
        this.m_SOOptimizer = b;
    }
    public String sOOptimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /** This method allows you to set/get the archiving strategy to use.
     * @return The current optimizing method
     */
    public int getMigrationRate() {
        return this.m_MigrationRate;
    }
    public void setMigrationRate(int b){
        this.m_MigrationRate = b;
    }
    public String migrationRateTipText() {
        return "Choose a proper migration rate.";
    }
}