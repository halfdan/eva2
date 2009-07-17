package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.gui.GenericObjectEditor;
import eva2.tools.Mathematics;

/**
 * Adapt PSO inertness linearly by time, from given start to end value.
 * This only works if iterations are known. The new variant allows exponential adaption,
 * where the second parameter (endV) is interpreted as halfing time in percent of the
 * full run.
 * 
 * @author mkron
 *
 */
public class PSOInertnessAdaption extends LinearParamAdaption implements Serializable {
	
	public PSOInertnessAdaption() {
		super("inertnessOrChi", 0.7, 0.2);
	}

	public void hideHideable() {
		GenericObjectEditor.setHideProperty(this.getClass(), "controlledParam", true);
	}
	
	public String startVTipText() {
		return "Start value for the inertness";
	}

	public String endVTipText() {
		return "End value for the inertness";
	}

	public String globalInfo() {
		return "Adapt the inertnessOrChi value of PSO.";
	}
}
