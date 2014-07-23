package eva2.problems;

import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;


/**
 * An abstract class for dynamic optimization problems. Main parameters are frequency and severity of
 * changes in the problem environment (the target function). The latter may be measured in absolute
 * function evaluations or relative to the population size. The necessary transmutations are to be
 * implemented,  however.
 */
public abstract class AbstractDynamicOptimizationProblem extends AbstractOptimizationProblem implements java.io.Serializable {

    /**
     * Default start time of the optimization process
     */
    protected double startTime;
    /**
     * Current simulation time during optimization
     */
    protected double currentProblemTime;

    /**
     * A severity measure of the dynamics
     */
    protected double severity;
    /**
     * A frequency measure of the dynamics
     */
    protected double frequency;
    /**
     * Switch indicating relative (to population size) or absolute (in evaluations) frequency
     */
    protected boolean frequencyRelative;
    /**
     *
     */
    private AbstractEAIndividual idealInd;

    /**
     * Switch controling extra problem visualization
     */
    private boolean bExtraPlot;
    /**
     * the plot instance for problem data visualization
     */
    transient protected Plot myplot = null;

    /**
     * A constructor.
     */
    public AbstractDynamicOptimizationProblem() {
        //System.out.println("AbstractDynamicOptimizationProblem()");
        bExtraPlot = false;
        myplot = null;
        idealInd = null;
//		initialize(0, 1., 0.1);
    }

    /**
     * This method inits the Problem to log multiruns at the given timestamp.
     *
     * @param startT start time stamp
     * @param sev    initial severity
     * @param freq   initial frequency
     */
    public void initialize(double startT, double sev, double freq) {
        setCurrentProblemTime(startT);
        setSeverity(sev);
        setFrequency(freq);

        if (bExtraPlot) {
            makePlot();
        }
    }

    @Override
    public void initializeProblem() {
        setCurrentProblemTime(getStartTime());
        if (myplot != null) {
            try {
                myplot.jump();
            } catch (NullPointerException e) {
                makePlot();
            }
        }
    }

    /**
     * Whenever the environment (or the time, primarily) has changed, some problem
     * properties (like stored individual fitness) may require updating.
     *
     * @param severity the severity of the change (time measure)
     */
    public abstract void resetProblem(double severity);

    /**
     * This method inits a given population at the current time stamp.
     *
     * @param population The populations that is to be inited
     */
    @Override
    public void initializePopulation(Population population) {
        //initializeProblem();	// this shouldnt be necessary
        this.initPopulationAt(population, getCurrentProblemTime());
    }

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be inited
     * @param time       current time stamp to be used
     */
    public abstract void initPopulationAt(Population population, double time);

    /**
     * @return the startTime
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * Sets the time stamp.
     *
     * @param newTime new time stamp for the evaluation
     */
    protected void setCurrentProblemTime(double newTime) {
        double sev = newTime - currentProblemTime;
        currentProblemTime = newTime;
        if (sev != 0.) {
            resetProblem(sev);
        }
    }

    /**
     * Return the problems current time stamp.
     *
     * @return the current time stamp
     */
    protected double getCurrentProblemTime() {
        return currentProblemTime;
    }

    public String problemTimeTipText() {
        return "simulated starting time for the run";
    }

    /**
     * @param sev the desired severity of a dynamic change
     */
    public void setSeverity(double sev) {
        severity = sev;
    }

    /**
     * @return the severity of a dynamic alteration
     */
    public double getSeverity() {
        return severity;
    }

    /**
     * The frequency of changes of the problem environment.
     * May be measured in absolute evaluations or multiples of the population size.
     *
     * @return the frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /**
     * @return true if the frequency value is meant to be relative to the population size
     */
    public boolean isFrequencyRelative() {
        return frequencyRelative;
    }

    public String frequencyTipText() {
        return "The frequency of changes in the target function, should be <= 1";
    }

    /**
     * @param frequencyRelative set true if the frequency value is meant to be relative to the population size
     */
    public void setFrequencyRelative(boolean frequencyRelative) {
        this.frequencyRelative = frequencyRelative;
    }

    public String frequencyRelativeTipText() {
        return "If marked, frequency is interpreted relative to population size, else it refers to absolute evaluations.";
    }

    /**
     * Increases the internal time stamp by the given value.
     *
     * @param dt time interval
     */
    protected void incProblemTime(double dt) {
        currentProblemTime += dt;
        resetProblem(dt);
    }

    /**
     * If there is a known global optimum, return an individual representing it, otherwise return false.
     *
     * @return the optimum represented in an individual, or null if the optimum is currently unknown
     */
    public abstract AbstractEAIndividual getCurrentOptimum();

    /******************** The most important methods ****************************************/

    /**
     * This method evaluates a single individual and sets the fitness value at default time stamp 0.
     *
     * @param individual The individual that is to be evalutated
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        if (problemToChangeAt(getCurrentProblemTime())) {
            changeProblemAt(getCurrentProblemTime());
        }
        this.evaluateAt(individual, getCurrentProblemTime());
        countEvaluation();
    }

    /**
     * Called every time an individual evaluation is performed.
     */
    protected abstract void countEvaluation();

    /**
     * Called to indicate a change in the problem environment. To be implemented.
     *
     * @param problemTime
     */
    protected abstract void changeProblemAt(double problemTime);

    /**
     * Implement an indicator for when changes in the problem environment are necessary. If it returns
     * <code>true</code>, the <code>changeProblemAt(double problemTime)</code> will be called.
     *
     * @param problemTime the current simulation time of the problem
     * @return true, if the problem is to change at the given time, else false
     */
    protected abstract boolean problemToChangeAt(double problemTime);

    /**
     * This method evaluates a single individual and sets the fitness values without changing the environment (in "no time").
     *
     * @param individual The individual that is to be evalutated
     * @param t          time stamp of the evaluation call
     */
    protected abstract void evaluateAt(AbstractEAIndividual individual, double t);

    /**
     * This method evaluate a single individual at the current problem time and sets the fitness values without changing the environment (in "no time").
     *
     * @param individual The individual that is to be evalutated
     */
    public void evaluateInstantly(AbstractEAIndividual individual) {
        evaluateAt(individual, getCurrentProblemTime());
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * Override population evaluation to do some data output.
     *
     * @param population the population whi
     */
    @Override
    public void evaluatePopulationEnd(Population population) {
        //System.out.println(">> mean distance at " + population.getFunctionCalls() + " / " + getProblemTime() + " is " + population.getMeanDistance());
        //System.out.println("> current best is " + population.getBestFitness()[0]);
        //double[] meanMinMax = population.getPopulationMeasures();

        //myplot.setConnectedPoint(population.getFunctionCalls(), population.getPopulationMeasures()[0], 0);

        AbstractEAIndividual popBest = (AbstractEAIndividual) (population.getBestIndividual());
        if (idealInd == null) {
            idealInd = (AbstractEAIndividual) popBest.clone();
        }

//		getCurrentOptimum((InterfaceDataTypeDouble)idealInd);

//		double d = new PhenotypeMetric().distance(popBest, idealInd);
//		double d = distanceBetween(popBest, idealInd);

        //System.out.println("tracking dist is " + d);

        //if (myplot!=null) myplot.setConnectedPoint(population.getFunctionCalls(), d, 0);
        //myplot.setUnconnectedPoint(population.getFunctionCalls(), population.getPopulationMeasures()[2], 2);
    }

    protected void setExtraPlot(boolean doPlot) {
        if (bExtraPlot && !doPlot) {
            myplot = null;
        } else if (!bExtraPlot && doPlot) {
            if (myplot != null) {
                myplot.jump();
            } else {
                makePlot();
            }
        }
        bExtraPlot = doPlot;
    }

    protected boolean isExtraPlot() {
        return bExtraPlot;
    }

    private void makePlot() {
        double[] tmpD = new double[2];
        tmpD[0] = 0;
        tmpD[1] = 0;
        // im not really certain about what tmpD is required for
        this.myplot = new Plot("population measures", "x1", "x2", tmpD, tmpD);
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "AbstractDynamicOptimizationProblem";
    }
}
