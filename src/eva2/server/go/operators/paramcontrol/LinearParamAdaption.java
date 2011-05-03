package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.tools.math.Mathematics;

/**
 * Linearly adapt a specific target String parameter.
 * 
 * @author mkron
 *
 */
public class LinearParamAdaption extends AbstractLinearParamAdaption 
implements InterfaceHasUpperDoubleBound, GenericParamAdaption, Serializable {
	String target = "undefinedParameter";
	
	public LinearParamAdaption() {
		super(0.2,0.7);
		target = "undefinedParameter";
	}
	
	public LinearParamAdaption(LinearParamAdaption o) {
		super(o);
		target=o.target;
	}
	
	public LinearParamAdaption(String target, double startValue, double endValue) {
		super(startValue, endValue);
		this.target = target;
	}
	
	public Object clone() {
		return new LinearParamAdaption(this);
	}

	public String getControlledParam() {
		return target;
	}
	public void setControlledParam(String target) {
		this.target = target;
	}
	
	public static String globalInfo() {
		return "Simple linear parameter adaption.";
	}
	
	public String[] customPropertyOrder() {
		return new String[] {"startV", "endV"};
	}
	
	/**
	 * Return the larger value of the start and end value.
	 *  
	 * @return
	 */
	public double getUpperBnd() {
		return Math.max(getEndV(), getStartV());
	}
	
	/**
	 * Set the larger one of start- or end-value to the given value. If they are
	 * equal, both are set.
	 * 
	 * @param u
	 */
	public void SetUpperBnd(double u) {
		if (getEndV()==getStartV()) {
			setEndV(u);
			setStartV(u);
		} else if (getEndV()>getStartV()) { // end value is larger
			if (u<getStartV()) System.err.println("Warning, changing direction of linear adaption!");
			setEndV(u);
		} else { // start value is larger
			if (u<getEndV()) System.err.println("Warning, changing direction of linear adaption!");
			setStartV(u);
		}
	}
}
