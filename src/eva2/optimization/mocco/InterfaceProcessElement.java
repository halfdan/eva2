package eva2.optimization.mocco;

/**
 *
 */
public interface InterfaceProcessElement {

    /**
     * This method will call the initialize method and will go to stall
     */
    void initProcessElementParametrization();

    /**
     * This method will wait for the parametrisation result
     *
     * @return int  Result
     */
    boolean isFinished();

}
