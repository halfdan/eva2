package eva2.server.go.operators.paretofrontmetrics;


import eva2.gui.PropertyFilePath;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.archiving.ArchivingAllDominating;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.server.go.tools.FileTools;
import java.util.ArrayList;

/** Maximum Pareto Front Error gives the maximum distance of all minimum distances of each
 * element in the current solution to the true Pareto front.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 09.06.2005
 * Time: 13:25:44
 * To change this template use File | Settings | File Templates.
 */
public class MetricMaximumParetoFrontError implements eva2.server.go.operators.paretofrontmetrics.InterfaceParetoFrontMetric, java.io.Serializable {
    private PropertyFilePath    m_InputFilePath         = PropertyFilePath.getFilePathFromResource("MOPReference/T1_250.txt");
    private String[]            m_Titles;
    private double[][]          m_Reference;

    public MetricMaximumParetoFrontError() {
        this.loadReferenceData();
    }

    public MetricMaximumParetoFrontError(MetricMaximumParetoFrontError b) {
        if (b.m_Titles != null) {
            this.m_Titles = new String[b.m_Titles.length];
            for (int i = 0; i < this.m_Titles.length; i++) {
                this.m_Titles[i] = b.m_Titles[i];
            }
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

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    @Override
    public Object clone() {
        return (Object) new MetricMaximumParetoFrontError(this);
    }

    /** This method allows you to init the metric loading data etc
     *
     */
    public void init() {
        this.loadReferenceData();
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
    @Override
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem) {
        double      result = 0, min, dist;
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
        result = Double.NEGATIVE_INFINITY;
        for (int j = 0; j < this.m_Reference.length; j++) {
            min = Double.POSITIVE_INFINITY;
            for (int i = 0; i < tmpPPO.size(); i++) {
                min = Math.min(min, distance(((AbstractEAIndividual)tmpPPO.get(i)).getFitness(), this.m_Reference[j]));
            }
            result = Math.max(result, min);
        }
        return result;
    }

    /** This method will calculate the distance
     * @return the distance
     */
    private double distance(double[] d1, double[] d2) {
        double      result = 0;
        for (int j = 0; (j < d1.length) && (j < d2.length); j++) {
            result += Math.pow((d1[j]-d2[j]), 2);
        }
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Maximum Pareto Front Error";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method calculates the maximum distance to the reference.";
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

}



