package eva2.optimization.operators.paramcontrol;

public interface ParamChangeListener {
	public void notifyChange(InterfaceParamControllable controllable, Object oldVal, Object newVal, String msg);
}
