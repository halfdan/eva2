package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceFirstOrderDerivableProblem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.ReflectPackage;
import eva2.util.annotation.Description;

/**
 * A gradient descent algorithm by hannes planatscher don't expect any
 * descriptions here... *big sigh*
 * <p/>
 * mkron added some!
 *
 */
@Description("Gradient Descent can be applied to derivable functions (InterfaceFirstOrderDerivableProblem).")
public class GradientDescentAlgorithm extends AbstractOptimizer implements java.io.Serializable {

    InterfaceDataTypeDouble bestDataTypeDouble, testDataTypeDouble;
    private int iterations = 1;
    private double wDecreaseStepSize = 0.5;
    private double wIncreaseStepSize = 1.1;
    boolean recovery = false;
    private int recoverylocksteps = 5;
    private double recoverythreshold = 100000;
    boolean localStepSizeAdaption = true;
    boolean globalStepSizeAdaption = false;
    private double globalinitstepsize = 1;
    double globalmaxstepsize = 3.0;
    double globalminstepsize = 1e-10;
    boolean manhattan = false;
    double localmaxstepsize = 10;
    double localminstepsize = 1e-10;
    private boolean momentumterm = false;
    public double maximumabsolutechange = 0.2;
    // These variables are necessary for the more complex LectureGUI enviroment
    private static final String lockKey = "gdaLockDataKey";
    private static final String lastFitnessKey = "gdaLastFitDataKey";
    private static final String stepSizeKey = "gdaStepSizeDataKey";
    private static final String wStepSizeKey = "gdaWStepSizeDataKey";
    private static final String gradientKey = "gdaGradientDataKey";
    private static final String changesKey = "gdaChangesDataKey";
    private static final String oldParamsKey = "gdaOldParamsDataKey";

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.setPopulation((Population) pop.clone());
        if (reset) {
            this.getPopulation().initialize();
            this.optimizationProblem.evaluate(this.getPopulation());
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    public GradientDescentAlgorithm() {
        this.population = new Population();
        this.population.setTargetSize(1);
    }

    /**
     * GDA with locally adapted step size.
     *
     * @param minStepSize
     * @param maxStepSize
     * @param maxAbsoluteChange
     */
    public GradientDescentAlgorithm(double minStepSize, double maxStepSize, double maxAbsoluteChange) {
        globalStepSizeAdaption = false;
        localStepSizeAdaption = true;
        localminstepsize = minStepSize;
        globalminstepsize = minStepSize;
        localmaxstepsize = maxStepSize;
        globalmaxstepsize = maxStepSize;
        maximumabsolutechange = maxAbsoluteChange;
    }

    @Override
    public Object clone() {
        /**
         * @todo Implement InterfaceOptimizer method
         */
        throw new java.lang.UnsupportedOperationException("Method clone() not yet implemented.");
    }

    @Override
    public String getName() {
        return "GradientDescentAlgorithm";
    }

    @Override
    public void initialize() {
        //System.out.println("initialize() called ");
//    indyhash = new Hashtable();
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
    }

    public double signum(double val) {
        return (val < 0) ? -1 : 1;
    }

    @Override
    public void optimize() {
        //  System.out.println("opt. called");
        AbstractEAIndividual indy;
//      if ((this.indyhash == null) || (this.indyhash.size() <1)) initialize();

        for (int i = 0; i < this.population.size(); i++) {
            indy = ((AbstractEAIndividual) this.population.get(i));
            if (!indy.hasData(gradientKey)) {
                //System.out.println("new indy to hash");
//        Hashtable history = new Hashtable();
                int[] lock = new int[((InterfaceDataTypeDouble) indy).getDoubleData().length];
                double[] wstepsize = new double[((InterfaceDataTypeDouble) indy).getDoubleData().length];
                for (int li = 0; li < lock.length; li++) {
                    lock[li] = 0;
                }
                for (int li = 0; li < lock.length; li++) {
                    wstepsize[li] = 1.0;
                }
                double fitness = 0;
                indy.putData(lockKey, lock);
                indy.putData(lastFitnessKey, new Double(fitness));
                indy.putData(stepSizeKey, new Double(globalinitstepsize));
                indy.putData(wStepSizeKey, wstepsize);
//        indyhash.put(indy, history);
            } else {
                //System.out.println("indy already in hash");
            }
        }
        // System.out.println("hashtable built");
        for (int i = 0; i < this.population.size(); i++) {

            indy = ((AbstractEAIndividual) this.population.get(i));
            double[][] range = ((InterfaceDataTypeDouble) indy).getDoubleRange();
            double[] params = ((InterfaceDataTypeDouble) indy).getDoubleData();
            indy.putData(oldParamsKey, params);

            int[] lock = (int[]) indy.getData(lockKey);
            double indystepsize = ((Double) indy.getData(stepSizeKey)).doubleValue();

            if ((this.optimizationProblem instanceof InterfaceFirstOrderDerivableProblem) && (indy instanceof InterfaceDataTypeDouble)) {
                for (int iterations = 0; iterations < this.iterations; iterations++) {

                    double[] oldgradient = indy.hasData(gradientKey) ? (double[]) indy.getData(gradientKey) : null;
                    double[] wstepsize = (double[]) indy.getData(wStepSizeKey);
                    double[] oldchange = null;

                    double[] gradient = ((InterfaceFirstOrderDerivableProblem) optimizationProblem).getFirstOrderGradients(params);
                    if ((oldgradient != null) && (wstepsize != null)) { // LOCAL adaption
                        for (int li = 0; li < wstepsize.length; li++) {
                            double prod = gradient[li] * oldgradient[li];
                            if (prod < 0) {
                                wstepsize[li] = wDecreaseStepSize * wstepsize[li];
                            } else if (prod > 0) {
                                wstepsize[li] = wIncreaseStepSize * wstepsize[li];
                            }
                            wstepsize[li] = (wstepsize[li] < localminstepsize) ? localminstepsize : wstepsize[li];
                            wstepsize[li] = (wstepsize[li] > localmaxstepsize) ? localmaxstepsize : wstepsize[li];

                        }

                    }
                    double[] newparams = new double[params.length];
                    indy.putData(gradientKey, gradient);
                    double[] change = new double[params.length];
                    if (indy.hasData(changesKey)) {
                        oldchange = (double[]) indy.getData(changesKey);
                    }
                    boolean dograddesc = (this.momentumterm) && (oldchange != null);

                    for (int j = 0; j < newparams.length; j++) {
                        if (lock[j] == 0) {
                            double tempstepsize = 1;
                            if (this.localStepSizeAdaption) {
                                tempstepsize *= wstepsize[j];
                            }
                            if (this.globalStepSizeAdaption) {
                                tempstepsize *= indystepsize;
                            }
                            double wchange = signum(tempstepsize * gradient[j]) * Math.min(maximumabsolutechange, Math.abs(tempstepsize * gradient[j])); //indystepsize * gradient[j];
                            if (this.manhattan) {
                                wchange = this.signum(wchange) * tempstepsize;
                            }
                            if (dograddesc) {
                                wchange += this.momentumweigth * oldchange[j];
                            }
                            newparams[j] = params[j] - wchange;
                            if (newparams[j] < range[j][0]) {
                                newparams[j] = range[j][0];
                            }
                            if (newparams[j] > range[j][1]) {
                                newparams[j] = range[j][1];
                            }
//              for (int g = 0; g < newparams.length; g++) {
//                System.out.println("Param " + g +": " + newparams[g]);
//              }
                            change[j] += wchange;
                        } else {
                            lock[j]--;
                        }
                    }
                    params = newparams;

                    indy.putData(changesKey, change);

                } // end loop iterations

                ((InterfaceDataTypeDouble) indy).setDoubleGenotype(params);

            } // end if ((this.problem instanceof InterfaceFirstOrderDerivableProblem) && (indy instanceof InterfaceDataTypeDouble)) {
            else {
                String msg = "Warning, problem of type InterfaceFirstOrderDerivableProblem and template of type InterfaceDataTypeDouble is required for " + this.getClass();
                EVAERROR.errorMsgOnce(msg);
                Class<?>[] clsArr = ReflectPackage.getAssignableClasses(InterfaceFirstOrderDerivableProblem.class.getName(), true, true);
                msg += " (available: ";
                for (Class<?> cls : clsArr) {
                    msg = msg + " " + cls.getSimpleName();
                }
                msg += ")";
                throw new RuntimeException(msg);
            }
        } // for loop population size

        this.optimizationProblem.evaluate(this.population);
        population.incrGeneration();

        if (this.recovery) {
            for (int i = 0; i < this.population.size(); i++) {
                indy = ((AbstractEAIndividual) this.population.get(i));
                if (indy.getFitness()[0] > recoverythreshold) {

                    ((InterfaceDataTypeDouble) indy).setDoublePhenotype((double[]) indy.getData(oldParamsKey));
                    double[] changes = (double[]) indy.getData(changesKey);
                    int[] lock = (int[]) indy.getData(lockKey);

                    int indexmaxchange = 0;
                    double maxchangeval = Double.NEGATIVE_INFINITY;
                    for (int j = 0; j < changes.length; j++) {
                        if ((changes[j] > maxchangeval) && (lock[j] == 0)) {
                            indexmaxchange = j;
                            maxchangeval = changes[j];
                        }
                    }
                    lock[indexmaxchange] = recoverylocksteps;
                    indy.putData(lockKey, lock);
                } else {
                }
            }
            this.optimizationProblem.evaluate(this.population);
            population.incrGeneration();
        }

        if (this.globalStepSizeAdaption) {

            //System.out.println("gsa main");
            for (int i = 0; i < this.population.size(); i++) {
                indy = ((AbstractEAIndividual) this.population.get(i));

                if (indy.getData(lastFitnessKey) != null) {
                    double lastfit = ((Double) indy.getData(lastFitnessKey)).doubleValue();
                    double indystepsize = ((Double) indy.getData(stepSizeKey)).doubleValue();

                    if (lastfit < indy.getFitness()[0]) { // GLOBAL adaption
                        indystepsize *= wDecreaseStepSize;
                    } else {
                        indystepsize *= wIncreaseStepSize;
                    }
                    indystepsize = (indystepsize > globalmaxstepsize) ? globalmaxstepsize : indystepsize;
                    indystepsize = (indystepsize < globalminstepsize) ? globalminstepsize : indystepsize;
                    indy.putData(stepSizeKey, indystepsize);
                }

                indy.putData(lastFitnessKey, indy.getFitness()[0]);
            }

        }


        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    private double momentumweigth = 0.1;

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    @Override
    public String getStringRepresentation() {
        return "GradientDescentAlgorithm";
    }

    public static void main(String[] args) {
        GradientDescentAlgorithm program = new GradientDescentAlgorithm();
        InterfaceOptimizationProblem problem = new F1Problem();
        program.setProblem(problem);
        program.initialize();
        for (int i = 0; i < 100; i++) {
            program.optimize();
            System.out.println(program.getPopulation().getBestFitness()[0]);
        }
        double[] res = ((InterfaceDataTypeDouble) program.getPopulation().getBestIndividual()).getDoubleData();
        for (int i = 0; i < res.length; i++) {
            System.out.print(res[i] + " ");
        }
    }

//////////////// for global adaption

    public boolean isAdaptStepSizeGlobally() {
        return globalStepSizeAdaption;
    }

    public void setAdaptStepSizeGlobally(boolean globalstepsizeadaption) {
        this.globalStepSizeAdaption = globalstepsizeadaption;
        if (globalstepsizeadaption && localStepSizeAdaption) {
            setAdaptStepSizeLocally(false);
        }
    }

    public String adaptStepSizeGloballyTipText() {
        return "Use a single step size per individual - (priority over local step size).";
    }

    public double getGlobalMaxStepSize() {
        return globalmaxstepsize;
    }

    public void setGlobalMaxStepSize(double p) {
        globalmaxstepsize = p;
    }

    public String globalMaxStepSizeTipText() {
        return "Maximum step size for global adaption.";
    }

    public double getGlobalMinStepSize() {
        return globalminstepsize;
    }

    public void setGlobalMinStepSize(double p) {
        globalminstepsize = p;
    }

    public String globalMindStepSizeTipText() {
        return "Minimum step size for global adaption.";
    }

    public double getGlobalInitStepSize() {
        return globalinitstepsize;
    }

    public void setGlobalInitStepSize(double initstepsize) {
        this.globalinitstepsize = initstepsize;
    }

    public String globalInitStepSizeTipText() {
        return "Initial step size for global adaption.";
    }

    //////////////// for local adaption
    public boolean isAdaptStepSizeLocally() {
        return localStepSizeAdaption;
    }

    public void setAdaptStepSizeLocally(boolean stepsizeadaption) {
        this.localStepSizeAdaption = stepsizeadaption;
        if (globalStepSizeAdaption && localStepSizeAdaption) {
            setAdaptStepSizeGlobally(false);
        }
    }

    public String adaptStepSizeLocallyTipText() {
        return "Use a step size parameter in any dimension.";
    }

    public double getLocalMinStepSize() {
        return localminstepsize;
    }

    public void setLocalMinStepSize(double localminstepsize) {
        this.localminstepsize = localminstepsize;
    }

    public double getLocalMaxStepSize() {
        return localmaxstepsize;
    }

    public void setLocalMaxStepSize(double localmaxstepsize) {
        this.localmaxstepsize = localmaxstepsize;
    }

    public void setStepSizeIncreaseFact(double nplus) {
        this.wIncreaseStepSize = nplus;
    }

    public double getStepSizeIncreaseFact() {
        return wIncreaseStepSize;
    }

    public String stepSizeIncreaseFactTipText() {
        return "Factor for increasing the step size in adaption.";
    }

    public void setStepSizeDecreaseFact(double nminus) {
        this.wDecreaseStepSize = nminus;
    }

    public double getStepSizeDecreaseFact() {
        return wDecreaseStepSize;
    }

    public String stepSizeDecreaseFactTipText() {
        return "Factor for decreasing the step size in adaption.";
    }

    //////////////// concerning recovery
    public boolean isRecovery() {
        return recovery;
    }

    public void setRecovery(boolean recovery) {
        this.recovery = recovery;
    }

    public int getRecoveryLocksteps() {
        return recoverylocksteps;
    }

    public void setRecoveryLocksteps(int locksteps) {
        this.recoverylocksteps = locksteps;
    }

    public double getRecoveryThreshold() {
        return recoverythreshold;
    }

    public void setRecoveryThreshold(double recoverythreshold) {
        this.recoverythreshold = recoverythreshold;
    }

    public String recoveryThresholdTipText() {
        return "If the fitness exceeds this threshold, an unstable area is assumed and one step recovered.";
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String iterationsTipText() {
        return "The number of GD-iterations per generation.";
    }

    public boolean isManhattan() {
        return manhattan;
    }

    public void setManhattan(boolean manhattan) {
        this.manhattan = manhattan;
    }

    public boolean isMomentumTerm() {
        return momentumterm;
    }

    public void setMomentumTerm(boolean momentum) {
        this.momentumterm = momentum;
    }

    public double getMomentumWeigth() {
        return momentumweigth;
    }

    public void setMomentumWeigth(double momentumweigth) {
        this.momentumweigth = momentumweigth;
    }

    public double getMaximumAbsoluteChange() {
        return maximumabsolutechange;
    }

    public void setMaximumAbsoluteChange(double maximumabsolutechange) {
        this.maximumabsolutechange = maximumabsolutechange;
    }

    public String maximumAbsoluteChangeTipText() {
        return "The maximum change along a coordinate in one step.";
    }
}
