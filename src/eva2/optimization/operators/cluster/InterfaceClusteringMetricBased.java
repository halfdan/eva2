package eva2.optimization.operators.cluster;

import eva2.optimization.operators.distancemetric.InterfaceDistanceMetric;

/**
 * A clustering method which is associated with a metric.
 * 
 * @author mkron
 *
 */
public interface InterfaceClusteringMetricBased {
    public InterfaceDistanceMetric getMetric();
    public void setMetric(InterfaceDistanceMetric m);
}
