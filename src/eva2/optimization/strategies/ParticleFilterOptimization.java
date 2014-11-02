package eva2.optimization.strategies;

import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.mutation.MutateESCorrVector;
import eva2.optimization.operator.mutation.MutateESFixedStepSize;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectParticleWheel;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Hidden;

/**
 * This is a Particle Filter implemented by Frank Senke, only some documentation
 * here and not completely checked whether this works on arbitrary problem
 * instances. MK did some adaptations, this should work on real valued problems
 * now.
 */
@Description("This is a Particle Filter Algorithm.")
public class ParticleFilterOptimization extends AbstractOptimizer implements java.io.Serializable {

    private InterfaceSelection parentSelection = new SelectParticleWheel(0.5);
    private boolean withShow = false;
    private double mutationSigma = 0.01;
    private double randomImmigrationQuota = 0.05;
    private double initialVelocity = 0.02;
    private double rotationDeg = 20.;
    private int popSize = 300;
    private int sleepTime = 0;
    transient private int indCount = 0;
    transient Plot myPlot = null;

    public ParticleFilterOptimization() {
        if (withShow) {
            setWithShow(true);
        }
    }

    public ParticleFilterOptimization(double vInit, double mute, double immiQuote, double rotDeg, double selScaling) {
        mutationSigma = mute;
        initialVelocity = vInit;
        randomImmigrationQuota = immiQuote;
        rotationDeg = rotDeg;
        parentSelection = new SelectParticleWheel(selScaling);
        if (withShow) {
            setWithShow(true);
        }
    }

    public ParticleFilterOptimization(ParticleFilterOptimization a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.parentSelection = (InterfaceSelection) a.parentSelection.clone();
        if (a.withShow) {
            setWithShow(true);
        }
    }

    public void hideHideable() {
        GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    }

    @Override
    public Object clone() {
        return new ParticleFilterOptimization(this);
    }

    @Override
    public void initialize() {
        if (initialVelocity <= 0.) {
            (((AbstractOptimizationProblem) optimizationProblem).getIndividualTemplate()).setMutationOperator(new MutateESFixedStepSize(mutationSigma));
        } else {
            (((AbstractOptimizationProblem) optimizationProblem).getIndividualTemplate()).setMutationOperator(new MutateESCorrVector(mutationSigma, initialVelocity, rotationDeg));
        }
        population.setTargetSize(popSize);
        this.optimizationProblem.initializePopulation(this.population);

        setWithShow(withShow);

        this.evaluatePopulation(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.population = (Population) pop.clone();
        if (reset) {
            this.population.initialize();
            this.evaluatePopulation(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private Population evaluatePopulation(Population population) {
        this.optimizationProblem.evaluate(population);
        population.incrGeneration();
        return population;
    }

    /**
     * This method will resample the given population using EA parent selection.
     */
    protected Population resample(Population pop) {
        Population parents;
        boolean doImmigr = false;

        this.parentSelection.prepareSelection(pop);

        // Generate a Population of Parents with Parantselectionmethod.
        // DONT forget cloning -> selection does only shallow copies!
        int targetSize = this.population.getTargetSize();
        if (randomImmigrationQuota > 0) {
            if (randomImmigrationQuota > 1.) {
                System.err.println("Error, invalid immigration quota!");
            } else {
                targetSize = (int) (this.population.getTargetSize() * (1. - randomImmigrationQuota));
                targetSize = Math.max(1, targetSize); // guarantee at least one to be selected 
                if (targetSize < this.population.getTargetSize()) {
                    doImmigr = true;
                }
            }
        }

        parents = (Population) (this.parentSelection.selectFrom(pop, targetSize)).clone();

        if (doImmigr) {
            // add immigrants
            AbstractEAIndividual immi;
            int i;
            for (i = 0; (i + parents.getTargetSize()) < pop.getTargetSize(); i++) {
                immi = (AbstractEAIndividual) pop.getEAIndividual(0).clone();
                immi.initialize(getProblem());
                parents.add(immi);
            }
            parents.synchSize();
        }

        parents.setFunctionCalls(pop.getFunctionCalls());
        parents.setGeneration(pop.getGeneration());

        if (withShow) {
            drawPop(parents, 3, true);
        }
        return parents;
    }

    protected void predict(Population pop) {
        indCount = 0;
        if (withShow) {
            drawPop(pop, 0, false);
        }
        for (int i = 0; i < pop.getTargetSize(); i++) {
            applyMotionModel((AbstractEAIndividual) pop.get(i), 0.);
            indCount++;
        }
        if (withShow) {
            drawPop(pop, 1, false);
        }
    }

    private void drawPop(Population pop, int graphLabel, boolean useCircles) {
        if (myPlot != null) {
            if (graphLabel < 0) {
                graphLabel = indCount;
            }
            for (int i = 0; i < pop.size(); i++) {
                InterfaceDataTypeDouble endy = (InterfaceDataTypeDouble) pop.getEAIndividual(i);
                double[] curPosition = endy.getDoubleData();

                if (useCircles) {
                    myPlot.getFunctionArea().drawCircle("", curPosition, graphLabel);
                } else {
                    myPlot.setUnconnectedPoint(curPosition[0], curPosition[1], graphLabel);
                }
            }
        }
    }

    protected void applyMotionModel(AbstractEAIndividual indy, double noise) {
        // this currently only performs a mutation
        indy.mutate();
        indy.resetFitness(0);
    }

    /**
     * Optimization loop of a resampling particle filter, restructured by MK.
     */
    @Override
    public void optimize() {
        Population nextGeneration;
        //AbstractEAIndividual   elite;

        // resample using selection
        nextGeneration = resample(population);

        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
            }
        }
        if (withShow) {
            clearPlot();
        }

        // predict step
        predict(nextGeneration);

        population = evaluatePopulation(nextGeneration);

        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);

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
        if (problem instanceof AbstractOptimizationProblem) {
            ((AbstractOptimizationProblem) problem).informAboutOptimizer(this);
        }
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        StringBuilder strB = new StringBuilder(200);
        strB.append("Particle Filter:\nOptimization Problem: ");
        strB.append(this.optimizationProblem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.population.getStringRepresentation());
        return strB.toString();
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "PF";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * This method will set the selection method that is to be used
     *
     * @param selection
     */
    public void setParentSelection(InterfaceSelection selection) {
        this.parentSelection = selection;
    }

    public InterfaceSelection getParentSelection() {
        return this.parentSelection;
    }

    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
    }

    /**
     * @return the withShow
     */
    public boolean isWithShow() {
        return withShow;
    }

    public Plot getPlot() {
        return myPlot;
    }

    protected void clearPlot() {
        if (myPlot != null) {
            myPlot.clearAll();
            double[][] range = null;
            if ((population != null) && (population.size() > 0)) {
                range = ((InterfaceDataTypeDouble) this.population.get(0)).getDoubleRange();
            }
            if (range != null) {
                myPlot.setCornerPoints(range, 0);
            }
        }
    }

    /**
     * @param withShow the withShow to set
     */
    public void setWithShow(boolean withShow) {
        this.withShow = withShow;
        if (!this.withShow) {
            myPlot = null;
        } else {
            double[][] range;
            if ((population != null) && (population.size() > 0)) {
                range = ((InterfaceDataTypeDouble) this.population.get(0)).getDoubleRange();
            } else {
                range = new double[2][];
                range[0] = new double[2];
                range[0][0] = 0;
                range[0][1] = 0;
                range[1] = range[0]; // this is evil
            }
            myPlot = new Plot("PF", "x1", "x2", range[0], range[1]);
        }
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

    /**
     * @return the mutationSigma
     */
    public double getMutationSigma() {
        return mutationSigma;
    }

    /**
     * @param mutationSigma the mutationSigma to set
     */
    public void setMutationSigma(double mutationSigma) {
        this.mutationSigma = mutationSigma;
    }

    public String mutationSigmaTipText() {
        return "The (fixed) mutation step for the gaussian motion model";
    }

    public double getRndImmigrQuota() {
        return randomImmigrationQuota;
    }

    public void setRndImmigrQuota(double randomImmigrationQuota) {
        this.randomImmigrationQuota = randomImmigrationQuota;
    }

    public String rndImmigrQuotaTipText() {
        return "The give ratio of the population will be reinitialized randomly in every iteration.";
    }

    public double getInitialVelocity() {
        return initialVelocity;
    }

    public void setInitialVelocity(double initialVelocity) {
        this.initialVelocity = initialVelocity;
    }

    public String initialVelocityTipText() {
        return "If > 0, a linear motion model will be applied, otherwise the gaussian model";
    }

    public double getRotationDeg() {
        return rotationDeg;
    }

    public void setRotationDeg(double rotationDeg) {
        this.rotationDeg = rotationDeg;
    }

    public int getPopSize() {
        return popSize;
    }

    public void setPopSize(int popSize) {
        this.popSize = popSize;
        population.setTargetSize(popSize);
    }
}
