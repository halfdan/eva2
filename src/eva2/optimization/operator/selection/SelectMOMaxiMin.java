package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.moso.MOSOMaxiMin;
import eva2.optimization.population.Population;

/**
 * A multi-objective selection criterion based on the maximin
 * method.
 */
public class SelectMOMaxiMin implements InterfaceSelection, java.io.Serializable {

    private MOSOMaxiMin maxiMin = new MOSOMaxiMin();
    private InterfaceSelection selection = new SelectBestIndividuals();
    private boolean obeyDebsConstViolationPrinciple = true;

    public SelectMOMaxiMin() {
    }

    public SelectMOMaxiMin(SelectMOMaxiMin a) {
        this.maxiMin = new MOSOMaxiMin();
        this.selection = (InterfaceSelection) a.selection.clone();
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return (Object) new SelectMOMaxiMin(this);
    }

    /**
     * This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     *
     * @param population The population that is to be processed.
     */
    @Override
    public void prepareSelection(Population population) {
        // nothing to prepare here
    }

    /**
     * This method will select one Individual from the given
     * Population in respect to the selection propability of the
     * individual.
     *
     * @param population The source population where to select from
     * @param size       The number of Individuals to select
     * @return The selected population.
     */
    @Override
    public Population selectFrom(Population population, int size) {
        Population result = new Population(), tmpPop = (Population) population.clone();
        double[] tmpD;
        // Now calculate the MaxiMin Criterium

        this.maxiMin.convertMultiObjective2SingleObjective(tmpPop);
        this.selection.setObeyDebsConstViolationPrinciple(this.obeyDebsConstViolationPrinciple);
        this.selection.prepareSelection(tmpPop);
        result = this.selection.selectFrom(tmpPop, size);

        // now unconvert from SO to MO
        for (int i = 0; i < result.size(); i++) {
            tmpD = (double[]) ((AbstractEAIndividual) result.get(i)).getData("MOFitness");
            ((AbstractEAIndividual) result.get(i)).setFitness(tmpD);
        }
        return result;
    }

    /**
     * This method allows you to select partners for a given Individual
     *
     * @param dad              The already seleceted parent
     * @param avaiablePartners The mating pool.
     * @param size             The number of partners needed.
     * @return The selected partners.
     */
    @Override
    public Population findPartnerFor(AbstractEAIndividual dad, Population avaiablePartners, int size) {
        return this.selectFrom(avaiablePartners, size);
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
        return "This selection method will use the MaxiMin criteria to select individuals (use SelectBestIndividuals).";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "MaxiMin Selection";
    }

    /**
     * Since SelectMOMaxiMin relies on a MOSO conversion
     * a single criterion selection method can be used.
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
    @Override
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
