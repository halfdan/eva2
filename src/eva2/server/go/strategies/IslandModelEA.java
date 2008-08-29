package eva2.server.go.strategies;

import eva2.gui.Plot;
import eva2.gui.PropertyRemoteServers;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.operators.migration.InterfaceMigration;
import eva2.server.go.operators.migration.MOBestMigration;
import eva2.server.go.operators.migration.MOClusteringSeparation;
import eva2.server.go.operators.migration.MOConeSeparation;
import eva2.server.go.operators.migration.SOBestMigration;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.F8Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.problems.TF1Problem;
import wsi.ra.jproxy.RMIProxyRemoteThread;
import wsi.ra.jproxy.RMIProxyLocal;
import wsi.ra.jproxy.RMIServer;

/** The one and only island model for parallelization. Since parallelization based
 * on the RMIProxyRemoteThread is on the one hand much slower than benchmark function
 * evaluation and on the other hand the GUI based distribution scheme is rather prone
 * to config errors (the correct ssh version is required, the jar needs to be in
 * the working dir and possible problem data must be on the servers to) an implicit
 * island-model has been implemented too to allow fast and reliabel computation.
 * This is still usefull, since it is less prone to premature convergence and also
 * an heterogenuous island model can be used.
 * 
 * A population of the same size is sent to all nodes and evaluated there independently 
 * for a cycle (more precisely: for MigrationRate generations) after which a communication
 * step is performed according to the migration model. Only after migration is a main
 * cycle complete, the statistics updated etc.
 *  
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 12.09.2004
 * Time: 14:48:20
 * To change this template use File | Settings | File Templates.
 */
public class IslandModelEA implements InterfacePopulationChangedEventListener, InterfaceOptimizer, java.io.Serializable {

    private Population                              m_Population        = new Population();
    private InterfaceOptimizer                      m_Optimizer         = new GeneticAlgorithm();
    private InterfaceMigration                      m_Migration         = new SOBestMigration();
    private InterfaceOptimizationProblem            m_Problem           = new F8Problem();
//    private String[]                                m_NodesList;
    private int                                     m_MigrationRate     = 10;
    private boolean                                 m_HeterogenuousProblems = false;
    private PropertyRemoteServers                   m_Servers           = new PropertyRemoteServers();

    // These are the processor to run on
    private int                                                 m_LocalCPUs         = 4;
    private boolean                                             m_Parallelize       = false;
    private InterfaceOptimizer[]                                m_Islands;
    transient private RMIServer                                 m_LocalServer       = null;

    // This is for debugging
    private boolean                                             m_LogLocalChanges   = true;
    private boolean                                             m_Show              = false;
    transient private Plot                                      m_Plot              = null;

    transient private String                                    m_Identifier        = "";
    transient private InterfacePopulationChangedEventListener   m_Listener;


    public IslandModelEA() {
    }

    public IslandModelEA(IslandModelEA a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Optimizer                    = (InterfaceOptimizer)a.m_Optimizer.clone();
        this.m_Migration                    = (InterfaceMigration)a.m_Migration.clone();
        this.m_Servers                      = (PropertyRemoteServers)a.m_Servers.clone();
        this.m_LocalCPUs                    = a.m_LocalCPUs;
        this.m_Parallelize                  = a.m_Parallelize;
    }

    public Object clone() {
        return (Object) new IslandModelEA(this);
    }

    public void init() {
        if (this.m_Show) {
            if (this.m_Plot == null) {
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                this.m_Plot = new eva2.gui.Plot("Island Model EA", "FitnessCalls", "Fitness", tmpD, tmpD);
            }
        }

        this.m_Population = new Population();
        this.m_Population.incrGeneration();
        this.m_Optimizer.init();
        this.m_Optimizer.SetProblem(this.m_Problem);
        InterfacePopulationChangedEventListener myLocal = null;
        if (this.m_Parallelize) {
            // this is running on remote machines
            if (this.m_LocalServer == null) this.m_LocalServer = RMIServer.getInstance();
            try {
                myLocal = (InterfacePopulationChangedEventListener) RMIProxyLocal.newInstance(this);
            } catch(Exception e) {
                System.out.println("Island Model EA warning on local RMIServer... but i'll start anyway!");
            }
            String[] nodesList = this.m_Servers.getCheckedServerNodes();
            if ((nodesList == null) || (nodesList.length == 0)) return;
            this.m_Islands = new InterfaceOptimizer[nodesList.length];
            for (int i = 0; i < nodesList.length; i++) {
                this.m_Islands[i] = (InterfaceOptimizer) RMIProxyRemoteThread.newInstance(this.m_Optimizer, nodesList[i]);
                this.m_Islands[i].SetIdentifier(""+i);
                this.m_Islands[i].init();
                if (this.m_LogLocalChanges)
                    this.m_Islands[i].addPopulationChangedEventListener(myLocal);
            }
        } else {
            // this is running on the local machine
            this.m_Islands = new InterfaceOptimizer[this.m_LocalCPUs];
            for (int i = 0; i < this.m_LocalCPUs; i++) {
                this.m_Islands[i] = (InterfaceOptimizer) this.m_Optimizer.clone();
                this.m_Islands[i].SetIdentifier(""+i);
                this.m_Islands[i].init();
                if (this.m_LogLocalChanges)
                    this.m_Islands[i].addPopulationChangedEventListener(this);
            }
        }

        this.m_Migration.initMigration(this.m_Islands);
        Population pop;
        for (int i = 0; i < this.m_Islands.length; i++) {
            pop = (Population)this.m_Islands[i].getPopulation().clone();
            this.m_Population.addPopulation(pop);
            this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
        this.firePropertyChangedEvent("NextGenerationPerformed", this.m_Optimizer.getPopulation());
    }

    /** This method will init the optimizer with a given population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population tpop, boolean reset) {
    	// TODO this is again evil copy&paste style
        if (this.m_Show) {
            if (this.m_Plot == null) {
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                this.m_Plot = new eva2.gui.Plot("Island Model EA", "FitnessCalls", "Fitness", tmpD, tmpD);
            }
        }

        this.m_Population = (Population)tpop.clone();
        if (reset) {
        	this.m_Population.init();
            this.m_Population.incrGeneration();
        }
        this.m_Optimizer.init();
        this.m_Optimizer.SetProblem(this.m_Problem);
        InterfacePopulationChangedEventListener myLocal = null;
        if (this.m_Parallelize) {
            // this is running on remote maschines
            if (this.m_LocalServer == null) this.m_LocalServer = RMIServer.getInstance();
            try {
                myLocal = (InterfacePopulationChangedEventListener) RMIProxyLocal.newInstance(this);
            } catch(Exception e) {
                System.out.println("Island Model EA warning on local RMIServer... but i'll start anyway!");
            }
            String[] nodesList = this.m_Servers.getCheckedServerNodes();
            if ((nodesList == null) || (nodesList.length == 0)) return;
            this.m_Islands = new InterfaceOptimizer[nodesList.length];
            for (int i = 0; i < nodesList.length; i++) {
                this.m_Islands[i] = (InterfaceOptimizer) RMIProxyRemoteThread.newInstance(this.m_Optimizer, nodesList[i]);
                this.m_Islands[i].SetIdentifier(""+i);
                this.m_Islands[i].init();
                if (this.m_LogLocalChanges)
                    this.m_Islands[i].addPopulationChangedEventListener(myLocal);
            }
        } else {
            // this is running on the local machine
            this.m_Islands = new InterfaceOptimizer[this.m_LocalCPUs];
            for (int i = 0; i < this.m_LocalCPUs; i++) {
                this.m_Islands[i] = (InterfaceOptimizer) this.m_Optimizer.clone();
                this.m_Islands[i].SetIdentifier(""+i);
                this.m_Islands[i].init();
                if (this.m_LogLocalChanges)
                    this.m_Islands[i].addPopulationChangedEventListener(this);
            }
        }

        this.m_Migration.initMigration(this.m_Islands);
        Population pop;
        for (int i = 0; i < this.m_Islands.length; i++) {
            pop = (Population)this.m_Islands[i].getPopulation().clone();
            this.m_Population.addPopulation(pop);
            this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
        this.firePropertyChangedEvent("NextGenerationPerformed", this.m_Optimizer.getPopulation());
    }

    /** The optimize method will compute an 'improved' and evaluated population
     */
    public void optimize() {
        for (int i = 0; i < this.m_Islands.length; i++) {
            if (this.m_Islands[i].getPopulation().size() > 0) this.m_Islands[i].optimize();
            else this.m_Islands[i].getPopulation().incrGeneration();
        }
        this.m_Population.incrGeneration();
        if ((this.m_Population.getGeneration() % this.m_MigrationRate) == 0) {
            this.communicate();
        }
        // this is necessary for heterogeneuous islands
        if (this.m_HeterogenuousProblems) {
            for (int i = 0; i < this.m_Islands.length; i++) {
                this.m_Islands[i].getProblem().evaluate(this.m_Islands[i].getPopulation());
            }
        }
        System.gc();
    }

    /** This method will manage comunication between the
     * islands
     */
    private void communicate() {
        // Here i'll have to wait until all islands are finished
        boolean allReachedG = false;
        int     G = this.m_Population.getGeneration();
        while (!allReachedG) {
            allReachedG = true;
            String gen = "[";
            for (int i = 0; i < this.m_Islands.length; i++) {
                gen += this.m_Islands[i].getPopulation().getGeneration()+"; ";
                if (this.m_Islands[i].getPopulation().getGeneration() != G) allReachedG = false;
            }
            if (!allReachedG) {
                System.out.println("Waiting...."+gen+"] ?= " + G);
                try {
                  Thread.sleep(1000);
                } catch (Exception e) {
                  System.out.println("Error in sleep of XThread");
                }
            }
        }
        this.m_Population.clear();
        this.m_Population.SetFunctionCalls(0);
        Population pop;
        for (int i = 0; i < this.m_Islands.length; i++) {
            pop = (Population)this.m_Islands[i].getPopulation().clone();
            this.m_Population.addPopulation(pop);
            this.m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
//        System.out.println("Fitnesscalls :" + this.m_Population.getFunctionCalls());
        this.firePropertyChangedEvent("NextGenerationPerformed", this.m_Optimizer.getPopulation());
        double plotValue = (this.m_Problem.getDoublePlotValue(this.m_Population)).doubleValue();
        if (this.m_Show) this.m_Plot.setConnectedPoint(this.m_Population.getFunctionCalls(), plotValue, 0);
        // now they are synchronized
        this.m_Migration.migrate(this.m_Islands);
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
    /** Something has changed
     */
    protected void firePropertyChangedEvent (String name, Population population) {     
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
        result += "Island Model Evolutionary Algorithm:\n";
        result += "Using:\n";
        result += " Migration Strategy    = " + this.m_Migration.getClass().toString() + "\n";
        result += " Migration rate        = " + this.m_MigrationRate + "\n";
        result += " Truely Parallel       = " + this.m_Parallelize + "\n";
        result += " Het. Problems         = " + this.m_HeterogenuousProblems + "\n";
        if (this.m_HeterogenuousProblems) {
            result += " Heterogenuous Optimizers: \n";
            for (int i = 0; i < this.m_Islands.length; i++) {
                result += this.m_Islands[i].getStringRepresentation() +"\n";
            }
        } else {
            result += " Homogeneous Optimizer = " + this.m_Optimizer.getClass().toString() + "\n";
            result += this.m_Optimizer.getStringRepresentation() +"\n";
        }
        //result += "=> The Optimization Problem: ";
        //result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        //result += this.m_Population.getStringRepresentation();
        return result;
    }

    /** This method is to test the parallelization scheme
     *
     * @param args
     */
    public static void main(String[] args) {
        // @todo die ServerStarter muss ich noch hin kriegen
        // @todo Wichtig ich brauche den wsi.ra.jproxy.RMIServer!
        PropertyRemoteServers s = new PropertyRemoteServers();
        s.addServerNode("raold1.informatik.uni-tuebingen.de", 2);
        s.addServerNode("raold2.informatik.uni-tuebingen.de", 2);
        s.addServerNode("raold3.informatik.uni-tuebingen.de", 2);
        s.setPassword("KozeOpL;");
        s.startServers();
        IslandModelEA imea  = new IslandModelEA();
        imea.m_Show = true;
        imea.m_Parallelize = true;
        imea.setServers(s);
        if (false) {
            imea.m_Optimizer    = new MultiObjectiveEA();
            ((MultiObjectiveEA)imea.m_Optimizer).setArchiveSize(25);
            ((MultiObjectiveEA)imea.m_Optimizer).getPopulation().setPopulationSize(50);
            imea.m_Problem      = new TF1Problem();
            ((TF1Problem)imea.m_Problem).setEAIndividual(new ESIndividualDoubleData());
//            ((TF1Problem)imea.m_Problem).setEAIndividual(new ESIndividualDoubleData());
//            imea.m_Problem      = new TFPortfolioSelectionProblem();
//            ((TFPortfolioSelectionProblem)imea.m_Problem).setEAIndividual(new ESIndividualDoubleData());
            if (false) {
                MOClusteringSeparation c = new MOClusteringSeparation();
                c.getKMeans().setUseSearchSpace(false);
                c.setUseConstraints(true);
                c.m_Debug = true;
                imea.m_Migration    = c;
            }
            if (false) {
                MOConeSeparation c = new MOConeSeparation();
                c.setUseConstraints(true);
                c.m_Debug = true;
                imea.m_Migration = c;
            }
            if (true) {
                imea.m_Migration    = new MOBestMigration();
            }
        } else {
            imea.m_Problem      = new F8Problem();
            ((F1Problem)imea.m_Problem).setEAIndividual(new ESIndividualDoubleData());
        }
        imea.m_MigrationRate = 15;
        imea.init();
        while (imea.getPopulation().getFunctionCalls() < 25000) {
            imea.optimize();
        }
        System.out.println("System.exit(0);");
        //System.exit(0);
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

    /** This method will return the Optimizers
     * @return An array of optimizers
     */
    public InterfaceOptimizer[] getOptimizers() {
        return this.m_Islands;
    }

    /** This method will allow you to toggel between homogenuous and heterogenuous problems.
     * In case of heterogenuous problems the individuals need to be reevaluated after migration.
     * @param t
     */
    public void setHeterogenuousProblems(boolean t) {
        this.m_HeterogenuousProblems = t;
    }

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    public void freeWilly() {
        for (int i = 0; i < this.m_Islands.length; i++) {
            this.m_Islands[i].freeWilly();
        }
    }

/**********************************************************************************************************************
 * These are for InterfacePopulationChangedEventListener
 */
    /** This method allows an optimizer to register a change in the EA-lecture
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name) {
        InterfaceOptimizer  opt = (InterfaceOptimizer)source;
        int                 sourceID = new Integer(opt.getIdentifier()).intValue();
        double              cFCOpt = opt.getPopulation().getFunctionCalls();
        double              plotValue = (this.m_Problem.getDoublePlotValue(opt.getPopulation())).doubleValue();

        if (this.m_Show) this.m_Plot.setConnectedPoint(cFCOpt, plotValue, (sourceID+1));

        //System.out.println("Someone has finished, ("+this.m_Generation+"/"+this.m_Performed+")");
        //System.out.println(sourceID + " is at generation "+ opt.getPopulation().getGeneration() +" i'm at " +this.m_Generation);
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
        return "Island EA";
    }

    /** This method allows you to toggle between a truly parallel
     * and a serial implementation.
     * @return The current optimzation mode
     */
    public boolean getParallelize() {
        return this.m_Parallelize;
    }
    public void setParallelize(boolean b){
        this.m_Parallelize = b;
    }
    public String parallelizeTipText() {
        return "Toggle between parallel and serial implementation.";
    }

    /** This will show the local performance
     * @return The current optimzation mode
     */
    public boolean getShow() {
        return this.m_Show;
    }
    public void setShow(boolean b){
        this.m_Show             = b;
        this.m_LogLocalChanges  = b;
    }
    public String showTipText() {
        return "This will show the local performance.";
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

    /** This method allows you to set/get the migration strategy to use.
     * @return The current migration strategy
     */
    public InterfaceMigration getMigrationStrategy() {
        return this.m_Migration;
    }
    public void setMigrationStrategy(InterfaceMigration b){
        this.m_Migration = b;
    }
    public String migrationStrategyTipText() {
        return "Choose a migration strategy to use.";
    }

    /** This method allows you to set/get the migration rate to use.
     * @return The current migration rate
     */
    public int getMigrationRate() {
        return this.m_MigrationRate;
    }
    public void setMigrationRate(int b){
        this.m_MigrationRate = b;
    }
    public String migrationRateTipText() {
        return "Set the migration rate for communication between islands.";
    }

    /** This method allows you to managae the available servers
     * @return The current servers
     */
    public PropertyRemoteServers getServers() {
        return this.m_Servers;
    }
    public void setServers(PropertyRemoteServers b){
        this.m_Servers = b;
    }
    public String serversTipText() {
        return "Choose and manage the servers (only active in parallelized mode).";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return this.m_Population;
    }
    public void setPopulation(Population pop) {
        // @todo Jetzt m�sste ich die pop auch auf die Rechner verteilen...
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "(Defunct)";
    }
    
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
    /** This method allows you to set the number of processors in local mode
     * @param n     Number of processors.
     */
    public void setNumberLocalCPUs(int n) {
        this.m_LocalCPUs = n;
    }
    public int getNumberLocalCPUs() {
        return this.m_LocalCPUs;
    }
    public String numberLocalCPUsTipText() {
        return "Set the number of local CPUS (only active in non-parallelized mode).";
    }
}