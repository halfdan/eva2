package eva2.optimization.operator.paramcontrol;

/**
 * An object which may be "controlled" dynamically using a String for access.
 *
 * @author mkron
 */
public interface InterfaceParamControllable {
    void notifyParamChanged(String member, Object oldVal, Object newVal);

    Object[] getParamControl();

    void addChangeListener(ParamChangeListener l);

    void removeChangeListener(ParamChangeListener l);
}
