package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.Plot;
import eva2.gui.plot.TopoPlot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.EAIndividualComparator;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.cluster.ClusteringDensityBased;
import eva2.optimization.operator.cluster.InterfaceClustering;
import eva2.optimization.operator.cluster.InterfaceClusteringDistanceParam;
import eva2.optimization.operator.cluster.InterfaceClusteringMetricBased;
import eva2.optimization.operator.distancemetric.ObjectiveSpaceMetric;
import eva2.optimization.operator.paramcontrol.InterfaceHasUpperDoubleBound;
import eva2.optimization.operator.paramcontrol.ParamAdaption;
import eva2.optimization.operator.paramcontrol.ParameterControlManager;
import eva2.optimization.operator.terminators.HistoryConvergenceTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.population.SolutionSet;
import eva2.problems.*;
import eva2.tools.EVAERROR;
import eva2.tools.chart2d.*;
import eva2.tools.math.Mathematics;
import eva2.util.annotation.Description;
import eva2.util.annotation.Hidden;

import java.util.*;

/**
 * The infamous clustering based niching EA, still under construction. It should
 * be able to identify and track multiple global/local optima at the same time.
 * <p>
 * Notes: For std. GA, the mutation rate may have to reduced, because the
 * initial step size tends to be rel. large and easily disperse clustered
 * species (so that they fall below the minimum swarm size and the local optimum
 * is lost).
 * <p>
 * For the CBN-PSO remember to use the IndividualDataMetric so that the
 * remembered positions are used for clustering (which are rel. stable - so that
 * species clustering actually makes sense).
 */
@Description("This is a versatile species based niching EA method.")
public class ClusterBasedNichingEA extends AbstractOptimizer implements InterfacePopulationChangedEventListener, InterfaceAdditionalPopulationInformer, java.io.Serializable {

    private static final long serialVersionUID = -3143069327594708609L;
    private transient Population populationArchive = new Population();
    private ArrayList<Population> species = new ArrayList<>();
    private Population undifferentiatedPopulation = new Population();
    private transient Population doomedPopulation = new Population();
    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private InterfaceOptimizer optimizer = new GeneticAlgorithm();
    private InterfaceClustering caForSpeciesDifferentation = new ClusteringDensityBased();
    private InterfaceClustering caForSpeciesMerging = new ClusteringDensityBased();
    private double clusterDiffDist = 0.05;
    private boolean useDistraction = false;
    private double epsilonBound = 1e-10;
    private int speciesCycle = 1;

    private int minGroupSize = 3;
    private boolean useSpeciesDifferentiation = true;
    private boolean mergeSpecies = true;
    private int populationSize = 50;
    private int convergedCnt = 0;
    private int collisions = 0;
    private int showCycle = 0;
    transient private TopoPlot topologyPlot;
    private int haltingWindow = 15;
    private double muLambdaRatio = 0.5;
    private int sleepTime = 0;
    private int maxSpeciesSize = 15;
    private EAIndividualComparator reduceSizeComparator = new EAIndividualComparator();
    private EAIndividualComparator histComparator = new EAIndividualComparator("", -1, true);
    protected ParameterControlManager paramControl = new ParameterControlManager();
    private double avgDistForConvergence = 0.1; // Upper bound for average indy distance in a species in the test for convergence

    public ClusterBasedNichingEA() {
        this.caForSpeciesMerging = new ClusteringDensityBased();
    }

    /**
     * ********************************************************************************************************************
     * These are for InterfaceParamControllable
     */
    public Object[] getParamControl() {
        List<Object> ctrlbls = ParameterControlManager.listOfControllables(this);
        ctrlbls.add(paramControl);
        return ctrlbls.toArray();
        // this works - however differently than when returning a ParameterControlManager
        // Only instances are returned which 
    }

    /**
     * This method is necessary to allow access from the Processor.
     *
     * @return
     */
//	public ParameterControlManager getParamControl() {
//		return paramControl;
//	}
    public ParamAdaption[] getParameterControl() {
        return paramControl.getSingleAdapters();
    }

    public void setParameterControl(ParamAdaption[] paramControl) {
        this.paramControl.setSingleAdapters(paramControl);
    }

    public String parameterControlTipText() {
        return "You may define dynamic paramter control strategies using the parameter name.";
    }

    public void addParameterControl(ParamAdaption pa) {
        this.paramControl.addSingleAdapter(pa);
    }

    public ClusterBasedNichingEA(ClusterBasedNichingEA a) {
        this.epsilonBound = a.epsilonBound;
        this.population = (Population) a.population.clone();
        this.populationArchive = (Population) a.populationArchive.clone();
        this.doomedPopulation = (Population) a.doomedPopulation.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.optimizer = (InterfaceOptimizer) a.optimizer.clone();
        this.species = (ArrayList<Population>) (a.species.clone());
        this.undifferentiatedPopulation = (Population) a.undifferentiatedPopulation.clone();
        this.caForSpeciesMerging = (InterfaceClustering) this.caForSpeciesMerging.clone();
        this.caForSpeciesDifferentation = (InterfaceClustering) this.caForSpeciesDifferentation.clone();
        this.speciesCycle = a.speciesCycle;
        this.minGroupSize = a.minGroupSize;
        this.useSpeciesDifferentiation = a.useSpeciesDifferentiation;
        this.mergeSpecies = a.mergeSpecies;
        this.populationSize = a.populationSize;
        this.haltingWindow = a.haltingWindow;
        this.maxSpeciesSize = a.maxSpeciesSize;
        this.muLambdaRatio = a.muLambdaRatio;
        this.sleepTime = a.sleepTime;
        this.convergedCnt = a.convergedCnt;
        this.collisions = a.collisions;
        this.clusterDiffDist = a.clusterDiffDist;
        this.useDistraction = a.useDistraction;
        this.showCycle = a.showCycle;
        this.maxSpeciesSize = a.maxSpeciesSize;
    }

    @Override
    public Object clone() {
        return new ClusterBasedNichingEA(this);
    }

    @Override
    public void initialize() {
        if (undifferentiatedPopulation == null) {
            this.undifferentiatedPopulation = new Population(populationSize);
        } else {
            undifferentiatedPopulation.resetProperties();
            undifferentiatedPopulation.setTargetSize(populationSize);
        }

        this.undifferentiatedPopulation.setUseHistory(true);

        this.optimizationProblem.initializePopulation(this.undifferentiatedPopulation);
        this.optimizer.initializeByPopulation(undifferentiatedPopulation, true);
        undifferentiatedPopulation = this.optimizer.getPopulation(); // some optimizers clone the given one.
        if (optimizer instanceof EvolutionStrategies) {
            EvolutionStrategies es = (EvolutionStrategies) optimizer;
            es.setLambda(getPopulationSize());
            es.setMu((int) (muLambdaRatio * (double) getPopulationSize()));
        }
//        this.optimizer.initialize();
        doomedPopulation = undifferentiatedPopulation.cloneWithoutInds();
        if (undifferentiatedPopulation.getFunctionCalls() != populationSize) {
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
        this.optimizer.addPopulationChangedEventListener(this);
        this.undifferentiatedPopulation.setTargetSize(this.populationSize);
        this.species = new ArrayList<>();
        this.populationArchive = undifferentiatedPopulation.cloneWithoutInds();
//    	if (useDistraction) distraction = new Distraction(distrDefaultStrength, Distraction.METH_BEST);
        convergedCnt = 0;
        collisions = 0;
        if (evalPop) {
            this.evaluatePopulation(this.undifferentiatedPopulation);
        }
        this.optimizer.initializeByPopulation(undifferentiatedPopulation, false);
        this.undifferentiatedPopulation = optimizer.getPopulation(); // required for changes to the population by the optimizer
        population = undifferentiatedPopulation;
        this.firePropertyChangedEvent("FirstGenerationPerformed");
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.undifferentiatedPopulation = (Population) pop.clone();
        if (reset) {
            this.undifferentiatedPopulation.initialize();
        }
        initDefaults(reset);
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.optimizationProblem.evaluate(population);
        population.incrGeneration();
    }

    private void plot(int gen) {
        if (!(this.optimizationProblem instanceof TF1Problem) && !(this.optimizationProblem instanceof Interface2DBorderProblem)) {
            return;
        }
        double[] a = new double[2];
        a[0] = 0.0;
        a[1] = 0.0;
        if (this.optimizationProblem instanceof TF1Problem) {
            // now i need to plot the pareto fronts
            Plot plot = new Plot("TF3Problem at gen. " + gen, "y1", "y2", a, a);
            plot.setUnconnectedPoint(0, 0, 0);
            plot.setUnconnectedPoint(1, 5, 0);
            GraphPointSet mySet = new GraphPointSet(10, plot.getFunctionArea());
            DPoint point;
            mySet.setConnectedMode(false);
            for (int i = 0; i < this.undifferentiatedPopulation.size(); i++) {
                AbstractEAIndividual indy = this.undifferentiatedPopulation.get(i);
                double[] d = indy.getFitness();
                point = new DPoint(d[0], d[1]);
                point.setIcon(new Chart2DDPointIconCircle());
                mySet.addDPoint(point);
            }
            for (int i = 0; i < this.species.size(); i++) {
                mySet = new GraphPointSet(10 + i, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                Population pop = this.species.get(i);
//                ArchivingAllDomiating arch = new ArchivingAllDomiating();
//                arch.plotParetoFront(pop, plot);
                for (int j = 0; j < pop.size(); j++) {
                    AbstractEAIndividual indy = pop.get(j);
                    double[] d = indy.getFitness();
                    point = new DPoint(d[0], d[1]);
                    point.setIcon(new Chart2DDPointIconText("P" + j));
                    mySet.addDPoint(point);
                }

            }

        }
        if (this.optimizationProblem instanceof Interface2DBorderProblem) {
            DPointSet popRep = new DPointSet();
            InterfaceDataTypeDouble tmpIndy1;
            Population pop;

            this.topologyPlot = new TopoPlot("CBN-Species at gen. " + gen, "x", "y", a, a);
            this.topologyPlot.setParams(50, 50);
            this.topologyPlot.setTopology((Interface2DBorderProblem) this.optimizationProblem);
            //draw the undifferentiated
            for (int i = 0; i < this.undifferentiatedPopulation.size(); i++) {
                tmpIndy1 = (InterfaceDataTypeDouble) this.undifferentiatedPopulation.get(i);
                popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
            }
            this.topologyPlot.getFunctionArea().addDElement(popRep);
            //draw the species
            for (int i = 0; i < this.species.size(); i++) {
                pop = this.species.get(i);
                plotPopConnected(topologyPlot, pop);
            }
            if (!useDistraction) {
                for (int i = 0; i < this.populationArchive.size(); i++) {
                    plotIndy(topologyPlot, 'x', (InterfaceDataTypeDouble) populationArchive.get(i));
                }
            } else {
//            	for (int i = 0; i < this.distraction.getDistractorSetSize(); i++) {
//            		plotPosFit('#',distraction.getDistractorCenter(i), distraction.getDistractorStrength(i));
//            	}
            }
        }
    }

    public static void plotPopConnected(TopoPlot tp, Population pop) {
        DPointSet popRep;
        InterfaceDataTypeDouble tmpIndy1;
        if (pop.size() > 1) {
            for (int j = 0; j < pop.size(); j++) {
                popRep = new DPointSet();
                tmpIndy1 = (InterfaceDataTypeDouble) pop.get(j);
                popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                tp.getFunctionArea().addDElement(popRep);

                plotLine(tp, pop.getEAIndividual(j), pop.getBestEAIndividual());
            }
        } else {
            // this is an inactive species
            plotIndy(tp, '+', (InterfaceDataTypeDouble) pop.get(0));
        }
    }

    public static void plotLine(TopoPlot tp, AbstractEAIndividual indy1,
                                AbstractEAIndividual indy2) {
//		DPointSet popRep;
        double[] pos1, pos2;
        if (indy1 instanceof InterfaceDataTypeDouble) {
            pos1 = ((InterfaceDataTypeDouble) indy1).getDoubleData();
        } else {
            pos1 = (indy1).getDoublePosition();
        }
        if (indy2 instanceof InterfaceDataTypeDouble) {
            pos2 = ((InterfaceDataTypeDouble) indy2).getDoubleData();
        } else {
            pos2 = (indy2).getDoublePosition();
        }
        tp.getFunctionArea().drawLine(pos1, pos2);
    }

    public static void plotIndy(Plot p, char c, InterfaceDataTypeDouble tmpIndy) {
        plotPosFit(p, c, tmpIndy.getDoubleData(), ((AbstractEAIndividual) tmpIndy).getFitness(0));
    }

    public static void plotPosFit(Plot p, char c, double[] position, double fitness) {
        DPointSet popRep;
        popRep = new DPointSet();
        popRep.addDPoint(new DPoint(position[0], position[1]));
        double d = Math.round(100 * fitness) / (double) 100;
        DPointIcon icon = new Chart2DDPointIconText(c + "" + d);
        ((Chart2DDPointIconText) icon).setIcon(new Chart2DDPointIconCircle());
        popRep.setIcon(icon);
        p.getFunctionArea().addDElement(popRep);
    }

    /**
     * This method is called to generate n freshly initialized individuals
     *
     * @param n Number of new individuals
     * @return A population of new individuals
     */
    private Population initializeIndividuals(int n) {
        Population result = undifferentiatedPopulation.cloneWithoutInds();
        result.setUseHistory(true);
        result.setTargetSize(n);
        //@todo: crossover between species is to be implemented
        optimizationProblem.initializePopulation(result);
        optimizationProblem.evaluate(result);
        optimizer.setPopulation(result);    // for some initialization by the optimizer, such as PSO memory
//        capMutationRate(result, RNG.randomDouble(0.001, 0.1));
        return result;
    }

    /**
     * This method checks whether a species is converged, i.e. the best fitness
     * has not improved for a number of generations.
     *
     * @param pop The species to test
     * @return True if converged.
     */
    private boolean testSpeciesForConvergence(Population pop) {
        int histLen = pop.getHistoryLength();

        if (histLen < haltingWindow) {
//        	System.out.println("not long enough... gen " + pop.getGeneration());
            return false;
        } else {
//        	TODO: undo: InterfaceTerminator convergenceTerminator = new DiversityTerminator(epsilonBound,new PhenotypeMetric(),0);
//        	boolean term = convergenceTerminator.isTerminated(pop);

            InterfaceTerminator convergenceTerminator = new HistoryConvergenceTerminator(haltingWindow, epsilonBound, 0, false);
            boolean term = convergenceTerminator.isTerminated(pop);
//    		if (term) {
//    			System.out.println("Conv spec. aged " + pop.getGeneration() + " with meas. " + BeanInspector.toString(pop.getPopulationMeasures()));
//    		}
            if (term) {
                // this case is especially relevant if sequential niching is "faked" using the CBN approach,
                // because in this case, the niching parameter is very large and the single (sequentially build) species
                // may be still spread throughout the search space and still not improve for the given halting window.
                // Omitting this causes high numbers of phantom solutions which are far from converged optima
                double[] specMeas = pop.getPopulationMeasures();
                if (specMeas[0] > avgDistForConvergence) {
//            		if (getClusterDiffDist()<=0.5) 
//            		System.err.println("ALTERNATIVE BREAK, FORBIDDING CONVERGENCE! sig=" + getClusterDiffDist() + " / avD="+ specMeas[0]);
                    InterfaceTerminator convTerm2 = new HistoryConvergenceTerminator(2 * haltingWindow, epsilonBound, 0, false);
                    term = convTerm2.isTerminated(pop);
                    //                		System.out.println("Twice the halting window passed without improvement and still no phenotypic convergence!!!");
                    return term;
                }
            }

            if (term) {
//        		 System.out.println();
            }
            // TODO something like this may be used as additional convergence criterion.
            // it influences the number of local optima archived and seems to increase the score, but not the best-found solutions, at least for a large clustering parameter 
//    		System.out.println("Terminated, subswarm measures: " + BeanInspector.toString(pop.getPopulationMeasures()));
//            	if (optimizer instanceof ParticleSwarmOptimization) {
//            		double swarmSp = ParticleSwarmOptimization.getPopulationVelSpeed(pop, 2, ParticleSwarmOptimization.partVelKey, null, null)[0];
//            		System.out.println("Swarm speed: " + swarmSp);
//            		if (swarmSp > 0.05) {
//            			System.err.println("Too high speed");
//            			term = false;
//            		}
//            	}
//        	}
            return term;
        }

    }

    /**
     * Define the criterion by which individual improvement is judged. The
     * original version defined improvement strictly, but for some EA this
     * should be done more laxly. E.g. DE will hardly ever stop improving
     * slightly, so optionally use an epsilon-bound: improvement only counts if
     * it is larger than epsilon in case useEpsilonBound is true.
     *
     * @param firstIndy
     * @param secIndy
     * @return true if the second individual has improved in relation to the
     *         first one
     */
    private boolean testSecondForImprovement(AbstractEAIndividual firstIndy, AbstractEAIndividual secIndy) {
        if (epsilonBound > 0) {
            double fitDiff = (new ObjectiveSpaceMetric()).distance(firstIndy, secIndy);
            boolean ret = (secIndy.isDominatingDebConstraints(firstIndy));
            ret = ret && (fitDiff > epsilonBound);  // there is improvement if the second is dominant and the fitness difference is larger than epsilon  
            return ret;
        } else {
            return (histComparator.compare(firstIndy, secIndy) > 0);
        }
    }

    private Population optimizeSpecies(Population species, boolean minorPlot) {
        optimizer.setPopulation(species);
        if (optimizer instanceof EvolutionStrategies) {
            EvolutionStrategies es = (EvolutionStrategies) optimizer;
            int mu = Math.max(1, (int) (muLambdaRatio * species.size()));
            if (mu >= species.size()) {
                mu = Math.max(1, species.size() - 1);
            }
            es.setMu(mu);
            es.setLambda(species.size());
        }

        if (BeanInspector.hasMethod(optimizer, "getLastModelPopulation", null) != null) {
            Object pc = BeanInspector.callIfAvailable(optimizer, "getLastTrainingPatterns", null);
        }

        this.optimizer.optimize();
        Population retPop = optimizer.getPopulation();
        if (retPop.size() != retPop.getTargetSize()) {
            retPop.synchSize();
        }
        return retPop;
    }

    @Override
    public void optimize() {
        Population reinitPop = null;
        if (doomedPopulation.size() > 0) {
            reinitPop = this.initializeIndividuals(doomedPopulation.size()); // do not add these to undifferentiated yet, that would mess up the evaluation count
            doomedPopulation.clear();
        }
        int countIndies = (reinitPop != null ? reinitPop.size() : 0) + undifferentiatedPopulation.size();
        for (Population specy1 : species) {
            countIndies += specy1.size();
        }
        if (this.showCycle > 0) {
            if (undifferentiatedPopulation.getGeneration() <= 1) {
                plot(undifferentiatedPopulation.getGeneration());
            }
        }
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // species evolution phase
        // optimize D_0
        this.undifferentiatedPopulation.synchSize();
        if (undifferentiatedPopulation.size() > 0) {
            undifferentiatedPopulation.putData(InterfaceSpeciesAware.populationTagKey, InterfaceSpeciesAware.explorerPopTag);
            undifferentiatedPopulation = optimizeSpecies(undifferentiatedPopulation, false);
        } else {
            undifferentiatedPopulation.incrGeneration();
        }

        Population curSpecies;
        // optimize the clustered species
        for (int i = this.species.size() - 1; i >= 0; i--) {
            curSpecies = this.species.get(i);
            curSpecies.setFunctionCalls(0);
            curSpecies.synchSize();
            if ((haltingWindow > 0) && (this.testSpeciesForConvergence(curSpecies))) {
                convergedCnt++;
                AbstractEAIndividual best = curSpecies.getBestHistoric(); // usually we want the best alltogether
                if (best == null) {
                    best = (AbstractEAIndividual) curSpecies.getBestEAIndividual().getClone();
                }
                int toReinit = 0;
                populationArchive.add(best);
                species.remove(i);  // remove the converged Species
                toReinit = curSpecies.size();
                // those will not be optimized anymore, so we dont need to doom them, but can directly add them to undiff!
                undifferentiatedPopulation.addPopulation(initializeIndividuals(toReinit));
                undifferentiatedPopulation.incrFunctionCallsBy(toReinit);

            } else {
                curSpecies.putData(InterfaceSpeciesAware.populationTagKey, InterfaceSpeciesAware.localPopTag);
                Population optimizedSpec = optimizeSpecies(curSpecies, true);
                this.species.set(i, optimizedSpec);
                curSpecies = this.species.get(i); // reset to expected population, just to be sure
            }
//            }
            // This is necessary to keep track of the function calls needed
            undifferentiatedPopulation.incrFunctionCallsBy(curSpecies.getFunctionCalls());
        }

        synchronized (population) { // fill the population instance with the current individuals from undiff, spec, etc.
            this.population = (Population) this.undifferentiatedPopulation.clone();
            population.setUseHistory(true);
            for (Population specy : this.species) {
                this.population.addPopulation(specy);
            }
            if (doomedPopulation.size() > 0) {
                population.addPopulation(reinitPop);
            } // this is just so that the numbers match up...
            population.synchSize();
        }

        // possible species differentiation and convergence
        if (this.undifferentiatedPopulation.getGeneration() % this.speciesCycle == 0) {
            initClustering();

            if (this.useSpeciesDifferentiation) {
                Population[] clusters;
                ArrayList<Population> newSpecies = new ArrayList<>();
                //cluster the undifferentiated population
                clusters = this.caForSpeciesDifferentation.cluster(this.undifferentiatedPopulation, population);
                for (int j = 1; j < clusters.length; j++) { // loop new clusters
                    splitFromFirst(undifferentiatedPopulation, clusters[j], false);
                    newSpecies.add(clusters[j]);
                }
                replaceUndifferentiated(clusters[0]);
                for (int i = 0; i < this.species.size(); i++) { // loop old species
                    curSpecies = this.species.get(i);
                    // check if a species has differentiated any further
                    clusters = this.caForSpeciesDifferentation.cluster(curSpecies, population);
                    if (clusters[0].size() > 0) {
                        mergeToFirst(undifferentiatedPopulation, clusters[0], false);
                    }
                    for (int j = 1; j < clusters.length; j++) { // set up new species
                        // this is treated as a split only if more than one cluster was found
                        // so if clustering results in a list of size 2: [undiff,spec], the curSpecies only is maintained.
                        if (clusters.length <= 2) {
                            clusters[j].addDataFromPopulation(curSpecies);
                        } // copy earlier data to corresponding new cluster
                        else {
                            splitFromFirst(curSpecies, clusters[j], true);
                        }
                        newSpecies.add(clusters[j]);

                    }
                }
                this.species = newSpecies;
            } // end of species differentiation

            // plot the populations
            if (this.showCycle > 0) {
                if ((this.undifferentiatedPopulation.getGeneration() <= 2) || (this.undifferentiatedPopulation.getGeneration() % this.showCycle == 0)) {
                    this.plot(this.undifferentiatedPopulation.getGeneration());
                }
            }

            if (this.mergeSpecies && (species.size() > 0)) {
                // first test if loners belong to any species
                int[] assocSpec = caForSpeciesMerging.associateLoners(undifferentiatedPopulation, species.toArray(new Population[species.size()]), population);
                for (int i = undifferentiatedPopulation.size() - 1; i >= 0; i--) { // backwards or die!
                    if (assocSpec[i] >= 0) {
                        // loner i should be merged to species assocSpec[i]
                        AbstractEAIndividual tmpIndy = this.undifferentiatedPopulation.get(i);
                        if (topologyPlot != null) {
                            plotLine(topologyPlot, tmpIndy, species.get(assocSpec[i]).getBestEAIndividual());
                        }
                        this.undifferentiatedPopulation.remove(i);
                        species.get(assocSpec[i]).add(tmpIndy); // TODO merge information from loners?
                    }
                }
                Population spec1, spec2;
                // test if species are close to already archived solutions - deactivate them if so
                assocSpec = caForSpeciesMerging.associateLoners(populationArchive, species.toArray(new Population[species.size()]), population);
                PriorityQueue<Integer> specToRemove = new PriorityQueue<>(5, Collections.reverseOrder()); // backwards sorted or DIE!
                for (int i = populationArchive.size() - 1; i >= 0; i--) {
                    if (assocSpec[i] >= 0) {
                        AbstractEAIndividual aIndy = populationArchive.getEAIndividual(i);
                        spec1 = this.species.get(assocSpec[i]);
                        // archived solution corresponds to an existing species
                        if (!specToRemove.contains(assocSpec[i])) {
                            // the species has not yet been deactivated
                            specToRemove.add(assocSpec[i]);
                            collisions++;
                            doomedPopulation.addPopulation(spec1);
                        }
                    }
                }
                int lastRemoved = Integer.MAX_VALUE;
                while (!specToRemove.isEmpty()) { // backwards sorted or DIE!
                    int specIndex = specToRemove.poll();
                    if (specIndex > lastRemoved) {
                        System.err.println("Stupid queue!!!");
                    }
                    species.remove(specIndex); // warning, dont try to remove Integer object but index i!
                    lastRemoved = specIndex;
                }
                // Now test if species should be merged among each other
                for (int i1 = 0; i1 < this.species.size(); i1++) {
                    spec1 = this.species.get(i1);
                    for (int i2 = i1 + 1; i2 < this.species.size(); i2++) {
                        spec2 = this.species.get(i2);
                        if (this.caForSpeciesMerging.mergingSpecies(spec1, spec2, population)) {

                            mergeToFirst(spec1, spec2, true);
                            this.species.remove(i2);
                            i2--;
                        }
                    }
                }
            } /// end of species merging

            if (maxSpeciesSize >= minGroupSize) {
                // reinit worst n individuals from all species which are too large
                for (Population curSpec : species) {
                    if (curSpec.size() > maxSpeciesSize) {
                        ArrayList<AbstractEAIndividual> sorted = curSpec.getSorted(reduceSizeComparator);
                        for (int k = maxSpeciesSize; k < sorted.size(); k++) {
                            if (curSpec.remove(sorted.get(k))) {
                                doomedPopulation.add(sorted.get(k));
                            }
                        }
                    }
                }
            }
        } // end of species cycle

        // add new individuals from last step to undifferentiated set
        if ((reinitPop != null) && (reinitPop.size() > 0)) {
            undifferentiatedPopulation.addPopulation(reinitPop);
            undifferentiatedPopulation.incrFunctionCallsBy(reinitPop.size());
        }
        undifferentiatedPopulation.setTargetSize(undifferentiatedPopulation.size());
        // output the result

        synchronized (population) { // fill the population instance with the current individuals from undiff, spec, etc.
            this.population = (Population) this.undifferentiatedPopulation.clone();
            population.setUseHistory(true);
            for (Population specy : this.species) {
                this.population.addPopulation(specy);
            }
            if (doomedPopulation.size() > 0) {
                population.addPopulation(doomedPopulation);
            } // this is just so that the numbers match up...
            population.synchSize();
            if (population.size() != populationSize) {
                System.err.println("Warning: Invalid population size in CBNEA! " + population.size());
            }
        }

        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * Initialize the clustering method for differentiation.
     */
    private void initClustering() {
        if (getClusterDiffDist() > 0) { // assume that it should be set
            if (this.caForSpeciesDifferentation instanceof InterfaceClusteringDistanceParam) {
                ((InterfaceClusteringDistanceParam) caForSpeciesDifferentation).setClustDistParam(getClusterDiffDist());
            } else {
                EVAERROR.errorMsgOnce("Warning: cluster distance is defined in CBN  but the clustering method " + caForSpeciesDifferentation.getClass() + " cant interpret it!");
            }
        }
        this.caForSpeciesDifferentation.initClustering(population);
    }

    /**
     * Replace the undifferentiated population with the given one.
     *
     * @param pop
     */
    private void replaceUndifferentiated(Population pop) {
        undifferentiatedPopulation.clear();
        undifferentiatedPopulation.addPopulation(pop);
    }

    private String specTag(Population spec) {
        return spec.size() + "(" + spec.getGeneration() + ((spec.hasData("MAPSOModelInformation")) ? "/" + (BeanInspector.callIfAvailable(spec.getData("MAPSOModelInformation"), "getStringRepresentation", null)) : "") + ")";
    }

    /**
     * Merge two species by adding the second to the first. Keep the longer
     * history. The second species should be deactivated after merging.
     *
     * @param spec1
     * @param spec2
     * @param plot
     */
    protected void mergeToFirst(Population spec1, Population spec2, boolean plot) {
        if (plot && (topologyPlot != null)) {
            plotLine(topologyPlot, spec1.getBestEAIndividual(), spec2.getBestEAIndividual());
        }
        spec1.addPopulation(spec2);
        // keep longer history
        if (spec2.getHistoryLength() > spec1.getHistoryLength()) {
            spec1.setHistory(spec2.getHistory());
        }
        if (spec2.getGeneration() > spec1.getGeneration()) {
            spec1.setGeneration(spec2.getGeneration());
        }
        // possibly notify the optimizer of the merging event to merge population based information
        if (optimizer instanceof InterfaceSpeciesAware) {
            ((InterfaceSpeciesAware) optimizer).mergeToFirstPopulationEvent(spec1, spec2);
        }
    }

    /**
     * A split event will reset the new species model so as to have a fresh
     * start.
     *
     * @param parentSp
     * @param newSp
     * @param startAtP1Gen
     */
    protected void splitFromFirst(Population parentSp, Population newSp, boolean startAtP1Gen) {
        newSp.setTargetSize(newSp.size());
        newSp.setUseHistory(true);
        if (startAtP1Gen) { // start explicitely as a child population of p1
            newSp.setGeneration(parentSp.getGeneration());
            newSp.setHistory((LinkedList<AbstractEAIndividual>) parentSp.getHistory().clone());
        } else { // start anew (from undiff)
            newSp.setGeneration(0);
            newSp.setHistory(new LinkedList<AbstractEAIndividual>());
        }

        if (optimizer instanceof InterfaceSpeciesAware) {
            ((InterfaceSpeciesAware) optimizer).splitFromFirst(parentSp, newSp);
        }
    }


    /**
     * This method allows an optimizer to register a change in the optimizer.
     *
     * @param source The source of the event.
     * @param name   Could be used to indicate the nature of the event.
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        //Population population = ((InterfaceOptimizer)source).getPopulation();
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
        this.optimizer.setProblem(this.optimizationProblem);
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
        result += "Genetic Algorithm:\n";
        result += "Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        result += this.population.getStringRepresentation();
        return result;
    }


    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "CBN-EA";
    }

    @Override
    public void setPopulation(Population pop) {
        this.undifferentiatedPopulation = pop;
        if (populationArchive == null) {
            populationArchive = new Population();
        }
        populationArchive.setPopMetric(pop.getPopMetric());
        population.setPopMetric(pop.getPopMetric());
        doomedPopulation.setPopMetric(pop.getPopMetric());

        if (caForSpeciesDifferentation instanceof InterfaceClusteringMetricBased) {
            ((InterfaceClusteringMetricBased) caForSpeciesDifferentation).setMetric(pop.getPopMetric());
        }
        if (caForSpeciesMerging instanceof InterfaceClusteringMetricBased) {
            ((InterfaceClusteringMetricBased) caForSpeciesMerging).setMetric(pop.getPopMetric());
        }
        pop.setUseHistory(true);
    }

    public Population getArchivedSolutions() {
        return (Population) populationArchive.clone();
    }

    @Override
    public SolutionSet getAllSolutions() {
        // return inactive species
        Population sols = getArchivedSolutions();
//    	sols.addPopulation(getPopulation());
        for (Population sp : species) {
            sols.add(sp.getBestIndividual());
        }
        if (undifferentiatedPopulation.size() > 0) {
            sols.add(undifferentiatedPopulation.getBestIndividual());
        }
//    	if (!sols.checkNoNullIndy()) {
//    		System.err.println("error in CBN...");
//    	}
        sols.synchSize();
        return new SolutionSet(getPopulation(), sols);
    }

    /**
     * This method allows you to set/get the switch that toggles the use of
     * species convergence.
     *
     * @return The current status of this flag
     */
    public boolean isUseMerging() {
        return this.mergeSpecies;
    }

    public void setUseMerging(boolean b) {
        this.mergeSpecies = b;
        GenericObjectEditor.setHideProperty(this.getClass(), "mergingCA", !mergeSpecies);
    }

    public String useMergingTipText() {
        return "Toggle the use of species merging.";
    }

    /**
     * Choose a population based optimizing technique to use
     *
     * @return The current optimizing method
     */
    public InterfaceOptimizer getOptimizer() {
        return this.optimizer;
    }

    public void setOptimizer(InterfaceOptimizer b) {
        this.optimizer = b;
        if (b instanceof EvolutionStrategies) {
            EvolutionStrategies es = (EvolutionStrategies) b;
            setMuLambdaRatio(es.getMu() / (double) es.getLambda());
        }
    }

    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /**
     * The cluster algorithm on which the species differentiation is based
     *
     * @return The current clustering method
     */
    public InterfaceClustering getDifferentiationCA() {
        return this.caForSpeciesDifferentation;
    }

    public void setDifferentiationCA(InterfaceClustering b) {
        this.caForSpeciesDifferentation = b;
    }

    public String differentiationCATipText() {
        return "The cluster algorithm on which the species differentation is based.";
    }

    /**
     * The Cluster Algorithm on which the species convergence is based.
     *
     * @return The current clustering method
     */
    public InterfaceClustering getMergingCA() {
        return this.caForSpeciesMerging;
    }

    public void setMergingCA(InterfaceClustering b) {
        this.caForSpeciesMerging = b;
    }

    public String mergingCATipText() {
        return "The cluster algorithm on which the species merging is based.";
    }


    /**
     * Determines how often species differentation/convergence is performed.
     *
     * @return This number gives the generations when specification is
     *         performed.
     */
    public int getSpeciesCycle() {
        return this.speciesCycle;
    }

    public void setSpeciesCycle(int b) {
        this.speciesCycle = b;
    }

    public String speciesCycleTipText() {
        return "Determines how often species differentation/convergence is performed.";
    }

    /**
     * TDetermines how often show is performed.
     *
     * @return This number gives the generations when specification is
     *         performed.
     */
    public int getShowCycle() {
        return this.showCycle;
    }

    public void setShowCycle(int b) {
        this.showCycle = b;
        if (b <= 0) {
            topologyPlot = null;
        }
    }

    public String showCycleTipText() {
        return "Determines how often show is performed (generations); set to zero to deactivate.";
    }

    /**
     * Determines the size of the initial population.
     *
     * @return This number gives initial population size.
     */
    public int getPopulationSize() {
        return this.populationSize;
    }

    public void setPopulationSize(int b) {
        this.populationSize = b;
    }

    public String populationSizeTipText() {
        return "Determines the size of the initial population.";
    }

    public String[] getGOEPropertyUpdateLinks() {
        return new String[]{"population", "populationSize", "populationSize", "population"};
    }

//	/**
//	 * @return the muLambdaRatio
//	 */
//	public double getMuLambdaRatio() {
//		return muLambdaRatio;
//	}

    /**
     * This is now set if an ES is set as optimizer.
     *
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

    @Override
    public String[] getAdditionalDataHeader() {
        return new String[]{"numUndiff", "numActSpec", "avgSpecMeas", "numArchived",
                "archivedMedCorr", "archivedMeanDist", "numCollisions", "clustSig"};
    }

    @Override
    public String[] getAdditionalDataInfo() {
        return new String[]{
                "The number of exploring individuals in the main population",
                "The number of active species (sub-populations)",
                "The average of the mean distance of individuals within a species",
                "The number of stored potential local optima",
                "The median correlation of archived solutions",
                "The mean distance of archived solutions",
                "The number of collisions events that happened so far",
                "The clustering distance"
        };
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
//		int actives = countActiveSpec();
        return new Object[]{
                undifferentiatedPopulation.size(),
                species.size(),
                getAvgSpeciesMeasures()[0],
                populationArchive.size(),
                populationArchive.getCorrelations()[3],
                populationArchive.getPopulationMeasures()[0],
                collisions,
                getClusterDiffDist()};
//		return undifferentiatedPopulation.size() + " \t " + species.size() + " \t " + BeanInspector.toString(getAvgSpeciesMeasures()[0]) + " \t " + (populationArchive.size());
    }

    /**
     * Calculate average of Population measures (mean, minimal and maximal
     * distance within a species)
     *
     * @return average population measures
     */
    protected double[] getAvgSpeciesMeasures() {
        if (species == null || (species.size() == 0)) {
            return new double[]{0};
        } else {
            double[] measures = species.get(0).getPopulationMeasures();
            for (int i = 1; i < species.size(); i++) {
                Mathematics.vvAdd(measures, species.get(i).getPopulationMeasures(), measures);
            }
            if (species.size() > 1) {
                Mathematics.svDiv((double) species.size(), measures, measures);
            }
            return measures;
        }
    }

    public int getMaxSpeciesSize() {
        return maxSpeciesSize;
    }

    public void setMaxSpeciesSize(int mMaxSpeciesSize) {
        maxSpeciesSize = mMaxSpeciesSize;
        GenericObjectEditor.setShowProperty(this.getClass(), "reduceSizeComparator", (maxSpeciesSize >= minGroupSize));
    }

    public String maxSpeciesSizeTipText() {
        return "If >= " + minGroupSize + ", larger species are reduced to the given size by reinitializing the worst individuals.";
    }

    public String reduceSizeComparatorTipText() {
        return "Set the comparator used to define the 'worst' individuals when reducing species size.";
    }

    public EAIndividualComparator getReduceSizeComparator() {
        return reduceSizeComparator;
    }

    public void setReduceSizeComparator(
            EAIndividualComparator reduceSizeComparator) {
        this.reduceSizeComparator = reduceSizeComparator;
    }

    public String[] customPropertyOrder() {
        return new String[]{"mergingCA", "differentiationCA"};
    }

    //	public void setHistComparator(AbstractEAIndividualComparator histComparator) {
//		this.histComparator = histComparator;
//	}
    public EAIndividualComparator getHistComparator() {
        return histComparator;
    }
//	public String histComparatorTipText() {
//		return "The comparator to keep track of old optima. Should correspond to the clustering metric."; 
//	}

    public void setClusterDiffDist(double clusterDiffDist) {
        this.clusterDiffDist = clusterDiffDist;
        if (clusterDiffDist < 0) {
            if ((optimizationProblem instanceof InterfaceProblemDouble) && (caForSpeciesDifferentation instanceof ClusteringDensityBased)) {
//				int numExpectedOptima = (int)((((double)getPopulationSize())*0.9)/((ClusteringDensityBased)caForSpeciesDifferentation).getMinimumGroupSize());
//				this.clusterDiffDist = EsDpiNiching.calcEstimatedNicheRadius(((AbstractProblemDouble)problem).makeRange(), numExpectedOptima, new EuclideanMetric());
                setUpperBoundClustDiff((InterfaceProblemDouble) optimizationProblem);
            } else {
                System.err.println("Warning, unable to calculate standard niche radius in CBN-EA");
            }
        }
    }

    /**
     * Calculate the clustering parameter in such a way that about one q-th part
     * of the range of the given problem is within one hyper sphere of the
     * clustering parameter.
     * <p>
     * For certain types of parameter adaption schemes, this automatically sets
     * the upper limit if the clustering parameter is controlled.
     *
     * @param prob
     * @param q
     */
    public void setUpperBoundClustDiff(InterfaceProblemDouble prob) {
        if (caForSpeciesDifferentation instanceof ClusteringDensityBased) {
            double meanSubSwarmSize = 0.5 * (((ClusteringDensityBased) caForSpeciesDifferentation).getMinimumGroupSize() + getMaxSpeciesSize());
            int numExpectedOptima = (int) ((((double) getPopulationSize())) / meanSubSwarmSize);
            double[][] range = ((InterfaceProblemDouble) optimizationProblem).makeRange();
            int dim = range.length;
            double nRad = EsDpiNiching.calcEstimatedNicheRadius(range, numExpectedOptima, ((ClusteringDensityBased) caForSpeciesDifferentation).getMetric());
            nRad *= Math.pow(0.5, 1 / dim);
//			System.out.println("Alternative clust diff from niche radius... " + nRad);
            this.clusterDiffDist = nRad;
//			System.out.println("Setting the clusterDiffDist to "+ clusterDiffDist);
            ParamAdaption[] adaptors = getParameterControl();
            if (adaptors.length > 0) {
                for (ParamAdaption adpt : adaptors) {
                    if (adpt.getControlledParam().equals("clusterDiffDist")) {
                        if (adpt instanceof InterfaceHasUpperDoubleBound) {
                            ((InterfaceHasUpperDoubleBound) adpt).SetUpperBnd(clusterDiffDist);
                        } else {
                            System.err.println("Warning, unknown parameter adaption type for automatic setting of upper bound of the clustering sigma (CBN-EA)");
                        }
                    }
                }
            }
//			double estRad = EsDpiNiching.calcEstimatedNicheRadius(prob.makeRange(), expectedPeaks, metric);
//			setClusterDiffDist(estRad);
        } else {
            System.err.println("Warning, unable to calculate standard niche radius in CBN-EA");
        }
    }

    public double getClusterDiffDist() {
        return clusterDiffDist;
    }
}
