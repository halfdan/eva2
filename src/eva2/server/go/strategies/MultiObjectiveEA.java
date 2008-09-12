package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.archiving.ArchivingNSGAII;
import eva2.server.go.operators.archiving.InformationRetrievalInserting;
import eva2.server.go.operators.archiving.InterfaceArchiving;
import eva2.server.go.operators.archiving.InterfaceInformationRetrieval;
import eva2.server.go.operators.selection.SelectMONonDominated;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.FM0Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** 
 * A generic framework for multi-objecitve optimization, you need
 * to specify an optimization strategy (like GA), an archiver and
 * an information retrival strategy. With this scheme you can realized:
 *	Vector Evaluated GA
 *	Random Weight GA
 *	Multiple Objective GA
 *	NSGA
 *	NSGA-II
 *	SPEA
 *	SPEA 2
 *	PESA
 *	PESA-II
 * In case you address a multi-objective optimization problem with a single-
 * objective optimizer instead of this MOEA, such an optimizer would randomly
 * toggle between the objective for each selection and thus explore at least
 * the extreme points of the objective space, but simpler methods like
 * random search or hill-climbing might even fail on that. 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.06.2003
 * Time: 11:03:50
 * To change this template use Options | File Templates.
 */
public class MultiObjectiveEA implements InterfaceOptimizer, java.io.Serializable {

    private InterfaceOptimizer              m_Optimizer         = new GeneticAlgorithm();
    private InterfaceArchiving              m_Archiver          = new ArchivingNSGAII();
    private InterfaceInformationRetrieval   m_InformationRetrieval = new InformationRetrievalInserting();
    private InterfaceOptimizationProblem    m_Problem           = new FM0Problem();
    private String                          m_Identifier        = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    public MultiObjectiveEA() {
        this.m_Optimizer.getPopulation().setPopulationSize(100);
        ((GeneticAlgorithm)this.m_Optimizer).setParentSelection(new SelectMONonDominated());
        ((GeneticAlgorithm)this.m_Optimizer).setPartnerSelection(new SelectMONonDominated());
    }

    public MultiObjectiveEA(MultiObjectiveEA a) {
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Optimizer                    = (InterfaceOptimizer)a.m_Optimizer.clone();
        this.m_Archiver                     = (InterfaceArchiving)a.m_Archiver.clone();
        this.m_InformationRetrieval         = (InterfaceInformationRetrieval)a.m_InformationRetrieval.clone();
    }

    public MultiObjectiveEA(InterfaceOptimizer subOpt, InterfaceArchiving archiving, int archiveSize,
    	    InterfaceInformationRetrieval infoRetrieval, AbstractOptimizationProblem problem) {
        setOptimizer(subOpt);
        setArchivingStrategy(archiving);
        setArchiveSize(archiveSize);
        setInformationRetrieval(infoRetrieval);
        SetProblem(problem);
    }
    
    public Object clone() {
        return (Object) new MultiObjectiveEA(this);
    }

    public void init() {
        this.m_Optimizer.init();
        this.m_Archiver.addElementsToArchive(this.m_Optimizer.getPopulation());
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }


    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Optimizer.initByPopulation(pop, reset);
        this.m_Archiver.addElementsToArchive(this.m_Optimizer.getPopulation());
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** The optimize method will compute a 'improved' and evaluated population
     */
    public void optimize() {
//        double[][] may = this.showMay(this.m_Optimizer.getPopulation());
        // This is in total compliance with Koch's framework nice isn't it?
        this.m_Optimizer.optimize();
        // now comes all the multiobjective optimization stuff
        // This is the Environment Selection
        this.m_Archiver.addElementsToArchive(this.m_Optimizer.getPopulation());
        //if (true) this.m_Archiver.plotArchive(this.m_Optimizer.getPopulation());
//        if (false) {
//            int popSize = this.m_Optimizer.getPopulation().size();
//            int archiveSize = this.m_Optimizer.getPopulation().getArchive().size();
//            int feasiblePop = 0, feasibleArch = 0;
//            for (int i = 0; i < popSize; i++) {
//                if (((AbstractEAIndividual)this.m_Optimizer.getPopulation().get(i)).getConstraintViolation() == 0) feasiblePop++;
//            }
//            for (int i = 0; i < archiveSize; i++) {
//                if (((AbstractEAIndividual)this.m_Optimizer.getPopulation().getArchive().get(i)).getConstraintViolation() == 0) feasibleArch++;
//            }
//            System.out.println("Population size : "+popSize + " ("+feasiblePop+"/"+(popSize-feasiblePop)+")");
//            System.out.println("Archive size    : "+archiveSize + " ("+feasibleArch+"/"+(archiveSize-feasibleArch)+")");
//        }

        // The InformationRetrieval will choose from the archive and the current population
        // the population from which in the next generation the parents will be selected.
        this.m_InformationRetrieval.retrieveInformationFrom(this.m_Optimizer.getPopulation());

//        double[][] mayday = this.showMay(this.m_Optimizer.getPopulation());
//        if ((mayday[0][0] > may[0][0]) || (mayday[1][1] > may[1][1])) {
//            System.out.println("Losing the edges:");
//            System.out.println("Before : (" +may[0][0]+"/"+may[0][1]+") and ("+may[1][0]+"/"+may[1][1]+")");
//            System.out.println("After  : (" +mayday[0][0]+"/"+mayday[0][1]+") and ("+mayday[1][0]+"/"+mayday[1][1]+")");
//        }

        System.gc();

        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    private double[][] showMay(Population pop) {
        Population tmp = new Population();
        tmp.addPopulation(pop);
        if (pop.getArchive() != null) tmp.addPopulation(pop.getArchive());

        double[][] fitness = new double[tmp.size()][];
        for (int i = 0; i < tmp.size(); i++) fitness[i] = ((AbstractEAIndividual)tmp.get(i)).getFitness();
        double[] minY, maxY;
        minY = fitness[0];
        maxY = fitness[0];
        for (int i = 1; i < fitness.length; i++) {
            if (minY[0] > fitness[i][0]) minY = fitness[i];
            if (maxY[1] > fitness[i][1]) maxY = fitness[i];
        }
        double[][] result = new double[2][];
        result[0] = minY;
        result[1] = maxY;
        //System.out.println("Borders: ("+ (Math.round((100*minY[0]))/100.0)+"/"+ (Math.round((100*minY[1]))/100.0)+") ("+ (Math.round((100*maxY[0]))/100.0)+"/"+ (Math.round((100*maxY[1]))/100.0)+")");
        return result;
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
        result += "Multi-Objective Evolutionary Algorithm:\n";
        result += "Using:\n";
        result += " Archiving Strategy    = " + this.m_Archiver.getClass().toString() + "\n";
        result += " Information Retrieval = " + this.m_InformationRetrieval.getClass().toString() + "\n";
        result += " Information Retrieval = " + this.getClass().toString() + "\n";
        result += " Optimizer             = " + this.m_Optimizer.getClass().toString() + "\n";
        result += this.m_Optimizer.getStringRepresentation() +"\n";
        //result += "=> The Optimization Problem: ";
        //result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        //result += this.m_Population.getStringRepresentation();
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
        return "This is general Evolutionary Multi-Criteria Optimization Framework.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "EMO";
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
    
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation(), getPopulation().getArchive());
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
