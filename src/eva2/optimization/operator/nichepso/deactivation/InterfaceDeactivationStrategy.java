package eva2.optimization.operator.nichepso.deactivation;

import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * interface for the deactivation strategies used in NichePSO/ANPSO
 */
public interface InterfaceDeactivationStrategy {

    /**
     * @param subswarm
     * @return
     * @tested decides whether a subswarm should be deacitvated according to the deactivation strategy
     */
    public abstract boolean shouldDeactivateSubswarm(ParticleSubSwarmOptimization subswarm);

    /**
     * @param subswarm
     * @param mainswarm
     * @tested deactivates a given subswarm.
     * What happens to the particles in this subswarm depends on the concrete strategy.
     * Return the list of indices to be reinitialized or null.
     */
    public abstract int[] deactivateSubswarm(ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm);

    public abstract Object clone();

}
