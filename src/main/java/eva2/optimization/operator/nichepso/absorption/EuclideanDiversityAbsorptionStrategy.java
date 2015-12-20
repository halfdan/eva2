package eva2.optimization.operator.nichepso.absorption;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * A threshold epsilon is proposed in [1] in order to prevent premature absorptions
 * of particles into subswarms which cover large parts of the search space.
 * A particle is absorbed into a subswarm if it lies within the radius of that subswarm
 * and the diversity of this subswarm is below the given threshold epsilon.
 * The diversity of a subswarm is defined as the average euclidean distance between
 * the particles of that subswarm and the global best position of that subswarm.
 * In [1], a threshold value of epsilon = 0.1 produced good results.
 * [1] A.P. Engelbrecht and L.N.H. van Loggerenberg.
 * Enhancing the nichepso.
 * In IEEE Congress on Evolutionary Computation,
 * 2007.
 */
public class EuclideanDiversityAbsorptionStrategy extends StandardAbsorptionStrategy {

    private double epsilon = 0.1; // 0.1 used in "Enhancing the NichePSO" by Engelbrecht et al.

    /**
     * *******************************************************************************************************************
     * ctors
     */
    public EuclideanDiversityAbsorptionStrategy() {
    }

    public EuclideanDiversityAbsorptionStrategy(EuclideanDiversityAbsorptionStrategy other) {
        this.epsilon = other.epsilon;
    }

    public EuclideanDiversityAbsorptionStrategy(double eps) {
        epsilon = eps;
    }

    /**
     * true if
     * the sub-swarm is active and
     * the particle lies in the radius of the sub-swarm and
     * the diversity (mean distance from the gbest) of the sub-swarm &lt; epsilon
     */
    @Override
    public boolean shouldAbsorbParticleIntoSubswarm(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm) {
        if (!super.shouldAbsorbParticleIntoSubswarm(indy, subswarm, mainswarm)) {
            return false; //
        }
        return diversityIsBelowThreshold(subswarm);
    }

    private boolean diversityIsBelowThreshold(ParticleSubSwarmOptimization subswarm) {
        double meanDistanceFromGBestPos = subswarm.getEuclideanDiversity();
        return meanDistanceFromGBestPos < getEpsilon();
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public String epsilonTipText() {
        return "threshold for the diversity of the subswarm that confines the absorption of main swarm particles";
    }
}
