package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.operator.selection.SelectRandom;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * This crowding method replaces the most similar individual from a random group if better.
 */
@Description("This method replaces the most similar individual from a random group if better.")
public class ReplacementCrowding implements InterfaceReplacement, java.io.Serializable {

    PhenotypeMetric metric = new PhenotypeMetric();
    SelectRandom random = new SelectRandom();
    int C = 5;

    public ReplacementCrowding() {

    }

    public ReplacementCrowding(ReplacementCrowding b) {
        this.metric = new PhenotypeMetric();
        this.random = new SelectRandom();
        this.C = b.C;
    }

    public ReplacementCrowding(int C) {
        setC(C);
    }

    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new ReplaceRandom();
    }

    /**
     * From a random subset of size C, the closest is replaced by the given individual.
     * The sub parameter is not regarded.
     *
     * @param indy The individual to insert
     * @param pop  The population
     * @param sub  The subset
     */
    @Override
    public void insertIndividual(AbstractEAIndividual indy, Population pop, Population sub) {
        int index = 0;

        double distance = Double.POSITIVE_INFINITY, tmpD;
        Population tmp = random.selectFrom(pop, this.C);
        for (int i = 0; i < tmp.size(); i++) {
            tmpD = this.metric.distance(indy, tmp.get(i));
            if (tmpD < distance) {
                index = i;
                distance = tmpD;
            }
        }
        if (indy.isDominatingDebConstraints(tmp.get(index))) {
            if (pop.remove(tmp.get(index))) {
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
        return "Crowding";
    }

    /**
     * This method will set c
     *
     * @param c
     */
    public void setC(int c) {
        this.C = c;
    }

    public int getC() {
        return this.C;
    }

    public String cTipText() {
        return "Set the crwoding factor.";
    }
}