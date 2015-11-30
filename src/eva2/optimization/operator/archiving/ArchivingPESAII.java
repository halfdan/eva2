package eva2.optimization.operator.archiving;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;

/**
 * The Pareto envelope sorting algorithm using a hypergrid and
 * the so called squeeze factor.
 */
@Description("Pareto Envelope-based Selection Algorithm revision 2.0.")
public class ArchivingPESAII extends AbstractArchiving implements java.io.Serializable {

    private int gridSize = 4;

    public ArchivingPESAII() {
    }

    public ArchivingPESAII(ArchivingPESAII a) {
        this.gridSize = a.gridSize;
    }

    @Override
    public Object clone() {
        return new ArchivingPESAII(this);
    }

    /**
     * This method allows you to merge to populations into an archive.
     * This method will add elements from pop to the archive but will also
     * remove elements from the archive if the archive target size is exceeded.
     *
     * @param pop The population that may add Individuals to the archive.
     */
    @Override
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) {
            pop.SetArchive(new Population());
        }
        Population archive = pop.getArchive();

        // test for each element in population if it
        // is dominating a element in the archive
        for (int i = 0; i < pop.size(); i++) {
            if (this.isDominant(pop.get(i), pop.getArchive())) {
                this.addIndividualToArchive((AbstractEAIndividual) pop.get(i).clone(), pop.getArchive());
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////


        // Now check whether there are individuals to remove
        int bigSqueeze, index;
        int[] squeezeFactor;
        while (archive.targetSizeExceeded()) {
            squeezeFactor = this.calculateSqueezeFactor(archive);
            bigSqueeze = 0;
            index = -1;
            for (int i = 0; i < squeezeFactor.length; i++) {
                if (bigSqueeze < squeezeFactor[i]) {
                    bigSqueeze = squeezeFactor[i];
                    index = i;
                }
                if ((bigSqueeze == squeezeFactor[i]) && RNG.flipCoin(0.5)) {
                    index = i;
                }
            }
            archive.remove(index);
        }
    }

    /**
     * This method will calculate the squeeze factor for a population
     * and will return the squeeze factor
     *
     * @param pop The population.
     */
    public int[] calculateSqueezeFactor(Population pop) {
        int[] result = new int[pop.size()];
        double[][] bounds;
        double[] tmpFit;
        AbstractEAIndividual tmpIndy;

        // first calculate the bounds of the search space
        bounds = new double[pop.get(0).getFitness().length][2];
        for (int i = 0; i < bounds.length; i++) {
            bounds[i][0] = Double.POSITIVE_INFINITY;
            bounds[i][1] = Double.NEGATIVE_INFINITY;
        }

        for (int i = 0; i < pop.size(); i++) {
            tmpFit = pop.get(i).getFitness();
            result[i] = 0;
            for (int j = 0; j < tmpFit.length; j++) {
                if (tmpFit[j] < bounds[j][0]) {
                    bounds[j][0] = tmpFit[j];
                }
                if (tmpFit[j] > bounds[j][1]) {
                    bounds[j][1] = tmpFit[j];
                }
            }
        }

        // now that i got the bounds i can calculate the squeeze grid
        int[] curGrid, tmpGrid = new int[bounds.length];
        double[] grid = new double[bounds.length];
        ArrayList<Integer> coll;
        boolean sameGrid;
        for (int i = 0; i < pop.size(); i++) {
            if (result[i] == 0) {
                curGrid = new int[bounds.length];
                // haven't calculated the squeeze factor for this guy yet
                // first i'll calculate the grid position this guy is in
                tmpFit = pop.get(i).getFitness();
                coll = new ArrayList<>();
                for (int j = 0; j < tmpFit.length; j++) {
                    grid[j] = (bounds[j][1] - bounds[j][0]) / this.gridSize;
                    curGrid[j] = (int) ((tmpFit[j] - bounds[j][0]) / grid[j]);
                }
                coll.add(i);
                for (int j = i + 1; j < pop.size(); j++) {
                    if (result[j] == 0) {
                        // check whether this guy is in the same grid as the
                        // first guy...
                        tmpFit = pop.get(j).getFitness();
                        sameGrid = true;
                        for (int k = 0; k < tmpFit.length; k++) {
                            tmpGrid[k] = (int) ((tmpFit[k] - bounds[k][0]) / grid[k]);
                            sameGrid &= curGrid[k] == tmpGrid[k];
                        }
                        if (sameGrid) {
                            coll.add(j);
                        }
                    }
                }
                // now i got all the boogies of the same grid element
                // lets assign them their squeeze factor
                for (int j = 0; j < coll.size(); j++) {
                    result[coll.get(j)] = coll.size();
                    tmpIndy = pop.get(coll.get(j).intValue());
                    tmpIndy.putData("SqueezeFactor", coll.size());
                    tmpIndy.putData("GridBox", curGrid);
                }
            }
        }

        return result;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "PESA II";
    }

    /**
     * This method allows you to choose the grid width.
     *
     * @param b The new size of a grid element.
     */
    public void setGridSize(int b) {
        this.gridSize = b;
    }

    public int getGridSize() {
        return this.gridSize;
    }

    public String gridSizeTipText() {
        return "Choose the number of a grid elements per dimension.";
    }
}