package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.server.go.populations.Population;

/**
 * A dummy implementation which does not do any adaption.
 * 
 * @author mkron
 *
 */
public class NoParamAdaption implements ParamAdaption, Serializable {

	public Object clone() {
		return new NoParamAdaption();
	}
	
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		return null;
	}

	public String getControlledParam() {
		return null;
	}
	
	public String globalInfo() {
		return "A dummy implementation which will not change parameters.";
	}

	public void finish(Object obj, Population pop) {}

	public void init(Object obj, Population pop, Object[] initialValues) {}
}
