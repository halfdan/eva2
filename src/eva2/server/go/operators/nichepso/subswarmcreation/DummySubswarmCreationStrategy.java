package eva2.server.go.operators.nichepso.subswarmcreation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.strategies.ParticleSubSwarmOptimization;

public class DummySubswarmCreationStrategy implements InterfaceSubswarmCreationStrategy {

    @Override
	public Object clone(){
		return (Object) new DummySubswarmCreationStrategy();
	}
	
    @Override
	public void createSubswarm(ParticleSubSwarmOptimization preparedSubswarm,
			AbstractEAIndividual indy, ParticleSubSwarmOptimization mainSwarm) {
		// TODO Auto-generated method stub

	}

    @Override
	public boolean shouldCreateSubswarm(AbstractEAIndividual indy,
			ParticleSubSwarmOptimization mainswarm) {
		// TODO Auto-generated method stub
		return false;
	}

}
