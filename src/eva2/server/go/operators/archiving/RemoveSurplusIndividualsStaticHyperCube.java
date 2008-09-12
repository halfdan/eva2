package eva2.server.go.operators.archiving;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import wsi.ra.math.RNG;

/** This class removes surplus individuals based on bounding
 * hybercube, which can be calculated in objective or decision
 * space. But i guess currently you can't toggel that. But here
 * the hybercubes are static, e.g. theey are not recalculated after
 * an individual is removed.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.05.2004
 * Time: 14:43:15
 * To change this template use File | Settings | File Templates.
 */
public class RemoveSurplusIndividualsStaticHyperCube extends RemoveSurplusIndividualsDynamicHyperCube implements java.io.Serializable {

    public RemoveSurplusIndividualsStaticHyperCube() {
    }

    public RemoveSurplusIndividualsStaticHyperCube(RemoveSurplusIndividualsStaticHyperCube a) {
    }

    public Object clone() {
        return (Object) new RemoveSurplusIndividualsStaticHyperCube(this);
    }

    /** This method will remove surplus individuals
     * from a given archive. Note archive will be altered!
     * @param archive
     */
    public void removeSurplusIndividuals(Population archive) {
        double[][]  fitness;
        double[]    space;
        int         indexSmallHyperCube;
        double      smallestHyperCube, tmpS;

        fitness = new double[archive.size()][];
        space   = new double[archive.size()];
        for (int i = 0; i < archive.size(); i++) {
            fitness[i] = ((AbstractEAIndividual)archive.get(i)).getFitness();
        }
        space = this.calculateHyperCubeVolumes(fitness);
        for (int i = 0; i < archive.size(); i++) {
            ((AbstractEAIndividual)archive.get(i)).putData("HyperCube", new Double(space[i]));
        }

        while(archive.size() > archive.getPopulationSize()) {
            // select the individual with the least space around him
            // to do this i got to find the next smaller and the next bigger one
            smallestHyperCube   = ((Double)((AbstractEAIndividual)archive.get(0)).getData("HyperCube")).doubleValue();
            indexSmallHyperCube = 0;
            for (int i = 1; i < archive.size(); i++) {
                tmpS = ((Double)((AbstractEAIndividual)archive.get(i)).getData("HyperCube")).doubleValue();
                if (tmpS < smallestHyperCube) {
                    smallestHyperCube   = tmpS;
                    indexSmallHyperCube = i;
                } else {
                    // if they are equal give them a fair chance to exchange between them
                    if ((tmpS == smallestHyperCube) && (RNG.flipCoin(0.5))) {
                        smallestHyperCube   = tmpS;
                        indexSmallHyperCube = i;
                    }
                }
            }
            archive.remove(indexSmallHyperCube);
        }
    }
}
