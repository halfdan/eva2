package eva2.optimization.operator.cluster;

import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;

/**
 * A clustering method which is associated with a metric.
 *
 * @author mkron
 */
public interface InterfaceClusteringMetricBased {
    InterfaceDistanceMetric getMetric();

    void setMetric(InterfaceDistanceMetric m);
}
