package eva2.server.go.operators.selection.probability;

import eva2.server.go.populations.Population;

/** The interface for methods with calculate the selection
 * propability from the fitness values. While the fitness
 * is typically to be minimized the selection probability
 * is within [0,1] summs up to one and is to be maximizes.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.03.2004
 * Time: 16:54:02
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceSelectionProbability {

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone();

    /** This method computes the selection probability for each individual
     *  in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param input         The name of the input.
     */
    public void computeSelectionProbability(Population population, String[] input, boolean obeyConst);

    /** This method computes the selection probability for each individual
     *  in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param input         The name of the input.
     */
    public void computeSelectionProbability(Population population, String input, boolean obeyConst);

    /** This method computes the selection probability for each individual
     *  in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param data         The input data as double[][].
     */
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst);
}
