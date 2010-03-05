package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.math.RNG;

/** This multi-objective selection method preferrs non-dominated
 * individuals over dominated ones. Actually, this fails in case
 * all individuals are Pareto optimal.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.07.2003
 * Time: 14:17:53
 * To change this template use Options | File Templates.
 */
public class SelectMONonDominated implements InterfaceSelection, java.io.Serializable {

    private boolean     m_ObeyDebsConstViolationPrinciple = true;

    public SelectMONonDominated() {
    }

    public SelectMONonDominated(SelectMONonDominated a) {
        this.m_ObeyDebsConstViolationPrinciple  = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectMONonDominated(this);
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
        Population              result = new Population();
        int                     index = RNG.randomInt(0, population.size()-1);

        if (this.m_ObeyDebsConstViolationPrinciple) {
            boolean feasible = false;
            for (int i = 0; i < population.size(); i++) {
                if (((AbstractEAIndividual)population.get(i)).getConstraintViolation() == 0) feasible = true;
            }
            if (feasible) {
                while (result.size() < size) {
                    int tmpI = index%population.size();
                    if (tmpI < 0) {
                        System.out.println("Index:      " + index);
                        System.out.println("Pop.Size(): " + population.size());
                        tmpI = 0;
                    }
                    if ((((AbstractEAIndividual)population.get(tmpI)).getConstraintViolation() == 0) && (this.isDominant((AbstractEAIndividual)population.get(tmpI), population))) {
                        result.addIndividual((AbstractEAIndividual)population.get(index%population.size()));
                    }
                    index++;
                }
            } else {
                SelectBestIndividuals select = new SelectBestIndividuals();
                result = select.selectFrom(population, size);
            }
        } else {
            while (result.size() < size) {
                int tmpI = index%population.size();
                if (tmpI < 0) {
                    System.out.println("Index:      " + index);
                    System.out.println("Pop.Size(): " + population.size());
                    tmpI = 0;
                }
                if (this.isDominant((AbstractEAIndividual)population.get(tmpI), population)) {
                    result.addIndividual((AbstractEAIndividual)population.get(index%population.size()));
                }
                index++;
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

    /** This mehtod will test if a given individual is dominant within
     * a given population
     * @param indy          The individual that is to be tested.
     * @param pop           The population that the individual is to be tested against.
     * @return True if the individual is dominating
     */
    public boolean isDominant(AbstractEAIndividual indy, Population pop) {
        if (this.m_ObeyDebsConstViolationPrinciple) {
            for (int i = 0; i < pop.size(); i++) {
                if (!(indy.equals(pop.get(i))) && (((AbstractEAIndividual)pop.get(i)).isDominatingDebConstraintsEqual(indy))) return false;
            }
        } else {
            for (int i = 0; i < pop.size(); i++) {
                if (!(indy.equals(pop.get(i))) && (((AbstractEAIndividual)pop.get(i)).isDominatingEqual(indy))) return false;
            }
        }
        return true;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This selection method will select all non-dominated individuals. Therefore the target size of the selection may be exceeded.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Non-Dominated Selection";
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
