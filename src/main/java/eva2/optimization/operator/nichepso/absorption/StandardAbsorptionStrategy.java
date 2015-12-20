package eva2.optimization.operator.nichepso.absorption;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;
import eva2.util.annotation.Description;

/**
 * Particles are absorbed into a subswarm when they move into an area
 * within the radius of that subswarm.
 * This strategy is proposed in
 * R. Brits, A. P. Engelbrecht and B. Bergh.
 * A Niching Particle Swarm Optimizer
 * In Proceedings of the 4th Asia-Pacific Conference on Simulated Evolution and Learning (SEAL'02),
 * 2002, 2, 692-696
 */
@Description("Strategy to absorb main swarm particles into a subswarm")
public class StandardAbsorptionStrategy implements InterfaceAbsorptionStrategy, java.io.Serializable {

    @Override
    public Object clone() {
        return new StandardAbsorptionStrategy();
    }

    /**
     * tested true if
     * the subswarm is active and
     * the particle lies in the radius of the subswarm
     */
    @Override
    public boolean shouldAbsorbParticleIntoSubswarm(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm) {
        if (!subswarm.isActive()) {
            return false; // no interaction between active mainswarmparticle and inactive subswarm
        }
        return particleLiesInSubswarmRadius(indy, subswarm);
    }

    private boolean particleLiesInSubswarmRadius(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm) {

        double R = subswarm.getBoundSwarmRadius(); // uses euclidean distance
        AbstractEAIndividual gbest = subswarm.getGBestIndividual();
        double dist = subswarm.distance(indy, gbest); // euclidean distance
        return dist <= R;
    }

    /**
     * Adds indy to an active sub-swarm, then removes indy from the main-swarm.
     */
    @Override
    public void absorbParticle(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm) {
        if (!subswarm.isActive()) {
            System.out.println("absorbParticle: trying to absorb a particle into an inactive sub-swarm.");
            return;
        }
        subswarm.add(indy);
        subswarm.populationSizeHasChanged();
        mainswarm.removeSubIndividual(indy);
        mainswarm.populationSizeHasChanged();
    }

}
