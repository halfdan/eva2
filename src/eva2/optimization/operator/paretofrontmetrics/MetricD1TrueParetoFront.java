package eva2.optimization.operator.paretofrontmetrics;


import eva2.gui.PropertyFilePath;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.population.Population;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.optimization.tools.FileTools;
import eva2.util.annotation.Description;

import java.util.ArrayList;

/**
 * The D1 Pareto front metric requires a reference Pareto front
 * and calculate the distance between the  current solution and
 * the true Pareto front.
 */
@Description("This method calculates the mean distance of the true Pareto front to the approximated set.")
public class MetricD1TrueParetoFront implements eva2.optimization.operator.paretofrontmetrics.InterfaceParetoFrontMetric, java.io.Serializable {

    private PropertyFilePath inputFilePath = PropertyFilePath.getFilePathFromResource("MOPReference/T1_250.txt");
    private String[] titles;
    private double[][] reference;

    public MetricD1TrueParetoFront() {
        this.loadReferenceData();
    }

    public MetricD1TrueParetoFront(MetricD1TrueParetoFront b) {
        if (b.titles != null) {
            this.titles = new String[b.titles.length];
            System.arraycopy(b.titles, 0, this.titles, 0, this.titles.length);
        }
        if (b.reference != null) {
            this.reference = new double[b.reference.length][b.reference[0].length];
            for (int i = 0; i < this.reference.length; i++) {
                System.arraycopy(b.reference[i], 0, this.reference[i], 0, this.reference[i].length);
            }
        }
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new MetricD1TrueParetoFront(this);
    }

    /**
     * This method allows you to initialize the metric loading data etc
     */
    public void init() {
        this.loadReferenceData();
    }

    /**
     * This method loads the reference data
     */
    private void loadReferenceData() {
        String[] tmpS, lines = FileTools.loadStringsFromFile(this.inputFilePath.getCompleteFilePath());
        if (lines == null) {
            System.out.println("Failed to read " + this.inputFilePath.getCompleteFilePath());
        }
        lines[0].trim();
        this.titles = lines[0].split("\t");
        ArrayList tmpA = new ArrayList();
        double[] tmpD;
        for (int i = 1; i < lines.length; i++) {
            tmpD = new double[this.titles.length];
            lines[i].trim();
            tmpS = lines[i].split("\t");
            for (int j = 0; (j < tmpD.length) && (j < tmpS.length); j++) {
                tmpD[j] = Double.parseDouble(tmpS[j]);
            }
            tmpA.add(tmpD);
        }
        this.reference = new double[tmpA.size()][];
        for (int i = 0; i < tmpA.size(); i++) {
            this.reference[i] = (double[]) tmpA.get(i);
        }
    }

    /**
     * This method gives a metric how to evaluate
     * an achieved Pareto-Front
     */
    @Override
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem) {
        double result = 0, min;
        Population tmpPPO = new Population();
        tmpPPO.addPopulation(pop);
        if (pop.getArchive() != null) {
            tmpPPO.addPopulation(pop.getArchive());
        }
        if (this.reference == null) {
            this.loadReferenceData();
            if (this.reference == null) {
                System.out.println("No reference data!");
                return 0;
            }
        }
        ArchivingAllDominating dom = new ArchivingAllDominating();
        dom.addElementsToArchive(tmpPPO);
        tmpPPO = tmpPPO.getArchive();
        result = 0;
        for (int j = 0; j < this.reference.length; j++) {
            min = Double.POSITIVE_INFINITY;
            for (int i = 0; i < tmpPPO.size(); i++) {
                min = Math.min(min, distance(tmpPPO.get(i).getFitness(), this.reference[j]));
            }
            result += min;
        }
        return (result / ((double) this.reference.length));
    }

    /**
     * This method will calculate the distance
     *
     * @return the distance
     */
    private double distance(double[] d1, double[] d2) {
        double result = 0;
        for (int j = 0; (j < d1.length) && (j < d2.length); j++) {
            result += Math.pow((d1[j] - d2[j]), 2);
        }
        return result;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "D1 P*";
    }

    /**
     * This method allows you to set the path to the data file.
     *
     * @param b File path.
     */
    public void setInputFilePath(PropertyFilePath b) {
        this.inputFilePath = b;
        this.loadReferenceData();
    }

    public PropertyFilePath getInputFilePath() {
        return this.inputFilePath;
    }

    public String inputFilePathTipText() {
        return "Select the reference soltuion by choosing the input file.";
    }
}