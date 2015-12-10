package eva2.optimization.operator.paretofrontmetrics;

import eva2.gui.PropertyFilePath;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.population.Population;
import eva2.optimization.tools.FileTools;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.util.annotation.Description;

import java.util.ArrayList;

/**
 * The error ratio metric only suited for small discrete
 * Pareto fronts, since it calculates the intersection between
 * the reference and the current solution.
 */
@Description("This method calculates how many solutions are contained in the reference solution.")
public class MetricErrorRatio implements eva2.optimization.operator.paretofrontmetrics.InterfaceParetoFrontMetric, java.io.Serializable {
    private PropertyFilePath inputFilePath = PropertyFilePath.getFilePathFromResource("MOPReference/T1_250.txt");
    private double epsilon = 0.0001;
    private String[] titles;
    private double[][] reference;

    public MetricErrorRatio() {
        this.loadReferenceData();
    }

    public MetricErrorRatio(MetricErrorRatio b) {
        this.epsilon = b.epsilon;
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
     * This method allows you to initialize the metric loading data etc
     */
    public void init() {
        this.loadReferenceData();
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new MetricErrorRatio(this);
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
        double result = 0;
        Population tmpPop = new Population();
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
        for (int i = 0; i < tmpPPO.size(); i++) {
            if (this.inReference(tmpPPO.get(i))) {
                result++;
            }
        }
        return (result / ((double) tmpPPO.size()));
    }

    /**
     * This method will determine wether or not the individual is in the reference set
     *
     * @return true if it is within the epsilon threshold
     */
    private boolean inReference(AbstractEAIndividual indy) {
        double[] fitness = indy.getFitness();
        double result = 0;
        for (int i = 0; i < this.reference.length; i++) {
            result = 0;
            for (int j = 0; (j < fitness.length) && (j < this.reference[i].length); j++) {
                result += Math.pow((fitness[j] - this.reference[i][j]), 2);
            }
            if (Math.sqrt(result) < this.epsilon) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "Error ratio";
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

    /**
     * This method will allow you set an upper border as constraint
     *
     * @param d The upper border.
     */
    public void setEpsilon(double d) {
        this.epsilon = d;
    }

    public double getEpsilon() {
        return this.epsilon;
    }

    public String epsilonTipText() {
        return "For continuous objectives spaces this gives an epsilon boundary for the solutions.";
    }
}



