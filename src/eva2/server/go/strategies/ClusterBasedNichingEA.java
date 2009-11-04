package eva2.server.go.strategies;

import java.util.ArrayList;

import eva2.gui.BeanInspector;
import eva2.gui.Chart2DDPointIconCircle;
import eva2.gui.Chart2DDPointIconText;
import eva2.gui.GenericObjectEditor;
import eva2.gui.GraphPointSet;
import eva2.gui.Plot;
import eva2.gui.TopoPlot;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.cluster.ClusteringDensityBased;
import eva2.server.go.operators.cluster.InterfaceClustering;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESGlobal;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.Interface2DBorderProblem;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.problems.TF1Problem;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.chart2d.DPointSet;
import eva2.tools.math.RNG;

/** The infamuos clustering based niching EA, still under construction.
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

    private Population                      m_Population                    = new Population();
    private transient Population			m_Archive						= new Population();
    public ArrayList<Population>			m_Species              			= new ArrayList<Population>();
    public Population                       m_Undifferentiated              = new Population();
    private InterfaceOptimizationProblem    m_Problem                       = new B1Problem();
    private InterfaceOptimizer              m_Optimizer                     = new GeneticAlgorithm();
    private InterfaceClustering             m_CAForSpeciesDifferentation    = new ClusteringDensityBased();
    private InterfaceClustering             m_CAForSpeciesConvergence       = new ClusteringDensityBased();
    //private Distraction						distraction						= null;
//    private boolean 						useDistraction	= false;
//    private double 							distrDefaultStrength = 1.;
    
    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    private int                             m_SpeciesCycle                  = 1;
    // from which size on is a species considered active 
    private int 							m_actSpecSize					= 2;
    private int 							m_minGroupSize					= 3;
//    private boolean                         m_UseClearing                   = false;
    private boolean							m_UseArchive					= true;
    private boolean                         m_UseSpeciesDifferentation      = true;
    private boolean                         m_UseSpeciesMerging         = true;
//    private boolean                         m_UseHaltingWindow              = true;
    private int                             m_PopulationSize                = 50;
    private int								convergedCnt					= 0;

    private static boolean                  TRACE     = false;
    private int                             m_ShowCycle = 0;
    transient private TopoPlot              m_Topology;
    private int                 			haltingWindow         			 = 15;
    private double							muLambdaRatio					 = 0.5;


    public ClusterBasedNichingEA() {
        this.m_CAForSpeciesConvergence = new ClusteringDensityBased();
        ((ClusteringDensityBased)this.m_CAForSpeciesConvergence).setMinimumGroupSize(m_minGroupSize);
//        if (useDistraction) distraction = new Distraction();
    }

    public ClusterBasedNichingEA(ClusterBasedNichingEA a) {    
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Optimizer                    = (InterfaceOptimizer)a.m_Optimizer.clone();
        this.m_Species                      = (ArrayList)(a.m_Species.clone());
        this.m_Undifferentiated             = (Population)a.m_Undifferentiated.clone();
        this.m_CAForSpeciesConvergence      = (InterfaceClustering)this.m_CAForSpeciesConvergence.clone();
        this.m_CAForSpeciesDifferentation   = (InterfaceClustering)this.m_CAForSpeciesDifferentation.clone();
        this.m_SpeciesCycle                 = a.m_SpeciesCycle;
        this.m_actSpecSize					= a.m_actSpecSize;
        this.m_minGroupSize					= a.m_minGroupSize;
//        this.m_UseClearing                  = a.m_UseClearing;
        this.m_UseSpeciesDifferentation     = a.m_UseSpeciesDifferentation;
        this.m_UseSpeciesMerging        = a.m_UseSpeciesMerging;
//        this.m_UseHaltingWindow             = a.m_UseHaltingWindow;
        this.m_PopulationSize               = a.m_PopulationSize;
        this.haltingWindow					= a.haltingWindow;
    }

    public Object clone() {
        return (Object) new ClusterBasedNichingEA(this);
    }

    public void init() {
        this.m_Optimizer.addPopulationChangedEventListener(this);
        this.m_Undifferentiated = new Population();
        this.m_Undifferentiated.setUseHistory(true);
        this.m_Undifferentiated.setPopulationSize(this.m_PopulationSize);
        this.m_Species = new ArrayList<Population>();
        this.m_Problem.initPopulation(this.m_Undifferentiated);
        this.evaluatePopulation(this.m_Undifferentiated);
        this.m_Optimizer.initByPopulation(m_Undifferentiated, false);
        m_Archive						= new Population();
        convergedCnt = 0;
        this.m_Undifferentiated = m_Optimizer.getPopulation(); // required for changes to the population by the optimizer
//        this.getPopulation().setUseHistory(true); // TODO this makes no sense if getPopulation clones the pop.
        this.firePropertyChangedEvent("FirstGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Optimizer.addPopulationChangedEventListener(this);
        pop.setUseHistory(true);
        this.m_Undifferentiated = (Population)pop.clone();
        if (reset) this.m_Undifferentiated.init();
        this.m_Undifferentiated.setPopulationSize(this.m_PopulationSize);
        this.m_Species = new ArrayList<Population>();
        m_Archive						= new Population();
        convergedCnt = 0;
        this.evaluatePopulation(this.m_Undifferentiated);
        this.m_Optimizer.initByPopulation(m_Undifferentiated, false);
        this.m_Undifferentiated = m_Optimizer.getPopulation(); // required for changes to the population by the optimizer
        this.firePropertyChangedEvent("FirstGenerationPerformed");
    }
    
    public void hideHideable() {
    	GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    	setUseMerging(isUseMerging());
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
            InterfaceDataTypeDouble tmpIndy1, best;
            Population              pop;

            this.m_Topology          = new TopoPlot("CBN-Species at gen. " + gen,"x","y",a,a);
            this.m_Topology.gridx = 60;
            this.m_Topology.gridy = 60;
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
                best = (InterfaceDataTypeDouble)pop.getBestIndividual();
                if (isActive(pop)) {
                    for (int j = 0; j < pop.size(); j++) {
                        popRep = new DPointSet();
                        tmpIndy1 = (InterfaceDataTypeDouble)pop.get(j);
                        popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                        this.m_Topology.getFunctionArea().addDElement(popRep);

                        popRep      = new DPointSet();
                        popRep.setConnected(true);
                        popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                        popRep.addDPoint(new DPoint(best.getDoubleData()[0], best.getDoubleData()[1]));
                        this.m_Topology.getFunctionArea().addDElement(popRep);
                    }
                } else {
                    // this is an inactive species
                    plotInactive((InterfaceDataTypeDouble)pop.get(0));
                }
            }
            for (int i = 0; i < this.m_Archive.size(); i++) {
            	plotInactive((InterfaceDataTypeDouble)m_Archive.get(i));
            }
        }
    }

	private void plotInactive(InterfaceDataTypeDouble tmpIndy) {
		DPointSet popRep;
		popRep = new DPointSet();
		popRep.addDPoint(new DPoint(tmpIndy.getDoubleData()[0], tmpIndy.getDoubleData()[1]));
		double d = Math.round(100*((AbstractEAIndividual)tmpIndy).getFitness(0))/(double)100;
		DPointIcon icon = new Chart2DDPointIconText(""+d);
		((Chart2DDPointIconText)icon).setIcon(new Chart2DDPointIconCircle());
		popRep.setIcon(icon);
		this.m_Topology.getFunctionArea().addDElement(popRep);
	}

    /** 
     * This method is used to cap the mutation rate.
     * For the global ES mutation, set the mutation rate to not more than cap. 
     * 
     * @param pop   The population
     * @param cap   The maximum mutation rate
     */
    private void capMutationRate(Population pop, double cap) {
        AbstractEAIndividual    indy;
        InterfaceMutation       mutator;

        if (cap <= 0) return;
        for (int i = 0; i < pop.size(); i++) {
            indy = (AbstractEAIndividual) pop.get(i);
            mutator = indy.getMutationOperator();
            if (mutator instanceof MutateESGlobal) {
                ((MutateESGlobal)mutator).setMutationStepSize(Math.min(cap, ((MutateESGlobal)mutator).getMutationStepSize()));
            }
        }
    }

    /** This method is called to generate n freshly initialized individuals
     * @param n     Number of new individuals
     * @return A population of new individuals
     */
    private Population initializeIndividuals(int n) {
        Population result = new Population();
        result.setUseHistory(true);
        result.setPopulationSize(n);
        //@todo: crossover between species is to be impelemented
        this.m_Problem.initPopulation(result);
        this.m_Problem.evaluate(result);
        this.capMutationRate(result, RNG.randomDouble(0.001, 0.1));
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
        ArrayList<AbstractEAIndividual> tmpA = pop.m_History;
        int     length = pop.m_History.size();

        if (length <= haltingWindow) {
//        	System.out.println("not long enough... gen " + pop.getGeneration());
        	return false;
        } else {
            AbstractEAIndividual historicHWAgo = ((AbstractEAIndividual)pop.m_History.get(length-haltingWindow));
            for (int i = 1; i < haltingWindow; i++) {
            	// if historic[-hW] is worse than historic[-hW+] return false
            	AbstractEAIndividual historicIter = pop.m_History.get(length-haltingWindow+i);
            	if (historicIter.isDominatingDebConstraints(historicHWAgo)) return false;
//                if (historicHWAgo.getFitness(0) > ((AbstractEAIndividual)pop.m_History.get(length-haltingWindow+i)).getFitness(0)) {
//                    System.out.println("( " + historic.getFitness(0) + "/" + ((AbstractEAIndividual)pop.m_History.get(length-haltingWindow+i)).getFitness(0));
//                    return false;
//                }
            }
        }
        if (TRACE) {
            double[] a = new double[2];
            a[0] = 0; a[1] = 0;
            Plot plot = new Plot("HaltingWindow", "History", "Fitness", a, a);
            for (int i = 0; i < tmpA.size(); i++) {
                a = ((AbstractEAIndividual)tmpA.get(i)).getFitness();
                plot.setUnconnectedPoint(i, a[0], 0);
            }
        }
        return true;
    }

    private Population optimizeSpecies(Population species) {
    	m_Optimizer.setPopulation(species);
//    	m_Optimizer.initByPopulation(species, false);
    	if (m_Optimizer instanceof EvolutionStrategies) {
    		EvolutionStrategies es = (EvolutionStrategies)m_Optimizer;
    		int mu = Math.max(1,(int)(muLambdaRatio*species.size()));
    		if (mu >= species.size()) {
    			if (TRACE) System.err.println("warning, muLambdaRatio produced mu >= lambda.. reducing to mu=lambda-1");
    			mu = species.size() - 1;
    		}
    		es.setMu(mu);
    		es.setLambda(species.size());
    		if (TRACE) System.out.println("mu: "+es.getMu() + " / lambda: " + es.getLambda());
    	}
    	if (TRACE) {
    		System.out.println("Bef: spec size: " + species.size() + ", says its " + species.getPopulationSize());
    		System.out.println("Best bef: " + BeanInspector.toString(m_Optimizer.getPopulation().getBestFitness()));
    	}
    	this.m_Optimizer.optimize();
    	Population retPop =  m_Optimizer.getPopulation();
		if (TRACE) {
			System.out.println("Aft: spec size: " + retPop.size() + ", says its " + retPop.getPopulationSize());
			System.out.println("Best aft: " + BeanInspector.toString(retPop.getBestFitness()));
		}
    	if (retPop.size() != retPop.getPopulationSize()) {
    		if (TRACE) System.out.println("correcting popsize after opt: " + retPop.getPopulationSize() + " to " + retPop.size());
    		retPop.setPopulationSize(retPop.size());
    	}
    	
//    	if (useDistraction) {
//    		if ((distraction != null) && (!distraction.isEmpty())) {
//    			System.out.println("Distraction step!!!");
//    			for (int i=0; i<retPop.size(); i++) {
//    				distraction.applyDistractionTo(retPop.getEAIndividual(i));
//    			}
//    		}
//    	}
    	return retPop;
    }
    
    public void optimize() {
    	// plot the populations
    	if (this.m_ShowCycle > 0) {
            if ((this.m_Undifferentiated.getGeneration() == 0) || (this.m_Undifferentiated.getGeneration() == 1) || (this.m_Undifferentiated.getGeneration() == 2)) {
                this.plot(this.m_Undifferentiated.getGeneration());
            } else {
                if (this.m_Undifferentiated.getGeneration()%this.m_ShowCycle == 0) this.plot(this.m_Undifferentiated.getGeneration());
            }
        }

        // species evolution phase
        if (TRACE) {
            System.out.println("");
            System.out.println("Optimizing Generation " + this.m_Undifferentiated.getGeneration());
        }

        // optimize D_0
        this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.size());
        if (isActive(m_Undifferentiated)) {
            this.capMutationRate(this.m_Undifferentiated, 0); // MK TODO this sets mutation rate to 0! why?
            m_Undifferentiated = optimizeSpecies(m_Undifferentiated);
        } else {
            this.m_Undifferentiated.incrGeneration();
            //m_Undifferentiated.incrFunctionCallsby(m_Undifferentiated.size()); // TODO this is not optimal, an evaluation is wasted to keep N steps per gen.
        }

        // now the population is of max size 
        if (TRACE) {
            System.out.println("-Undiff. size: " + this.m_Undifferentiated.size());
            System.out.println("-Number of Demes: " + this.m_Species.size());
            System.out.println("-Funcalls: "+m_Undifferentiated.getFunctionCalls());
        }

        Population curSpecies;
        // optimize the clustered species
        for (int i = this.m_Species.size() - 1; i >= 0; i--) {
            if (TRACE) System.out.println("-Deme " + i + " size: " + ((Population)this.m_Species.get(i)).size());
            curSpecies = ((Population)this.m_Species.get(i));
            curSpecies.SetFunctionCalls(0);
            curSpecies.setPopulationSize(curSpecies.size());
            if (isActive(curSpecies)) {
                if ((haltingWindow > 0) && (this.testSpeciesForConvergence(curSpecies))) {
///////////////////////////////////////////// Halting Window /////////////////////////////////////////////////
//                    if (this.m_Debug) {
//                        System.out.println("Undiff.Size: " + this.m_Undifferentiated.size() +"/"+this.m_Undifferentiated.getPopulationSize());
//                        System.out.println("Diff.Size  : " + ((Population)this.m_Species.get(i)).size() +"/"+((Population)this.m_Species.get(i)).getPopulationSize());
//                    }
                    convergedCnt++;
                    if (TRACE) System.out.println("--Converged: "+convergedCnt);
                    // memorize the best one....
                    AbstractEAIndividual best = (AbstractEAIndividual)curSpecies.getBestEAIndividual().getClone();

                    int reinitCount = -1;

//                	if (useDistraction) {
//                		if (distraction == null) distraction = new Distraction(distrDefaultStrength, Distraction.METH_BEST);
//                		distraction.addDistractorFrom(curSpecies);
//                		System.out.println("** Adding distractor! " + BeanInspector.toString(distraction.getDistractionCenter(curSpecies, distraction.getDistractionMethod().getSelectedTagID())));
//                	}

                	if (m_UseArchive) {
                    	m_Archive.add(best);
                    	m_Species.remove(i);  //REMOVES the converged Species
                    	reinitCount = curSpecies.size();	// add all as new
                    } else {
                    	// now reset the converged species to inactivity size = 1
                    	reinitCount = curSpecies.size()-1;    // add all but one as new           
                    	deactivateSpecies(curSpecies, best);
                    }
                    // reinit the surplus individuals and add these new individuals to undifferentiated
                    m_Undifferentiated.addPopulation(this.initializeIndividuals(reinitCount));
                    m_Undifferentiated.incrFunctionCallsBy(reinitCount);
                    m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize()+reinitCount);
//                    if (this.m_Debug) {
//                        System.out.println("Undiff.Size: " + this.m_Undifferentiated.size() +"/"+this.m_Undifferentiated.getPopulationSize());
//                        System.out.println("Diff.Size  : " + ((Population)this.m_Species.get(i)).size() +"/"+((Population)this.m_Species.get(i)).getPopulationSize());
//                    }
//                    if (this.m_Debug) System.out.println("--------------------------End converged");
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
                } else {
                	// actually optimize D_i
                    this.capMutationRate(curSpecies, 0.05);
                    Population optimizedSpec = optimizeSpecies(curSpecies);
                    this.m_Species.set(i, optimizedSpec);
                    curSpecies = ((Population)this.m_Species.get(i)); // reset to expected population, just to be sure
                }
            } else {
                // a single individual species, this element is inactive
            	if (TRACE) System.out.println("inactive species not optimized");
//            	curSpecies.incrFunctionCallsby(curSpecies.size()); // was todo: not so good
            }
            // This is necessary to keep track of the function calls needed
            this.m_Undifferentiated.SetFunctionCalls(this.m_Undifferentiated.getFunctionCalls() + curSpecies.getFunctionCalls());
            if (TRACE) System.out.println("### funcalls: "+m_Undifferentiated.getFunctionCalls());
        }
        if (TRACE) System.out.println("-Number of Demes: " + this.m_Species.size());

        //////////////////////
    	if (!isActive(m_Undifferentiated)) {
    		if (TRACE) System.out.println("Inactive Undiff-pop, adding " + m_Undifferentiated.size() + " fun calls...");
    		m_Undifferentiated.incrFunctionCallsBy(m_Undifferentiated.size());
    	}
        if (this.m_Undifferentiated.getFunctionCalls() % this.m_PopulationSize != 0) {
        	if (TRACE) System.out.println("### mismatching number of funcalls, inactive species? Correcting by " + (m_PopulationSize - (m_Undifferentiated.getFunctionCalls() % m_PopulationSize)));
        	if (TRACE) System.out.println("### undiff " + ((isActive(m_Undifferentiated)) ? "active!" : "inactive!"));
        	m_Undifferentiated.incrFunctionCallsBy(m_PopulationSize - (m_Undifferentiated.getFunctionCalls() % m_PopulationSize));
        } else if (TRACE) System.out.println("### undiff active: " + isActive(m_Undifferentiated));        
        
        // possible species differentiation and convergence
        if (this.m_Undifferentiated.getGeneration()%this.m_SpeciesCycle == 0) {
            if (TRACE) System.out.println("Species cycle:");
            
            if (this.m_UseSpeciesDifferentation) {
///////////////////////////// species differentiation phase
                if (TRACE) System.out.println("-Species Differentation:");
                Population[]    ClusterResult;
                ArrayList<Population>       newSpecies = new ArrayList<Population>();
                //cluster the undifferentiated population
                ClusterResult   = this.m_CAForSpeciesDifferentation.cluster(this.m_Undifferentiated);
                this.m_Undifferentiated = ClusterResult[0];
                for (int j = 1; j < ClusterResult.length; j++) {
                	ClusterResult[j].setUseHistory(true);
                	ClusterResult[j].setGenerationTo(0);
                    ClusterResult[j].m_History = new ArrayList<AbstractEAIndividual>();
                    newSpecies.add(ClusterResult[j]);
                }
                for (int i = 0; i < this.m_Species.size(); i++) {
                	curSpecies = this.m_Species.get(i);
                    if (isActive(curSpecies)) {
                        // only active populations are clustered
                        ClusterResult = this.m_CAForSpeciesDifferentation.cluster(curSpecies);
                        this.m_Undifferentiated.addPopulation(ClusterResult[0]);
//                        ClusterResult[0].setUseHistory(true);
                        this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize() + ClusterResult[0].size());
                        for (int j = 1; j < ClusterResult.length; j++) { // set up new species
                            ClusterResult[j].setPopulationSize(ClusterResult[j].size());
                        	ClusterResult[j].setUseHistory(true);
//                            if (ClusterResult.length > 2) ClusterResult[j].m_History = new ArrayList(); // mk: why min 3? Ill copy the history from the original pop for any j...
                            ClusterResult[j].m_History = (ArrayList<AbstractEAIndividual>) curSpecies.m_History.clone();
                            newSpecies.add(ClusterResult[j]);
                        }
                    } else {
                        // inactive populations are added directly
                        newSpecies.add(this.m_Species.get(i));
                    }
                }
                this.m_Species = newSpecies;
                if (TRACE) {
                    System.out.println("--Number of species: " + this.m_Species.size());
                    System.out.println("---Undiff size: " + this.m_Undifferentiated.size());
                    for (int i = 0; i < this.m_Species.size(); i++) {
                        System.out.println("---Deme " + i + " size: " + ((Population)this.m_Species.get(i)).size());
                    }
                }
                //if (this.m_Show) this.plot();
            }

            if (this.m_UseSpeciesMerging) {
///////////////////////////// species convergence phase
                if (TRACE) {
                	System.out.println("-Species convergence:");
                	System.out.println("-Funcalls: " + m_Undifferentiated.getFunctionCalls());
                }
                
                // first test if loners belong to any species
                boolean found = false;
                for (int i = 0; i < this.m_Undifferentiated.size(); i++) {
                	int j=0;
                	while (!found && j<m_Species.size()) {
                    //for (int j = 0; j < this.m_Species.size(); j++) {
                    	curSpecies = (Population)this.m_Species.get(j);
                        AbstractEAIndividual tmpIndy = (AbstractEAIndividual)this.m_Undifferentiated.get(i);
                        if (this.m_CAForSpeciesConvergence.belongsToSpecies(tmpIndy, curSpecies)) {
                            if (TRACE) System.out.println("--Adding loner to species "+j);
                            found = true;
                            this.m_Undifferentiated.remove(i);
                            if (isActive(curSpecies)) {
                                curSpecies.add(tmpIndy);
                                this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize()-1);
                                i--; // needs to be reduced because D0 size has decreased
                            } else {
                                // the species is inactive and seen as converged, so reinitialize the individual
                                this.m_Undifferentiated.add(i, this.initializeIndividuals(1).get(0));
//                                m_Undifferentiated.incrFunctionCallsby(1);
                            }
                        }
                        j++;
                    }
                }
                // Now test if species converge
                Population spec1, spec2;
                for (int i1 = 0; i1 < this.m_Species.size(); i1++) {
                    spec1 = (Population)this.m_Species.get(i1);
                    for (int i2 = i1+1; i2 < this.m_Species.size(); i2++) {
                        spec2 = (Population)this.m_Species.get(i2);
                        if (this.m_CAForSpeciesConvergence.mergingSpecies(spec1, spec2)) {
                            if (TRACE) System.out.println("--------------------Merging species (" + i1 +", " +i2 +") ["+spec1.size()+"/"+spec2.size()+"]");
//                            this.m_CAForSpeciesConvergence.convergingSpecies(spec1, spec2);
                            if (isActive(spec1) && isActive(spec2)) {
                                if (TRACE) System.out.println("---Active merge");
                                
                                spec1.addPopulation(spec2);
                                // keep longer history
                                if (spec2.m_History.size() > spec1.m_History.size()) spec1.m_History = spec2.m_History;
                                this.m_Species.remove(i2);
                                i2--;
                            } else {
                            	// one of the species is converged, so we interpret the best as the optimum found
                                if (TRACE) System.out.println("---Inactive merge");
                                // save best in singular species and reinit the rest of the individuals
                                spec1.addPopulation(spec2);
                                this.m_Species.remove(i2);
                                i2--;
                                int reinitCount = spec1.size()-1;
                                // now reset the converged species to inactivity DEACTIVATE!
                                deactivateSpecies(spec1, spec1.getBestEAIndividual());
                                // reinitialized individuals and add them to undifferentiated
                                this.m_Undifferentiated.addPopulation(this.initializeIndividuals(reinitCount));
//                                m_Undifferentiated.incrFunctionCallsby(reinitCount);
                                this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize()+reinitCount);
                            }
                        }
                    }
                }
                if (TRACE) System.out.println("--Number of species: " + this.m_Species.size());
            }

//            if (this.m_UseClearing) {
//                //@todo
//                if (this.m_Debug) System.out.println("-Clearing applied:");
//                for (int i = 0; i < this.m_Species.size(); i++) {
//                    this.m_Undifferentiated.add(((Population)this.m_Species.get(i)).getBestEAIndividual());
//                }
//                this.m_Species      = new ArrayList();
//                Population tmpPop   = new Population();
//                tmpPop.setPopulationSize(this.m_Undifferentiated.getPopulationSize() - this.m_Undifferentiated.size());
//                this.m_Problem.initPopulation(tmpPop);
//                this.evaluatePopulation(tmpPop);
//                this.m_Undifferentiated.addPopulation(tmpPop);
//            }
        }
        // output the result
        if (TRACE) System.out.println("-Number of species: " + this.m_Species.size());
        if (TRACE) System.out.println("-Funcalls: " + this.m_Undifferentiated.getFunctionCalls());

        this.m_Population = (Population)this.m_Undifferentiated.clone();
        m_Population.setUseHistory(true);
        if (TRACE) System.out.println("initing with " + this.m_Undifferentiated.size());
        for (int i = 0; i < this.m_Species.size(); i++) {
            if (TRACE) System.out.println("Adding deme " + i + " with size " + ((Population)this.m_Species.get(i)).size());
            this.m_Population.addPopulation((Population)this.m_Species.get(i));
        }
        if (TRACE) System.out.println("Population size: " + this.m_Population.size());
//        if (TRACE) {
//        	Distraction distr = new Distraction(5., 0, m_Species);
//        	if (!distr.isEmpty()) {
//        		double[] distVect = distr.calcDistractionFor(m_Undifferentiated.getBestEAIndividual());
//        		System.out.println("species distract best towards " + BeanInspector.toString(distVect));
//        	}
//        }
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /**
     * Return true if the given population is considered active.
     *  
     * @param pop	a population
     * @return true, if pop is considered an active population, else false
     */
    protected boolean isActive(Population pop) {
    	return (pop.size() >= m_actSpecSize);
    }
    
    /**
     * Deactivate a given species by removing all individuals and inserting
     * only the given survivor, sets the population size to one.
     * 
     * @param spec
     */
    protected void deactivateSpecies(Population spec, AbstractEAIndividual survivor) {
    	spec.setPopulationSize(1);
    	spec.clear();
    	spec.add(survivor);   	
    }
  
    
    public int countActiveSpec() {
    	int k = 0;
    	for (int i=0; i<m_Species.size(); i++) {
    		if (isActive(m_Species.get(i))) k++;
    	}
    	return k;
    }
    
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
    public String globalInfo() {
        return "This is a versatile species based niching EA method.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "CBN-EA";
    }

    public Population getPopulation() {
        this.m_Population = (Population)m_Undifferentiated.clone();
//        m_Population.addPopulation(this.m_Undifferentiated);
        for (int i = 0; i < this.m_Species.size(); i++) this.m_Population.addPopulation((Population)this.m_Species.get(i));
        m_Population.setPopulationSize(m_Population.size()); // set it to true value
        return this.m_Population;
    }
    
    public void setPopulation(Population pop){
        this.m_Undifferentiated = pop;
        pop.setUseHistory(true);
    }
    
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
    
    public InterfaceSolutionSet getAllSolutions() {
    	// return inactive species
    	Population sols = (Population)m_Archive.clone();
    	sols.addPopulation(getPopulation());
    	sols.setPopulationSize(sols.size());
    	return new SolutionSet(getPopulation(), sols);
    }
    
    /** This method allows you to set/get the switch that toggles the use
     * of species differentiation.
     * @return The current status of this flag
     */
    public boolean getApplyDifferentiation() {
        return this.m_UseSpeciesDifferentation;
    }
    public void setApplyDifferentiation(boolean b){
        this.m_UseSpeciesDifferentation = b;
    }
    public String applyDifferentiationTipText() {
        return "Toggle the species differentation mechanism.";
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

//    /** This method allows you to toggle the use of the halting window.
//     * @return The current status of this flag
//     */
//    public boolean getUseHaltingWindow() {
//        return this.m_UseHaltingWindow;
//    }
//    public void setUseHaltingWindow(boolean b){
//        this.m_UseHaltingWindow = b;
//    }
    public String useHaltingWindowTipText() {
        return "With a halting window converged species are frozen.";
    }

    /** This method allows you to set/get the switch that toggles the use
     * of species convergence.
     * @return The current status of this flag
     */
    public boolean isUseMerging() {
        return this.m_UseSpeciesMerging;
    }
    public void setUseMerging(boolean b){
        this.m_UseSpeciesMerging = b;
        GenericObjectEditor.setHideProperty(this.getClass(), "mergingCA", !m_UseSpeciesMerging);
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
        return this.m_CAForSpeciesConvergence;
    }
    public void setMergingCA(InterfaceClustering b){
        this.m_CAForSpeciesConvergence = b;
    }
    public String mergingCATipText() {
        return "The cluster algorithm on which the species merging is based.";
    }

    public void setUseArchive(boolean v) {
    	m_UseArchive = v;
    }
    public boolean isUseArchive() {
    	return m_UseArchive;
    }
    public String useArchiveTipText() {
    	return "Toggle usage of an archive where converged species are saved and the individuals reinitialized.";
    }
    
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
        return "Determines how often species differentation/merging is performed.";
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
		return "Lenght of the halting window defining when a cluster is seen as converged and frozen; set to zero to disable.";
	}

	public String getAdditionalFileStringHeader(PopulationInterface pop) {
		return " Undiff. \t #Act.spec.; \t #Inact.spec."; 
	}

	public String getAdditionalFileStringValue(PopulationInterface pop) {
		int actives = countActiveSpec();
		return m_Undifferentiated.size() + " \t " + actives + " \t " + (m_Species.size()-actives);
	}

	public String[] customPropertyOrder() {
		return new String[]{"mergingCA", "differentiationCA"};
	}
}
