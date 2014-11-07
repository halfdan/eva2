package eva2.optimization.operator.archiving;

import eva2.optimization.operator.moso.MOSOMaxiMin;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectBestIndividuals;
import eva2.optimization.population.Population;
import eva2.tools.chart2d.Chart2DDPointIconCross;
import eva2.tools.chart2d.DPointIcon;
import eva2.util.annotation.Description;


/**
 * Another eva2.problems.simple archiving strategy not based on dominance but on the MaxiMin
 * criterion. Doesn't work well on non-convex Pareto fronts.
 */
@Description("Maxi Min Archiving.")
public class ArchivingMaxiMin implements InterfaceArchiving, java.io.Serializable {

    private MOSOMaxiMin maxiMin = new MOSOMaxiMin();
    private InterfaceSelection selection = new SelectBestIndividuals();
    private boolean obeyDebsConstViolationPrinciple = true;

    public ArchivingMaxiMin() {
    }

    public ArchivingMaxiMin(ArchivingMaxiMin a) {
        this.maxiMin = new MOSOMaxiMin();
        this.selection = (InterfaceSelection) a.selection.clone();
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new ArchivingMaxiMin(this);
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
        Population archive;
        double[] tmpD;
        if (pop.getArchive() == null) {
            pop.SetArchive(new Population());
        }

        // First merge the current population and the archive
        Population tmpPop = new Population();
        tmpPop.addPopulation((Population) pop.getClone());
        tmpPop.addPopulation((Population) pop.getArchive().getClone());
        tmpPop.removeRedundantIndiesUsingFitness();

        // Now calculate the MaxiMin Criterium
        this.maxiMin.convertMultiObjective2SingleObjective(tmpPop);
        this.selection.setObeyDebsConstViolationPrinciple(this.obeyDebsConstViolationPrinciple);
        this.selection.prepareSelection(tmpPop);
        archive = this.selection.selectFrom(tmpPop, pop.getArchive().getTargetSize());
        archive.setTargetSize(pop.getArchive().getTargetSize());

        // now unconvert from SO to MO
        for (int i = 0; i < archive.size(); i++) {
            tmpD = (double[]) archive.get(i).getData("MOFitness");
            archive.get(i).setFitness(tmpD);
        }

        pop.SetArchive(archive);
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

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "MaxiMin";
    }

    /**
     * Since SelectMOMaxiMin relies on a MOSO conversion
     * a single criterion selection method must be used.
     *
     * @param pop The selection method used.
     */
    public void setSelectionMethod(InterfaceSelection pop) {
        this.selection = pop;
    }

    public InterfaceSelection getSelectionMethod() {
        return this.selection;
    }

    public String selectionMethodTipText() {
        return "Choose the selection method (single-criteria ones please).";
    }

    /**
     * Toggle the use of obeying the constraint violation principle
     * of Deb
     *
     * @param b The new state
     */
    public void setObeyDebsConstViolationPrinciple(boolean b) {
        this.obeyDebsConstViolationPrinciple = b;
    }

    public boolean getObeyDebsConstViolationPrinciple() {
        return this.obeyDebsConstViolationPrinciple;
    }

    public String obeyDebsConstViolationPrincipleToolTip() {
        return "Toggle the use of Deb's coonstraint violation principle.";
    }
}