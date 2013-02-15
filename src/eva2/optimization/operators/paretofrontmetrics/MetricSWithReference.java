package eva2.optimization.operators.paretofrontmetrics;


import eva2.gui.PropertyFilePath;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operators.archiving.ArchivingAllDominating;
import eva2.optimization.populations.Population;
import eva2.optimization.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.optimization.tools.FileTools;
import java.util.ArrayList;

/** S-Metric calculates the hyper-volume covered between the current solutions and a reference point.
 * But here the difference to a given hybervolume is to be minimized.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.06.2005
 * Time: 10:58:33
 * To change this template use File | Settings | File Templates.
 */
public class MetricSWithReference implements InterfaceParetoFrontMetric, java.io.Serializable {
    private double[][]          m_ObjectiveSpaceRange;
    private PropertyFilePath    m_InputFilePath         = PropertyFilePath.getFilePathFromResource("MOPReference/T1_250.txt");
    private String[]            m_Titles;
    private double[][]          m_Reference;
    private double              m_ReferenceSMetric      = -1;

    public MetricSWithReference() {
        this.loadReferenceData();
    }

    public MetricSWithReference(MetricSWithReference b) {
        if (b.m_ObjectiveSpaceRange != null) {
            this.m_ObjectiveSpaceRange = new double[b.m_ObjectiveSpaceRange.length][2];
            for (int i = 0; i < this.m_ObjectiveSpaceRange.length; i++) {
                this.m_ObjectiveSpaceRange[i][0] = b.m_ObjectiveSpaceRange[i][0];
                this.m_ObjectiveSpaceRange[i][1] = b.m_ObjectiveSpaceRange[i][1];
            }
        }
        this.m_InputFilePath    = b.m_InputFilePath;
        if (b.m_Titles != null) {
            this.m_Titles = new String[b.m_Titles.length];
            System.arraycopy(b.m_Titles, 0, this.m_Titles, 0, this.m_Titles.length);
        }
        if (b.m_Reference != null) {
            this.m_Reference = new double[b.m_Reference.length][];
            for (int i = 0; i < this.m_Reference.length; i++) {
                this.m_Reference[i] = new double[b.m_Reference[i].length];
                System.arraycopy(b.m_Reference[i], 0, this.m_Reference[i], 0, this.m_Reference[i].length);
            }
        }
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    @Override
    public Object clone() {
        return (Object) new MetricSWithReference(this);
    }

    /** This method allows you to init the metric loading data etc
     *
     */
    public void init() {
        this.loadReferenceData();
    }

    public void setObjectiveSpaceRange(double[][] range) {
        this.m_ObjectiveSpaceRange = range;
    }

    /** This method loads the reference data
     *
     */
    private void loadReferenceData() {
        String[]    tmpS, lines = FileTools.loadStringsFromFile(this.m_InputFilePath.getCompleteFilePath());
        if (lines == null) {
            System.out.println("Failed to read "+this.m_InputFilePath.getCompleteFilePath());
        }
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
        this.m_ReferenceSMetric = -1;
    }

    /** This method gives a metric how to evaluate
     * an achieved Pareto-Front
     */
    @Override
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem) {
        this.m_ObjectiveSpaceRange = problem.getObjectiveSpaceRange();
        double smetric = this.calculateSMetric(pop, this.m_ObjectiveSpaceRange, this.m_ObjectiveSpaceRange.length);
        if (this.m_ReferenceSMetric < 0) {
            Population              tmpPop = new Population();
            AbstractEAIndividual  tmpIndy;
            tmpPop.setTargetSize(this.m_Reference.length);
            tmpPop.clear();
            for (int i = 0; i < this.m_Reference.length; i++) {
                tmpIndy = new ESIndividualDoubleData();
                tmpIndy.setFitness(this.m_Reference[i]);
                tmpPop.addIndividual(tmpIndy);
            }
            this.m_ReferenceSMetric = this.calculateSMetric(tmpPop, this.m_ObjectiveSpaceRange, this.m_ObjectiveSpaceRange.length);
        }
        return (smetric - this.m_ReferenceSMetric);
    }

    /** This method will calucuate the s-metric from a double array of
     * fitness cases
     * @param pop   Array of fitness cases
     * @param border    The border to use wehn calculating the s-metric.
     * @return s-metric
     */
    public double calculateSMetric(Population pop, double[][] border, int dim) {
        double  result      = 0;
        int     tmpIndex;
        double  tmpSmallest;
        Population smPop = null;
        AbstractEAIndividual tmpIndy;

        Population archive = pop.getArchive();
        if ((archive == null) || (archive.size() == 0)) {
            Population tmpPop = (Population) pop.getClone();
            ArchivingAllDominating tmpArch = new ArchivingAllDominating();
            tmpArch.addElementsToArchive(tmpPop);
            archive = tmpPop.getArchive();
        }
        // Now we have an archive, lets caluculate the s-metric
        // first extract the fitnesscases from the archive
        if (dim > 2) {
            smPop = new Population();
        }
        double[][]  f = new double[archive.size()][dim];
        double[]    tmpF, redF;
        for (int i = 0; i < f.length; i++) {
            tmpF = ((AbstractEAIndividual)archive.get(i)).getFitness();
            for (int j = 0; j < dim; j++) {
                f[i][j] = tmpF[j];
            }
            if (smPop != null) {
                redF = new double[tmpF.length -1];
                for (int j = 0; j < redF.length; j++) {
                    redF[j] = tmpF[j];
                }
                tmpIndy = new ESIndividualDoubleData();
                tmpIndy.setFitness(redF);
                smPop.add(i, tmpIndy);
            }
        }
        if (f.length < 1) {
            // In this case i don't have any individual, therefore
            // s-metric is given by the border
            result = 1.0;
            for (int i = 0; i < dim; i++) {
                result *= (border[i][1] - border[i][0]);
            }
            return result;
        }

        double[] lastValue = null;
        lastValue   = new double[dim];
        for (int i = 0; i < dim; i++) {
            lastValue[i] = border[i][1];
        }
        lastValue[dim-1] = border[dim-1][0]-1;

        for (int i = 0; i < f.length; i++) {
            // first search for the smallest in the last dimension
            tmpIndex    = 0;
            tmpSmallest = Double.MAX_VALUE;
            for (int j = 0; j < f.length; j++) {
                if ((f[j][dim-1] > lastValue[dim-1]) && (f[j][dim-1] < tmpSmallest)) {
                    tmpIndex    = j;
                    tmpSmallest = f[j][dim-1];
                }
            }
            if (f[tmpIndex][dim-1] > lastValue[dim-1]) {
                // now i should have identified the current smallest
                // here i found the very first individual, therefore
                // no lastValue has been set... set it to border
                if (lastValue[dim-1] < border[dim-1][0]) {
                    lastValue[dim-1] = border[dim-1][0];
                }
                if (dim == 2) {
                    result += lastValue[0] * (f[tmpIndex][1]-lastValue[1]);
                } else {
                    // reduce dimension and call calculateCMetric recursively
                    Population  tmpPop = new Population();
                    double[][]  tmpBorder;
                    double      tmpS;
                    for (int j = 0; j < archive.size(); j++) {
                        if (((AbstractEAIndividual)archive.get(j)).getFitness(dim-1) < f[tmpIndex][dim-1]) {
                            // this one is small enough to join the next level
                            tmpPop.add(smPop.get(j));
                        }
                    }

                    tmpBorder = new double[border.length-1][2];
                    for (int j = 0; j < tmpBorder.length; j++) {
                        tmpBorder[j][0] = border[j][0];
                        tmpBorder[j][1] = border[j][1];
                    }
                    tmpS    = this.calculateSMetric(tmpPop, tmpBorder, dim-1);
                    result  += (f[tmpIndex][dim-1] - lastValue[dim-1]) * tmpS;
                }
                for (int j = 0; j < f[tmpIndex].length; j++) {
                    lastValue[j] = f[tmpIndex][j];
                }
            } else {
                // no smallest found break
                i  = f.length+1;
                break;
            }
        }
        // i should add the final element to the result
        if (dim == 2) {
            // reaching from lastValue to upper border
            result += lastValue[0] * (border[1][1] - lastValue[1]);
        } else {
            // reduce dimension and call calculateCMetric recursively
            Population  tmpPop = new Population();
            double[][]  tmpBorder;
            double      tmpS;
            for (int j = 0; j < archive.size(); j++) {
                if (((AbstractEAIndividual)archive.get(j)).getFitness(dim-1) <= lastValue[dim-1]) {
                    // this one is small enough to join the next level
                    tmpPop.add(smPop.get(j));
                }
            }
            tmpBorder = new double[border.length-1][2];
            for (int j = 0; j < tmpBorder.length; j++) {
                tmpBorder[j][0] = border[j][0];
                tmpBorder[j][1] = border[j][1];
            }
            tmpS    = this.calculateSMetric(tmpPop, tmpBorder, dim-1);
            result  += (border[dim-1][1] - lastValue[dim-1]) * tmpS;
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
        return "S-Metric";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Difference between the current SMetric and the reference SMetric (curSM - refSM).";
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