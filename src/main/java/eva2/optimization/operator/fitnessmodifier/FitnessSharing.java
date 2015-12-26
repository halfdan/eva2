package eva2.optimization.operator.fitnessmodifier;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

/**
 * The fitness modifier are defunct and are to be moved to
 * the selection operators...
 */
@Description("This is a normation method based on Fitness Sharing. It adds a penalty for too similar individuals on the standard Normation method.")
public class FitnessSharing implements java.io.Serializable, InterfaceFitnessModifier {

    private double sharingDistance = 0.05;
    private InterfaceDistanceMetric distanceMetric = new PhenotypeMetric();

    /**
     * This method allows you to modify the fitness of the individuals
     * of a population. Note that by altering the fitness you may require
     * your problem to store the unaltered fitness somewhere else so that
     * you may still fetch it!
     */
    @Override
    public void modifyFitness(Population population) {
        // prepare the calculation
        double[][] data = new double[population.size()][];
        for (int i = 0; i < data.length; i++) {
            data[i] = population.get(i).getFitness();
        }
        double min = Double.POSITIVE_INFINITY, fitnessSharing;
        double[] result = new double[data.length];
        AbstractEAIndividual tmpIndy;

        for (int x = 0; x < data[0].length; x++) {
            for (int i = 0; i < data.length; i++) {
                data[i][x] = -data[i][x];
            }
            for (int i = 0; i < data.length; i++) {
                if (data[i][x] < min) {
                    min = data[i][x];
                }
            }

            for (int i = 0; i < data.length; i++) {
                // This will cause the worst individual to have no chance of being selected
                // also note that if all individual achieve equal fitness the sum will be zero
                result[i] = data[i][x] - min + 0.1;
            }

            for (int i = 0; i < population.size(); i++) {
                tmpIndy = population.get(i);
                fitnessSharing = 0;
                for (int j = 0; j < population.size(); j++) {
                    if (this.sharingDistance < this.distanceMetric.distance(tmpIndy, population.get(j))) {
                        fitnessSharing += 1 - (this.distanceMetric.distance(tmpIndy, population.get(j)) / this.sharingDistance);
                    }
                }
                result[i] /= fitnessSharing;
            }

            for (int i = 0; i < population.size(); i++) {
                population.get(i).setFitnessAt(x, result[i]);
            }
        }
    }

    /**
     * These methods allows you to set/get the Sharing Distance
     *
     * @param sharingDistance The sharing distance
     */
    @Parameter(description = "The threshold for the similarity penalty.")
    public void setSharingDistance(double sharingDistance) {
        this.sharingDistance = sharingDistance;
    }

    public double getSharingDistance() {
        return this.sharingDistance;
    }

    /**
     * These methods allows you to set/get the type of Distance Metric.
     *
     * @param metric The distance metric to use
     */
    @Parameter(name = "metric", description = "The distance metric used. Note: This depends on the type of EAIndividual used!")
    public void setMetric(InterfaceDistanceMetric metric) {
        this.distanceMetric = metric;
    }

    public InterfaceDistanceMetric getMetric() {
        return this.distanceMetric;
    }
}
