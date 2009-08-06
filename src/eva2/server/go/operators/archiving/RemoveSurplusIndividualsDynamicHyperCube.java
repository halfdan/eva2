package eva2.server.go.operators.archiving;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.math.RNG;

/** This class removes surplus individuals based on bounding
 * hybercube, which can be calculated in objective or decision
 * space. But i guess currently you can't toggel that. But here
 * the hybercubes are dynamic, e.g. theey are recalculated after
 * an individual is removed.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.05.2004
 * Time: 14:36:54
 * To change this template use File | Settings | File Templates.
 */
public class RemoveSurplusIndividualsDynamicHyperCube implements InterfaceRemoveSurplusIndividuals, java.io.Serializable {

    public RemoveSurplusIndividualsDynamicHyperCube() {
    }

    public RemoveSurplusIndividualsDynamicHyperCube(RemoveSurplusIndividualsDynamicHyperCube a) {
    }

    public Object clone() {
        return (Object) new RemoveSurplusIndividualsDynamicHyperCube(this);
    }

    /** This method will remove surplus individuals
     * from a given archive. Note archive will be altered!
     * @param archive
     */
    public void removeSurplusIndividuals(Population archive) {
        double[][]  fitness;
        double[]    space;
        int         indexSmallHyperCube;
        while(archive.size() > archive.getPopulationSize()) {
            // select the individual with the least space around him
            // to do this i got to find the next smaller and the next bigger one
            fitness = new double[archive.size()][];
            space   = new double[archive.size()];
            for (int i = 0; i < archive.size(); i++) {
                fitness[i] = ((AbstractEAIndividual)archive.get(i)).getFitness();
            }
            space = this.calculateHyperCubeVolumes(fitness);
            // now find the individual with the smallest hypervolume
            indexSmallHyperCube = 0;
            for (int i = 1; i < archive.size(); i++) {
                if (space[i] < space[indexSmallHyperCube]) indexSmallHyperCube = i;
                else {
                    // if they are equal give them a fair chance to exchange between them
                    if ((space[i] == space[indexSmallHyperCube]) && (RNG.flipCoin(0.5))) {
                        indexSmallHyperCube = i;
                    }
               }
            }
            archive.remove(indexSmallHyperCube);
        }
    }

    /** This method calculates the surrounding hypervolumes for a given array of fitness values
     * @param fitness   The array of multi dimensional fitness values.
     * @return An array of hypercube volumes,
     */
    public double[] calculateHyperCubeVolumes(double[][] fitness) {
        double[]    result = new double[fitness.length];
        int         upperI, lowerI;
        double      upperX, lowerX;

        for (int i = 0; i < fitness.length; i++) {
            result[i] = 1;
            for (int y = 0; y < fitness[i].length; y++) {
                upperI = -1;
                lowerI = -1;
                upperX = Double.POSITIVE_INFINITY;
                lowerX = Double.POSITIVE_INFINITY;
                // now find the boundary in this dimension
                // overall elements in the fitness array
                for (int j = 0; j < fitness.length; j++) {
                    if (i != j) {
                        // the lower bound
                        if ((fitness[j][y] <= fitness[i][y]) && (Math.abs(fitness[j][y] - fitness[i][y]) < lowerX)) {
                            lowerX = Math.abs(fitness[j][y] - fitness[i][y]);
                            lowerI = j;
                        }
                        // the upper bound
                        if ((fitness[j][y] >= fitness[i][y]) && (Math.abs(fitness[j][y] - fitness[i][y]) < upperX)) {
                            upperX = Math.abs(fitness[j][y] - fitness[i][y]);
                            upperI = j;
                        }
                    }
                }
                // now i should have the lower and the upperbound
                if ((upperI == -1) || (lowerI == -1)) result[i] *= Double.POSITIVE_INFINITY;
                else result[i] *= Math.abs(Math.abs(fitness[upperI][y] - fitness[lowerI][y]));
            }
        }
        return result;
    }
}
