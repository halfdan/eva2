package eva2.server.go.problems;

/**
 * An interface refactored from InterfaceMultiModalProblem, having only methods used for
 * plotting nice colored graphs in JavaEvA GUI using the TopoPlot class.
 * 
 * @author mkron
 */
public interface Interface2DBorderProblem {
    /** This method returns the 2d borders of the problem
     * @return double[][]
     */
    public double[][] get2DBorder();

    /** This method returns the double value at a given position.
     * @param point     The double[2] that is queried.
     * @return double
     */
    public double functionValue(double[] point);
}
