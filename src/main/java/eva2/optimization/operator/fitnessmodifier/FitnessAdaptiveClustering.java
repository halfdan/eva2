package eva2.optimization.operator.fitnessmodifier;

import eva2.optimization.operator.cluster.ClusteringDensityBased;
import eva2.optimization.operator.cluster.InterfaceClustering;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * The fitness modifier are defunct and are to be moved to
 * the selection operators...
 */
@Description("This is a normation method based on Fitness Sharing. It adds a penalty for too similar individuals on the standard Normation method.")
public class FitnessAdaptiveClustering implements java.io.Serializable, InterfaceFitnessModifier {

    private InterfaceClustering clusteringAlgorithm = new ClusteringDensityBased();

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
        double min = Double.POSITIVE_INFINITY;
        double[] result = new double[data.length];

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
            this.clusteringAlgorithm.initClustering(population);
            // Now search for clusters
            Population[] ClusterResult = this.clusteringAlgorithm.cluster(population, population);
            Population cluster;
            for (int i = 1; i < ClusterResult.length; i++) {
                cluster = ClusterResult[i];
                for (int j = 0; j < cluster.size(); j++) {
                    result[i] /= ((double) cluster.size());
                }
            }

            for (int i = 0; i < population.size(); i++) {
                population.get(i).SetFitness(x, result[i]);
            }
        }
    }

    /**
     * This method allows you to set/get the clustering method on which the
     * species convergence is based.
     *
     * @return The current clustering method
     */
    public InterfaceClustering getClusteringAlgorithm() {
        return this.clusteringAlgorithm;
    }

    public void setClusteringAlgorithm(InterfaceClustering b) {
        this.clusteringAlgorithm = b;
    }

    public String clusteringAlgorithmTipText() {
        return "The Cluster Algorithm on which the adaptive fitness sharing is based.";
    }
}
