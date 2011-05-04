package eva2.server.go.strategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.gui.TopoPlot;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.PopulationInterface;
import eva2.server.go.enums.PSOTopologyEnum;
import eva2.server.go.enums.PostProcessMethod;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.AbstractEAIndividualComparator;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.distancemetric.EuclideanMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.paramcontrol.ParamAdaption;
import eva2.server.go.operators.paramcontrol.ParameterControlManager;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.Interface2DBorderProblem;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.problems.InterfaceProblemDouble;
import eva2.tools.EVAERROR;
import eva2.tools.SelectedTag;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointSet;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.tools.math.Jama.Matrix;


/** 
 * This implements particle swarm optimization by Kennedy and Eberhardt.
 * Works fine but is limited to real-valued genotypes and the original
 * version ignored range constraints on the decision variables. I've
 * implemented 'brakes' before an individual is updated it is checked
 * whether the new individual would violate range constraints, if so
 * the velocity vector is reduced.
 * 
 * Possible topologies are: "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random"
 * in that order starting by 0.
 * 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.10.2004
 * Time: 11:23:21
 * To change this template use File | Settings | File Templates.
 */
public class ParticleSwarmOptimization implements InterfaceOptimizer, java.io.Serializable, InterfaceAdditionalPopulationInformer {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = -149996122795669589L;
		
	protected Population                      m_Population        = new Population();
	Object[] 								sortedPop			= null;
	protected AbstractEAIndividual            m_BestIndividual = null;
	protected InterfaceOptimizationProblem    m_Problem           = new F1Problem();
	protected boolean                         m_CheckRange  = true;
	protected boolean						  checkSpeedLimit 		= false;
	protected boolean						useAlternative		= false;
	protected PSOTopologyEnum				topology = PSOTopologyEnum.grid;
	/**
	 * Defines which version of PSO is applied, classical inertness or constriction (using chi) 
	 */
	protected SelectedTag						algType;
	protected int                             m_TopologyRange     = 2;
	protected double                          m_InitialVelocity   = 0.2;
	protected double                          m_SpeedLimit        = 0.1;
	protected double                          m_Phi1              = 2.05;
	protected double                          m_Phi2              = 2.05;
	// for multi-swarm topology: radius of the swarm relative to the range
	protected double						  	m_swarmRadius		  = 0.2;
	// for multi-swarm: maximum sub swarm size. zero means unlimited
	protected int 							  	maxSubSwarmSize = 0;
	protected int								minSubSwarmSize = 2;
	protected int								treeStruct = 1;
	protected boolean 							wrapTopology = true;
//	protected boolean							doLocalSearch = false;
//	protected int 								localSearchGens=100;
//	protected int 								lsStepsPerInd=200;
	protected int								treeLevels, treeOrphans, treeLastFullLevelNodeCnt;
	protected int								dmsRegroupInterval = 10;
	private transient Vector<int[]>					dmsLinks = null;
	protected ParameterControlManager			paramControl = new ParameterControlManager();
	
	/**
	 * InertnessOrChi may contain the inertness or chi parameter depending on algoType
	 */
	protected double                          m_InertnessOrChi     = 0.73;
	protected double                          m_ReduceSpeed       = 0.8;
	private		double							reduceSpeedOnConstViolation = 0.5;
	public static final int						defaultType = 0;
	public static final int						resetType = 99;
	transient final static String				partTypeKey = "ParticleType";
	public transient final static String		partBestPosKey = "BestPosition";
	transient final static String				partBestFitKey = "BestFitness";
	public transient final static String				partVelKey = "Velocity";
	transient final static String 				multiSwTypeKey="MultiSwarmType";
	transient final static String 				multiSwSizeKey="MultiSwarmSize";
	transient final static String				indexKey="particleIndex";
	transient final static String				sortedIndexKey="sortedParticleIndex";
	transient final static String				dmsGroupIndexKey="dmsGroupIndex";

	protected String							m_Identifier = "";
	transient private InterfacePopulationChangedEventListener m_Listener;
	transient private TopoPlot					topoPlot = null;

	/// sleep time so that visual plot can be followed easier
	protected int 				sleepTime = 0;

	// for tracing the average velocity
	transient private double[] 	tracedVelocity = null;
	// parameter for exponential moving average
	private int		emaPeriods		= 0;

	// for debugging only
	transient private static boolean TRACE = false; 
	transient protected boolean               m_Show = false;
	transient protected eva2.gui.Plot      m_Plot;

	private boolean externalInitialPop = false;
	private static String lastSuccessKey = "successfulUpdate";
//	private double lsCandidateRatio=0.25;


	public ParticleSwarmOptimization() {
//		this.m_Topology = new SelectedTag( "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random" );
//		m_Topology.setSelectedTag(1);
		topology = PSOTopologyEnum.grid;
		algType = new SelectedTag("Inertness", "Constriction");
		algType.setSelectedTag(1);

		setConstriction(getPhi1(), getPhi2());
		hideHideable();
	}

	public ParticleSwarmOptimization(ParticleSwarmOptimization a) {
		topology=a.topology;
//		if (a.m_Topology != null)
//			this.m_Topology = (SelectedTag)a.m_Topology.clone();
		if (a.algType != null)
			this.algType = (SelectedTag)a.algType.clone();
		this.m_Population                   = (Population)a.m_Population.clone();
		this.m_Problem                      = a.m_Problem;
		this.m_Identifier                   = a.m_Identifier;
		this.m_InitialVelocity              = a.m_InitialVelocity;
		this.m_SpeedLimit                   = a.m_SpeedLimit;
		this.m_Phi1                         = a.m_Phi1;
		this.m_Phi2                         = a.m_Phi2;
		this.m_InertnessOrChi               = a.m_InertnessOrChi;
		this.m_TopologyRange                = a.m_TopologyRange;
		this.paramControl					= (ParameterControlManager) a.paramControl.clone();
		//this.setCheckSpeedLimit(a.isCheckSpeedLimit());
	}

	/**
	 * Constructor for most common parameters with constriction based approach.
	 * 
	 * @param popSize swarm size
	 * @param p1 the value for phi1 
	 * @param p2 the value for phi1 
	 * @param topo type of the neighbourhood topology
	 * @param topoRange range of the neighbourhood topology
	 */
	public ParticleSwarmOptimization(int popSize, double p1, double p2, PSOTopologyEnum topo, int topoRange) {
		this();
		algType.setSelectedTag(1); // set to constriction
		m_Population = new Population(popSize);
		setPhiValues(p1, p2);
		m_TopologyRange=topoRange;
		topology=topo;
	}
	
//	/**
//	 * Constructor for most common parameters with constriction based approach and local search.
//	 * 
//	 * @param popSize swarm size
//	 * @param p1 the value for phi1 
//	 * @param p2 the value for phi1 
//	 * @param topo type of the neighbourhood topology
//	 * @param topoRange range of the neighbourhood topology
//	 * @param lsEveryNGens interval of local search steps in generations
//	 * @param stepsPerInd number of local search steps per individual
//	 * @param candidateRatio ratio of population on which local search is performed
//	 */
//	public ParticleSwarmOptimization(int popSize, double p1, double p2, PSOTopologyEnum topo, int topoRange, int lsEveryNGens, int stepsPerInd, double candidateRatio) {
//		this(popSize, p1, p2, topo, topoRange);
//		setDoLocalSearch(true);
//		localSearchGens=lsEveryNGens;
//		lsStepsPerInd=stepsPerInd;
//		lsCandidateRatio = candidateRatio;
//	}
	
	public Object clone() {
		return (Object) new ParticleSwarmOptimization(this);
	}

	/**
	 * Take care that all properties which may be hidden (and currently are) send a "hide" message to the Java Bean properties.   
	 * This is called by PropertySheetPanel in use with the GenericObjectEditor.
	 */
	public void hideHideable() {
		setCheckSpeedLimit(checkSpeedLimit);
		setTopology(getTopology());
	}

	public void init() {
		if (m_Plot!= null) {
//			m_Plot.dispose();
			m_Plot = null;
		}
		if (topoPlot!= null) {
//			topoPlot.dispose();
			topoPlot = null;
		}
		tracedVelocity = null;
		if (!externalInitialPop) this.m_Problem.initPopulation(this.m_Population);
		// evaluation needs to be done here now, as its omitted if reset is false
		initDefaults(this.m_Population);
		this.evaluatePopulation(this.m_Population);
		if (m_BestIndividual == null) m_BestIndividual = m_Population.getBestEAIndividual();
		initByPopulation(null, false);
		externalInitialPop = false;
	}

	/**
	 * Set the initial random velocity vector.
	 *
	 * @param indy 	the individual to work on
	 * @param initialV initial velocity relative to the range
	 */
	public static void initIndividualDefaults(AbstractEAIndividual indy, double initialV) {
		double[] writeData;
		// init velocity
		writeData=Mathematics.randomVector(((InterfaceDataTypeDouble)indy).getDoubleData().length, 1); 
		
		//sum = Math.sqrt(sum);
		double relSpeed = Mathematics.getRelativeLength(writeData, ((InterfaceDataTypeDouble)indy).getDoubleRange());
		for (int j = 0; j < writeData.length; j++) {
			writeData[j]    = (writeData[j]/relSpeed)*initialV;
		}
		indy.putData(partTypeKey, defaultType);
		indy.putData(partVelKey, writeData);
	}

	/**
	 * Set current position and fitness as best personal position and fitness.
	 *
	 * @param indy 	the individual to work on
	 */
	protected static void initIndividualMemory(AbstractEAIndividual indy) {
		// init best fitness
		double[] tmpD        = indy.getFitness();
		double[] writeData   = new double[tmpD.length];
		System.arraycopy(tmpD, 0, writeData, 0, tmpD.length);
		indy.putData(partBestFitKey, writeData);
		// init best position
		tmpD        = ((InterfaceDataTypeDouble)indy).getDoubleData();
		writeData   = new double[tmpD.length];
		System.arraycopy(tmpD, 0, writeData, 0, tmpD.length);
		indy.putData(partBestPosKey, writeData);
	}

	/**
	 * Update the exponential moving average of the population velocity with the current population.
	 *
	 * @param population
	 */
	protected void traceEMA(Population population) {
		if (population.get(0) instanceof InterfaceDataTypeDouble) {
			double[] curAvVelAndSpeed;
			if (population.getGeneration() == 0) return;
			
			curAvVelAndSpeed = getPopulationVelSpeed(population, 3, partVelKey, partTypeKey, defaultType);
			double[][] range = ((InterfaceDataTypeDouble)population.get(0)).getDoubleRange();
			if (tracedVelocity == null) {
				tracedVelocity = new double[((InterfaceDataTypeDouble)population.get(0)).getDoubleData().length];
				for (int i=0; i<tracedVelocity.length; i++) tracedVelocity[i] = curAvVelAndSpeed[i];
			} else {
				if (population.getGeneration() < emaPeriods) {// if less than emaPeriods have passed, use larger alpha
					addMovingAverage(tracedVelocity, curAvVelAndSpeed, 2./(population.getGeneration()+1));
				} else {
					addMovingAverage(tracedVelocity, curAvVelAndSpeed, 2./(emaPeriods+1));
				}
			}
			if (m_Show) System.out.println(population.getGeneration() + " - abs avg " + curAvVelAndSpeed[curAvVelAndSpeed.length - 1] + ", vect " + Mathematics.getRelativeLength(curAvVelAndSpeed, range) + ", rel " + (curAvVelAndSpeed[curAvVelAndSpeed.length - 1]/Mathematics.getRelativeLength(curAvVelAndSpeed, range)));
		}
	}

	private void addMovingAverage(double[] speedSoFar, double[] curAvSpeed, double alpha) {
		for (int i=0; i<speedSoFar.length; i++) {
			speedSoFar[i] = (1.-alpha)*speedSoFar[i] + alpha*curAvSpeed[i];
		}

	}

	public double[] getEMASpeed() {
		return tracedVelocity;
	}

	public double getRelativeEMASpeed(double[][] range) {
		return Mathematics.getRelativeLength(getEMASpeed(), range);
	}

	/**
	 * May calculate both cumulative vectorial swarm velocity or the average absolute particle speed
	 * in the population. The mode switch may be 1 then the vectorial swarm velocity is returned; if it
	 * is 2, the average speed relative to the range is returned (double array of length one); if it is 3 both
	 * is returned in one array where the average absolute speed is in the last entry. 
	 * The velocity vector key is to be passed over. Optionally, an additional identifier is tested - they may be null.
	 * @see AbstractEAIndividual.getData(String)
	 *
	 * @param pop		the swarm population
	 * @param calcModeSwitch		mode switch
	 * @param velocityKey	String to access velocity vector within AbstractEAIndividual
	 * @param typeString	optional type string to access individual type
	 * @param requiredType optional required type identifier of the individual. 
	 * @return	double array containing the vectorial sum of particle velocities or an array containing the average absolute speed 
	 */
	public static double[] getPopulationVelSpeed(Population pop, int calcModeSwitch, final String velocityKey, final String typeString, final Object requiredType) {
		AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(0);
		if (!(indy instanceof InterfaceDataTypeDouble)) System.err.println("error, PSO needs individuals with double data!");

		double[] ret;
		double[][] range = ((InterfaceDataTypeDouble)indy).getDoubleRange();
		int retSize = 0;
		///// warning, this method uses dark magic

		boolean calcVectVelocity = ((calcModeSwitch & 1) > 0);
		boolean calcAbsSpeedAverage = ((calcModeSwitch & 2) > 0);
		if ((calcModeSwitch & 3) == 0) System.err.println("Error, switch must be 1, 2 or 3 (getPopulationVelSpeed)");

		double[] velocity       = (double[]) indy.getData(velocityKey);
		if (velocity != null) {
			if (calcVectVelocity) retSize+=velocity.length;	// entries for cumulative velocity 
			if (calcAbsSpeedAverage) retSize++;		// one entry for average absolute speed
			// return length of the array depends on what should be calculated
			ret = new double[retSize];

			double[] cumulVeloc    = new double[velocity.length];
			double avSpeed = 0.;

			for (int i=0; i<cumulVeloc.length; i++) cumulVeloc[i] = 0.;
			int indCnt = 0;

			for (int i = 0; i < pop.size(); i++) {
				indy = (AbstractEAIndividual)pop.get(i);

				if (indy.hasData(velocityKey)) {
					velocity = (double[]) (indy.getData(velocityKey));
					if (velocity != null) {
						if ((typeString == null) || (indy.getData(typeString).equals(requiredType))) {
						//if (particleHasSpeed(indy)) {
							indCnt++;
							if (calcVectVelocity) { 
								for (int j=0; j<cumulVeloc.length; j++) cumulVeloc[j] += velocity[j];
							}
							if (calcAbsSpeedAverage) {
								avSpeed += Mathematics.getRelativeLength(velocity, range);
							}
						}
					} else System.err.println("Error: Indy without velocity!! (getPopulationVelSpeed)");
				}
			}
			if (calcVectVelocity) {
				for (int j=0; j<cumulVeloc.length; j++) ret[j] = cumulVeloc[j]/((double)indCnt);
			}
			if (calcAbsSpeedAverage) {
				avSpeed /= ((double)indCnt);
				ret[ret.length - 1] = avSpeed;
			}
			return ret;
		} else {
			System.err.println("warning, no speed in particle! (getPopulationVelocity)");
			return null;
		}
		//for (int j=0; j<avSpeed.length; j++) avSpeed[j] /= velocity[j];
	}

	/**
	 * Check particle type and return true if the particle is 'standard', i.e. it has a speed property.
	 *
	 * @param indy	the individual to check
	 * @return	true if the particle has a speed property
	 */
	protected boolean particleHasSpeed(AbstractEAIndividual indy) {
		return isParticleType(indy, defaultType);
	}
	
	protected boolean isParticleTypeByIndex(int index, int type) {
    	return isParticleType((AbstractEAIndividual)m_Population.get(index), type); 
    }
    
	protected boolean isParticleType(AbstractEAIndividual indy, int type) {
    	return (((Integer)indy.getData(partTypeKey))==type);
    }

	/**
	 * Calculates the vectorial sum of the velocities of all particles in the given population.
	 *
	 * @param pop
	 * @return	vectorial sum of the particle velocities
	 */
	public double[] getPopulationVelocity(Population pop) {
		return getPopulationVelSpeed(pop, 1, partVelKey, partTypeKey, defaultType);
	}
	
	/**
	 * Calculates the average of the (range-) normed velocities of all particles in the given population.
	 *
	 * @param pop 
	 * @return average of the (range-) normed velocities
	 */
	public double getPopulationAvgNormedVelocity(Population pop) {
		return getPopulationVelSpeed(pop, 2, partVelKey, partTypeKey, defaultType)[0];
	}

	/** 
	 * This method will init the optimizer with a given population or, if pop is null,
	 * initialize the current population as if it was new.
	 * @param pop       The initial population
	 * @param reset     If true the population is reset.
	 */
	public void initByPopulation(Population pop, boolean reset) {
		if (pop != null) {
			this.m_Population = (Population)pop.clone();
			externalInitialPop  = true;
		}
		if (reset) this.m_Population.init();

		AbstractEAIndividual    indy;

		if (!defaultsDone(m_Population.getEAIndividual(0))) initDefaults(m_Population);

		if (reset) this.evaluatePopulation(this.m_Population);

		for (int i = 0; i < this.m_Population.size(); i++) {
			indy = (AbstractEAIndividual) this.m_Population.get(i);
			if (indy instanceof InterfaceDataTypeDouble) {
				initIndividualMemory(indy);
			}
		}

		this.m_BestIndividual = (AbstractEAIndividual)this.m_Population.getBestEAIndividual().clone();

		if (reset) this.firePropertyChangedEvent(Population.nextGenerationPerformed);
		
		treeLevels = 0;
		// the HPSO tree will contain layers 0...HPSOLevels, the last one is "incomplete" with only HPSOOrphans number of nodes
		if (getTopology()==PSOTopologyEnum.hpso || getTopology()==PSOTopologyEnum.tree) {
			if (m_TopologyRange<2) System.err.println("Error, tree/hpso requires topology range of at least 2!");
			else {
				while (getMaxNodes(m_TopologyRange, treeLevels) < m_Population.size()) treeLevels++;
				treeOrphans = m_Population.size()-getMaxNodes(m_TopologyRange, treeLevels-1);
				treeLastFullLevelNodeCnt = (int)Math.pow(m_TopologyRange, treeLevels-1);
			}
		}
		if (getTopology()==PSOTopologyEnum.dms) dmsLinks=regroupSwarm(m_Population, getTopologyRange());
	}

	private boolean defaultsDone(AbstractEAIndividual indy) {
		return (indy.hasData(partVelKey) && indy.hasData(indexKey));
	}

	/**
	 * Initialize individual defaults for the given population.
	 * 
	 * @param pop
	 */
	protected void initDefaults(Population pop) {
		AbstractEAIndividual indy;
		for (int i = 0; i < pop.size(); i++) {
			indy = (AbstractEAIndividual) pop.get(i);
			if (indy instanceof InterfaceDataTypeDouble) {
				if (!externalInitialPop || (!defaultsDone(indy))) initIndividualDefaults(indy, m_InitialVelocity);
			}
			indy.putData(indexKey, i);
			indy.SetIndividualIndex(i);
		}
	}
	
	/**
	 * Return the number of nodes of a complete n-ary tree with given branching factor and depth.
	 *  
	 * @param branch
	 * @param depth
	 * @return
	 */
	public int getMaxNodes(int branch, int depth) {
		int ret=(int)((Math.pow(branch, depth+1)-1)/(branch-1));
		return ret;
	}

	/** This method will evaluate the current population using the
	 * given problem.
	 * @param population The population that is to be evaluated
	 */
	protected void evaluatePopulation(Population population) {
		this.m_Problem.evaluate(population);
		population.incrGeneration();
		if (emaPeriods > 0) {
			traceEMA(population);
		}
//		AbstractEAIndividual indy = population.getBestEAIndividual();
		//System.out.println("best ind at " + indy.getStringRepresentation() + " , fit is " + indy.getFitness(0));
		//try { Thread.sleep(10); } catch(Exception e) {}
	}

	public static void dumpPop(String prefix, Population pop) {
		if (prefix!=null) System.out.println(prefix);
		for (int i=0; i<pop.size(); i++) {
			AbstractEAIndividual indy=pop.getEAIndividual(i);
			String info=getParticleInfo(indy);
			System.out.println(info);
		}
	}
	
	public static String getParticleInfo(AbstractEAIndividual indy) {
		String str = AbstractEAIndividual.getDefaultStringRepresentation(indy);
		str += " / Vel: " + BeanInspector.toString(indy.getData(partVelKey));
		str += " / BestP: " + BeanInspector.toString(indy.getData(partBestPosKey));
		str += " / BestF: " + BeanInspector.toString(indy.getData(partBestFitKey));
		str += " / PType: " + indy.getData(partTypeKey);
		return str;
	}

	/**
	 * Compare the given attractor fitness value to the one remembered by the neighbour individual
	 * if useHistoric is true, else with the current fitness of the neighbour individual. 
	 * If it is better than the attractor, overwrite the attractor with the neighbours information.
	 *
	 * @param attractorFit
	 * @param attractorPos
	 * @param neighbourIndy
	 * @param useHistoric
	 */
	protected void compareAndSetAttractor(double[] attractorFit, double[] attractorPos, AbstractEAIndividual neighbourIndy, boolean useHistoric) {
		double[] neighbourFit;
		double[] neighbourPos;
		if (useHistoric) {
			neighbourFit = (double[])neighbourIndy.getData(partBestFitKey);
			neighbourPos = (double[])neighbourIndy.getData(partBestPosKey);
		} else {
			neighbourFit = neighbourIndy.getFitness();
			neighbourPos = ((InterfaceDataTypeDouble)neighbourIndy).getDoubleData();
		}
		
		if (neighbourFit == null || attractorFit == null) {
			System.err.println("error!");
		}
//		if (fit[0] >= tmpFit[0]) { // now multi-obj.
		if (AbstractEAIndividual.isDominatingFitness(neighbourFit, attractorFit)) { // if the remembered fitness dominates the given one, overwrite the given one
			// replace best fitness and position with current best
			System.arraycopy(neighbourFit, 0, attractorFit, 0, neighbourFit.length);
			System.arraycopy(neighbourPos, 0, attractorPos, 0, neighbourPos.length);
		}
	}

	protected void resetIndividual(AbstractEAIndividual indy) {
		resetIndividual(indy, m_InitialVelocity);
		plotIndy(((InterfaceDataTypeDouble)indy).getDoubleData(), null, (Integer)indy.getData(indexKey));
	}
	
	public static void resetIndividual(AbstractEAIndividual indy, double initialV) {
		if (indy instanceof InterfaceDataTypeDouble) {
			indy.setParents(null);
			indy.defaultInit(null);
			indy.putData(partTypeKey, defaultType); // turn into default type
			initIndividualDefaults(indy, initialV);
			initIndividualMemory(indy);
		} else System.err.println("error, double valued individuals required for PSO");
	}
	
	/** This method will update a given individual
	 * according to the PSO method
	 * @param index      The individual to update.
	 * @param pop       The current population.
	 * @param best      The best individual found so far.
	 */
	protected void updateIndividual(int index, AbstractEAIndividual indy, Population pop) {
		if (indy instanceof InterfaceDataTypeDouble) {
			int type=(Integer)indy.getData(partTypeKey);
			switch (type) {
			case resetType:
				resetIndividual(indy);
				break;
			case defaultType:
				defaultIndividualUpdate(index, indy, pop);
				break;
			default: System.err.println("particle type " + type + " unknown!"); break;
			}
		} else {
			throw new RuntimeException("Could not perform PSO update, because individual is not instance of InterfaceESIndividual!");
		}
	}
	
	protected void defaultIndividualUpdate(int index, AbstractEAIndividual indy, Population pop) {
		InterfaceDataTypeDouble endy = (InterfaceDataTypeDouble) indy;
		
		indy.putData(partTypeKey, defaultType);
		// default update
		double[] personalBestPos   = (double[]) indy.getData(partBestPosKey);
		double[] velocity       = (double[]) indy.getData(partVelKey);
		double[] curPosition    = endy.getDoubleData();
		double[][]  range   = endy.getDoubleRange();

		// search for the local best position
		double[] neighbourBestPos = findNeighbourhoodOptimum(index, pop);

		// now update the velocity
		double[] curVelocity = updateVelocity(index, velocity, personalBestPos, curPosition, neighbourBestPos, range);

		// check the speed limit
		if (checkSpeedLimit) enforceSpeedLimit(curVelocity, range, getSpeedLimit(index));

		// enforce range constraints if necessary
		if (m_CheckRange) ensureConstraints(curPosition, curVelocity, range); 

		plotIndy(curPosition, curVelocity, (Integer)indy.getData(indexKey));
		// finally update the position
		updatePosition(indy, curVelocity, curPosition, range);

		resetFitness(indy);
	}

	protected void plotIndy(double[] curPosition, double[] curVelocity, int index) {
		if (this.m_Show) {
			if (curVelocity == null) {
				this.m_Plot.setUnconnectedPoint(curPosition[0], curPosition[1], index);
			} else {
				this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index);
				this.m_Plot.setConnectedPoint(curPosition[0] + curVelocity[0], curPosition[1] + curVelocity[1], index);
			}

//						m_Plot = null; 
//						show();
//						if (pop.getGeneration()%15 == 0) {
//							this.m_Plot.clearAll();
//							m_Plot.setUnconnectedPoint(-10, -10, 0);
//							m_Plot.setUnconnectedPoint(10, 10, 0);
//						}
			
//			if (index != 0) return;
//			double[] bestPosition = (double[])m_BestIndividual.getData(partBestPosKey);
//			double[] localBestPos = findNeighbourhoodOptimum(index, m_Population);
//			this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index+1);
//			this.m_Plot.setConnectedPoint(curPosition[0] + curVelocity[0], curPosition[1] + curVelocity[1], index+1);
//			this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index+2);
//			this.m_Plot.setConnectedPoint(bestPosition[0], bestPosition[1], index+2);
//			this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index+3);
//			this.m_Plot.setConnectedPoint(localBestPos[0], localBestPos[1], index+3);

			//                this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index+1);
			//                this.m_Plot.setConnectedPoint(localBestPosition[0], localBestPosition[1], index+1);
			//                this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index+1);
			//                this.m_Plot.setConnectedPoint(bestPosition[0], bestPosition[1], index+1);
			//                this.m_Plot.setUnconnectedPoint(curPosition[0], curPosition[1], 100*index+1);
		}
	}
	
	/**
	 * Loop the population and update each individual props if indicated by isIndividualToUpdate.
	 *
	 * @param pop	the population to work on
	 */
	protected void updateSwarmMemory(Population pop) {
		for (int i=0; i<pop.size(); i++) {	
			AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(i);
			if (isIndividualToUpdate(indy)) {
				updateIndProps(indy, indy);
				indy.putData(lastSuccessKey , indy.getData(partVelKey));
//				System.err.println("updated " + i + " - "+ getParticleInfo(indy));
			} else indy.putData(lastSuccessKey, null);
		}
	}

	/**
	 * Reset the fitness of an individual the "hard" way.
	 *
	 * @param indy
	 */
	protected void resetFitness(AbstractEAIndividual indy) {
//		double[] fit = new double[1];
//		fit[0] = 0;
//		indy.SetFitness(fit);
		indy.resetFitness(0);
		indy.resetConstraintViolation();
	}

	/**
	 * Write current fitness and position as best fitness and position into individual properties.
	 *
	 * @param srcIndy	the individual to update
	 */
	protected void updateIndProps(AbstractEAIndividual trgIndy, AbstractEAIndividual srcIndy) {
		trgIndy.putData(partBestFitKey, srcIndy.getFitness().clone());
		trgIndy.putData(partBestPosKey, ((InterfaceDataTypeDouble)srcIndy).getDoubleData());
	}

	/**
	 * Return true if the given individual requires a property update, i.e. the current fitness is better than the best one
	 * seen so far.
	 *
	 * @param indy
	 * @return true if the given individual requires a property update
	 */
	protected boolean isIndividualToUpdate(AbstractEAIndividual indy) {
		double[] bestFitness    = (double[]) indy.getData(partBestFitKey);
		return (AbstractEAIndividual.isDominatingFitnessNotEqual(indy.getFitness(), bestFitness));
	}

	/**
	 * Main velocity update step.
	 *
	 * @param index
	 * @param lastVelocity
	 * @param personalBestPos
	 * @param curPosition
	 * @param neighbourBestPos
	 * @param range
	 * @return
	 */
	protected double[] updateVelocity(int index, double[] lastVelocity, double[] personalBestPos, double[] curPosition, double[] neighbourBestPos, double[][] range) {
//		for (int i = 0; i < lastVelocity.length; i++) {
//			socCogn[i] = (personalBestPos[i]-curPosition[i]);
//			neiDiff[i] = (neighbourBestPos[i]-curPosition[i]);
//		}
//		
//		System.out.println("-- len bef " + vecLen(socCogn));
//		rotateAllAxes(socCogn, .5, false);
//		System.out.println("-- len aft " + vecLen(socCogn));
//		rotateAllAxes(neiDiff, .5, false);
//		// TODO!!!
//		if (algType.getSelectedTag().getID()==1) chi=m_InertnessOrChi;
//		else chi = 1.;
//		
//		double scaleCog = this.m_Phi1*chi*RNG.randomDouble(0,1);
//		double scaleNei = this.m_Phi2*chi*RNG.randomDouble(0,1);
//		
//		
//		for (int i=0; i<lastVelocity.length; i++) {
//			curVelocity[i]  = this.m_InertnessOrChi * lastVelocity[i];
//			curVelocity[i]  += scaleCog*socCogn[i];
//			curVelocity[i]  += scaleNei*neiDiff[i];
//		}
		
//		for (int i = 0; i < lastVelocity.length; i++) {
//			// the component from the old velocity
//			curVelocity[i]  = this.m_InertnessOrChi * lastVelocity[i];
//			if (algType.getSelectedTag().getID()==1) chi=m_InertnessOrChi;
//			else chi = 1.;
//			// the component from the cognition model
//			//curVelocity[i]  += this.m_Phi1*chi*RNG.randomDouble(0,1)*(personalBestPos[i]-curPosition[i]);
//			double dir,diff;
//			dir = (personalBestPos[i] < curPosition[i]) ? -1 : 1;
//			diff = Math.abs((personalBestPos[i]-curPosition[i]));
//			curVelocity[i]  += this.m_Phi1*chi*.5*dir*Math.max(diff, .1);
//			// the component from the social model
//			//curVelocity[i]  += this.m_Phi2*chi*RNG.randomDouble(0,1)*(neighbourBestPos[i]-curPosition[i]);
//			dir = (neighbourBestPos[i]< curPosition[i]) ? -1 : 1;
//			diff = Math.abs((neighbourBestPos[i]-curPosition[i]));
//			curVelocity[i]  += this.m_Phi2*chi*.5*dir*Math.max(diff, .1);
//		}
		double[] accel, curVelocity    = new double[lastVelocity.length];
		
		if (useAlternative) {
			accel	= getAccelerationAlternative(index, personalBestPos, neighbourBestPos, curPosition, range);
		} else {
			accel	= getAcceleration(personalBestPos, neighbourBestPos, curPosition, range);
		}
		
		//System.out.println("accel is " + getVecNorm(accel));
		
		for (int i = 0; i < lastVelocity.length; i++) {
			curVelocity[i]  = this.m_InertnessOrChi * lastVelocity[i];
			curVelocity[i]  += accel[i];
		}
		return curVelocity;
	}

	protected double[] getAcceleration(double[] personalBestPos, double[] neighbourBestPos, double[] curPosition, double[][] range) {
		double[] accel = new double[curPosition.length];
		double chi;
//		double r1, r2;
//		r1=RNG.randomDouble(0,1);
//		r2=RNG.randomDouble(0,1);
		for (int i = 0; i < personalBestPos.length; i++) {
			// the component from the old velocity
			accel[i]  = 0;
			if (algType.getSelectedTag().getID()==1) chi = m_InertnessOrChi;
			else chi = 1.;
			// the component from the cognition model
			accel[i]  = this.m_Phi1*chi*RNG.randomDouble(0,1)*(personalBestPos[i]-curPosition[i]);
			// the component from the social model
			accel[i]  += this.m_Phi2*chi*RNG.randomDouble(0,1)*(neighbourBestPos[i]-curPosition[i]);
		}
		return accel;
	}
	
	protected double[] getAccelerationAlternative(int index, double[] personalBestPos, double[] neighbourBestPos, double[] curPosition, double[][] range) {
		double[] accel    = getAcceleration(personalBestPos, neighbourBestPos, curPosition, range);
		double[] successfulVel = getSuccessfulVel(index);
//		double succW = 0.5;
		if (successfulVel!=null) {
			Mathematics.vvAdd(accel, successfulVel, accel);
			Mathematics.svMult(0.5, accel, accel);
		}
		return accel;
	}
	
	private double[] getSuccessfulVel(int index) {
		if (true) {
			return (double[])m_Population.getEAIndividual(index).getData(lastSuccessKey);
		} else { // random one
		ArrayList<Integer> successes = new ArrayList<Integer>();
		for (int i = 0; i < this.m_Population.size(); i++) {
			double[] succVel = (double[])m_Population.getEAIndividual(i).getData(lastSuccessKey);
			if (succVel!=null) successes.add(new Integer(i));
		}
		if (successes.size()>0) {
			int i = successes.get(RNG.randomInt(successes.size()));
			return (double[])m_Population.getEAIndividual(i).getData(lastSuccessKey);
		} else return null;
		}
	}

//	protected double[] getAccelerationAlternative(double[] personalBestPos, double[] neighbourBestPos, double[] curPosition, double[][] range) {
//		double[] accel    = new double[curPosition.length];
//		double chi;
//
//		if (algType.getSelectedTag().getID()==1) chi=m_InertnessOrChi;
//		else chi = 1.;
//
//		boolean rotatedVect=false;
//		if (rotatedVect) {
//			Matrix cogVecB = new Matrix(curPosition.length, 1);
//			Matrix socVecB = new Matrix(curPosition.length, 1);
//			for (int i = 0; i < personalBestPos.length; i++) {
//				cogVecB.set(i, 0, (personalBestPos[i]-curPosition[i]));
//				socVecB.set(i, 0, (neighbourBestPos[i]-curPosition[i]));
//			}
//			Matrix cogRandB = getOrientedGaussianRandomVectorB(cogVecB, 5);
//			Matrix socRandB = getOrientedGaussianRandomVectorB(socVecB, 5);
//
//			for (int i = 0; i < curPosition.length; i++) {
//				// the component from the cognition model
//				//			accel[i]  = this.m_Phi1*chi*/*RNG.randomDouble(0,1)**/cogRand.getElement(i);
//				accel[i]  = this.m_Phi1*chi*/*RNG.randomDouble(0,1)**/cogRandB.get(i,0);
//				// the component from the social model
//				//			accel[i]  += this.m_Phi2*chi*/*RNG.randomDouble(0,1)**/socRand.getElement(i);
//				accel[i]  += this.m_Phi2*chi*/*RNG.randomDouble(0,1)**/socRandB.get(i,0);
//			}
//			return accel;
//		} else {
//			double sPB = RNG.randomDouble();
//			double sNB = 1.-sPB;
//			double[] mean= personalBestPos.clone();
//			Mathematics.svMult(sPB, mean);
//			Mathematics.svvAddScaled(sNB, neighbourBestPos, mean, mean); // middle position
//			double stddev = chi*EuclideanMetric.euclideanDistance(personalBestPos, neighbourBestPos)/3.; // std.dev is one third of the distance
//			double[] gausRnd = getGaussianVector(mean, stddev);
//			for (int i=0; i<accel.length; i++) {
//				accel[i] = (gausRnd[i] - curPosition[i]); 
//			}
//			return accel;
//		}
//	}
	
//	public static void main(String[] args) {
//		ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
//		GVector tmp, vec = new GVector(5);
//		GVector vecSum = new GVector(5);
//		vec.setElement(1,3);
//		vec.setElement(2,3);
//		
//		int max=10000;
//		for (int i=0; i<max; i++) {
//			tmp = pso.getOrientedGaussianRandomVector(vec, 5.);
//			System.out.print(tmp.toString());
//			System.out.println(" " + tmp.norm());
//			//vecSum.add(tmp);
//		}
//		
//		for (int i=0; i<max; i++) {
//			tmp = pso.getOrientedGaussianRandomVector(vec, 5.);
//			System.out.print(tmp.toString());
//			System.out.println(" " + tmp.norm());
//			//vecSum.add(tmp);
//		}
//		//vecSum.normalize();
//		//System.out.println(vec.toString() + " -> " + vecSum.toString());
//	}
	
	/**
	 * Return a random vector after a gaussian distribution oriented along dir, meaning that
	 * variance is len(dir) along dir and len(dir)/scale in any other direction.
	 * If dir is a null-vector, having neither direction nor length, a null vector is returned.
	 * 
	 * @param dir
	 * @return
	 */
	protected Matrix getOrientedGaussianRandomVectorB(Matrix dir, double scale) {
		double len = dir.norm2();
		int dim = dir.getRowDimension();

		Matrix resVec = new Matrix(dim, 1);
		Matrix randVec = new Matrix(dim, 1);
		
		if (len > 0) {
			// initialize random vector
//			randVec.set(0, 0, len/2.+RNG.gaussianDouble(len/2));
//			for (int i=1; i<dim; i++) {
//				randVec.set(i, 0, RNG.gaussianDouble(len/(scale*2)));
//			}
			randVec.set(0, 0, project(0, len, len/2+RNG.gaussianDouble(len/2)));
			for (int i=1; i<dim; i++) {
				randVec.set(i, 0, project(-len/2, len/2, RNG.gaussianDouble(len/(scale*2))));
			}
			Matrix rotation = Mathematics.getRotationMatrix(dir);
			rotation = rotation.transpose();
			//printMatrix(rotation);
			resVec = rotation.times(randVec);
		}
		return resVec;
	}
	
	protected double[] getGaussianVector(double[] mean, double dev) {
		double[] res = new double[mean.length];
		RNG.gaussianVector(dev, res, false);
		Mathematics.vvAdd(mean, res, res);
		return res;
	}
	
	public static double project(double min, double max, double val) {
		if (val < min) return min;
		else if (val > max) return max;
		else return val;
	}

	protected void printMatrix(Matrix M) {
		for (int i=0; i<M.getRowDimension(); i++) {
			for (int j=0; j<M.getColumnDimension(); j++) System.out.print(" " + M.get(i, j));
			System.out.println("");
		}
	}
	
	/**
	 * In the topology range for the given index, find the best stored individual and return its position.
	 *
	 * @param index		index of the individual for which to check
	 * @param pop		the current swarm
	 * @param best		the currently best individual 
	 * @return	a copy of the position of the best remembered individual in the neigbourhood
	 */
	protected double[] findNeighbourhoodOptimum(int index, Population pop) {
		double[] localBestPosition = null;
		double[] localBestFitness = null;
		int                     tmpIndex;
		AbstractEAIndividual bestIndy, indy = pop.getEAIndividual(index);
		boolean useHistoric = true;
		int sortedIndex=-1;
		int k;

		localBestFitness        = ((double[])(indy).getData(partBestFitKey)).clone();
		localBestPosition       = ((double[])(indy).getData(partBestPosKey)).clone();
		if (useHistoric) {
			bestIndy = m_BestIndividual;
		} else {
			bestIndy = pop.getBestEAIndividual();
		}
		
		switch (topology) {
		case linear: 
			// linear
			for (int x = -this.m_TopologyRange; x <= this.m_TopologyRange; x++) {
				if (wrapTopology) tmpIndex = (index + x + pop.size()) % pop.size();
				else tmpIndex = index + x;
				
				if ((x != 0) && (tmpIndex >= 0) && (tmpIndex < pop.size())) {
					this.compareAndSetAttractor(localBestFitness, localBestPosition, (AbstractEAIndividual)pop.get(tmpIndex), useHistoric);
				}
			}
			break;
		case grid: 
			// grid
			int     corner = 1+(int)Math.sqrt(pop.size());
			for (int x = -this.m_TopologyRange; x <= this.m_TopologyRange; x++) {
				for (int y = -this.m_TopologyRange; y <= this.m_TopologyRange; y++) {
					tmpIndex = index + x + (y*corner);
					if (wrapTopology) tmpIndex=(tmpIndex + pop.size())% pop.size(); // wrap the grid toroidal
					if ((x != index) && (tmpIndex >= 0) && (tmpIndex < pop.size())) {
						this.compareAndSetAttractor(localBestFitness, localBestPosition, (AbstractEAIndividual)pop.get(tmpIndex), useHistoric);
					}
				}
			}
			break;
		case star: 
			// star: only compare to the absolutely best
			this.compareAndSetAttractor(localBestFitness, localBestPosition, bestIndy, useHistoric);
			break;
		case multiSwarm:
			// self-organised multi-swarms
			AbstractEAIndividual leader = (AbstractEAIndividual)indy.getData(multiSwTypeKey);
			if (leader != null) {	// refer to position of leader, this may be the individual itself
				if ((leader == indy) && ((Integer)indy.getData(multiSwSizeKey) < minSubSwarmSize)) {
					// swarm too small
					this.compareAndSetAttractor(localBestFitness, localBestPosition, bestIndy, useHistoric);
					//System.out.println("swarm too small.. using global attractor");
				} else {
//					if (!Arrays.equals(((InterfaceESIndividual)leader).getDGenotype(), ((InterfaceESIndividual)bestIndy).getDGenotype())) {
//						System.out.println("leader is different from best");
//					}
					this.compareAndSetAttractor(localBestFitness, localBestPosition, leader, false);
				}
			} else {
				// TODO handle this?
				System.err.println("no leader present!");
			}
			break;
		case tree: // Sorted Tree
			sortedIndex = (Integer)((AbstractEAIndividual)sortedPop[index]).getData(sortedIndexKey);

			if (sortedIndex>0) {	// its found and its not the root. root has no parent to check for
				k = getParentIndex(m_TopologyRange, sortedIndex, pop.size());
				compareAndSetAttractor(localBestFitness, localBestPosition, (AbstractEAIndividual)sortedPop[k], useHistoric);
			}
			if (treeStruct == 1) { // loop all children
				if (isComplete(sortedIndex, pop.size())) { // the node has full degree
					k = m_TopologyRange*sortedIndex+1; // this is the offset of the nodes children 
					for (int i=0; i<m_TopologyRange; i++) {
						compareAndSetAttractor(localBestFitness, localBestPosition, (AbstractEAIndividual)sortedPop[k+i], useHistoric);
					}
				} else if (isIncomplete(sortedIndex, pop.size())) { // the node does not have full degree but might have orphans
					int numOrphs = numOrphans(sortedIndex, pop.size());
					if (numOrphs > 0) {
						k = indexOfFirstOrphan(sortedIndex, pop.size());
						for (int i=0; i<numOrphs; i++) {
							compareAndSetAttractor(localBestFitness, localBestPosition, (AbstractEAIndividual)sortedPop[k], useHistoric);
							k += treeLastFullLevelNodeCnt; // hop to next (possible) orphan index
						}
					}
				}
				// this was the binary variant
//				k = (2*sortedIndex+1);
//				if (k < pop.size()) {
//					compareAndSet(localBestFitness, localBestPosition, (AbstractEAIndividual)sortedPop[k], useHistoric);
//					k++;
//					if (k < pop.size()) compareAndSet(localBestFitness, localBestPosition, (AbstractEAIndividual)sortedPop[k], useHistoric);
//				}
			}
			break;
		case hpso: // Hierarchical PSO
			if (index>=0) {
				k = getParentIndex(m_TopologyRange, index, pop.size());
//				compareAndSet(localBestFitness, localBestPosition, (AbstractEAIndividual)pop.get(k), useHistoric);
				indy = (AbstractEAIndividual)pop.get(k);
				System.arraycopy((double[])indy.getData(partBestFitKey), 0, localBestFitness, 0, localBestFitness.length);
				System.arraycopy((double[])indy.getData(partBestPosKey), 0, localBestPosition, 0, localBestPosition.length);

			}
			break;
		case random: // m_TopologyRange random informants, may be the same several times
			for (int i=0; i<m_TopologyRange; i++) {
				// select random informant
				indy = (AbstractEAIndividual)pop.get(RNG.randomInt(0, pop.size()-1));
				// set local values
				compareAndSetAttractor(localBestFitness, localBestPosition, indy, useHistoric);
			}
			break;
		case dms: 
			int groupIndex = (Integer)pop.getEAIndividual(index).getData(dmsGroupIndexKey);
			int[] groupLinks = dmsLinks.get(groupIndex);
			for (int i=0; i<groupLinks.length; i++) {
				if (groupLinks[i]!=index) {
					// 	select informant
					indy = pop.getEAIndividual(groupLinks[i]);
					// 	set local values
					compareAndSetAttractor(localBestFitness, localBestPosition, indy, useHistoric);
				}
			}
			break;
		}
		return localBestPosition;
	}
	
	/**
	 * Calculate a the parent index to a given index in a tree structure. The tree
	 * is complete except the last level, which is filled by assigning each parent
	 * of the last full level an orphan from left to right, so that no two parents
	 * differ in degree by more than one.   
	 * 
	 * @param branch
	 * @param index
	 * @param popSize
	 * @return
	 */
	protected int getParentIndex(int branch, int index, int popSize) {
		int k;
		if (isOrphan(index, popSize)) {
			k = popSize-treeOrphans-treeLastFullLevelNodeCnt;
			k += ((index - popSize + treeOrphans) % treeLastFullLevelNodeCnt); // index of the parent
		} else k = (index-1)/branch;
		return k;
	}
	
	protected boolean isOrphan(int index, int popSize) {
		return (index >= popSize - treeOrphans);
	}
	
	protected boolean isComplete(int index, int popSize) {
		return (index < popSize-treeOrphans-treeLastFullLevelNodeCnt);
	}
	
	protected boolean isIncomplete(int index, int popSize) {
		return ((index < popSize-treeOrphans) && (index >= popSize-treeOrphans-treeLastFullLevelNodeCnt));
	}
	
	/**
	 * Calculate the number of child orphans for tree nodes in the last internal layer (which is not complete,
	 * in the sense the nodes do not have full degree). Returns -1 if the indexed node is an orphan itself, 
	 * or it is within a complete layer or if the index is out of range.
	 *  
	 * @param index
	 * @param popSize
	 * @return
	 */
	protected int numOrphans(int index, int popSize) {
		int k;
		if (isIncomplete(index, popSize)) {
			k = treeOrphans / treeLastFullLevelNodeCnt;
			// if the index lies within orphans modulo lastFullCnt starting from lastFullLevel offset, there is one more child node
			if ((index - (popSize-treeOrphans-treeLastFullLevelNodeCnt)) >= (treeOrphans % treeLastFullLevelNodeCnt)) return k;
			else return k+1;
		} else return -1;
	}
	
	protected int indexOfFirstOrphan(int index, int popSize) {
		if (isIncomplete(index, popSize)) {
			int k = popSize - treeOrphans + (index - (popSize - treeOrphans - treeLastFullLevelNodeCnt));
			return k;
		} else return -1;
	}
	
	/**
	 * Update the given individuals position with given speed and position, maybe perform checkBounds.
	 * Remember to reset the fitness and constraint violation of the individual.
	 * 
	 * @param indy
	 * @param curVelocity
	 * @param curPosition
	 * @param range
	 */
	protected void updatePosition(AbstractEAIndividual indy, double[] curVelocity, double[] curPosition, double[][] range) {
		double[] newPosition    = new double[curPosition.length];

		for (int i = 0; i < curPosition.length; i++) {
			newPosition[i] = curPosition[i] + curVelocity[i];
		}
		if (m_CheckRange && isOutOfRange(newPosition, range)) {
			System.err.println("error, individual violates constraints!");
		}

		// finally set the new position and the current velocity
		if (indy instanceof InterfaceDataTypeDouble) ((InterfaceDataTypeDouble)indy).SetDoubleGenotype(newPosition);
		else {
			((InterfaceDataTypeDouble) indy).SetDoubleGenotype(newPosition); // WARNING, this does a checkBounds in any case!
			if (!m_CheckRange) System.err.println("warning, checkbounds will be forced by InterfaceESIndividual!");
		}

		indy.putData(partVelKey, curVelocity);
//		((InterfaceESIndividual) indy).SetDGenotype(newPosition);
	}

	/**
	 * Test a position for range violation efficiently.
	 *
	 * @param pos	the position vector to test
	 * @param range	the range array
	 * @return	true, if pos violoates range, else false
	 */
	protected boolean isOutOfRange(double[] pos, double[][] range) {
		boolean violatesRange = false;
		for (int i = 0; i < pos.length; i++) {
			if (!violatesRange) {
				violatesRange = (pos[i] < range[i][0]) || (pos[i] > range[i][1]);
			} else break;
		}
		return violatesRange;
	}

	/**
	 * Enforce the speed limit by repeatedly multiplying with the reduce-speed factor.
	 * Requires the range array because the speed is handled relative to the range.
	 *
	 * @param curVelocity	the velocity vector to be modified
	 * @param range			the range array
	 * @param speedLim		the speed limit relative to the range
	 */
	protected void enforceSpeedLimit(double[] curVelocity, double[][] range, double speedLim) {
		while (Mathematics.getRelativeLength(curVelocity, range) > speedLim) {
			for (int i = 0; i < curVelocity.length; i++) {
				curVelocity[i] *= this.m_ReduceSpeed;
			}
		} 
	}

	/**
	 * Makes sure that the future position (after adding velocity to pos) remains inside the
	 * range array. Therefore, the velocity may be repeatedly multiplied by reduceSpeedOnConstViolation.
	 *
	 * @param pos		the current particle position
	 * @param velocity	the current particle velocity to be modified
	 * @param range		the range array
	 */
	protected void ensureConstraints(double[] pos, double[] velocity, double[][] range) {
		double[] newPos    = new double[pos.length];
		for (int i = 0; i < pos.length; i++) newPos[i] = pos[i] + velocity[i];
		if (isOutOfRange(pos, range)) {
			System.err.println("warning, ensureConstraints called with already violating position (PSO)... reinitializing particle.");
			for (int i=0; i<pos.length; i++) {
				if (!Mathematics.isInRange(pos[i], range[i][0], range[i][1])) pos[i]=RNG.randomDouble(range[i][0],range[i][1]);
			}
		}
		for (int i = 0; i < pos.length; i++) {
			if (!Mathematics.isInRange(newPos[i], range[i][0], range[i][1])) {
				if ((pos[i] == range[i][0]) || (pos[i] == range[i][1])) {
					// bounce?
					velocity[i] *= reduceSpeedOnConstViolation; 	// bounce velocity and reduce
					if (((pos[i] == range[i][0]) && (newPos[i] < range[i][0])) || ((pos[i] == range[i][1]) && (newPos[i] > range[i][1]))) {
						velocity[i] *= -1; // bounce only if leaving in this direction.
					}
					newPos[i] = pos[i]+velocity[i];
				} else {
					// set vel. to land on the bounds
					velocity[i] = (newPos[i] < range[i][0]) ? (range[i][0]-pos[i]) : (range[i][1]-pos[i]);
					newPos[i] = pos[i]+velocity[i];
					if ((newPos[i] < range[i][0]) || (newPos[i] > range[i][1])) {
						velocity[i]*=.999; /// beware of floating point errors.
						newPos[i] = pos[i]+velocity[i];
					}
				}
				while ((newPos[i] < range[i][0]) || (newPos[i] > range[i][1])) {
					//System.err.println("missed, pos was " + pos[i] + " vel was "+velocity[i]);
					velocity[i]*=reduceSpeedOnConstViolation;			
					newPos[i] = pos[i]+velocity[i];
				}
			}
		}
		if (isOutOfRange(newPos, range)) {
			System.err.println("narg, still out of range");
		}
	}

	public void optimize() {
//		System.out.println(">>> " + m_Population.getStringRepresentation());
		startOptimize();

		// Update the individuals
		updatePopulation();

		// evaluate the population
		this.evaluatePopulation(this.m_Population);
				
		// update the individual memory
		updateSwarmMemory(m_Population);

		// log the best individual of the population
		logBestIndividual();

//		System.out.println("<<< " + m_Population.getStringRepresentation());
		
//		if (doLocalSearch && (m_Population.getGeneration()%localSearchGens==0)) {
////			System.out.println("Local search at gen "+m_Population.getGeneration());
//			Population bestN = m_Population.getBestNIndividuals(Math.max(1,(int)(lsCandidateRatio*m_Population.size())));
////			Population bestN = m_Population.getSortedNIndividuals(Math.max(1,(int)(lsCandidateRatio*m_Population.size())), false);
//			Population cands=(Population)bestN.clone();
//			int maxSteps=cands.size()*lsStepsPerInd;
//			int stepsDone = PostProcess.processSingleCandidates(PostProcessMethod.nelderMead, cands, maxSteps, 0.01, (AbstractOptimizationProblem)this.m_Problem, null);
//			for (int i=0; i<cands.size(); i++) {
//				if (AbstractEAIndividual.isDominatingFitnessNotEqual(cands.getEAIndividual(i).getFitness(),
//						(double[])bestN.getEAIndividual(i).getData(partBestFitKey))) {
////					System.out.println("Improved to " + BeanInspector.toString(cands.getEAIndividual(i).getFitness()) + " from " + BeanInspector.toString((double[])bestN.getEAIndividual(i).getData(partBestFitKey)));
//					updateIndProps(bestN.getEAIndividual(i), cands.getEAIndividual(i));
//				}
//			}
//			if (stepsDone>maxSteps) {
////				System.err.println("Warning: more steps performed than alloed in PSO LS: " + stepsDone + " vs. " + maxSteps);
//				m_Population.incrFunctionCallsBy(stepsDone);
//			} else m_Population.incrFunctionCallsBy(maxSteps);
//		}

		this.firePropertyChangedEvent(Population.nextGenerationPerformed);

		if (sleepTime > 0 ) try { Thread.sleep(sleepTime); } catch(Exception e) {}
		
//		maybeClearPlot();
	}

	protected void maybeClearPlot() {
		if (((m_Population.getGeneration() % 23) == 0) && isShow() && (m_Plot != null)) {
			m_Plot.clearAll();
			InterfaceDataTypeDouble indy = (InterfaceDataTypeDouble)this.m_Population.get(0);
			double[][] range = indy.getDoubleRange();
			m_Plot.setCornerPoints(range, 0);
		}
	}

	/**
	 * Do some preparations in the beginning of the loop.
	 *
	 */
	protected void startOptimize() {
		if (TRACE) {
			for (int i=0; i<m_Population.size(); i++) {
				AbstractEAIndividual indy = m_Population.getEAIndividual(i);
				System.out.println(BeanInspector.toString(indy.getData(partTypeKey)));
				System.out.println(BeanInspector.toString(indy.getData(partBestPosKey)));
				System.out.println(BeanInspector.toString(indy.getData(partBestFitKey)));
				System.out.println(BeanInspector.toString(indy.getData(partVelKey)));
			}
		}
		if (this.m_Show) this.show();
		sortedPop = null;// to make sure that the last sorted version is not reused
	}

	/**
	 * Log the best individual so far.
	 *
	 */
	protected void logBestIndividual() {
		if (this.m_Population.getBestEAIndividual().isDominatingDebConstraints(this.m_BestIndividual)) {
			this.m_BestIndividual = (AbstractEAIndividual)this.m_Population.getBestEAIndividual().clone();
			this.m_BestIndividual.putData(partBestFitKey, this.m_BestIndividual.getFitness().clone());
			this.m_BestIndividual.putData(partBestPosKey, ((InterfaceDataTypeDouble)this.m_BestIndividual).getDoubleData());
//			System.out.println("new best: "+m_BestIndividual.toString());
		}
	}

	/**
	 * Loop over the population and trigger the individual update.
	 *
	 */
	protected void updatePopulation() {

		updateTopology(this.m_Population);

		for (int i = 0; i < this.m_Population.size(); i++) {
			this.updateIndividual(i, (AbstractEAIndividual)m_Population.get(i), this.m_Population);
		}

		if (m_Show) {
			if (this.m_Problem instanceof Interface2DBorderProblem) {
				DPointSet               popRep  = new DPointSet();
				InterfaceDataTypeDouble tmpIndy1;

				double[]   a = new double[2];
				a[0] = 0.0;
				a[1] = 0.0;
				if (topoPlot == null) {
					this.topoPlot = new TopoPlot("CBN-Species","x","y",a,a);
					this.topoPlot.setParams(60, 60);
					this.topoPlot.setTopology((Interface2DBorderProblem)this.m_Problem);
				}
				
				for (int i = 0; i < this.m_Population.size(); i++) {
					tmpIndy1 = (InterfaceDataTypeDouble)this.m_Population.get(i);
					popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
				}
				this.topoPlot.getFunctionArea().addDElement(popRep);
				//draw the species
			}
		}
	}
	
	protected void addSortedIndicesTo(Object[] sortedPopulation, Population pop) {
		int origIndex;
		for (int i=0; i<pop.size(); i++) {
			// cross-link the sorted list for faster access
			origIndex = (Integer)((AbstractEAIndividual)sortedPopulation[i]).getData(indexKey);
			((AbstractEAIndividual)pop.get(origIndex)).putData(sortedIndexKey, new Integer(i));
		}
	}

	protected void updateTopology(Population pop) {
//		int topoID = this.m_Topology.getSelectedTag().getID();
//		this.m_Topology = new SelectedTag( "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random" );
		if (topology == PSOTopologyEnum.dms) { // Dynamic multi-swarm after Liang & Suganthan
			if (pop.getGeneration() % getDmsRegroupGens()==0) {
				dmsLinks = regroupSwarm(pop, getTopologyRange());
			}
		}
		if ((topology == PSOTopologyEnum.multiSwarm) || (topology == PSOTopologyEnum.tree)) {
			sortedPop = pop.toArray();
			if ((topology == PSOTopologyEnum.multiSwarm) || (treeStruct>=2)) Arrays.sort(sortedPop, new AbstractEAIndividualComparator()); 
			else Arrays.sort(sortedPop, new AbstractEAIndividualComparator(partBestFitKey));
			addSortedIndicesTo(sortedPop, pop);
		}
		if (topology == PSOTopologyEnum.multiSwarm) {
			// prepare multi swarm topology
			PhenotypeMetric metric = new PhenotypeMetric();
			Vector<AbstractEAIndividual> leaders = new Vector<AbstractEAIndividual>(pop.size());
			int cur = 0;
			boolean found = false, superfluous = false;
			double dist;
			while (cur < pop.size()) {
				found = false;
				superfluous = false;
				for (int i=0; i<leaders.size(); i++) {
					dist = metric.distance((AbstractEAIndividual)sortedPop[cur], leaders.get(i));
					//System.out.println("dist is "+dist);
					if ((m_swarmRadius*2.) > dist) {	// a formal leader is found
						int sSize = (Integer)(leaders.get(i)).getData(multiSwSizeKey);
						if ((maxSubSwarmSize > 0) && (sSize >= maxSubSwarmSize)) {
							// swarm is too big already
							superfluous = true; // toggle reinitialize
							found = true;
							break;
						} else {
							found = true;
							// assign to leader, update swarm size
							((AbstractEAIndividual)sortedPop[cur]).putData(multiSwTypeKey, leaders.get(i));
							((AbstractEAIndividual)sortedPop[cur]).putData(multiSwSizeKey, new Integer(-1));
							leaders.get(i).putData(multiSwSizeKey, 1+sSize);
							break;
						}
					}
				}
				if (!found) { // new leader is found
					leaders.add(((AbstractEAIndividual)sortedPop[cur]));
					((AbstractEAIndividual)sortedPop[cur]).putData(multiSwTypeKey, sortedPop[cur]);
					((AbstractEAIndividual)sortedPop[cur]).putData(multiSwSizeKey, new Integer(1));
				} else if (superfluous) {
					//System.out.println("reinitializing " + cur);
					((AbstractEAIndividual)sortedPop[cur]).putData(partTypeKey, resetType);
					((AbstractEAIndividual)sortedPop[cur]).putData(multiSwTypeKey, sortedPop[cur]);
					((AbstractEAIndividual)sortedPop[cur]).putData(multiSwSizeKey, new Integer(1));
				}
				cur++;
			}
//			for (int i=0; i<pop.size(); i++) { // this causes quadratic complexity. however, popsizes are usually small for pso, so its acceptable for testing
//			if ((i!=index) && ((m_swarmRadius*2.)>metric.distance((AbstractEAIndividual)pop.get(i), (AbstractEAIndividual)pop.get(index)))) {
//			this.compareAndSet(localBestFitness, localBestPosition, (AbstractEAIndividual)pop.get(i));
//			}
//			}
//			for (int i=0; i<leaders.size(); i++) {
//				int sSize = (Integer)(leaders.get(i)).getData(multiSwSizeKey);
//				m_Population.indexOf(leaders.get(i));
//				System.out.print("s " + i + " w " +  sSize + " (" + m_Population.indexOf(leaders.get(i)) + "), ");
//			}
			//System.out.println(" -- best " + m_Population.indexOf(m_Population.getBestEAIndividual()));
		}
		if (topology == PSOTopologyEnum.hpso) { // HPSO sorting the population
			int parentIndex;
			AbstractEAIndividual indy;
			AbstractEAIndividualComparator comp = new AbstractEAIndividualComparator(partBestFitKey);
			for (int i=0; i<pop.size(); i++) {
				// loop over the part of the tree which is complete (full degree in each level)
				parentIndex=getParentIndex(m_TopologyRange, i, pop.size());
				if (comp.compare(pop.get(i), pop.get(parentIndex)) < 0) { // sibling is dominant!
					// so switch them
					indy = (AbstractEAIndividual)pop.get(i);
					pop.set(i, pop.get(parentIndex));
					pop.set(parentIndex, indy);
				}
			}
			// the old way (two loops) without good parentIndex function
//			for (int i=0; i<pop.size()-treeOrphans; i++) {
//				// loop over the part of the tree which is complete (full degree in each level)
//				parentIndex=getParentIndex(branchDegree, i, pop.size());
//				if (comp.compare(pop.get(i), pop.get(parentIndex)) < 0) { // sibling is dominant!
//					// so switch them
//					indy = (AbstractEAIndividual)pop.get(i);
//					pop.set(i, pop.get(parentIndex));
//					pop.set(parentIndex, indy);
//				}
//			}
//			int nodesOfLastLevel = (int)Math.pow(branchDegree, treeLevels-1);
//			// calc index of first parent of orphaned nodes
//			parentIndex = pop.size()-treeOrphans-nodesOfLastLevel;
//			int offset = pop.size()-treeOrphans;
//			for (int i=0; i<treeOrphans; i++) {
//				// now care about the orphans (last level)
//				if (comp.compare(pop.get(offset+i), pop.get(parentIndex)) < 0) { // sibling is dominant!
//					// so switch them
//					indy = (AbstractEAIndividual)pop.get(offset+i);
//					pop.set(offset+i, pop.get(parentIndex));
//					pop.set(parentIndex, indy);
//				}
//				parentIndex++;
//				if (parentIndex == offset) parentIndex = pop.size()-treeOrphans-nodesOfLastLevel;
//			}	
		}
	}

	/**
	 * Randomly assign groups of size groupSize.
	 * 
	 * @param links
	 * @param groupSize
	 */
	private Vector<int[]> regroupSwarm(Population pop, int groupSize) {
		int numGroups = pop.size() / groupSize; // truncated integer: last group is larger
		int[] perm = RNG.randomPerm(pop.size());
		
		Vector<int[]> links = new Vector<int[]>(numGroups);
		for (int i=0; i<numGroups; i++) {
			if (i<numGroups-1) links.add(new int[groupSize]);
			else links.add(new int[pop.size()-(groupSize*i)]); // the last group is larger
			int[] group=links.get(i);
			for (int k=0; k<group.length; k++) {
				group[k]=perm[groupSize*i+k];
				pop.getEAIndividual(group[k]).putData(dmsGroupIndexKey, i);
			}
		}
		return links;
	}

//	/**
//	 * Randomly assign groups of size groupSize.
//	 * 
//	 * @param links
//	 * @param groupSize
//	 */
//	private int[] regroupSwarm(Population pop, int groupSize) {
//		int groupIndex, numGroups = pop.size() / groupSize; // truncated integer: last group is larger
////		int hangover = pop.size()-(numGroups*groupSize); // Ueberhangmandate ... woanders zuteilen um einergruppen zu vermeiden
//		
//		int[] perm = RNG.randomPerm(pop.size());
//		
//		for (int k=0; k<perm.length; k++) {
//			groupIndex=k/groupSize;
//			if (groupIndex>=numGroups) groupIndex--; 
//			pop.getEAIndividual(perm[k]).putData(dmsGroupIndexKey, groupIndex);
//		}
//		return perm;
//	}
	
	/** 
	 * This method is simply for debugging.
	 */
	protected void show() {
		if (this.m_Plot == null) {
			InterfaceDataTypeDouble indy = (InterfaceDataTypeDouble)this.m_Population.get(0);
			double[][] range = indy.getDoubleRange();
			this.m_Plot = new eva2.gui.Plot("PSO "+ m_Population.getGeneration(), "x1", "x2", range[0], range[1]);
//			this.m_Plot.setUnconnectedPoint(range[0][0], range[1][0], 0);
//			this.m_Plot.setUnconnectedPoint(range[0][1], range[1][1], 0);
		}
	}

	public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
		this.m_Listener = ea;
	}
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
	public void SetProblem (InterfaceOptimizationProblem problem) {
		this.m_Problem = problem;
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
		result += "Particle Swarm Optimization:\n";
		result += "Optimization Problem: ";
		result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
		result += this.m_Population.getStringRepresentation();
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
	public static String globalInfo() {
		return "Particle Swarm Optimization by Kennedy and Eberhart.";
	}
	/** This method will return a naming String
	 * @return The name of the algorithm
	 */
	public String getName() {
//		return "PSO-"+getTopology()+getTopologyRange()+(isDoLocalSearch() ? "-ls_" : "_")+getPhi1()+"_"+getPhi2();
		return "PSO-"+getTopology()+getTopologyRange()+"_"+getPhi1()+"_"+getPhi2();
	}

	public Population getPopulation() {
		return this.m_Population;
	}
	public void setPopulation(Population pop){
		this.m_Population = pop;
		if (pop.size() != pop.getTargetSize()) { // new particle count!
			tracedVelocity = null;
			initByPopulation(null, false);
		} else for (int i=0; i<pop.size(); i++) {
			AbstractEAIndividual indy = pop.getEAIndividual(i);
			if (indy==null) {
				System.err.println("Error in PSO.setPopulation!");
			} else if (!indy.hasData(partTypeKey)) {
				initIndividualDefaults(indy, m_InitialVelocity);
				initIndividualMemory(indy);
				indy.putData(indexKey, i);
				indy.SetIndividualIndex(i);
				if (TRACE) System.err.println("init indy " + i + " " + AbstractEAIndividual.getDefaultDataString(indy));
			}
		}
		m_BestIndividual = pop.getBestEAIndividual();
	}
	public String populationTipText() {
		return "Edit the properties of the population used.";
	}
    
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
    
    public AbstractEAIndividual getBestIndividual() {
    	return m_BestIndividual;
    }
    
    
	/** This method will set the initial velocity
	 * @param f
	 */
	public void setInitialVelocity (double f) {
		this.m_InitialVelocity = f;
	}
	public double getInitialVelocity() {
		return this.m_InitialVelocity;
	}
	public String initialVelocityTipText() {
		return "The initial velocity for each PSO particle.";
	}

	/** This method will set the speed limit
	 * @param k
	 */
	public void setSpeedLimit (double k) {
		if (k < 0) k = 0;
		if (k > 1) k = 1;
		this.m_SpeedLimit = k;
	}
	
	public double getSpeedLimit() {
		return this.m_SpeedLimit;
	}
	
	/**
	 * It may be useful to give each particle a different speed limit.
	 * 
	 * @param index
	 * @return
	 */
	protected double getSpeedLimit(int index) {
		return this.m_SpeedLimit;
	}	
	
	public String speedLimitTipText() {
		return "The speed limit in respect to the size of the search space [0,1].";
	}

	/**
	 * Set the phi values as well as the inertness parameter so as to resemble the constriction scheme. 
	 * The constriction scheme calculates the speed update in the following
	 * way: v(t+1) = Chi * ( v(t) + tau1*u1*(p-x(t)) * tau2*u2*(g-x(t)))
	 * with u1, u2 random variables in (0,1) and tau1 and tau2 usually set to 2.05. The sum tau1 and tau2
	 * must be greater than 4. The Chi parameter (constriction) is set as in
	 *                 2
	 *  Chi = ------------------------
	 *       |2-tau-sqrt(tau^2-4 tau)|
	 * where tau = tau1 + tau2 
	 * See Clerc&Kennedy: The Particle Swarm: Explosion, stability and convergence, 2002.
	 *
	 * @param tau1
	 * @param tau2
	 */
	protected void setConstriction(double tau1, double tau2) {
		double pSum = tau1+tau2;
		if (pSum <= 4) {
			System.err.println("error, invalid tauSum value in PSO::setConstriction");
		} else {
			if (!getAlgoType().isSelectedString("Constriction")) System.err.println("Warning, PSO algorithm variant constriction expected!");
			m_Phi1=tau1;
			m_Phi2=tau2;
			setInertnessOrChi(2./(Math.abs(2-pSum-Math.sqrt((pSum*pSum)-(4*pSum)))));
		}
	}

	/** 
	 * This method will set the inertness/chi value
	 * @param k
	 */
	public void setInertnessOrChi(double k) {
		this.m_InertnessOrChi = k;
	}

	public double getInertnessOrChi() {
		return this.m_InertnessOrChi;
	}
	public String inertnessOrChiTipText() {
		return "Gives the speed decay of the previous velocity [0,1] in inertness mode or the chi value in constriction mode which is calculated from phi1 and phi2.";
	}

	/** This method will set greediness to move towards the best cognition
	 * @param l
	 */
	public void setPhi1 (double l) {
		this.m_Phi1 = l;
		if (algType.getSelectedTag().getID() == 1) setConstriction(getPhi1(), getPhi2());
	}
	public double getPhi1() {
		return this.m_Phi1;
	}
	public String phi1TipText() {
		return "Acceleration for the cognition model.";
	}

	/** This method will set greediness to move towards the best social
	 * @param l
	 */
	public void setPhi2 (double l) {
		this.m_Phi2 = l;
		if (algType.getSelectedTag().getID() == 1) setConstriction(getPhi1(), getPhi2());
	}
	public double getPhi2() {
		return this.m_Phi2;
	}
	public String phi2TipText() {
		return "Acceleration for the social model.";
	}

	/**
	 * Set the phi1 / phi2 parameter values (and in the constriction variant, adapt constriction factor).
	 *   
	 * @param phi1
	 * @param phi2
	 */
	public void setPhiValues(double phi1, double phi2) {
		m_Phi1 = phi1;
		m_Phi2 = phi2;
		if (algType.isSelectedString("Constriction")) setConstriction(phi1, phi2);
	}
	
	/**
	 * Directly set all parameter values phi1, phi2 and inertness/constriction factor.
	 *  
	 * @param phi1
	 * @param phi2
	 * @param inertness
	 */
	public void setParameterValues(double phi1, double phi2, double inertness) {
		m_Phi1 = phi1;
		m_Phi2 = phi2;
		setInertnessOrChi(inertness);
	}
	
	/** This method allows you to choose the topology type.
	 * @param s  The type.
	 */
	public void setTopology(PSOTopologyEnum t) {
		this.topology = t;
		setGOEShowProperties(getClass());
	}
	
	public void setGOEShowProperties(Class<?> cls) {
//		this.m_Topology = new SelectedTag( "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random" );
		
		// linear, grid, random
		GenericObjectEditor.setShowProperty(cls, "topologyRange", (topology==PSOTopologyEnum.linear) || (topology==PSOTopologyEnum.grid) || (topology==PSOTopologyEnum.random) || (topology==PSOTopologyEnum.tree) || (topology==PSOTopologyEnum.hpso) || (topology==PSOTopologyEnum.dms));
		// multi swarm
		GenericObjectEditor.setShowProperty(cls, "subSwarmRadius", (topology==PSOTopologyEnum.multiSwarm));
		// multi swarm
		GenericObjectEditor.setShowProperty(cls, "maxSubSwarmSize", (topology==PSOTopologyEnum.multiSwarm));
		// tree
		GenericObjectEditor.setShowProperty(cls, "treeStruct", (topology==PSOTopologyEnum.tree));
		// tree, hpso
//		GenericObjectEditor.setShowProperty(cls, "treeBranchDegree", (topology==PSOTopologyEnum.tree) || (topology==PSOTopologyEnum.hpso));
		// linear
		GenericObjectEditor.setShowProperty(cls, "wrapTopology", (topology==PSOTopologyEnum.linear) || (topology==PSOTopologyEnum.grid));
		// dms
		GenericObjectEditor.setShowProperty(cls, "dmsRegroupGens", (topology==PSOTopologyEnum.dms));
	}
	
	public PSOTopologyEnum getTopology() {
		return topology;
	}
	public String topologyTipText() {
		return "Choose the topology type";
	}

	/** This method allows you to choose the algorithm type.
	 * @param s  The type.
	 */
	public void setAlgoType(SelectedTag s) {
		this.algType = s;
		if (s.getSelectedTag().getID() == 1) setConstriction(getPhi1(), getPhi2());
	}

	public SelectedTag getAlgoType() {
		return this.algType;
	}

	public String algoTypeTipText() {
		return "Choose the inertness or constriction method. Chi is calculated automatically in constriction.";
	}

	/** The range of the local neighbourhood.
	 * @param s  The range.
	 */
	public void setTopologyRange(int s) {
		this.m_TopologyRange = s;
	}
	public int getTopologyRange() {
		return this.m_TopologyRange;
	}
	public String topologyRangeTipText() {
		return "The range of the neighborhood topology.";
	}

	/** Toggle Check Constraints.
	 * @param s    Check Constraints.
	 */
	public void setCheckRange(boolean s) {
		this.m_CheckRange = s;
	}
	public boolean isCheckRange() {
		return this.m_CheckRange;
	}
	public String checkRangeTipText() {
		return "Toggle whether particles are allowed to leave the range.";
	}

	/**
	 * @return true if swarm visualization is turned on
	 **/
	public boolean isShow() {
		return m_Show;
	}

	/**
	 * @param set swarm visualization (2D)
	 **/
	public void setShow(boolean show) {
		m_Show = show;
		if (!show) m_Plot = null;
	}
	public String showTipText() {
		return "Activate for debugging in 2D";
	}
	
	/**
	 * @return the checkSpeedLimit
	 **/
	public boolean isCheckSpeedLimit() {
		return checkSpeedLimit;
	}

	/**
	 * @param checkSpeedLimit the checkSpeedLimit to set
	 **/
	public void setCheckSpeedLimit(boolean checkSpeedLimit) {
		this.checkSpeedLimit = checkSpeedLimit;
		GenericObjectEditor.setHideProperty(getClass(), "speedLimit", !checkSpeedLimit);
	}

	public String checkSpeedLimitTipText() {
		return "if activated, the speed limit is enforced for the particles";
	}

//	public int getEMAPeriods() {
//	return emaPeriods;
//	}

//	public void setEMAPeriods(int emaP) {
//	this.emaPeriods = emaP;
//	}

	/**
	 * @return the sleepTime
	 **/
	public int getSleepTime() {
		return sleepTime;
	}
	/**
	 * @param sleepTime the sleepTime to set
	 **/
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	public String sleepTimeTipText() {
		return "Sleep for a time between iterations - to be used with debugging and the show option.";
	}
	
	
	public double getSubSwarmRadius() {
		return m_swarmRadius;
	}

	public void setSubSwarmRadius(double radius) {
		m_swarmRadius = radius;
	}

	public String subSwarmRadiusTipText() {
		return "Define the maximum distance to a swarm leader in the multi-swarm variant";
	}

	public int getMaxSubSwarmSize() {
		return maxSubSwarmSize;
	}

	public void setMaxSubSwarmSize(int subSize) {
		maxSubSwarmSize = subSize;
	}

	public String maxSubSwarmSizeTipText() {
		return "Maximum size of a sub swarm. Violating particles will be reinitialized. 0 means no limit to the sub swarm size.";
	}

	public int getTreeStruct() {
		return treeStruct;
	}

	public void SetTreeStruct(int treeStruct) {
		this.treeStruct = treeStruct;
	}
	
// This was for testing rotation operators 
//	public boolean isUseAlternative() {
//		return useAlternative;
//	}
//
//	public void setUseAlternative(boolean useAlternative) {
//		this.useAlternative = useAlternative;
//	}

	public boolean isWrapTopology() {
		return wrapTopology;
	}

	public void setWrapTopology(boolean wrapTopology) {
		this.wrapTopology = wrapTopology;
	}
	
	public String wrapTopologyTipText() {
		return "Wraps the topology to a ring structure";
	}

	protected int getEmaPeriods() {
		return emaPeriods;
	}

	protected void setEmaPeriods(int emaPeriods) {
		this.emaPeriods = emaPeriods;
	}

	/**
	 * This method is necessary to allow access from the Processor.
	 * @return
	 */
	public ParameterControlManager getParamControl() {
		return paramControl;
	}
	
	public ParamAdaption[] getParameterControl() {
		return paramControl.getSingleAdapters();
	}
	public void setParameterControl(ParamAdaption[] paramControl) {
		this.paramControl.setSingleAdapters(paramControl);
	}
	public String parameterControlTipText() {
		return "You may define dynamic paramter control strategies using the parameter name.";
	}

	/**
	 * Retrieve the set of personal best positions contained in the given population.
	 * @param population
	 * @return
	 */
	protected Population getPersonalBestPos(Population population) {
		Population bests = new Population(population.size());
		AbstractEAIndividual indy = (AbstractEAIndividual)population.getEAIndividual(0).clone();
		if (!indy.hasData(partBestFitKey)) return null;
		for (int i=0; i<population.size(); i++) {
			double[] personalBestPos = (double[]) population.getEAIndividual(i).getData(partBestPosKey);
			double[] personalBestfit = (double[]) population.getEAIndividual(i).getData(partBestFitKey);
			
			double relDiff;
			if (personalBestfit[0]!=0) relDiff = (personalBestfit[0]-((InterfaceProblemDouble)m_Problem).eval(personalBestPos)[0])/personalBestfit[0];
			else relDiff=(personalBestfit[0]-((InterfaceProblemDouble)m_Problem).eval(personalBestPos)[0]); // absolute diff in this case
//			if (personalBestfit[0]!=((InterfaceProblemDouble)m_Problem).eval(personalBestPos)[0]) {
			if (Math.abs(relDiff)>1e-20) {
				System.err.println("Warning: mismatching best fitness by " + relDiff);
				System.err.println("partInfo: " + i + " - " + getParticleInfo(population.getEAIndividual(i)));
			}
			if (Math.abs(relDiff)>1e-10) {
				System.err.println("partInfo: " + i + " - " + getParticleInfo(population.getEAIndividual(i)));
				throw new RuntimeException("Mismatching best fitness!! " + personalBestfit[0] + " vs. " + ((InterfaceProblemDouble)m_Problem).eval(personalBestPos)[0]);
			}
			((InterfaceDataTypeDouble)indy).SetDoubleGenotype(personalBestPos);
			indy.SetFitness(personalBestfit);
			bests.add((AbstractEAIndividual)indy.clone());
		}
		return bests;
	}

	public int getDmsRegroupGens() {
		return dmsRegroupInterval;
	}

	public void setDmsRegroupGens(int dmsRegroupInterval) {
		this.dmsRegroupInterval = dmsRegroupInterval;
	}
	
	public String dmsRegroupGensTipText() {
		return "The number of generations after which new subswarms are randomly formed.";
	}

//	public boolean isDoLocalSearch() {
//		return doLocalSearch;
//	}
//
//	public void setDoLocalSearch(boolean doLocalSearch) {
//		this.doLocalSearch = doLocalSearch;
//	}

	public String[] getAdditionalDataHeader() {
		if (emaPeriods > 0) return new String[]{"meanEMASpeed", "meanCurSpeed"};
		else return new String[]{"meanCurSpeed"};
	}	
	
	public String[] getAdditionalDataInfo() {
		if (emaPeriods > 0) return new String[]{"Exponential moving average of the (range-relative) speed of all particles", "The mean (range-relative) current speed of all particles"};
		else return new String[]{"The mean (range-relative) current speed of all particles"};
	}

	public Object[] getAdditionalDataValue(PopulationInterface pop) {
		AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(0);
		if (emaPeriods>0) {
			double relSp;
			if (indy instanceof InterfaceDataTypeDouble) {
				relSp = getRelativeEMASpeed(((InterfaceDataTypeDouble)indy).getDoubleRange());
			} else relSp=Double.NaN;
			return new Object[]{relSp, getPopulationAvgNormedVelocity((Population) pop)};
		} else return new Object[]{getPopulationAvgNormedVelocity((Population) pop)};
	}
}