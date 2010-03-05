package eva2.server.go.operators.archiving;


import java.util.ArrayList;
import java.awt.*;

import eva2.gui.*;
import eva2.server.go.IndividualInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DLine;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.math.RNG;

/** The Pareto envelope sorting algorithm using a hybergrid and
 * the so called squezze factor.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.04.2004
 * Time: 14:24:54
 * To change this template use File | Settings | File Templates.
 */
public class ArchivingPESAII extends AbstractArchiving implements java.io.Serializable {

    private int         m_GridSize = 4;

    public ArchivingPESAII() {
    }

    public ArchivingPESAII(ArchivingPESAII a) {
        this.m_GridSize     = a.m_GridSize;
    }

    public Object clone() {
        return (Object) new ArchivingPESAII(this);
    }

    /** This method allows you to merge to populations into an archive.
     *  This method will add elements from pop to the archive but will also
     *  remove elements from the archive if the archive target size is exceeded.
     * @param pop       The population that may add Individuals to the archive.
     */
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) pop.SetArchive(new Population());
        Population  archive = pop.getArchive();

        ////////////////////////////////////////////////////////////////////////////////////
        if (this.m_Debug) {
            this.m_Plot = new eva2.gui.Plot("Debug SPEAII", "Y1", "Y2");
            System.out.println("Population size: " + pop.size());
            // plot the population
            this.m_Plot.setUnconnectedPoint(0, 0, 11);
            this.m_Plot.setUnconnectedPoint(1.2, 2.0, 11);
            Population tmpPop = new Population();
            tmpPop.addPopulation(pop);
            tmpPop.addPopulation(pop.getArchive());
            double[][]      trueFitness;
            GraphPointSet   mySet = new GraphPointSet(10, this.m_Plot.getFunctionArea());
            DPoint          myPoint;
            Chart2DDPointIconText tmp;
            trueFitness = new double[archive.size()][];
            for (int i = 0; i < archive.size(); i++) {
                trueFitness[i] = ((AbstractEAIndividual)archive.get(i)).getFitness();
            }
            mySet.setConnectedMode(false);
            for (int i = 0; i < trueFitness.length; i++) {
                myPoint = new DPoint(trueFitness[i][0], trueFitness[i][1]);
                tmp = new Chart2DDPointIconText("");
                tmp.setIcon(new Chart2DDPointIconCircle());
                myPoint.setIcon(tmp);
                mySet.addDPoint(myPoint);
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////


        // test for each element in population if it
        // is dominating a element in the archive
        for (int i = 0; i < pop.size(); i++) {
            if (this.isDominant((AbstractEAIndividual)pop.get(i), pop.getArchive())) {
                this.addIndividualToArchive((AbstractEAIndividual)((AbstractEAIndividual)pop.get(i)).clone(), pop.getArchive());
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////
        if (this.m_Debug) {
            // first plot the grid
            double[][]  bounds;
            double[][]  trueFitness = new double[archive.size()][];

            // first calculate the bounds of the search space
            bounds = new double[((AbstractEAIndividual)archive.get(0)).getFitness().length][2];
            for (int i = 0; i < bounds.length; i++) {
                bounds[i][0] = Double.POSITIVE_INFINITY;
                bounds[i][1] = Double.NEGATIVE_INFINITY;
            }
            for (int i = 0; i < archive.size(); i++) {
                trueFitness[i] = ((AbstractEAIndividual)archive.get(i)).getFitness();
                for (int j = 0; j < trueFitness[i].length; j++) {
                    if (trueFitness[i][j] < bounds[j][0]) bounds[j][0] = trueFitness[i][j];
                    if (trueFitness[i][j] > bounds[j][1]) bounds[j][1] = trueFitness[i][j];
                }
            }
            double  gridx, gridy;
            DLine   line;
            gridx = (bounds[0][1] - bounds[0][0])/this.m_GridSize;
            gridy = (bounds[1][1] - bounds[1][0])/this.m_GridSize;
            for (int i = 0; i <= this.m_GridSize; i++) {
                line = new DLine(bounds[0][0]+gridx*i, bounds[1][0], bounds[0][0]+gridx*i, bounds[1][1], Color.BLUE);
                this.m_Plot.getFunctionArea().addDElement(line);
                line = new DLine(bounds[0][0], bounds[1][0]+gridy*i, bounds[0][1], bounds[1][0]+gridy*i, Color.BLUE);
                this.m_Plot.getFunctionArea().addDElement(line);
            }

            // now plot the archive with squeezing factor
            int[]           sqFactor   = this.calculateSqueezeFactor(archive);
            GraphPointSet   mySet = new GraphPointSet(10, this.m_Plot.getFunctionArea());
            DPoint          myPoint;
            Chart2DDPointIconText       tmp;
            Chart2DDPointIconCircle     tmp2;
            mySet.setConnectedMode(false);
            for (int i = 0; i < trueFitness.length; i++) {
                myPoint = new DPoint(trueFitness[i][0], trueFitness[i][1]);
                tmp = new Chart2DDPointIconText("SF:"+sqFactor[i]);
                tmp2 = new Chart2DDPointIconCircle();
                tmp2.setFillColor(Color.GREEN);
                tmp.setIcon(tmp2);
                myPoint.setIcon(tmp);
                mySet.addDPoint(myPoint);
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////

        // Now check whether there are individuals to remove
        int         bigSqueeze, index;
        int[]       squeezeFactor;
        while(archive.targetSizeExceeded()) {
            squeezeFactor   = this.calculateSqueezeFactor(archive);
            bigSqueeze = 0;
            index = -1;
            for (int i = 0; i < squeezeFactor.length; i++) {
                if (bigSqueeze < squeezeFactor[i]) {
                    bigSqueeze = squeezeFactor[i];
                    index = i;
                }
                if ((bigSqueeze == squeezeFactor[i]) && RNG.flipCoin(0.5)) index = i;
            }
            archive.remove(index);
        }
        ////////////////////////////////////////////////////////////////////////////////////
        if (this.m_Debug) {
            double[][] trueFitness;
            GraphPointSet   mySet = new GraphPointSet(10, this.m_Plot.getFunctionArea());
            DPoint          myPoint;
            Chart2DDPointIconCircle tmp;
            trueFitness = new double[archive.size()][];
            for (int i = 0; i < archive.size(); i++) {
                trueFitness[i] = ((AbstractEAIndividual)archive.get(i)).getFitness();
            }
            mySet.setConnectedMode(false);
            for (int i = 0; i < trueFitness.length; i++) {
                myPoint = new DPoint(trueFitness[i][0], trueFitness[i][1]);
                tmp = new Chart2DDPointIconCircle();
                tmp.setFillColor(Color.RED);
                myPoint.setIcon(tmp);
                mySet.addDPoint(myPoint);
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////

    }

    /** This method will calculate the squeeze factor for a population
     * and will return the squeeze factor
     * @param pop   The population.
     *
     */
    public int[] calculateSqueezeFactor(Population pop) {
        int[]       result = new int[pop.size()];
        double[][]  bounds;
        double[]    tmpFit;
        AbstractEAIndividual tmpIndy;
//        boolean     debug = true;

        // first calculate the bounds of the search space
        bounds = new double[((AbstractEAIndividual)pop.get(0)).getFitness().length][2];
        for (int i = 0; i < bounds.length; i++) {
            bounds[i][0] = Double.POSITIVE_INFINITY;
            bounds[i][1] = Double.NEGATIVE_INFINITY;
        }
//        if (debug) System.out.println("The individuals:");
        for (int i = 0; i < pop.size(); i++) {
            tmpFit      = ((AbstractEAIndividual)pop.get(i)).getFitness();
//            if (debug) System.out.println("Individual "+i+": "+tmpFit[0] +"/"+tmpFit[1]);
            result[i]   = 0;
            for (int j = 0; j < tmpFit.length; j++) {
                if (tmpFit[j] < bounds[j][0]) bounds[j][0] = tmpFit[j];
                if (tmpFit[j] > bounds[j][1]) bounds[j][1] = tmpFit[j];
            }
        }
//        if (debug) {
//            System.out.println("The bounds are ("+bounds[0][0]+"/"+bounds[0][1]+")("+bounds[1][0]+"/"+bounds[1][1]+")");
//            System.out.println("Gridwidth is "+((bounds[0][1] - bounds[0][0])/this.m_GridSize)+"/"+((bounds[1][1] - bounds[1][0])/this.m_GridSize));
//        }

        // now that i got the bounds i can calculate the squeeze grid
        int[]       curGrid = new int[bounds.length], tmpGrid = new int[bounds.length];
        double[]    grid = new double[bounds.length];
        ArrayList   coll;
        boolean     sameGrid;
        for (int i = 0; i < pop.size(); i++) {
            if (result[i] == 0) {
                curGrid = new int[bounds.length];
                // haven't calculated the squeeze factor for this guy yet
                // first i'll calculate the grid position this guy is in
                tmpFit = ((AbstractEAIndividual)pop.get(i)).getFitness();
                coll = new ArrayList();
                for (int j = 0; j < tmpFit.length; j++) {
                    grid[j] = (bounds[j][1] - bounds[j][0])/this.m_GridSize;
                    curGrid[j] = (int)((tmpFit[j]-bounds[j][0])/grid[j]);
                }
//                if (debug) {
//                    System.out.println("Indy "+i+" ("+tmpFit[0] +"/"+tmpFit[1]+") unassigned is in grid ["+curGrid[0]+"/"+curGrid[1]+"]");
//                    System.out.println("");
//                    System.out.println("Checking for individuals in the same grid");
//                }
                coll.add(new Integer(i));
                for (int j = i+1; j < pop.size(); j++) {
                    if (result[j] == 0) {
                        // check whether this guy is in the same grid as the
                        // first guy...
                        tmpFit = ((AbstractEAIndividual)pop.get(j)).getFitness();
                        sameGrid = true;
                        for (int k = 0; k < tmpFit.length; k++) {
                            tmpGrid[k] = (int)((tmpFit[k]-bounds[k][0])/grid[k]);
                            if (curGrid[k] == tmpGrid[k]) sameGrid &= true;
                            else sameGrid &= false;
                        }
                        if (sameGrid) coll.add(new Integer(j));
//                        if (debug) {
//                            System.out.println("Checking indy "+j+" ("+tmpFit[0] +"/"+tmpFit[1]+") in grid ["+tmpGrid[0]+"/"+tmpGrid[1]+"]");
//                        }
                    }
                }
                // now i got all the boogies of the same grid element
                // lets assign them thier squeeze factor
                for (int j = 0; j < coll.size(); j++) {
                    result[((Integer)coll.get(j)).intValue()] = coll.size();
                    tmpIndy = (AbstractEAIndividual)pop.get(((Integer)coll.get(j)).intValue());
                    tmpIndy.putData("SqueezeFactor", new Integer(coll.size()));
                    tmpIndy.putData("GridBox", curGrid);
                }
            }
        }

        return result;
    }


/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Pareto Envelope-based Selection Algorithm revision 2.0.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "PESA II";
    }

    /** This method allows you to choose the grid width.
     * @param b     The new size of a grid element.
     */
    public void setGridSize(int b) {
        this.m_GridSize = b;
    }
    public int getGridSize() {
        return this.m_GridSize;
    }
    public String gridSizeTipText() {
        return "Choose the number of a grid elements per dimension.";
    }
}