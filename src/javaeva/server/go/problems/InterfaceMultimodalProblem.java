package javaeva.server.go.problems;

import javaeva.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.04.2003
 * Time: 10:57:47
 * To change this template use Options | File Templates.
 */
public interface InterfaceMultimodalProblem {

    /** This method will prepare the problem to return a list of all optima
     * if possible and to return quality measures like NumberOfOptimaFound and
     * the MaximumPeakRatio. This method should be called by the user.
     */
    public void initListOfOptima();

    /** This method returns a list of all optima as population
     * @return population
     */
    public Population getAllOptima();

    /** This method returns the overall number of optima
     * @return int
     */
    public int getNumberOfOptima();

    /** This method returns the Number of Identified optima
     * @param pop       A population of possible solutions.
     * @return int
     */
    public int getNumberOfFoundOptima(Population pop);

    /** This method returns the Maximum Peak Ratio.
     * @param pop       A population of possible solutions.
     * @return double
     */
    public double getMaximumPeakRatio(Population pop);

    /** This method returns this min and may fitness occuring
     * @return double[]
     */
    public double[] getExtrema();
}
