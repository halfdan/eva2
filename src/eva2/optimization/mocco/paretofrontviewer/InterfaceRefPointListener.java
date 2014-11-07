package eva2.optimization.mocco.paretofrontviewer;

/**
 *
 */
public interface InterfaceRefPointListener {

    /**
     * This method will notify the listener that a point has been selected
     *
     * @param point The selected point, most likely 2d
     */
    public void refPointGiven(double[] point);
}
