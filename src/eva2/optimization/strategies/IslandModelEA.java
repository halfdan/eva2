package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.gui.plot.Plot;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.migration.InterfaceMigration;
import eva2.optimization.operator.migration.MOBestMigration;
import eva2.optimization.operator.migration.MOClusteringSeparation;
import eva2.optimization.operator.migration.MOConeSeparation;
import eva2.optimization.operator.migration.SOBestMigration;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.problems.F8Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.problems.TF1Problem;

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
 *
 * A population of the same size is sent to all nodes and evaluated there
 * independently for a cycle (more precisely: for MigrationRate generations)
 * after which a communication step is performed according to the migration
 * model. Only after migration is a main cycle complete, the statistics updated
 * etc.
 *
 * Created by IntelliJ IDEA. User: streiche Date: 12.09.2004 Time: 14:48:20 To
 * change this template use File | Settings | File Templates.
 */
public class IslandModelEA implements InterfacePopulationChangedEventListener, InterfaceOptimizer, java.io.Serializable {

    private Population m_Population = new Population();
    private InterfaceOptimizer m_Optimizer = new GeneticAlgorithm();
    private InterfaceMigration m_Migration = new SOBestMigration();
    private InterfaceOptimizationProblem m_Problem = new F8Problem();
//    private String[]                                m_NodesList;
    private int m_MigrationRate = 10;
    private boolean m_HeterogenuousProblems = false;
    // These are the processor to run on
    private int m_numLocalCPUs = 1;
    private boolean m_localOnly = false;
    transient private InterfaceOptimizer[] m_Islands;
    // This is for debugging
    private boolean m_LogLocalChanges = true;
    private boolean m_Show = false;
    transient private Plot m_Plot = null;
    transient private String m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;
    transient private final boolean TRACE = false;

    public IslandModelEA() {
    }

    public IslandModelEA(IslandModelEA a) {
        this.m_Population = (Population) a.m_Population.clone();
        this.m_Problem = (InterfaceOptimizationProblem) a.m_Problem.clone();
        this.m_Optimizer = (InterfaceOptimizer) a.m_Optimizer.clone();
        this.m_Migration = (InterfaceMigration) a.m_Migration.clone();
        this.m_MigrationRate = a.m_MigrationRate;
        this.m_HeterogenuousProblems = a.m_HeterogenuousProblems;
        this.m_numLocalCPUs = a.m_numLocalCPUs;
        this.m_localOnly = a.m_localOnly;
    }

    @Override
    public Object clone() {
        return (Object) new IslandModelEA(this);
    }

    @Override
    public void init() {
        if (this.m_Show) {
            if (this.m_Plot == null) {
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                this.m_Plot = new Plot("Island Model EA", "FitnessCalls", "Fitness", tmpD, tmpD);
            }
        }

//        this.m_Population = new Population();
        this.m_Population.clear();
        this.m_Population.init();
        this.m_Optimizer.init();
        this.m_Optimizer.setProblem(this.m_Problem);
        this.m_Optimizer.setPopulation((Population) m_Population.clone());
        InterfacePopulationChangedEventListener myLocal = null;
        if (this.m_localOnly) {
            // this is running on the local machine
            this.m_Islands = new InterfaceOptimizer[this.m_numLocalCPUs];
            for (int i = 0; i < this.m_numLocalCPUs; i++) {
                this.m_Islands[i] = (InterfaceOptimizer) this.m_Optimizer.clone();
                this.m_Islands[i].setIdentifier("" + i);
                this.m_Islands[i].init();
                if (this.m_LogLocalChanges) {
                    this.m_Islands[i].addPopulationChangedEventListener(this);
                }
            }
        } else {
            // this is running on remote machines
            // ToDo: Parallelize with Threads?!?
            /*if (this.m_LocalServer == null) {
             this.m_LocalServer = RMIServer.getInstance();
             }
             try {
             myLocal = (InterfacePopulationChangedEventListener) RMIProxyLocal.newInstance(this);
             } catch(Exception e) {
             System.err.println("Island Model EA warning on local RMIServer... but i'll start anyway!");
             }
             String[] nodesList = this.m_Servers.getCheckedServerNodes();
             if ((nodesList == null) || (nodesList.length == 0)) {
             throw new RuntimeException("Error, no active remote servers available! Activate servers or use localOnly mode.");
             }
             this.m_Islands = new InterfaceOptimizer[nodesList.length];
             for (int i = 0; i < nodesList.length; i++) {
             this.m_Islands[i] = (InterfaceOptimizer) RMIProxyRemoteThread.newInstance(this.optimizer, nodesList[i]);
             this.m_Islands[i].setIdentifier(""+i);
             this.m_Islands[i].init();
             if (this.m_LogLocalChanges) {
             this.m_Islands[i].addPopulationChangedEventListener(myLocal);
             }
             }*/
        }

        this.m_Migration.initMigration(this.m_Islands);
        Population pop;
        this.m_Population.incrGeneration(); // the island-initialization has increased the island-pop generations. 

        for (int i = 0; i < this.m_Islands.length; i++) {
            pop = (Population) this.m_Islands[i].getPopulation().clone();
            this.m_Population.addPopulation(pop);
            this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
            if (m_Islands[i].getPopulation().getGeneration() != m_Population.getGeneration()) {
                System.err.println("Error, inconsistent generations!");
            }
        }
        this.firePropertyChangedEvent(Population.nextGenerationPerformed, this.m_Optimizer.getPopulation());
    }

    /**
     * This method will init the optimizer with a given population
     *
     * @param reset If true the population is reset.
     */
    @Override
    public void initByPopulation(Population tpop, boolean reset) {
        // TODO this is again evil copy&paste style
        if (this.m_Show) {
            if (this.m_Plot == null) {
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                this.m_Plot = new Plot("Island Model EA", "FitnessCalls", "Fitness", tmpD, tmpD);
            }
        }

        this.m_Population = (Population) tpop.clone();
        if (reset) {
            this.m_Population.init();
            this.m_Population.incrGeneration();
        }
        this.m_Optimizer.init();
        this.m_Optimizer.setProblem(this.m_Problem);
        InterfacePopulationChangedEventListener myLocal = null;
        if (this.m_localOnly) {
            // this is running on the local machine
            this.m_Islands = new InterfaceOptimizer[this.m_numLocalCPUs];
            for (int i = 0; i < this.m_numLocalCPUs; i++) {
                this.m_Islands[i] = (InterfaceOptimizer) this.m_Optimizer.clone();
                this.m_Islands[i].setIdentifier("" + i);
                this.m_Islands[i].init();
                if (this.m_LogLocalChanges) {
                    this.m_Islands[i].addPopulationChangedEventListener(this);
                }
            }
        } else {
            // this is running on remote machines
            // ToDo: Parallellize with threads?!?
            /*
             if (this.m_LocalServer == null) {
             this.m_LocalServer = RMIServer.getInstance();
             }
             try {
             myLocal = (InterfacePopulationChangedEventListener) RMIProxyLocal.newInstance(this);
             } catch(Exception e) {
             System.err.println("Island Model EA warning on local RMIServer... but i'll try to start anyway!");
             }
             String[] nodesList = this.m_Servers.getCheckedServerNodes();
             if ((nodesList == null) || (nodesList.length == 0)) {
             return;
             }
             this.m_Islands = new InterfaceOptimizer[nodesList.length];
             for (int i = 0; i < nodesList.length; i++) {
             this.m_Islands[i] = (InterfaceOptimizer) RMIProxyRemoteThread.newInstance(this.optimizer, nodesList[i]);
             this.m_Islands[i].setIdentifier(""+i);
             this.m_Islands[i].init();
             if (this.m_LogLocalChanges) {
             this.m_Islands[i].addPopulationChangedEventListener(myLocal);
             }
             }*/
        }

        this.m_Migration.initMigration(this.m_Islands);
        Population pop;
        for (int i = 0; i < this.m_Islands.length; i++) {
            pop = (Population) this.m_Islands[i].getPopulation().clone();
            this.m_Population.addPopulation(pop);
            this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
        this.firePropertyChangedEvent(Population.nextGenerationPerformed, this.m_Optimizer.getPopulation());
    }

    /**
     * The optimize method will compute an 'improved' and evaluated population
     */
    @Override
    public void optimize() {
        for (int i = 0; i < this.m_Islands.length; i++) {
            if (this.m_Islands[i].getPopulation().size() > 0) {
                this.m_Islands[i].optimize();
                if (TRACE) {
                    System.out.println(BeanInspector.toString(m_Islands[i].getPopulation()));
                }
            } else {
                this.m_Islands[i].getPopulation().incrGeneration();
            }
            if (TRACE) {
                System.out.println("----");
            }
        }
        this.m_Population.incrGeneration();
        if ((this.m_Population.getGeneration() % this.m_MigrationRate) == 0) {
            this.communicate();
        }
        // this is necessary for heterogeneous islands
        if (this.m_HeterogenuousProblems) {
            for (int i = 0; i < this.m_Islands.length; i++) {
                this.m_Islands[i].getProblem().evaluate(this.m_Islands[i].getPopulation());
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
        int G = this.m_Population.getGeneration();
        while (!allReachedG) {
            allReachedG = true;
            String gen = "[";
            for (int i = 0; i < this.m_Islands.length; i++) {
                gen += this.m_Islands[i].getPopulation().getGeneration() + "; ";
                if (this.m_Islands[i].getPopulation().getGeneration() != G) {
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
        this.m_Population.clear();
        this.m_Population.setFunctionCalls(0);
        Population pop;
        for (int i = 0; i < this.m_Islands.length; i++) {
            pop = (Population) this.m_Islands[i].getPopulation().clone();
            this.m_Population.addPopulation(pop);
            this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
//        System.out.println("Fitnesscalls :" + this.m_Population.getFunctionCalls());
        this.firePropertyChangedEvent(Population.nextGenerationPerformed, this.m_Optimizer.getPopulation());
        double plotValue = (this.m_Problem.getDoublePlotValue(this.m_Population)).doubleValue();
        if (this.m_Show) {
            this.m_Plot.setConnectedPoint(this.m_Population.getFunctionCalls(), plotValue, 0);
        }
        // now they are synchronized
        this.m_Migration.migrate(this.m_Islands);
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (m_Listener == ea) {
            m_Listener = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name, Population population) {
        if (this.m_Listener != null) {
            this.m_Listener.registerPopulationStateChanged(this, name);
        }
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
        this.m_Optimizer.setProblem(problem);
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.m_Problem;
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
        result += " Migration Strategy    = " + this.m_Migration.getClass().toString() + "\n";
        result += " Migration rate        = " + this.m_MigrationRate + "\n";
        result += " Local only       = " + this.m_localOnly + "\n";
        result += " Het. Problems         = " + this.m_HeterogenuousProblems + "\n";
        if (this.m_HeterogenuousProblems) {
            result += " Heterogenuous Optimizers: \n";
            for (int i = 0; i < this.m_Islands.length; i++) {
                result += this.m_Islands[i].getStringRepresentation() + "\n";
            }
        } else {
            result += " Homogeneous Optimizer = " + this.m_Optimizer.getClass().toString() + "\n";
            result += this.m_Optimizer.getStringRepresentation() + "\n";
        }
        //result += "=> The Optimization Problem: ";
        //result += this.problem.getStringRepresentationForProblem(this) +"\n";
        //result += this.m_Population.getStringRepresentation();
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
        imea.m_Show = true;
        imea.m_localOnly = false;
        if (false) {
            imea.m_Optimizer = new MultiObjectiveEA();
            ((MultiObjectiveEA) imea.m_Optimizer).setArchiveSize(25);
            ((MultiObjectiveEA) imea.m_Optimizer).getPopulation().setTargetSize(50);
            imea.m_Problem = new TF1Problem();
            ((TF1Problem) imea.m_Problem).setEAIndividual(new ESIndividualDoubleData());
//            ((TF1Problem)imea.problem).setEAIndividual(new ESIndividualDoubleData());
//            imea.problem      = new TFPortfolioSelectionProblem();
//            ((TFPortfolioSelectionProblem)imea.problem).setEAIndividual(new ESIndividualDoubleData());
            if (false) {
                MOClusteringSeparation c = new MOClusteringSeparation();
                c.getKMeans().setUseSearchSpace(false);
                c.setUseConstraints(true);
                c.m_Debug = true;
                imea.m_Migration = c;
            }
            if (false) {
                MOConeSeparation c = new MOConeSeparation();
                c.setUseConstraints(true);
                c.m_Debug = true;
                imea.m_Migration = c;
            }
            if (true) {
                imea.m_Migration = new MOBestMigration();
            }
        } else {
            imea.m_Problem = new F8Problem();
            ((F1Problem) imea.m_Problem).setEAIndividual(new ESIndividualDoubleData());
        }
        imea.m_MigrationRate = 15;
        imea.init();
        while (imea.getPopulation().getFunctionCalls() < 25000) {
            imea.optimize();
        }
        System.out.println("System.exit(0);");
        //System.exit(0);
    }

    /**
     * This method allows you to set an identifier for the algorithm
     *
     * @param name The indenifier
     */
    @Override
    public void setIdentifier(String name) {
        this.m_Identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.m_Identifier;
    }

    /**
     * This method will return the Optimizers
     *
     * @return An array of optimizers
     */
    public InterfaceOptimizer[] getOptimizers() {
        return this.m_Islands;
    }

    /**
     * This method will allow you to toggel between homogenuous and
     * heterogenuous problems. In case of heterogenuous problems the individuals
     * need to be reevaluated after migration.
     *
     * @param t
     */
    public void setHeterogenuousProblems(boolean t) {
        this.m_HeterogenuousProblems = t;
    }

    /**
     * ********************************************************************************************************************
     * These are for InterfacePopulationChangedEventListener
     */
    /**
     * This method allows an optimizer to register a change in the EA-lecture
     *
     * @param source The source of the event.
     * @param name Could be used to indicate the nature of the event.
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        InterfaceOptimizer opt = (InterfaceOptimizer) source;
        int sourceID = new Integer(opt.getIdentifier()).intValue();
        double cFCOpt = opt.getPopulation().getFunctionCalls();
        double plotValue = (this.m_Problem.getDoublePlotValue(opt.getPopulation())).doubleValue();

        if (this.m_Show) {
            this.m_Plot.setConnectedPoint(cFCOpt, plotValue, (sourceID + 1));
        }

        //System.out.println("Someone has finished, ("+this.m_Generation+"/"+this.m_Performed+")");
        //System.out.println(sourceID + " is at generation "+ opt.getPopulation().getGeneration() +" i'm at " +this.m_Generation);
    }

    /**
     * ********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is an island model EA distributing the individuals across several (remote) CPUs for optimization.";
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
//        return this.m_localOnly;
//    }
    public void setLocalOnly(boolean b) {
        this.m_localOnly = b;
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
        return this.m_Show;
    }

    public void setShow(boolean b) {
        this.m_Show = b;
        this.m_LogLocalChanges = b;
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
        return this.m_Optimizer;
    }

    public void setOptimizer(InterfaceOptimizer b) {
        this.m_Optimizer = b;
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
        return this.m_Migration;
    }

    public void setMigrationStrategy(InterfaceMigration b) {
        this.m_Migration = b;
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
        return this.m_MigrationRate;
    }

    public void setMigrationRate(int b) {
        this.m_MigrationRate = b;
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
        return this.m_Population;
    }

    @Override
    public void setPopulation(Population pop) {
        // @todo Jetzt m�sste ich die pop auch auf die Rechner verteilen...
        this.m_Population = pop;
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
            this.m_numLocalCPUs = n;
        } else {
            System.err.println("Number of CPUs must be at least 1!");
        }
    }
    // TODO Deactivated from GUI because the current implementation does not really parallelize on a multicore. 
    // Instead, the new direct problem parallelization can be used.
//    public int getNumberLocalCPUs() {
//        return this.m_LocalCPUs;
//    }

    public String numberLocalCPUsTipText() {
        return "Set the number of local CPUS (>=1, only used in local mode).";
    }
}