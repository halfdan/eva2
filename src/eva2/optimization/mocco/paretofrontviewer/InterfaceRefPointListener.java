package eva2.optimization.mocco.paretofrontviewer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.11.2005
 * Time: 16:44:05
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceRefPointListener {

    /**
     * This method will notify the listener that a point has been selected
     *
     * @param point The selected point, most likely 2d
     */
    public void refPointGiven(double[] point);
}
