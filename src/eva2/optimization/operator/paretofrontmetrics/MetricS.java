package eva2.optimization.operator.paretofrontmetrics;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.population.Population;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;

/**
 * S-Metric calculates the hyper-volume covered between the current solutions and a reference point.
 */
public class MetricS implements InterfaceParetoFrontMetric, java.io.Serializable {

    private double[][] objectiveSpaceRange;

    public MetricS() {

    }

    public MetricS(MetricS b) {
        if (b.objectiveSpaceRange != null) {
            this.objectiveSpaceRange = new double[b.objectiveSpaceRange.length][2];
            for (int i = 0; i < this.objectiveSpaceRange.length; i++) {
                this.objectiveSpaceRange[i][0] = b.objectiveSpaceRange[i][0];
                this.objectiveSpaceRange[i][1] = b.objectiveSpaceRange[i][1];
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
        return new MetricS(this);
    }

    public void setObjectiveSpaceRange(double[][] range) {
        this.objectiveSpaceRange = range;
    }

    /**
     * This method allows you to initialize the metric loading data etc
     */
    public void init() {

    }

    /**
     * This method gives a metric how to evaluate
     * an achieved Pareto-Front
     */
    @Override
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem) {
        this.objectiveSpaceRange = problem.getObjectiveSpaceRange();

        double smetric = this.calculateSMetric(pop, this.objectiveSpaceRange, this.objectiveSpaceRange.length);
        double reference = 1;
        for (double[] anObjectiveSpaceRange : this.objectiveSpaceRange) {
            reference *= (anObjectiveSpaceRange[1] - anObjectiveSpaceRange[0]);
        }
        //System.out.println("SMetric: "+smetric +" Reference: " + reference);
        double res = ((Math.abs(smetric) / Math.abs(reference)) * 100);
        return res;
    }

    /**
     * This method will calculate the s-metric from a double array of
     * fitness cases
     *
     * @param pop    Array of fitness cases
     * @param border The border to use when calculating the s-metric.
     * @return s-metric
     */
    public double calculateSMetric(Population pop, double[][] border, int dim) {
        double result = 0;
        int tmpIndex;
        double tmpSmallest;
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
        if (dim == 1) {
            return pop.getBestFitness()[0];
        }

        if (dim > 2) {
            smPop = new Population();
        }
        double[][] f = new double[archive.size()][dim];
        double[] tmpF, redF;
        for (int i = 0; i < f.length; i++) {
            tmpF = ((AbstractEAIndividual) archive.get(i)).getFitness();
            System.arraycopy(tmpF, 0, f[i], 0, dim);
            if (smPop != null) {
                redF = new double[tmpF.length - 1];
                System.arraycopy(tmpF, 0, redF, 0, redF.length);
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
        lastValue = new double[dim];
        for (int i = 0; i < dim; i++) {
            lastValue[i] = border[i][1];
        }
        lastValue[dim - 1] = border[dim - 1][0] - 1;

        for (int i = 0; i < f.length; i++) {
            // first search for the smallest in the last dimension
            tmpIndex = 0;
            tmpSmallest = Double.MAX_VALUE;
            for (int j = 0; j < f.length; j++) {
                if ((f[j][dim - 1] > lastValue[dim - 1]) && (f[j][dim - 1] < tmpSmallest)) {
                    tmpIndex = j;
                    tmpSmallest = f[j][dim - 1];
                }
            }
            if (f[tmpIndex][dim - 1] > lastValue[dim - 1]) {
                // now i should have identified the current smallest
                // here i found the very first individual, therefore
                // no lastValue has been set... set it to border
                if (lastValue[dim - 1] < border[dim - 1][0]) {
                    lastValue[dim - 1] = border[dim - 1][0];
                }
                if (dim == 2) {
                    result += lastValue[0] * (f[tmpIndex][1] - lastValue[1]);
                } else {
                    // reduce dimension and call calculateCMetric recursively
                    Population tmpPop = new Population();
                    double[][] tmpBorder;
                    double tmpS;
                    for (int j = 0; j < archive.size(); j++) {
                        if (((AbstractEAIndividual) archive.get(j)).getFitness(dim - 1) < f[tmpIndex][dim - 1]) {
                            // this one is small enough to join the next level
                            tmpPop.add(smPop.get(j));
                        }
                    }

                    tmpBorder = new double[border.length - 1][2];
                    for (int j = 0; j < tmpBorder.length; j++) {
                        tmpBorder[j][0] = border[j][0];
                        tmpBorder[j][1] = border[j][1];
                    }
                    tmpS = this.calculateSMetric(tmpPop, tmpBorder, dim - 1);
                    result += (f[tmpIndex][dim - 1] - lastValue[dim - 1]) * tmpS;
                }
                System.arraycopy(f[tmpIndex], 0, lastValue, 0, f[tmpIndex].length);
            } else {
                // no smallest found break
                i = f.length + 1;
                break;
            }
        }
        // i should add the final element to the result
        if (dim == 2) {
            // reaching from lastValue to upper border
            result += lastValue[0] * (border[1][1] - lastValue[1]);
        } else {
            // reduce dimension and call calculateCMetric recursively
            Population tmpPop = new Population();
            double[][] tmpBorder;
            double tmpS;
            for (int j = 0; j < archive.size(); j++) {
                if (((AbstractEAIndividual) archive.get(j)).getFitness(dim - 1) <= lastValue[dim - 1]) {
                    // this one is small enough to join the next level
                    tmpPop.add(smPop.get(j));
                }
            }
            tmpBorder = new double[border.length - 1][2];
            for (int j = 0; j < tmpBorder.length; j++) {
                tmpBorder[j][0] = border[j][0];
                tmpBorder[j][1] = border[j][1];
            }
            tmpS = this.calculateSMetric(tmpPop, tmpBorder, dim - 1);
            result += (border[dim - 1][1] - lastValue[dim - 1]) * tmpS;
        }
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "S-Metric";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Calculating the hypervolume UNDER the given Pareto-front.";
    }
}