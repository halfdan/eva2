package eva2.optimization.strategies;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.mutation.MutateESRankMuCMA;
import eva2.optimization.operator.terminators.FitnessConvergenceTerminator;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.ChangeTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.DirectionTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.StagnationTypeEnum;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * This implements the IPOP (increased population size restart) strategy ES, which increases
 * the ES population size (lambda) after phases or stagnation and restarts the optimization.
 * Stagnation is for this implementation defined by a FitnessConvergenceTerminator instance
 * which terminates if the absolute change in fitness is below a threshold (default 10e-12) for a
 * certain number of generations (default: 10+floor(30*n/lambda) for problem dimension n).
 * <p/>
 * If the MutateESRankMuCMA mutation operator is used, additional criteria are used for restarts,
 * such as numeric conditions of the covariance matrix.
 * Lambda is increased multiplicatively for every restart, and typical initial values are
 * mu=5, lambda=10, incFact=2.
 * The IPOP-CMA-ES won the CEC 2005 benchmark challenge.
 * Refer to Auger&Hansen 05 for more details.
 * <p/>
 * A.Auger & N.Hansen. A Restart CMA Evolution Strategy With Increasing Population Size. CEC 2005.
 *
 * @author mkron
 */
public class EvolutionStrategyIPOP extends EvolutionStrategies implements InterfacePopulationChangedEventListener, InterfaceAdditionalPopulationInformer {
    private static final long serialVersionUID = 4102736881931867818L;
    int dim = -1;
    int initialLambda = 10;

    private double stagThreshold = 10e-12;
    //	private int stagTime = -1;
    private int stagTimeArbitrary = 10;
    private boolean useArbitraryStagTime = false;

    double incPopSizeFact = 2.;
    FitnessConvergenceTerminator fitConvTerm = null;
    LinkedList<AbstractEAIndividual> bestList = null;
    AbstractEAIndividual best = null;

    public EvolutionStrategyIPOP(int mu, int lambda, boolean usePlus) {
        super(mu, lambda, usePlus);
        setForceOrigPopSize(false);
        setInitialLambda(lambda);
    }

    public EvolutionStrategyIPOP() {
        super();
        setForceOrigPopSize(false);
        setMu(5);
        setLambda(10);
    }

    public EvolutionStrategyIPOP(EvolutionStrategyIPOP other) {
        super(other);
        dim = other.dim;
        initialLambda = other.initialLambda;
        incPopSizeFact = other.incPopSizeFact;
        stagThreshold = other.stagThreshold;
//		stagTime 		= other.stagTime;

        if (other.fitConvTerm != null) {
            fitConvTerm = new FitnessConvergenceTerminator(other.fitConvTerm);
        }
    }

    @Override
    public Object clone() {
        return new EvolutionStrategyIPOP(this);
    }

    @Override
    public void optimize() {
//        Population  nextGeneration, parents;
//
//        // first perform the environment selection to select myu parents
//        parents = selectParents();
//
//        // population / parents are of sizes lambda / mu
//        if (parents.getEAIndividual(0).getMutationOperator() instanceof InterfaceMutationGenerational) {
//        	((InterfaceMutationGenerational)parents.getEAIndividual(0).getMutationOperator()).adaptAfterSelection(getPopulation(), parents);
//        }
//        
//        // now generate the lambda offsprings
//        nextGeneration = this.generateEvalChildren(parents); // create lambda new ones from mu parents
//        
//        if (this.isPlusStrategy()) nextGeneration.addPopulation(parents);
//       
//        setPop(getReplacePop(nextGeneration));
//
//        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        //////////////////////////
        super.optimize();

        // remember the best indy
        if ((best == null) || !best.isDominating(getPopulation().getBestEAIndividual())) {
            best = getPopulation().getBestEAIndividual();
        }
        if (EvolutionStrategyIPOP.testIPOPStopCrit(fitConvTerm, getPopulation())) {
            // reinitialize population with increased mu,lambda
            boostPopSize();
        }
    }

    @Override
    public void hideHideable() {
        GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
        setStagnationTimeUserDef(isStagnationTimeUserDef());
    }

    /**
     * Reinitialize population with increased mu,lambda
     */
    private void boostPopSize() {
        // potentially increase pop size, increase by at least one if the factor is > 1.
        int newLambda = Math.max((int) (getLambda() * incPopSizeFact), getLambda() + ((incPopSizeFact > 1.) ? 1 : 0));
        setLambda(newLambda);
        checkPopulationConstraints();
        // update the stagnation time in the terminator
        if (!isStagnationTimeUserDef() && (fitConvTerm != null)) {
            fitConvTerm.setStagnationTime(calcDefaultStagnationTime());
            fitConvTerm.init(getProblem());
        }
        bestList.add(best);
        best = null;
        Population newPop = getPopulation().cloneWithoutInds();
        getProblem().initializePopulation(newPop); // this is where the reinit event of Pop is called, meaning that the rank-mu-cma matrix is reinitialized as well
        double[] badFit = getPopulation().getBestFitness().clone();
        Arrays.fill(badFit, Double.MAX_VALUE);
        newPop.setAllFitnessValues(badFit);
        getPopulation().clear();
        getPopulation().addAll(newPop);
        getProblem().evaluate(getPopulation());
    }

    @Override
    protected void firePropertyChangedEvent(String name) {
        if (name.equals(Population.FUN_CALL_INTERVAL_REACHED)) {
            super.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        } else {
        } // nothing, evt is produced in #registerPopulationStateChanged, dont forward original due to changing pop size
    }

    @Override
    public void init() {
//    	setMu(initialMu);
        if (getMu() > initialLambda) {
            System.err.println("mu is " + getMu() + ", initial lambda was " + initialLambda);
            setMu((initialLambda / 2) + 1);
            System.err.println("Warning, too small initial lambda, adapting mu to " + getMu());
        }
        super.setLambda(initialLambda);
        checkPopulationConstraints();
        setForceOrigPopSize(false);
        getPopulation().setNotifyEvalInterval(Math.max(initialLambda, 100));
        super.init();
        bestList = new LinkedList<AbstractEAIndividual>();
        best = getPopulation().getBestEAIndividual();
        dim = AbstractEAIndividual.getDoublePositionShallow(getPopulation().getEAIndividual(0)).length;
        fitConvTerm = new FitnessConvergenceTerminator(stagThreshold, (isStagnationTimeUserDef()) ? stagTimeArbitrary : calcDefaultStagnationTime(), StagnationTypeEnum.generationBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease); // gen. based, absolute
        getPopulation().addPopulationChangedEventListener(this);
        getPopulation().setNotifyEvalInterval(initialLambda);
    }

    /**
     * The default stagnation time in generations as suggested by Auger&Hansen 05.
     *
     * @return
     */
    private int calcDefaultStagnationTime() {
        return (int) (10 + Math.floor(30 * dim / getLambda()));
    }

    /**
     * Test for the IPOP stopping criteria.
     *
     * @param population
     * @return
     */
    public static boolean testIPOPStopCrit(InterfaceTerminator term, Population pop) {
        boolean ret = false;

        int curGen = pop.getGeneration();
        MutateESRankMuCMA rcmaMute = null;
        if (pop.getEAIndividual(0).getMutationOperator() instanceof MutateESRankMuCMA) {
            rcmaMute = (MutateESRankMuCMA) pop.getEAIndividual(0).getMutationOperator();
        }

        // stop if the range of the best fitness of the last 10 + flor(30 n /lambda) generations is zero
        // or if the range of these values and all fit values of the recent generation is below Tolfun = 10^-12
        //// interpret it a bit differently using FitnessConvergenceTerminator
        if (term.isTerminated(new SolutionSet(pop))) {
//			System.out.println(fitConvTerm.lastTerminationMessage());
            ret = true;
        } else {
            if (rcmaMute != null) {
                // stop if the std dev of the normal distribution is smaller than TolX in all coords
                // and sigma p_c is smaller than TolX in all components; TolX = 10^-12 sigma_0

                if (rcmaMute.testAllDistBelow(pop, 10e-12 * rcmaMute.getFirstSigma(pop))) {
                    ret = true;
                }

                // stop if adding a 0.1 std dev vector in a principal axis dir. of C does not change <x>_w^g
                if (!ret && (rcmaMute.testNoChangeAddingDevAxis(pop, 0.1, curGen))) {
                    ret = true;
                }

                // stop if adding a 0.2 std dev in each coordinate does (not???) change <x>_w^g
                if (!ret && (rcmaMute.testNoEffectCoord(pop, 0.2))) {
                    ret = true;
                }

                // stop if the condition number of C exceeds 10^14
                if (!ret && (rcmaMute.testCCondition(pop, 10e14))) {
                    ret = true;
                }
//				System.out.println("ret is " + ret);
            }
        }
        return ret;
    }

    /**
     * Returns the current population and a set of best individuals found (best current
     * and best single ones before
     * reinitializing the population after boosting the population size).
     *
     * @return A solution set of the current population and possibly earlier solutions
     */
    @Override
    public SolutionSet getAllSolutions() {
        Population sols = getPopulation().cloneWithoutInds();
        if (bestList != null) {
            sols.addAll(bestList);
        }
        if (best != null) {
            sols.add(best);
        } else {
            sols.add(getPopulation().getBestEAIndividual());
        }

        SolutionSet solSet = new SolutionSet(getPopulation(), sols);
        return solSet;
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (name.equals(Population.FUN_CALL_INTERVAL_REACHED)) {
            getPopulation().SetFunctionCalls(((Population) source).getFunctionCalls()); // TODO this is ugly
            super.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        } else {
//			System.err.println("Not forwarding event " + name);
        }
    }

    @Override
    public String getName() {
        return getIncPopSizeFact() + "-IPOP-ES";
    }

    public static String globalInfo() {
        return "An ES with increasing population size.";
    }
//	
//	protected void checkPopulationConstraints() {
//		if (getLambda()!=initialLambda) setLambda(initialLambda);
//		if (getMu()>getLambda()) System.err.println("Invalid mu/lambda ratio!");
//		super.checkPopulationConstraints();
//	}

    /**
     * Set an initial population size (if smaller lambda this is ignored).
     *
     * @param l The inital population size.
     */
    public void setInitialLambda(int l) {
        initialLambda = l;
//		if (initialLambda < getMu()) setMu((initialLambda/2)+1); // do this on init
    }

    public int getInitialLambda() {
        return initialLambda;
    }

    public String initialLambdaTipText() {
        return "Set the initial population size (lambda); mu should be about lambda/2";
    }

    /**
     * @return the incPopSizeFact
     */
    public double getIncPopSizeFact() {
        return incPopSizeFact;
    }

    /**
     * @param incPopSizeFact the incPopSizeFact to set
     */
    public void setIncPopSizeFact(double incPopSizeFact) {
        this.incPopSizeFact = incPopSizeFact;
    }

    public String incPopSizeFactTipText() {
        return "Factor by which to increase lambda for each restart event, default is 2.";
    }

    /**
     * @return the stagThreshold
     */
    public double getStagThreshold() {
        return stagThreshold;
    }

    /**
     * @param stagThreshold the stagThreshold to set
     */
    public void setStagThreshold(double stagThreshold) {
        this.stagThreshold = stagThreshold;
        if (fitConvTerm != null) {
            fitConvTerm.setConvergenceThreshold(stagThreshold);
        }
    }

    public String getStagThresholdTipText() {
        return "Trigger new aera if the fitness does not change more than this threshold within certain no. iterations.";
    }

    /**
     * @return the stagTime
     */
    public int getStagnationGenerations() {
        return stagTimeArbitrary;
    }

    /**
     * @param stagTime the stagTime to set
     */
    public void setStagnationGenerations(int stagTime) {
        this.stagTimeArbitrary = stagTime;
        if (isStagnationTimeUserDef()) {
            if (fitConvTerm != null) {
                fitConvTerm.setStagnationTime(stagTime);
            }
        }
    }

    public String stagnationGenerationsTipText() {
        return "Set a user defined stagnation time in generations.";
    }

    /**
     * @return the useArbitraryStagTime
     */
    public boolean isStagnationTimeUserDef() {
        return useArbitraryStagTime;
    }

    /**
     * @param useArbitraryStagTime the useArbitraryStagTime to set
     */
    public void setStagnationTimeUserDef(boolean useArbitraryStagTime) {
        this.useArbitraryStagTime = useArbitraryStagTime;
        GenericObjectEditor.setShowProperty(this.getClass(), "stagnationGenerations", useArbitraryStagTime);
        if (fitConvTerm != null) {
            if (useArbitraryStagTime) {
                fitConvTerm.setStagnationTime(getStagnationGenerations());
            } else {
                fitConvTerm.setStagnationTime(calcDefaultStagnationTime());
            }
        }
    }

    public String stagnationTimeUserDefTipText() {
        return "Set or unset the user defined stagnation time.";
    }

    private double getMeanArchivedDist() {
        if (bestList == null) {
            return 0.;
        } else {
            Population tmpPop = new Population(bestList.size());
            tmpPop.addAll(bestList);
            tmpPop.synchSize();
            return tmpPop.getPopulationMeasures()[0];
        }
    }

    /*
     * (non-Javadoc)
     * @see eva2.optimization.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataHeader()
     */
    @Override
    public String[] getAdditionalDataHeader() {
        return new String[]{"numArchived", "archivedMeanDist", "lambda"};
    }

    /*
     * (non-Javadoc)
     * @see eva2.optimization.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataInfo()
     */
    @Override
    public String[] getAdditionalDataInfo() {
        return new String[]{"Number of archived solutions", "Mean distance of archived solutions", "Current population size parameter lambda"};
    }

    /*
     * (non-Javadoc)
     * @see eva2.optimization.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataValue(eva2.optimization.PopulationInterface)
     */
    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        return new Object[]{(bestList == null) ? ((int) 0) : bestList.size(), (getMeanArchivedDist()), getLambda()};
    }
}
