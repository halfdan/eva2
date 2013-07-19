package eva2.optimization.operator.cluster;

/**
 * A clustering method which has a cluster parameter - the niche radius for example.
 * 
 * @author mkron
 *
 */
public interface InterfaceClusteringDistanceParam extends InterfaceClustering {
	public double getClustDistParam();
	public void setClustDistParam(double param);
}
