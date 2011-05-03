package eva2.server.go.operators.paramcontrol;

public interface ParamChangeListener {
	public void notifyChange(InterfaceParamControllable controllable, Object oldVal, Object newVal, String msg);
}
