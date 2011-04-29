package eva2.server.go.operators.cluster;

import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;

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
