package eva2.tools;

/**
 * This class represents a cluster object in the R^N.
 */
public class Cluster {
    /**
     * Number of samples in the cluster.
     */
    public int m_SamplesInCluster;
    /**
     * Center of the cluster.
     */
    public double[] m_Center;
    /**
     * nearest sample (double[]) to the center of the cluster.
     */
    public double[] m_NearestSample;

    /**
     * This class represents a cluster of
     * sample points.
     *
     * @param center           center
     * @param SamplesInCluster Number of samples in cluster
     * @param nearestSample    Nearest sample to cluster center.
     */
    public Cluster(double[] center, int SamplesInCluster, double[] nearestSample) {
        m_SamplesInCluster = SamplesInCluster;
        m_Center = center;
        m_NearestSample = nearestSample;
    }
}