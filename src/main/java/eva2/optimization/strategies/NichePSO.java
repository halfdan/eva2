package eva2.optimization.strategies;

import eva2.OptimizerFactory;
import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.plot.TopoPlot;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.enums.PSOTopology;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.nichepso.absorption.EuclideanDiversityAbsorptionStrategy;
import eva2.optimization.operator.nichepso.absorption.InterfaceAbsorptionStrategy;
import eva2.optimization.operator.nichepso.absorption.StandardAbsorptionStrategy;
import eva2.optimization.operator.nichepso.deactivation.InterfaceDeactivationStrategy;
import eva2.optimization.operator.nichepso.deactivation.StandardDeactivationStrategy;
import eva2.optimization.operator.nichepso.merging.InterfaceMergingStrategy;
import eva2.optimization.operator.nichepso.merging.ScatterMergingStrategy;
import eva2.optimization.operator.nichepso.merging.StandardMergingStrategy;
import eva2.optimization.operator.nichepso.subswarmcreation.InterfaceSubswarmCreationStrategy;
import eva2.optimization.operator.nichepso.subswarmcreation.StandardSubswarmCreationStrategy;
import eva2.optimization.operator.paramcontrol.LinearParamAdaption;
import eva2.optimization.operator.paramcontrol.ParamAdaption;
import eva2.optimization.operator.paramcontrol.ParameterControlManager;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.population.SolutionSet;
import eva2.problems.*;
import eva2.tools.chart2d.*;
import eva2.util.annotation.Description;
import eva2.util.annotation.Hidden;
import eva2.util.annotation.Parameter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * The NichePSO extends the particle swarm optimizer (PSO) by Kennedy and
 * Eberhart to locate multiple optima of a multimodal objective function. The
 * original algorithm is proposed in [1] and uses a main swarm to explore the
 * search space. Subswarms are formed from that main swarm to refine and
 * represent single niches which are assumed to correspond to local or global
 * optima.
 * <p>
 * Different strategies are employed in order to: create subswarms from the main
 * swarm merge subswarms if they converge to the same solution absorb main swarm
 * particles into a subswarm in case a particle enters the area covered by the
 * subswarm deactivate subswarms when all containing particles converged on a
 * solution
 * <p>
 * Some implementations of these strategies and the deactivation strategy itself
 * extend the original algorithm. As proposed in [1], NichePSO uses the
 * "cognition only" model of the "inertness weight" PSO to train the main swarm.
 * <p>
 * mainSwarmInertness sets the inertia weight omega and weights the particles
 * tendency to follow its former movement. This controls exploration (favored by
 * larger values) against exploitation (favored by smaller values).
 * mainSwarmPhi1 sets Phi1 and weights the cognitive component. The term
 * corresponds to the particles tendency to return to its personal best
 * position. The combination of a typical value of mainSwarmInertness = 0.7
 * linearly decreasing to 0.2 and a typical value of mainSwarmPhi1 = 1.2
 * produces good results. maxAllowedSwarmRadius defines the maximal radius a
 * subswarm can formally have, but it does not affect the actual radius. This
 * adjustment is proposed in [2] in order to improve the performance of the
 * NichePSO. Experiments showed a good performance for relatively small values
 * of maxAllowedSwarmRadius &lt;= 0.0001 on lower dimensional problems. For higher
 * dimensional problems, larger values may be preferable.
 * <p>
 * [1] R. Brits, A. P. Engelbrecht and B. Bergh. A Niching Particle Swarm
 * Optimizer In Proceedings of the 4th Asia-Pacific Conference on Simulated
 * Evolution and Learning (SEAL'02), 2002, 2, 692-696 [2] E. �zcan and M.
 * Yilmaz. Particle Swarms for Multimodal Optimization. In: ICANNGA (1), Seiten
 * 366�375, 2007
 */
@Description("A Niching Particle Swarm Optimizer")
public class NichePSO extends AbstractOptimizer implements InterfaceAdditionalPopulationInformer, java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2036532085674554490L;
    /**
     * ********************************************************************************************************************
     * members
     */
    // nichePSO Parameter 
    protected int mainSwarmSize = 75;
    protected double maxAllowedSwarmRadius = 0.0001; // formally limits the swarm radius of the subswarms
    // Parameter for the mainswarm
//	protected double mainSwarmPhi1 = 1.2;
//	protected double mainSwarmPhi2 = 0; // by default no communication in the mainswarm
    protected PSOTopology mainSwarmTopology = PSOTopology.grid; // = 1;
    protected int mainSwarmTopologyRange = 0;
    private ParticleSwarmOptimization.PSOType mainSwarmAlgoType = ParticleSwarmOptimization.PSOType.Inertness; // 0: inertness, 1: constriction
    //	private InterfaceParameterAging mainSwarmParamAging = new LinearParameterAging();
    protected ParameterControlManager paramControl = new ParameterControlManager();
    boolean returnRepresentativeSolutionsOnly = true; // if true only the representatives of every subswarm are returned, else every particles pbest 
    boolean partlyInactive = false; // used to inactivate parts of the optimizer to see the effect on the performance
    private boolean verbose = false; // print events on the console
    transient boolean log = false; // for debugging: produce the NichePSO-file and FinalSuggestedOptima-plot with the elite
    transient boolean plotFinal = false; // plot finalSuggestedOptima
    protected boolean plot = false; // produce plots
    protected boolean useSinglePlotWindow = true;
    transient boolean savePlots = false; // save produced plots as jpegs (turn off for many multiruns...)
    protected int showCycle = 10; // produce a plot every n generations
    protected transient String dirForCurrentExperiment = "unset";
    // the main swarm and the subswarms
    protected ParticleSubSwarmOptimization mainSwarm = new ParticleSubSwarmOptimization();
    protected Vector<ParticleSubSwarmOptimization> subSwarms = new Vector<>();
    protected ParticleSubSwarmOptimization subswarmOptimizerTemplate = new ParticleSubSwarmOptimization();
    // individuals to be reinitialized in the next iteration
    protected Vector<int[]> indicesToReinit = null;
    // the strategies
    protected InterfaceDeactivationStrategy deactivationStrategy = new StandardDeactivationStrategy();
    protected InterfaceMergingStrategy mergingStrategy = new StandardMergingStrategy();
    protected InterfaceAbsorptionStrategy absorptionStrategy = new StandardAbsorptionStrategy();
    protected InterfaceSubswarmCreationStrategy subswarmCreationStrategy = new StandardSubswarmCreationStrategy();
    // the problem
    protected InterfaceOptimizationProblem optimizationProblem = new FM0Problem();
    // for debugging: file containing the output 
    transient protected BufferedWriter outputFile = null;
    // for debugging and plotting	-----------------------------------------------
    transient protected TopoPlot topoPlot;
    transient protected boolean showNextPlot = false;
    transient protected boolean deactivationOccured = false;
    transient protected boolean mergingOccurd = false;
    transient protected boolean absorbtionOccurd = false;
    transient protected boolean creationOccurd = false;
    // deactivation
    transient protected Vector<ParticleSubSwarmOptimization> deactivatedSwarm;
    // merging
    transient protected Vector<ParticleSubSwarmOptimization> borg;
    transient protected Vector<ParticleSubSwarmOptimization> others;
    transient protected Vector<AbstractEAIndividual> borgbest;
    transient protected Vector<AbstractEAIndividual> othersbest;
    // absorbtion
    transient protected Vector<AbstractEAIndividual> indytoabsorb;
    // subswarmcreation
    transient protected Vector<AbstractEAIndividual> indyconverged;
    transient protected Vector<AbstractEAIndividual> convergedneighbor;
    //-----------------------------------------------------------------------------
    public static final String stdDevKey = "StdDevKey";
    public static final String fitArchiveKey = "FitnessArchiveKey";
    // by default, calculate the individuals fitness std dev using this number of past fitness values 
    public static final int defaultFitStdDevHorizon = 3;

    public NichePSO() {
        if (log) {
            initLogFile();
        }
        initMainSwarm(); // not really necessary if initialize is called before optimization but this way initialize doesnt change the parameters of a newly constructed object
        initSubswarmOptimizerTemplate();

        hideHideable();
    }

    /**
     * Take care that all properties which may be hidden (and currently are)
     * send a "hide" message to the Java Bean properties. This is called by
     * PropertySheetPanel in use with the GenericObjectEditor.
     */
    public void hideHideable() {
        // the following properties are hidden from the GUI in the ANPSO ctor but should be shown for the NichePSO
        GenericObjectEditor.setHideProperty(getClass(), "subswarmCreationStrategy", false);
        GenericObjectEditor.setHideProperty(getClass(), "mergingStrategy", false);
        GenericObjectEditor.setHideProperty(getClass(), "absorptionStrategy", false);
        GenericObjectEditor.setHideProperty(getClass(), "maxAllowedSwarmRadius", false);
    }

    public NichePSO(NichePSO a) {
        this.mainSwarmSize = a.mainSwarmSize;
        this.maxAllowedSwarmRadius = a.maxAllowedSwarmRadius;

//		this.mainSwarmPhi1 = a.mainSwarmPhi1;
//		this.mainSwarmPhi2 = a.mainSwarmPhi2;
        this.mainSwarmTopology = a.mainSwarmTopology;
        this.mainSwarmTopologyRange = a.mainSwarmTopologyRange;
        this.mainSwarmAlgoType = a.mainSwarmAlgoType;
//		this.mainSwarmParamAging = (InterfaceParameterAging)a.mainSwarmParamAging.clone();
        this.paramControl = (ParameterControlManager) a.paramControl.clone();

        this.returnRepresentativeSolutionsOnly = a.returnRepresentativeSolutionsOnly;
        this.partlyInactive = a.partlyInactive;
        this.verbose = a.verbose;
        this.log = a.log;
        this.useSinglePlotWindow = a.useSinglePlotWindow;
        this.savePlots = a.savePlots;
        this.showCycle = a.showCycle;
        this.setDirForCurrentExperiment(a.getDirForCurrentExperiment());

        this.setMainSwarm((ParticleSubSwarmOptimization) a.getMainSwarm().clone());
        this.setSubSwarms((Vector<ParticleSubSwarmOptimization>) a.getSubSwarms().clone());
        this.setSubswarmOptimizerTemplate((ParticleSubSwarmOptimization) a.getSubswarmOptimizerTemplate().clone());

        this.deactivationStrategy = (InterfaceDeactivationStrategy) a.deactivationStrategy.clone();
        this.mergingStrategy = (InterfaceMergingStrategy) a.mergingStrategy.clone();
        this.absorptionStrategy = (InterfaceAbsorptionStrategy) a.absorptionStrategy.clone();
        this.subswarmCreationStrategy = (InterfaceSubswarmCreationStrategy) a.subswarmCreationStrategy.clone();

        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
    }

    @Override
    public Object clone() {
        return new NichePSO(this);
    }

    /**
     * ps sets the mainswarm according to the NichePSO Parameters,
     * called via initialize()
     */
    protected void initMainSwarm() {
        // pass NichePSO parameter on to the mainswarmoptimzer
        setMainSwarmSize(mainSwarmSize); // (particles are initialized later via initialize)
        getMainSwarm().setProblem(optimizationProblem);
        getMainSwarm().SetMaxAllowedSwarmRadius(maxAllowedSwarmRadius);
        getMainSwarm().getPopulation().setGeneration(0);

        // choose PSO-type for the mainswarmoptimizer 
        getMainSwarm().setGcpso(false);
//		getMainSwarm().setAlgoType(new SelectedTag("Inertness"));
        getMainSwarm().setParameterControl(new ParamAdaption[]{getDefaultInertnessAdaption()});
        getMainSwarm().setAlgoType(mainSwarmAlgoType);
        setMainSwarmAlgoType(mainSwarmAlgoType); // set algo type, may influence aging

//		getMainSwarm().setPhi1(mainSwarmPhi1); // cognitive component "tendency to return to the best position visited so far"
//		getMainSwarm().setPhi2(mainSwarmPhi2); // social component "tendency to be attracted towards the best position found in its neighbourhood."

        getMainSwarm().setTopology(mainSwarmTopology);
        getMainSwarm().setTopologyRange(mainSwarmTopologyRange);
    }

    public static ParamAdaption getDefaultInertnessAdaption() {
        return new LinearParamAdaption("inertnessOrChi", 0.7, 0.2);
    }

    /**
     * inits the template used for creating subswarms this is only
     * called in the ctor not via initialize() (would overwrite changes set from
     * outside for the next run)
     */
    protected void initSubswarmOptimizerTemplate() {
        // pass on the parameters set via NichePSO (done in the analogous nichePSO-Setters as well -> no initialize() necessary)
        getSubswarmOptimizerTemplate().setProblem(optimizationProblem);
        getSubswarmOptimizerTemplate().SetMaxAllowedSwarmRadius(maxAllowedSwarmRadius);

        // choose PSO-type for the subswarmoptimizer
        getSubswarmOptimizerTemplate().setGcpso(true);
        getSubswarmOptimizerTemplate().setRho(0.1); // on 2D Problems empirically better than default value 1
        getSubswarmOptimizerTemplate().setAlgoType(ParticleSwarmOptimization.PSOType.Constriction);

        //"Several studies propose different values for these parameters" (http://tracer.uc3m.es/tws/pso/parameters.html)
        //Bergh2002 p.87
        //Particle Swarm Optimization - an introduction and its recent developments slide 22 P6 (in the constriction variant)
        getSubswarmOptimizerTemplate().setInertnessOrChi(0.7298437881283576);
        getSubswarmOptimizerTemplate().setPhi1(2.05);
        getSubswarmOptimizerTemplate().setPhi2(2.05);

        //subswarmOptimizerTemplate.initGCPSOMember();
//		getSubswarmOptimizerTemplate().getPopulation().setMaxHistoryLength(25);
    }

    /**
     * @return an optimizer with parameters set according to the nichePSO
     * returns the optimizer that should be used to create a new
     * subswarm
     */
    public ParticleSubSwarmOptimization getNewSubSwarmOptimizer() {
        //initSubswarmOptimizerTemplate();
        ParticleSubSwarmOptimization template = (ParticleSubSwarmOptimization) getSubswarmOptimizerTemplate().clone(); // this implicitely clones the problem but does not initialize it again...
        template.setProblem(this.optimizationProblem); //... let all subswarms use the same correct initialised problem instance
        return template;
    }

    /**
     * @see eva2.optimization.strategies.InterfaceOptimizer#initialize()
     */
    @Override
    public void initialize() { // (called right before next optimize/mutltirun)
        // initialize main swarm
        initMainSwarm(); // MOE: auch bei multirun: m�gliche �nderungen an Gr��e, AlgoType, maxrad, delta etc. aus letzter Optimierung zur�cksetzen
        // mainSwarm.initialize():
        // - place all indys rndly in search space (depends on: mainSwarmSize, problem)
        // - use rnd initialize velocity vector
        // - evaluate (depends on: problem)
        // - initialize fit-archive,stddev, pbest, PBestImprovementsInARow
        // - update mbestindividual, maxposdist
        // - set particleIndexCounter
        getMainSwarm().initialize();

        // initialize subswarms
        //initSubswarmOptimizerTemplate(); //only in ctor, would change parameters for the next multirun 
        //subwarmOptimizerTemplate.initialize(); // dont initialize and evaluate individuals !
        setSubSwarms(new Vector<ParticleSubSwarmOptimization>()); // dont want to use subswarms from old optimization run (especially not in multiruns)...
        indicesToReinit = null;
        // show in plot
        //MainSwarm.setShow(true);
        if (isPlot()) {
            initPlotSwarm();
        }
    }

    /**
     * uses the given population and basically sets rnd
     * velocity vectors (if reset == false)
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        // initializeByPopulation(...):
        // - use indys from pop 
        // - use rnd initialize velocity vector
        // - evaluate (depends on: problem)
        // - initialize fit-archive,stddev and pbest
        getMainSwarm().initializeByPopulation(pop, reset);

        initSubswarmOptimizerTemplate();
    }

    /**
     * @see eva2.optimization.strategies.InterfaceOptimizer#optimize()
     */
    @Override
    public void optimize() {
//		System.out.println(BeanInspector.toString(getMainSwarm()));
        if (isVerbose()) {
            Population pop = getPopulation();
            System.out.println("pop bef: " + pop.size() + " " + pop.getFunctionCalls());
        }
        // main swarm:
        if (getMainSwarm().getPopulation().size() == 0) {// || mainSwarm.getPopulation().size() == 1){
            if (isVerbose()) {
                System.out.print("MainSwarm size is 0\n");
            }
            // increment the generationcount for the terminator: 
            // 1 generation equals one optimize call including the optimization of the 
            // (possibly empty) mainswarm and all subswarms
            getMainSwarm().getPopulation().incrGeneration();
        } else {
            getMainSwarm().optimize();
        }

        maybeReinitIndies();

        // subswarms:
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization subswarm = getSubSwarms().get(i);
            if (subswarm.isActive()) {
                subswarm.optimize();
            }
//			System.out.println(i + " " + subswarm.getPopulation().getFunctionCalls());
        }

        // deactivation:
        deactivateSubSwarmsIfPossible();

        // merging:
        mergeSubswarmsIfPossible();

        // absorption:
        absorbParticlesIfPossible();

        // create new subswarms:
        createSubswarmIfPossible();

        firePropertyChangedEvent("NextGenerationPerformed"); // calls Listener that sth changed...
        if (isVerbose()) {
            Population pop = getPopulation();
            System.out.println("pop aft: " + pop.size() + " " + pop.getFunctionCalls());
        }

        /**
         * plotting *********************************************************************************
         */
        if (isPlot()) {
            doPlot();
        }
        /**
         * end plotting ******************************************************************************
         */
        // reset flags etc for:
        // deactivation
        deactivationOccured = false;
        deactivatedSwarm = new Vector<>();
//		reinitedSwarm = new Vector<ParticleSubSwarmOptimization>();
        // merging
        mergingOccurd = false;
        borg = new Vector<>();
        others = new Vector<>();
        borgbest = new Vector<>();
        othersbest = new Vector<>();
        // absorbtion
        absorbtionOccurd = false;
        indytoabsorb = new Vector<>();
        // subswarmcreation
        creationOccurd = false;
        indyconverged = new Vector<>();
        convergedneighbor = new Vector<>();
    }

    /**
     * Check if lone individuals are scheduled for reinitialization into the
     * main swarm. This happens after subswarm deactivation. Should be called
     * only directly after the main swarm optimization call.
     */
    protected void maybeReinitIndies() {
        if (indicesToReinit != null && (indicesToReinit.size() > 0)) { // add new individuals 
            getMainSwarm().reinitIndividuals(indicesToReinit);
            indicesToReinit.clear();
        }
    }

    /**
     * Schedule new particles to be added to this swarm, rndly inited over the
     * search space by the problem
     *
     * @param particleIndices set of indices that should be used for the added
     *                        particles, if null new indices are created
     */
    public void scheduleNewParticlesToPopulation(int[] particleIndices) {
        if (particleIndices != null) {
            if (indicesToReinit == null) {
                indicesToReinit = new Vector<>();
            }
            indicesToReinit.add(particleIndices);
        }
    }

    protected void doPlot() {
        showNextPlot = (deactivationOccured || mergingOccurd || absorbtionOccurd || creationOccurd);// repopoccurd || reinitoccurd);
        if (this.getMainSwarm().getPopulation().getGeneration() % this.getShowCycle() == 0) {// || showNextPlot){
            // plot merging step
            if (mergingOccurd) {
                plotMainSwarm(false);
                plotMergingCondition();
                if (savePlots) {
                    String generation = String.valueOf(getMainSwarm().getPopulation().getGeneration());
                    //	saveCurrentPlotAsJPG(getCurrentDateAsString()+generation+"a");
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            // plot main step
            synchronized (topoPlot.getClass()) {
                plotMainSwarm(false);
                plotSubSwarms();
            }
            if (savePlots) {
                String gen = String.valueOf(getMainSwarm().getPopulation().getGeneration());
                //	saveCurrentPlotAsJPG(getCurrentDateAsString()+gen+"b");
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            //plotBoundStdDevInMainSwarm(0.5);
            showNextPlot = false;
        }
    }

    /**
     * ********************************************************************************************************************
     * Deactivation
     */
    protected void deactivationEventFor(ParticleSubSwarmOptimization subswarm) {
        if (isVerbose()) {
            System.out.println("deactivating subswarm");
        }
        deactivatedSwarm.add(subswarm); // only for plotting
        deactivationOccured = true;
    }

    /**
     * deactivates the subswarms according to the decativation
     * strategy
     */
    protected void deactivateSubSwarmsIfPossible() {
        // check for every subswarm...
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentsubswarm = getSubSwarms().get(i);
            //.. if it meets the criterion of the deactivation strategy
            if (getDeactivationStrategy().shouldDeactivateSubswarm(currentsubswarm)) {
                if (isVerbose()) {
                    System.out.println("deactivation in NPSO!");
                }
                deactivationEventFor(currentsubswarm);
                scheduleNewParticlesToPopulation(getDeactivationStrategy().deactivateSubswarm(currentsubswarm, getMainSwarm()));
            }
        }
    }

    protected double getAvgActiveSubSwarmSize() {
        double avgSize = 0;
        int actCnt = 0;
        // check for every subswarm...
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentsubswarm = getSubSwarms().get(i);
            if (currentsubswarm.isActive()) {
                actCnt++;
                avgSize += currentsubswarm.population.size();
            }
        }
        if (actCnt > 0) {
            return (avgSize / actCnt);
        } else {
            return 0;
        }
    }

    protected int countActiveSubswarms() {
        int actCnt = 0;
        // check for every subswarm...
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentsubswarm = getSubSwarms().get(i);
            if (currentsubswarm.isActive()) {
                actCnt++;
            }
        }
        return actCnt;
    }

    protected void mergingEventFor(int i, int j) {
        if (isVerbose()) {
            System.out.print("merge condition \n");
        }
        ParticleSubSwarmOptimization borg = getSubSwarms().get(i);
        ParticleSubSwarmOptimization others = getSubSwarms().get(j);
        this.borg.add((ParticleSubSwarmOptimization) borg.clone()); // for plotting only
        this.others.add((ParticleSubSwarmOptimization) others.clone()); // for plotting only
        mergingOccurd = true;
        this.borgbest.add(borg.bestIndividual); // for plotting only
        this.othersbest.add(others.bestIndividual); // for plotting only
    }

    /**
     * merges the subswarms according to the merging strategy
     */
    protected void mergeSubswarmsIfPossible() {
        boolean runagain = false;
        if (isVerbose()) {
            System.out.println("possibly merging " + getSubSwarms().size() + " subswarms...");
        }
        // check for every two subswarms...
        for (int i = 0; i < getSubSwarms().size(); ++i) {
//			System.out.print(" " + getSubSwarms().get(i).getPopulation().size());
            for (int j = i + 1; j < getSubSwarms().size(); ++j) {
                //...  if they should be merged according to the merging strategy
                if (getMergingStrategy().shouldMergeSubswarms(getSubSwarms().get(i), getSubSwarms().get(j))) {
                    if (isVerbose()) {
                        System.out.println("Merging in NPSO!");
                    }
                    mergingEventFor(i, j); // for plotting
                    getMergingStrategy().mergeSubswarms(i, j, getSubSwarms(), getMainSwarm());
                    runagain = true; //  merged subswarm may overlap with another swarm now. This might not have been considered in this run...
                    --j; // subSwarms.size() has decreased and all elements >=j were shifted one position to the left
                }
            }
        }
//		System.out.println();
        if (runagain) {
            mergeSubswarmsIfPossible();
        }
    }

    /**
     * Adds indy to an active subswarm, then removes indy from the mainswarm.
     *
     * @param indy
     * @param subswarm
     */
    protected void absorbtionEventFor(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm) {
        if (isVerbose()) {
            System.out.print("Absorbtion \n");
        }
        absorbtionOccurd = true;
        this.indytoabsorb.add(indy);
    }

    /**
     * absorbs the mainswarm particles into the subswarm according
     * to the absorbtion strategy
     */
    protected void absorbParticlesIfPossible() {
        boolean runagain = false;
        // check for every particle in the main swarm...
        for (int i = 0; i < getMainSwarm().getPopulation().size(); ++i) {
            AbstractEAIndividual currentindy = getMainSwarm().getPopulation().getEAIndividual(i);
            // ...if it matches the absorption-criterion for any subswarm:
            for (int j = 0; j < getSubSwarms().size(); ++j) {
                ParticleSubSwarmOptimization currentsubwarm = getSubSwarms().get(j);
                if (getAbsorptionStrategy().shouldAbsorbParticleIntoSubswarm(currentindy, currentsubwarm, this.getMainSwarm())) {
                    if (isVerbose()) {
                        System.out.println("Absorbing particle (NPSO)");
                    }
                    absorbtionEventFor(currentindy, currentsubwarm);
                    getAbsorptionStrategy().absorbParticle(currentindy, currentsubwarm, this.getMainSwarm());
                    --i; // ith particle is removed, all indizes shift one position to the left
                    runagain = true; // if the absorbed particle provides a new best position, the radius of the swarm will change -> run absorption again 
                    break; // dont try to absorb this particle again into another subswarm
                }
            }
        }
        if (runagain) {
            absorbParticlesIfPossible();
        }
    }

    protected void subswarmCreationEventFor(AbstractEAIndividual currentindy, ParticleSubSwarmOptimization subswarm) {
        if (isVerbose()) {
            System.out.print("creating subswarm\n");
        }
        creationOccurd = true;
        this.indyconverged.add(currentindy);
        for (int i = 0; i < subswarm.getPopulation().size(); ++i) {
            AbstractEAIndividual indy = subswarm.getPopulation().getEAIndividual(i);
            if (indy.getIndyID() != currentindy.getIndyID()) {
                this.convergedneighbor.add(indy);
            }
        }
    }

    /**
     * creates a subswarm from every particle in the mainswarm
     * that meets the convergence-criteria of the creation strategy
     */
    protected void createSubswarmIfPossible() {
        // for every particle...
        for (int i = 0; i < getMainSwarm().getPopulation().size(); ++i) {
            AbstractEAIndividual currentindy = getMainSwarm().getPopulation().getEAIndividual(i);
            //... that meets the convergence-criteria of the creation strategie
            if (getSubswarmCreationStrategy().shouldCreateSubswarm(currentindy, getMainSwarm())) {
                if (isVerbose()) {
                    System.out.println("Creating sub swarm (NPSO)");
                }
                // use an optimizer according to the template
                ParticleSubSwarmOptimization newSubswarm = getNewSubSwarmOptimizer();
                // and create a subswarm from the given particle
                getSubswarmCreationStrategy().createSubswarm(newSubswarm, currentindy, getMainSwarm());
                subswarmCreationEventFor(currentindy, newSubswarm);
                // add the subswarm to the set of subswarms:
                this.getSubSwarms().add(newSubswarm);
                i = 0; // start again because indizes changed and we dont know how...  
            }
        }
    }

    @Override
    public void setPopulation(Population pop) {
        //pass on to mainswarm optimizer
        getMainSwarm().setPopulation(pop);
    }

    /**
     * @return a population consisting of copies from the mainswarm and all
     *         active subswarms
     */
    public Population getActivePopulation() {
        // construct a metapop with clones from the mainswarm and all active subswarms
        Population metapop = (Population) getMainSwarm().getPopulation().clone();
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentsubswarm = getSubSwarms().get(i);
            if (currentsubswarm.isActive()) {
                Population currentsubswarmpop = (Population) currentsubswarm.getPopulation().clone();
                metapop.addPopulation(currentsubswarmpop);
            }
        }

        // set correct number of generations
        metapop.setGeneration(getMainSwarm().getPopulation().getGeneration());

        // set correct number of function calls
        int calls = getMainSwarm().getPopulation().getFunctionCalls();
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentsubswarm = getSubSwarms().get(i);
            // calls from inactive populations have to be counted as well...
            calls += currentsubswarm.getPopulation().getFunctionCalls();
        }
        metapop.setFunctionCalls(calls);
        // care for consistent size:
        metapop.synchSize();
        return metapop;
    }

    /**
     * @return a population consisting of copies from the mainswarm and all
     *         subswarms.
     * returns a population consisting of copies from the
     * mainswarm and all subswarms (active and inactive, so the size of this
     * Population is not necessarily constant). (Especially important for the
     * call back regarding the output file... ) Beware:
     * getPopulation().getPopulationSize() returns the !initial! size of the
     * main swarm, the actual size of the complete population is accessed via
     * getPopulation().size()
     */
    @Override
    public Population getPopulation() {
        boolean activeOnly = true; // true makes problems if all subswarms are deactivated at the same time!
        // construct a metapop with clones from the mainswarm and all subswarms
        Population metapop = getMainSwarm().getPopulation().cloneWithoutInds();
        metapop.ensureCapacity(getMainSwarmSize());
        metapop.addPopulation(getMainSwarm().getPopulation());
        int activeCnt = 0;
//		Population currentsubswarm;
        for (int i = 0; i < getSubSwarms().size(); ++i) {
//			currentsubswarm = (Population)getSubSwarms().get(i).getPopulation().clone();
            if (getSubSwarms().get(i).isActive()) {
                activeCnt++;
                metapop.addPopulation(getSubSwarms().get(i).getPopulation());
            } else if (!activeOnly) {
                metapop.addPopulation(getSubSwarms().get(i).getPopulation());
            }
        }

        if (isVerbose()) {
            System.out.println("Active populations: " + activeCnt);
        }
        // set correct number of generations
        metapop.setGeneration(getMainSwarm().getPopulation().getGeneration());

        // set correct number of function calls
        int calls = getMainSwarm().getPopulation().getFunctionCalls();
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            calls += getSubSwarms().get(i).getPopulation().getFunctionCalls();
        }
//		System.out.println("metapop size " + metapop.size());
        metapop.setFunctionCalls(calls);

        if (metapop.size() == 0) {
            System.err.println("NichePSO ERROR! " + metapop.getFunctionCalls());

            int i = getSubSwarms().size() - 1;
            while (i >= 0 && (metapop.size() < mainSwarmSize)) {
//			for (int i = 0; i < getSubSwarms().size(); ++i){
                metapop.addPopulation(getSubSwarms().get(i).getPopulation());
                i--;
            }
        }
        return metapop;
    }

    public String populationTipText() {
        return "please use mainSwarmSize to set the population size";
    }

    /**
     * @return a population consisting of the personal best solutions of every
     *         particle in the mainswarm and all subswarms
     * @see eva2.optimization.strategies.InterfaceOptimizer#getAllSolutions()
     */
    @Override
    public SolutionSet getAllSolutions() {
        // hier kann dasselbe geliefert werden wie bei getPopulation
        // speziell fuer multi-modale optimierung kann aber noch "mehr" als die aktuelle Population zurueckgeliefert werden
        // zB die aktuelle Population und ein Archiv fruehererer Loesungen (das machen CBN und CHC jetzt).

        if (returnRepresentativeSolutionsOnly) {
            Population sols = getSubswarmRepresentatives(false);
            Population metapop = getPopulation();
            sols.setFunctionCalls(metapop.getFunctionCalls());
            sols.setGeneration(metapop.getGeneration());
            return new SolutionSet(metapop, sols);
        } else {
            Population metapop = getPopulation();
            Population sols = new Population();
            for (int i = 0; i < metapop.size(); ++i) {
                AbstractEAIndividual indy = metapop.getEAIndividual(i);
                AbstractEAIndividual pbest = (AbstractEAIndividual) indy.getData("PersonalBestKey");
                sols.add(pbest);
            }
            sols.setFunctionCalls(metapop.getFunctionCalls());
            sols.setGeneration(metapop.getFunctionCalls());
            return new SolutionSet(sols);
        }
    }

    /**
     * @return the best solution found by any particle in any swarm
     */
    public AbstractEAIndividual getGlobalBestSolution() {
        Population metapop = getPopulation();
        if (metapop.size() == 0) {
            System.out.println("getGlobalBestSolution: all swarms are empty ");
            return null;
        }
        AbstractEAIndividual gbest = (AbstractEAIndividual) metapop.getEAIndividual(0).getData("PersonalBestKey");
        for (int i = 1; i < metapop.size(); ++i) {
            AbstractEAIndividual currentPBest = (AbstractEAIndividual) metapop.getEAIndividual(i).getData("PersonalBestKey");
            if (currentPBest.isDominating(gbest)) {
                gbest = currentPBest;
            }
        }
        return gbest;
    }

    /**
     * @return array with copies of the gbest individuals
     * returns the cloned global best individuals (ie best of all
     * time) from every subswarm and the main swarm
     */
    public Population getSubswarmRepresentatives(boolean onlyInactive) {
        Population representatives = new Population(getSubSwarms().size() + 1);
        Vector<ParticleSubSwarmOptimization> subSwarms = getSubSwarms();
//		AbstractEAIndividual[] representatives = new AbstractEAIndividual[getSubSwarms().size()+1];
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            if (!onlyInactive || (!subSwarms.get(i).isActive())) {
                representatives.add((AbstractEAIndividual) (subSwarms.get(i)).bestIndividual.clone());
            }
        }
        if (!onlyInactive && (getMainSwarm().getPopulation().size() != 0)) {
            representatives.add((AbstractEAIndividual) getMainSwarm().bestIndividual.clone()); // assures at least one solution, even if no subswarm has been created
        }
        return representatives;
    }

    /**
     * @param size
     * ps sets the !initial! size of the mainswarm population use this
     * instead of getPopulation.setPopulationSize()
     */
    public void setMainSwarmSize(int size) {
        // set member
        this.mainSwarmSize = size;
        // pass on to the mainswarm optimizer
        getMainSwarm().getPopulation().setTargetSize(size);
    }

    /**
     * @return the !initial! size of the mainswarm population
     * returns the !initial! size of the mainswarm population
     */
    public int getMainSwarmSize() {
        return this.mainSwarmSize;
    }

    public String mainSwarmSizeTipText() {
        return "sets the initial size of the mainswarm population";
    }

    /**
     * @param val
     * defines the maximal allowed subswarm radius for absorption and
     * merging
     */
    public void setMaxAllowedSwarmRadius(double val) {
        // set member
        this.maxAllowedSwarmRadius = val;
        // pass on to the main- and subswarm optimizers
        getMainSwarm().SetMaxAllowedSwarmRadius(val);
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            getSubSwarms().get(i).SetMaxAllowedSwarmRadius(val);
        }
        getSubswarmOptimizerTemplate().SetMaxAllowedSwarmRadius(val);
    }

    /**
     * @return
     */
    public double getMaxAllowedSwarmRadius() {
        return this.maxAllowedSwarmRadius;
    }

    public String maxAllowedSwarmRadiusTipText() {
        return "no subswarm radius is allowed to (formally) exceed this threshold (see help for details)";
    }

    //	public double getMainSwarmPhi1() {
//		return mainSwarmPhi1;
//	}
//
//	public void setMainSwarmPhi1(double mainSwarmPhi1) {
//		this.mainSwarmPhi1 = mainSwarmPhi1;
//		double inertChi = getMainSwarm().getInertnessOrChi();
//		getMainSwarm().setPhi1(mainSwarmPhi1);
//		// if constriction calc. changed the inertness, update it here, else dont.
////		if (getMainSwarm().getInertnessOrChi() != inertChi) mainSwarmParamAging.setStartValue(getMainSwarm().getInertnessOrChi());
//		if (getMainSwarm().getInertnessOrChi() != inertChi) getMainSwarm().setInertnessOrChi(mainSwarmPhi1);
//	}
//
//	public double getMainSwarmPhi2() {
//		return mainSwarmPhi2;
//	}
    public double getMainSwarmInitialVelocity() {
        return mainSwarm.getInitialVelocity();
    }

    public void setMainSwarmInitialVelocity(double v) {
        mainSwarm.setInitialVelocity(v);
    }

    public String mainSwarmInitialVelocityTipText() {
        return "The initial velocity (normed by search range) for the main swarm.";
    }

    public String mainSwarmPhi1TipText() {
        return "weights the cognitive component for the PSO used to train the main swarm";
    }

    public void setMainSwarmTopologyTag(int mainSwarmTopologyTag) {
        // Possible topologies are: "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random"  in that order starting by 0.
        this.mainSwarmTopology = PSOTopology.getFromId(mainSwarmTopologyTag);
    }

    public PSOTopology getMainSwarmTopology() {
        return mainSwarm.topology;
    }

    /**
     * This method allows you to choose the topology type.
     *
     * @param t The type.
     */
    public void setMainSwarmTopology(PSOTopology t) {
        mainSwarm.topology = t;
        this.mainSwarmTopology = t;
        GenericObjectEditor.setHideProperty(getClass(), "mainSwarmTopologyRange", mainSwarmTopology == PSOTopology.multiSwarm); // "Multi-Swarm" has no topologyRange
    }

    public int getMainSwarmTopologyRange() {
        return mainSwarmTopologyRange;
    }

    public void setMainSwarmTopologyRange(int mainSwarmTopologyRange) {
        this.mainSwarmTopologyRange = mainSwarmTopologyRange;
    }

    public ParticleSwarmOptimization.PSOType getMainSwarmAlgoType() {
        if (mainSwarmAlgoType != getMainSwarm().getAlgoType()) {
            System.err.println("Error in NichePSO:getMainSwarmAlgoType() !!");
        }
        return getMainSwarm().getAlgoType();
    }

    public void setMainSwarmAlgoType(ParticleSwarmOptimization.PSOType st) {
        getMainSwarm().setAlgoType(st);
        mainSwarmAlgoType = st;
//		mainSwarmParamAging.setStartValue(getMainSwarm().getInertnessOrChi());
    }

    /**
     * Allow GOE/PropertySheetPanel to know about change references in
     * sub-objects.
     *
     * @return
     */
    public String[] getGOEPropertyUpdateLinks() {
        return new String[]{"mainSwarmAlgoType", "mainSwarmInertness", "mainSwarmPhi1", "mainSwarmInertness", "mainSwarmPhi2", "mainSwarmInertness"};
    }

    public boolean isReturnRepresentativeSolutionsOnly() {
        return returnRepresentativeSolutionsOnly;
    }

    public void SetReturnRepresentativeSolutionsOnly(boolean returnRepresentativeSolutionsOnly) {
        this.returnRepresentativeSolutionsOnly = returnRepresentativeSolutionsOnly;
    }

    /**
     * @param val
     */
    public void SetPartlyInactive(boolean val) {
        this.partlyInactive = val;
    }

    /**
     * @return
     */
    public boolean isPartlyInactive() {
        return partlyInactive;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public String verboseTipText() {
        return "activate to print additional information to the console during optimization";
    }

    public boolean isLog() {
        return log;
    }

    public void SetLog(boolean log) {
        this.log = log;
    }

    public boolean isPlotFinal() {
        return plotFinal;
    }

    public void SetPlotFinal(boolean plotFinal) {
        this.plotFinal = plotFinal;
    }

    public void setPlot(boolean plot) {
        this.plot = plot;
    }

    public boolean isPlot() {
        return plot;
    }

    public String plotTipText() {
        return "toggles the plot window";
    }

    public boolean isUseSinglePlotWindow() {
        return useSinglePlotWindow;
    }

    public void setUseSinglePlotWindow(boolean useSinglePlotWindow) {
        this.useSinglePlotWindow = useSinglePlotWindow;
    }

    public String useSinglePlotWindowTipText() {
        return "deactivate to open a new window for every plot";
    }

    public boolean isSavePlots() {
        return savePlots;
    }

    public void SetSavePlots(boolean savePlots) {
        this.savePlots = savePlots;
    }

    public void setShowCycle(int showCycle) {
        this.showCycle = showCycle;
    }

    public int getShowCycle() {
        return showCycle;
    }

    public String showCycleTipText() {
        return "sets the interval (in generations) used to update the plot window";
    }

    /**
     * @return
     */
    public String getDirForCurrentExperiment() {
        return dirForCurrentExperiment;
    }

    /**
     * sets the base directory for any output. Output produced: - if isLog(), a
     * log file is written - if isPlot and isSavePlots, pictures are produced
     *
     * @param dirForCurrentExperiment
     */
    public void setDirForCurrentExperiment(String dirForCurrentExperiment) {
        this.dirForCurrentExperiment = dirForCurrentExperiment;
    }

    public void setMainSwarm(ParticleSubSwarmOptimization mainSwarm) {
        this.mainSwarm = mainSwarm;
    }

    public ParticleSubSwarmOptimization getMainSwarm() {
        return mainSwarm;
    }

    public String mainSwarmInertnessTipText() {
        return "sets the inertness weight used for the PSO to train the main swarm (see help for details)";
    }

    public void setSubSwarms(Vector<ParticleSubSwarmOptimization> subSwarms) {
        this.subSwarms = subSwarms;
    }

    public Vector<ParticleSubSwarmOptimization> getSubSwarms() {
        return subSwarms;
    }

    public void setSubswarmOptimizerTemplate(ParticleSubSwarmOptimization subswarmOptimizerTemplate) {
        this.subswarmOptimizerTemplate = subswarmOptimizerTemplate;
    }

    public ParticleSubSwarmOptimization getSubswarmOptimizerTemplate() {
        return subswarmOptimizerTemplate;
    }

    public String subswarmOptimizerTemplateTipText() {
        return "sets the optimizer used to train the subswarms";
    }

    public void setDeactivationStrategy(InterfaceDeactivationStrategy deactivationStrategy) {
        this.deactivationStrategy = deactivationStrategy;
    }

    public InterfaceDeactivationStrategy getDeactivationStrategy() {
        return deactivationStrategy;
    }

    public String deactivationStrategyTipText() {
        return "sets the strategy used to deactivate subswarms";
    }

    public void setMergingStrategy(InterfaceMergingStrategy mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }

    public InterfaceMergingStrategy getMergingStrategy() {
        return mergingStrategy;
    }

    public String mergingStrategyTipText() {
        return "sets the strategy used to merge subswarms";
    }

    public void setAbsorptionStrategy(InterfaceAbsorptionStrategy absorptionStrategy) {
        this.absorptionStrategy = absorptionStrategy;
    }

    public InterfaceAbsorptionStrategy getAbsorptionStrategy() {
        return absorptionStrategy;
    }

    public String absorptionStrategyTipText() {
        return "sets the strategy used to absorb main swarm particles into a subswarm";
    }

    public void setSubswarmCreationStrategy(InterfaceSubswarmCreationStrategy subswarmCreationStrategy) {
        this.subswarmCreationStrategy = subswarmCreationStrategy;
    }

    public InterfaceSubswarmCreationStrategy getSubswarmCreationStrategy() {
        return subswarmCreationStrategy;
    }

    public String subswarmCreationStrategyTipText() {
        return "sets the strategy to create subswarms from the main swarm";
    }
    /**
     * @param problem
     * This method will set the problem that is to be optimized
     */
    @Override
    @Hidden
    public void setProblem(InterfaceOptimizationProblem problem) {
        // set member
        this.optimizationProblem = problem;
        // pass on to the main- and subswarm optimizers
        getMainSwarm().setProblem(problem);
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            getSubSwarms().get(i).setProblem(problem);
        }
        getSubswarmOptimizerTemplate().setProblem(problem);
    }

    /**
     * ********************************************************************************************************************
     * getter: "descriptive parameters" for the mainswarm and subswarms
     */
    public double getAveDistToNeighborInMainswarm() {
        return getMainSwarm().getAveDistToNeighbor();
    }

    public double[] getFitDevsInMain() {
        double[] res = new double[getMainSwarm().getPopulation().size()];
        for (int i = 0; i < getMainSwarm().getPopulation().size(); ++i) {
            AbstractEAIndividual indy = getMainSwarm().getPopulation().getEAIndividual(i);
            res[i] = (Double) indy.getData(NichePSO.stdDevKey);
        }
        return res;
    }

    /**
     * @param vals
     * @return
     */
    public double getMedian(double[] vals) {
        java.util.Arrays.sort(vals);
        double result;
        if (vals.length % 2 == 0) { // even
            int mid1 = (vals.length / 2) - 1;
            int mid2 = (vals.length / 2);
            result = 1.0 / 2.0 * (vals[mid1] + vals[mid2]);

        } else { // odd
            int mid = ((vals.length + 1) / 2) - 1;
            result = vals[mid];
        }
        return result;
    }

    public double getMedianSubswarmSize() {
        if (getSubSwarms().size() == 0) {
            return 0;
        }
        double[] size = new double[getSubSwarms().size()];
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            if (getSubSwarms().get(i) == null) { // happend once - cant reproduce...
                System.out.println("getMedianSubswarmSize: subSwarms has null objects - why ?");
                break;
            }
            size[i] = getSubSwarms().get(i).getPopulation().size();
        }
        return getMedian(size);
    }

    public double getMeanSubswarmSize() {
        double mean = 0;
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            mean += getSubSwarms().get(i).getPopulation().size();
        }
        mean /= getSubSwarms().size();
        return mean;
    }

    public double getMaxSubswarmSize() {
        double max = 0;
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            if (getSubSwarms().get(i).getPopulation().size() > max) {
                max = getSubSwarms().get(i).getPopulation().size();
            }
        }
        return max;
    }

    public double getMeanSubswarmDistanceNormalised() {
        double meanDist = 0;
        int pairs = 0;
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            for (int j = i + 1; j < getSubSwarms().size(); ++j) {
                ParticleSubSwarmOptimization sub1 = getSubSwarms().get(i);
                ParticleSubSwarmOptimization sub2 = getSubSwarms().get(j);
                meanDist += getMainSwarm().distance(sub1.getGBestIndividual(), sub2.getGBestIndividual());
                ++pairs;
            }
        }
        meanDist /= pairs;
        meanDist /= getMainSwarm().maxPosDist;
        return meanDist;
    }

    public double getMeanSubswarmDiversityNormalised() {
        double meanDiv = 0;
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentSubswarm = getSubSwarms().get(i);
            meanDiv += currentSubswarm.getEuclideanDiversity();
        }
        meanDiv /= (double) getSubSwarms().size();
        meanDiv /= getMainSwarm().maxPosDist;
        return meanDiv;
    }

    /**
     * @return The name of the algorithm
     * This method will return a naming String
     */
    @Override
    public String getName() {
        return "NichePSO-" + getMainSwarmSize();
    }

    /**
     * @return A descriptive string
     * This method will return a string describing all properties of
     * the optimizer and the applied methods.
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "niching particle swarm optimization."
                + " This algorithm optimizes multiple optima of an multimodal objective function in parallel.\n";
        return result;
    }

    /**
     * @return descriptive string of the elite
     * emp returns a string that lists the global best individuals (ie
     * best of all time) from every subswarm
     */
    public String getSubswarmRepresentativesAsString(boolean onlyInactive) {
        String result = "\nSubswarmRepresentatives: \n";
        Population reps = getSubswarmRepresentatives(onlyInactive);
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            result += reps.getEAIndividual(i).getStringRepresentation() + "\n";
        }
        //result += "\n";
        return result;
    }

    /**
     * @return
     * emp
     */
    public String getPerformanceAsString() {
        if (!(optimizationProblem instanceof InterfaceMultimodalProblem)) {
            System.out.println("getPerformanceAsString: problem not instanceof InterfaceMultimodalProblem");
            return "";
        }

        String result = "Performance (#Optima found, Max Peak Ratio, which optima are found): \n";

        // construct an elite-population (with the gbest individual from every subswarm)
        Population elitepop = getSubswarmRepresentatives(false);

        // use elite population to compute performance
        if (optimizationProblem instanceof InterfaceMultimodalProblem) {
            result += ((InterfaceMultimodalProblemKnown) optimizationProblem).getNumberOfFoundOptima(elitepop);
            result += "(" + ((InterfaceMultimodalProblemKnown) optimizationProblem).getRealOptima().size() + ")\t";
            result += ((InterfaceMultimodalProblemKnown) optimizationProblem).getMaximumPeakRatio(elitepop) + "\t";
            //boolean[] opts = ((InterfaceMultimodalProblem)problem).whichOptimaAreFound(elitepop);
            //result += "Optima:";
            //for (int i = 0; i < opts.length; ++i){
            //result += String.valueOf(opts[i])+" ";
            //}
            result += "\n";
        }
        return result;
    }

    public String getReport() {
        String result = "";
        result = "Generations: " + getPopulation().getGeneration();
        result += " FunctionCalls: " + getPopulation().getFunctionCalls();
        result += " MainSwarmSize: " + getMainSwarm().getPopulation().size();
        result += " Subswarms: ";
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            result += "(" + i + ")" + getSubSwarms().get(i).getPopulation().size() + " ";
        }
        result += "SwarmSize: " + getPopulation().size();
        result += "\n";

        return result;
    }

    /**
     * ********************************************************************************************************************
     * for the logfile
     */
    /**
     * emp generates the NichePSO_date file
     */
    protected void initLogFile() {
        // opening output file...
        if (getDirForCurrentExperiment().equals("unset")) {
            System.out.println("initLogFile: no directory for output specified, please use setDirForCurrentExperiment first");
            return;
        }

        // path
        File outputPath = new File(getDirForCurrentExperiment() + "\\NichePSO-LogFiles\\");
        if (!outputPath.exists()) {
            outputPath.mkdirs();
        }

        // file name
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd'_'HH.mm.ss'_'E");
        String startDate = formatter.format(new Date());
        String name = "NichePSO-LogFile" + "__" + startDate + ".dat";

        File f = new File(outputPath, name); // plattform independent representation...

        try {
            if (outputFile != null) {
                outputFile.close();
            } // close old file
            outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
        } catch (FileNotFoundException e) {
            System.out.println("Could not open output file! Filename: " + name);
        } catch (IOException e) {
            System.out.println("Could not close old output file! Filename: " + name);
        }
    }

    /**
     * @param line string to be written
     * emp writes something to the LogFile
     */
    protected void writeToLogFile(String line) {
        String write = line + "\n";
        if (outputFile == null) {
            return;
        }
        try {
            outputFile.write(write, 0, write.length());
            outputFile.flush();
        } catch (IOException e) {
            System.out.println("Problems writing to output file!");
        }
    }

    /**
     * @param index
     * @return particle with given index
     * @param pop
     */
    public AbstractEAIndividual getIndyByParticleIndexAndPopulation(Population pop, Integer index) {
        for (int i = 0; i < pop.size(); ++i) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
            //Integer tmp = (Integer)indy.getData("particleIndex"); // here getData was a cpu-time hotspot ->  AbstractEAIndividual now has an index as "direct member"
            //if (index.equals(tmp)) return indy;
            if (index == indy.getIndividualIndex()) {
                return indy;
            }
        }
        return null;
    }

    /**
     * @return main swarm particle with given index
     * @param index
     */
    public AbstractEAIndividual getIndyByParticleIndex(Integer index) {
        AbstractEAIndividual indy = null;
        Population pop = getMainSwarm().getPopulation();
        indy = getIndyByParticleIndexAndPopulation(pop, index);
        if (indy != null) {
            return indy;
        }
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            pop = getSubSwarms().get(i).getPopulation();
            indy = getIndyByParticleIndexAndPopulation(pop, index);
            if (indy != null) {
                return indy;
            }
        }
        return null;
    }

    /**
     * @return particle with minimal stddev in fitness over the last 3
     * iterations
     */
    protected AbstractEAIndividual getIndyWithMinStdDev() {
        Population mainpop = getMainSwarm().getPopulation();
        if (mainpop.size() == 0) {
            return null;
        }

        double min = Double.POSITIVE_INFINITY;
        int minindex = 0;
        AbstractEAIndividual tmpIndy1;
        for (int i = 0; i < mainpop.size(); ++i) {
            tmpIndy1 = mainpop.get(i);
            Double da = (Double) ((tmpIndy1).getData(NichePSO.stdDevKey));
            if (da < min) {
                min = da;
                minindex = i;
            }
        }
        return mainpop.get(minindex);
    }

    /**
     * Inits a new Topoplot
     */
    protected void initPlotSwarm() {
        double[] a = new double[2];
        a[0] = 0.0;
        a[1] = 0.0;
        this.topoPlot = new TopoPlot("NichePSO-MainSwarm", "x", "y", a, a);
        this.topoPlot.setParams(60, 60);
        if (optimizationProblem instanceof Interface2DBorderProblem) {
            this.topoPlot.setTopology((Interface2DBorderProblem) this.optimizationProblem);
        } // draws colored plot
    }

    /**
     * Clean everything except topology colors
     */
    protected void cleanPlotSwarm() {
        // delete all previous points
        DElement[] elements = this.topoPlot.getFunctionArea().getDElements();
        int lastIndex = elements.length - 1;
        DElement last = elements[lastIndex];
        while (last instanceof DPointSet || last instanceof DPoint || last instanceof DPointIcon) {
            this.topoPlot.getFunctionArea().removeDElement(last);
//			elements = this.topoPlot.getFunctionArea().getDElements();
            lastIndex--;
            last = elements[lastIndex];
        }
    }

    /**
     * Plots the std dev of all particles in the main swarm
     */
    protected void plotAllStdDevsInMainSwarm() {
        //add further information to MainSwarm-Plot	point by point   
        InterfaceDataTypeDouble tmpIndy1;
        Population mainpop = getMainSwarm().getPopulation();
        for (int i = 0; i < mainpop.size(); ++i) {
            // add particle index to plot so it can be tracked over time 
            AbstractEAIndividual indy = mainpop.getEAIndividual(i);
            Integer index = indy.getIndividualIndex();//(Integer)indy.getData("particleIndex");
            String id = "(" + index.toString() + ") ";

            tmpIndy1 = (InterfaceDataTypeDouble) mainpop.get(i);
            DPoint point = new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]);
            Double da = (Double) (((AbstractEAIndividual) tmpIndy1).getData(NichePSO.stdDevKey));
            double d = da;
            String ds = String.format("%6.2f", d);
            DPointIcon icon = new DPointIconText(id + ds);
            ((DPointIconText) icon).setIcon(new DPointIconCircle());
            point.setIcon(icon);
            this.topoPlot.getFunctionArea().addDElement(point);
        }
    }

    /**
     * Plots only the minimal std dev for the respective particle
     */
    protected void plotMinStdDevInMainSwarm() {
        //add further information to MainSwarm-Plot	(minimal stddev) 
        InterfaceDataTypeDouble tmpIndy1;
        AbstractEAIndividual indy = getIndyWithMinStdDev();
        tmpIndy1 = (InterfaceDataTypeDouble) indy;
        Double da = (Double) ((indy).getData(NichePSO.stdDevKey));
        double min = da;
        DPoint point = new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]);
        String ds = String.format("%6.2f", min);
        DPointIcon icon = new DPointIconText(ds);
        ((DPointIconText) icon).setIcon(new DPointIconCircle());
        point.setIcon(icon);
        this.topoPlot.getFunctionArea().addDElement(point);
    }

    /**
     * @param boundary
     * Plots all std devs &lt; boundary for the respective particles
     */
    protected void plotBoundStdDevInMainSwarm(double boundary) {
        InterfaceDataTypeDouble tmpIndy1;
        Population mainpop = getMainSwarm().getPopulation();
        for (int i = 0; i < mainpop.size(); ++i) {
            // add particle index to plot so it can be tracked over time 
            AbstractEAIndividual indy = mainpop.getEAIndividual(i);
            Integer index = indy.getIndividualIndex();//(Integer)indy.getData("particleIndex");
            String id = "(" + index.toString() + ") ";

            tmpIndy1 = (InterfaceDataTypeDouble) mainpop.get(i);
            DPoint point = new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]);
            Double da = (Double) (((AbstractEAIndividual) tmpIndy1).getData(NichePSO.stdDevKey));
            double d = da;
            String ds = String.format("%6.2f", d);
            DPointIcon icon = new DPointIconText(id + ds);
            ((DPointIconText) icon).setIcon(new DPointIconCircle());
            point.setIcon(icon);
            if (d < boundary) {
                this.topoPlot.getFunctionArea().addDElement(point);
            }
        }
    }

    /**
     * @param index index of the particle
     * @param text  information to be added
     * Plots a circle around the individual and adds some information
     */
    protected void plotCircleForIndy(int index, String text) {
        AbstractEAIndividual indy = getIndyByParticleIndex(index);
        InterfaceDataTypeDouble tmpIndy1 = (InterfaceDataTypeDouble) indy;
        DPoint point = new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]);
        DPointIcon icon = new DPointIconText(text);
        ((DPointIconText) icon).setIcon(new DPointIconCircle());
        point.setIcon(icon);
        this.topoPlot.getFunctionArea().addDElement(point);
    }

    /**
     * @param indy invidual
     * @param text information to be added
     * Plots a circle around the individual and adds some information
     */
    protected void plotCircleForIndy(AbstractEAIndividual indy, String text) {
        InterfaceDataTypeDouble tmpIndy1 = (InterfaceDataTypeDouble) indy;
        DPoint point = new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]);
        DPointIcon icon = new DPointIconText(text);
        ((DPointIconText) icon).setIcon(new DPointIconCircle());
        point.setIcon(icon);
        this.topoPlot.getFunctionArea().addDElement(point);
    }

    /**
     * Cleans the previous plot and plots the mainswarm as points
     */
    protected void plotMainSwarm(boolean withIDs) {
        if (this.optimizationProblem instanceof Interface2DBorderProblem) {
            DPointSet popRep = new DPointSet();
            InterfaceDataTypeDouble tmpIndy1;

            cleanPlotSwarm();

            //draw MainSwarm
            Population mainpop = getMainSwarm().getPopulation();
            for (int i = 0; i < mainpop.size(); i++) {
                tmpIndy1 = (InterfaceDataTypeDouble) mainpop.get(i);
                popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
            }
            this.topoPlot.getFunctionArea().addDElement(popRep); // time consuming

            // identify every particle in the main swarm
            if (withIDs) {
                for (int i = 0; i < getMainSwarm().getPopulation().size(); ++i) {
                    AbstractEAIndividual currentindy = getMainSwarm().getPopulation().getEAIndividual(i);
                    int particleindex = currentindy.getIndividualIndex(); // should be unique and constant

                    AbstractEAIndividual leader = (AbstractEAIndividual) currentindy.getData("MultiSwarmType");
                    int leaderIndex = 0;
                    if (leader != null) {
                        leaderIndex = leader.getIndividualIndex();
                    }

                    if (currentindy.getData("newParticleFlag") != null) {
                        plotCircleForIndy(currentindy, particleindex + " reinit");
                        currentindy.putData("newParticleFlag", null);
                    } else {
                        String info = particleindex + " (" + leaderIndex + ")";
                        plotCircleForIndy(currentindy, info);
                    }
                }
            }

            //plotBoundStdDevInMainSwarm(MainSwarm.getDelta()+0.05);
            //plotMinStdDevInMainSwarm();
            //plotAllStdDevsInMainSwarm();
        }
    }

    protected void plotSubSwarmsWithIndizes(boolean plotActive, boolean plotInactive) {
        for (int i = 0; i < this.getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentsub = this.getSubSwarms().get(i);
            if (!currentsub.isActive() && plotInactive) {
                plotCircleForIndy(currentsub.bestIndividual, String.valueOf(i) + "[I]");
            }
            if (currentsub.isActive() && plotActive) {
                plotCircleForIndy(currentsub.bestIndividual, String.valueOf(i));
            }
        }
    }

    protected void plotAbsorptionCondition() {
        for (int i = 0; i < indytoabsorb.size(); ++i) {
            AbstractEAIndividual indy = indytoabsorb.get(i);
            int particleIndex = indy.getIndividualIndex();//((Integer)indy.getData("particleIndex")).intValue();
            plotCircleForIndy(indytoabsorb.get(i), String.valueOf(particleIndex) + " absorption");
        }
    }

    protected void plotMergingCondition() {
        for (int i = 0; i < this.borg.size(); ++i) {
            plotSwarmToMerge(borg.get(i), i);
            plotSwarmToMerge(others.get(i), i);
        }
        plotAbsorptionCondition();
    }

    protected void plotSwarmToMerge(ParticleSubSwarmOptimization swarm, int index) {
        InterfaceDataTypeDouble tmpIndy1;
        Population swarmpop = swarm.getPopulation();
        InterfaceDataTypeDouble best = (InterfaceDataTypeDouble) swarm.bestIndividual;
        DPointSet popRep = new DPointSet();

        //...draw SubSwarm as points
        for (int j = 0; j < swarmpop.size(); j++) {
            popRep.setConnected(false);
            tmpIndy1 = (InterfaceDataTypeDouble) swarmpop.get(j);
            popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
        }
        this.topoPlot.getFunctionArea().addDElement(popRep); // time consuming

        //...draw circle for best 
        if (!swarm.isActive()) {
            plotCircleForIndy((AbstractEAIndividual) best, "[I]-Merging " + String.valueOf(index));
        } else {
            plotCircleForIndy((AbstractEAIndividual) best, getMaxStdDevFromSwarmAsString(swarm) + "-Merging " + String.valueOf(index));
        }

        //...draw SubSwarm as connected lines to best
        popRep = new DPointSet();
        for (int j = 0; j < swarmpop.size(); j++) {
            tmpIndy1 = (InterfaceDataTypeDouble) swarmpop.get(j);
            popRep.setConnected(true);
            popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
            popRep.addDPoint(new DPoint(best.getDoubleData()[0], best.getDoubleData()[1]));
        }
        this.topoPlot.getFunctionArea().addDElement(popRep); // time consuming

    }

    /**
     * Plots all subswarms as connected lines to their respective best
     * individual
     */
    protected void plotSubSwarms() {
        if (this.optimizationProblem instanceof Interface2DBorderProblem) {
            //DPointSet               popRep  = new DPointSet();
            InterfaceDataTypeDouble tmpIndy1;

            //cleanPlotSubSwarms();

            // for all SubSwarms...
            for (int i = 0; i < this.getSubSwarms().size(); i++) {
                ParticleSubSwarmOptimization currentsubswarm = this.getSubSwarms().get(i);
                Population currentsubswarmpop = currentsubswarm.getPopulation();
                //InterfaceDataTypeDouble best = (InterfaceDataTypeDouble)currentsubswarmpop.getBestIndividual();
                InterfaceDataTypeDouble best = (InterfaceDataTypeDouble) currentsubswarm.bestIndividual;
                DPointSet popRep = new DPointSet();

                //...draw SubSwarm as points
                for (int j = 0; j < currentsubswarmpop.size(); j++) {
                    popRep.setConnected(false);
                    tmpIndy1 = (InterfaceDataTypeDouble) currentsubswarmpop.get(j);
                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                }
                this.topoPlot.getFunctionArea().addDElement(popRep); // time consuming

                //...draw circle for best 
                if (!currentsubswarm.isActive()) {
                    plotCircleForIndy((AbstractEAIndividual) best, "[I]");
                } else {
                    if (!getSubswarmOptimizerTemplate().isGcpso()) {
                        //plotCircleForIndy((AbstractEAIndividual)best,getMaxStdDevFromSwarmAsString(currentsubswarm));
                    }
                    if (getSubswarmOptimizerTemplate().isGcpso()) {
                        String rhoAsString = String.format("%6.3f", currentsubswarm.getRho());
                        //plotCircleForIndy((AbstractEAIndividual)best,rhoAsString);
                        if (currentsubswarm.gbestParticle != null) {
                            //plotCircleForIndy((AbstractEAIndividual)currentsubswarm.gbestParticle,"gbest");
                        }
                    }
                }

                //...draw SubSwarm as connected lines to best
                popRep = new DPointSet();
                for (int j = 0; j < currentsubswarmpop.size(); j++) {
                    //popRep.setConnected(false);
                    tmpIndy1 = (InterfaceDataTypeDouble) currentsubswarmpop.get(j);
                    //popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));

                    popRep.setConnected(true);
                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                    popRep.addDPoint(new DPoint(best.getDoubleData()[0], best.getDoubleData()[1]));
                }
                this.topoPlot.getFunctionArea().addDElement(popRep); // time consuming
            }
        } // endif
    }

    protected String getMaxStdDevFromSwarmAsString(ParticleSubSwarmOptimization swarm) {
        double max = -1;
        for (int i = 0; i < swarm.getPopulation().size(); ++i) {
            AbstractEAIndividual currentindy = swarm.getPopulation().getEAIndividual(i);
            Double da = (Double) ((currentindy).getData(NichePSO.stdDevKey));
            double d = da;
            if (d > max) {
                max = d;
            }
        }
        String ds = String.format("%6.3f", max);
        return ds;
    }

    /**
     * Plots information about merging, absorption and subswarm-creation
     */
    protected void plotAdditionalInfo() {
        // from merging
        if (mergingOccurd) {
            for (int i = 0; i < borgbest.size(); ++i) {
                plotCircleForIndy(borgbest.get(i), "merging " + i);
                plotCircleForIndy(othersbest.get(i), "merging " + i);
            }
        }

        // from absorbtion
        if (absorbtionOccurd) {
            for (int i = 0; i < indytoabsorb.size(); ++i) {
                AbstractEAIndividual indy = indytoabsorb.get(i);
                int particleIndex = indy.getIndividualIndex();
                plotCircleForIndy(indy, particleIndex + " absorbed");
            }
        }

        // from subswarm-creation
        if (creationOccurd) {
            for (int i = 0; i < indyconverged.size(); ++i) {
                int convergedIndex = indyconverged.get(i).getIndividualIndex();//((Integer)indyconverged.get(i).getData("particleIndex")).intValue();
                int convergedneighborIndex = convergedneighbor.get(i).getIndividualIndex();//((Integer)convergedneighbor.get(i).getData("particleIndex")).intValue();
                plotCircleForIndy(indyconverged.get(i), "converged " + convergedIndex);
                plotCircleForIndy(convergedneighbor.get(i), "neighbor " + convergedneighborIndex);
            }
        }

        // from deactivation and reinit
        if (deactivationOccured) {
            for (int i = 0; i < deactivatedSwarm.size(); ++i) {
                plotCircleForIndy(deactivatedSwarm.get(i).bestIndividual, "   deac");
            }
//			for (int i = 0; i < reinitedSwarm.size(); ++i){
//				plotSwarm(reinitedSwarm.get(i),"   reinit");
//			}
        }
    }

    protected void plotSwarm(ParticleSubSwarmOptimization swarm, String text) {
        for (int i = 0; i < swarm.getPopulation().size(); ++i) {
            AbstractEAIndividual currentindy = swarm.getPopulation().getEAIndividual(i);
            plotCircleForIndy(currentindy, text);
        }
    }

    /**
     * @param particleIndex
     */
    protected void plotTraceIndy(int particleIndex) {
        AbstractEAIndividual indy = getIndyByParticleIndex(particleIndex);

        // collect Information to be printed
        String text = "";
        double[] vel = (double[]) indy.getData("velocity"); //beware: curVel -> newPos -> plot (not  curVel -> plot -> newPos)
        String xv = String.format("%6.2f", vel[0]);
        String yv = String.format("%6.2f", vel[1]);
        text = xv + " " + yv;

        // mark indy and add information
        plotCircleForIndy(indy, text);
    }

    /**
     * @param indy
     * Plots the old position, new position, personal best position and
     * neighborhood best position for a given individual
     */
    protected void plotStatusForIndy(AbstractEAIndividual indy) {
        //plot newPos
        InterfaceDataTypeDouble tmpIndy1 = (InterfaceDataTypeDouble) indy;
        DPoint point = new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]);
        DPointIcon icon = new DPointIconText("");
        ((DPointIconText) icon).setIcon(new DPointIconPoint());
        point.setIcon(icon);
        this.topoPlot.getFunctionArea().addDElement(point);

        //plot oldPos
        if (!(indy.getData("oldPosition") == null)) {
            double[] oldpos = (double[]) indy.getData("oldPosition");
            point = new DPoint(oldpos[0], oldpos[1]);
            icon = new DPointIconText("");
            ((DPointIconText) icon).setIcon(new DPointIconCross());
            point.setIcon(icon);
            this.topoPlot.getFunctionArea().addDElement(point);
        }

        //plot personalBestPos
        double[] pbestpos = (double[]) indy.getData("BestPosition");
        point = new DPoint(pbestpos[0], pbestpos[1]);
        icon = new DPointIconText("");
        ((DPointIconText) icon).setIcon(new DPointIconCircle());
        point.setIcon(icon);
        this.topoPlot.getFunctionArea().addDElement(point);

        //plot neighbourBestPos
        double[] neighbourBestPos = (double[]) indy.getData("neighbourBestPos");
        point = new DPoint(neighbourBestPos[0], neighbourBestPos[1]);
        icon = new DPointIconText("");
        ((DPointIconText) icon).setIcon(new DPointIconContent());
        point.setIcon(icon);
        this.topoPlot.getFunctionArea().addDElement(point);
    }

    //    protected void saveCurrentPlotAsJPG(String filename){
//    	if (getDirForCurrentExperiment().equals("unset")) {
//    		System.out.println("saveCurrentPlotAsJPG: no directory for output specified, please use setDirForCurrentExperiment first");
//    		return;
//    	}
//    	
//		try {
//			Thread.sleep(1000); // last plot may not be saved completely...synchrosleep
//		} catch (InterruptedException e) {}
//		
//		// path
//		File outputPath = new File(getDirForCurrentExperiment()+"\\Bilder\\"); // plattform independent representation...
//		if (!outputPath.exists()) outputPath.mkdirs();
//		
//		// file name
//		String name = filename+".jpeg";
//		
//		File f = new File(outputPath,name); 
//		
//		synchronized(topoPlot){
//			try {
//				JFrame frame = topoPlot.internalFrame;
//				Robot       robot = new Robot();
//				Rectangle   area;
//				area        = frame.getBounds();
//				BufferedImage   bufferedImage   = robot.createScreenCapture(area);
//
//				// JFileChooser    fc              = new JFileChooser();
//				// if (fc.showSaveDialog(topoPlot.internalFrame) != JFileChooser.APPROVE_OPTION) return;
//				System.out.println("Name " + f.getName());
//				try {
//					FileOutputStream fos = new FileOutputStream(f);
//					BufferedOutputStream bos = new BufferedOutputStream(fos);
//					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
//					encoder.encode(bufferedImage);
//					bos.close();
//				} catch (Exception eee) {}
//
//
//			} catch (AWTException ee) {
//				ee.printStackTrace();
//			}
//		}
//		
//		try {
//			Thread.sleep(1000); // nichts l�schen vor speichern...synchrosleep
//		} catch (InterruptedException e) {}
//	}
    protected String getCurrentDateAsString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd'_'HH.mm.ss'_'E");
        String date = formatter.format(new Date());
        return date;
    }

    public static OptimizationParameters nichePSO(AbstractOptimizationProblem problem, long randSeed, InterfaceTerminator term) {
        NichePSO npso = new NichePSO();
        npso.setMainSwarmSize(75);

        return OptimizerFactory.makeParams(npso, 75, problem, randSeed, term);
    }

    /**
     * Create a Niche PSO with parameters as to Brits, Engelbrecht &amp; Bergh: A
     * Niching Particle Swarm Optimizer. SEAL 2002. Exeption: the swarm size is
     * 200 by default, because 30 (of the orig. paper) seems way too low.
     * <p>
     * The evaluation count is required currently due to the
     * generation-dependent intertness decay used by the std. variant. To alter
     * the terminator, use OptimizationParameters.setTerminator(), and mind the intertness
     * behavior of the NichePSO, which can be altered by using
     * getMainSwarm().setInertnessAging(InterfaceParameteraging)
     *
     * @param problem
     * @param randSeed
     * @param evalCnt
     * @return
     */
    public static OptimizationParameters stdNPSO(AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        return stdNPSO(null, problem, randSeed, evalCnt);
    }

    public static OptimizationParameters starNPSO(AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        return starNPSO(null, problem, randSeed, evalCnt);
    }

    /**
     * Set parameters as to Brits, Engelbrecht &amp; Bergh: A Niching Particle Swarm
     * Optimizer. SEAL 2002. Exception: the swarm size is 100 by default, because
     * 30 (of the orig. paper) seems way too low.
     *
     * @param npso  an already existing NichePSO instance or null to create a new one
     * @param problem
     * @param randSeed
     * @param evalCnt
     * @return
     * @see #stdNPSO(eva2.problems.AbstractOptimizationProblem, long, int)
     */
    public static OptimizationParameters stdNPSO(NichePSO npso, AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        if (npso == null) {
            npso = new NichePSO();
        }
        int popSize = 100;
        npso.setMainSwarmSize(popSize);
//		double avgRange = Mathematics.getAvgRange(((InterfaceDataTypeDouble)problem.getIndividualTemplate()).getDoubleRange());

        // set strategies
        npso.setDeactivationStrategy(new StandardDeactivationStrategy(0.00001));
        npso.setMergingStrategy(new StandardMergingStrategy(0.001));
        npso.setAbsorptionStrategy(new StandardAbsorptionStrategy());
        npso.setSubswarmCreationStrategy(new StandardSubswarmCreationStrategy(0.0001));

        npso.setMaxAllowedSwarmRadius(0.0001); // formally limits the swarm radius of the subswarms

        // Parameter for the mainswarm
        npso.setMainSwarmAlgoType(ParticleSwarmOptimization.PSOType.Inertness);
        npso.getMainSwarm().setPhi1(1.2);
        npso.getMainSwarm().setPhi2(0); // by default no communication in the mainswarm
        npso.setMainSwarmTopologyTag(0); // this doesnt have any effect due to no communication
        npso.setMainSwarmTopologyRange(0);
        npso.mainSwarmAlgoType = ParticleSwarmOptimization.PSOType.Inertness;
        npso.getMainSwarm().setParameterControl(new ParamAdaption[]{getDefaultInertnessAdaption()});
//		npso.getMainSwarm().setSpeedLimit(avgRange/2.);
//		npso.getMainSwarm().setCheckSpeedLimit(true);

        // parameter for the subswarms
        npso.getSubswarmOptimizerTemplate().setGcpso(true);
        npso.getSubswarmOptimizerTemplate().setRho(0.1); // on 2D Problems empirically better than default value 1
//		npso.getSubswarmOptimizerTemplate().setAlgoType(new SelectedTag("Constriction"));
        npso.getSubswarmOptimizerTemplate().setAlgoType(ParticleSwarmOptimization.PSOType.Constriction); // constriction
        npso.getSubswarmOptimizerTemplate().setConstriction(2.05, 2.05);
//		npso.getSubswarmOptimizerTemplate().setInertnessAging(new LinearParameterAging(0.7, 0.2, evalCnt/(10*popSize))); // expect shorter 

        return OptimizerFactory.makeParams(npso, popSize, problem, randSeed, new EvaluationTerminator(evalCnt));
    }

    /**
     * Set parameters as to Brits, Engelbrecht &amp; Bergh: A Niching Particle Swarm
     * Optimizer. SEAL 2002. Exception: the swarm size is 200 by default, because
     * 30 (of the orig. paper) seems way too low.
     *
     * @param npso an already existing NichePSO instance or null to create a new one
     * @param problem
     * @param randSeed
     * @param evalCnt
     * @return
     * @see #stdNPSO(eva2.problems.AbstractOptimizationProblem, long, int)
     */
    public static OptimizationParameters starNPSO(NichePSO npso, AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        starNPSO(npso, evalCnt);
        return OptimizerFactory.makeParams(npso, npso.getMainSwarmSize(), problem, randSeed, new EvaluationTerminator(evalCnt));
    }

    public static NichePSO starNPSO(NichePSO npso, int evalCnt) {
        if (npso == null) {
            npso = new NichePSO();
        }
        int popSize = 200;
        npso.setMainSwarmSize(popSize);

        // set strategies
        npso.setDeactivationStrategy(new StandardDeactivationStrategy());
        npso.setMergingStrategy(new ScatterMergingStrategy(0.001));
        npso.setAbsorptionStrategy(new EuclideanDiversityAbsorptionStrategy(0.1));// 0.1 used in "Enhancing the NichePSO" by Engelbrecht et al. 
        npso.setSubswarmCreationStrategy(new StandardSubswarmCreationStrategy(0.0001)); // from  "Enhancing the NichePSO" by Engelbrecht et al.

        npso.setMaxAllowedSwarmRadius(0.0001); // formally limits the swarm radius of the subswarms

        // Parameter for the mainswarm
        npso.getMainSwarm().setAlgoType(ParticleSwarmOptimization.PSOType.Inertness);
        npso.setMainSwarmAlgoType(ParticleSwarmOptimization.PSOType.Inertness);
        npso.getMainSwarm().setPhi1(1.2);
        npso.setMainSwarmTopologyTag(0); // this doesnt have any effect due to no communication
        npso.setMainSwarmTopologyRange(0);
        npso.mainSwarmAlgoType = ParticleSwarmOptimization.PSOType.Inertness;
        npso.getMainSwarm().setParameterControl(new ParamAdaption[]{getDefaultInertnessAdaption()});

        // parameters for the subswarms
        npso.getSubswarmOptimizerTemplate().setGcpso(true);
        npso.getSubswarmOptimizerTemplate().setRho(0.01);
        npso.getSubswarmOptimizerTemplate().setAlgoType(ParticleSwarmOptimization.PSOType.Constriction);

        npso.getSubswarmOptimizerTemplate().setConstriction(2.05, 2.05);
        return npso;
    }

    @Override
    public String[] getAdditionalDataHeader() {
        return new String[]{"mainSwarmSize", "numActSpec", "avgSpecSize", "numArchived", "archivedMedCorr", "archivedMeanDist", "mainSwarmInertness"};
    }

    @Override
    public String[] getAdditionalDataInfo() {
        return new String[]{
            "Size of the main swarm of explorers",
            "Number of sub-swarms currently active",
            "Average sub-swarm size",
            "The number of stored potential local optima",
            "The median correlation of stored solutions",
            "The mean distance of stored solutions",
            "Current inertness of the main swarm"
        };
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        int actSwarms = countActiveSubswarms();
        double avgSpSize = getAvgActiveSubSwarmSize();
        Population inactives = getSubswarmRepresentatives(true);
        double medCor = inactives.getCorrelations()[3]; // median correlation of best indies of inactive subswarms
        double meanDist = inactives.getPopulationMeasures()[0];
        return new Object[]{getMainSwarm().getPopulation().size(),
                actSwarms,
                avgSpSize,
                getNumArchived(),
                medCor,
                meanDist,
                getMainSwarm().getInertnessOrChi()};
    }

    /**
     * Return the number of archived solutions, which is the number of inactive
     * subswarms.
     *
     * @return
     */
    protected int getNumArchived() {
        return (getSubSwarms().size() - countActiveSubswarms());
    }

    /**
     * This method is necessary to allow access from the Processor.
     *
     * @return
     */
    public Object[] getParamControl() {
        List<Object> ctrlbls = ParameterControlManager.listOfControllables(this);
        ctrlbls.add(paramControl);
        return ctrlbls.toArray();
        // this works - however differently than when returning a ParameterControlManager
    }

    public ParamAdaption[] getParameterControl() {
        return paramControl.getSingleAdapters();
    }

    @Parameter(description = "You may define dynamic paramter control strategies using the parameter name.")
    public void setParameterControl(ParamAdaption[] paramControl) {
        this.paramControl.setSingleAdapters(paramControl);
    }
}