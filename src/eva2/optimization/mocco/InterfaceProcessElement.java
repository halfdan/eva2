package eva2.optimization.mocco;

/**
 *
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
