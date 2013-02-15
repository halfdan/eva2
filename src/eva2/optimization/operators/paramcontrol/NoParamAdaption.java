package eva2.optimization.operators.paramcontrol;

import eva2.optimization.populations.Population;
import java.io.Serializable;

/**
 * A dummy implementation which does not do any adaption.
 * 
 * @author mkron
 *
 */
public class NoParamAdaption implements ParamAdaption, Serializable {

    @Override
	public Object clone() {
		return new NoParamAdaption();
	}
	
    @Override
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		return null;
	}

    @Override
	public String getControlledParam() {
		return null;
	}
	
	public static String globalInfo() {
		return "A dummy implementation which will not change parameters.";
	}

    @Override
	public void finish(Object obj, Population pop) {}

    @Override
	public void init(Object obj, Population pop, Object[] initialValues) {}
}
