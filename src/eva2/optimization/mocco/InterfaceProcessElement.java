package eva2.optimization.mocco;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.10.2005
 * Time: 14:56:28
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceProcessElement {

    /**
     * This method will call the init method and will go to stall
     */
    public void initProcessElementParametrization();

    /**
     * This method will wait for the parametrisation result
     *
     * @return int  Result
     */
    public boolean isFinished();

}
