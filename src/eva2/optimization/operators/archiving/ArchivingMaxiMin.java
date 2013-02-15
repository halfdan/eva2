package eva2.optimization.operators.archiving;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operators.moso.MOSOMaxiMin;
import eva2.optimization.operators.selection.InterfaceSelection;
import eva2.optimization.operators.selection.SelectBestIndividuals;
import eva2.optimization.populations.Population;
import eva2.tools.chart2d.Chart2DDPointIconCross;
import eva2.tools.chart2d.DPointIcon;


/** Another simple archiving strategy not based on dominance but on the MaxiMin
 * criterion. Doesn't work well on non-convex Pareto fronts.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 09.08.2004
 * Time: 17:59:26
 * To change this template use File | Settings | File Templates.
 */
public class ArchivingMaxiMin implements InterfaceArchiving, java.io.Serializable {

    private MOSOMaxiMin             m_MaxiMin   = new MOSOMaxiMin();
    private InterfaceSelection      m_Selection = new SelectBestIndividuals();
    private boolean                 m_ObeyDebsConstViolationPrinciple = true;

    public ArchivingMaxiMin() {
    }

    public ArchivingMaxiMin(ArchivingMaxiMin a) {
        this.m_MaxiMin      = new MOSOMaxiMin();
        this.m_Selection    = (InterfaceSelection)a.m_Selection.clone();
        this.m_ObeyDebsConstViolationPrinciple   = a.m_ObeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return (Object) new ArchivingMaxiMin(this);
    }

    /** This method allows you to merge to populations into an archive.
     *  This method will add elements from pop to the archive but will also
     *  remove elements from the archive if the archive target size is exceeded.
     * @param pop       The population that may add Individuals to the archive.
     */
    @Override
    public void addElementsToArchive(Population pop) {
        Population      archive;
        double[]        tmpD;
        if (pop.getArchive() == null) {
            pop.SetArchive(new Population());
        }

        // First merge the current population and the archive
        Population tmpPop = new Population();
        tmpPop.addPopulation((Population)pop.getClone());
        tmpPop.addPopulation((Population)pop.getArchive().getClone());
        tmpPop.removeRedundantIndiesUsingFitness();

        // Now calculate the MaxiMin Criterium
        this.m_MaxiMin.convertMultiObjective2SingleObjective(tmpPop);
        this.m_Selection.setObeyDebsConstViolationPrinciple(this.m_ObeyDebsConstViolationPrinciple);
        this.m_Selection.prepareSelection(tmpPop);
        archive = this.m_Selection.selectFrom(tmpPop, pop.getArchive().getTargetSize());
        archive.setTargetSize(pop.getArchive().getTargetSize());

        // now unconvert from SO to MO
        for (int i = 0; i < archive.size(); i++) {
            tmpD = (double[])((AbstractEAIndividual)archive.get(i)).getData("MOFitness");
            ((AbstractEAIndividual)archive.get(i)).setFitness(tmpD);
        }

        pop.SetArchive(archive);
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
    public static String globalInfo() {
        return "Maxi Min Archiving.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "MaxiMin";
    }

    /** Since SelectMOMaxiMin relies on a MOSO conversion
     * a single criterion selection method must be used.
     * @param pop     The selection method used.
     */
    public void setSelectionMethod(InterfaceSelection pop) {
        this.m_Selection = pop;
    }
    public InterfaceSelection getSelectionMethod() {
        return this.m_Selection;
    }
    public String selectionMethodTipText() {
        return "Choose the selection method (single-criteria ones please).";
    }

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
//extends AbstractArchiving implements java.io.Serializable {
//
//    private MOSOMaxiMin             m_MaxiMin   = new MOSOMaxiMin();
//    private InterfaceSelection      m_Selection = new SelectBestIndividuals();
//
//    public ArchivingMaxiMin() {
//    }
//
//    public ArchivingMaxiMin(ArchivingMaxiMin a) {
//        this.m_MaxiMin      = new MOSOMaxiMin();
//        this.m_Selection    = (InterfaceSelection)a.m_Selection.clone();
//    }
//
//    public Object clone() {
//        return (Object) new ArchivingMaxiMin(this);
//    }
//
//    /** This method allows you to merge to populations into an archive.
//     *  This method will add elements from pop to the archive but will also
//     *  remove elements from the archive if the archive target size is exceeded.
//     * @param pop       The population that may add Individuals to the archive.
//     */
//    public void addElementsToArchive(Population pop) {
//        Population      archive;
//        double[]        tmpD;
//        if (pop.getArchive() == null) pop.SetArchive(new Population());
//
//        // First merge the current population and the archive
//        Population tmpPop = new Population();
//        tmpPop.addPopulation((Population)pop.getClone());
//        tmpPop.addPopulation((Population)pop.getArchive().getClone());
//        tmpPop.removeDoubleInstancesUsingFitness();
//
//        // Now calculate the MaxiMin Criterium
//        this.m_MaxiMin.convertMultiObjective2SingleObjective(tmpPop);
//        this.m_Selection.prepareSelection(tmpPop);
//        archive = this.m_Selection.selectFrom(tmpPop, pop.getArchive().getPopulationSize());
//        archive.setPopulationSize(pop.getArchive().getPopulationSize());
//
//        // now unconvert from SO to MO
//        for (int i = 0; i < archive.size(); i++) {
//            tmpD = (double[])((AbstractEAIndividual)archive.get(i)).getData("MOFitness");
//            ((AbstractEAIndividual)archive.get(i)).SetFitness(tmpD);
//        }
//
//        pop.SetArchive(archive);
//    }
//
//    /** This method allows you to determine an icon for a given individual
//     * @param pop   The population
//     * @param index The identifier for the individual
//     */
//    public DPointIcon getIconFor(int index, Population pop) {
//        return new Chart2DDPointIconCross();
//    }
//
///**********************************************************************************************************************
// * These are for GUI
// */
//    /** This method returns a global info string
//     * @return description
//     */
//    public static String globalInfo() {
//        return "Maxi Min Archiving.";
//    }
//    /** This method will return a naming String
//     * @return The name of the algorithm
//     */
//    public String getName() {
//        return "MaxiMin";
//    }
//
//    /** Since SelectMOMaxiMin relies on a MOSO conversion
//     * a single criterion selection method can be used.
//     * @param pop     The selection method used.
//     */
//    public void setSelectionMethod(InterfaceSelection pop) {
//        this.m_Selection = pop;
//    }
//    public InterfaceSelection getSelectionMethod() {
//        return this.m_Selection;
//    }
//    public String selectionMethodTipText() {
//        return "Choose the selection method (single-criteria ones please).";
//    }
//}