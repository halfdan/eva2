package eva2.optimization.mocco.paretofrontviewer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.11.2005
 * Time: 11:15:52
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceParetoFrontView {

    /**
     * This method notifies the Pareto front view that
     * the data has changed most likely due to changes in
     * the problem definition
     */
    public void updateView();
}
