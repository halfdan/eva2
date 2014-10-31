package eva2.optimization.strategies;

import eva2.gui.plot.Plot;
import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.migration.*;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.F1Problem;
import eva2.problems.F8Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.problems.TF1Problem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Hidden;

/**
 * The one and only island model for parallelization. Since parallelization
 * based on the RMIProxyRemoteThread is on the one hand much slower than
 * benchmark function evaluation and on the other hand the GUI based
 * distribution scheme is rather prone to config errors (the correct ssh version
 * is required, the jar needs to be in the working dir and possible problem data
 * must be on the servers to) an implicit island-model has been implemented too
 * to allow fast and reliable computation. This is still usefull, since it is
 * less prone to premature convergence and also an heterogenuous island model
 * can be used.
 * <p/>
 * A population of the same size is sent to all nodes and evaluated there
 * independently for a cycle (more precisely: for MigrationRate generations)
 * after which a communication step is performed according to the migration
 * model. Only after migration is a main cycle complete, the statistics updated
 * etc.
 */
@Description("This is an island model EA distributing the individuals across several (remote) CPUs for optimization.")
public class IslandModelEA implements InterfacePopulationChangedEventListener, InterfaceOptimizer, java.io.Serializable {

    private Population population = new Population();
    private InterfaceOptimizer optimizer = new GeneticAlgorithm();
    private InterfaceMigration migration = new SOBestMigration();
    private InterfaceOptimizationProblem optimizationProblem = new F8Problem();
    private int migrationRate = 10;
    private boolean heterogeneousProblems = false;
    // These are the processor to run on
    private int numLocalCPUs = 1;
    private boolean numLocalOnly = false;
    transient private InterfaceOptimizer[] islands;
    // This is for debugging
    private boolean logLocalChanges = true;
    private boolean show = false;
    transient private Plot plot = null;
    transient private String identifier = "";
    transient private InterfacePopulationChangedEventListener populationChangedEventListener;

    public IslandModelEA() {
    }

    public IslandModelEA(IslandModelEA a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.optimizer = (InterfaceOptimizer) a.optimizer.clone();
        this.migration = (InterfaceMigration) a.migration.clone();
        this.migrationRate = a.migrationRate;
        this.heterogeneousProblems = a.heterogeneousProblems;
        this.numLocalCPUs = a.numLocalCPUs;
        this.numLocalOnly = a.numLocalOnly;
    }

    @Override
    public Object clone() {
        return new IslandModelEA(this);
    }

    @Override
    public void initialize() {
        if (this.show) {
            if (this.plot == null) {
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                this.plot = new Plot("Island Model EA", "FitnessCalls", "Fitness", tmpD, tmpD);
            }
        }

//        this.population = new Population();
        this.population.clear();
        this.population.init();
        this.optimizer.initialize();
        this.optimizer.setProblem(this.optimizationProblem);
        this.optimizer.setPopulation((Population) population.clone());
        InterfacePopulationChangedEventListener myLocal = null;
        if (this.numLocalOnly) {
            // this is running on the local machine
            this.islands = new InterfaceOptimizer[this.numLocalCPUs];
            for (int i = 0; i < this.numLocalCPUs; i++) {
                this.islands[i] = (InterfaceOptimizer) this.optimizer.clone();
                //this.islands[i].setIdentifier("" + i);
                this.islands[i].initialize();
                if (this.logLocalChanges) {
                    this.islands[i].addPopulationChangedEventListener(this);
                }
            }
        } else {
            // this is running on remote machines
            // ToDo: Parallelize with Threads?!?
            /*
             if ((nodesList == null) || (nodesList.length == 0)) {
             throw new RuntimeException("Error, no active remote servers available! Activate servers or use localOnly mode.");
             }
             this.islands = new InterfaceOptimizer[nodesList.length];
             for (int i = 0; i < nodesList.length; i++) {
             this.islands[i] = (InterfaceOptimizer) RMIProxyRemoteThread.newInstance(this.optimizer, nodesList[i]);
             this.islands[i].setIdentifier(""+i);
             this.islands[i].initialize();
             if (this.logLocalChanges) {
             this.islands[i].addPopulationChangedEventListener(myLocal);
             }
             }*/
        }

        this.migration.initializeMigration(this.islands);
        Population pop;
        this.population.incrGeneration(); // the island-initialization has increased the island-pop generations.

        for (int i = 0; i < this.islands.length; i++) {
            pop = (Population) this.islands[i].getPopulation().clone();
            this.population.addPopulation(pop);
            this.population.incrFunctionCallsBy(pop.getFunctionCalls());
            if (islands[i].getPopulation().getGeneration() != population.getGeneration()) {
                System.err.println("Error, inconsistent generations!");
            }
        }
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED, this.optimizer.getPopulation());
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population tpop, boolean reset) {
        // TODO this is again evil copy&paste style
        if (this.show) {
            if (this.plot == null) {
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                this.plot = new Plot("Island Model EA", "FitnessCalls", "Fitness", tmpD, tmpD);
            }
        }

        this.population = (Population) tpop.clone();
        if (reset) {
            this.population.init();
            this.population.incrGeneration();
        }
        this.optimizer.initialize();
        this.optimizer.setProblem(this.optimizationProblem);
        InterfacePopulationChangedEventListener myLocal = null;
        if (this.numLocalOnly) {
            // this is running on the local machine
            this.islands = new InterfaceOptimizer[this.numLocalCPUs];
            for (int i = 0; i < this.numLocalCPUs; i++) {
                this.islands[i] = (InterfaceOptimizer) this.optimizer.clone();
                //this.islands[i].setIdentifier("" + i);
                this.islands[i].initialize();
                if (this.logLocalChanges) {
                    this.islands[i].addPopulationChangedEventListener(this);
                }
            }
        } else {
            // this is running on remote machines
            // ToDo: Parallellize with threads?!?
            /*

             this.islands = new InterfaceOptimizer[nodesList.length];
             for (int i = 0; i < nodesList.length; i++) {
             this.islands[i] = (InterfaceOptimizer) RMIProxyRemoteThread.newInstance(this.optimizer, nodesList[i]);
             this.islands[i].setIdentifier(""+i);
             this.islands[i].initialize();
             if (this.logLocalChanges) {
             this.islands[i].addPopulationChangedEventListener(myLocal);
             }
             }*/
        }

        this.migration.initializeMigration(this.islands);
        Population pop;
        for (int i = 0; i < this.islands.length; i++) {
            pop = (Population) this.islands[i].getPopulation().clone();
            this.population.addPopulation(pop);
            this.population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED, this.optimizer.getPopulation());
    }

    /**
     * The optimize method will compute an 'improved' and evaluated population
     */
    @Override
    public void optimize() {
        for (int i = 0; i < this.islands.length; i++) {
            if (this.islands[i].getPopulation().size() > 0) {
                this.islands[i].optimize();
            } else {
                this.islands[i].getPopulation().incrGeneration();
            }
        }
        this.population.incrGeneration();
        if ((this.population.getGeneration() % this.migrationRate) == 0) {
            this.communicate();
        }
        // this is necessary for heterogeneous islands
        if (this.heterogeneousProblems) {
            for (int i = 0; i < this.islands.length; i++) {
                this.islands[i].getProblem().evaluate(this.islands[i].getPopulation());
            }
        }
        System.gc();
    }

    /**
     * This method will manage communication between the islands
     */
    private void communicate() {
        // Here i'll have to wait until all islands are finished
        boolean allReachedG = false;
        int G = this.population.getGeneration();
        while (!allReachedG) {
            allReachedG = true;
            String gen = "[";
            for (int i = 0; i < this.islands.length; i++) {
                gen += this.islands[i].getPopulation().getGeneration() + "; ";
                if (this.islands[i].getPopulation().getGeneration() != G) {
                    allReachedG = false;
                }
            }
            if (!allReachedG) {
                System.out.println("Waiting...." + gen + "] ?= " + G);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error in sleep of XThread");
                }
            }
        }
        this.population.clear();
        this.population.setFunctionCalls(0);
        Population pop;
        for (int i = 0; i < this.islands.length; i++) {
            pop = (Population) this.islands[i].getPopulation().clone();
            this.population.addPopulation(pop);
            this.population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
//        System.out.println("Fitnesscalls :" + this.population.getFunctionCalls());
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED, this.optimizer.getPopulation());
        double plotValue = (this.optimizationProblem.getDoublePlotValue(this.population)).doubleValue();
        if (this.show) {
            this.plot.setConnectedPoint(this.population.getFunctionCalls(), plotValue, 0);
        }
        // now they are synchronized
        this.migration.migrate(this.islands);
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
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

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name, Population population) {
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
    @Hidden
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
        result += "Island Model Evolutionary Algorithm:\n";
        result += "Using:\n";
        result += " Migration Strategy    = " + this.migration.getClass().toString() + "\n";
        result += " Migration rate        = " + this.migrationRate + "\n";
        result += " Local only       = " + this.numLocalOnly + "\n";
        result += " Het. Problems         = " + this.heterogeneousProblems + "\n";
        if (this.heterogeneousProblems) {
            result += " Heterogenuous Optimizers: \n";
            for (int i = 0; i < this.islands.length; i++) {
                result += this.islands[i].getStringRepresentation() + "\n";
            }
        } else {
            result += " Homogeneous Optimizer = " + this.optimizer.getClass().toString() + "\n";
            result += this.optimizer.getStringRepresentation() + "\n";
        }
        //result += "=> The Optimization Problem: ";
        //result += this.problem.getStringRepresentationForProblem(this) +"\n";
        //result += this.population.getStringRepresentation();
        return result;
    }

    /**
     * This method is to test the parallelization scheme
     *
     * @param args
     */
    public static void main(String[] args) {
        // @todo die ServerStarter muss ich noch hin kriegen
        // @todo Wichtig ich brauche den eva2.tools.jproxy.RMIServer!
        IslandModelEA imea = new IslandModelEA();
        imea.show = true;
        imea.numLocalOnly = false;
        if (false) {
            imea.optimizer = new MultiObjectiveEA();
            ((MultiObjectiveEA) imea.optimizer).setArchiveSize(25);
            imea.optimizer.getPopulation().setTargetSize(50);
            imea.optimizationProblem = new TF1Problem();
            ((TF1Problem) imea.optimizationProblem).setEAIndividual(new ESIndividualDoubleData());
//            ((TF1Problem)imea.problem).setEAIndividual(new ESIndividualDoubleData());
//            imea.problem      = new TFPortfolioSelectionProblem();
//            ((TFPortfolioSelectionProblem)imea.problem).setEAIndividual(new ESIndividualDoubleData());
            if (false) {
                MOClusteringSeparation c = new MOClusteringSeparation();
                c.getKMeans().setUseSearchSpace(false);
                c.setUseConstraints(true);
                c.debug = true;
                imea.migration = c;
            }
            if (false) {
                MOConeSeparation c = new MOConeSeparation();
                c.setUseConstraints(true);
                c.debug = true;
                imea.migration = c;
            }
            if (true) {
                imea.migration = new MOBestMigration();
            }
        } else {
            imea.optimizationProblem = new F8Problem();
            ((F1Problem) imea.optimizationProblem).setEAIndividual(new ESIndividualDoubleData());
        }
        imea.migrationRate = 15;
        imea.initialize();
        while (imea.getPopulation().getFunctionCalls() < 25000) {
            imea.optimize();
        }
        System.out.println("System.exit(0);");
        //System.exit(0);
    }

    /**
     * This method will return the Optimizers
     *
     * @return An array of optimizers
     */
    public InterfaceOptimizer[] getOptimizers() {
        return this.islands;
    }

    /**
     * This method will allow you to toggle between homogeneous and
     * heterogeneous problems. In case of heterogeneous problems the individuals
     * need to be reevaluated after migration.
     *
     * @param t
     */
    public void setHeterogeneousProblems(boolean t) {
        this.heterogeneousProblems = t;
    }

    /**
     * ********************************************************************************************************************
     * These are for InterfacePopulationChangedEventListener
     */
    /**
     * This method allows an optimizer to register a change in the EA-lecture
     *
     * @param source The source of the event.
     * @param name   Could be used to indicate the nature of the event.
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        InterfaceOptimizer opt = (InterfaceOptimizer) source;
        int sourceID = 12; //Integer.parseInt(opt.getIdentifier());
        double cFCOpt = opt.getPopulation().getFunctionCalls();
        double plotValue = this.optimizationProblem.getDoublePlotValue(opt.getPopulation());

        if (this.show) {
            this.plot.setConnectedPoint(cFCOpt, plotValue, (sourceID + 1));
        }
    }


    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "IslandEA";
    }

    /**
     * This method allows you to toggle between a truly parallel and a serial
     * implementation.
     *
     * @return The current optimization mode
     */
    // TODO Deactivated from GUI because the current implementation does not really parallelize on a multicore. 
    // Instead, the new direct problem parallelization can be used.
//    public boolean isLocalOnly() {
//        return this.numLocalOnly;
//    }
    public void setLocalOnly(boolean b) {
        this.numLocalOnly = b;
    }

    public String localOnlyTipText() {
        return "Toggle between usage of local CPUs and remote servers.";
    }

    /**
     * This will show the local performance
     *
     * @return The current optimzation mode
     */
    public boolean getShow() {
        return this.show;
    }

    public void setShow(boolean b) {
        this.show = b;
        this.logLocalChanges = b;
    }

    public String showTipText() {
        return "This will show the local performance.";
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
     * This method allows you to set/get the migration strategy to use.
     *
     * @return The current migration strategy
     */
    public InterfaceMigration getMigrationStrategy() {
        return this.migration;
    }

    public void setMigrationStrategy(InterfaceMigration b) {
        this.migration = b;
    }

    public String migrationStrategyTipText() {
        return "Choose a migration strategy to use.";
    }

    /**
     * This method allows you to set/get the migration rate to use.
     *
     * @return The current migration rate
     */
    public int getMigrationRate() {
        return this.migrationRate;
    }

    public void setMigrationRate(int b) {
        this.migrationRate = b;
    }

    public String migrationRateTipText() {
        return "Set the migration rate for communication between islands.";
    }

    public String serversTipText() {
        return "Choose and manage the servers (only active in parallelized mode).";
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
        return this.population;
    }

    @Override
    public void setPopulation(Population pop) {
        // @todo Jetzt m�sste ich die pop auch auf die Rechner verteilen...
        this.population = pop;
    }

    public String populationTipText() {
        return "(Defunct)";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * This method allows you to set the number of processors in local mode
     *
     * @param n Number of processors.
     */
    public void setNumberLocalCPUs(int n) {
        if (n >= 1) {
            this.numLocalCPUs = n;
        } else {
            System.err.println("Number of CPUs must be at least 1!");
        }
    }

    public String numberLocalCPUsTipText() {
        return "Set the number of local CPUS (>=1, only used in local mode).";
    }
}