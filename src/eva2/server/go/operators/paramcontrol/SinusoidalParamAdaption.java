package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.server.go.populations.Population;

/**
 * A sinusoidal adaption scheme which can be dampened. The value oscillates between a lower and an upper
 * value with a given iteration period. The dampening is integrated as a sub-linear reduction of the
 * frequency, turning sin(t) into sin(((t+1)^d)-1) which is linear for d=1. For slightly smaller values,
 * the frequency slowly decreases, while for slightly larger values, it slowly increases.
 * 
 * @author mkron
 *
 */
public class SinusoidalParamAdaption implements InterfaceHasUpperDoubleBound, ParamAdaption, GenericParamAdaption, Serializable {
	private double upperBnd=1;
	private double lowerBnd=0;
	private int iterationPeriod=1000;
	private int initialShift=0;
//	protected ParameterControlManager		paramControl = new ParameterControlManager();
	private boolean beatFreq = false;
	
	private double medVal=0;
	private String paramName = "unknownParam";
	private double dampening = 1.;
	
	public SinusoidalParamAdaption() {
		updateMed();
	}
	
	public SinusoidalParamAdaption(double lower, double upper, double dampening, int period, int start, String param) {
		upperBnd = upper;
		lowerBnd = lower;
		iterationPeriod = period;
		initialShift = start;
		paramName = param;
		this.dampening = dampening;
		updateMed();
	}
	
	public SinusoidalParamAdaption(double lower, double upper, int period, int start, String param) {
		this(lower, upper, 1., period, start, param);
	}
	
	public SinusoidalParamAdaption(SinusoidalParamAdaption o) {
		this.upperBnd=o.upperBnd;
		this.lowerBnd=o.lowerBnd;
		this.iterationPeriod = o.iterationPeriod;
		this.initialShift = o.initialShift;
		this.medVal = o.medVal;
		this.paramName = o.paramName;
		this.dampening = o.dampening;
		updateMed();
	}

    @Override
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		double res=0;
		double t = (2*Math.PI/iterationPeriod)*(iteration-initialShift);
		if (dampening!=1) {
			t = SinusoidalParamAdaption.dampen(t, dampening);
		}
		
		res = medVal + (upperBnd-lowerBnd) * 0.5* Math.sin(t);
//		System.out.println("In " + this + " at " + iteration + ": " + res); 
		
		return res;
	}
	
	public String getName() {
		return "SinAdapt("+getControlledParam()+"_"+lowerBnd+"_"+upperBnd+"_"+iterationPeriod+((dampening!=1) ? ("_dmp-"+dampening):"")+")";
	}
	
	public String globalInfo() {
		return "Sinusoidally oscillating value, the frequency may be varyied with time. E.g. use dampening 0.9 " +
				"for a slightly decreasing frequency, dampening 1.1 for a slight increase. The frequency is modified " +
				"in the form sin(t) -> sin(-1+(t+1)^d)";
	}

	/**
	 * Calculate sub-linear t as -1+(t+1)^dampeningExp
	 * @param t
	 * @param dampeningExp
	 * @return
	 */
	private static double dampen(double t, double dampeningExp) {
		return Math.pow(t+1, dampeningExp)-1;
	}

    @Override
	public Object clone() {
		return new SinusoidalParamAdaption(this);
	}
	
    @Override
	public void finish(Object obj, Population pop) {
	}

    @Override
	public String getControlledParam() {
		return paramName ;
	}

    @Override
	public void init(Object obj, Population pop, Object[] initialValues) {
	}
    @Override
	public void setControlledParam(String prm) {
		paramName = prm;
	}
	public String controlledParamTipText() {
		return "The name of the generic parameter to be adapted.";
	}
	
//	public ParameterControlManager getParamControl() {
//		return paramControl;
//	}
//	public static void main(String[] args) {
//		SinusoidalParamAdaption spa = new SinusoidalParamAdaption(1, 2, 100, 0, "asdf");
//		SinusoidalParamAdaption spa9 = new SinusoidalParamAdaption(spa);
//		spa9.dampening=0.9;
//		SinusoidalParamAdaption spa11 = new SinusoidalParamAdaption(spa);
//		spa11.dampening=1.1;
//		for (int i=0; i<1000; i++) {
//			double t = i;
//			System.out.println(t + " " + spa.calcValue(null, null, i, 1000) + " " + spa9.calcValue(null, null, i, 1000) + " " + spa11.calcValue(null, null, i, 1000));
//		}
//	}

    @Override
	public double getUpperBnd() {
		return upperBnd;
	}
	public void setUpperBnd(double upperBnd) {
		this.upperBnd = upperBnd;
		updateMed();
	}
    @Override
	public void SetUpperBnd(double u) {
		this.setUpperBnd(u);
	}
	public String upperBndTipText() {
		return "The upper deviation of the oscillation.";
	}

	public double getLowerBnd() {
		return lowerBnd;
	}
	public void setLowerBnd(double lowerBnd) {
		this.lowerBnd = lowerBnd;
		updateMed();
	}
	private void updateMed() {
		medVal = 0.5*(upperBnd + lowerBnd);
	}

	public String lowerBndTipText() {
		return "The lower deviation of the oscillation."; 
	}

	public int getIterationPeriod() {
		return iterationPeriod;
	}
	public void setIterationPeriod(int iterationPeriod) {
		this.iterationPeriod = iterationPeriod;
	}
	public String iterationPeriodTipText() {
		return "The period length of the oscillation, in iterations.";
	}

	public int getInitialShift() {
		return initialShift;
	}
	public void setInitialShift(int initialShift) {
		this.initialShift = initialShift;
	}
	public String initialShiftTipText() {
		return "The initial phase shift of the sinusoidal, in iterations.";
	}

	public double getDampening() {
		return dampening;
	}
	public void setDampening(double dampening) {
		this.dampening = dampening;
	}
	public String dampeningTipText() {
		return "Dampening exponent for frequency variation: values above 1 increase frequency with time, values below dampen it.";
	}
//
//	public void setBeatFreq(boolean beatFreq) {
//		this.beatFreq = beatFreq;
//		if (this.beatFreq) {
//			paramControl.setSingleAdapters(new ParamAdaption[]{new SinusoidalParamAdaption(1000, 10000, 50000, 1000, "iterationPeriod")});
//		} else {
//			paramControl.setSingleAdapters(null);
//		}
//	}
//
//	public boolean isBeatFreq() {
//		return beatFreq;
//	}
	
}
