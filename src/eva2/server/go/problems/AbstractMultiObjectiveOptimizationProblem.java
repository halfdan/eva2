package eva2.server.go.problems;

import javax.swing.*;

import eva2.gui.Chart2DDPointIconCircle;
import eva2.gui.GraphPointSet;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.archiving.ArchivingAllDominating;
import eva2.server.go.operators.archiving.ArchivingNSGA;
import eva2.server.go.operators.moso.InterfaceMOSOConverter;
import eva2.server.go.operators.moso.MOSONoConvert;
import eva2.server.go.operators.paretofrontmetrics.InterfaceParetoFrontMetric;
import eva2.server.go.operators.paretofrontmetrics.MetricS;
import eva2.server.go.populations.Population;

import java.util.ArrayList;
import java.awt.*;

import wsi.ra.chart2d.DPointIcon;
import wsi.ra.chart2d.DPoint;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.04.2004
 * Time: 13:32:08
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractMultiObjectiveOptimizationProblem extends AbstractOptimizationProblem {

    protected InterfaceMOSOConverter            m_MOSOConverter     = new MOSONoConvert();
    protected InterfaceParetoFrontMetric        m_Metric            = new MetricS();
    transient protected Population              m_ParetoFront       = new Population();
    public    ArrayList                         m_AreaConst4Parallelization = new ArrayList();
    protected int                               m_OutputDimension   = 2;

    /**
     * TODO
     */
    public    double[][]                        m_Border;
    transient protected eva2.gui.Plot        m_Plot;
    transient protected JFrame                  m_Result;
    transient boolean                           m_Show              = false;

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public abstract Object clone();

    /** This method inits the Problem to log multiruns for the s-Metric it
     * is necessary to give the border to get reliable results.
     * also it is necessary to init the local Pareto-Front and the
     * problem frame (i'll provide a default implementation here.
     */
    public void initProblem() {
        this.m_Border = new double[2][2];
        for (int i = 0; i < this.m_Border.length; i++) {
            this.m_Border[i][0] = 0;
            this.m_Border[i][1] = 5;
        }
        this.m_ParetoFront = new Population();
        if (this.m_Show) this.initProblemFrame();
    }

    /** This method checks whether the problem has truely evaluated
     * to a multiobjective problem
     * @return true if all individuals are multiobjective
     */
    public boolean isPopulationMultiObjective(Population pop) {
        if (pop == null) return false;
        if (pop.size() == 0) return false;
        int best = pop.getBestFitness().length, tmp;
        for (int i = 0; i < pop.size(); i++) {
            tmp = ((AbstractEAIndividual)pop.get(i)).getFitness().length;
            if (tmp <= 1) {
                //System.out.println("There is a single objective individual in the population!");
                return false;
            }
            if (tmp != best) {
                //System.out.println("Not all individual have equal length fitness!");
                return false;
            }
        }
        return true;
    }

    /** This method will reset the local Pareto-Front
     * This caused a lot of trouble for the DeNovo Approach of MOCCO
     */
    public void resetParetoFront() {
        this.m_ParetoFront = new Population();
    }

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population) {
        AbstractEAIndividual    tmpIndy;
        double[]                fitness;

        evaluatePopulationStart(population);

        // first evaluate the population
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual) population.get(i);
            tmpIndy.resetConstraintViolation();
            this.evaluate(tmpIndy);
            fitness = tmpIndy.getFitness();
            // check and update border if necessary
            if (m_Border == null)
            	this.m_Border = new double[fitness.length][2];
            else if (fitness.length != this.m_Border.length) {
                //System.out.println("AbstractMOOptimizationProblem: Warning fitness.length("+fitness.length+") doesn't fit border.length("+this.m_Border.length+")");
                //System.out.println("Resetting the border!");
                this.m_Border = new double[fitness.length][2];
            }
            for (int j = 0; j < fitness.length; j++) {
//                if ((this.m_Border[j][0] > fitness[j]) || (this.m_Border[j][1] < fitness[j])) {
//                    System.out.println("border... " + j);
//                    System.out.println(this.m_Border[j][0]+" > "+fitness[j]);
//                    System.out.println(this.m_Border[j][1]+" < "+fitness[j]);
//                }
                this.m_Border[j][0] = Math.min(this.m_Border[j][0], fitness[j]);
                this.m_Border[j][1] = Math.max(this.m_Border[j][1], fitness[j]);
            }
            population.incrFunctionCalls();
        }

        evaluatePopulationEnd(population); // refactored by MK
    }

    public void evaluatePopulationEnd(Population population) {
        // So what is the problem:
        // on the one hand i want to log the pareto-front in the
        // multiobjective case
        // but on the other hand i also need to log the pareto-front
        // in the single objective case if a MOSOConverter is used,
        // here i want to log all found pareto-optimal solutions, which
        // could be pretty many

        // currently the problem should be multi-criteria
        // log the pareto-front
        if (this.isPopulationMultiObjective(population)) {
            if (this.m_ParetoFront == null) this.m_ParetoFront = new Population();
            if (this.m_ParetoFront.getArchive() == null) {
                Population archive = new Population();
                archive.setPopulationSize(100);
                this.m_ParetoFront.SetArchive(archive);
            }
            this.m_ParetoFront.addPopulation((Population) population.getClone());
            ArchivingNSGA archiving = new ArchivingNSGA();
            archiving.addElementsToArchive(this.m_ParetoFront);
            this.m_ParetoFront = this.m_ParetoFront.getArchive();
        }

        // Sometimes you want to transform a multiobjective optimization problem
        // into a single objective one, this way single objective optimization
        // algorithms can be applied more easily
        this.m_MOSOConverter.convertMultiObjective2SingleObjective(population);

        if (this.m_Show) this.drawProblem(population);
    }

   /** This method will init the problem specific visualisation of the problem
     */
    public void initProblemFrame() {
        double[] tmpD = new double[2];
        tmpD[0] = 0;
        tmpD[1] = 0;
        if (this.m_Plot == null) m_Plot = new eva2.gui.Plot("Multiobjective Optimization", "Y1", "Y2", tmpD, tmpD);

        // plot init stuff
        this.initAdditionalData(this.m_Plot, 10);
    }

    /** This method will plot a reference solutions or something like it
     * @param plot      The plot where you can draw your stuff.
     * @param index     The first index where you can draw your stuff
     */
    public void initAdditionalData(eva2.gui.Plot plot, int index) {
        // for example plot the current population
        plot.clearGraph(index);
        plot.setUnconnectedPoint(0, 0, index);
    }

    /** This method will draw the current state of the optimization process
     * @param p     The current population
     */
    public void drawProblem(Population p) {

        ArchivingAllDominating  tmpArch = new ArchivingAllDominating();
        Population              tmpPop = null;

        if (p.getGeneration() > 2) {
//            m_Plot = new eva2.gui.Plot("Multiobjective Optimization", "Y1", "Y2");
            // i want to plot the pareto front for MOEA and other strategies
            // but i have to differentiate between the case where
            // there is a true MOEA at work and where the
            // MOOpt was converted into a SOOpt
            if (this.isPopulationMultiObjective(p)) {
                // in this case i have to use my local archive
                tmpPop = this.m_ParetoFront;
            } else {
                // in this case i use the population of the optimizer
                // and eventually the pop.archive if there is one
                tmpPop = new Population();
                tmpPop.addPopulation(p);
                if (p.getArchive() != null) tmpPop.addPopulation(p.getArchive());
                tmpArch.addElementsToArchive(tmpPop);
                tmpPop = tmpPop.getArchive();
            }
            if (tmpPop != null) {
                // i got either a multiobjective population or a multiobjective local population
                this.m_Plot.clearAll();
                tmpArch.plotParetoFront(tmpPop, this.m_Plot);
                if ((true) && (p.getArchive() != null)) {
                    GraphPointSet   mySet = new GraphPointSet(10, this.m_Plot.getFunctionArea());
                    DPoint          myPoint;
                    Chart2DDPointIconCircle      icon;
                    double[]        tmpD;
                    mySet.setConnectedMode(false);
                    tmpPop = p.getArchive();
                    for (int i = 0; i < tmpPop.size(); i++) {
                        icon    = new Chart2DDPointIconCircle();
                        tmpD    = ((AbstractEAIndividual)tmpPop.get(i)).getFitness();
                        myPoint = new DPoint(tmpD[0], tmpD[1]);
                        if (((AbstractEAIndividual)tmpPop.get(i)).getConstraintViolation() > 0) {
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
            } else {
//                // in this case i got a single objective optimization problem
//                if (this.m_Plot != null) this.m_Plot.dispose();
//                if (this.m_Result == null) {
//                    this.m_Result = new JFrame();
//                    this.m_Result.addWindowListener(new WindowAdapter() {
//                        public void windowClosing(WindowEvent ev) {
//                            System.gc();
//                        }
//                    });
//                }
            }
            // draw additional data
            this.drawAdditionalData(this.m_Plot, p, 10);
        }
    }

    /** This method will plot a reference solutions or something like it
     * @param plot      The plot where you can draw your stuff.
     * @param index     The first index where you can draw your stuff
     */
    public void drawAdditionalData(eva2.gui.Plot plot, Population pop, int index) {
        double[] tmpFitness;
        // for example plot the current population
        plot.clearGraph(index);
        for (int i = 0; i < pop.size(); i++) {
            tmpFitness = ((AbstractEAIndividual)pop.get(i)).getFitness();
            if (tmpFitness.length <= 1) tmpFitness = (double[])((AbstractEAIndividual)pop.get(i)).getData("MOFitness");
            plot.setUnconnectedPoint(tmpFitness[0], tmpFitness[1], index);
        }
        plot.setUnconnectedPoint(this.m_Border[0][1], this.m_Border[1][1], index);
        plot.setUnconnectedPoint(this.m_Border[0][0], this.m_Border[1][0], index);
    }

    /** This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     */
    public Double getDoublePlotValue(Population pop) {
        if (this.isPopulationMultiObjective(pop)) {
            return new Double(this.calculateMetric(pop));
        } else {
            // in this case the local Pareto-Front could be multi-objective
            if (this.isPopulationMultiObjective(this.m_ParetoFront)) {
                return new Double(this.calculateMetric(this.m_ParetoFront));
            } else {
                return new Double(pop.getBestEAIndividual().getFitness(0));
            }
        }
    }

    /** This method return the local Pareto front
     * @return the local Pareto-front log
     */
    public Population getLocalParetoFront() {
        return this.m_ParetoFront;
    }

    /** This method returns the header for the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringHeader(Population pop) {
        String result;
        if (this.isPopulationMultiObjective(pop))
            result = "SMetric";
        else
            result = "BestFitness";
        return result;
    }

    /** This method returns the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringValue(Population pop) {
        String result = "";
        if (this.isPopulationMultiObjective(pop))
            result += this.calculateMetric(pop);
        else
            result += pop.getBestEAIndividual().getFitness()[0];
        return result;
    }

    public double calculateMetric(Population pop) {
        return this.m_Metric.calculateMetricOn(pop, this);
    }

    /** This method should return the objective space range
     * @return The objective space range
     */
    public double[][] getObjectiveSpaceRange() {
        return this.m_Border;
    }

    /** This method should return the output dimension
     * @return The output dimension
     */
    public int getOutputDimension() {
        return this.m_OutputDimension;
    }

//    /** This method will calculate the s-Metric if an archive population is present.
//     * @param pop       The population.
//     * @return s-metric
//     */
//    public double tcalculateSMetric(Population pop) {
//        double result = 0;
//
//        ((SMetric)this.m_Metric).setObjectiveSpaceRange(this.m_Border);
//        result = this.m_Metric.calculateMetricOn(pop);
//
//        return result;
//    }
//
//    /** This method will calculate the s-Metric if an archive population is present.
//     * @param pop       The population.
//     * @return s-metric
//     */
//    public double calculateRelativeSMetric(Population pop, double[][] ref) {
//        double result = 0;
//        SMetricWithReference tmpMetric = new SMetricWithReference();
//        tmpMetric.setObjectiveSpaceRange(this.m_Border);
//        tmpMetric.setReferenceFront(ref);
//        result = tmpMetric.calculateMetricOn(pop);
//        return result;
//    }

    /** This method allows you to set a Multiobjective to
     * Singleobjective converter if you choose to.
     * @param b     The new MO2SO converter.
     */
    public void setMOSOConverter(InterfaceMOSOConverter b) {
        this.m_MOSOConverter = b;
        this.m_MOSOConverter.setOutputDimension(this.m_OutputDimension);
    }
    public InterfaceMOSOConverter getMOSOConverter() {
        return this.m_MOSOConverter;
    }
    public String mOSOConverterTipText() {
        return "Choose a Multiobjective to Singleobjective converter.";
    }

    /** This method allows you to choose the metric.
     * @param b     The new metric.
     */
    public void setMetric(InterfaceParetoFrontMetric b) {
        this.m_Metric = b;
    }
    public InterfaceParetoFrontMetric getMetric() {
        return this.m_Metric;
    }
    public String metricTipText() {
        return "Choose the metric to use.";
    }

}
