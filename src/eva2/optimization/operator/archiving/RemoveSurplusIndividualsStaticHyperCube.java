package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

/**
 * This class removes surplus individuals based on bounding
 * hybercube, which can be calculated in objective or decision
 * space. But i guess currently you can't toggel that. But here
 * the hybercubes are static, e.g. theey are not recalculated after
 * an individual is removed.
 */
public class RemoveSurplusIndividualsStaticHyperCube extends RemoveSurplusIndividualsDynamicHyperCube implements java.io.Serializable {

    public RemoveSurplusIndividualsStaticHyperCube() {
    }

    public RemoveSurplusIndividualsStaticHyperCube(RemoveSurplusIndividualsStaticHyperCube a) {
    }

    @Override
    public Object clone() {
        return new RemoveSurplusIndividualsStaticHyperCube(this);
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
        double smallestHyperCube, tmpS;

        fitness = new double[archive.size()][];
        space = new double[archive.size()];
        for (int i = 0; i < archive.size(); i++) {
            fitness[i] = archive.get(i).getFitness();
        }
        space = this.calculateHyperCubeVolumes(fitness);
        for (int i = 0; i < archive.size(); i++) {
            archive.get(i).putData("HyperCube", space[i]);
        }

        while (archive.targetSizeExceeded()) {
            // select the individual with the least space around him
            // to do this i got to find the next smaller and the next bigger one
            smallestHyperCube = (Double) archive.get(0).getData("HyperCube");
            indexSmallHyperCube = 0;
            for (int i = 1; i < archive.size(); i++) {
                tmpS = (Double) archive.get(i).getData("HyperCube");
                if (tmpS < smallestHyperCube) {
                    smallestHyperCube = tmpS;
                    indexSmallHyperCube = i;
                } else {
                    // if they are equal give them a fair chance to exchange between them
                    if ((tmpS == smallestHyperCube) && (RNG.flipCoin(0.5))) {
                        smallestHyperCube = tmpS;
                        indexSmallHyperCube = i;
                    }
                }
            }
            archive.remove(indexSmallHyperCube);
        }
    }
}
