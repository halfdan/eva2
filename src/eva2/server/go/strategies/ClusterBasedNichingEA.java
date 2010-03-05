package eva2.server.go.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.gui.GraphPointSet;
import eva2.gui.Plot;
import eva2.gui.TopoPlot;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.AbstractEAIndividualComparator;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.cluster.ClusteringDensityBased;
import eva2.server.go.operators.cluster.InterfaceClustering;
import eva2.server.go.operators.distancemetric.ObjectiveSpaceMetric;
//import eva2.server.go.populations.Distraction;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.Interface2DBorderProblem;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.problems.TF1Problem;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.chart2d.DPointSet;
import eva2.tools.math.Mathematics;

/** The infamous clustering based niching EA, still under construction.
 * It should be able to identify and track multiple global/local optima
 * at the same time.
 * 
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 322 $
 *            $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007) $
 *            $Author: mkron $
 */

public class ClusterBasedNichingEA implements InterfacePopulationChangedEventListener, InterfaceAdditionalPopulationInformer, InterfaceOptimizer, java.io.Serializable {
	private static final long serialVersionUID = -3143069327594708609L;
	private Population                      m_Population                    = new Population();
    private transient Population			m_Archive						= new Population();
    private ArrayList<Population>			m_Species              			= new ArrayList<Population>();
    private Population                      m_Undifferentiated              = new Population();
    private transient Population			m_doomedPop						= new Population();
    
    private InterfaceOptimizationProblem    m_Problem                       = new B1Problem();
    private InterfaceOptimizer              m_Optimizer                     = new GeneticAlgorithm();
    private InterfaceClustering             m_CAForSpeciesDifferentation    = new ClusteringDensityBased();
    private InterfaceClustering             m_CAForSpeciesMerging       	= new ClusteringDensityBased();
//    private Distraction						distraction						= null;
    private boolean 						useDistraction	= false;
//    private double 							distrDefaultStrength = .7;
    private double							epsilonBound = 1e-10;
    
    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    private int                             m_SpeciesCycle                  = 1;
    // from which size on is a species considered active 
//    private int 							m_actSpecSize					= 2;
    private int 							m_minGroupSize					= 3;
//    private boolean                         m_UseClearing                   = false;
//    private boolean							m_UseArchive					= true;
    private boolean                         m_UseSpeciesDifferentiation      = true;
    private boolean                         m_mergeSpecies					= true;
    private int                             m_PopulationSize                = 50;
    private int								convergedCnt					= 0;

    private static boolean                  TRACE     = false,  TRACE_STATE=false;
    private int                             m_ShowCycle = 0;
    transient private TopoPlot              m_Topology;
    private int                 			haltingWindow         			 = 15;
    private double							muLambdaRatio					 = 0.5;
	private int sleepTime = 0;
	private int m_maxSpeciesSize = 15;
	private AbstractEAIndividualComparator reduceSizeComparator = new AbstractEAIndividualComparator();

	public ClusterBasedNichingEA() {
        this.m_CAForSpeciesMerging = new ClusteringDensityBased();
        ((ClusteringDensityBased)this.m_CAForSpeciesMerging).setMinimumGroupSize(m_minGroupSize);
//        if (useDistraction) distraction = new Distraction(distrDefaultStrength, Distraction.METH_BEST);
    }

    public ClusterBasedNichingEA(ClusterBasedNichingEA a) {    
    	this.epsilonBound					= a.epsilonBound;
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Archive						= (Population)a.m_Archive.clone();
        this.m_doomedPop					= (Population)a.m_doomedPop.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Optimizer                    = (InterfaceOptimizer)a.m_Optimizer.clone();
        this.m_Species                      = (ArrayList<Population>)(a.m_Species.clone());
        this.m_Undifferentiated             = (Population)a.m_Undifferentiated.clone();
        this.m_CAForSpeciesMerging      	= (InterfaceClustering)this.m_CAForSpeciesMerging.clone();
        this.m_CAForSpeciesDifferentation   = (InterfaceClustering)this.m_CAForSpeciesDifferentation.clone();
        this.m_SpeciesCycle                 = a.m_SpeciesCycle;
        this.m_minGroupSize					= a.m_minGroupSize;
        this.m_UseSpeciesDifferentiation    = a.m_UseSpeciesDifferentiation;
        this.m_mergeSpecies        			= a.m_mergeSpecies;
        this.m_PopulationSize               = a.m_PopulationSize;
        this.haltingWindow					= a.haltingWindow;
        this.m_maxSpeciesSize				= a.m_maxSpeciesSize;
        this.muLambdaRatio					= a.muLambdaRatio;
        this.sleepTime						= a.sleepTime;
        this.convergedCnt					= a.convergedCnt;
    }

    public Object clone() {
        return (Object) new ClusterBasedNichingEA(this);
    }

    public void init() {
        this.m_Undifferentiated = new Population(m_PopulationSize);
        this.m_Undifferentiated.setUseHistory(true);

        this.m_Problem.initPopulation(this.m_Undifferentiated);
        this.m_Optimizer.setPopulation(m_Undifferentiated);
        if (m_Optimizer instanceof EvolutionStrategies) {
        	EvolutionStrategies es = (EvolutionStrategies)m_Optimizer;
        	es.setLambda(getPopulationSize());
        	es.setMu((int)(muLambdaRatio*(double)getPopulationSize()));
        }
        this.m_Optimizer.init();
        m_doomedPop = new Population();
        if (m_Undifferentiated.getFunctionCalls()!=m_PopulationSize) {
        	System.err.println("Whats that in CBN!?");
        }
        initDefaults(false);
    }        

    public void hideHideable() {
    	GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    	setMaxSpeciesSize(getMaxSpeciesSize());
    	setUseMerging(isUseMerging());
    }
    
    /** 
     * Do not reinitialize the main population!
     * 
     * @param evalPop
     */
    private void initDefaults(boolean evalPop) {
    	this.m_Optimizer.addPopulationChangedEventListener(this);
    	this.m_Undifferentiated.setTargetSize(this.m_PopulationSize);
    	this.m_Species = new ArrayList<Population>();
    	this.m_Archive = new Population();
//    	if (useDistraction) distraction = new Distraction(distrDefaultStrength, Distraction.METH_BEST);
    	convergedCnt = 0;
    	if (evalPop) this.evaluatePopulation(this.m_Undifferentiated);
    	this.m_Optimizer.initByPopulation(m_Undifferentiated, false);
    	this.m_Undifferentiated = m_Optimizer.getPopulation(); // required for changes to the population by the optimizer
    	this.firePropertyChangedEvent("FirstGenerationPerformed");
    }
    
    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Undifferentiated = (Population)pop.clone();
        if (reset) this.m_Undifferentiated.init();
    	initDefaults(reset);
    }

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
    }

    private void plot(int gen) {
    	if (!(this.m_Problem instanceof TF1Problem) && !(this.m_Problem instanceof Interface2DBorderProblem)) return;
        double[]   a = new double[2];
        a[0] = 0.0;
        a[1] = 0.0;
        if (this.m_Problem instanceof TF1Problem) {
            // now i need to plot the pareto fronts
            Plot plot = new Plot("TF3Problem at gen. "+gen, "y1", "y2", a, a);
            plot.setUnconnectedPoint(0,0,0);
            plot.setUnconnectedPoint(1,5,0);
            GraphPointSet   mySet = new GraphPointSet(10, plot.getFunctionArea());
            DPoint          point;
            mySet.setConnectedMode(false);
            for (int i = 0; i < this.m_Undifferentiated.size(); i++) {
                AbstractEAIndividual indy = (AbstractEAIndividual)this.m_Undifferentiated.get(i);
                double [] d = indy.getFitness();
                point = new DPoint(d[0], d[1]);
                point.setIcon(new Chart2DDPointIconCircle());
                mySet.addDPoint(point);
            }
            for (int i = 0; i < this.m_Species.size(); i++) {
                mySet = new GraphPointSet(10+i, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                Population pop = (Population)this.m_Species.get(i);
//                ArchivingAllDomiating arch = new ArchivingAllDomiating();
//                arch.plotParetoFront(pop, plot);
                for (int j = 0; j < pop.size(); j++) {
                    AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(j);
                    double [] d = indy.getFitness();
                    point = new DPoint(d[0], d[1]);
                    point.setIcon(new Chart2DDPointIconText("P"+j));
                    mySet.addDPoint(point);
                }

            }

        }
        if (this.m_Problem instanceof Interface2DBorderProblem) {
            DPointSet               popRep  = new DPointSet();
            InterfaceDataTypeDouble tmpIndy1;
            Population              pop;

            this.m_Topology          = new TopoPlot("CBN-Species at gen. " + gen,"x","y",a,a);
			this.m_Topology.setParams(60, 60);
            this.m_Topology.setTopology((Interface2DBorderProblem)this.m_Problem);
            //draw the undifferentiated
            for (int i = 0; i < this.m_Undifferentiated.size(); i++) {
                tmpIndy1 = (InterfaceDataTypeDouble)this.m_Undifferentiated.get(i);
                popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
            }
            this.m_Topology.getFunctionArea().addDElement(popRep);
            //draw the species
            for (int i = 0; i < this.m_Species.size(); i++) {
                pop = (Population)this.m_Species.get(i);
                plotPopConnected(m_Topology, pop);
            }
            if (!useDistraction) {
            	for (int i = 0; i < this.m_Archive.size(); i++) {
            		plotIndy('x',(InterfaceDataTypeDouble)m_Archive.get(i));
            	}
            } else {
//            	for (int i = 0; i < this.distraction.getDistractorSetSize(); i++) {
//            		plotPosFit('#',distraction.getDistractorCenter(i), distraction.getDistractorStrength(i));
//            	}
            }
        }
        if (sleepTime > 0) {
        	try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

	private void plotPopConnected(TopoPlot tp, Population pop) {
		DPointSet popRep;
		InterfaceDataTypeDouble tmpIndy1;
		if (pop.size()>1) {
		    for (int j = 0; j < pop.size(); j++) {
		        popRep = new DPointSet();
		        tmpIndy1 = (InterfaceDataTypeDouble)pop.get(j);
		        popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
		        tp.getFunctionArea().addDElement(popRep);

		        plotLine(tp, pop.getEAIndividual(j), pop.getBestEAIndividual());
		    }
		} else {
		    // this is an inactive species
			plotIndy('+',(InterfaceDataTypeDouble)pop.get(0));
		}
	}

	private void plotLine(TopoPlot tp, AbstractEAIndividual indy1,
			AbstractEAIndividual indy2) {
		DPointSet popRep;
		double[] pos1, pos2;
		if (indy1 instanceof InterfaceDataTypeDouble) pos1=((InterfaceDataTypeDouble)indy1).getDoubleData();
		else pos1=(indy1).getDoublePosition();
		if (indy2 instanceof InterfaceDataTypeDouble) pos2=((InterfaceDataTypeDouble)indy2).getDoubleData();
		else pos2 =(indy2).getDoublePosition();
		
		popRep      = new DPointSet();
		popRep.setConnected(true);
		popRep.addDPoint(new DPoint(pos1[0], pos1[1]));
		popRep.addDPoint(new DPoint(pos2[0], pos2[1]));
		tp.getFunctionArea().addDElement(popRep);
	}

	private void plotIndy(char c, InterfaceDataTypeDouble tmpIndy) {
		plotPosFit(c, tmpIndy.getDoubleData(), ((AbstractEAIndividual)tmpIndy).getFitness(0));
	}

	private void plotPosFit(char c, double[] position, double fitness) {
		DPointSet popRep;
		popRep = new DPointSet();
		popRep.addDPoint(new DPoint(position[0], position[1]));
		double d = Math.round(100*fitness)/(double)100;
		DPointIcon icon = new Chart2DDPointIconText(c+""+d);
		((Chart2DDPointIconText)icon).setIcon(new Chart2DDPointIconCircle());
		popRep.setIcon(icon);
		this.m_Topology.getFunctionArea().addDElement(popRep);
	}

    /** This method is called to generate n freshly initialized individuals
     * @param n     Number of new individuals
     * @return A population of new individuals
     */
    private Population initializeIndividuals(int n) {
        Population result = new Population();
        result.setUseHistory(true);
        result.setTargetSize(n);
        //@todo: crossover between species is to be implemented
        m_Problem.initPopulation(result);
        m_Problem.evaluate(result);
        m_Optimizer.setPopulation(result);	// for some initialization by the optimizer, such as PSO memory 
//        capMutationRate(result, RNG.randomDouble(0.001, 0.1));
        return result;
    }

    /** 
     * This method checks whether a species is converged, i.e. the best fitness has not improved
     * for a number of generations.
     *  
     * @param pop   The species to test
     * @return True if converged.
     */
    private boolean testSpeciesForConvergence(Population pop) {
        ArrayList<AbstractEAIndividual> speciesHistory = pop.getHistory();
        int     histLen = speciesHistory.size();

        if (histLen <= haltingWindow) {
//        	System.out.println("not long enough... gen " + pop.getGeneration());
        	return false;
        } else {
            AbstractEAIndividual historicHWAgo = speciesHistory.get(histLen-haltingWindow);
            for (int i = 1; i < haltingWindow; i++) {
            	// if historic[-hW] is worse than historic[-hW+i] return false
            	AbstractEAIndividual historicIter = speciesHistory.get(histLen-haltingWindow+i);
            	// if the iterated indy (the later one in history) has improved, there is no convergence.
            	if (testSecondForImprovement(historicHWAgo, historicIter)) return false;

//                if (historicHWAgo.getFitness(0) > ((AbstractEAIndividual)pop.m_History.get(length-haltingWindow+i)).getFitness(0)) {
//                    System.out.println("( " + historic.getFitness(0) + "/" + ((AbstractEAIndividual)pop.m_History.get(length-haltingWindow+i)).getFitness(0));
//                    return false;
//                }
            }
        }
        if (false) { // plot the historic fitness values
            double[] a = new double[2];
            a[0] = 0; a[1] = 0;
            Plot plot = new Plot("HaltingWindow", "History", "Fitness", a, a);
            plot.setUnconnectedPoint(0, -1, 0);
            for (int i = haltingWindow; i > 0; i--) {
                a = speciesHistory.get(histLen-i).getFitness();
                plot.setUnconnectedPoint(haltingWindow-i+1, a[0], 0);
            }
        }
        return true;
    }

    /**
     * Define the criterion by which individual improvement is judged. The original version defined
     * improvement strictly, but for some EA this should be done more laxly. E.g. DE will hardly ever
     * stop improving slightly, so optionally use an epsilon-bound: improvement only counts if it is
     * larger than epsilon in case useEpsilonBound is true.
     * 
     * @param firstIndy
     * @param secIndy
     * @return true if the second individual has improved in relation to the first one
     */
    private boolean testSecondForImprovement(AbstractEAIndividual firstIndy, AbstractEAIndividual secIndy) {
    	if (epsilonBound > 0) {
    		double fitDiff = (new ObjectiveSpaceMetric()).distance(firstIndy, secIndy);
    		boolean ret = (secIndy.isDominatingDebConstraints(firstIndy));
    		ret = ret && (fitDiff > epsilonBound);  // there is improvement if the second is dominant and the fitness difference is larger than epsilon  
    		return ret;
    	} else return (secIndy.isDominatingDebConstraints(firstIndy));
    }
    
    private Population optimizeSpecies(Population species, boolean minorPlot) {
    	m_Optimizer.setPopulation(species);
//    	m_Optimizer.initByPopulation(species, false);
    	if (m_Optimizer instanceof EvolutionStrategies) {
    		EvolutionStrategies es = (EvolutionStrategies)m_Optimizer;
    		int mu = Math.max(1,(int)(muLambdaRatio*species.size()));
    		if (mu >= species.size()) {
    			if (TRACE) System.err.println("warning, muLambdaRatio produced mu >= lambda.. reducing to mu=lambda-1");
    			mu = Math.max(1,species.size() - 1);
    		}
    		es.setMu(mu);
    		es.setLambda(species.size());
    		if (TRACE) System.out.println("mu: "+es.getMu() + " / lambda: " + es.getLambda());
    	}
    	if (TRACE) {
    		System.out.println("Bef: spec size: " + species.size() + ", target size " + species.getTargetSize());
    		System.out.println("Best bef: " + BeanInspector.toString(m_Optimizer.getPopulation().getBestFitness()));
    	}
    	
    	if (BeanInspector.hasMethod(m_Optimizer, "getLastModelPopulation")!=null) {
    		Object pc = BeanInspector.callIfAvailable(m_Optimizer, "getLastTrainingPatterns", null);
   			System.out.println("MAPSO train set bef optSpec: " + BeanInspector.callIfAvailable(pc, "getStringRepresentation", null));
    	}

    	this.m_Optimizer.optimize();
    	Population retPop =  m_Optimizer.getPopulation();
    	
		if (TRACE) {
			System.out.println("Aft: spec size: " + retPop.size() + ", target size " + retPop.getTargetSize());
			System.out.println("Best aft: " + BeanInspector.toString(retPop.getBestFitness()));
		}
    	if (retPop.size() != retPop.getTargetSize()) {
    		if (TRACE) System.out.println("correcting popsize after opt: " + retPop.getTargetSize() + " to " + retPop.size());
    		retPop.synchSize();
    	}
    	
//    	if (useDistraction) { // distraction step
//    		if ((distraction != null) && (!distraction.isEmpty())) {
//    			System.out.println("Distraction step!!!");
//    			boolean distrHappened = false;
//    			for (int i=0; i<retPop.size(); i++) {
//    				distrHappened |= distraction.applyDistractionTo(retPop.getEAIndividual(i));
//    			}
//    			if (distrHappened) {
////    				Object distrList = species.getData("distraction");
////    				if (distr)
////    				species.addData("distr", value)
//    			}
//    		}
//    	}
    	return retPop;
    }
    
    public void optimize() {
    	Population reinitPop = null;
    	if (TRACE_STATE) {
    		printState("---- CBN Optimizing", m_doomedPop);
    		//            System.out.println("-Funcalls: "+m_Undifferentiated.getFunctionCalls());
    	}   	
    	if (m_doomedPop.size()>0) {
    		reinitPop = this.initializeIndividuals(m_doomedPop.size()); // do not add these to undifferentiated yet, that would mess up the evaluation count 
    		m_doomedPop.clear();
    		if (TRACE) System.out.println("Reinited " + reinitPop.size() + " indies... ");
    	}
    	int countIndies = (reinitPop != null ? reinitPop.size() : 0) + m_Undifferentiated.size();
    	for (int i=0; i<m_Species.size(); i++) countIndies+=m_Species.get(i).size();
    	if (TRACE) System.out.println("NumIndies: " + countIndies);;
    	if (this.m_ShowCycle > 0) {
            if (m_Undifferentiated.getGeneration()<=1) plot(m_Undifferentiated.getGeneration());
        }
        // species evolution phase


        // optimize D_0
        this.m_Undifferentiated.synchSize();
        if (m_Undifferentiated.size()>0)  {
//            this.capMutationRate(this.m_Undifferentiated, 0); // MK this sets mutation rate to 0! why?
        	m_Undifferentiated.putData(InterfaceSpeciesAware.populationTagKey, InterfaceSpeciesAware.explorerPopTag);
            m_Undifferentiated = optimizeSpecies(m_Undifferentiated, false);
        } else m_Undifferentiated.incrGeneration();

        Population curSpecies;
        // optimize the clustered species
        for (int i = this.m_Species.size() - 1; i >= 0; i--) {
            if (TRACE) System.out.println("-Deme " + i + " size: " + ((Population)this.m_Species.get(i)).size());
            curSpecies = ((Population)this.m_Species.get(i));
            curSpecies.SetFunctionCalls(0);
            curSpecies.synchSize();
//            if (isActive(curSpecies)) { // Lets have only active species...
                if ((haltingWindow > 0) && (this.testSpeciesForConvergence(curSpecies))) {
///////////////////////////////////////////// Halting Window /////////////////////////////////////////////////
//                    if (this.m_Debug) {
//                        System.out.println("Undiff.Size: " + this.m_Undifferentiated.size() +"/"+this.m_Undifferentiated.getPopulationSize());
//                        System.out.println("Diff.Size  : " + ((Population)this.m_Species.get(i)).size() +"/"+((Population)this.m_Species.get(i)).getPopulationSize());
//                    }
                    convergedCnt++;
//                    if (TRACE) 
                    if (TRACE) System.out.print("--Converged: "+convergedCnt + " - " + testSpeciesForConvergence(curSpecies));
                    if (TRACE) System.out.println(curSpecies.getBestEAIndividual());
                    	
                    // memorize the best one....
                    AbstractEAIndividual best = (AbstractEAIndividual)curSpecies.getBestEAIndividual().getClone();
//                	if (useDistraction) { // Add distractor!
//                		if (distraction == null) distraction = new Distraction(distrDefaultStrength, Distraction.METH_BEST);
//                		distraction.addDistractorFrom(curSpecies);
//                		System.out.println("** Adding distractor! " + BeanInspector.toString(distraction.getDistractionCenter(curSpecies, distraction.getDistractionMethod().getSelectedTagID())));
//                	}
                	int toReinit=0;
                	if (true) { //if (m_UseArchive) {
                    	m_Archive.add(best);
                    	m_Species.remove(i);  // remove the converged Species
                    	toReinit=curSpecies.size();
                    }
//                	else {
//                    	// reset the converged species to inactivity size = 1        
//                    	toReinit=curSpecies.size()-1;
//                    	deactivateSpecies(curSpecies, best, null);
//                    }
                	// those will not be optimized anymore, so we dont need to doom them, but can directly add them to undiff!
                	m_Undifferentiated.addPopulation(initializeIndividuals(toReinit));
                	m_Undifferentiated.incrFunctionCallsBy(toReinit);

//                    if (this.m_Debug) {
//                        System.out.println("Undiff.Size: " + this.m_Undifferentiated.size() +"/"+this.m_Undifferentiated.getPopulationSize());
//                        System.out.println("Diff.Size  : " + ((Population)this.m_Species.get(i)).size() +"/"+((Population)this.m_Species.get(i)).getPopulationSize());
//                    }
//                    if (this.m_Debug) System.out.println("--------------------------End converged");
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
                } else {
                	// actually optimize D_i
//                    this.capMutationRate(curSpecies, 0.05);
                	curSpecies.putData(InterfaceSpeciesAware.populationTagKey, InterfaceSpeciesAware.localPopTag);
                    Population optimizedSpec = optimizeSpecies(curSpecies, true);
                    this.m_Species.set(i, optimizedSpec);
                    curSpecies = ((Population)this.m_Species.get(i)); // reset to expected population, just to be sure
                }
//            }
            // This is necessary to keep track of the function calls needed
            m_Undifferentiated.incrFunctionCallsBy(curSpecies.getFunctionCalls());
            if (TRACE) System.out.println("### funcalls: "+m_Undifferentiated.getFunctionCalls());
        }

        //////////////////////
        if ((this.m_Undifferentiated.getFunctionCalls()+(reinitPop==null ? 0 : (reinitPop.size()))) % this.m_PopulationSize != 0) {
        	if (TRACE) System.out.println("### mismatching number of funcalls, inactive species?");// Correcting by " + (m_PopulationSize - (m_Undifferentiated.getFunctionCalls() % m_PopulationSize)));
//        	if (TRACE) System.out.println("### undiff " + ((isActive(m_Undifferentiated)) ? "active!" : "inactive!"));
//        	m_Undifferentiated.incrFunctionCallsBy(m_PopulationSize - (m_Undifferentiated.getFunctionCalls() % m_PopulationSize));
        } //else if (TRACE) System.out.println("### undiff active: " + isActive(m_Undifferentiated));        
        
        // possible species differentiation and convergence
        if (this.m_Undifferentiated.getGeneration()%this.m_SpeciesCycle == 0) {
            if (TRACE) System.out.println("Species cycle:");
            this.m_CAForSpeciesDifferentation.initClustering(m_Population);
            if (this.m_UseSpeciesDifferentiation) {
///////////////////////////// species differentiation phase
                if (TRACE) printState("---Species Differentation", reinitPop);
                Population[]    clusters;
                ArrayList<Population>       newSpecies = new ArrayList<Population>();
                //cluster the undifferentiated population
                clusters   = this.m_CAForSpeciesDifferentation.cluster(this.m_Undifferentiated, m_Population);
                if (TRACE) System.out.println("clustered undiff to " + clusters.length);
                replaceUndifferentiated(clusters[0]);
                for (int j = 1; j < clusters.length; j++) { // loop new clusters
                    splitFromFirst(m_Undifferentiated, clusters[j], false);
                    newSpecies.add(clusters[j]);
                }
                for (int i = 0; i < this.m_Species.size(); i++) { // loop old species
                	curSpecies = this.m_Species.get(i);
//                    if (curSpecies.size()>m_minGroupSize) { // only active populations are clustered
                    	// check if a species has differentiated any further
                        clusters = this.m_CAForSpeciesDifferentation.cluster(curSpecies, m_Population);
                        if (TRACE) System.out.println("clustered " + i + " to " + clusters.length);
                        if (clusters[0].size()>0) mergeToFirst(m_Undifferentiated, clusters[0], false);
                        for (int j = 1; j < clusters.length; j++) { // set up new species
                        	// this is treated as a split only if more than one cluster was found
                        	// so if clustering results in a list of size 2: [undiff,spec], the curSpecies only is maintained.
                            if (clusters.length<=2) clusters[j].addDataFromPopulation(curSpecies); // copy earlier data to corresponding new cluster
                            else splitFromFirst(curSpecies, clusters[j], true);
                            newSpecies.add(clusters[j]);

                        }
//                    } else {
//                        // small populations are kept directly
//                        newSpecies.add(curSpecies);
//                    }
                }
                this.m_Species = newSpecies;
                if (TRACE) printState("---After differentiation", reinitPop);

                //if (this.m_Show) this.plot();
            } // end of species differentiation

        	// plot the populations
        	if (this.m_ShowCycle > 0) {
                if ((this.m_Undifferentiated.getGeneration() <= 2)) {
                    this.plot(this.m_Undifferentiated.getGeneration());
                } else {
                    if (this.m_Undifferentiated.getGeneration()%this.m_ShowCycle == 0) this.plot(this.m_Undifferentiated.getGeneration());
                }
            }

            if (this.m_mergeSpecies && (m_Species.size()>0)) {
///////////////////////////// species merging phase
                if (TRACE) {
                	System.out.println("-Species merge:");
                }
                // first test if loners belong to any species
                int[] assocSpec = m_CAForSpeciesMerging.associateLoners(m_Undifferentiated, m_Species.toArray(new Population[m_Species.size()]), m_Population);
                for (int i=m_Undifferentiated.size()-1; i>=0; i--) { // backwards or die!
                	if (assocSpec[i]>=0) {
                		// loner i should be merged to species assocSpec[i]
                		AbstractEAIndividual tmpIndy = (AbstractEAIndividual)this.m_Undifferentiated.get(i);
                        if (m_Topology!=null) plotLine(m_Topology, tmpIndy, m_Species.get(assocSpec[i]).getBestEAIndividual());
                        this.m_Undifferentiated.remove(i);
                        m_Species.get(assocSpec[i]).add(tmpIndy); // TODO merge information from loners?
                	}
                }
                if (TRACE) printState("---After loner-merges", reinitPop);
                Population spec1, spec2;
                // test if species are close to already archived solutions - deactivate them if so
                assocSpec = m_CAForSpeciesMerging.associateLoners(m_Archive, m_Species.toArray(new Population[m_Species.size()]), m_Population);
                PriorityQueue<Integer> specToRemove = new PriorityQueue<Integer>(5,Collections.reverseOrder()); // backwards sorted or DIE!
                for (int i=m_Archive.size()-1; i>=0; i--) {
                	if (assocSpec[i]>=0) {
                		AbstractEAIndividual aIndy = m_Archive.getEAIndividual(i); 
                		spec1 = (Population)this.m_Species.get(assocSpec[i]);
                		// archived solution corresponds to an existing species
            			if (!specToRemove.contains(assocSpec[i])) {
            				// the species has not yet been deactivated
            				specToRemove.add(assocSpec[i]);
                			if (TRACE) System.out.println("Inactive merge - resetting " + spec1.size() + " surplus indies");
                			if (spec1.getBestEAIndividual().isDominating(aIndy)) {
                				// update the archived one with the better one? No rather not - it may happen that a large species is assoctiated which is quite large and spans over several optima - in that case an earlier found may get lost
//                				m_Archive.set(i, spec1.getBestEAIndividual());
                			}
                			m_doomedPop.addPopulation(spec1);
            			}
                	}
                }
                int lastRemoved = Integer.MAX_VALUE;
                while (!specToRemove.isEmpty()) { // backwards sorted or DIE!
                	int specIndex = specToRemove.poll();
                	if (specIndex > lastRemoved) System.err.println("Stupid queue!!!");
                	if (TRACE) System.out.println("Removing species at index " + specIndex);
                	m_Species.remove(specIndex); // warning, dont try to remove Integer object but index i!
                	lastRemoved = specIndex;
                }
                if (TRACE) printState("---After archive-merges", reinitPop);
                // Now test if species should be merged among each other
                for (int i1 = 0; i1 < this.m_Species.size(); i1++) {
                    spec1 = (Population)this.m_Species.get(i1);
                    for (int i2 = i1+1; i2 < this.m_Species.size(); i2++) {
                        spec2 = (Population)this.m_Species.get(i2);
                        if (this.m_CAForSpeciesMerging.mergingSpecies(spec1, spec2, m_Population)) {
                        	if (TRACE) System.out.println("----Merging species (" + i1 +", " +i2 +") ["+spec1.size()+"/"+spec2.size()+"]");
                        	mergeToFirst(spec1, spec2, true); 
                        	this.m_Species.remove(i2);
                        	i2--;
                        }
                    }
                }
                if (TRACE) printState("---After merging", reinitPop);
            } /// end of species merging

            if (m_maxSpeciesSize >= m_minGroupSize) {
            	// reinit worst n individuals from all species which are too large
            	for (int i=0; i<m_Species.size(); i++) {
            		Population curSpec = m_Species.get(i);
            		if (curSpec.size()>m_maxSpeciesSize) {
            			ArrayList<AbstractEAIndividual> sorted = curSpec.getSorted(reduceSizeComparator);
            			for (int k=m_maxSpeciesSize; k<sorted.size();k++)  {
            				if (curSpec.remove(sorted.get(k))) {
            					m_doomedPop.add(sorted.get(k));
            				}
            			}
//            			reinitCount = sorted.size()-m_maxSpeciesSize;
//            			curSpec.setPopulationSize(m_maxSpeciesSize);
//            			if (TRACE) System.out.println("Reduced spec " + i + " to size " + curSpec.size() + ", reinit of " + reinitCount + " indies immanent...");
//                        this.m_Undifferentiated.addPopulation(this.initializeIndividuals(reinitCount));
//                        this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize()+reinitCount);
            		}
            	}
            }
        } // end of species cycle

        // add new individuals from last step to undifferentiated set
        if ((reinitPop!=null) && (reinitPop.size()>0)) {
        	m_Undifferentiated.addPopulation(reinitPop);
        	m_Undifferentiated.incrFunctionCallsBy(reinitPop.size());
        }
    	m_Undifferentiated.setTargetSize(m_Undifferentiated.size());
        // output the result
        if (TRACE) System.out.println("-Funcalls: " + this.m_Undifferentiated.getFunctionCalls());
        
        synchronized (m_Population) { // fill the m_Population instance with the current individuals from undiff, spec, etc.
        	this.m_Population = (Population)this.m_Undifferentiated.clone();
        	m_Population.setUseHistory(true);
        	for (int i = 0; i < this.m_Species.size(); i++) {
        		this.m_Population.addPopulation((Population)this.m_Species.get(i));
        	}
        	if (m_doomedPop.size()>0) m_Population.addPopulation(m_doomedPop); // this is just so that the numbers match up...
        	m_Population.synchSize();
        	if (TRACE) {
        		System.out.println("Doomed size: " + m_doomedPop.size());
        		System.out.println("Population size: " + this.m_Population.size());
        	}
        	if (m_Population.size()!=m_PopulationSize) {
        		System.err.println("Warning: Invalid population size in CBNEA! " + m_Population.size());
        	}
        	if (TRACE_STATE) {
        		printState("---- EoCBN", m_doomedPop);
        		System.out.println("Archive: " + m_Archive.getStringRepresentation());
        	}
        }
//        if (TRACE) {
//        	// this is just a test adding all species centers as distractors with high strength
//        	Distraction distr = new Distraction(5., 0, m_Species);
//        	if (!distr.isEmpty()) {
//        		double[] distVect = distr.calcDistractionFor(m_Undifferentiated.getBestEAIndividual());
//        		System.out.println("species distract best towards " + BeanInspector.toString(distVect));
//        	}
//        }
        
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }
//
//    /**
//     * Unite all current species and the undiff. pop and return
//     * the merged set. Note that data such as generation, evaluations etc
//     * are not copied!
//     * 
//     * @return
//     */
//	private Population getCurrentPop() {
//		Population pop = new Population(getPopulationSize());
//		pop.addPopulation(m_Undifferentiated);
//		for (int i=0; i<m_Species.size(); i++) pop.addPopulation(m_Species.get(i));
//		pop.synchSize();
//		return pop;
//	}

	/**
     * Replace the undifferentiated population with the given one.
     * 
     * @param pop
     */
	private void replaceUndifferentiated(Population pop) {
        // this.m_Undifferentiated = ClusterResult[0];
		m_Undifferentiated.clear();
		m_Undifferentiated.addPopulation(pop);
	}

	private void printState(String headline, Population reinit) {
		System.out.print(headline + ", Gen. " + this.m_Undifferentiated.getGeneration());
		System.out.print(" - Undiff.: " + specTag(m_Undifferentiated));
		System.out.print(", Demes: "); 
		int sum=m_Undifferentiated.size() + (reinit==null ? 0 : reinit.size());
		if (m_Species.size()>0) {
			sum+=m_Species.get(0).size();
			System.out.print(specTag(m_Species.get(0)));
		    for (int i=1; i<m_Species.size(); i++) {
		    	System.out.print(", " + specTag(m_Species.get(i))) ;
		    	sum+=m_Species.get(i).size();
		    }
		}
		System.out.println(", reinit: " + (reinit==null ? 0 : reinit.size()) + ", sum: " + sum);
	}
	
	private String specTag(Population spec) {
		return spec.size() + "("+spec.getGeneration()+((spec.hasData("MAPSOModelInformation")) ? "/"+(BeanInspector.callIfAvailable(spec.getData("MAPSOModelInformation"), "getStringRepresentation",null)) : "")+ ")" ;
	}

    /**
     * Merge two species by adding the second to the first. Keep the longer history. The second
     * species should be deactivated after merging.
     * 
     * @param pop1
     * @param pop2
     */
    protected void mergeToFirst(Population spec1, Population spec2, boolean plot) {
    	if (plot && (m_Topology!=null)) plotLine(m_Topology, spec1.getBestEAIndividual(), spec2.getBestEAIndividual());
    	spec1.addPopulation(spec2);
    	// keep longer history
        if (spec2.getHistoryLength() > spec1.getHistoryLength()) spec1.SetHistory(spec2.getHistory());
        if (spec2.getGeneration() > spec1.getGeneration()) spec1.setGenerationTo(spec2.getGeneration());
        // possibly notify the optimizer of the merging event to merge population based information
        if (m_Optimizer instanceof InterfaceSpeciesAware) ((InterfaceSpeciesAware)m_Optimizer).mergeToFirstPopulationEvent(spec1, spec2);
    }

    /**
     * A split event will reset the new species model so as to have a fresh start.
     * 
     * @param parentSp
     * @param newSp
     * @param startAtP1Gen
     */
    protected void splitFromFirst(Population parentSp, Population newSp, boolean startAtP1Gen) {
		newSp.setTargetSize(newSp.size());
		newSp.setUseHistory(true);
    	if (startAtP1Gen) { // start explicitely as a child population of p1
    		newSp.setGenerationTo(parentSp.getGeneration());
    		newSp.SetHistory((ArrayList<AbstractEAIndividual>) parentSp.getHistory().clone());
    	} else { // start anew (from undiff)
        	newSp.setGenerationTo(0);
            newSp.SetHistory(new ArrayList<AbstractEAIndividual>());
    	}
    	
        if (m_Optimizer instanceof InterfaceSpeciesAware) ((InterfaceSpeciesAware)m_Optimizer).splitFromFirst(parentSp, newSp);
	}

//    /**
//     * Return true if the given population is considered active.
//     *  
//     * @param pop	a population
//     * @return true, if pop is considered an active population, else false
//     */
//    protected boolean isActive(Population pop) {
//    	return (pop.size() >= m_actSpecSize);
//    }
    
//    /**
//     * Deactivate a given species by removing all individuals and inserting
//     * only the given survivor, sets the population size to one.
//     * 
//     * @param spec
//     */
//    protected void deactivateSpecies(Population spec, AbstractEAIndividual survivor, Population collectDoomed) {
//    	if (!spec.remove(survivor)) System.err.println("WARNING: removing survivor failed in CBN.deactivateSpecies!");;
//    	if (collectDoomed!=null) collectDoomed.addPopulation(spec);
//    	spec.clear();
//    	spec.add(survivor);
//    	spec.setPopulationSize(1);
//    }
    
//    public int countActiveSpec() {
//    	int k = 0;
//    	for (int i=0; i<m_Species.size(); i++) {
//    		if (isActive(m_Species.get(i))) k++;
//    	}
//    	return k;
//    }
    
    /** This method allows an optimizer to register a change in the optimizer.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name) {
        //Population population = ((InterfaceOptimizer)source).getPopulation();
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
        this.m_Optimizer.SetProblem(this.m_Problem);
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
        result += "Genetic Algorithm:\n";
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
        return "This is a versatile species based niching EA method.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "CBN-EA";
    }

    public Population getPopulation() {
//        this.m_Population = (Population)m_Undifferentiated.clone();
//        for (int i = 0; i < this.m_Species.size(); i++) this.m_Population.addPopulation((Population)this.m_Species.get(i));
//        m_Population.setPopulationSize(m_Population.size()); // set it to true value
        return this.m_Population;
    }
    
    public void setPopulation(Population pop){
        this.m_Undifferentiated = pop;
        pop.setUseHistory(true);
    }
    
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
    
    public SolutionSet getAllSolutions() {
    	// return inactive species
    	Population sols = (Population)m_Archive.clone();
    	sols.addPopulation(getPopulation());
    	sols.synchSize();
    	return new SolutionSet(getPopulation(), sols);
    }

//    /** Clearing removes all but the best individuals from an identified species.
//     * @return The current status of this flag
//     */
//    public boolean getApplyClearing() {
//        return this.m_UseClearing;
//    }
//    public void setApplyClearing(boolean b){
//        this.m_UseClearing = b;
//    }
//    public String applyClearingTipText() {
//        return "Clearing removes all but the best individuals from an identified species.";
//    }
    
    /** This method allows you to set/get the switch that toggles the use
     * of species convergence.
     * @return The current status of this flag
     */
    public boolean isUseMerging() {
        return this.m_mergeSpecies;
    }
    public void setUseMerging(boolean b){
        this.m_mergeSpecies = b;
        GenericObjectEditor.setHideProperty(this.getClass(), "mergingCA", !m_mergeSpecies);
    }
    public String useMergingTipText() {
        return "Toggle the use of species merging.";
    }
    
    /** Choose a population based optimizing technique to use
     * @return The current optimizing method
     */
    public InterfaceOptimizer getOptimizer() {
        return this.m_Optimizer;
    }
    public void setOptimizer(InterfaceOptimizer b){
        this.m_Optimizer = b;
    	if (b instanceof EvolutionStrategies) {
    		EvolutionStrategies es = (EvolutionStrategies)b;
    		setMuLambdaRatio(es.getMu()/(double)es.getLambda());
    	}
    }
    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /** The cluster algorithm on which the species differentiation is based
     * @return The current clustering method
     */
    public InterfaceClustering getDifferentiationCA() {
        return this.m_CAForSpeciesDifferentation;
    }
    public void setDifferentiationCA(InterfaceClustering b){
        this.m_CAForSpeciesDifferentation = b;
    }
    public String differentiationCATipText() {
        return "The cluster algorithm on which the species differentation is based.";
    }

    /** The Cluster Algorithm on which the species convergence is based.
     * @return The current clustering method
     */
    public InterfaceClustering getMergingCA() {
        return this.m_CAForSpeciesMerging;
    }
    public void setMergingCA(InterfaceClustering b){
        this.m_CAForSpeciesMerging = b;
    }
    public String mergingCATipText() {
        return "The cluster algorithm on which the species merging is based.";
    }

//    public void setUseArchive(boolean v) {
//    	m_UseArchive = v;
//    }
//    public boolean isUseArchive() {
//    	return m_UseArchive;
//    }
//    public String useArchiveTipText() {
//    	return "Toggle usage of an archive where converged species are saved and the individuals reinitialized.";
//    }
    
    /** Determines how often species differentation/convergence is performed.
     * @return This number gives the generations when specification is performed.
     */
    public int getSpeciesCycle() {
        return this.m_SpeciesCycle;
    }
    public void setSpeciesCycle(int b){
        this.m_SpeciesCycle = b;
    }
    public String speciesCycleTipText() {
        return "Determines how often species differentation/convergence is performed.";
    }

    /** TDetermines how often show is performed.
     * @return This number gives the generations when specification is performed.
     */
    public int getShowCycle() {
        return this.m_ShowCycle;
    }
    public void setShowCycle(int b){
        this.m_ShowCycle = b;
    }
    public String showCycleTipText() {
        return "Determines how often show is performed (generations); set to zero to deactivate.";
    }
    /** Determines the size of the initial population.
     * @return This number gives initial population size.
     */
    public int getPopulationSize() {
        return this.m_PopulationSize;
    }
    public void setPopulationSize(int b){
        this.m_PopulationSize = b;
    }
    public String populationSizeTipText() {
        return "Determines the size of the initial population.";
    }

    public String[] getGOEPropertyUpdateLinks() {
    	return new String[] {"population", "populationSize", "populationSize", "population"};
    }
    
//	/**
//	 * @return the muLambdaRatio
//	 */
//	public double getMuLambdaRatio() {
//		return muLambdaRatio;
//	}

	/**
	 * This is now set if an ES is set as optimizer.
	 * @param muLambdaRatio the muLambdaRatio to set
	 */
	public void setMuLambdaRatio(double muLambdaRatio) {
		this.muLambdaRatio = muLambdaRatio;
	}

	/**
	 * @return the haltingWindow
	 */
	public int getHaltingWindow() {
		return haltingWindow;
	}

	/**
	 * @param haltingWindow the haltingWindow to set
	 */
	public void setHaltingWindow(int haltingWindow) {
		this.haltingWindow = haltingWindow;
	}

	public String haltingWindowTipText() {
		return "Number of generations after which a cluster without improvement is seen as converged and deactivated; set to zero to disable.";
	}

//	/**
//	 * @return the useDistraction
//	 */
//	public boolean isDistractionActive() {
//		return useDistraction;
//	}
//
//	/**
//	 * @param useDistraction the useDistraction to set
//	 */
//	public void setDistractionActive(boolean useDistraction) {
//		this.useDistraction = useDistraction;
//	}

//	/**
//	 * @return the distrDefaultStrength
//	 */
//	public double getDistrStrength() {
//		return distrDefaultStrength;
//	}
//
//	/**
//	 * @param distrDefaultStrength the distrDefaultStrength to set
//	 */
//	public void setDistrStrength(double distrDefaultStrength) {
//		this.distrDefaultStrength = distrDefaultStrength;
//		distraction.setDefaultStrength(distrDefaultStrength);
//	}

	/**
	 * @return the sleepTime
	 */
	public int getSleepTime() {
		return sleepTime;
	}
	/**
	 * @param sleepTime the sleepTime to set
	 */
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	public String sleepTimeTipText() {
		return "Let the thread sleep between iterations (nice when visualizing)";
	}

	/**
	 * @return the epsilonBound
	 */
	public double getEpsilonBound() {
		return epsilonBound;
	}
	/**
	 * @param epsilonBound the epsilonBound to set
	 */
	public void setEpsilonBound(double epsilonBound) {
		this.epsilonBound = epsilonBound;
	}
	public String epsilonBoundTipText() {
		return "If fitness improves less than this value within the halting window, convergence is assumed. May be set to zero.";
	}

	public String getAdditionalFileStringHeader(PopulationInterface pop) {
		return " Undiff. \t #Act.spec. \tAvg.Spec.Meas. \t #Archived."; 
	}

	public String getAdditionalFileStringValue(PopulationInterface pop) {
//		int actives = countActiveSpec();
		return m_Undifferentiated.size() + " \t " + m_Species.size() + " \t " + BeanInspector.toString(getAvgSpeciesMeasures()[0]) + " \t " + (m_Archive.size());
	}

	/**
	 * Calculate average of Population measures (mean, minimal and maximal distance within a species)
	 * @return average population measures
	 */
    protected double[] getAvgSpeciesMeasures() {
		if (m_Species==null || (m_Species.size()==0)) return new double[]{0};
		else {
			double[] measures = m_Species.get(0).getPopulationMeasures();
			for (int i=1; i<m_Species.size(); i++) {
				Mathematics.vvAdd(measures, m_Species.get(i).getPopulationMeasures(), measures);
			}
			if (m_Species.size()>1) Mathematics.svDiv((double)m_Species.size(), measures, measures); 
			return measures;
		}
	}

	public int getMaxSpeciesSize() {
		return m_maxSpeciesSize;
	}
    public void setMaxSpeciesSize(int mMaxSpeciesSize) {
		m_maxSpeciesSize = mMaxSpeciesSize;
		GenericObjectEditor.setShowProperty(this.getClass(), "reduceSizeComparator", (m_maxSpeciesSize >= m_minGroupSize));
	}
    public String maxSpeciesSizeTipText() {
    	return "If >= " + m_minGroupSize + ", larger species are reduced to the given size by reinitializing the worst individuals.";  
    }

    public String reduceSizeComparatorTipText() {
    	return "Set the comparator used to define the 'worst' individuals when reducing species size.";
    }
	public AbstractEAIndividualComparator getReduceSizeComparator() {
		return reduceSizeComparator;
	}
	public void setReduceSizeComparator(
			AbstractEAIndividualComparator reduceSizeComparator) {
		this.reduceSizeComparator = reduceSizeComparator;
	}
	
    public String[] customPropertyOrder() {
    	return new String[]{"mergingCA", "differentiationCA"};
    }
    
}
