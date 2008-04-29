package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import wsi.ra.math.RNG;

/** Simple method to selecet all.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event. pff
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 31.03.2004
 * Time: 15:08:53
 * To change this template use File | Settings | File Templates.
 */
public class SelectAll implements InterfaceSelection, java.io.Serializable {

    private boolean     m_ObeyDebsConstViolationPrinciple = true;

    public SelectAll() {
    }

    public SelectAll(SelectAll a) {
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectAll(this);
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

    /** This method will select one Individual from the given
     * Population in respect to the selection propability of the
     * individual.
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        Population result = new Population();
        result.setPopulationSize(size);
        if (this.m_ObeyDebsConstViolationPrinciple) {
            int index = 0;
            while (result.size() < size) {
                if (!((AbstractEAIndividual)population.get(index%population.size())).violatesConstraint()) {
                        result.add(population.get(index%population.size()));
                }
                index++;
                if ((index >= size) && (result.size() == 0)) {
                    // darn no one is feasible
                    for (int i = 0; i < size; i++) {
                        result.add(population.get(i%population.size()));
                    }
                }
            }

        } else {
            for (int i = 0; i < size; i++) {
                result.add(population.get(i%population.size()));
            }
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
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "All Selection";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method selects all individuals.";
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
