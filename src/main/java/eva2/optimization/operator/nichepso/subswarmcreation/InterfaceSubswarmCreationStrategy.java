package eva2.optimization.operator.nichepso.subswarmcreation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * interface for the subswarm creation strategies used in NichePSO
 */
public interface InterfaceSubswarmCreationStrategy {

    /**
     * Decides whether a subswarm should be created for the given indy and mainswarm according to the creation strategy
     *
     * @param indy
     * @param mainswarm
     * @return
     */
    boolean shouldCreateSubswarm(
            AbstractEAIndividual indy,
            ParticleSubSwarmOptimization mainswarm);

    /**
     * Creates a subswarm from indy, the details depend on the concrete strategy.
     *
     * @param preparedSubswarm a subswarm which is appropriately prepared
     *                         (ie its problem, optimization strategy etc. are set correctly from the "meta-optimizer")
     *                         Afterwards the subswarm contains the generated particles
     * @param indy             a particle from which a subswarm should be created
     * @param mainSwarm        the main swarm which contains indy
     * Creates a subswarm from indy, the details depend on the concrete strategy.
     */
    void createSubswarm(
            ParticleSubSwarmOptimization preparedSubswarm,
            AbstractEAIndividual indy,
            ParticleSubSwarmOptimization mainSwarm);

    Object clone();
}
