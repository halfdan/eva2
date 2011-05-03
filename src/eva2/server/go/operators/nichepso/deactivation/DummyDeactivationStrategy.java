package eva2.server.go.operators.nichepso.deactivation;

import eva2.server.go.strategies.ParticleSubSwarmOptimization;

/**
 * dummy strategy does not deactivate anything
 *
 */
public class DummyDeactivationStrategy implements InterfaceDeactivationStrategy, java.io.Serializable {

	public Object clone(){
		return (Object) new DummyDeactivationStrategy();
	}

	public int[] deactivateSubswarm(ParticleSubSwarmOptimization subswarm,
			ParticleSubSwarmOptimization mainswarm) {
		return null;
	}

	public boolean shouldDeactivateSubswarm(
			ParticleSubSwarmOptimization subswarm) {
		return false;
	}

}
