package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.gui.BeanInspector;
import eva2.server.go.populations.Population;
import eva2.tools.math.Mathematics;

/**
 * Simple linear adaption of a String property.
 * 
 * @author mkron
 *
 */
public abstract class AbstractLinearParamAdaption implements ParamAdaption, Serializable {
	private double startV=0.2, endV=0.7;

	public AbstractLinearParamAdaption(AbstractLinearParamAdaption o) {
		startV=o.startV;
		endV=o.endV;
	}
	
	public AbstractLinearParamAdaption(double startValue, double endValue) {
		this.startV = startValue;
		this.endV = endValue;
	}
	
	@Override
	public abstract Object clone();
	
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		return Mathematics.linearInterpolation(iteration, 0, maxIteration, startV, endV);
	}

	public abstract String getControlledParam();
	public String controlledParamTipText() {
		return "The name of the parameter to be controlled by this adaption scheme.";
	}

	public void init(Object obj, Population pop, Object[] initialValues) {
		BeanInspector.setMem(obj, getControlledParam(), startV);
	}
	public void finish(Object obj, Population pop) {}
	
	public double getStartV() {
		return startV;
	}
	public void setStartV(double startV) {
		this.startV = startV;
	}
	public String startVTipText() {
		return "The initial value.";
	}

	public double getEndV() {
		return endV;
	}
	public void setEndV(double endV) {
		this.endV = endV;
	}
	public String endVTipText() {
		return "The final value.";
	}
	
	public String getName() {
		return "Lin.adpt." + getControlledParam() + "(" + startV + "-" + endV + ")";
	}

}
