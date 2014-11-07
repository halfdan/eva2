package eva2.optimization.operator.archiving;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.EAIndividualComparator;
import eva2.optimization.population.Population;

import java.util.Arrays;

public class ArchivingNSGAIISMeasure extends ArchivingNSGAII {

    /**
     * This method will cacluated the NSGAII crowding distance
     * for all individuals
     *
     * @param fronts The pareto fronts
     */
    @Override
    public void calculateCrowdingDistance(Population[] fronts) {
        //TODO Dimension der Zielfunktion checken

        for (int rank = 0; rank < fronts.length; rank++) {
            calculateCrowdingDistance(fronts[rank]);
        }
    }


    /**
     * This mehtod will test if a given individual is dominant within
     * a given population
     *
     * @param indy The individual that is to be tested.
     * @param pop  The population that the individual is to be tested against.
     * @return True if the individual is dominating
     */
    @Override
    public boolean isDominant(AbstractEAIndividual indy, Population pop) {
        if (this.obeyDebsConstViolationPrinciple) {
            for (int i = 0; i < pop.size(); i++) {
                if (!(indy.equals(pop.get(i)) || indy.equalFitness(pop.get(i))) && (pop.get(i).isDominatingDebConstraints(indy))) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < pop.size(); i++) {
                if (!(indy.equals(pop.get(i)) || indy.equalFitness(pop.get(i))) && (pop.get(i).isDominating(indy))) {
                    return false;
                }
            }
        }
        return true;
    }

    public void calculateCrowdingDistance(Population front) {

        Object[] frontArray = front.toArray();
        boolean[] assigned = new boolean[frontArray.length];

        double[] v = new double[frontArray.length];
        int i, left, right;

        // initialization of assignment vector
        for (i = 0; i < frontArray.length; i++) {
            assigned[i] = false;

        }


        Arrays.sort(frontArray, new EAIndividualComparator(0));


        ((AbstractEAIndividual) frontArray[0]).putData("HyperCube", Double.MAX_VALUE); //die beiden aussen bekommen maximal wert als smeasure
        ((AbstractEAIndividual) frontArray[frontArray.length - 1]).putData("HyperCube", Double.MAX_VALUE);
        v[0] = Double.MAX_VALUE;
        v[frontArray.length - 1] = Double.MAX_VALUE;


        for (int e = 1; e < frontArray.length - 1; e++) { // loop over all non-border elements
            for (i = 1; (assigned[i]); i++) ; // determine 1st not assigned, non-border element

            for (left = 0; i < frontArray.length - 1; ) {   // loop over all not assigned elements
                // determine right not assigned neighbor
                for (right = i + 1; (assigned[right]); right++) ;

                v[i] = (((AbstractEAIndividual) frontArray[right]).getFitness(0) - ((AbstractEAIndividual) frontArray[i]).getFitness(0)) *
                        (((AbstractEAIndividual) frontArray[left]).getFitness(1) - ((AbstractEAIndividual) frontArray[i]).getFitness(1));

                left = i;
                i = right;
            }

            int minIndex = 0;
            double min = v[minIndex];
            for (int f = 1; f < frontArray.length - 1; f++) {
                if (!assigned[f]) {
                    if (v[f] < min) {
                        min = v[f];
                        minIndex = f;
                    }
                }
            }
            assigned[minIndex] = true;
            ((AbstractEAIndividual) frontArray[minIndex]).putData("HyperCube", (double) e);
        }


    }
}
