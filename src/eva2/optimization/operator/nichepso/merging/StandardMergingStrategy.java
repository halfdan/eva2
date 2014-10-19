package eva2.optimization.operator.nichepso.merging;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.operator.distancemetric.EuclideanMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;

import java.util.Vector;

/**
 * Two subswarms are merged if their radii overlap.
 * In case both radii equal zero the subswarms are merged if their distance is below a given threshold mu.
 * This strategy is proposed in [1] and a small value, such as mu = 0.001, is suggested.
 * [1] R. Brits, A. P. Engelbrecht and B. Bergh.
 * A Niching Particle Swarm Optimizer
 * In Proceedings of the 4th Asia-Pacific Conference on Simulated Evolution and Learning (SEAL'02),
 * 2002, 2, 692-696
 */
public class StandardMergingStrategy implements InterfaceMergingStrategy, java.io.Serializable {

    private double mu = 0.001; // "experimentally found to be effective" according to "a niching particle swarm optimizer" by Brits et al.

    public String globalInfo() {
        return "Strategy to merge subswarms";
    }

    /**
     * *******************************************************************************************************************
     * ctors
     */
    public StandardMergingStrategy() {

    }

    public StandardMergingStrategy(double theMu) {
        mu = theMu;
    }

    public StandardMergingStrategy(StandardMergingStrategy other) {
        this.mu = other.mu;
    }

    @Override
    public Object clone() {
        return new StandardMergingStrategy(this);
    }

/**********************************************************************************************************************
 * shouldMergeSubswarms
 */
    /**
     * @tested the subswarms are merged, if they overlap (or are very close) and if they are of equal state
     * (non-Javadoc) @see javaeva.server.oa.go.Operators.NichePSO.InterfaceMergingStrategie#shouldMergeSubswarms(javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization, javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization)
     */
    @Override
    public boolean shouldMergeSubswarms(ParticleSubSwarmOptimization subswarm1, ParticleSubSwarmOptimization subswarm2) {
        // check for equal state
        if (subswarm1.isActive() && !subswarm2.isActive()) {
            return false;
        }
        if (!subswarm1.isActive() && subswarm2.isActive()) {
            return false;
        }

        if (!subswarmsOverlapOrAreVeryClose(subswarm1, subswarm2)) {
            return false;
        }


        return true;
    }

    private boolean subswarmsOverlapOrAreVeryClose(ParticleSubSwarmOptimization subswarm1, ParticleSubSwarmOptimization subswarm2) {
        // check if they overlap
        AbstractEAIndividual gbesti = subswarm1.getGBestIndividual();
        AbstractEAIndividual gbestj = subswarm2.getGBestIndividual();
        InterfaceESIndividual i1 = null, i2 = null;

        double dist;
        if (gbesti instanceof InterfaceESIndividual) {
            i1 = (InterfaceESIndividual) gbesti;
            i2 = (InterfaceESIndividual) gbestj;
        }

        if (i1 != null) {
            dist = EuclideanMetric.euclideanDistance(i1.getDGenotype(), i2.getDGenotype());
        } else {
            dist = subswarm1.distance(gbesti, gbestj);
        } // euclidean distance
//		System.out.println("dist is " + dist);

        if (dist < (subswarm1.getMaxAllowedSwarmRadiusAbs() + subswarm2.getMaxAllowedSwarmRadiusAbs())) {
            // only then is the next (expensive) test feasible
            double Ri = subswarm1.getBoundSwarmRadius(); // uses euclidean distance
            double Rj = subswarm2.getBoundSwarmRadius(); // uses euclidean distance

            if (dist < Ri + Rj) { // all in euclidean metric
                return true;
            }
        }

        // check if they are "very close"
        double dist_norm;
        if (i1 != null) {
            dist_norm = EuclideanMetric.normedEuclideanDistance(i1.getDGenotype(), i1.getDoubleRange(),
                    i2.getDGenotype(), i2.getDoubleRange());
        } else {
            dist_norm = PhenotypeMetric.dist(gbesti, gbestj);
        } // normalised distance

        //if (Ri == 0 && Rj == 0 && dist_norm < getEpsilon()){ // see "Enhancing the NichePSO" paper
        if (dist_norm < getMu()) { // Ri und Rj auf null testen sinvoll ?
            return true;
        }
        return false;
    }

/**********************************************************************************************************************
 * mergeSubswarms
 */

    /**
     * @param i
     * @param j
     * @tested junit
     * adds population of subswarm j to population of subswarm i, then deletes subswarm j.
     */
    @Override
    public void mergeSubswarms(
            int i,
            int j,
            Vector<ParticleSubSwarmOptimization> subSwarms,
            ParticleSubSwarmOptimization mainSwarm) {
        ParticleSubSwarmOptimization borg = subSwarms.get(i);
        ParticleSubSwarmOptimization others = subSwarms.get(j);
//		System.out.println("merging " + (borg.isActive() ? " active " : " inactive ") + " with " + (others.isActive() ? "active" : "inactive"));
        borg.addPopulation(others);
        borg.populationSizeHasChanged();

        subSwarms.remove(j); // ok: function calls added to borg swarm...
    }

    /**
     * *******************************************************************************************************************
     * getter, setter
     */

    public void setMu(double mu) {
        this.mu = mu;
    }

    public double getMu() {
        return mu;
    }

    public String muTipText() {
        return "threshold used to merge subswarms that lie very close but have no spatial extent";
    }
}
