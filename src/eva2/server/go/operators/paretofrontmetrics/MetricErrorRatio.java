package eva2.server.go.operators.paretofrontmetrics;


import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.ArrayList;

import eva2.gui.PropertyFilePath;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.archiving.ArchivingAllDominating;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.server.go.tools.FileTools;
import eva2.tools.ResourceLoader;


/** The error ratio metric only suited for small discrete
 * Pareto fronts, since it calculates the intersection between
 * the reference and the current solution.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2005
 * Time: 17:22:23
 * To change this template use File | Settings | File Templates.
 */
public class MetricErrorRatio implements eva2.server.go.operators.paretofrontmetrics.InterfaceParetoFrontMetric, java.io.Serializable {
    private PropertyFilePath    m_InputFilePath         = PropertyFilePath.getFilePathFromResource("resources/MOPReference/T1_250.txt");
    private double              m_Epsilon               = 0.0001;
    private String[]            m_Titles;
    private double[][]          m_Reference;

    public MetricErrorRatio() {
        this.loadReferenceData();
    }

    public MetricErrorRatio(MetricErrorRatio b) {
        this.m_Epsilon          = b.m_Epsilon;
        if (b.m_Titles != null) {
            this.m_Titles = new String[b.m_Titles.length];
            for (int i = 0; i < this.m_Titles.length; i++) this.m_Titles[i] = b.m_Titles[i];
        }
        if (b.m_Reference != null) {
            this.m_Reference = new double[b.m_Reference.length][b.m_Reference[0].length];
            for (int i = 0; i < this.m_Reference.length; i++) {
                for (int j = 0; j < this.m_Reference[i].length; j++) {
                    this.m_Reference[i][j] = b.m_Reference[i][j];
                }
            }
        }
    }

    /** This method allows you to init the metric loading data etc
     *
     */
    public void init() {
        this.loadReferenceData();
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new MetricErrorRatio(this);
    }

    /** This method loads the reference data
     *
     */
    private void loadReferenceData() {
        String[]    tmpS, lines = FileTools.loadStringsFromFile(this.m_InputFilePath.getCompleteFilePath());
        if (lines == null) System.out.println("Failed to read "+this.m_InputFilePath.getCompleteFilePath());
        lines[0].trim();
        this.m_Titles = lines[0].split("\t");
        ArrayList   tmpA = new ArrayList();
        double[]    tmpD;
        for (int i = 1; i < lines.length; i++) {
            tmpD = new double[this.m_Titles.length];
            lines[i].trim();
            tmpS = lines[i].split("\t");
            for (int j = 0; (j < tmpD.length) && (j < tmpS.length); j++) {
                tmpD[j] = new Double(tmpS[j]).doubleValue();
            }
            tmpA.add(tmpD);
        }
        this.m_Reference = new double[tmpA.size()][];
        for (int i = 0; i < tmpA.size(); i++) {
            this.m_Reference[i] = (double[])tmpA.get(i);
        }
    }

    /** This method gives a metric how to evaluate
     * an achieved Pareto-Front
     */
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem) {
        double      result = 0;
        Population  tmpPop = new Population();
        Population  tmpPPO = new Population();
        tmpPPO.addPopulation(pop);
        if (pop.getArchive() != null) tmpPPO.addPopulation(pop.getArchive());
        if (this.m_Reference == null) {
            this.loadReferenceData();
            if (this.m_Reference == null) {
                System.out.println("No reference data!");
                return 0;
            }
        }
        ArchivingAllDominating dom = new ArchivingAllDominating();
        dom.addElementsToArchive(tmpPPO);
        tmpPPO = tmpPPO.getArchive();
        for (int i = 0; i < tmpPPO.size(); i++) {
            if (this.inReference((AbstractEAIndividual)tmpPPO.get(i))) {
                result++;
            }
        }
        return (result/((double)tmpPPO.size()));
    }

    /** This method will determine wether or not the individual is in the reference set
     * @return true if it is within the epsilon threshold
     */
    private boolean inReference(AbstractEAIndividual indy) {
        double[] fitness = indy.getFitness();
        double      result = 0;
        for (int i = 0; i < this.m_Reference.length; i++) {
            result = 0;
            for (int j = 0; (j < fitness.length) && (j < this.m_Reference[i].length); j++) {
                result += Math.pow((fitness[j]-this.m_Reference[i][j]), 2);
            }
            if (Math.sqrt(result) < this.m_Epsilon) return true;
        }
        return false;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Error ratio";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method calculates how many solutions are contained in the reference solution.";
    }

    /** This method allows you to set the path to the data file.
     * @param b     File path.
     */
    public void setInputFilePath(PropertyFilePath b) {
        this.m_InputFilePath = b;
        this.loadReferenceData();
    }
    public PropertyFilePath getInputFilePath() {
        return this.m_InputFilePath;
    }
    public String inputFilePathTipText() {
        return "Select the reference soltuion by choosing the input file.";
    }

    /** This method will allow you set an upper border as constraint
     * @param d     The upper border.
     */
    public void setEpsilon(double d) {
        this.m_Epsilon = d;
    }
    public double getEpsilon() {
        return this.m_Epsilon;
    }
    public String epsilonTipText() {
        return "For continuous objectives spaces this gives an epsilon boundary for the solutions.";
    }
}



