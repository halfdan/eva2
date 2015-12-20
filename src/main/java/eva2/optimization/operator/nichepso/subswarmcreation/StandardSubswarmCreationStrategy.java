package eva2.optimization.operator.nichepso.subswarmcreation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.NichePSO;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;
import eva2.util.annotation.Description;

/**
 * The standard deviation in the fitness of each main swarm particle over the last 3 iterations is calculated.
 * If this standard deviation falls below a given threshold delta it is assumed
 * that the particle is converging on an optimum and a subswarm is created with that particle and its topological neighbor.
 * The strategy is proposed in [1] suggesting a value of delta = 0.0001.
 * [1] R. Brits, A. P. Engelbrecht and B. Bergh.
 * A Niching Particle Swarm Optimizer
 * In Proceedings of the 4th Asia-Pacific Conference on Simulated Evolution and Learning (SEAL'02),
 * 2002, 2, 692-696
 */
@Description("Strategy to create subswarms from the main swarm")
public class StandardSubswarmCreationStrategy implements InterfaceSubswarmCreationStrategy, java.io.Serializable {

    protected double delta = 0.0001; //  "experimentally found to be effective" according to "a niching particle swarm optimizer" by Brits et al.

    public StandardSubswarmCreationStrategy(double theDelta) {
        delta = theDelta;
    }

    public StandardSubswarmCreationStrategy() {
        delta = 0.0001;
    }

    @Override
    public Object clone() {
        return new StandardSubswarmCreationStrategy(delta);
    }

    /**
     * @param indy main swarm particle
     * @return true if the stddev of the particles fitness &lt; delta and no constraints are violated
     */
    @Override
    public boolean shouldCreateSubswarm(AbstractEAIndividual indy, ParticleSubSwarmOptimization mainswarm) {
        if (createSubswarmConstraintViolation(indy, mainswarm)) {
            return false;
        }

        // check for stddev < delta condition
        double stddev = (Double) indy.getData(NichePSO.stdDevKey);
        return stddev < getDelta();

    }

    /**
     * @param indy main swarm particle
     * @return true, if reasons exist why no subswarm should be created from indy.
     *
     * Reasons like:
     * poor fitness (not implemented),
     * convergence on plateau (not implemented),
     * indy is the only particle in the mainswarm and therefor has no neighbor
     */
    public boolean createSubswarmConstraintViolation(AbstractEAIndividual indy, ParticleSubSwarmOptimization mainswarm) {
        boolean result = false;

        // check for MainSwarm-Size
        if (mainswarm.getPopulation().size() < 2) {
            //if (verbose) System.out.print("createSubswarmConstraintViolation: MainSwarm too small, no subswarm can be created\n");
            result = true;
        }

        return result;
    }


    /**
     * Creates a subswarm from the given particle and its neighbor in the mainswarm,
     * then deletes the two particles from the mainswarm.
     */
    @Override
    public void createSubswarm(ParticleSubSwarmOptimization preparedSubswarm, AbstractEAIndividual indy, ParticleSubSwarmOptimization mainSwarm) {

        // get the neighbor to create the subswarm
        AbstractEAIndividual neighbor = mainSwarm.getMemberNeighbor(indy);

        Population pop = new Population(2);
        pop.add(indy);
        pop.add(neighbor);
        preparedSubswarm.setPopulation(pop);
        preparedSubswarm.populationSizeHasChanged();

        // remove particles from the main swarm:
        mainSwarm.removeSubIndividual(indy);
        mainSwarm.removeSubIndividual(neighbor);
        mainSwarm.populationSizeHasChanged();
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public String deltaTipText() {
        return "threshold used to identify converging particles which lead to the creation of subswarms";
    }

}
