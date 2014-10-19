package eva2.optimization.strategies;

import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.plot.TopoPlot;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.cluster.ClusteringDynPeakIdent;
import eva2.optimization.operator.distancemetric.EuclideanMetric;
import eva2.optimization.operator.distancemetric.IndividualDataMetric;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.operator.mutation.InterfaceAdaptOperatorGenerational;
import eva2.optimization.operator.paramcontrol.ParamAdaption;
import eva2.optimization.operator.paramcontrol.ParameterControlManager;
import eva2.optimization.operator.selection.*;
import eva2.optimization.operator.terminators.HistoryConvergenceTerminator;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.population.SolutionSet;
import eva2.problems.Interface2DBorderProblem;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Formatter;

/**
 * The ES niching with dynamic peak identification. Basically, for a set of
 * "peaks" a set of (mu,lambda)-ESes are performed in parallel, with interaction
 * based on a niche radius (fixed so far). The number of peaks must be
 * predefined, and lambda new samples are drawn for every peak in every
 * iteration. Thus, in relation to the standard ES, some new parameters are due.
 * On the other hand, the selection schemes are predefined. This is done
 * according to Shir&Bäck, Niching in Evolution Strategies, Tec.Report 2005.
 * <p/>
 * Unfortunately the algorithm was not described in every detail. It remained
 * unclear how exactly the peak population where filled and esp. what happens if
 * DPI returns less than q peaks. In this implementation, this is solved by
 * setting a maximum size to each peak cluster (muPerPeak). For a peak
 * population, only the best muPerPeak remain and the rest is assigned to the
 * unclustered set. From these, pseudo-peaks are then formed until the expected
 * number of peak populations is produced.
 * <p/>
 * Also, they gave a general rule for setting the niche radius depending on the
 * problem domain, however in their experiments, they were able to identify
 * optima which were much closer (i.e., on Ackley's), so it is unclear which
 * niche radius was actually used there
 * <p/>
 * Due to the "non-standard" environmental selection mechanism (a fixed
 * proportion is chosen from the children, the rest from the parents), a
 * preselection mechanism was implemented here, slightly breaking the ES
 * framework which is still used to optimize the single peak populations.
 * <p/>
 * This class should also cover "Dynamic niching in evolution strategies with
 * covariance matrix adaptation" by Shir & Bäck, CEC 2005, when employing
 * SelectBest as parent selection and muPerPeak=1.
 * <p/>
 * Some notes:
 * <p/>
 * If strictNiching is deactivated, niches are disappearing because they are
 * sometimes assigned the global opt. and in that niche are very bad so they get
 * thrown away due to the maximum niche count if strictNiching is activated,
 * niches are very small - or even empty. Why is that? This can hardly be
 * sensible esp. with a very small niche radius for F8. This would mean that in
 * the beginning, nearly all offspring are created from very few pairs of peaks.
 * <p/>
 * The mu/lambda information was lost on the way to the MutateESRankMuCMA
 * class... I added it now as additional pop.data which is only loaded if the
 * mu/lambda ratio deduced from the orig/selected population sizes does not make
 * sense.
 * <p/>
 * With the explorer peaks reinited every 100 generations or so, the course of
 * the MPR for a *single* run will not be a clear logistic function, because
 * "finding" additional peaks produces small steps in the MPR.
 * <p/>
 * The Niching ES is now using the Population to catch the generational events,
 * not producing it itself, similar to Tribes, ScatterSearch or IPOP-ES. Thus it
 * could be used with a dynamic population size without too much hassle.
 * <p/>
 * TODO Add adaptive niche radius. Add parameter to every indy which is adapted
 * after all new peaks have been found.
 */
@Description("A niching ES with dynamic peak identification, after Shir and Bäck: Niching in Evolution Strategies, "
        + "GECCO 2005. Basically, there are several variants of a (mu,lambda)-ES performed "
        + "in parallel, which are reclustered in each iteration based on the dynamic peak set.")
public class EsDpiNiching implements InterfaceOptimizer, Serializable, InterfaceAdditionalPopulationInformer, InterfacePopulationChangedEventListener {

    private static final boolean TRACE = false, TRACE_DEMES = false;
    private double nicheRadius = 0.3;
    private int expectedPeaks = 5;
    private int explorerPeaks = 0;
    private int muPerPeak = 50;
    private int lambdaPerPeak = 60;
    private int eta = 30;
    private boolean doEtaPreselection = true;
    private int numRndImmigrants = 100;
    private boolean useNicheRadiusEstimation = true;
    private boolean reinitAlreadyFound = true; // check if current species have converged to already archived solutions - if so, deactivate them
    private transient Population archive = new Population(); // collect deactivated optima
    protected ParameterControlManager paramControl = new ParameterControlManager();
    private transient EvolutionStrategies[] peakOpts = null;
    Population population = new Population();
    private InterfaceOptimizationProblem problem;
    private transient InterfacePopulationChangedEventListener populationChangedEventListener;
    private String identifier = "Niching-ES";
    private transient TopoPlot plot = null;
    private transient Population randomNewIndies = null;
    private int plotInterval = 0;
    private InterfaceDistanceMetric metric = new PhenotypeMetric();
    private boolean addLonersToPeaks = false;
    private InterfaceSelection parentSel = new SelectTournament();
    private boolean allowSingularPeakPops = false;
    private int resetExplorerInterval = 1; // how often are the exploring peaks reset?
    private int convCount = 0;
    private int haltingWindowLen = 15; // the window length along which species convergence is measured
    private double deactConvThresh = 1e-5; // the threshold for species convergence and deactivation (std. dev of historical fitnesses)
    private int fitCriterion = 0; // fitness criterion to check for species convergence
    private int collisions = 0;
    private boolean doNumPeakAdaption = false;
    private double collisionDetNicheRadius = 0.001; // distance below which collision between active species and archived solutios is assumed
    private static final String origPeakIndyKey = "originalPeakIndividualKey";
    public static final String originalPeakPop = "originalPeakPopulationID";

    public EsDpiNiching() {
    }

    /**
     * Use the given parameters, the maximal estimation for the niche radius, no
     * preselection and no explorers.
     *
     * @param muPerPeak
     * @param lambdaPerPeak
     * @param expectedPeaks
     * @param rndImmigrants
     */
    public EsDpiNiching(int muPerPeak, int lambdaPerPeak, int expectedPeaks, int rndImmigrants) {
        this(-1., muPerPeak, lambdaPerPeak, expectedPeaks, rndImmigrants, 0, 0, 0);
    }

    /**
     * Use the given parameters, no preselection and no explorers.
     *
     * @param nicheRadius
     * @param muPerPeak
     * @param lambdaPerPeak
     * @param expectedPeaks
     * @param rndImmigrants
     */
    public EsDpiNiching(double nicheRadius, int muPerPeak, int lambdaPerPeak, int expectedPeaks, int rndImmigrants) {
        this(nicheRadius, muPerPeak, lambdaPerPeak, expectedPeaks, rndImmigrants, 0, 0, 0);
    }

    /**
     * If the niche radius is zero or negative, the automatic estimation is
     * used. Otherwise the given radius is used. If etaPresel is positive,
     * eta-preselection is activated, otherwise it is inactive.
     *
     * @param nicheRadius
     * @param muPerPeak
     * @param lambdaPerPeak
     * @param expectedPeaks
     * @param rndImmigrants
     * @param explorerPeaks
     * @param resetExplInterval
     * @param etaPresel
     */
    public EsDpiNiching(double nicheRadius, int muPerPeak, int lambdaPerPeak, int expectedPeaks, int rndImmigrants, int explorerPeaks, int resetExplInterval, int etaPresel) {
        doEtaPreselection = (etaPresel > 0);
        if (nicheRadius > 0) {
            setNicheRadius(nicheRadius);
            setUseNicheRadiusEstimation(false);
        } else {
            setUseNicheRadiusEstimation(true);
        }
        this.eta = etaPresel;
        this.muPerPeak = muPerPeak;
        this.lambdaPerPeak = lambdaPerPeak;
        this.numRndImmigrants = rndImmigrants;
        this.expectedPeaks = expectedPeaks;
        this.resetExplorerInterval = resetExplInterval;
    }

    public EsDpiNiching(EsDpiNiching o) {
        // TODO update!
        this.nicheRadius = o.nicheRadius;
        this.resetExplorerInterval = o.resetExplorerInterval;
        this.expectedPeaks = o.expectedPeaks;
        this.explorerPeaks = o.explorerPeaks;
        this.muPerPeak = o.muPerPeak;
        this.lambdaPerPeak = o.lambdaPerPeak;
        this.eta = o.eta;
        this.doEtaPreselection = o.doEtaPreselection;
        this.numRndImmigrants = o.numRndImmigrants;
        this.useNicheRadiusEstimation = o.useNicheRadiusEstimation;
        this.setAllowSingularPeakPops(o.isAllowSingularPeakPops());

        if (o.population != null) {
            this.population = (Population) o.population.clone();
        }
        if (o.problem != null) {
            this.problem = (InterfaceOptimizationProblem) o.problem.clone();
        }
        this.identifier = o.identifier;
        this.plotInterval = o.plotInterval;
    }

    public void hideHideable() {
        setDoEtaPreselection(isDoEtaPreselection());
        setUseNicheRadiusEstimation(isUseNicheRadiusEstimation());
        GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    }

    @Override
    public Object clone() {
        return new EsDpiNiching(this);
    }

    @Override
    public void initialize() {
        convCount = 0; // reset number of converged species (found peaks)
        collisions = 0; // reset the number of collisions
        population = new Population((getExpectedPeaks() + getExplorerPeaks()) * lambdaPerPeak);
        population.setMaxHistoryLength(haltingWindowLen);

        archive = new Population();
        peakOpts = new EvolutionStrategies[getExpectedPeaks() + getExplorerPeaks()];
        for (int i = 0; i < peakOpts.length; i++) {
            if (doEtaPreselection) {
                // When preselection is employed in this.optimize, the ESes only "get to see" muPerPeak individuals,
                // but they should generate lambdaPerPeak individuals, this lambda is set only after initialization.
                peakOpts[i] = new EvolutionStrategies(muPerPeak, muPerPeak, false);
            } else {
                peakOpts[i] = new EvolutionStrategies(muPerPeak, lambdaPerPeak, false);
            }
            // Trying to come close to the selection scheme of Shir&Bäck'05:
            peakOpts[i].setParentSelection(parentSel);
            peakOpts[i].setPartnerSelection(new SelectBestSingle(true));
            peakOpts[i].setProblem(problem);
            peakOpts[i].initialize();
            peakOpts[i].setLambda(lambdaPerPeak); // set lambda after initialization
            peakOpts[i].setForceOrigPopSize(false);
            peakOpts[i].checkPopulationConstraints();
            peakOpts[i].getPopulation().setMaxHistoryLength(haltingWindowLen);
            population.incrFunctionCallsBy(peakOpts[i].getPopulation().getFunctionCalls());
        }
        // possibly reduce immigrants to produce a conforming number of initial evaluations (same number as in later iterations: numPeaks*muLambda+immigrants)
//		int initialImmigrants = (fullNumPeaks()*lambdaPerPeak+getNumRndImmigrants())-(fullNumPeaks()*muPerPeak+getNumRndImmigrants());
        if (getNumRndImmigrants() > 0) {
            generateEvalImmigrants(getNumRndImmigrants());
        }
        if (isDoEtaPreselection() && (getEta() > getMuPerPeak())) {
            System.err.println("Warning, eta should be less-equal mu... setting eta=" + getMuPerPeak());
            setEta(getMuPerPeak());
        }
        collectPopulationIncGen(population, peakOpts, randomNewIndies);
        population.addPopulationChangedEventListener(this);
        population.setNotifyEvalInterval(50);

        if (isUseNicheRadiusEstimation()) {
            updateNicheRadius();
        }
    }

    private void updateNicheRadius() {
        AbstractEAIndividual indy = population.getEAIndividual(0);
        if (indy instanceof InterfaceDataTypeDouble) {
            setEstimatedNicheRadius(((InterfaceDataTypeDouble) indy).getDoubleRange());
        } else {
            System.err.println("Error, default niche radius can only be estimated for individuals of type InterfaceDataTypeDouble!");
        }
    }

    /**
     * Use the deactivation of converged species, storing them to an archive,
     * with the given parameters. If windowLen <= 0, the deactivation mechanism
     * is disabled. This provides for semi-sequential niching with DPI-ES
     *
     * @param threshold
     * @param windowLen
     */
    public void setSpeciesDeactivation(double threshold, int windowLen) {
        setHaltingWindow(windowLen);
        setEpsilonBound(threshold);
    }

    private void setEstimatedNicheRadius(double[][] range) {
        double estRad = EsDpiNiching.calcEstimatedNicheRadius(range, expectedPeaks, metric);
        setNicheRadius(estRad);
    }

    /**
     * Calculate the estimated maximal niche radius for a given problem range.
     * This is an estimate on the q-th part of the volume transfered to the
     * radius of a hypersphere, where q is the number of expected peaks.
     *
     * @param range
     * @param numExpectedOptima
     * @param metric
     * @return
     */
    public static double calcEstimatedNicheRadius(double[][] range, int numExpectedOptima, InterfaceDistanceMetric metric) {
        int dim = range.length;
        double r;
        if (metric instanceof EuclideanMetric) {
            r = Mathematics.getAvgRangeL2(range);
        } else { //if (metric instanceof PhenotypeMetric) { // r can be assumed to be 0.5*sqrt(dim)
            if (!(metric instanceof PhenotypeMetric)) {
                if ((metric instanceof IndividualDataMetric) && (((IndividualDataMetric) metric).getBaseMetric() instanceof EuclideanMetric)) {
                } // this is ok as well
                else {
                    System.err.println("Warning, unexpected metric in EsDpiNiching! Estimated niche radius may fail...");
                }
            }
            r = 0.5 * Math.sqrt(dim);
        }
//		setNicheRadius(r*Math.pow(numExpectedPeaks, -1./(double)dim));
        return r * Math.pow(numExpectedOptima, -1. / (double) dim);
    }

    public void addExpectedPeaks(int k) {
        // TODO  what to do if new peaks should be introduced?
        // inc. expected peaks, allocate new optimizers, possibly adapt clustering distance

        // this must happen to a specific time, namely before or after one iteration.
        if (k <= 0) {
            System.err.println("Invalid k in addExpectedPeaks (" + k + ").");
        }
        setExpectedPeaks(getExpectedPeaks() + k);
        EvolutionStrategies[] newPeakOpts = new EvolutionStrategies[getExpectedPeaks() + getExplorerPeaks()];
        for (int i = 0; i < newPeakOpts.length; i++) { // allocate new peak optimizers
            if (i < peakOpts.length) {
                newPeakOpts[i] = peakOpts[i];
            } else {
                newPeakOpts[i] = new EvolutionStrategies(peakOpts[0]);
            }
        }
        peakOpts = newPeakOpts;
        if (isUseNicheRadiusEstimation()) {
            updateNicheRadius();
        }
    }

    private int increaseExpectedPeaksCriterion() {
//		if ((getPopulation().getGeneration() % 5) == 0) return 1;
//		else return 0;
        if (isDoNumPeakAdaption() && (archive.size() >= getExpectedPeaks())) {
            return (int) Math.max(((double) getExpectedPeaks()) * 1.2, 2.);
        } else {
            return 0;
        }
    }

    public boolean isDoNumPeakAdaption() {
        return doNumPeakAdaption;
    }

    public void setDoNumPeakAdaption(boolean doApt) {
        doNumPeakAdaption = doApt;
    }

    public String doNumPeakAdaptionTipText() {
        return "Activate online adaption of the number of expected peaks";
    }

    @Override
    public void optimize() {
        Population peakPopSet[];
        if (increaseExpectedPeaksCriterion() > 0) {
            addExpectedPeaks(increaseExpectedPeaksCriterion());
        }
        if (TRACE) {
            System.out.println("--- FULL POP SIZE: " + population.size() + " , funcalls: " + population.getFunctionCalls() + ", gen " + population.getGeneration());
        }
        ClusteringDynPeakIdent dpiClustering = new ClusteringDynPeakIdent(getExpectedPeaks(), getLambdaPerPeak(), nicheRadius, true, metric);
        // perform dynamic peak identification resulting in the dynamic peak set dps
        dpiClustering.initClustering(population);
        peakPopSet = dpiClustering.cluster(population, population);
        // remove this instance as listener because we only want to "hear" events of the main population
        for (int i = 0; i < peakPopSet.length; i++) {
            peakPopSet[i].removePopulationChangedEventListener(this);
        }
        setGeneration(population.getGeneration(), peakPopSet);
        if (TRACE_DEMES) {
            printDemes("After clustering: ", peakPopSet);
        }

        int curNumPeaks = peakPopSet.length - 1;

//		boolean copyHashFromParents=true;
        // transfer population hash data along the peaks -> for each peak chose the original last population
        // and transfer the data to the new cluster
//		if (copyHashFromParents) {
        copyDataFromParents(peakPopSet);
//		} else {
//			copyDataFromClosest(clusteredPeakPops);
//		}

        int reqNewPeaks = 0;
        if (curNumPeaks < getExpectedPeaks()) {
            reqNewPeaks = getExpectedPeaks() - curNumPeaks; // not enough desired peaks were found
        }
        if (getExplorerPeaks() > 0) {
            if ((getPopulation().getGeneration() % resetExplorerInterval == 0)) {
                // reset the explorers
                reqNewPeaks += getExplorerPeaks();
            } else {
                // take over the last explorers
                // TODO only if they have not made it to the desired peak populations?
                Population[] clustersWithExplorers = new Population[peakPopSet.length + getExplorerPeaks()];
                for (int i = 0; i < clustersWithExplorers.length; i++) {
                    if (i < peakPopSet.length) {
                        clustersWithExplorers[i] = peakPopSet[i];
                    } // index i-1 because unclustered indies do not have an optimizer 
                    else {
                        clustersWithExplorers[i] = peakOpts[i - 1].getPopulation();
                    }
                }
                peakPopSet = clustersWithExplorers;
            }
        }
        // add missing and explorer peaks as new random individuals (or old unclustered ones)
        peakPopSet = generateMissingSpecies(peakPopSet, getMuPerPeak(), reqNewPeaks, false);

        if (TRACE_DEMES) {
            printDemes("After expansion: ", peakPopSet);
        }

        if (archive != null && (archive.size() > 0) && isReinitOnCollision()) {
            double origNicheRad = dpiClustering.getNicheRadius();
            dpiClustering.setNicheRadius(collisionDetNicheRadius);
            int[] assoc = dpiClustering.associateLoners(archive, peakPopSet, population);
            for (int i = 0; i < assoc.length; i++) {
                if (assoc[i] >= 0) {
                    collisions++;
                    // if an association was found, there is a peak close to an archived solution, so reset it.
                    if (!archive.getEAIndividual(i).isDominating(peakPopSet[assoc[i]].getBestEAIndividual())) {
                        // the new found species is actually better than the archived solution
                        archive.set(i, peakPopSet[assoc[i]].getBestEAIndividual().clone()); // replace archived indy
                    }
                    if (TRACE) {
                        System.out.println(" Converged on archived solution.. resetting peak pop " + assoc[i]);
                    }
                    peakPopSet[assoc[i]] = initRandomPeakPop(getMuPerPeak());
                }
            }
            dpiClustering.setNicheRadius(origNicheRad);
            if (TRACE_DEMES) {
                printDemes("After archivie-merge: ", peakPopSet);
            }
        }

        if (TRACE) {
            for (int k = 0; k < peakPopSet.length - 1; k++) {
                for (int j = k + 1; j < peakPopSet.length; j++) {
                    Population cut = peakPopSet[k].setCut(peakPopSet[j]);
                    if (cut.size() > 0) {
                        System.err.println("duplicate inherited in EsDpiNiching! OK if explorer peaks exist that are not always reinited and " + j + ">" + getExpectedPeaks() + " and " + k + "==0.");
                    }
                }
            }
        }
        plot = null;
        // now generate the lambda offsprings
//		nextGeneration = this.generateEvalChildren(dps); // create lambda new ones from mu parents
        for (int clustIndex = 1; clustIndex < peakPopSet.length; clustIndex++) {
            AbstractEAIndividual curPeak = (AbstractEAIndividual) peakPopSet[clustIndex].getBestEAIndividual().clone();
            // set the population
            Population curSpecies = peakPopSet[clustIndex];
            if (curSpecies.size() == 1 && (!isAllowSingularPeakPops())) {
                // Quoting SB05: "In case that the niche only contains one individual, the second parent will be the best indidivual of another niche."
                AbstractEAIndividual bestOther = (AbstractEAIndividual) selectBestFromOtherSpecies(clustIndex, peakPopSet).clone();
                if (TRACE) {
                    System.out.println("Adding best from other species: " + bestOther);
                }
                curSpecies.add(bestOther);
            } else if (curSpecies.size() == 0) {
                System.err.println("Warning, empty niche population in EsDpiNiching!");
            }
            peakOpts[clustIndex - 1].setPop(curSpecies); // -1 because clustering delivers first cluster at index 1

            // check for deactivation of that niche
            if (getHaltingWindow() > 0) {
                // possibly deactivate a converged species and add its representative to the archive
                HistoryConvergenceTerminator hConv = new HistoryConvergenceTerminator(haltingWindowLen, deactConvThresh, fitCriterion, true);
                if (hConv.isTerminated(curSpecies)) { // species is terminated
                    peakPopSet[clustIndex] = deactivateSpecies(clustIndex, true); // store best, reinitialize randomly
                }
            }

            Population optimizedSpecies = peakOpts[clustIndex - 1].getPopulation();
            if (doDraw(population.getGeneration())) {
                drawPeakPop("" + clustIndex, curSpecies);
            }
            if (TRACE) {
                System.out.println("Optimizing cluster index " + (clustIndex) + ", size " + curSpecies.size());
            }
            peakOpts[clustIndex - 1].optimize(); // !!!!!!!! Actual optimization step
            optimizedSpecies = peakOpts[clustIndex - 1].getPopulation();
            optimizedSpecies.putData(origPeakIndyKey, curPeak);
            population.incrFunctionCallsBy(optimizedSpecies.size());
//        	optimizedSpecies.incrGeneration(); // is already done in the .optimize() call above
//        	optimizedSpecies.incrFunctionCallsBy(optimizedSpecies.size());
            if (TRACE) {
                System.out.println("  ..." + optimizedSpecies.size() + " more funcalls... ");
            }

        }

        // we may have a problem if, by chance, all species have been deactivated simultaneously AND there are no unclustered !
        if (dynamicPopSize() == 0) {
            // if this is the case, we just reinit a single in analogy to a missing peak
            peakOpts[0].getPopulation().addPopulation(initRandomPeakPop(getMuPerPeak()));
        }

        if (TRACE) {
            for (int k = 0; k < peakPopSet.length - 1; k++) {
                for (int j = k + 1; j < peakPopSet.length; j++) {
                    Population cut = peakPopSet[k].setCut(peakPopSet[j]);
                    if (cut.size() > 1) {
                        // one may happen after a cluster had a size of one, since then another leader is added
                        if (cut.size() == 2 && (peakPopSet[k].size() == 2) && (peakPopSet[j].size() == 2)) {
                            // two may happen since they can be added reciprocally to each other
                        } else {
                            System.err.println("duplicate indy in EsDpiNiching. OK if explorer peaks exist that are not always reinited and " + j + ">" + getExpectedPeaks() + " and " + k + "==0.");
                        }
                    }
                }
            }
        }
        if (doEtaPreselection) { // this basically replaces ES-environment selection
            // select the eta best from the offspring per peak population
            // fill up to muPerPeak by adding from the old peak population
            SelectBestIndividuals selBest = new SelectBestIndividuals();
            Population loners = peakPopSet[0];
            plot = null;
            for (int i = 0; i < peakOpts.length; i++) {
                Population offspring = peakOpts[i].getPopulation();
                if (i + 1 >= peakPopSet.length) {
                    System.err.println("Warning: fewer clusters than expected peaks in EsDpiNiching!");
                    offspring.clear(); // empty set to avoid duplicates!
                } else {
                    if (TRACE) {
                        System.out.println("EtaPresel: from " + offspring.size() + " offspring selecting " + eta);
                    }
                    Population selected = selBest.selectFrom(offspring, eta);
                    if (!selected.isSubSet(offspring)) {
                        System.err.println("fatal problem in EsDpiNiching!!!");
                    }
                    if (offspring.setCut(peakPopSet[i + 1]).size() > 0) {
                        System.err.println("problem in EsDpiNiching!!!");
                    }
                    int delta = muPerPeak - eta;
                    if (delta > 0) {
                        Population filterPeakPop = peakPopSet[i + 1].filter(selected);
                        if (TRACE) {
                            System.out.println("Adding " + Math.min(delta, filterPeakPop.size()) + " from peak population.");
                        }
                        // (few) duplicates may happen because parents are copied to another peak population
                        // if a cluster had a size of 1 AND parents may survive due to elitism.
                        selected.addPopulation(selBest.selectFrom(filterPeakPop, Math.min(delta, filterPeakPop.size())), false);
                        if (selected.size() < muPerPeak && (addLonersToPeaks)) {
                            delta = Math.min(muPerPeak - selected.size(), loners.size());
                            if (TRACE) {
                                System.out.println("filling up with lucky loners: " + delta);
                            }
                            // fill up with loner indies
                            SelectRandom selRnd = new SelectRandom(false); // no duplicates wanted!
                            Population luckyLosers = selRnd.selectFrom(loners, delta);
                            selected.addPopulation(luckyLosers, true);
                            loners.removeMembers(luckyLosers, true);
                            // fill up with random indies
                            //        				Population randomNewIndies = new Population(lambdaPerPeak - selected.size());
                            //        				problem.initializePopulation(randomNewIndies); // function calls??
                            //        				selected.addAll(randomNewIndies);
                        }
                    }
                    if (TRACE) {
                        for (int k = 0; k < i; k++) {
                            if (selected.setCut(peakOpts[k].getPopulation()).size() > 2) { //one may happen after a cluster had size one (see above) 
                                System.err.println("Warning, nonempty set cut between " + k + " and " + i + " !");
                            }
                        }
                    }
                    peakOpts[i].population.clear();
                    peakOpts[i].population.addAll(selected);
                    if (doDraw(population.getGeneration())) {
                        drawPeakPop("" + i, selected);
                    }
                }
            }
        }
//        System.out.println("Best of second peak: " + clusteredPeakPops[2].getBestEAIndividual());
        if (doDraw(population.getGeneration()) && archive != null) {
            for (int i = 0; i < this.archive.size(); i++) {
                ClusterBasedNichingEA.plotIndy(plot, 'x', (InterfaceDataTypeDouble) archive.get(i));
            }
        }
        if (getNumRndImmigrants() > 0) {
            generateEvalImmigrants(getNumRndImmigrants());
        }
        collectPopulationIncGen(population, peakOpts, randomNewIndies);
        //this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED); // moved this to registerPopulationStateChanged which is called from the population
    }

    private Population deactivateSpecies(int clustIndex, boolean resetRandomly) {
        Population optimizedSpecies = peakOpts[clustIndex - 1].getPopulation();
        convCount++;
        // get best indy, add it to archive, clear population of the optimizer for a reset
        archive.add(optimizedSpecies.getBestIndividual());
        if (resetRandomly) {
            optimizedSpecies.clear();
            optimizedSpecies.clearHistory();
            optimizedSpecies = initRandomPeakPop(getMuPerPeak());
            peakOpts[clustIndex - 1].setPop(optimizedSpecies);
        }
        return optimizedSpecies;
    }

    /**
     * Copy the additional data from the parent populations to the new clusters.
     *
     * @param clusteredPeakPops
     */
    private void copyDataFromParents(Population[] clusteredPeakPops) {
        for (int i = 1; i < clusteredPeakPops.length; i++) {
            Integer origEsPop = (Integer) clusteredPeakPops[i].getBestEAIndividual().getData(originalPeakPop);
            if (origEsPop != null && (origEsPop >= 0)) {
                Population origPop = peakOpts[origEsPop].getPopulation();
                clusteredPeakPops[i].copyHashData(origPop);
                clusteredPeakPops[i].SetHistory(origPop.getHistory()); // copy the history for deactivation!
//				System.out.println("Copied hash for peak " + clusteredPeakPops[i].getBestEAIndividual() + ", cluster " + i + " from " + origEsPop);
            } else { // ok in the first iteration of if the indy was a random immigrant
                if (population.getGeneration() > 1 && (getNumRndImmigrants() == 0)) {
                    System.err.println("Error, empty original es pop ID!");
                }
            }
        }
    }

    //	/**
//	 * For each new cluster copy the additional data from the closest parent population.
//	 *  
//	 * @param clusteredPeakPops
//	 */
//	private void copyDataFromClosest(Population[] clusteredPeakPops) {
//		Population centers = new Population(clusteredPeakPops.length);
//		AbstractEAIndividual indy;
//		// collect leaders
//		for (int i=0; i<peakOpts.length; i++) {
//			Population peakPop = peakOpts[i].getPopulation();
//			indy = (AbstractEAIndividual) peakPop.getData(origPeakIndyKey);
//			if (indy!=null) centers.add(indy);
//			else centers.add(peakOpts[i].getPopulation().getCenterIndy());
//		}
//		for (int i=1; i<clusteredPeakPops.length; i++) {
//			indy = clusteredPeakPops[i].getBestEAIndividual();
//			Pair<Integer,Double> closestIdDist = Population.getClosestFarthestIndy(clusteredPeakPops[i].getBestEAIndividual(), centers, metric, true);
//			Population closestPop = peakOpts[closestIdDist.head()].getPopulation();
//			System.out.println("Closest to new peak " + indy.toString() + " is old pop " + closestPop.getData(origPeakIndyKey));
//			System.out.println("   meanX was " + (MutateESRankMuCMA.getMeanXOfPop(closestPop)));
//			clusteredPeakPops[i].copyHashData(closestPop);
//		}
//	}
    private void printDemes(String prefix, Population[] peakPops) {
        System.out.print(prefix + " demes: ");
        for (int i = 0; i < peakPops.length; i++) {
            if (i < 5) {
                StringBuffer sb = new StringBuffer();
                Formatter fm = new Formatter(sb);
                fm.format("%d/%.2f ", peakPops[i].size(), peakPops[i].getPopulationMeasures()[0]);
                System.out.print(sb.toString());
            } else {
                System.out.print(peakPops[i].size() + " ");
            }
        }
        System.out.println();
        for (int i = 0; i < peakPops.length; i++) {
            try {
                if (peakPops[i].size() > 0) {
                    System.out.print(format(peakPops[i].getBestEAIndividual().getFitness(0), 3, 3));
                }
            } catch (Exception e) {
                System.err.println("NARG!");
            }

        }
        System.out.println();
    }

    public static String format(double d, int len, int prec) {
        StringBuffer sb = new StringBuffer();
        Formatter fm = new Formatter(sb);
        if (Math.abs(d) > 1e6) {
            fm.format("%" + prec + "." + len + "e ", d);
        } else {
            fm.format("%" + prec + "." + len + "f ", d);
        }
        return sb.toString();
    }

    private void setGeneration(int gen, Population[] pops) {
        for (int i = 0; i < pops.length; i++) {
            pops[i].setGeneration(gen);
        }
    }

    /**
     * Generate additional pseudo peak populations (species) by either moving
     * unclustered ones or by creating new ones randomly. If unclustered indies
     * are to be used, there must be enough of them available. The new peaks are
     * appended to the given cluster list and returned (as a new array
     * instance).
     *
     * @param origClusters              the original clusters, where index 0 is the
     *                                  unclustered rest
     * @param cntPerNewSpecies          the number of indies to use per new peak
     *                                  population
     * @param fromUnclusteredOrRandomly if true, use unclustered indies as new
     *                                  pops, otherwise create them randomly
     * @return
     */
    private Population[] generateMissingSpecies(Population[] origClusters, int cntPerNewSpecies, int newPops, boolean fromUnclusteredOrRandomly) {
        if (newPops == 0) {
            return origClusters;
        } else {
            Population[] newClusters = new Population[origClusters.length + newPops];
            if (fromUnclusteredOrRandomly) {
                //    		int missingPeaks = newClusters.length-origClusters.length;
                for (int i = origClusters.length; i < newClusters.length; i++) { // for each new species to be formed:
                    newClusters[i] = origClusters[0].moveRandNIndividuals(cntPerNewSpecies);
                }
            } else { // create random indies with really bad fitness
                for (int i = origClusters.length; i < newClusters.length; i++) { // for each new species to be formed:
                    newClusters[i] = initRandomPeakPop(cntPerNewSpecies);
                }
            }
            for (int i = 0; i < origClusters.length; i++) {
                newClusters[i] = origClusters[i];
            }
            if (TRACE) {
                System.out.println("Generated missing peak species...");
            }
            return newClusters;
        }
    }

    /**
     * Initialize a new peak population with the given number of indies, which
     * are initialized randomly (using the problem instance) and assigned a
     * maximally bad fitness.
     *
     * @param cntPerNewSpecies
     * @return
     */
    private Population initRandomPeakPop(int cntPerNewSpecies) {
        Population newPop = new Population(cntPerNewSpecies);
        problem.initializePopulation(newPop);
        newPop.putData(EvolutionStrategies.esLambdaParam, getLambdaPerPeak());
        newPop.putData(EvolutionStrategies.esMuParam, getMuPerPeak());
        newPop.setMaxHistoryLength(haltingWindowLen);
        double[] badFit = population.getEAIndividual(0).getFitness().clone();
        Arrays.fill(badFit, Double.MAX_VALUE);
        newPop.setAllFitnessValues(badFit);
        return newPop;
    }

    private void generateEvalImmigrants(int cnt) {
        if (cnt > 0) {
            randomNewIndies = new Population(cnt);
            problem.initializePopulation(randomNewIndies);
            problem.evaluate(randomNewIndies);
            population.incrFunctionCallsBy(cnt);
            if (TRACE) {
                System.out.println("evaluated immigrants: " + randomNewIndies.size());
            }
        } else {
            randomNewIndies = null;
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.populationChangedEventListener != null) {
            this.populationChangedEventListener.registerPopulationStateChanged(this, name);
        }
    }

    private AbstractEAIndividual selectBestFromOtherSpecies(int i, Population[] clusteredSpecies) {
        // the index must be >0 and != i:
        int rndIndex = RNG.randomInt(clusteredSpecies.length - 2);
        if (rndIndex == 0) {
            rndIndex++;
        }
        if (rndIndex == i) {
            rndIndex++;
        }
        if (TRACE) {
            System.out.println("selected spec index " + rndIndex);
        }
        return clusteredSpecies[rndIndex].getBestEAIndividual();
    }

    private Population[] getOptPops() {
        Population pops[] = new Population[peakOpts.length];
        for (int i = 0; i < peakOpts.length; i++) {
            pops[i] = peakOpts[i].getPopulation();
        }
        return pops;
    }

    private void drawPeakPop(String prefix, Population npop) {
        if (npop != null && (npop.size() > 0)) {
            try {
                if (plot == null) {
                    plot = new TopoPlot("Niching-ES " + npop.getGeneration(), "x", "y");
                    plot.setParams(50, 50);
                    if (problem instanceof Interface2DBorderProblem) {
                        plot.setTopology((Interface2DBorderProblem) problem);
                    }
                }
                ClusterBasedNichingEA.plotPopConnected(plot, npop);
                plot.drawIndividual(1, 0, "", npop.getBestEAIndividual());
//		plot.drawPopulation(prefix, npop);
            } catch (Exception e) {
                plot = null;
            }
        }
    }

    private boolean doDraw(int gen) {
        return (plotInterval > 0) && ((gen % plotInterval) == 0);
    }

    /**
     * Within pop, replace all individuals which are better than indy by indy.
     * This makes sure that SelectBest will select indy on the population.
     *
     * @param pop
     * @param indy
     */
    private static void replaceWorstAndAllBetterIndiesBy(Population pop,
                                                         AbstractEAIndividual indy) {
        int lastIndex = -1;
        if (!pop.contains(indy)) { // replace best
            int bestIndex = pop.getIndexOfBestIndividualPrefFeasible();
            pop.set(bestIndex, indy);
            lastIndex = bestIndex;
        }
        int bestIndex = pop.getIndexOfBestIndividualPrefFeasible();
        while ((lastIndex != bestIndex) && (pop.getEAIndividual(bestIndex).isDominatingDebConstraints(indy))) {// remove best, add indy
            pop.set(bestIndex, indy);
            lastIndex = bestIndex;
            bestIndex = pop.getIndexOfBestIndividualPrefFeasible();
        }
    }

    /**
     * Perform mutation adaption for instances of InterfaceMutationGenerational.
     * Specifically, the original population of each dps-member is cloned and
     * the method adaptAfterSelection is called which may modify the population.
     * The vector of these new populations is returned.
     *
     * @param collectedPop
     * @param dps
     * @param opts
     * @return the vector of Population instances
     */
    private static Population[] performDPSGenerationalAdaption(Population collectedPop,
                                                               Population dps, EvolutionStrategies[] opts) {
        // loop over all dps individuals and
        // 	 clone the new mutator instance from the original population
        //   call the generational adaption for the new instance
        Population[] resPops = new Population[dps.size()];
        for (int i = 0; i < dps.size(); i++) {
            if (dps.getEAIndividual(i).getMutationOperator() instanceof InterfaceAdaptOperatorGenerational) {
                InterfaceAdaptOperatorGenerational mutGen = (InterfaceAdaptOperatorGenerational) dps.getEAIndividual(i).getMutationOperator();
                Integer originIndex = (Integer) dps.getEAIndividual(i).getData(originalPeakPop);
                Population selectedPop = new Population(1);
                selectedPop.add(dps.getEAIndividual(i));
                Population newPop = (Population) opts[originIndex].getPopulation().clone();
                mutGen.adaptAfterSelection(newPop, selectedPop);
                resPops[i] = newPop;
            }
        }
        return resPops;
    }

    /**
     * Collect clones of the original populations for each peak.
     *
     * @param dps
     * @param opts
     * @return
     */
    private static Population[] collectOriginalPops(Population dps, EvolutionStrategies[] opts) {
        Population[] resPops = new Population[dps.size()];
        for (int i = 0; i < dps.size(); i++) {
            Integer originIndex = (Integer) dps.getEAIndividual(i).getData(originalPeakPop);
            resPops[i] = (Population) opts[originIndex].getPopulation().clone();
        }
        return resPops;
    }

    /**
     * Update the collected population pop from the ESes. The function calls of
     * one last iteration are added up, so this method is expected to be called
     * exactly once per niching iteration. Immigrants are added as well - and
     * the number of immigrants is added to the pop's function calls.
     *
     * @param pop
     * @param esses
     */
    private static void collectPopulationIncGen(Population pop, EvolutionStrategies[] esses, Population immigrants) {
        pop.clear();
        for (int i = 0; i < esses.length; i++) {
            Population pi = esses[i].getPopulation();
            pi.putDataAllIndies(originalPeakPop, new Integer(i));
            // (few) duplicates may happen because parents are copied to another peak population
            // if a cluster had a size of 1 AND parents may survive due to elitism.
            pop.addPopulation(pi, false);
//			if (i==0) pop.setGeneration(pi.getGeneration());
//			else if (pop.getGeneration()!=pi.getGeneration()) System.err.println("Error, mismatching generation in collectPopulation");
        }
        if (immigrants != null) {
            immigrants.putDataAllIndies(originalPeakPop, new Integer(-2));
            pop.addPopulation(immigrants, true);
        }
        pop.incrGeneration();
        pop.synchSize();
        if (TRACE) {
            System.out.println("Collected " + pop.size() + " indies in pop.");
        }
    }

    /**
     * Calculate the dynamic population size, which is the number of individuals
     * that are currently "alive" in the peak set. This must be implemented in
     * analogy to
     * {@link #collectPopulationIncGen(Population, EvolutionStrategies[], Population)}
     *
     * @return
     */
    private int dynamicPopSize() {
        int numIndies = 0;
        for (int i = 0; i < peakOpts.length; i++) {
            Population pi = peakOpts[i].getPopulation();
            numIndies += pi.size();
        }
        if (randomNewIndies != null) {
            numIndies += randomNewIndies.size();
        }
        return numIndies;
    }

    public double getNicheRadius() {
        return nicheRadius;
    }

    public void setNicheRadius(double nicheRadius) {
        this.nicheRadius = nicheRadius;
    }

    public String nicheRadiusTipText() {
        return "The niche radius to be used.";
    }

    public int getLambdaPerPeak() {
        return lambdaPerPeak;
    }

    public void setLambdaPerPeak(int lambdaPP) {
        lambdaPerPeak = lambdaPP;
    }

    public String lambdaPerPeakTipText() {
        return "The number of descendants created for each peak.";
    }

    public int getExpectedPeaks() {
        return expectedPeaks;
    }

    public void setExpectedPeaks(int ep) {
        if (ep > 0) {
            expectedPeaks = ep;
        } else {
            System.err.println("Error, expecting positive number of peaks!");
        }
    }

    public String expectedPeaksTipText() {
        return "The number of expected peaks on the problem.";
    }

    public int getExplorerPeaks() {
        return explorerPeaks;
    }

    public void setExplorerPeaks(int ep) {
        if (ep >= 0) {
            explorerPeaks = ep;
        } else {
            System.err.println("Error, expecting nonzero number of explorer peaks!");
        }
    }

    public String explorerPeaksTipText() {
        return "The number of additional explorer peaks.";
    }

    @Override
    public String getName() {
        return identifier + "_" + getExpectedPeaks() + "_" + getNicheRadius();
    }

    @Override
    public void addPopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        populationChangedEventListener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (ea.equals(populationChangedEventListener)) {
            populationChangedEventListener = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        Population peaks = new Population(peakOpts.length);
        for (int i = 0; i < peakOpts.length; i++) {
            peaks.add(peakOpts[i].getPopulation().getBestEAIndividual());
        }
        if (archive != null) {
            peaks.addPopulation(archive); // add stored indies
        }
        peaks.synchSize();
        return new SolutionSet(getPopulation(), peaks);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(String name) {
        identifier = name;
    }

    @Override
    public void setPopulation(Population pop) {
        // this might cause problems if the pop.size() does not fit the EsDpiNiching parameters mu/lamba per peak
        this.population = pop;
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return problem;
    }

    @Override
    public void setProblem(InterfaceOptimizationProblem prob) {
        this.problem = prob;
    }

    @Override
    public String getStringRepresentation() {
        StringBuffer sb = new StringBuffer("EsDpiNiching:\n");
        sb.append("Optimization Problem: ");
        sb.append(this.problem.getStringRepresentationForProblem(this));
        sb.append("\n");
        sb.append(this.population.getStringRepresentation());
        return sb.toString();
    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
//		int pSize = pop.size();
        this.population = (Population) pop.clone();
        if (reset) {
            this.population.init();
            this.problem.evaluate(population);
            population.incrGeneration();
        }
    }

    public int getPlotInterval() {
        return plotInterval;
    }

    public void setPlotInterval(int plotInterval) {
        this.plotInterval = plotInterval;
    }

    public String plotIntervalTipText() {
        return "If > 0, show debug visualization at indicated iterations.";
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public String etaTipText() {
        return "The number of offspring individuals per peak which will be preselected.";
    }

    public boolean isDoEtaPreselection() {
        return doEtaPreselection;
    }

    public void setDoEtaPreselection(boolean doEtaPreselection) {
        this.doEtaPreselection = doEtaPreselection;
        GenericObjectEditor.setShowProperty(this.getClass(), "eta", doEtaPreselection);
    }

    public String doEtaPreselectionTipText() {
        return "Replace ES environmental selection by choosing some individuals from the offspring within a niche and some from the former niche population.";
    }

    public void setNumRndImmigrants(int numRndImmigrants) {
        this.numRndImmigrants = numRndImmigrants;
    }

    public int getNumRndImmigrants() {
        return numRndImmigrants;
    }

    public String numRndImmigrantsTipText() {
        return "A number of individuals will be randomly created in every iteration.";
    }

    public int getMuPerPeak() {
        return muPerPeak;
    }

    public void setMuPerPeak(int muPerPeak) {
        this.muPerPeak = muPerPeak;
    }

    public String muPerPeakTipText() {
        return "Number of parent individuals per niche.";
    }

    public void setUseNicheRadiusEstimation(boolean useNicheRadiusEstimation) {
        this.useNicheRadiusEstimation = useNicheRadiusEstimation;
        GenericObjectEditor.setHideProperty(this.getClass(), "nicheRadius", useNicheRadiusEstimation);
    }

    public boolean isUseNicheRadiusEstimation() {
        return useNicheRadiusEstimation;
    }

    public String useNicheRadiusEstimationTipText() {
        return "Activate to use a niche radius corresponding to the q-th part of the search space (q number of peaks expected) - often niche radii should be smaller since this is close to the upper bound.";
    }

    public InterfaceSelection getParentSelection() {
        return parentSel;
    }

    public void setParentSelection(InterfaceSelection parentSel) {
        this.parentSel = parentSel;
    }

    public String parentSelectionTipText() {
        return "Set the parent selection method for the underlying ES.";
    }

    public void setAllowSingularPeakPops(boolean allowSingularPeakPops) {
        this.allowSingularPeakPops = allowSingularPeakPops;
    }

    public boolean isAllowSingularPeakPops() {
        return allowSingularPeakPops;
    }

    public String allowSingularPeakPopsTipText() {
        return "Allow peak populations of size 1 or force a randomly selected other peak as second indy.";
    }

    public int getResetExplorerInterval() {
        return resetExplorerInterval;
    }

    public void setResetExplorerInterval(int resInt) {
        if (resInt > 0) {
            this.resetExplorerInterval = resInt;
        } else {
            System.err.println("The explorer reset interval should be positive!");
        }
    }

    public String resetExplorerIntervalTipText() {
        return "The explorer peaks are reset in intervals of iterations (generations).";
    }

    public int getHaltingWindow() {
        return haltingWindowLen;
    }

    public void setHaltingWindow(int hw) {
        haltingWindowLen = hw;
    }

    public String haltingWindowTipText() {
        return "Number of generations after which a species without improvement is seen as converged and deactivated; set to zero to disable.";
    }

    public double getEpsilonBound() {
        return deactConvThresh;
    }

    public void setEpsilonBound(double epsilonBound) {
        this.deactConvThresh = epsilonBound;
    }

    public String epsilonBoundTipText() {
        return "If fitness std. dev. changes less than this value within the halting window, convergence is assumed.";
    }

    @Override
    public String[] getAdditionalDataHeader() {
        return new String[]{"nicheRadius", "numExpectedPeaks", "numArchived", "archivedMeanDist", "numCollisions"};
    }

    @Override
    public String[] getAdditionalDataInfo() {
        return new String[]{"The niche radius employed for Dynamic Peak Identificatio", "The number of expected peaks",
                "The number of stored potential local optima", "Mean distance of archived solutions",
                "The number of collisions detected so far"};
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        return new Object[]{getNicheRadius(), getExpectedPeaks(), archive.size(), archive.getPopulationMeasures()[0], collisions};
    }

    /**
     * This method is necessary to allow access from the Processor.
     *
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

    public void setReinitOnCollision(boolean reinitAlreadyFound) {
        this.reinitAlreadyFound = reinitAlreadyFound;
    }

    public boolean isReinitOnCollision() {
        return reinitAlreadyFound;
    }

    public String reinitOnCollisionTipText() {
        return "Indicate whether already known (archived) peaks should trigger a reset of close-by species (corresp. to niche radius).";
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (getPopulation() != source) {
            System.err.println("Warning, mismatching population in " + this.getClass().getName());
        }
        if (name.equals(Population.FUN_CALL_INTERVAL_REACHED)) {
//    		getPopulation().setFunctionCalls(((Population)source).getFunctionCalls()); // this is ugly and I dont know what its for.. possibly if the population instance changes along the GUi?
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        } else {
            // this may come from cloned instances with the same listener - should not happen since they are removed. 
            // it may still come from "incGeneration" calls - we can ignore those
//			System.err.println("Not forwarding event " + name);
        }
    }
}
