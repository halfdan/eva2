package eva2.optimization.operator.paramcontrol;

/**
 * This should be implemented by adaptors which can be applied to generic target properties.
 *
 * @author mkron
 */
public interface GenericParamAdaption extends ParamAdaption {

    /**
     * Set the target property which is to be adapted.
     *
     * @param prm
     */
    public void setControlledParam(String prm);
}
