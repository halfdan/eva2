package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.server.go.populations.Population;

/**
 * Adapt a generic parameter using exponential decay.
 * 
 * @author mkron
 *
 */
public class ExponentialDecayAdaption implements GenericParamAdaption, Serializable {
	double startValue=0.2, halvingTimePerCent=50;
	String target = "undefinedParameter";
	
	public ExponentialDecayAdaption() {
		startValue=0.2;
		halvingTimePerCent=50;
		target="undefinedParameter";
	}
	
	public ExponentialDecayAdaption(
			ExponentialDecayAdaption o) {
		startValue = o.startValue;
		halvingTimePerCent = o.halvingTimePerCent;
		target = o.target;
	}

	public Object clone() {
		return new ExponentialDecayAdaption(this);
	}
	
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		return startValue*Math.pow(0.5, (iteration/(double)maxIteration)*100/halvingTimePerCent);
	}

	public String getControlledParam() {
		return target;
	}

	public double getStartValue() {
		return startValue;
	}

	public void setStartValue(double startValue) {
		this.startValue = startValue;
	}

	public double getHalvingTimePerCent() {
		return halvingTimePerCent;
	}

	public void setHalvingTimePerCent(double halvingTimePerCent) {
		this.halvingTimePerCent = halvingTimePerCent;
	}

	public void setControlledParam(String target) {
		this.target = target;
	}
	
	public String getName() {
		return "Exp. adapt. "+target+" ("+startValue + "/" + halvingTimePerCent + ")";
	}
	
	public static String globalInfo() {
		return "Exponential decay with a percentual halving time.";
	}

	public void finish(Object obj, Population pop) {}

	public void init(Object obj, Population pop, Object[] initialValues) {}

}
