package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;

import eva2.tools.Mathematics;

/**
 * Linearly adapt a specific target String parameter.
 * 
 * @author mkron
 *
 */
public class LinearParamAdaption extends AbstractLinearParamAdaption implements GenericParamAdaption, Serializable {
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
	
	public String globalInfo() {
		return "Simple linear parameter adaption.";
	}
}
