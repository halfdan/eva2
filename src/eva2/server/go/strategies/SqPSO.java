package eva2.server.go.strategies;

import eva2.server.go.enums.PSOTopologyEnum;
import eva2.server.go.operators.cluster.ClusteringDensityBased;
import eva2.server.go.operators.distancemetric.IndividualDataMetric;
import eva2.server.go.operators.paramcontrol.ParamAdaption;

/**
 * A thunk class preconfiguring CBN-EA to function as a sequential niching method. This
 * is to be comparable to parallel and semi-sequential niching (esp. using the same convergence
 * criterion).
 * 
 * @author mkron
 *
 */
public class SqPSO extends ClusterBasedNichingEA {
	public SqPSO() {
		this(1e-10, 15, 15); // default values
	}
	
	public SqPSO(double epsilonConv, int haltingWindow, int popSize) {
		super();
		setClusterDiffDist(Double.MAX_VALUE);
		setMaxSpeciesSize(-1);
		// dummy: cluster all always
		setDifferentiationCA(new ClusteringDensityBased(Double.MAX_VALUE, 1,
				new IndividualDataMetric(ParticleSwarmOptimization.partBestPosKey)));
		// just a dummy
		setMergingCA(new ClusteringDensityBased(0., 0, new IndividualDataMetric(ParticleSwarmOptimization.partBestPosKey)));
		setEpsilonBound(epsilonConv);
		setHaltingWindow(haltingWindow);
		setMaxSpeciesSize(popSize);
		setOptimizer(new ParticleSwarmOptimization(popSize, 2.05, 2.05, PSOTopologyEnum.grid, 2));
		ParamAdaption[] defAdpt = new ParamAdaption[]{};
		setParameterControl(defAdpt);
//		if (threshAdaption) addParameterControl(getDefaultThreshAdaption());
		setPopulationSize(popSize);
	}

//	public void hideHideable() 
	
	public String getName() {
		return "SqPSO";
	}
}
