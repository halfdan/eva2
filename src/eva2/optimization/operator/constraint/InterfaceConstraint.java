package eva2.optimization.operator.constraint;

import eva2.optimization.individuals.AbstractEAIndividual;

/**
 * This is a interface for area constraint for parallel MOEAs
 * giving area constraints on the separated parts for the Pareto front.
 */
public interface InterfaceConstraint {
    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    public Object clone();

    /**
     * This method allows you wether or not a given individual
     * violates the constraints.
     *
     * @param indy The individual to check.
     * @return true if valid false else.
     */
    public boolean isValid(AbstractEAIndividual indy);
}
