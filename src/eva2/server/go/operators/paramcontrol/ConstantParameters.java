package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.server.go.populations.Population;

/**
 * Dummy implementation. This class is ignored by the Processor. Parameters will not be changed.
 * 
 * @author mkron
 *
 */
public class ConstantParameters extends AbstractParameterControl implements Serializable {
	public ConstantParameters() {}
	
	public ConstantParameters(ConstantParameters o) {
		super(o);
	}

	@Override
	public Object clone() {
		return new ConstantParameters(this);
	}
	
	public String[] getControlledParameters() {
		return null;
	}

	@Override
	public Object[] getValues(Object obj, Population pop, int iteration, int maxIteration) {
		return null;
	}

	public void updateParameters(Object obj) {
	}
	
	public String globalInfo() {
		return "Parameters will not be changed.";
	}
}
