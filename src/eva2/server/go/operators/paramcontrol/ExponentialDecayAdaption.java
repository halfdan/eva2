package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.server.go.populations.Population;

/**
 * Adapt a generic parameter using exponential decay.
 * 
 * @author mkron
 *
 */
public class ExponentialDecayAdaption implements ParamAdaption, GenericParamAdaption, Serializable {
	private double startValue=0.2, halvingTimePerCent=50;
	private double saturation=0.;
	private String target = "undefinedParameter";
	
	public ExponentialDecayAdaption() {
	}
	
	public ExponentialDecayAdaption(double startV, double halvingTimePC, String param) {
		this(startV, halvingTimePC, 0., param);
	}
	public ExponentialDecayAdaption(double startV, double halvingTimePC, double offset, String param) {
		this.setSaturation(offset);
		startValue = startV;
		halvingTimePerCent = halvingTimePC;
		target = param;
	}
	
	public ExponentialDecayAdaption(
			ExponentialDecayAdaption o) {
		startValue = o.startValue;
		halvingTimePerCent = o.halvingTimePerCent;
		target = o.target;
		setSaturation(o.getSaturation());
	}

	public Object clone() {
		return new ExponentialDecayAdaption(this);
	}
	
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		return getSaturation()+(startValue-getSaturation())*Math.pow(0.5, (iteration/(double)maxIteration)*100/halvingTimePerCent);
//		return startValue*Math.pow(0.5, (iteration/(double)maxIteration)*100/halvingTimePerCent);
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
	public String startValueTipText() {
		return "The initial starting value at generation zero.";
	}

	public double getHalvingTimePerCent() {
		return halvingTimePerCent;
	}
	public void setHalvingTimePerCent(double halvingTimePerCent) {
		this.halvingTimePerCent = halvingTimePerCent;
	}
	public String halvingTimePerCentTipText() {
		return "The number of iterations (usually generations) within which the respecitve value will be halved.";
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
	
	public static void main(String[] args) {
		ExponentialDecayAdaption eda = new ExponentialDecayAdaption(1, 20, 0.05, "");
		int maxIt = 1000;
		for (int i=0; i<maxIt; i+=10) {
			System.out.println(i + " " + eda.calcValue(null, null, i, maxIt));
		}
	}

	public void setSaturation(double saturation) {
		this.saturation = saturation;
	}
	public double getSaturation() {
		return saturation;
	}
	public String saturationTipText() {
		return "Saturation value of the value (y-offset of the exponential).";
	}

}
