package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * Simple method to select all.
 * In case of multiple fitness values the selection
 * criteria is selected randomly for each selection event.
 */
@Description("This method selects all individuals.")
public class SelectAll implements InterfaceSelection, java.io.Serializable {

    private boolean obeyDebsConstViolationPrinciple = true;

    public SelectAll() {
    }

    public SelectAll(SelectAll a) {
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new SelectAll(this);
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
        Population result = new Population();
        result.setTargetSize(size);
        if (this.obeyDebsConstViolationPrinciple) {
            int index = 0;
            while (result.size() < size) {
                if (!((AbstractEAIndividual) population.get(index % population.size())).violatesConstraint()) {
                    result.add(population.get(index % population.size()));
                }
                index++;
                if ((index >= size) && (result.size() == 0)) {
                    // darn no one is feasible
                    for (int i = 0; i < size; i++) {
                        result.add(population.get(i % population.size()));
                    }
                }
            }

        } else {
            for (int i = 0; i < size; i++) {
                result.add(population.get(i % population.size()));
            }
        }
        return result;
    }

    /**
     * This method allows you to select partners for a given Individual
     *
     * @param dad              The already seleceted parent
     * @param availablePartners The mating pool.
     * @param size             The number of partners needed.
     * @return The selected partners.
     */
    @Override
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size) {
        return this.selectFrom(availablePartners, size);
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "All Selection";
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
