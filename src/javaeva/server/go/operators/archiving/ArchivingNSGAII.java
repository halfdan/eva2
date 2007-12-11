package javaeva.server.go.operators.archiving;

import javaeva.server.go.IndividualInterface;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.populations.Population;
import javaeva.server.go.tools.RandomNumberGenerator;
import javaeva.gui.*;

import java.util.ArrayList;

import wsi.ra.chart2d.DPoint;
import wsi.ra.chart2d.DPointIcon;

/** The secon verison of the non dominace sorting GA.
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

    public Object clone() {
        return (Object) new ArchivingNSGAII(this);
    }

    /** This method allows you to merge to populations into an archive.
     *  This method will add elements from pop to the archive but will also
     *  remove elements from the archive if the archive target size is exceeded.
     * @param pop       The population that may add Individuals to the archive.
     */
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) pop.SetArchive(new Population());

        //////////////////////////////////////////////////////////////////////////////////////////////
        if (this.m_Debug && false) {
            // plot the complete population
            double[] tmpD = new double[2];
            tmpD[0] = 0;
            tmpD[1] = 0;
            this.m_Plot = new javaeva.gui.Plot("Debug NSGAII", "Y1", "Y2", tmpD, tmpD);
            System.out.println("Population size: " + pop.size());
            // plot the population
            this.m_Plot.setUnconnectedPoint(0, 0, 11);
            this.m_Plot.setUnconnectedPoint(1.05, 2.5, 11);
            double[][] trueFitness = new double[pop.size()][];
            for (int i = 0; i < pop.size(); i++) {
                trueFitness[i]  = ((AbstractEAIndividual)pop.get(i)).getFitness();
                this.m_Plot.setUnconnectedPoint(trueFitness[i][0], trueFitness[i][1], 11);
            }
        }
        //////////////////////////////////////////////////////////////////////////////////////////////

        // First merge the current population and the archive
        Population tmpPop = new Population();
        tmpPop.addPopulation((Population)pop.getClone());
        tmpPop.addPopulation((Population)pop.getArchive().getClone());
        tmpPop.removeDoubleInstancesUsingFitness();

        // Now fetch the n pareto-fronts
        Population[] fronts = this.getNonDomiatedSortedFronts(tmpPop);
        tmpPop.clear();
        tmpPop = null;
//        this.calculateCrowdingDistance(fronts);

        // Now init the new archive
        Population archive = new Population();
        archive.setPopulationSize(pop.getArchive().getPopulationSize());

        // Now add the fronts to the archive
        int index = 0;
        while ((index < fronts.length) && ((archive.size() + fronts[index].size()) < archive.getPopulationSize())) {
            archive.addPopulation(fronts[index]);
            index++;
        }

        if ((index < fronts.length) && (archive.size() < archive.getPopulationSize())) {
            // In this case there are still elements left in the front which could be added to the archive!
            // and there is still some place left in the archive
            // therefore we could add some individuals from front[index]
            // to the archive using the crowding distance sorting
            fronts[index].setPopulationSize(archive.getPopulationSize() - archive.size());
            this.m_Cleaner.removeSurplusIndividuals(fronts[index]);
            archive.addPopulation(fronts[index]);
        }
        for (int i = 0; i < fronts.length; i++) {
            fronts[i].clear();
            fronts[i] = null;
        }
        fronts = null;
        pop.SetArchive(archive);
    }

    /** This method will dissect a given populaiton into n pareto-fronts
     * @param pop   The population to analyse
     * @return Population[] the n pareto-fronts
     */
    public Population[] getNonDomiatedSortedFronts(Population pop) {
        Population      tmpPop, tmpDom, tmpNonDom;
        Population[]    result  = null;
        ArrayList       tmpResult = new ArrayList();
        int             level   = 1;

        tmpPop = new Population();
        tmpPop.addPopulation(pop);

        while (tmpPop.size() > 0) {
            tmpDom      = new Population();
            tmpNonDom   = new Population();
            for (int i = 0; i < tmpPop.size(); i++) {
                if (this.isDominant((AbstractEAIndividual) tmpPop.get(i), tmpPop)) {
                    ((AbstractEAIndividual)tmpPop.get(i)).SetData("ParetoLevel", new Integer(level));
                    tmpDom.add(tmpPop.get(i));

                } else {
                    tmpNonDom.add(tmpPop.get(i));
                }
            }
            tmpPop      = tmpNonDom;
            if (tmpDom.size() < 1) {
                System.out.println("Problem NSGA II at level " + level + ".");
                tmpDom.addPopulation(tmpNonDom);
                for (int i = 0; i < tmpDom.size(); i++)
                    ((AbstractEAIndividual)tmpDom.get(i)).SetData("ParetoLevel", new Integer(level));
                tmpPop.clear();
//                System.out.println(""+tmpPop.getStringRepresentation());
//                tmpPop.removeDoubleInstancesUsingFitness();
            }
            tmpResult.add(tmpDom);
            level++;
        }
        result = new Population[tmpResult.size()];
        for (int i = 0; i < result.length; i++) result[i] = (Population) tmpResult.get(i);
        return result;
    }
    /** This method will cacluated the NSGAII crowding distance
     * for all individuals
     * @param fronts    The pareto fronts
     */
    public void calculateCrowdingDistance(Population[] fronts) {
        RemoveSurplusIndividualsDynamicHyperCube heidi = new RemoveSurplusIndividualsDynamicHyperCube();
        double[][]  fitness;
        double[]    hyperCube;
        for (int i = 0; i < fronts.length; i++) {
            fitness     = new double[fronts[i].size()][];
            hyperCube   = new double[fronts[i].size()];
            for (int j = 0; j < fronts[i].size(); j++) {
                fitness[j] = ((AbstractEAIndividual)fronts[i].get(j)).getFitness();
            }
            hyperCube = heidi.calculateHyperCubeVolumes(fitness);
            for (int j = 0; j < fronts[i].size(); j++) {
                ((AbstractEAIndividual)fronts[i].get(j)).SetData("HyperCube", new Double(hyperCube[j]));
            }
        }
    }

    /** This method allows you to determine an icon for a given individual
     * @param pop   The population
     * @param index The identifier for the individual
     */
    public DPointIcon getIconFor(int index, Population pop) {
        return new Chart2DDPointIconCross();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Non-dominating sorting GA revision 2.0.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "NSGA II";
    }

}