package eva2.optimization.operator.nichepso.deactivation;

import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * interface for the deactivation strategies used in NichePSO/ANPSO
 */
public interface InterfaceDeactivationStrategy {

    /**
     * Decides whether a subswarm should be deactivated according to the deactivation strategy
     *
     * @param subswarm
     * @return
     */
    boolean shouldDeactivateSubswarm(ParticleSubSwarmOptimization subswarm);

    /**
     * Deactivates a given subswarm.
     * What happens to the particles in this subswarm depends on the concrete strategy.
     *
     * @param subswarm
     * @param mainswarm
     * @return the list of indices to be reinitialized or null.
     */
    int[] deactivateSubswarm(ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm);

    Object clone();

}
