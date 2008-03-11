package javaeva.server.go.strategies;

import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.archiving.ArchivingMOMAIIAllDominating;
import javaeva.server.go.operators.archiving.InformationRetrievalInserting;
import javaeva.server.go.operators.archiving.InterfaceArchiving;
import javaeva.server.go.operators.archiving.InterfaceInformationRetrieval;
import javaeva.server.go.operators.selection.SelectMOMAIIDominanceCounter;
import javaeva.server.go.operators.selection.SelectMONonDominated;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.FM0Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;

/** This is still under construction.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.02.2005
 * Time: 16:39:05
 * To change this template use File | Settings | File Templates.
 */
public class MultiObjectiveMemeticAlgorithmII implements InterfaceOptimizer, java.io.Serializable {

    private InterfaceOptimizer              m_Optimizer         = new GeneticAlgorithm();
    private InterfaceArchiving              m_Archiver          = new ArchivingMOMAIIAllDominating();
    private InterfaceInformationRetrieval   m_InformationRetrieval = new InformationRetrievalInserting();
    private InterfaceOptimizationProblem    m_Problem           = new FM0Problem();
    private String                          m_Identifier        = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    public MultiObjectiveMemeticAlgorithmII() {
        this.m_Optimizer.getPopulation().setPopulationSize(100);
        ((GeneticAlgorithm)this.m_Optimizer).setParentSelection(new SelectMOMAIIDominanceCounter());
        ((GeneticAlgorithm)this.m_Optimizer).setPartnerSelection(new SelectMOMAIIDominanceCounter());
    }

    public MultiObjectiveMemeticAlgorithmII(MultiObjectiveMemeticAlgorithmII a) {
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Optimizer                    = (InterfaceOptimizer)a.m_Optimizer.clone();
        this.m_Archiver                     = (InterfaceArchiving)a.m_Archiver.clone();
        this.m_InformationRetrieval         = (InterfaceInformationRetrieval)a.m_InformationRetrieval.clone();
    }

    public Object clone() {
        return (Object) new MultiObjectiveMemeticAlgorithmII(this);
    }

    public void init() {
        this.m_Optimizer.init();
        this.m_Archiver.addElementsToArchive(this.m_Optimizer.getPopulation());
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Optimizer.initByPopulation(pop, reset);
        this.m_Archiver.addElementsToArchive(this.m_Optimizer.getPopulation());
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** The optimize method will compute a 'improved' and evaluated population
     */
    public void optimize() {

        // This is in total compliance with Koch's framework nice isn't it?
        this.m_Optimizer.optimize();

        // now comes all the multiobjective optimization stuff
        // This is the Environment Selection
        this.m_Archiver.addElementsToArchive(this.m_Optimizer.getPopulation());

        // The InformationRetrieval will choose from the archive and the current population
        // the population from which in the next generation the parents will be selected.
        this.m_InformationRetrieval.retrieveInformationFrom(this.m_Optimizer.getPopulation());

        System.gc();

        this.firePropertyChangedEvent("NextGenerationPerformed");
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
        this.m_Optimizer.SetProblem(problem);
    }
    public InterfaceOptimizationProblem getProblem () {
        return this.m_Problem;
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "MOMA II:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        result += this.m_Optimizer.getPopulation().getStringRepresentation();
        return result;
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
        return "Multi-Objective Memetic Algorithms level II.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "MOMA II";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return this.m_Optimizer.getPopulation();
    }
    public void setPopulation(Population pop){
        this.m_Optimizer.setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the Population used.";
    }
    
    public Population getAllSolutions() {
    	return getPopulation();
    }
    /** This method allows you to set/get the optimizing technique to use.
     * @return The current optimizing method
     */
    public InterfaceOptimizer getOptimizer() {
        return this.m_Optimizer;
    }
    public void setOptimizer(InterfaceOptimizer b){
        this.m_Optimizer = b;
    }
    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /** This method allows you to set/get the archiving strategy to use.
     * @return The current optimizing method
     */
    public InterfaceArchiving getArchivingStrategy() {
        return this.m_Archiver;
    }
    public void setArchivingStrategy(InterfaceArchiving b){
        this.m_Archiver = b;
    }
    public String archivingStrategyTipText() {
        return "Choose the archiving strategy.";
    }

    /** This method allows you to set/get the Information Retrieval strategy to use.
     * @return The current optimizing method
     */
    public InterfaceInformationRetrieval getInformationRetrieval() {
        return this.m_InformationRetrieval;
    }
    public void setInformationRetrieval(InterfaceInformationRetrieval b){
        this.m_InformationRetrieval = b;
    }
    public String informationRetrievalTipText() {
        return "Choose the Information Retrieval strategy.";
    }

    /** This method allows you to set/get the size of the archive.
     * @return The current optimizing method
     */
    public int getArchiveSize() {
        Population archive = this.m_Optimizer.getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            this.m_Optimizer.getPopulation().SetArchive(archive);
        }
        return archive.getPopulationSize();
    }
    public void setArchiveSize(int b){
        Population archive = this.m_Optimizer.getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            this.m_Optimizer.getPopulation().SetArchive(archive);
        }
        archive.setPopulationSize(b);
    }
    public String archiveSizeTipText() {
        return "Choose the size of the archive.";
    }
}