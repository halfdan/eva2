package eva2.optimization.operators.constraint;

import eva2.optimization.individuals.AbstractEAIndividual;

/** This is a interface for area constraint for parallel MOEAs
 * giving area constraints on the separated parts for the Pareto front.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.09.2004
 * Time: 19:23:02
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceConstraint {
    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone();

    /** This method allows you wether or not a given individual
     * violates the constraints.
     * @param indy  The individual to check.
     * @return true if valid false else.
     */
    public boolean isValid(AbstractEAIndividual indy);
}
