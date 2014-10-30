package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * The deterministic crowiding method replaces the most similar parent if better
 */
@Description("This method replaces the most similar parent if better.")
public class ReplaceDeterministicCrowding implements InterfaceReplacement, java.io.Serializable {

    PhenotypeMetric metric = new PhenotypeMetric();

    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new ReplaceRandom();
    }

    /**
     * Take the closest individual within the subset and remove it from pop. Add
     * indy as a replacement.
     *
     * @param indy The individual to insert
     * @param pop  The population
     * @param sub  The subset
     */
    @Override
    public void insertIndividual(AbstractEAIndividual indy, Population pop, Population sub) {
        int index = 0;

        double distance = Double.POSITIVE_INFINITY, tmpD;

        for (int i = 0; i < sub.size(); i++) {
            tmpD = this.metric.distance(indy, (AbstractEAIndividual) sub.get(i));
            if (tmpD < distance) {
                index = i;
                distance = tmpD;
            }
        }
        if (indy.isDominatingDebConstraints((AbstractEAIndividual) sub.get(index))) {
            if (pop.remove(sub.get(index))) {
                pop.addIndividual(indy);
            }
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Deterministic Crowding";
    }
}