package eva2.server.go.operators.paramcontrol;

/**
 * An object which may be "controlled" dynamically using a String for access.
 * @author mkron
 *
 */
public interface InterfaceParamControllable {
	public void notifyParamChanged(String member, Object oldVal, Object newVal);
	public Object[] getParamControl();
	public void addChangeListener(ParamChangeListener l);
	public void removeChangeListener(ParamChangeListener l);
}
