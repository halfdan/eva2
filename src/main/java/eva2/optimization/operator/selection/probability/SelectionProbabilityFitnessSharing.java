package eva2.optimization.operator.selection.probability;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * Here we have the infamous fitness sharing method.
 */
@Description("This is a fitness sharing based normation method.")
public class SelectionProbabilityFitnessSharing extends AbstractSelectionProbability implements java.io.Serializable {

    private InterfaceSelectionProbability basicNormationMethod = new SelectionProbabilityStandard();
    private InterfaceDistanceMetric distanceMetric = new PhenotypeMetric();
    private double sharingDistance = 0.1;

    public SelectionProbabilityFitnessSharing() {
    }

    public SelectionProbabilityFitnessSharing(SelectionProbabilityFitnessSharing a) {
        if (a.basicNormationMethod != null) {
            this.basicNormationMethod = (InterfaceSelectionProbability) a.basicNormationMethod.clone();
        }
        if (a.distanceMetric != null) {
            this.distanceMetric = a.distanceMetric;
        }
        this.sharingDistance = a.sharingDistance;
    }

    @Override
    public Object clone() {
        return new SelectionProbabilityFitnessSharing(this);
    }

    /**
     * This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     * probability sums up to one. Keep in mind that fitness is always to be
     * minimizied! Small values for data =&gt; big values for selectionprob.
     *
     * @param population The population to compute.
     * @param data       The input data as double[][].
     */
    @Override
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        this.basicNormationMethod.computeSelectionProbability(population, data, obeyConst);
        // now perform fitness sharing
        double[] selProb = new double[population.size()];
        double distance, sharing, sum = 0;
        AbstractEAIndividual tmpIndy1, tmpIndy2;
        for (int i = 0; i < population.size(); i++) {
            tmpIndy1 = population.get(i);
            selProb[i] = tmpIndy1.getSelectionProbability()[0];
            sharing = 0;
            for (int j = 0; j < population.size(); j++) {
                if (i != j) {
                    distance = this.distanceMetric.distance(tmpIndy1, population.get(i));
                    if (distance < this.sharingDistance) {
                        sharing += (1 - distance / this.sharingDistance);
                    }
                }
            }
            selProb[i] /= (1 + sharing);
            sum += selProb[i];
        }
        for (int i = 0; i < population.size(); i++) {
            tmpIndy1 = population.get(i);
            tmpIndy1.setSelectionProbability(0, (selProb[i] / sum));
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Fitness Sharing";
    }

    /**
     * This method will allow you to set and get the Q Parameter
     *
     * @return The new selection pressure q.
     */
    public InterfaceSelectionProbability getBasicNormationMethod() {
        return this.basicNormationMethod;
    }

    public void setBasicNormationMethod(InterfaceSelectionProbability b) {
        this.basicNormationMethod = b;
    }

    public String basicNormationMethodTipText() {
        return "Choose the basic normation method.";
    }

    /**
     * This method will allow you to set and get the Q Parameter
     *
     * @return The new selection pressure q.
     */
    public InterfaceDistanceMetric getDistanceMetric() {
        return this.distanceMetric;
    }

    public void setDistanceMetric(InterfaceDistanceMetric b) {
        this.distanceMetric = b;
    }

    public String distanceMetricTipText() {
        return "Select the distance metric to use.";
    }

    /**
     * This method will allow you to set and get the Q Parameter
     *
     * @return The new selection pressure q.
     */
    public double getSharingDistance() {
        return this.sharingDistance;
    }

    public void setSharingDistance(double b) {
        if (b < 0.000001) {
            b = 0.000001;
        }
        this.sharingDistance = b;
    }

    public String sharingDistanceTipText() {
        return "Choose the sharing distance to use.";
    }
}