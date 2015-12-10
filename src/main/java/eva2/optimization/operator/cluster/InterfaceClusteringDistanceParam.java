package eva2.optimization.operator.cluster;

/**
 * A clustering method which has a cluster parameter - the niche radius for example.
 *
 * @author mkron
 */
public interface InterfaceClusteringDistanceParam extends InterfaceClustering {
    double getClustDistParam();

    void setClustDistParam(double param);
}
