package eva2.optimization.strategies;

import eva2.OptimizerFactory;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.enums.PSOTopologyEnum;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.nichepso.deactivation.StandardDeactivationStrategy;
import eva2.optimization.operator.paramcontrol.LinearParamAdaption;
import eva2.optimization.operator.paramcontrol.ParamAdaption;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.problems.Interface2DBorderProblem;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.ToolBox;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointSet;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * The Adaptive Niching PSO(ANPSO)[1] extends the particle swarm optimizer (PSO)
 * by Kennedy and Eberhart to locate multiple optima of a multimodal objective function.
 * The Algorithm uses similar concepts as the NichePSO such as a main swarm to explore
 * the search space and subswarms to refine and represent single niches.
 * The implemented version uses the "inertness weight" PSO to train the main swarm.
 * mainSwarmInertness sets the inertia weight omega and weights the particles tendency to follow its former movement.
 * This controls exploration (favored by larger values) against exploitation (favored by smaller values).
 * mainSwarmPhi1 sets Phi1 and weights the cognitive component.
 * The term corresponds to the particles tendency to return to its personal best position.
 * mainSwarmPhi2 sets Phi2 and weights the social component.
 * The term corresonds to the particles tendency to be attracted towards its neighborhood best position.
 * A small value for mainSwarmPhi2 reduces the influence of the neighborhood and each particle tend to
 * perfom a local search on its own. This results in a high exploration ability of the main swarm and
 * potentially leads to a good amount of identified optima.
 * Larger values for mainSwarmPhi2 on the other hand might help to concentrate on fewer optima with superior fitness values.
 * <p/>
 * To avoid further parameters and the need to specify adequate values, which are often difficult to decide,
 * the following adaption mechanism is employed:
 * ANPSO adaptively determines a threshold parameter r during every generation by computing the average
 * distance a particle has to its neighbor. Subsequently, a so called s-matrix is calculated that keeps
 * track of how long any two particles are close (i.e. within r) to each other.
 * The s-matrix is used to build a niche graph that represents particles as vertices and connects all particles
 * that have been close to each other for at least two consecutive generations.
 * Particles that are connected in the niche graph form a subswarm.
 * The implemented version uses a strategy to deactivate subswarms when all containing particles converged on a solution.
 * Furthermore, different neighborhood topologies can be choosen for the main swarm.
 * In case the Multi-Swarm topology is used, the species radius is adaptively determined as well.
 * <p/>
 * [1] S. Bird and X. Li:
 * Adaptively choosing niching parameters in a PSO.
 * In: GECCO '06: Proceedings of the 8th annual conference on Genetic and evolutionary computation,
 * Seiten 3--10, New York, NY, USA, 2006. ACM
 *
 * @author aschoff, mkron
 */
public class ANPSO extends NichePSO implements InterfaceOptimizer, InterfaceAdditionalPopulationInformer, java.io.Serializable {


    /**
     * *******************************************************************************************************************
     * members
     */
    // for ANPSO it is necessary to keep an own archiv of inactive subswarms, because the standard set of
    // subswarms is completely renewed every iteration.
    public Vector<ParticleSubSwarmOptimization> inactiveSubSwarms = new Vector<ParticleSubSwarmOptimization>();

    // the s matrix keeps track of how long particles are close to each other
    int[][] s = new int[mainSwarmSize][mainSwarmSize];

    // the niche graph represents particles as vertices and connects all particles that
    // have been close to each other for at least two consecutive generations
    protected NicheGraph nicheGraph = new NicheGraph();

    // defines the minimal distance for neighboring particles at which they form a subswarm
    // (this is proposed by Bird and Lee for further investigation)
    protected double minimalR = 0;
    private double updateRadius = 0.;
    // maximum subswarm size
    private int maxInitialSubSwarmSize = 0;

    private int maxNeighborCntNicheGraph = 4; // maximum for neighborhood matrix
    private int minNeighborCntNicheGraph = 0; // minimum for neighborhood matrix
    private int neighborCntNicheGraphForEdge = 2; // number of steps req. to build an edge

    /**
     * *******************************************************************************************************************
     * ctors, clone
     */
    public ANPSO() {
//		NichePSO.stdNPSO(anpso, problem, randSeed, evalCnt);
//		NichePSO.stdNPSO((ANPSO)this, (AbstractOptimizationProblem)this.problem, 0, 1000);

        /////////// from NichePSO
//		super.initMainSwarm(); // not really necessary if init is called before optimization but this way init doesnt change the parameters of a newly constructed object
//		super.initSubswarmOptimizerTemplate();
//		setMergingStrategy(new StandardMergingStrategy(0.001));
//		setAbsorptionStrategy(new StandardAbsorptionStrategy());
//		setSubswarmCreationStrategy(new StandardSubswarmCreationStrategy(0.0001));
//		
//		setMaxAllowedSwarmRadius(0.0001); // formally limits the swarm radius of the subswarms

        // Parameter for the mainswarm
//		npso.getMainSwarm().setSpeedLimit(avgRange/2.);
//		npso.getMainSwarm().setCheckSpeedLimit(true);

        // parameter for the subswarms
//		System.out.println(BeanInspector.niceToString(getSubswarmOptimizerTemplate()));
//		getSubswarmOptimizerTemplate().setGcpso(true);
//		getSubswarmOptimizerTemplate().setRho(0.1); // on 2D Problems empirically better than default value 1
//		getSubswarmOptimizerTemplate().setAlgoType(new SelectedTag("Constriction"));
//		getSubswarmOptimizerTemplate().setConstriction(2.05, 2.05);
//		System.out.println(BeanInspector.niceToString(getSubswarmOptimizerTemplate()));

        ///////////end from NichePSO
        getMainSwarm().setPhi1(2.05);
        getMainSwarm().setPhi2(2.05);
        getMainSwarm().setInertnessOrChi(0.7298437881283576);
//		setMainSwarmInertness(new NoParameterAging(0.7298437881283576));
        setMainSwarmAlgoType(getMainSwarm().getAlgoType().setSelectedTag("Constriction")); // constriction
        setMaxInitialSubSwarmSize(0); // deactivate early reinits
        setMainSwarmTopology(PSOTopologyEnum.grid);
        setMainSwarmTopologyRange(1);
        setDeactivationStrategy(new StandardDeactivationStrategy(0.000001, 8));
        setMainSwarmSize(100);
    }

	/*public ANPSO(){
        // Parameter for the mainswarm
//		//Earlier standard settings: 
//		this.setMainSwarmAlgoType(getMainSwarm().getAlgoType().setSelectedTag("Inertness"));
//		this.mainSwarmPhi1 = 1.2;
//		this.mainSwarmPhi2 = 0.6; // ANPSO uses communication in the main swarm 
//		this.mainSwarmTopology = PSOTopologyEnum.multiSwarm; //"Multi-Swarm" favors the formation of groups in the main swarm
//		this.mainSwarmTopologyRange = 2; // range for topologies like random, grid etc. (does not affect "Multi-Swarm")
//		this.setMainSwarmInertness(new NoParameterAging(0.73)); 
		this.setMainSwarmPhi1(2.05);
		this.setMainSwarmPhi2(2.05);
		this.setMainSwarmInertness(new NoParameterAging(0.7298437881283576));
		this.setMainSwarmAlgoType(getMainSwarm().getAlgoType().setSelectedTag("Constriction")); // constriction
		this.setMaxInitialSubSwarmSize(0); // deactivate early reinits
		this.setMainSwarmTopology(PSOTopologyEnum.grid);
		this.setMainSwarmTopologyRange(1); 
		this.setDeactivationStrategy(new StandardDeactivationStrategy(0.000001, 8));

		hideHideable();
		initMainSwarm();
	}*/

    public ANPSO(int mainSwarmSize, double phi1, double phi2, PSOTopologyEnum mainSwarmTopo, int mainSwarmTopoRange, int maxInitialSubSwarmSize) {
        this();
        setMainSwarmSize(mainSwarmSize);
        getMainSwarm().setPhi1(phi1);
        getMainSwarm().setPhi2(phi2);
//		setMainSwarmPhi1(phi1);
//		setMainSwarmPhi2(phi2);
        setMainSwarmTopologyRange(mainSwarmTopoRange);
        setMainSwarmTopology(mainSwarmTopo);
        setMaxInitialSubSwarmSize(maxInitialSubSwarmSize);
    }

    /**
     * Take care that all properties which may be hidden (and currently are) send a "hide" message to the Java Bean properties.
     * This is called by PropertySheetPanel in use with the GenericObjectEditor.
     */
    @Override
    public void hideHideable() {
        // hide the following unused properties from the GUI
        GenericObjectEditor.setHideProperty(getClass(), "subswarmCreationStrategy", true);
        GenericObjectEditor.setHideProperty(getClass(), "mergingStrategy", true);
        GenericObjectEditor.setHideProperty(getClass(), "absorptionStrategy", true);
        GenericObjectEditor.setHideProperty(getClass(), "maxAllowedSwarmRadius", true);
        GenericObjectEditor.setHideProperty(getClass(), "mainSwarmTopologyRange", mainSwarmTopology == PSOTopologyEnum.multiSwarm); // "Multi-Swarm" has no topologyRange

        // population size is set via setMainSwarmSize
        GenericObjectEditor.setHideProperty(getClass(), "population", true);
//		setGOEShowProperties(getClass()); 
    }

    public ANPSO(ANPSO o) {
        super(o);
        this.inactiveSubSwarms = (Vector<ParticleSubSwarmOptimization>) o.inactiveSubSwarms.clone();
        this.s = new int[mainSwarmSize][mainSwarmSize];
        for (int i = 0; i < s.length; ++i) {
            System.arraycopy(o.s[i], 0, s[i], 0, s[i].length);
        }

        this.nicheGraph = (NicheGraph) nicheGraph.clone();
        this.minimalR = o.minimalR;
        this.minNeighborCntNicheGraph = o.minNeighborCntNicheGraph;
        this.maxNeighborCntNicheGraph = o.maxNeighborCntNicheGraph;
        this.neighborCntNicheGraphForEdge = o.neighborCntNicheGraphForEdge;
    }

    @Override
    public Object clone() {
        return (Object) new ANPSO(this);
    }

    /**
     * *******************************************************************************************************************
     * inits
     */
    @Override
    public void init() { //  MOE: wird vor Optimierung / n�chstem multirun 1x aufgerufen
        super.init();
        initMainSwarm();
        initSTo(0);
        initNicheGraph();

        inactiveSubSwarms = new Vector<ParticleSubSwarmOptimization>(); // dont want to use subswarms from old optimization run (especially not in multiruns)...
    }

    /**
     * resets all entries of the s matrix that correspond to any
     * particle in the given subswarm
     *
     * @param subswarm
     */
    private void resetSMatrixEntriesFor(ParticleSubSwarmOptimization subswarm) {
        Population pop = subswarm.getPopulation();
        for (int i = 0; i < pop.size(); ++i) {
            //Integer index = pop.getEAIndividual(i).getIndividualIndex();(Integer)pop.getEAIndividual(i).getData("particleIndex");
            resetSMatrixForIndex(pop.getEAIndividual(i).getIndividualIndex(), 0);
        }
    }

    /**
     * sets the given row and column to the given value
     * (i.e. all entries corresponding to a specific particle)
     *
     * @param index
     * @param val
     */
    private void resetSMatrixForIndex(int index, int val) {
        for (int i = 0; i < s.length; ++i) {
            for (int j = 0; j < s[i].length; ++j) {
                if (i == index || j == index) {
                    s[i][j] = val;
                }
            }
        }
    }

    /**
     * sets every entry of the s matrix to the given value
     *
     * @param val
     */
    private void initSTo(int val) {
        for (int i = 0; i < s.length; ++i) {
            for (int j = 0; j < s[i].length; ++j) {
                s[i][j] = val;
            }
        }
    }

    /**
     * this inits the "niche graph".
     * Every particle is represented as a vertex in the "niche graph".
     */
    private void initNicheGraph() {
        nicheGraph = new NicheGraph();
        Population activePop = getActivePopulation();
        for (int i = 0; i < activePop.size(); ++i) {
            //Integer index = activePop.getEAIndividual(i).getParticleIndex();//(Integer)activePop.getEAIndividual(i).getData("particleIndex");
            String vertex = "" + activePop.getEAIndividual(i).getIndividualIndex();
            nicheGraph.addVertex(vertex);
        }
    }

/**********************************************************************************************************************
 * ANPSO core	

 //	/**
 //	 * @param pop
 //	 * @param normalized use a normalized metric to compute distances?
 //	 * @return ave distance to neighbor in the given population
 //	 */
//	public double getAveDistToClosestNeighbor(Population pop, boolean normalized){
//		PhenotypeMetric metric = new PhenotypeMetric();
////		ArrayList<Double> distances = new ArrayList<Double>(pop.size());
//		double sum = 0;
//		double d=0;
//		for (int i = 0; i < pop.size(); ++i){
//			AbstractEAIndividual neighbor, indy = pop.getEAIndividual(i);
//			int neighborIndex = pop.getNeighborIndex(indy);
//			if (neighborIndex >= 0) neighbor = pop.getEAIndividual(neighborIndex);
//			else return -1;
//			if (normalized){
//				d = metric.distance(indy, neighbor);
//			} else { 
//				d = PhenotypeMetric.euclidianDistance(AbstractEAIndividual.getDoublePosition(indy),
//						AbstractEAIndividual.getDoublePosition(neighbor));
//			}
////			distances.add(d);
//			sum += d;
//		}
//		double avg = sum/(double)pop.size();
////		if (normalized && (pop.getGeneration()==1)) {
////			double var = 0;
////			for (int i=0; i<distances.size(); i++) {
////				var += Math.pow(distances.get(i)-avg, 2);
////			}
////			System.out.println("gen " + pop.getGeneration() + " dim " + ((AbstractProblemDouble)problem).getProblemDimension() +" avg " + avg + " variance " + var);
////		}
//		return avg;
//	}

//	
//	/**
//	 * @param pop
//	 * @param indy
//	 * @return closest neighbor (euclidian measure) of the given individual in the given population 
//	 */
//	private AbstractEAIndividual getNeighbor(Population pop, AbstractEAIndividual indy) {
//		if (pop.size() < 2){
//			System.out.println("getNeighbor: swarm empty or indy only particle");
//			return null;
//		}
//
//		// get the neighbor...
//		int index = -1;
//		double mindist = Double.POSITIVE_INFINITY;
//
//		for (int i = 0; i < pop.size(); ++i){ 
//			AbstractEAIndividual currentindy = pop.getEAIndividual(i);
//			if (!indy.equals(currentindy)){ // dont compare particle to itself or a copy of itself
//				double dist = getMainSwarm().distance(indy,currentindy);
//				if (dist  < mindist){ 
//					mindist = dist;
//					index = i;
//				}
//			}
//		}
//		if (index == -1){
//			System.out.println("getNeighbor: all individuals in population are equal !?");
//			return null;
//		}
//		return pop.getEAIndividual(index);
//	}

    /**
     * Builds the s matrix and the niche graph.
     * The s matrix keeps track of the number of generations
     * each particle has been close to every other particle.
     * The niche graph connects particles that have been close
     * for at least two consecutive generations.
     */
    public void updateSMatrixAndNicheGraph() {
        // init the niche graph (all particles as vertices, no edges):
        initNicheGraph();

        // compute population statistic for parameter r:
        Population metapop = getActivePopulation();
        if (metapop.size() < 2) {
            // this might happen if all but a single individual are scheduled to be reinitialized after species convergence
//			System.err.println("Warning, active population of size " + metapop.size() +", radius will not be updated...");
        } else {
            updateRadius = metapop.getAvgDistToClosestNeighbor(false, false)[0];
        } // use all particles not mainswarm particles only...
        if (isVerbose()) {
            System.out.print(" radius is " + updateRadius);
        }

        // build the s matrix and add edges to the niche graph:
        // get particles i and j (unique in all swarms)...
        AbstractEAIndividual[] sortedPop = sortActivePopByParticleIndex(); // sort once to reduce cpu-time spend for accessing members...
//		System.out.println("metapop size is " + metapop.size());
        for (int i = 0; i < s.length - 1; ++i) {
            AbstractEAIndividual indy_i = sortedPop[i];
            if (indy_i == null) {
                continue;
            } // may happen is some sub swarm was deactivated and the indies are scheduled for reinit
            for (int j = i + 1; j < s[i].length; ++j) {
                // call hotspot
                AbstractEAIndividual indy_j = sortedPop[j];
                if (indy_j == null) {
                    continue;
                } // may happen is some sub swarm was deactivated and the indies are scheduled for reinit
                if (indy_i == null || indy_j == null) {
                    System.out.println("updateSMatrixAndNicheGraph: indices problem");
                }

                // ...check if they are "close to each other"
                double dist = getMainSwarm().distance(indy_i, indy_j);
                if (dist < updateRadius || dist < minimalR) {
                    // increment entry that counts the number of generations
                    // i and j have been close to each other
                    ++s[i][j];

                    // values must not exceed 4 to allow a particle to leave a niche
                    // in case it is further than r for two consecutive steps
                    if (s[i][j] > maxNeighborCntNicheGraph) {
                        s[i][j] = maxNeighborCntNicheGraph;
                    }

                    // if i and j had been close to each other for at least two generations
                    // they are connected by an edge
                    if (s[i][j] >= neighborCntNicheGraphForEdge) {
                        String pi = "" + i;
                        String pj = "" + j;
                        nicheGraph.addEdge(pi, pj);
                    }
                } else {
                    // decrement entry that counts the number of generations
                    // i and j have been close to each other
                    --s[i][j];
                    if (s[i][j] < minNeighborCntNicheGraph) {
                        s[i][j] = minNeighborCntNicheGraph;
                    }
                }
            }
        }
    }

    /**
     * The returned array may contain null entries (for indies scheduled for reinitialization).
     *
     * @param pop
     * @return an array of references sorted according to the particle indices
     *         (i.e. returnedArray[i] = individual with individualIndex i)
     */
    protected AbstractEAIndividual[] sortActivePopByParticleIndex() {
        Population pop = getActivePopulation();
        AbstractEAIndividual[] sorted;
        if (pop.size() < mainSwarmSize) {
            int reinitSize = 0;
            if (indicesToReinit != null) {
                for (int i = 0; i < indicesToReinit.size(); i++) {
                    reinitSize += indicesToReinit.get(i).length;
                }
            }
            if (pop.size() + reinitSize == mainSwarmSize) {  // good case, extend pop size; null entries are to be tolerated.
                sorted = new AbstractEAIndividual[pop.size() + reinitSize];
            } else {
                throw new RuntimeException("Error, invalid size of active population (ANPSO.sortActivePopByParticleIndex()");
            }
        } else {
            sorted = new AbstractEAIndividual[pop.size()];
        }
        for (int i = 0; i < pop.size(); ++i) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
            if (sorted[indy.getIndividualIndex()] != null) {
                System.err.println("error in sortByParticleIndex!");
            }
            if (sorted[indy.getIndividualIndex()] != null) {
                throw new RuntimeException("Error, inconsistency in ANPSO! (index wrong)");
            }
            sorted[indy.getIndividualIndex()] = indy;
        }
        return sorted;
    }


    /**
     * uses the sets of particles as different subswarms and removes the particles from the mainswarm
     *
     * @param setOfSubswarms
     */
    public void useAsSubSwarms(Vector<Population> setOfSubswarms) {
        Vector<ParticleSubSwarmOptimization> newSubSwarms = new Vector<ParticleSubSwarmOptimization>();

        for (int i = 0; i < setOfSubswarms.size(); ++i) {
            Population pop = setOfSubswarms.get(i);
            ParticleSubSwarmOptimization subswarm = getNewSubSwarmOptimizer();
            subswarm.getPopulation().clear();
            subswarm.getPopulation().addAll(pop);
            subswarm.populationSizeHasChanged();
            newSubSwarms.add(subswarm);
            getMainSwarm().removeSubPopulation(pop, true); // the new subswarm can also come from an earlier subswarm
            getMainSwarm().populationSizeHasChanged();
        }
        if (isVerbose()) {
            System.out.println();
            for (int i = 0; i < newSubSwarms.size(); i++) {
                System.out.println("Swarm " + i + " (" + newSubSwarms.get(i).getPopulation().size() + "), best " + newSubSwarms.get(i).getBestIndividual());
            }
        }
        // add the function calls from the subswarms of the last iteration to the mainswarm before forgetting them
        // the function calls from the inactivated subswarms are transfered to the mainswarm as well
        // -> do not count them in getPopulation a second time
        int calls = 0;
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization subswarm = getSubSwarms().get(i);
            calls += subswarm.getPopulation().getFunctionCalls();
        }
        getMainSwarm().getPopulation().incrFunctionCallsBy(calls);

        this.SetSubSwarms(newSubSwarms);
    }

    /**
     * uses the population as the mainswarm
     *
     * @param pop
     */
    public void useAsMainSwarm(Population pop) {
        int generations = getMainSwarm().getPopulation().getGeneration();
        int calls = getMainSwarm().getPopulation().getFunctionCalls();
        getMainSwarm().setPopulation(pop);
        getMainSwarm().populationSizeHasChanged();
        getMainSwarm().getPopulation().setGenerationTo(generations);
        getMainSwarm().getPopulation().SetFunctionCalls(calls);
    }

    /**
     * creates subswarms from all particles that correspond to connected vertices in the niche graph.
     * Particles corresponding to unconnected vertices belong to the mainswarm.
     */
    public void createSubswarmsFromNicheGraph() {
        // get all sets of vertices that are connected in the niche graph...
        //(subSwarms = new Vector<ParticleSubSwarmOptimization>(); // too early, particles would be lost...
        List<Set<String>> connectedComps = nicheGraph.getConnectedComponents();

        Population tmpPop = new Population(), newMainPop = new Population();
        Vector<Population> setOfSubswarms = new Vector<Population>();
        boolean reinitSuperfl = true;
        boolean TRACEMTHD = false;

        // ... and use the corresponding particles to create the subswarms
        if (TRACEMTHD) {
            System.out.println("---------");
        }
        for (Set<String> connSet : connectedComps) {
            if (connSet.size() > 1) {// create niche
                Population pop = new Population(connSet.size());
                for (String indexStr : connSet) {
                    Integer index = Integer.valueOf(indexStr);
                    AbstractEAIndividual indy = getIndyByParticleIndex(index.intValue()); // may be taken from a main swarm or current subwarm
                    if (indy == null) {
                        System.err.println("createNichesFromNicheGraph problem -> getIndyByParticleIndex returned null");
                    }
                    pop.add(indy);
                }
                if (TRACEMTHD) {
                    System.out.print(" subswarm size ssize " + pop.size());
                }
                if (maxInitialSubSwarmSize > 0 && (pop.size() > maxInitialSubSwarmSize)) {
                    if (TRACEMTHD) {
                        System.out.print(" removing " + (pop.size() - maxInitialSubSwarmSize));
                    }
                    tmpPop = pop.getWorstNIndividuals(pop.size() - maxInitialSubSwarmSize, -1);
                    tmpPop.synchSize();
//					Population testPop=(Population)pop.clone();
                    pop.removeMembers(tmpPop, true);
                    if (reinitSuperfl) {
                        for (int i = 0; i < tmpPop.size(); i++) {
                            AbstractEAIndividual indy = tmpPop.getEAIndividual(i);
                            indy.init(m_Problem);
                            indy.resetFitness(Double.MAX_VALUE); // TODO this is not so nice... they should be collected in a reinit-list and inserted at the beginning of the next optimize step
                            ParticleSwarmOptimization.initIndividualDefaults(indy, 0.2);
                            ParticleSwarmOptimization.initIndividualMemory(indy);
                            ParticleSubSwarmOptimization.initSubSwarmDefaultsOf(indy);
                        }
                    }
                    newMainPop.addPopulation(tmpPop);
                    pop.synchSize();
                }
                setOfSubswarms.add(pop);
                if (TRACEMTHD) {
                    System.out.print("\nNew subswarm of size: " + pop.size());
                }
            } else { // move particles corresponding to unconnected vertices to the mainswarm
                Iterator<String> it = connSet.iterator();
                Integer index = Integer.valueOf(it.next());
                AbstractEAIndividual indy = getIndyByParticleIndex(index.intValue());
                newMainPop.add(indy);
            }
        }
//		for (int i=0; i<setOfSubswarms.size(); i++) {
//			if (!getMainSwarm().getPopulation().assertMembers(setOfSubswarms.get(i))) {
//				System.err.println("Wrong subswarm " + i);
//			}
//		}
        if (TRACEMTHD) {
            System.out.println();
        }
        newMainPop.synchSize();
        for (int i = 0; i < setOfSubswarms.size(); i++) {
            setOfSubswarms.get(i).synchSize();
        }
        useAsSubSwarms(setOfSubswarms);
        useAsMainSwarm(newMainPop);
    }

/**********************************************************************************************************************
 * Optimization
 */
    /**
     * @tested (non-Javadoc) @see javaeva.server.oa.go.Strategies.InterfaceOptimizer#optimize()
     */
    @Override
    public void optimize() {
//		System.out.println(BeanInspector.toString(getMainSwarm()));
        // main swarm:
        if (getMainSwarm().getPopulation().size() == 0) {// || mainSwarm.getPopulation().size() == 1){
            if (isVerbose()) {
                System.out.print("MainSwarm size is 0\n");
            }
            // increment the generationcount for the Terminator:
            // 1 Generation equals one optimize call including the optimization of the
            // (possibly empty) mainSwarm and all subSwarms
            getMainSwarm().getPopulation().incrGeneration();
        } else {
            getMainSwarm().optimize();
        }
        maybeReinitIndies();

        // sub swarms:
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization subswarm = getSubSwarms().get(i);
            if (subswarm.isActive()) {
                subswarm.optimize();
            }
        }

        // deactivation:
        deactivateSubSwarmsIfPossible();

        if (isVerbose()) {
            System.out.print("active swarms: " + countActiveSubswarms() + " of " + getSubSwarms().size());
        }

        // build the s matrix and the niche graph
        // these data structures represent particles that have been close to each other
        // for at least two consecutive generations
        updateSMatrixAndNicheGraph();

        // create subswarms from the particles corresponding to connected vertices in the niche graph
        createSubswarmsFromNicheGraph();

        // one might create additional subswarms from the main swarm
        // using the standard strategie from the NichePSO
        // createSubswarmIfPossible();

        // adapt the species radius of the SPSO using similar population statistics as for the radius parameter r
        if ((mainSwarm.getTopology() == PSOTopologyEnum.multiSwarm) && (mainSwarm.getMaxSubSwarmSize() > 1)) { //Multi-Swarm
            double aveDistToNeighInMain = getMainSwarm().getPopulation().getAvgDistToClosestNeighbor(true, false)[0];
            getMainSwarm().setSubSwarmRadius(aveDistToNeighInMain);
        }

        if (isVerbose()) {
            System.out.println();
        }
        firePropertyChangedEvent("NextGenerationPerformed"); // calls Listener that sth changed...

        /** plotting **********************************************************************************/
        if (isPlot()) {
            doPlot();
        }
//		System.out.println();
        /** end plotting *******************************************************************************/

        // reset flags etc for:
        // deactivation
        deactivationOccured = false;
        deactivatedSwarm = new Vector<ParticleSubSwarmOptimization>();
        //reinitedSwarm = new Vector<ParticleSubSwarmOptimization>();
        // merging
        mergingOccurd = false;
        borg = new Vector<ParticleSubSwarmOptimization>();
        others = new Vector<ParticleSubSwarmOptimization>();
        borgbest = new Vector<AbstractEAIndividual>();
        othersbest = new Vector<AbstractEAIndividual>();
        // absorbtion
        absorbtionOccurd = false;
        indytoabsorb = new Vector<AbstractEAIndividual>();
        // subswarmcreation
        creationOccurd = false;
        indyconverged = new Vector<AbstractEAIndividual>();
        convergedneighbor = new Vector<AbstractEAIndividual>();
        //clearing - deprecated
        //reinitoccurd = false;

    }


    /**
     * *******************************************************************************************************************
     * Deactivation
     */

    @Override
    protected void deactivationEventFor(ParticleSubSwarmOptimization subswarm) {
        super.deactivationEventFor(subswarm);
        resetSMatrixEntriesFor(subswarm);
        inactiveSubSwarms.add(subswarm); // ANPSO will later remove the inactive subswarm from the standard set of subswarms...
    }

/**********************************************************************************************************************
 * setter, getter
 */
    /**
     * @param size
     * @tested ps
     * sets the !initial! size of the mainswarm population
     * use this instead of getPopulation.setPopulationSize()
     */
    @Override
    public void setMainSwarmSize(int size) {
        // set member
        this.mainSwarmSize = size;
        // pass on to the mainswarm optimizer
        getMainSwarm().getPopulation().setTargetSize(size);
        // update s
        s = new int[size][size];
        initSTo(0);
        initNicheGraph();
    }

    /**
     * @param includeInactive
     * @return a population with clones from all subswarms
     */
    public Population getSubswarmMetapop(boolean includeInactive) {
        // construct a metapop with clones from all subswarms
        Population metapop = new Population();
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization currentsubswarm = getSubSwarms().get(i);
            if (includeInactive || currentsubswarm.isActive()) {
                Population currentsubswarmpop = (Population) currentsubswarm.getPopulation().clone();
                metapop.addPopulation(currentsubswarmpop);
            }
        }
        return metapop;
    }

    /**
     * @return a population consisting of copies from the mainswarm and all subswarms.
     * @tested junit
     * returns a population consisting of copies from the mainswarm and all subswarms
     * (active and inactive, so the size of this Population is not necessarily constant).
     * Especially important for the call back regarding the output file...
     * Beware: getPopulation().getPopulationSize() returns the !initial! size of the main swarm,
     * the actual size of the complete population is accessed via getPopulation().size()
     */
    @Override
    public Population getPopulation() {
        // construct a metapop with clones from the mainswarm and all subswarms
        Population metapop = (Population) getMainSwarm().getPopulation().clone();
        Population currentsubswarm = new Population();
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            currentsubswarm = (Population) getSubSwarms().get(i).getPopulation().clone();
            metapop.addPopulation(currentsubswarm);
        }
        for (int i = 0; i < inactiveSubSwarms.size(); ++i) { // in the case of ANPSO
            currentsubswarm = (Population) inactiveSubSwarms.get(i).getPopulation().clone();
            metapop.addPopulation(currentsubswarm);
        }
        // add the best pbest particle to the population
//		if (metapop.size() != 0){
//			AbstractEAIndividual hero = (AbstractEAIndividual)metapop.getEAIndividual(0).getData("PersonalBestKey");
//			for (int i = 0; i < metapop.size(); ++i){
//				AbstractEAIndividual currentPBest = (AbstractEAIndividual)metapop.getEAIndividual(i).getData("PersonalBestKey");
//				if (currentPBest.isDominating(hero)){
//					hero = currentPBest;
//				}
//			}
//			metapop.add(hero);
//		}

        // set correct number of generations
        metapop.setGenerationTo(getMainSwarm().getPopulation().getGeneration());

        // set correct number of function calls
        int calls = getMainSwarm().getPopulation().getFunctionCalls();
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            ParticleSubSwarmOptimization subswarm = getSubSwarms().get(i);
            //	if (subswarm.isActive()){
            calls += subswarm.getPopulation().getFunctionCalls();
            //	}
        }
        // calls from inactivated subswarms were transfered to the mainswarm, see useAsSubSwarms method

        metapop.SetFunctionCalls(calls);

        return metapop;
    }

    /**
     * @return array with copies of the gbest individuals
     * @tested junit
     * returns the cloned global best individuals (ie best of all time) from every subswarm
     */
    public Population getSubswarmRepresentatives() {
        //boolean includeMainSwarm = false;
        int mainSize = 0;
        //if (includeMainSwarm) mainSize = getMainSwarm().getPopulation().size();
        Population elitePop = new Population(getSubSwarms().size() + inactiveSubSwarms.size() + mainSize);
//		if (includeMainSwarm){
//			for (int i = 0; i < mainSize; ++i){
//				elite[i] = getMainSwarm().getPopulation().getEAIndividual(i);
//			}
//		}
        for (int i = 0; i < getSubSwarms().size(); ++i) {
            AbstractEAIndividual bestSS = ((ParticleSubSwarmOptimization) getSubSwarms().get(i)).getBestIndividual();
            elitePop.addIndividual((AbstractEAIndividual) ((ParticleSubSwarmOptimization) getSubSwarms().get(i)).m_BestIndividual.clone());
        }
        for (int i = 0; i < inactiveSubSwarms.size(); ++i) {
            elitePop.addIndividual((AbstractEAIndividual) ((ParticleSubSwarmOptimization) inactiveSubSwarms.get(i)).m_BestIndividual.clone());
        }
        return elitePop;
    }

    /**
     * @return descriptive string of the elite
     * @tested emp
     * returns a string that lists the global best individuals (ie best of all time) from every subswarm
     */
    public String getSubswarmRepresentativesAsString() {
        String result = "\nSubswarmRepresentatives: \n";
        Population elite = getSubswarmRepresentatives();
        for (int i = 0; i < getSubSwarms().size() + inactiveSubSwarms.size(); ++i) {
            result += elite.getEAIndividual(i).getStringRepresentation() + "\n";
        }
        //result += "\n";
        return result;
    }

    /**
     * @tested plots all subswarms as connected lines to their respective best individual
     */
    @Override
    protected void plotSubSwarms() {
        if (this.m_Problem instanceof Interface2DBorderProblem) {
            //DPointSet               popRep  = new DPointSet();
            InterfaceDataTypeDouble tmpIndy1;

            //cleanPlotSubSwarms();

            // for all inactive SubSwarms from ANPSO...
            for (int i = 0; i < this.inactiveSubSwarms.size(); i++) {
                ParticleSubSwarmOptimization currentsubswarm = this.inactiveSubSwarms.get(i);
                InterfaceDataTypeDouble best = (InterfaceDataTypeDouble) currentsubswarm.m_BestIndividual;
                plotCircleForIndy((AbstractEAIndividual) best, "[I]");
            }

            // for all SubSwarms...
            for (int i = 0; i < this.getSubSwarms().size(); i++) {
                ParticleSubSwarmOptimization currentsubswarm = this.getSubSwarms().get(i);
                Population currentsubswarmpop = (Population) currentsubswarm.getPopulation();
                //InterfaceDataTypeDouble best = (InterfaceDataTypeDouble)currentsubswarmpop.getBestIndividual();
                InterfaceDataTypeDouble best = (InterfaceDataTypeDouble) currentsubswarm.m_BestIndividual;
                DPointSet popRep = new DPointSet();

                //...draw SubSwarm as points
                for (int j = 0; j < currentsubswarmpop.size(); j++) {
                    popRep.setConnected(false);
                    tmpIndy1 = (InterfaceDataTypeDouble) currentsubswarmpop.get(j);
                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                }
                this.m_TopologySwarm.getFunctionArea().addDElement(popRep); // time consuming

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
                this.m_TopologySwarm.getFunctionArea().addDElement(popRep); // time consuming
            }
        } // endif
    }

    /**
     * @param index
     * @return inactive particles with given index
     *         (may return more than one particle for a given index because indizes are reused during deactivation
     *         and the reinitialized particle may be deactivated again...)
     * @tested method not used any more
     */
    public Vector<AbstractEAIndividual> getInactiveIndiesByParticleIndex(Integer index) {
        Vector<AbstractEAIndividual> indies = null;
        AbstractEAIndividual indy = null;
        for (int i = 0; i < inactiveSubSwarms.size(); ++i) {
            Population pop = inactiveSubSwarms.get(i).getPopulation();
            indy = getIndyByParticleIndexAndPopulation(pop, index); // fix if needed: only returns the first occurence...
            if (indy != null) {
                indies.add(indy);
            }
        }
        return indies;
    }

    /**
     * @return The name of the algorithm
     * @tested nn
     * This method will return a naming String
     */
    @Override
    public String getName() {
        return "ANPSO-" + getMainSwarmSize();
    }

//
//	public double getMinimalR() {
//		return minimalR;
//	}


    public void SetMinimalR(double minimalR) {
        this.minimalR = minimalR;
    }

    public int getMaxInitialSubSwarmSize() {
        return maxInitialSubSwarmSize;
    }

    public String maxInitialSubSwarmSizeTipText() {
        return "The maximum size of sub swarms at creation time.";
    }

    public void setMaxInitialSubSwarmSize(int maxSubSwarmSize) {
        this.maxInitialSubSwarmSize = maxSubSwarmSize;
    }

//	public void setMainSwarmPhi2(double mainSwarmPhi2) {
//		super.SetMainSwarmPhi2(mainSwarmPhi2);
//	}
//	public String mainSwarmPhi2TipText(){
//		return "weights the social component for the PSO used to train the main swarm";
//	}

//	/** This method allows you to choose the topology type.
//	 * @param s  The type.
//	 */
//	public void setMainSwarmTopology(SelectedTag s) {
//		mainSwarm.m_Topology = s;
//		this.mainSwarmTopologyTag = s.getSelectedTagID();
//		GenericObjectEditor.setHideProperty(getClass(), "mainSwarmTopologyRange", mainSwarmTopologyTag == 3); // "Multi-Swarm" has no topologyRange
//	}

    public String mainSwarmTopologyTipText() {
        return "sets the topology type used to train the main swarm";
    }

    public String mainSwarmTopologyRangeTipText() {
        return "sets the range of the neighborhood topology for the main swarm";
    }

    public static final OptimizationParameters aNichePSO(AbstractOptimizationProblem problem, long randSeed, InterfaceTerminator term) {
        ANPSO anpso = new ANPSO();
        anpso.setMainSwarmSize(75);

        return OptimizerFactory.makeParams(anpso, 75, problem, randSeed, term);
    }

    /**
     * Creates a standard ANPSO variant with default constricted PSO parameters and a grid topology.
     *
     * @param problem
     * @param randSeed
     * @param evalCnt
     * @return
     */
    public static final OptimizationParameters stdANPSO(AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        ANPSO anpso = new ANPSO();
        NichePSO.stdNPSO(anpso, problem, randSeed, evalCnt);

        anpso.getMainSwarm().setPhi1(2.05);
        anpso.getMainSwarm().setPhi2(2.05);
        anpso.getMainSwarm().setInertnessOrChi(0.7298437881283576);
        anpso.setMainSwarmAlgoType(anpso.getMainSwarm().getAlgoType().setSelectedTag("Constriction")); // constriction
        anpso.setMaxInitialSubSwarmSize(0); // deactivate early reinits
        anpso.setMainSwarmTopology(PSOTopologyEnum.grid);
        anpso.setMainSwarmTopologyRange(1);
        anpso.setDeactivationStrategy(new StandardDeactivationStrategy(0.000001, 8));

//		es gibt kein species size limit wie im orig-paper, aber sie berichten dort, dass sie für
//		höhere dimensionsn (3,4), eh keines benutzen.

        return OptimizerFactory.makeParams(anpso, anpso.getMainSwarmSize(), problem, randSeed, new EvaluationTerminator(evalCnt));
    }

    public static final OptimizationParameters starANPSO(AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        ANPSO anpso = new ANPSO();
        NichePSO.starNPSO(anpso, problem, randSeed, evalCnt);

        anpso.getMainSwarm().setParameterControl(new ParamAdaption[]{new LinearParamAdaption("inertnessOrChi", 0.7, 0.2)});
//		anpso.setMainSwarmInertness(new LinearParameterAging(0.7, 0.2, evalCnt/anpso.getMainSwarmSize()));

        anpso.setMainSwarmAlgoType(anpso.getMainSwarm().getAlgoType().setSelectedTag("Inertness"));
        anpso.getMainSwarm().setPhi1(1.2);
        anpso.getMainSwarm().setPhi2(0.6);  // ANPSO uses communication in the main swarm
        //Possible topologies are: "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random" in that order starting by 0.
        anpso.SetMainSwarmTopologyTag(3); //"Multi-Swarm" favors the formation of groups in the main swarm
        anpso.setMainSwarmTopologyRange(2); // range for topologies like random, grid etc. (does not affect "Multi-Swarm")
        anpso.setMaxInitialSubSwarmSize(0); // deactivate early reinits

//		es gibt kein species size limit wie im orig-paper, aber sie berichten dort, dass sie für
//		höhere dimensionsn (3,4), eh keines benutzen.

        return OptimizerFactory.makeParams(anpso, anpso.getMainSwarmSize(), problem, randSeed, new EvaluationTerminator(evalCnt));
    }

    public static final OptimizationParameters gmakANPSO(AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        ANPSO anpso = new ANPSO();
        NichePSO.starNPSO(anpso, problem, randSeed, evalCnt);

        anpso.getMainSwarm().setParameterControl(new ParamAdaption[]{new LinearParamAdaption("inertnessOrChi", 0.7, 0.2)});

        anpso.setMainSwarmAlgoType(anpso.getMainSwarm().getAlgoType().setSelectedTag("Inertness"));
        anpso.getMainSwarm().setPhi1(1.2);
        anpso.getMainSwarm().setPhi2(1.2);  // ANPSO uses communication in the main swarm
        //Possible topologies are: "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random" in that order starting by 0.
        anpso.SetMainSwarmTopologyTag(3); //"Multi-Swarm" favors the formation of groups in the main swarm
        anpso.setMainSwarmTopologyRange(4); // range for topologies like random, grid etc. (does not affect "Multi-Swarm")

//		es gibt kein species size limit wie im orig-paper, aber sie berichten dort, dass sie für
//		höhere dimensionsn (3,4), eh keines benutzen.

        anpso.getSubswarmOptimizerTemplate().setRho(1);
        anpso.getSubswarmOptimizerTemplate().SetRhoIncreaseFactor(2);
        anpso.getSubswarmOptimizerTemplate().SetRhoDecreaseFactor(0.5);

        return OptimizerFactory.makeParams(anpso, anpso.getMainSwarmSize(), problem, randSeed, new EvaluationTerminator(evalCnt));
    }

    /**
     * Create a grid-star-ANPSO with range 2.
     *
     * @param problem
     * @param randSeed
     * @param evalCnt
     * @return
     */
    public static final OptimizationParameters sgANPSO(AbstractOptimizationProblem problem, long randSeed, int evalCnt) {
        return starTopoANPSO(problem, randSeed, evalCnt, 1, 2);
    }

    /**
     * Create a starANPSO with a given main swarm topology.
     *
     * @param problem
     * @param randSeed
     * @param evalCnt
     * @param topology
     * @param topologyRange
     * @return
     */
    public static final OptimizationParameters starTopoANPSO(AbstractOptimizationProblem problem, long randSeed, int evalCnt, int topology, int topologyRange) {
        OptimizationParameters params = starANPSO(problem, randSeed, evalCnt);
        ((ANPSO) params.getOptimizer()).SetMainSwarmTopologyTag(topology);
        ((ANPSO) params.getOptimizer()).setMainSwarmTopologyRange(topologyRange);
        ((ANPSO) params.getOptimizer()).getMainSwarm().setInertnessOrChi(0.73);

        return params;
    }

    @Override
    public String[] getAdditionalDataHeader() {
        return ToolBox.appendArrays(super.getAdditionalDataHeader(), new String[]{"mainSwarmBestFit", "swarmRad"});
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        return ToolBox.appendArrays(super.getAdditionalDataValue(pop), new Object[]{getMainSwarm().getPopulation().getBestFitness()[0], updateRadius});
    }

    @Override
    public String[] getAdditionalDataInfo() {
        return ToolBox.appendArrays(super.getAdditionalDataInfo(), new String[]{"The best fitness within the main swarm", "The current value of the adapted swarm radius"});
    }

    @Override
    public Population getSubswarmRepresentatives(boolean onlyInactive) {

        Population representatives = super.getSubswarmRepresentatives(onlyInactive);
        // this vector does not yet contain the archived solutions
        for (int i = 0; i < inactiveSubSwarms.size(); i++) {
            representatives.add(inactiveSubSwarms.get(i).getBestIndividual());
        }
        representatives.synchSize();
        return representatives;
    }

/*
    public String[] getAdditionalDataHeader(PopulationInterface pop) {
		return new String[]{"mainSwarmSize","numActSpec","numArchived", "archivedMedCorr"};
	}

	public Object[] getAdditionalDataValue(PopulationInterface pop) {
		int actSwarms = countActiveSubswarms();
		double medCor = (getSubswarmRepresentatives(true)).getCorrelations()[3]; // median correlation of best indies of inactive subswarms
		return new Object[]{getMainSwarm().getPopulation().size(),
				actSwarms,
				(getSubSwarms().size()-actSwarms),
				medCor};
	}

*/

    /**
     * Return the median correlation of the best individuals of the given set of swarms.
     */
    private double getMedCorrelation(
            Vector<ParticleSubSwarmOptimization> swarms) {
        Population pop = new Population(swarms.size());
        for (int i = 0; i < swarms.size(); i++) {
            if (swarms.get(i) != null) {
                pop.addIndividual(swarms.get(i).getBestIndividual());
            }
        }
        return pop.getCorrelations()[3];
    }

    /**
     * Return the mean distance of the best individuals of the given set of swarms.
     *
     * @param swarms
     * @return
     */
    private double getMeanDist(
            Vector<ParticleSubSwarmOptimization> swarms) {
        Population pop = new Population(swarms.size());
        for (int i = 0; i < swarms.size(); i++) {
            if (swarms.get(i) != null) {
                pop.addIndividual(swarms.get(i).getBestIndividual());
            }
        }
        return pop.getPopulationMeasures()[0];
    }

    @Override
    protected int getNumArchived() {
        return (inactiveSubSwarms.size());
    }
}
