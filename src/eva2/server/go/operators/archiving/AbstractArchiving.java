package eva2.server.go.operators.archiving;

import eva2.gui.*;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.chart2d.Chart2DDPointIconCross;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;

/** This is an abstract archiving strategy giving default implementation of determining
 * dominance, inserting individuals in exsisting Pareto front (removing now dominated solutions)
 * and some plot methods typically used for debugging.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.08.2003
 * Time: 17:00:44
 * To change this template use Options | File Templates.
 */
public abstract class AbstractArchiving implements InterfaceArchiving, java.io.Serializable {

    protected boolean                       m_Debug = false;
    transient protected eva2.gui.Plot    m_Plot = null;
    protected int                           p = 0;
    public boolean                          m_ObeyDebsConstViolationPrinciple = true;

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    @Override
    public abstract Object clone();

    /** This mehtod will test if a given individual is dominant within
     * a given population
     * @param indy          The individual that is to be tested.
     * @param pop           The population that the individual is to be tested against.
     * @return True if the individual is dominating
     */
    public boolean isDominant(AbstractEAIndividual indy, Population pop) {
        if (this.m_ObeyDebsConstViolationPrinciple) {
            for (int i = 0; i < pop.size(); i++) {
                if (!(indy.equals(pop.get(i))) && (((AbstractEAIndividual)pop.get(i)).isDominatingDebConstraints(indy))) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < pop.size(); i++) {
                if (!(indy.equals(pop.get(i))) && (((AbstractEAIndividual)pop.get(i)).isDominating(indy))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** This method will add a given Individual to the archive and will remove
     * all individuals that are dominated by the new individual.
     * @param indy          The individual that is to be tested.
     * @param archive       The population that the individual is to be inserted to.
     */
    public void addIndividualToArchive(AbstractEAIndividual indy, Population archive) {
        double[]            indyFitness = indy.getFitness(), tmpFitness;
        boolean             isDominating;

        for (int i = 0; i < archive.size(); i++) {
            isDominating = true;
            tmpFitness = ((AbstractEAIndividual)archive.get(i)).getFitness();
            try {
                for (int j = 0; j < indyFitness.length; j++) {
                    if (indyFitness[j] <= tmpFitness[j]) {
                        isDominating &= true;
                    }
                    else {
                        isDominating &= false;
                    }
                }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                //System.out.println("-------addIndividualToArchive-------("+indyFitness.length+"/"+tmpFitness.length+")");
            }
            if (isDominating) {
                archive.remove(i);
                i--;
            }
        }
        archive.add(indy);
    }

    /** This method allows you to plot a pareto front of a given population.
     * Here default icons will be used.
     * @param pop       The population to plot
     * @param plot      The plot to use
     **/
     public void plotParetoFront(Population pop, Plot plot) {
        DPointIcon[] icons = new DPointIcon[pop.size()];
        for (int i = 0; i < icons.length; i++) {
             icons[i] = new Chart2DDPointIconCross();
         }
        this.plotParetoFront(pop, icons, plot);
    }

    /** This method allows you to plot a pareto front of a given population
     * @param pop       The population to plot
     * @param plot      The plot to use
     * @param icons     The icons to use, perhaps with additional information
     */
    public void plotParetoFront(Population pop, DPointIcon[] icons, Plot plot) {
        GraphPointSet   mySet = new GraphPointSet(10, plot.getFunctionArea());
        DPoint          myPoint;
        AbstractEAIndividual tmpIndy;
        double[][]      tmpFit;
        int             index;
        double          curVal;
        double[]        lastValue = null, tmpD;

        mySet.setConnectedMode(false);
        this.p++;
        mySet = new GraphPointSet(10000+p, plot.getFunctionArea());
        mySet.setConnectedMode(false);
        lastValue = null;
        // first prepare the tmpFit
        tmpFit = new double[pop.size()][];
        for (int j = 0; j < pop.size(); j++) {
            tmpIndy = ((AbstractEAIndividual)pop.get(j));
            if (tmpIndy.getFitness().length <= 1) {
                tmpD = (double[])tmpIndy.getData("MOFitness");
                tmpFit[j] = new double[tmpD.length];
                System.arraycopy(tmpD, 0, tmpFit[j], 0, tmpD.length);
            } else {
                tmpFit[j] = new double[tmpIndy.getFitness().length];
                System.arraycopy(tmpIndy.getFitness(), 0, tmpFit[j], 0, tmpIndy.getFitness().length);
            }
        }
        // now plot
        for (int j = 0; j < pop.size(); j++) {
            index = -1;
            curVal = Double.POSITIVE_INFINITY;
            for (int n = 0; n < tmpFit.length; n++) {
                if (tmpFit[n][0] < curVal) {
                    index = n;
                    curVal = tmpFit[n][0];
                }
            }
            myPoint = new DPoint(tmpFit[index][0], tmpFit[index][1]);
            if (lastValue != null) {
                plot.setConnectedPoint(lastValue[0], lastValue[1], 20000+p);
                plot.setConnectedPoint(tmpFit[index][0], lastValue[1], 20000+p);
                plot.setConnectedPoint(tmpFit[index][0], tmpFit[index][1], 20000+p);
            }
            lastValue = new double[tmpFit[index].length];
            System.arraycopy(tmpFit[index], 0, lastValue, 0, lastValue.length);
            tmpFit[index][0] = Double.POSITIVE_INFINITY;
            myPoint.setIcon(icons[index]);
            mySet.addDPoint(myPoint);
       }
       if (lastValue != null) {
            plot.setConnectedPoint(lastValue[0], lastValue[1], 20000+p);
        }
        p++;
    }

/**********************************************************************************************************************
 * These are for GUI
 */

//    /** This method allows you to toggle the debug mode.
//     * @param b     True in case of the debug mode.
//     */
//    public void setDebugFront(boolean b) {
//        this.m_Debug = b;
//    }
//    public boolean getDebugFront() {
//        return this.m_Debug;
//    }
//    public String debugFrontTipText() {
//        return "Toggles the debug mode.";
//    }

    /** Toggle the use of obeying the constraint violation principle
     * of Deb
     * @param b     The new state
     */
    public void setObeyDebsConstViolationPrinciple(boolean b) {
        this.m_ObeyDebsConstViolationPrinciple = b;
    }
    public boolean getObeyDebsConstViolationPrinciple() {
        return this.m_ObeyDebsConstViolationPrinciple;
    }
    public String obeyDebsConstViolationPrincipleToolTip() {
        return "Toggle the use of Deb's coonstraint violation principle.";
    }
}
