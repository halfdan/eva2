package eva2.tools;

/**
 * This class represents a cluster object in the R^N.
 */
public class Cluster {
    /**
     * Number of samples in the cluster.
     */
    public int samplesInCluster;
    /**
     * Center of the cluster.
     */
    public double[] center;
    /**
     * nearest sample (double[]) to the center of the cluster.
     */
    public double[] nearestSample;

    /**
     * This class represents a cluster of
     * sample points.
     *
     * @param center           center
     * @param SamplesInCluster Number of samples in cluster
     * @param nearestSample    Nearest sample to cluster center.
     */
    public Cluster(double[] center, int SamplesInCluster, double[] nearestSample) {
        samplesInCluster = SamplesInCluster;
        this.center = center;
        this.nearestSample = nearestSample;
    }
}