package eva2.optimization.operator.archiving;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.chart2d.Chart2DDPointIconCross;
import eva2.tools.chart2d.DPointIcon;

import java.util.ArrayList;


/**
 * The secon verison of the non dominace sorting GA.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.08.2003
 * Time: 16:56:10
 * To change this template use Options | File Templates.
 */
public class ArchivingNSGAII extends ArchivingNSGA implements java.io.Serializable {

    public ArchivingNSGAII() {
    }

    public ArchivingNSGAII(ArchivingNSGAII a) {
    }

    @Override
    public Object clone() {
        return new ArchivingNSGAII(this);
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

        // First merge the current population and the archive
        Population tmpPop = new Population();
        tmpPop.addPopulation((Population) pop.getClone());
        tmpPop.addPopulation((Population) pop.getArchive().getClone());
        tmpPop.removeRedundantIndiesUsingFitness();

        // Now fetch the n pareto-fronts
        Population[] fronts = this.getNonDominatedSortedFronts(tmpPop);
        tmpPop.clear();
        tmpPop = null;
//        this.calculateCrowdingDistance(fronts);

        // Now initialize the new archive
        Population archive = new Population();
        archive.setTargetSize(pop.getArchive().getTargetSize());

        // Now add the fronts to the archive
        int index = 0;
        while ((index < fronts.length) && ((archive.size() + fronts[index].size()) < archive.getTargetSize())) {
            archive.addPopulation(fronts[index]);
            index++;
        }

        if ((index < fronts.length) && (!archive.targetSizeReached())) {
            // In this case there are still elements left in the front which could be added to the archive!
            // and there is still some place left in the archive
            // therefore we could add some individuals from front[index]
            // to the archive using the crowding distance sorting
            fronts[index].setTargetSize(archive.getTargetSize() - archive.size());
            this.cleaner.removeSurplusIndividuals(fronts[index]);
            archive.addPopulation(fronts[index]);
        }
        for (int i = 0; i < fronts.length; i++) {
            fronts[i].clear();
            fronts[i] = null;
        }
        fronts = null;
        pop.SetArchive(archive);
    }

    /**
     * Return the pareto front from a given population.
     *
     * @param pop
     * @return
     */
    public static Population getNonDominatedSortedFront(Population pop) {
        ArchivingNSGAII arch = new ArchivingNSGAII();
        Population[] fronts = arch.getNonDominatedSortedFronts(pop);
        return fronts[0];
    }

    /**
     * This method will dissect a given population into n pareto-fronts
     *
     * @param pop The population to analyse
     * @return Population[] the n pareto-fronts
     */
    public Population[] getNonDominatedSortedFronts(Population pop) {
        Population tmpPop, tmpDom, tmpNonDom;
        Population[] result = null;
        ArrayList tmpResult = new ArrayList();
        int level = 1;

        tmpPop = new Population();
        tmpPop.addPopulation(pop);

        while (tmpPop.size() > 0) {
            tmpDom = new Population();
            tmpNonDom = new Population();
            for (int i = 0; i < tmpPop.size(); i++) {
                if (this.isDominant((AbstractEAIndividual) tmpPop.get(i), tmpPop)) {
                    ((AbstractEAIndividual) tmpPop.get(i)).putData("ParetoLevel", new Integer(level));
                    tmpDom.add(tmpPop.get(i));

                } else {
                    tmpNonDom.add(tmpPop.get(i));
                }
            }
            tmpPop = tmpNonDom;
            if (tmpDom.size() < 1) {
                System.out.println("Problem NSGA II at level " + level + ".");
                tmpDom.addPopulation(tmpNonDom);
                for (int i = 0; i < tmpDom.size(); i++) {
                    ((AbstractEAIndividual) tmpDom.get(i)).putData("ParetoLevel", new Integer(level));
                }
                tmpPop.clear();
//                System.out.println(""+tmpPop.getStringRepresentation());
//                tmpPop.removeDoubleInstancesUsingFitness();
            }
            tmpResult.add(tmpDom);
            level++;
        }
        result = new Population[tmpResult.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Population) tmpResult.get(i);
        }
        return result;
    }

    /**
     * This method will cacluated the NSGAII crowding distance
     * for all individuals
     *
     * @param fronts The pareto fronts
     */
    public void calculateCrowdingDistance(Population[] fronts) {
        RemoveSurplusIndividualsDynamicHyperCube heidi = new RemoveSurplusIndividualsDynamicHyperCube();
        double[][] fitness;
        double[] hyperCube;
        for (int i = 0; i < fronts.length; i++) {
            fitness = new double[fronts[i].size()][];
            hyperCube = new double[fronts[i].size()];
            for (int j = 0; j < fronts[i].size(); j++) {
                fitness[j] = ((AbstractEAIndividual) fronts[i].get(j)).getFitness();
            }
            hyperCube = heidi.calculateHyperCubeVolumes(fitness);
            for (int j = 0; j < fronts[i].size(); j++) {
                ((AbstractEAIndividual) fronts[i].get(j)).putData("HyperCube", new Double(hyperCube[j]));
            }
        }
    }

    /**
     * This method allows you to determine an icon for a given individual
     *
     * @param pop   The population
     * @param index The identifier for the individual
     */
    public DPointIcon getIconFor(int index, Population pop) {
        return new Chart2DDPointIconCross();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Non-dominating sorting GA revision 2.0.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "NSGA II";
    }

}