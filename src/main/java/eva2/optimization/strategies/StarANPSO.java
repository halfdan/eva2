package eva2.optimization.strategies;

import eva2.optimization.operator.paramcontrol.LinearParamAdaption;
import eva2.optimization.operator.paramcontrol.ParamAdaption;

public class StarANPSO extends ANPSO {
    private int defaultEvalCnt = 10000;

    public StarANPSO() {
        super();

        NichePSO.starNPSO(this, defaultEvalCnt);
        getMainSwarm().setAlgoType(ParticleSwarmOptimization.PSOType.Inertness);
        setMainSwarmAlgoType(ParticleSwarmOptimization.PSOType.Inertness);
        getMainSwarm().setParameterControl(new ParamAdaption[]{new LinearParamAdaption("inertnessOrChi", 0.7, 0.2)});
//		setMainSwarmInertness(new LinearParameterAging(0.7, 0.2, defaultEvalCnt/getMainSwarmSize()));


        getMainSwarm().setPhi1(1.2);
        getMainSwarm().setPhi2(0.6);  // ANPSO uses communication in the main swarm
        //Possible topologies are: "Linear", "Grid", "Star", "Multi-Swarm", "Tree", "HPSO", "Random" in that order starting by 0.
        setMainSwarmTopologyTag(3); //"Multi-Swarm" favors the formation of groups in the main swarm
        setMainSwarmTopologyRange(2); // range for topologies like random, grid etc. (does not affect "Multi-Swarm")
        setMaxInitialSubSwarmSize(0); // deactivate early reinits
    }

//	public void setEvaluationCount(int evalCnt) {
//		setMainSwarmInertness(new LinearParameterAging(0.7, 0.2, evalCnt/getMainSwarmSize()));
//	}

    public StarANPSO(StarANPSO o) {
        super(o);
        this.defaultEvalCnt = o.defaultEvalCnt;
    }

    @Override
    public String getName() {
        return "Star-" + super.getName();
    }
}
