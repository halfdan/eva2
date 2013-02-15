package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.gui.Plot;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operators.distancemetric.EuclideanMetric;
import eva2.optimization.operators.mutation.MutateESCorrVector;
import eva2.optimization.operators.mutation.MutateESFixedStepSize;
import eva2.optimization.operators.selection.InterfaceSelection;
import eva2.optimization.operators.selection.SelectParticleWheel;
import eva2.optimization.populations.InterfaceSolutionSet;
import eva2.optimization.populations.Population;
import eva2.optimization.populations.SolutionSet;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;

/**
 * This is a Particle Filter implemented by Frank Senke, only some documentation
 * here and not completely checked whether this works on arbitrary problem
 * instances. MK did some adaptations, this should work on real valued problems
 * now.
 *
 * This is a implementation of Genetic Algorithms. Copyright: Copyright (c) 2003
 * Company: University of Tuebingen, Computer Architecture
 *
 * @author Felix Streichert
 * @version: $Revision: 307 $ $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec
 * 2007) $ $Author: mkron $
 */
public class ParticleFilterOptimization implements InterfaceOptimizer, java.io.Serializable {

    /**
     * Comment for
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    private Population m_Population = new Population();
    private InterfaceOptimizationProblem m_Problem = new F1Problem();
    private InterfaceSelection m_ParentSelection = new SelectParticleWheel(0.5);
    //private boolean							m_UseElitism		= true;
    private String m_Identifier = "";
    private boolean withShow = false;
    private double mutationSigma = 0.01;
    private double randomImmigrationQuota = 0.05;
    private double initialVelocity = 0.02;
    private double rotationDeg = 20.;
    private int popSize = 300;
    private int sleepTime = 0;
    transient private int indCount = 0;
    transient private InterfacePopulationChangedEventListener m_Listener;
    transient Plot myPlot = null;
    public static final boolean TRACE = false;

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
        m_ParentSelection = new SelectParticleWheel(selScaling);
        if (withShow) {
            setWithShow(true);
        }
    }

    public ParticleFilterOptimization(ParticleFilterOptimization a) {
        this.m_Population = (Population) a.m_Population.clone();
        this.m_Problem = (InterfaceOptimizationProblem) a.m_Problem.clone();
        this.m_Identifier = a.m_Identifier;
        //this.m_Plague                       = a.m_Plague;
        //this.m_NumberOfPartners             = a.m_NumberOfPartners;
        //this.m_UseElitism                   = a.m_UseElitism;
        this.m_ParentSelection = (InterfaceSelection) a.m_ParentSelection.clone();
        if (a.withShow) {
            setWithShow(true);
        }
    }

    public void hideHideable() {
        GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    }

    @Override
    public Object clone() {
        return (Object) new ParticleFilterOptimization(this);
    }

    @Override
    public void init() {
        //System.out.println("popsize is   " + m_Population.size());
        //System.out.println("pops targ is " + m_Population.getPopulationSize());

        if (initialVelocity <= 0.) {
            (((AbstractOptimizationProblem) m_Problem).getIndividualTemplate()).setMutationOperator(new MutateESFixedStepSize(mutationSigma));
        } else {
            (((AbstractOptimizationProblem) m_Problem).getIndividualTemplate()).setMutationOperator(new MutateESCorrVector(mutationSigma, initialVelocity, rotationDeg));
        }
        m_Population.setTargetSize(popSize);
        this.m_Problem.initPopulation(this.m_Population);

        setWithShow(withShow);

        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /**
     * This method will init the optimizer with a given population
     *
     * @param pop The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population) pop.clone();
        if (reset) {
            this.m_Population.init();
            this.evaluatePopulation(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private Population evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
        return population;
    }

    /**
     * This method will resample the given population using EA parent selection.
     *
     */
    protected Population resample(Population pop) {
        Population parents;
        boolean doImmigr = false;

        this.m_ParentSelection.prepareSelection(pop);

        // Generate a Population of Parents with Parantselectionmethod.
        // DONT forget cloning -> selection does only shallow copies!
        int targetSize = this.m_Population.getTargetSize();
        if (randomImmigrationQuota > 0) {
            if (randomImmigrationQuota > 1.) {
                System.err.println("Error, invalid immigration quota!");
            } else {
                targetSize = (int) (this.m_Population.getTargetSize() * (1. - randomImmigrationQuota));
                targetSize = Math.max(1, targetSize); // guarantee at least one to be selected 
                if (targetSize < this.m_Population.getTargetSize()) {
                    doImmigr = true;
                }
            }
        }

        parents = (Population) (this.m_ParentSelection.selectFrom(pop, targetSize)).clone();

        if (doImmigr) {
            // add immigrants
            AbstractEAIndividual immi;
            int i;
            for (i = 0; (i + parents.getTargetSize()) < pop.getTargetSize(); i++) {
                immi = (AbstractEAIndividual) pop.getEAIndividual(0).clone();
                immi.init(getProblem());
                parents.add(immi);
            }
            parents.synchSize();
            if (TRACE) {
                System.out.println("Added " + i + " random individuals");
            }
        }

        parents.SetFunctionCalls(pop.getFunctionCalls());
        parents.setGenerationTo(pop.getGeneration());

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
            applyMotionModel((AbstractEAIndividual) ((AbstractEAIndividual) pop.get(i)), 0.);
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
//    			myPlot.setConnectedPoint(curPosition[0], curPosition[1], graphLabel);
//    			if ( !useCircles && (pop.getEAIndividual(i).hasData(MutateESCorrVector.vectorKey))) {
//    				double[] v=(double[])pop.getEAIndividual(i).getData(MutateESCorrVector.vectorKey);
//    				myPlot.setConnectedPoint(curPosition[0], curPosition[1], graphLabel+5);
//    				curPosition=Mathematics.vvAdd(v, curPosition);
//    				myPlot.setConnectedPoint(curPosition[0], curPosition[1], graphLabel+5);
//    			}
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
     *
     */
    @Override
    public void optimize() {
        Population nextGeneration;
        //AbstractEAIndividual   elite;

        // resample using selection
        nextGeneration = resample(m_Population);

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
        if (TRACE) {
            System.out.println("Speed is  " + BeanInspector.toString(ParticleSwarmOptimization.getPopulationVelSpeed(m_Population, 3, MutateESCorrVector.vectorKey, null, null)) + " popM " + BeanInspector.toString(m_Population.getPopulationMeasures(new EuclideanMetric())));
        }

        m_Population = evaluatePopulation(nextGeneration);

//        collectStatistics(m_Population);

        this.firePropertyChangedEvent(Population.nextGenerationPerformed);

    }

    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (m_Listener == ea) {
            m_Listener = null;
            return true;
        } else {
            return false;
        }
    }

    protected void firePropertyChangedEvent(String name) {
        if (this.m_Listener != null) {
            this.m_Listener.registerPopulationStateChanged(this, name);
        }
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
        if (problem instanceof AbstractOptimizationProblem) {
            ((AbstractOptimizationProblem) problem).informAboutOptimizer(this);
        }
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.m_Problem;
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
        strB.append(this.m_Problem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.m_Population.getStringRepresentation());
        return strB.toString();
    }

    /**
     * This method allows you to set an identifier for the algorithm
     *
     * @param name The indenifier
     */
    @Override
    public void setIdentifier(String name) {
        this.m_Identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.m_Identifier;
    }

    /**
     * ********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a Particle Filter Algorithm.";
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

    /**
     * Assuming that all optimizer will store thier data in a population we will
     * allow acess to this population to query to current state of the
     * optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.m_Population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.m_Population = pop;
    }

    public String populationTipText() {
        return "Edit the properties of the population used.";
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
        this.m_ParentSelection = selection;
    }

    public InterfaceSelection getParentSelection() {
        return this.m_ParentSelection;
    }

    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
    }

    /**
     * @return the withShow
	 *
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
            if ((m_Population != null) && (m_Population.size() > 0)) {
                range = ((InterfaceDataTypeDouble) this.m_Population.get(0)).getDoubleRange();
            }
            if (range != null) {
                myPlot.setCornerPoints(range, 0);
            }
        }
    }

    /**
     * @param withShow the withShow to set
	 *
     */
    public void setWithShow(boolean wShow) {
        this.withShow = wShow;
        if (!withShow) {
            myPlot = null;
        } else {
            double[][] range;
            if ((m_Population != null) && (m_Population.size() > 0)) {
                range = ((InterfaceDataTypeDouble) this.m_Population.get(0)).getDoubleRange();
            } else {
                range = new double[2][];
                range[0] = new double[2];
                range[0][0] = 0;
                range[0][1] = 0;
                range[1] = range[0]; // this is evil
            }
            myPlot = new eva2.gui.Plot("PF", "x1", "x2", range[0], range[1]);
        }
    }

    /**
     * @return the sleepTime
	 *
     */
    public int getSleepTime() {
        return sleepTime;
    }

    /**
     * @param sleepTime the sleepTime to set
	 *
     */
    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * @return the mutationSigma
	 *
     */
    public double getMutationSigma() {
        return mutationSigma;
    }

    /**
     * @param mutationSigma the mutationSigma to set
	 *
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
        m_Population.setTargetSize(popSize);
    }
}
