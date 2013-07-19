package eva2.optimization.operator.nichepso.absorption;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * interface for the absorption strategies used in NichePSO
 */
public interface InterfaceAbsorptionStrategy {

	/** @tested 
	 * decides whether indy should be absorbed into the subswarm according to the absorption strategie
	 * @param indy
	 * @param subswarm the swarm indy will be absorbed from
	 * @param mainswarm the swarm indy currently belongs to 
	 * (population statistics from that swarm may be used to decide whether indy should be absorbed)
	 * @return 
	 */
	public abstract boolean shouldAbsorbParticleIntoSubswarm(
			AbstractEAIndividual indy, 
			ParticleSubSwarmOptimization subswarm, 
			ParticleSubSwarmOptimization mainswarm);

	/** @tested 
	 * absorbs indy according to the absorbtion strategy
	 * @param indy
	 * @param subswarm the swarm indy will be absorbed from
	 * @param mainswarm the swarm indy currently belongs to 
	 */
	public abstract void absorbParticle(
			AbstractEAIndividual indy, 
			ParticleSubSwarmOptimization subswarm,
			ParticleSubSwarmOptimization mainswarm);

	public abstract Object clone();
}
