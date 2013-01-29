package eva2.server.go.operators.nichepso.absorption;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.strategies.ParticleSubSwarmOptimization;

/**
 * Particles are absorbed into a subswarm when they move into an area 
 * within the radius of that subswarm. 
 * This strategy is proposed in
 * R. Brits, A. P. Engelbrecht and B. Bergh. 
 * A Niching Particle Swarm Optimizer 
 * In Proceedings of the 4th Asia-Pacific Conference on Simulated Evolution and Learning (SEAL'02), 
 * 2002, 2, 692-696 
 */
public class StandardAbsorptionStrategy implements	InterfaceAbsorptionStrategy, java.io.Serializable {

/**********************************************************************************************************************
 * ctors, init, clone
 */	
    @Override
	public Object clone(){
		return (Object) new StandardAbsorptionStrategy();
	}
	
	public String globalInfo(){
		return "Strategy to absorb main swarm particles into a subswarm";
	}
	
/**********************************************************************************************************************
 * shouldAbsorbParticleIntoSubswarm
 */
	
	/**  @tested 
	 * true if
	 * the subswarm is active and 
	 * the particle lies in the radius of the subswarm 
	 * (non-Javadoc) @see javaeva.server.oa.go.Operators.NichePSO.InterfaceAbsorptionStrategy#shouldAbsorbParticleIntoSubswarm(javaeva.server.oa.go.EAIndividuals.AbstractEAIndividual, javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization)
	 */
    @Override
	public boolean shouldAbsorbParticleIntoSubswarm(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm) {
		if (!subswarm.isActive()){
			return false; // no interaction between active mainswarmparticle and inactive subswarm
		}
		if (!particleLiesInSubswarmRadius(indy, subswarm)) {
			return false;
		}
		return true;
	}

	private boolean particleLiesInSubswarmRadius(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm) {
		
		double R = subswarm.getBoundSwarmRadius(); // uses euclidean distance
		AbstractEAIndividual gbest = subswarm.getGBestIndividual(); 
		double dist = subswarm.distance(indy,gbest); // euclidean distance
		if (dist <= R){
			return true;
		}else {
                return false;
            }
	}


/**********************************************************************************************************************
 * absorbParticle
 */
	
	/**  @tested junit
	 * adds indy to an active subswarm, then removes indy from the mainswarm.
	 * (non-Javadoc) @see javaeva.server.oa.go.Operators.NichePSO.InterfaceAbsorptionStrategy#absorbParticle(javaeva.server.oa.go.EAIndividuals.AbstractEAIndividual, javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization, javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization)
	 */
    @Override
	public void absorbParticle(AbstractEAIndividual indy, ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm) {
		if (!subswarm.isActive()){
			System.out.println("absorbParticle: trying to absorb a particle into an inactive subswarm.");
			return;
		}
		subswarm.add(indy);
		subswarm.populationSizeHasChanged();
		mainswarm.removeSubIndividual(indy);
		mainswarm.populationSizeHasChanged();
	}
	
}
