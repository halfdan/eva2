package javaeva.server.go.problems;

import javaeva.server.go.populations.Population;

/**
 * A multimodal problem which has knowledge of its optima.
 * 
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
    public void initListOfOptima();

    /** 
     * This method returns a list of all optima as population or null if 
     * the optima are unknown.
     * 
     * @return population
     */
    public Population getRealOptima();

    /** 
     * Return the number of identified optima or -1 if 
     * the real optima are unknown.
     * @param pop       A population of possible solutions.
     * @return int
     */
    public int getNumberOfFoundOptima(Population pop);

    /** 
     * This method returns the Maximum Peak Ratio.
     * @param pop       A population of possible solutions.
     * @return double
     */
    public double getMaximumPeakRatio(Population pop);
    
    /**
     * Return the maximum normed distance to a known optimum for which the
     * optimum is considered found.
     *   
     * @return
     */
    public double getEpsilon();
}
