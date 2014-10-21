package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;


/**
 * Selection methods have to implement on the one hand the
 * selection of the mother and on the other hand the selection
 * for the fathers (eventually more than one). This allows
 * niching method based on mating restrion, which is currently
 * not implemented. All selection method should obey Deb's constraint
 * handling principle, first select feasible, only if all are infeasible
 * select the individuals with the smallest constraint violation.
 */
public interface InterfaceSelection {

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    public Object clone();

    /**
     * This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     *
     * @param population The population that is to be processed.
     */
    public void prepareSelection(Population population);

    /**
     * This method will select >size< individuals from the given
     * Population.
     *
     * @param population The source population where to select from
     * @param size       The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size);

    /**
     * This method allows you to select >size< partners for a given Individual
     *
     * @param dad               The already seleceted parent
     * @param availablePartners The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size);

    /**
     * Toggle the use of obeying the constraint violation principle
     * of Deb
     *
     * @param b The new state
     */
    public void setObeyDebsConstViolationPrinciple(boolean b);
}
