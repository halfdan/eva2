package eva2.optimization.operator.paramcontrol;

public interface ParamChangeListener {
	public void notifyChange(InterfaceParamControllable controllable, Object oldVal, Object newVal, String msg);
}
