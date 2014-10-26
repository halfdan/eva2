package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.EAIndividualComparator;
import eva2.optimization.operator.archiving.ArchivingNSGAII;
import eva2.optimization.operator.archiving.InformationRetrievalInserting;
import eva2.optimization.operator.archiving.InterfaceArchiving;
import eva2.optimization.operator.archiving.InterfaceInformationRetrieval;
import eva2.optimization.operator.selection.SelectMONonDominated;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.FM0Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * A generic framework for multi-objecitve optimization, you need to specify an
 * optimization strategy (like GA), an archiver and an information retrival
 * strategy. With this scheme you can realized: Vector Evaluated GA Random
 * Weight GA Multiple Objective GA NSGA NSGA-II SPEA SPEA 2 PESA PESA-II In case
 * you address a multi-objective optimization problem with a single- objective
 * optimizer instead of this MOEA, such an optimizer would randomly toggle
 * between the objective for each selection and thus explore at least the
 * extreme points of the objective space, but simpler methods like random search
 * or hill-climbing might even fail on that.
 */
@Description("This is a general Multi-objective Evolutionary Optimization Framework.")
public class MultiObjectiveEA implements InterfaceOptimizer, java.io.Serializable {

    private InterfaceOptimizer optimizer = new GeneticAlgorithm();
    private InterfaceArchiving archiver = new ArchivingNSGAII();
    private InterfaceInformationRetrieval informationRetrieval = new InformationRetrievalInserting();
    private InterfaceOptimizationProblem optimizationProblem = new FM0Problem();
    private String identifier = "";
    transient private InterfacePopulationChangedEventListener populationChangedEventListener;

    public MultiObjectiveEA() {
        this.optimizer.getPopulation().setTargetSize(100);
        ((GeneticAlgorithm) this.optimizer).setParentSelection(new SelectMONonDominated());
        ((GeneticAlgorithm) this.optimizer).setPartnerSelection(new SelectMONonDominated());
    }

    public MultiObjectiveEA(MultiObjectiveEA a) {
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.optimizer = (InterfaceOptimizer) a.optimizer.clone();
        this.archiver = (InterfaceArchiving) a.archiver.clone();
        this.informationRetrieval = (InterfaceInformationRetrieval) a.informationRetrieval.clone();
    }

    public MultiObjectiveEA(InterfaceOptimizer subOpt, InterfaceArchiving archiving, int archiveSize,
                            InterfaceInformationRetrieval infoRetrieval, AbstractOptimizationProblem problem) {
        setOptimizer(subOpt);
        setArchivingStrategy(archiving);
        setArchiveSize(archiveSize);
        setInformationRetrieval(infoRetrieval);
        setProblem(problem);
    }

    @Override
    public Object clone() {
        return new MultiObjectiveEA(this);
    }

    @Override
    public void initialize() {
        this.optimizer.initialize();
        this.archiver.addElementsToArchive(this.optimizer.getPopulation());
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.optimizer.initializeByPopulation(pop, reset);
        this.archiver.addElementsToArchive(this.optimizer.getPopulation());
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * The optimize method will compute a 'improved' and evaluated population
     */
    @Override
    public void optimize() {
//        double[][] may = this.showMay(this.optimizer.getPopulation());
        // This is in total compliance with Koch's framework nice isn't it?
        this.optimizer.optimize();
        // now comes all the multiobjective optimization stuff
        // This is the Environment Selection
        this.archiver.addElementsToArchive(this.optimizer.getPopulation());
        //if (true) this.archiver.plotArchive(this.optimizer.getPopulation());
//        if (false) {
//            int popSize = this.optimizer.getPopulation().size();
//            int archiveSize = this.optimizer.getPopulation().getArchive().size();
//            int feasiblePop = 0, feasibleArch = 0;
//            for (int i = 0; i < popSize; i++) {
//                if (((AbstractEAIndividual)this.optimizer.getPopulation().get(i)).getConstraintViolation() == 0) feasiblePop++;
//            }
//            for (int i = 0; i < archiveSize; i++) {
//                if (((AbstractEAIndividual)this.optimizer.getPopulation().getArchive().get(i)).getConstraintViolation() == 0) feasibleArch++;
//            }
//            System.out.println("Population size : "+popSize + " ("+feasiblePop+"/"+(popSize-feasiblePop)+")");
//            System.out.println("Archive size    : "+archiveSize + " ("+feasibleArch+"/"+(archiveSize-feasibleArch)+")");
//        }

        // The InformationRetrieval will choose from the archive and the current population
        // the population from which in the next generation the parents will be selected.
        this.informationRetrieval.retrieveInformationFrom(this.optimizer.getPopulation());

//        double[][] mayday = this.showMay(this.optimizer.getPopulation());
//        if ((mayday[0][0] > may[0][0]) || (mayday[1][1] > may[1][1])) {
//            System.out.println("Losing the edges:");
//            System.out.println("Before : (" +may[0][0]+"/"+may[0][1]+") and ("+may[1][0]+"/"+may[1][1]+")");
//            System.out.println("After  : (" +mayday[0][0]+"/"+mayday[0][1]+") and ("+mayday[1][0]+"/"+mayday[1][1]+")");
//        }

        System.gc();

        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    private double[][] showMay(Population pop) {
        Population tmp = new Population();
        tmp.addPopulation(pop);
        if (pop.getArchive() != null) {
            tmp.addPopulation(pop.getArchive());
        }

        double[][] fitness = new double[tmp.size()][];
        for (int i = 0; i < tmp.size(); i++) {
            fitness[i] = ((AbstractEAIndividual) tmp.get(i)).getFitness();
        }
        double[] minY, maxY;
        minY = fitness[0];
        maxY = fitness[0];
        for (int i = 1; i < fitness.length; i++) {
            if (minY[0] > fitness[i][0]) {
                minY = fitness[i];
            }
            if (maxY[1] > fitness[i][1]) {
                maxY = fitness[i];
            }
        }
        double[][] result = new double[2][];
        result[0] = minY;
        result[1] = maxY;
        //System.out.println("Borders: ("+ (Math.round((100*minY[0]))/100.0)+"/"+ (Math.round((100*minY[1]))/100.0)+") ("+ (Math.round((100*maxY[0]))/100.0)+"/"+ (Math.round((100*maxY[1]))/100.0)+")");
        return result;
    }

    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.populationChangedEventListener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (populationChangedEventListener == ea) {
            populationChangedEventListener = null;
            return true;
        } else {
            return false;
        }
    }

    protected void firePropertyChangedEvent(String name) {
        if (this.populationChangedEventListener != null) {
            this.populationChangedEventListener.registerPopulationStateChanged(this, name);
        }
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.optimizationProblem = problem;
        this.optimizer.setProblem(problem);
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.optimizationProblem;
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "Multi-Objective Evolutionary Algorithm:\n";
        result += "Using:\n";
        result += " Archiving Strategy    = " + this.archiver.getClass().toString() + "\n";
        result += " Information Retrieval = " + this.informationRetrieval.getClass().toString() + "\n";
        result += " Information Retrieval = " + this.getClass().toString() + "\n";
        result += " Optimizer             = " + this.optimizer.getClass().toString() + "\n";
        result += this.optimizer.getStringRepresentation() + "\n";
        //result += "=> The Optimization Problem: ";
        //result += this.problem.getStringRepresentationForProblem(this) +"\n";
        //result += this.population.getStringRepresentation();
        return result;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MOEA";
    }

    /**
     * Assuming that all optimizer will store thier data in a population we will
     * allow acess to this population to query to current state of the
     * optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.optimizer.getPopulation();
    }

    @Override
    public void setPopulation(Population pop) {
        this.optimizer.setPopulation(pop);
    }

    public String populationTipText() {
        return "Edit the properties of the Population used.";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation(), ArchivingNSGAII.getNonDominatedSortedFront(getPopulation().getArchive()).getSortedPop(new EAIndividualComparator(0)));
    }

    /**
     * This method allows you to set/get the optimizing technique to use.
     *
     * @return The current optimizing method
     */
    public InterfaceOptimizer getOptimizer() {
        return this.optimizer;
    }

    public void setOptimizer(InterfaceOptimizer b) {
        this.optimizer = b;
    }

    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /**
     * This method allows you to set/get the archiving strategy to use.
     *
     * @return The current optimizing method
     */
    public InterfaceArchiving getArchivingStrategy() {
        return this.archiver;
    }

    public void setArchivingStrategy(InterfaceArchiving b) {
        this.archiver = b;
    }

    public String archivingStrategyTipText() {
        return "Choose the archiving strategy.";
    }

    /**
     * This method allows you to set/get the Information Retrieval strategy to
     * use.
     *
     * @return The current optimizing method
     */
    public InterfaceInformationRetrieval getInformationRetrieval() {
        return this.informationRetrieval;
    }

    public void setInformationRetrieval(InterfaceInformationRetrieval b) {
        this.informationRetrieval = b;
    }

    public String informationRetrievalTipText() {
        return "Choose the Information Retrieval strategy.";
    }

    /**
     * This method allows you to set/get the size of the archive.
     *
     * @return The current optimizing method
     */
    public int getArchiveSize() {
        Population archive = this.optimizer.getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            this.optimizer.getPopulation().SetArchive(archive);
        }
        return archive.getTargetSize();
    }

    public void setArchiveSize(int b) {
        Population archive = this.optimizer.getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            this.optimizer.getPopulation().SetArchive(archive);
        }
        archive.setTargetSize(b);
    }

    public String archiveSizeTipText() {
        return "Choose the size of the archive.";
    }
}
