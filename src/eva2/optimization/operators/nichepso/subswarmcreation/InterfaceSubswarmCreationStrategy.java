package eva2.optimization.operators.nichepso.subswarmcreation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * interface for the subswarm creation strategies used in NichePSO
 */
public interface InterfaceSubswarmCreationStrategy {

	/** @tested 
	 * decides whether a subswarm should be created for the given indy and mainswarm according to the creation strategie
	 * @param indy
	 * @param mainswarm
	 * @return
	 */
	public abstract boolean shouldCreateSubswarm(
			AbstractEAIndividual indy, 
			ParticleSubSwarmOptimization mainswarm);
	
	/** @tested 
	 * creates a subswarm from indy, the details depend on the concrete strategy.
	 * @param preparedSubswarm a subswarm which is appropriatly prepared 
	 * (ie its problem, optimization strategy etc. are set correctly from the "meta-optimizer") 
	 * Afterwards the subswarm containes the generated particles
	 * @param indy a particle from which a subswarm should be created
	 * @param mainSwarm the main swarm which contains indy
	 */
	public abstract void createSubswarm(
			ParticleSubSwarmOptimization preparedSubswarm,
			AbstractEAIndividual indy,
			ParticleSubSwarmOptimization mainSwarm);

	public abstract Object clone();
}
