package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.server.go.InterfaceTerminator;

/**
 * Dummy implementation. This class is ignored by the Processor. Parameters will not be changed.
 * 
 * @author mkron
 *
 */
public class ConstantParameters extends AbstractParameterControl implements Serializable {

	public String[] getControlledParameters() {
		return null;
	}

	@Override
	public Object[] getValues(Object obj, int iteration, int maxIteration) {
		return null;
	}

	public void updateParameters(Object obj) {
	}
	
	public String globalInfo() {
		return "Parameters will not be changed.";
	}
}
