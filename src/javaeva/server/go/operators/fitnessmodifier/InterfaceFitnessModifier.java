package javaeva.server.go.operators.fitnessmodifier;

import javaeva.server.go.populations.Population;

/** The fitness modifier are defunct and are to be moved to
 * the selection operators...
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.03.2004
 * Time: 17:08:17
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceFitnessModifier {

    /** This method allows you to modify the fitness of the individuals
     * of a population. Note that by altering the fitness you may require
     * your problem to store the unaltered fitness somewhere else so that
     * you may still fetch it!
     */
    public void modifyFitness(Population population);

}
