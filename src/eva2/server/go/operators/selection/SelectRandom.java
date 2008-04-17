package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.tools.RandomNumberGenerator;


/** Random selection typically used for ES a mating selection.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event. pff
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.03.2003
 * Time: 11:36:00
 * To change this template use Options | File Templates.
 */
public class SelectRandom implements InterfaceSelection, java.io.Serializable {

    private boolean     m_ObeyDebsConstViolationPrinciple = false;

    public SelectRandom() {
    }

    public SelectRandom(SelectRandom a) {
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectRandom(this);
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
        Population result = new Population();
        result.setPopulationSize(size);
        if (this.m_ObeyDebsConstViolationPrinciple) {
            int index = 0, rand;
            while (result.size() < size) {
                rand = RandomNumberGenerator.randomInt(0, population.size()-1);
                if (!((AbstractEAIndividual)population.get(rand)).violatesConstraint())
                    result.add(population.get(rand));
                index++;
                if ((index > 0) && (result.size() == 0 )) {
                    // darn there seems to be no feasible solution
                    // just select random one instead
                    for (int i = 0; i < size; i++) {
                        result.add(population.get(RandomNumberGenerator.randomInt(0, population.size()-1)));
                    }
                }
            }

        } else {
            for (int i = 0; i < size; i++) {
                result.add(population.get(RandomNumberGenerator.randomInt(0, population.size()-1)));
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
        return "Random Selection";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method selects randomly. Therefore, it even works fine on Multiobjective fitness cases.";
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
