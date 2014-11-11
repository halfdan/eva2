package eva2.optimization.operator.nichepso.absorption;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * Particles are absorbed into a subswarm if they move into an area covered by that subswarm.
 * To prevent the destabilisation of a subswarm only particles are absorbed which do not know
 * a position that is better than the best position known by the subswarm.
 */
public class ConsiderPBestAbsorptionStrategy extends StandardAbsorptionStrategy {


    /**
     * True if the subswarm is active and the particle lies in the radius of the subswarm and
     * the particles pbest is not better than the subswarms gbest (this would "pull the subswarm away")
     */
    @Override
    public boolean shouldAbsorbParticleIntoSubswarm(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm) {
        if (!super.shouldAbsorbParticleIntoSubswarm(indy, subswarm, mainswarm)) {
            return false;
        }
        return !absorbtionConstraintViolation(indy, subswarm);
    }

    /**
     * @param indy
     * @param subswarm
     * @return
     */
    private boolean absorbtionConstraintViolation(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm) {
        AbstractEAIndividual indysPBest = (AbstractEAIndividual) indy.getData("PersonalBestKey");
        AbstractEAIndividual subswarmsGBest = subswarm.getGBestIndividual();
        return indysPBest.isDominating(subswarmsGBest);
    }
}
