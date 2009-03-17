package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.tools.Mathematics;

/**
 * Adapt PSO inertness linearly by time, from given start to end value.
 * This only works if iterations are known.
 * 
 * @author mkron
 *
 */
public class LinearInertnessAdaption extends AbstractParameterControl implements Serializable {
	String[] params = {"inertnessOrChi"};
	private double startV=0.7;
	private double endV=0.2;
	
	public LinearInertnessAdaption() {
		startV=0.7;
		endV=0.2;
	}
	
	public LinearInertnessAdaption(double startValue, double endValue, int functionCalls) {
		startV=startValue;
		endV=endValue;
	}
	
	public String[] getControlledParameters() {
		return params;
	}

	public Object[] getValues(Object obj, int iteration, int maxIteration) {
		if (maxIteration < 0) { // there is no maxIteration known
			System.err.println("Not changing inertness - missing iteration information!");
			return null;
		} else {
			Object[] vals=new Object[1];
			vals[0] = new Double(calcInertness(iteration, maxIteration));
			return vals;
		}
	}

	private double calcInertness(int iteration, int max) {
		return Mathematics.linearInterpolation(iteration, 0, max, startV, endV);
//		return startV+((endV-startV)*(iteration/(double)max));
	}

	public double getStartVal() {
		return startV;
	}

	public void setStartVal(double startV) {
		this.startV = startV;
	}
	
	public String startValTipText() {
		return "Start value for the inertness";
	}

	public double getEndVal() {
		return endV;
	}

	public void setEndVal(double endV) {
		this.endV = endV;
	}
	
	public String endValTipText() {
		return "End value for the inertness";
	}
	
	public String globalInfo() {
		return "Linearly adapt the inertnessOrChi value of PSO";
	}
}
