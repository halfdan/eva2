package eva2.optimization.operator.nichepso.merging;

import eva2.optimization.strategies.ParticleSubSwarmOptimization;
import eva2.util.annotation.Description;

import java.util.Vector;


/**
 * Two subswarms are merged if their radii overlap.
 * In case both radii equal zero the subswarms are merged if their distance is below a given threshold mu.
 * During merging, the particles of one of these subswarms are reinitialized into the main swarm.
 * This improves exploration compared to the StandardMergingStrategy.
 * This strategy is proposed in [1] and a small value, such as mu = 0.001, is suggested.
 * [1] A.P. Engelbrecht and L.N.H. van Loggerenberg.
 * Enhancing the nichepso.
 * In IEEE Congress on Evolutionary Computation,
 * 2007.
 */
@Description("Strategy to merge subswarms")
public class ScatterMergingStrategy extends StandardMergingStrategy {

    public ScatterMergingStrategy() {
        super();
    }

    public ScatterMergingStrategy(double theMu) {
        super(theMu);
    }

    /**
     * @param i
     * @param j
     * @tested if both active: reinitializes subswarm j back into the main swarm and deletes it from the subswarms.
     * if both inactive: adds population of subswarm j to population of subswarm i, then deletes subswarm j.
     */
    @Override
    public void mergeSubswarms(
            int i,
            int j,
            Vector<ParticleSubSwarmOptimization> subSwarms,
            ParticleSubSwarmOptimization mainSwarm) {
        ParticleSubSwarmOptimization borg = subSwarms.get(i);
        ParticleSubSwarmOptimization others = subSwarms.get(j);

        if (borg.isActive() && others.isActive()) {
            mergeActiveSubswarms(i, j, subSwarms, mainSwarm);
            return;
        }
        if (!borg.isActive() && !others.isActive()) {
            mergeInactiveSubswarms(i, j, subSwarms, mainSwarm);
            return;
        }
        System.out.print("ScatterMergingStrategy.mergeSubswarms problem: subswarms not of equal state.");
    }

    private void mergeInactiveSubswarms(
            int i,
            int j,
            Vector<ParticleSubSwarmOptimization> subSwarms,
            ParticleSubSwarmOptimization mainSwarm) {
        ParticleSubSwarmOptimization borg = subSwarms.get(i);
        ParticleSubSwarmOptimization others = subSwarms.get(j);

        borg.addPopulation(others);
        borg.populationSizeHasChanged();

        subSwarms.remove(j);
    }

    /**
     * @param i
     * @param j
     * @param subSwarms
     * @param mainSwarm
     * @tested subswarm j is reinited into the mainswarm and deleted, the function calls are added to the main swarm
     */
    private void mergeActiveSubswarms(
            int i,
            int j,
            Vector<ParticleSubSwarmOptimization> subSwarms,
            ParticleSubSwarmOptimization mainSwarm) {
        mainSwarm.reinitIndividuals(subSwarms.get(j).getPopulation().size()); //  add to the mainswarm
        //mainSwarm.populationSizeHasChanged(); // already called in addNewParticlesToPopulation
        // transfer functioncalls from the subswarm to the mainswarm before deleting it:
        int calls = subSwarms.get(j).getPopulation().getFunctionCalls();
        mainSwarm.getPopulation().incrFunctionCallsBy(calls);
        subSwarms.remove(j);
    }
}
