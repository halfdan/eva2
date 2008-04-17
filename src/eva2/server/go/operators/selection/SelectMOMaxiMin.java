package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.moso.MOSOMaxiMin;
import eva2.server.go.populations.Population;
import eva2.server.go.tools.RandomNumberGenerator;

/** A multi-objective selection criterion based on the maximin
 * method.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 09.08.2004
 * Time: 18:24:24
 * To change this template use File | Settings | File Templates.
 */
public class SelectMOMaxiMin implements InterfaceSelection, java.io.Serializable {

    private MOSOMaxiMin             m_MaxiMin   = new MOSOMaxiMin();
    private InterfaceSelection      m_Selection = new SelectBestIndividuals();
    private boolean                 m_ObeyDebsConstViolationPrinciple = true;

    public SelectMOMaxiMin() {
    }

    public SelectMOMaxiMin(SelectMOMaxiMin a) {
        this.m_MaxiMin              = new MOSOMaxiMin();
        this.m_Selection            = (InterfaceSelection)a.m_Selection.clone();
        this.m_ObeyDebsConstViolationPrinciple  = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectMOMaxiMin(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    public void prepareSelection(Population population) {
        // nothing to prepare here
    }

    /** This method will select one Indiviudal from the given
     * Population in respect to the selection propability of the
     * individual.
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        Population              result = new Population(), tmpPop = (Population)population.clone();
        double[]                tmpD;
        // Now calculate the MaxiMin Criterium

        this.m_MaxiMin.convertMultiObjective2SingleObjective(tmpPop);
        this.m_Selection.setObeyDebsConstViolationPrinciple(this.m_ObeyDebsConstViolationPrinciple);
        this.m_Selection.prepareSelection(tmpPop);
        result = this.m_Selection.selectFrom(tmpPop, size);

        // now unconvert from SO to MO
        for (int i = 0; i < result.size(); i++) {
            tmpD = (double[])((AbstractEAIndividual)result.get(i)).getData("MOFitness");
            ((AbstractEAIndividual)result.get(i)).SetFitness(tmpD);
        }
        return result;
    }

    /** This method allows you to select partners for a given Individual
     * @param dad               The already seleceted parent
     * @param avaiablePartners  The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    public Population findPartnerFor(AbstractEAIndividual dad, Population avaiablePartners, int size) {
        return this.selectFrom(avaiablePartners, size);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This selection method will use the MaxiMin criteria to select individuals (use SelectBestIndividuals).";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "MaxiMin Selection";
    }

    /** Since SelectMOMaxiMin relies on a MOSO conversion
     * a single criteria selection method can be used.
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

    /** Toggel the use of obeying the constraint violation principle
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
