package eva2.optimization.operator.paramcontrol;

//package eva2.optimization.operators.paramcontrol;
//
//import java.io.Serializable;
//
//import eva2.util.annotation.Description;
//import eva2.tools.Mathematics;
//
///**
// * Adapt an instance parameter by time, from given start to end value.
// * This only works if iterations are known. The new variant allows exponential adaption,
// * where the second parameter (endV) is interpreted as halving time in percent of the
// * full run.
// * 
// * @author mkron
// *
// */
//@Description("A single parameter may be adapted using linear interpolation or exponential decrease.")
//public class SingleParamAdaption extends AbstractAdaptiveParameters implements Serializable {
//	protected String[] params = null;
//	private double startV;
//	private double endV;
//	private AdaptivityEnum adpType = AdaptivityEnum.linear;
//	public static boolean hideFromGOE = true;
//	
//	public SingleParamAdaption(String paramName, double startValue, double endValue) {
//		super();
//		params = new String[]{paramName};
//		startV=startValue;
//		endV=endValue;
//	}
//
//	public SingleParamAdaption(SingleParamAdaption o) {
//		super(o);
//		startV=o.startV;
//		endV=o.endV;
//		adpType=o.adpType;
//		params=o.params.clone();
//	}
//
//	@Override
//	public Object clone() {
//		return new SingleParamAdaption(this);
//	}
//	
//	public String[] getControlledParameters() {
//		return params;
//	}
//
//	public String startValTipText() {
//		return "Start value for the parameter";
//	}
//
//	public String endValTipText() {
//		return "End value for the parameter";
//	}
//
//	@Override
//	protected AdaptivityEnum getAdaptivityType(int p) {
//		return adpType;
//	}
//
//	public double getStartV() {
//		return startV;
//	}
//
//	public void setStartV(double startV) {
//		this.startV = startV;
//	}
//
//	public double getEndV() {
//		return endV;
//	}
//
//	public void setEndV(double endV) {
//		this.endV = endV;
//	}
//
//	public AdaptivityEnum getAdpType() {
//		return adpType;
//	}
//
//	public void setAdpType(AdaptivityEnum adpType) {
//		this.adpType = adpType;
//	}
//
//	public String adpTypeTipText() {
//		return "Select type of adaption.";
//	}
//	
//	@Override
//	public double getAdaptionParameter(int controlledIndex, int paramIndex) {
//		if (paramIndex==0) return startV;
//		else return endV;
//	}
//}
