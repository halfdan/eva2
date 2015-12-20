package eva2.optimization.operator.nichepso.absorption;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * interface for the absorption strategies used in NichePSO
 */
public interface InterfaceAbsorptionStrategy {

    /**
     * @param indy      Individual
     * @param subswarm  the swarm indy will be absorbed from
     * @param mainswarm the swarm indy currently belongs to
     *                  (population statistics from that swarm may be used to decide whether indy should be absorbed)
     * @return Decides whether indy should be absorbed into the sub-swarm according to the absorption strategy
     */
    boolean shouldAbsorbParticleIntoSubswarm(
            AbstractEAIndividual indy,
            ParticleSubSwarmOptimization subswarm,
            ParticleSubSwarmOptimization mainswarm);

    /**
     * Absorbs indy according to the absorption strategy
     *
     * @param indy      Individual
     * @param subswarm  the swarm indy will be absorbed from
     * @param mainswarm the swarm indy currently belongs to
     */
    void absorbParticle(
            AbstractEAIndividual indy,
            ParticleSubSwarmOptimization subswarm,
            ParticleSubSwarmOptimization mainswarm);

    Object clone();
}
