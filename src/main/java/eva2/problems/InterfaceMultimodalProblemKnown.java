package eva2.problems;

import eva2.optimization.population.Population;

/**
 * A multimodal problem which has knowledge of its optima.
 * <p>
 * User: streiche
 * Date: 23.04.2003
 * Time: 10:57:47
 * To change this template use Options | File Templates.
 */
public interface InterfaceMultimodalProblemKnown extends InterfaceMultimodalProblem {

    /**
     * This method will prepare the problem to return a list of all optima
     * if possible and to return quality measures like NumberOfOptimaFound and
     * the MaximumPeakRatio. This method should be called by the user.
     */
    void initListOfOptima();

    /**
     * Return true if the full list of optima is available, else false.
     *
     * @return
     */
    boolean fullListAvailable();

    /**
     * This method returns a list of all optima as population or null if
     * the optima are unknown.
     *
     * @return population
     */
    Population getRealOptima();

    /**
     * Return the number of identified optima or -1 if
     * the real optima are unknown.
     *
     * @param pop A population of possible solutions.
     * @return int
     */
    int getNumberOfFoundOptima(Population pop);

    /**
     * This method returns the Maximum Peak Ratio.
     *
     * @param pop A population of possible solutions.
     * @return double
     */
    double getMaximumPeakRatio(Population pop);

    /**
     * Return the maximum normed distance to a known optimum for which the
     * optimum is considered found.
     *
     * @return
     */
    double getDefaultAccuracy();
}
