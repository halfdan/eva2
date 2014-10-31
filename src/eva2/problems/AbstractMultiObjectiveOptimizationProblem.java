package eva2.problems;

import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.operator.archiving.ArchivingNSGA;
import eva2.optimization.operator.moso.InterfaceMOSOConverter;
import eva2.optimization.operator.moso.MOSONoConvert;
import eva2.optimization.operator.paretofrontmetrics.InterfaceParetoFrontMetric;
import eva2.optimization.operator.paretofrontmetrics.MetricS;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.ToolBox;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DPoint;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 *
 */
public abstract class AbstractMultiObjectiveOptimizationProblem extends AbstractOptimizationProblem {


    /**
     * Generated serial version identifier
     */
    private static final long serialVersionUID = -6882081673229946521L;

    /**
     * @author mkron
     */
    class MultiObjectiveEvalThread extends Thread {
        AbstractMultiObjectiveOptimizationProblem prob;
        AbstractEAIndividual ind;
        Vector<AbstractEAIndividual> resultrep;
        Population pop;
        Semaphore semaphore = null;

        public MultiObjectiveEvalThread(AbstractMultiObjectiveOptimizationProblem prob, AbstractEAIndividual ind, Vector<AbstractEAIndividual> resultrep, Population pop, Semaphore sema) {
            this.ind = ind;
            this.prob = prob;
            this.resultrep = resultrep;
            this.pop = pop;
            this.semaphore = sema;
        }

        @Override
        public void run() {
            double[] fitness;
            prob.evaluate(ind);
            resultrep.add(ind);


            fitness = ind.getFitness();
            // check and update border if necessary
            if (border == null) {
                prob.border = new double[fitness.length][2];
            } else if (fitness.length != prob.border.length) {
                prob.border = new double[fitness.length][2];
            }
            for (int j = 0; j < fitness.length; j++) {
                prob.border[j][0] = Math.min(prob.border[j][0], fitness[j]);
                prob.border[j][1] = Math.max(prob.border[j][1], fitness[j]);
            }
            pop.incrFunctionCalls();
            semaphore.release();

        }
    }

    protected InterfaceMOSOConverter mosoConverter = new MOSONoConvert();
    protected InterfaceParetoFrontMetric metric = new MetricS();
    transient protected Population paretoFront = new Population();
    public ArrayList areaConst4Parallelization = new ArrayList();
    protected int outputDimension = 2;
    double defaultBorderLow = 0;
    double defaultBorderHigh = 5;

    transient protected double[][] border;
    transient protected Plot plot;
    transient protected JFrame resultFrame;
    protected transient boolean show = false;

    public AbstractMultiObjectiveOptimizationProblem(double borderHigh) {
        super();
        defaultBorderHigh = borderHigh;
        this.template = new ESIndividualDoubleData();
        makeBorder();
        if (this.show) {
            this.initializeProblemFrame();
        }
    }

    public AbstractMultiObjectiveOptimizationProblem() {
        this(5.);
    }

    /**
     * This method allows you to toggle pareto-front visualisation on and off.
     *
     * @param b True if the pareto-front is to be shown.
     */
    public void setShowParetoFront(boolean b) {
        this.show = b;
        if (this.show) {
            this.initializeProblemFrame();
        } else if (this.plot != null) {
            this.plot.dispose();
            this.plot = null;
        }
    }

    public boolean isShowParetoFront() {
        return this.show;
    }

    public String showParetoFrontTipText() {
        return "Toggles the pareto-front visualisation.";
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public abstract Object clone();

    /**
     * This method inits the Problem to log multiruns for the s-Metric it
     * is necessary to give the border to get reliable results.
     * also it is necessary to initialize the local Pareto-Front and the
     * problem frame (i'll provide a default implementation here.
     */
    @Override
    public void initializeProblem() {
        makeBorder();
        this.paretoFront = new Population();
        if (this.show) {
            this.initializeProblemFrame();
        }
    }

    protected void makeBorder() {
        if (this.border == null) {
            this.border = new double[outputDimension][2];
        }
        for (int i = 0; i < this.border.length; i++) {
            this.border[i][0] = getLowerBorder(i);
            this.border[i][1] = getUpperBorder(i);
        }
    }

    protected double getUpperBorder(int i) {
        return defaultBorderHigh;
    }

    protected double getLowerBorder(int i) {
        return defaultBorderLow;
    }

    /**
     * This method checks whether the problem has truely evaluated
     * to a multiobjective problem
     *
     * @return true if all individuals are multiobjective
     */
    public static boolean isPopulationMultiObjective(Population pop) {
        if (pop == null) {
            return false;
        }
        if (pop.size() == 0) {
            return false;
        }
        int bestFitLen = pop.getBestFitness().length, tmpFitLen;
        for (int i = 0; i < pop.size(); i++) {
            tmpFitLen = ((AbstractEAIndividual) pop.get(i)).getFitness().length;
            if (tmpFitLen <= 1) {
                return false;
            }
            if (tmpFitLen != bestFitLen) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method will reset the local Pareto-Front
     * This caused a lot of trouble for the DeNovo Approach of MOCCO
     */
    public void resetParetoFront() {
        this.paretoFront = new Population();
    }

    @Override
    public void evaluatePopulationStart(Population population) {
        super.evaluatePopulationStart(population);
        if (this.show && (this.plot == null)) {
            this.initializeProblemFrame();
        }
    }

    @Override
    public void evaluatePopulationEnd(Population population) {
        super.evaluatePopulationEnd(population);
        double[] fitness;

        for (int i = 0; i < population.size(); i++) {
            // check and update border if necessary
            AbstractEAIndividual tmpIndy = (AbstractEAIndividual) population.get(i);
            fitness = tmpIndy.getFitness();
            // check and update border if necessary
            if (border == null) {
                this.border = new double[fitness.length][2];
            } else if (fitness.length != this.border.length) {
                this.border = new double[fitness.length][2];
            }
            for (int j = 0; j < fitness.length; j++) {
                this.border[j][0] = Math.min(this.border[j][0], fitness[j]);
                this.border[j][1] = Math.max(this.border[j][1], fitness[j]);
            }
        }

        // So what is the problem:
        // on the one hand i want to log the pareto-front in the
        // multiobjective case
        // but on the other hand i also need to log the pareto-front
        // in the single objective case if a MOSOConverter is used,
        // here i want to log all found pareto-optimal solutions, which
        // could be pretty many

        // currently the problem should be multi-criteria
        logPopToParetoFront(paretoFront, population);

        // Sometimes you want to transform a multiobjective optimization problem
        // into a single objective one, this way single objective optimization
        // algorithms can be applied more easily
        this.mosoConverter.convertMultiObjective2SingleObjective(population);

        if (this.show) {
            if (plot.isValid()) {
                AbstractMultiObjectiveOptimizationProblem.drawProblem(population, plot, this);
            }
        }
    }

    /**
     * Unite the given population with the given pareto front and replace pFront with the new pareto front.
     * This variant uses ArchivingNSGA
     *
     * @param pFront
     * @param pop
     */
    public static void logPopToParetoFront(Population pFront, Population pop) {
        // log the pareto-front
        if (AbstractMultiObjectiveOptimizationProblem.isPopulationMultiObjective(pop)) {
            if (pFront == null) {
                System.err.println("Error, give at least an empty population as initial pareto front");
            }
            if (pFront.getArchive() == null) {
                pFront.SetArchive(new Population(100));
            }
            Population tmpPop = new Population(pop.size());
            tmpPop.addPopulation(pop);
            tmpPop.addPopulation(pFront);
            ArchivingNSGA archiving = new ArchivingNSGA();
            archiving.addElementsToArchive(tmpPop);
            pFront.clear();
            pFront.addPopulation(tmpPop.getArchive());
        }
    }

    /**
     * This method will initialize the problem specific visualisation of the problem
     */
    public void initializeProblemFrame() {
        double[] tmpD = new double[2];
        tmpD[0] = 0;
        tmpD[1] = 0;
        if (this.plot == null) {
            plot = new Plot("Multiobjective Optimization", "Y1", "Y2", tmpD, tmpD);
        }

        // plot initialize stuff
        this.initAdditionalData(this.plot, 10);
    }

    /**
     * This method will plot a reference solutions or something like it
     *
     * @param plot  The plot where you can draw your stuff.
     * @param index The first index where you can draw your stuff
     */
    public void initAdditionalData(Plot plot, int index) {
        // for example plot the current population
        plot.clearGraph(index);
        plot.setUnconnectedPoint(0, 0, index);
    }

    /**
     * This method will draw the current state of the optimization process
     *
     * @param pFront The current population
     */
    public static void drawProblem(Population pFront, Population archive, Plot plot) {
        ArchivingAllDominating tmpArch = new ArchivingAllDominating();

        // i want to plot the pareto front for MOEA and other strategies
        // but i have to differentiate between the case where
        // there is a true MOEA at work and where the
        // MOOpt was converted into a SOOpt
        if (pFront != null && (plot != null)) {
            // i got either a multiobjective population or a multiobjective local population
            plot.clearAll();
            tmpArch.plotParetoFront(pFront, plot);
            if (archive != null) {
                GraphPointSet mySet = new GraphPointSet(10, plot.getFunctionArea());
                DPoint myPoint;
                Chart2DDPointIconCircle icon;
                double[] tmpD;
                mySet.setConnectedMode(false);
                for (int i = 0; i < archive.size(); i++) {
                    icon = new Chart2DDPointIconCircle();
                    tmpD = ((AbstractEAIndividual) archive.get(i)).getFitness();
                    if (tmpD.length < 2) {
                        throw new RuntimeException("Error, problem seems not to be multi-objective, pareto front plot not possible!");
                    }
                    myPoint = new DPoint(tmpD[0], tmpD[1]);
                    if (((AbstractEAIndividual) archive.get(i)).getConstraintViolation() > 0) {
                        icon.setBorderColor(Color.RED);
                        icon.setFillColor(Color.RED);
                    } else {
                        icon.setBorderColor(Color.BLACK);
                        icon.setFillColor(Color.BLACK);
                    }
                    myPoint.setIcon(icon);
                    mySet.addDPoint(myPoint);
                }
            }
        }
    }

    /**
     * This method will draw the current state of the optimization process
     *
     * @param p The current population
     */
    public static void drawProblem(Population p, Plot plot, AbstractMultiObjectiveOptimizationProblem moProblem) {
        ArchivingAllDominating tmpArch = new ArchivingAllDominating();
        Population tmpPop = null;

        if (p.getGeneration() > 2) {
            // i want to plot the pareto front for MOEA and other strategies
            // but i have to differentiate between the case where
            // there is a true MOEA at work and where the
            // MOOpt was converted into a SOOpt
            if (AbstractMultiObjectiveOptimizationProblem.isPopulationMultiObjective(p)) {
                // in this case i have to use my local archive
                tmpPop = moProblem.paretoFront;
            } else {
                // in this case i use the population of the optimizer
                // and eventually the population.archive if there is one
                tmpPop = new Population();
                tmpPop.addPopulation(p);
                if (p.getArchive() != null) {
                    tmpPop.addPopulation(p.getArchive());
                }
                tmpArch.addElementsToArchive(tmpPop);
                tmpPop = tmpPop.getArchive();
            }

            if (tmpPop != null) {
                drawProblem(tmpPop, p.getArchive(), plot);
            }
            // else : in this case i got a single objective optimization problem
            // draw additional data
            moProblem.drawAdditionalData(plot, p, 10);
        }
    }

    /**
     * This method will plot a reference solutions or something like it
     *
     * @param plot  The plot where you can draw your stuff.
     * @param index The first index where you can draw your stuff
     */
    public void drawAdditionalData(Plot plot, Population pop, int index) {
        double[] tmpFitness;
        // for example plot the current population
        plot.clearGraph(index);
        for (int i = 0; i < pop.size(); i++) {
            tmpFitness = ((AbstractEAIndividual) pop.get(i)).getFitness();
            if (tmpFitness.length <= 1) {
                tmpFitness = (double[]) ((AbstractEAIndividual) pop.get(i)).getData("MOFitness");
            }
            plot.setUnconnectedPoint(tmpFitness[0], tmpFitness[1], index);
        }
        plot.setUnconnectedPoint(this.border[0][1], this.border[1][1], index);
        plot.setUnconnectedPoint(this.border[0][0], this.border[1][0], index);
    }

    /**
     * Plot the given population to a MO-plot with red dots for constraint violations and blue otherwise.
     * The given border is used to set plot limits but may be null.
     *
     * @param plot
     * @param pop
     * @param border
     * @param index
     */
    public static void drawWithConstraints(Plot plot, Population pop, double[][] border, int index) {
        // for example plot the current population
        double[][] trueFitness, moFitness;
        double[] constraint;

        GraphPointSet mySet = new GraphPointSet(index, plot.getFunctionArea());
        DPoint myPoint;
        double tmp1;

        trueFitness = new double[pop.size()][];
        constraint = new double[pop.size()];
        if (((AbstractEAIndividual) pop.get(0)).hasData("MOFitness")) {
            moFitness = new double[pop.size()][];
        } else {
            moFitness = null;
        }
        for (int i = 0; i < pop.size(); i++) {
            constraint[i] = ((AbstractEAIndividual) pop.get(i)).getConstraintViolation();
            trueFitness[i] = ((AbstractEAIndividual) pop.get(i)).getFitness();
            if (moFitness != null) {
                moFitness[i] = (double[]) ((AbstractEAIndividual) pop.get(i)).getData("MOFitness");
            }
        }
        mySet.setConnectedMode(false);
        for (int i = 0; i < trueFitness.length; i++) {
            if (moFitness != null) {
                // moso is active
                if (checkValidAt(moFitness, i)) {
                    myPoint = new DPoint(moFitness[i][0], moFitness[i][1]);
                    tmp1 = Math.round(trueFitness[i][0] * 100) / 100.0;
                    addPoint(constraint, mySet, myPoint, i, "" + tmp1);
                }
            } else {
                // no moso is active
                if (checkValidAt(trueFitness, i)) {
                    myPoint = new DPoint(trueFitness[i][0], trueFitness[i][1]);
                    addPoint(constraint, mySet, myPoint, i, "");
                }
            }
        }
        if (border != null) {
            plot.setUnconnectedPoint(border[0][1], border[1][1], index + 1);
            plot.setUnconnectedPoint(border[0][0], border[1][0], index + 1);
        }
    }

    /**
     * Check whether the given fitness array contains valid double values at position i.
     *
     * @param fitArray
     * @param i
     * @return
     */
    private static boolean checkValidAt(double[][] fitArray, int i) {
        return !(new Double(fitArray[i][0]).isNaN()) && !(new Double(fitArray[i][1]).isNaN()) &&
                !(new Double(fitArray[i][0]).isInfinite()) && !(new Double(fitArray[i][1]).isInfinite());
    }

    private static void addPoint(double[] constraint, GraphPointSet mySet,
                                 DPoint myPoint, int i, String text) {
        Chart2DDPointIconCircle icon;
        Chart2DDPointIconText tmp = new Chart2DDPointIconText(text);
        icon = new Chart2DDPointIconCircle();
        if (constraint[i] > 0) {
            icon.setBorderColor(Color.RED);
            icon.setFillColor(Color.RED);
        } else {
            icon.setBorderColor(Color.BLUE);
            icon.setFillColor(Color.BLUE);
        }
        tmp.setIcon(icon);
        myPoint.setIcon(tmp);
        mySet.addDPoint(myPoint);
    }

    /**
     * This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     */
    @Override
    public Double getDoublePlotValue(Population pop) {
        if (AbstractMultiObjectiveOptimizationProblem.isPopulationMultiObjective(pop)) {
            return this.calculateMetric(pop);
        } else {
            // in this case the local Pareto-Front could be multi-objective
            if (AbstractMultiObjectiveOptimizationProblem.isPopulationMultiObjective(this.paretoFront)) {
                return this.calculateMetric(this.paretoFront);
            } else {
                return pop.getBestEAIndividual().getFitness(0);
            }
        }
    }

    /**
     * This method return the local Pareto front
     *
     * @return the local Pareto-front log
     */
    public Population getLocalParetoFront() {
        return this.paretoFront;
    }

    @Override
    public String[] getAdditionalDataHeader() {
        String[] superHd = super.getAdditionalDataHeader();
        return ToolBox.appendArrays(new String[]{"paretoMetricCurrent", "paretoMetricFront"}, superHd);
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        Object[] result = new Object[2];
        if (mosoConverter != null && !(mosoConverter instanceof MOSONoConvert)) {
            result[0] = Double.NaN;
            result[1] = Double.NaN;
        } else {
            result[0] = this.calculateMetric((Population) pop);
            result[1] = this.calculateMetric(getLocalParetoFront());
        }
        return ToolBox.appendArrays(result, super.getAdditionalDataValue(pop));
    }

    @Override
    public String[] getAdditionalDataInfo() {
        String[] superInfo = super.getAdditionalDataInfo();
        return ToolBox.appendArrays(new String[]{"Pareto metric on the current population (per generation)",
                "Pareto metric on the collected pareto front"}, superInfo);
    }

    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param pop
     * @return
     */
    public double calculateMetric(Population pop) {
        if (pop == null || (pop.size() == 0)) {
            return Double.NaN;
        }
        return this.metric.calculateMetricOn(pop, this);
    }

    /**
     * This method should return the objective space range
     *
     * @return The objective space range
     */
    public double[][] getObjectiveSpaceRange() {
        return this.border;
    }

    /**
     * This method should return the output dimension
     *
     * @return The output dimension
     */
    public int getOutputDimension() {
        return this.outputDimension;
    }

    /**
     * This method allows you to set a Multiobjective to
     * Singleobjective converter if you choose to.
     *
     * @param b The new MO2SO converter.
     */
    public void setMOSOConverter(InterfaceMOSOConverter b) {
        this.mosoConverter = b;
        this.mosoConverter.setOutputDimension(this.outputDimension);
    }

    public InterfaceMOSOConverter getMOSOConverter() {
        return this.mosoConverter;
    }

    public String mOSOConverterTipText() {
        return "Choose a Multiobjective to Singleobjective converter.";
    }

    /**
     * This method allows you to choose the metric.
     *
     * @param b The new metric.
     */
    public void setMetric(InterfaceParetoFrontMetric b) {
        this.metric = b;
    }

    public InterfaceParetoFrontMetric getMetric() {
        return this.metric;
    }

    public String metricTipText() {
        return "Choose the metric to use.";
    }

}
