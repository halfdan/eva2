package eva2.optimization.problems;

/**
 * An interface refactored from InterfaceMultiModalProblem, having only methods used for
 * plotting nice colored graphs in GUI using the TopoPlot class.
 * 
 * @author mkron
 */
public interface Interface2DBorderProblem {
    /** This method returns the 2d borders of the problem
     * @return double[][]
     */
    public double[][] get2DBorder();

    /** 
     * This method returns the double value at a given position. The value should be
     * based on the same projection delivered by the project2DPoint function.
     * 
     * @param point     The double[2] that is queried.
     * @return double
     */
    public double functionValue(double[] point);
    
    /**
     * Project a 2D point to the default higher-dimensional cut to be displayed (if required for plotting).
     * 
     * @param point the double[2] that is queried
     * @return a (higher dimensional) projection of the point
     */
    public double[] project2DPoint(double[] point);
}
