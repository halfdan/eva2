package eva2.optimization.operator.archiving;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

/**
 * This class removes surplus individuals based on bounding
 * hybercube, which can be calculated in objective or decision
 * space. But i guess currently you can't toggel that. But here
 * the hybercubes are dynamic, e.g. theey are recalculated after
 * an individual is removed.
 */
public class RemoveSurplusIndividualsSMetric implements InterfaceRemoveSurplusIndividuals, java.io.Serializable {

    public RemoveSurplusIndividualsSMetric() {
    }

    public RemoveSurplusIndividualsSMetric(RemoveSurplusIndividualsSMetric a) {
    }

    @Override
    public Object clone() {
        return new RemoveSurplusIndividualsSMetric(this);
    }

    /**
     * This method will remove surplus individuals
     * from a given archive. Note archive will be altered!
     *
     * @param archive
     */
    @Override
    public void removeSurplusIndividuals(Population archive) {
        double[][] fitness;
        double[] space;
        int indexSmallHyperCube;
        while (archive.targetSizeExceeded()) {
            // select the individual with the least space around him
            // to do this i got to find the next smaller and the next bigger one
            fitness = new double[archive.size()][];
            space = new double[archive.size()];
            for (int i = 0; i < archive.size(); i++) {
                fitness[i] = ((AbstractEAIndividual) archive.get(i)).getFitness();
            }
            space = this.calculateContributingHypervolume(fitness);
            // now find the individual with the smallest hypervolume
            indexSmallHyperCube = 0;
            for (int i = 1; i < archive.size(); i++) {
                if (space[i] < space[indexSmallHyperCube]) {
                    indexSmallHyperCube = i;
                } else {
                    // if they are equal give them a fair chance to exchange between them
                    if ((space[i] == space[indexSmallHyperCube]) && (RNG.flipCoin(0.5))) {
                        indexSmallHyperCube = i;
                    }
                }
            }
            archive.remove(indexSmallHyperCube);
        }
    }

    public double[] calculateContributingHypervolume(double[][] fitness) {


        int counter;
        // Vector< Integer > sort=new Vector<Integer>();
        // Vector< Integer > global=new Vector<Integer>();
        // Vector< Boolean > assigned=new Vector<Boolean>();
        int size = fitness.length;//TODO size bestimmen (anzahl individuen?)
        double result[] = new double[size];
        int[] sort = new int[size];
        int[] global = new int[size];
        boolean[] assigned = new boolean[size];
        double temp1;
        double temp2;
        double v;
        int i, left, right;


        // Initialize sharing values
        // setMOOShare(0.0);


        // gather individuals with the same rank into a sub population
        // PopulationMOO subpop( numberOfMOORank( rank ) );
        counter = 0;
        for (i = 0; i < fitness.length; i++) {
            global[counter] = i;
            counter++;
        }

        // sort according to 1st objective
        int obj = 0;
        // initialization of index vector
        for (i = 0; i < size; i++) {
            sort[i] = i;
            assigned[i] = false;
        }
        // sort
        boolean changed;


        changed = false;
        for (i = 0; i < counter - 1; i++) {

            temp1 = fitness[global[sort[i]]][obj];
            temp2 = fitness[global[sort[i + 1]]][obj];

            if (temp1 > temp2) {
                int temp = sort[i];
                sort[i] = sort[i + 1];
                sort[i + 1] = temp;
                changed = true;
            }
        }

        while (changed) {
            changed = false;
            for (i = 0; i < counter - 1; i++) {

                temp1 = fitness[global[sort[i]]][obj];
                temp2 = fitness[global[sort[i + 1]]][obj];

                if (temp1 > temp2) {
                    int temp = sort[i];
                    sort[i] = sort[i + 1];
                    sort[i + 1] = temp;
                    changed = true;
                }
            }
        }


        result[global[sort[0]]] = Double.MAX_VALUE; //die beiden aussen bekommen maximal wert als smeasure
        result[global[sort[counter - 1]]] = Double.MAX_VALUE;

        for (int e = 1; e < counter - 1; e++) { // loop over all non-border elements
            for (i = 1; (assigned[sort[i]]); i++) ; // determine 1st not assigned, non-border element
            for (left = 0; i < counter - 1; ) {   // loop over all not assigned elements
                // determine right not assigned neighbor
                for (right = i + 1; (assigned[sort[right]]); right++) ;


                v = (fitness[global[sort[right]]][0] -
                        fitness[global[sort[i]]][0]) *
                        (fitness[global[sort[left]]][1] -
                                fitness[global[sort[i]]][1]);

                result[global[sort[i]]] = v;

                left = i;
                i = right;
            }
            int minIndex = 0;
            double min = result[global[sort[minIndex]]];
            for (int f = 1; f < counter - 1; f++) {
                if (!assigned[sort[f]]) {
                    if (result[global[sort[f]]] < min) {
                        min = result[global[sort[f]]];
                        minIndex = f;
                    }
                }
            }
            assigned[sort[minIndex]] = true;
            result[global[sort[minIndex]]] = e;
        }
        return result;
    }

}