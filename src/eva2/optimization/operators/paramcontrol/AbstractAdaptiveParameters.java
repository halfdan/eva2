package eva2.optimization.operators.paramcontrol;

//package eva2.optimization.operators.paramcontrol;
//
//import java.io.Serializable;
//
//import eva2.tools.Mathematics;
//
///**
// * Generically adapted parameters. Currently, linear and exponential decrease is supported. Each type
// * of adaptivity uses the parameters in each context:
// * - linear: parameter 0 is start value, parameter 1 is end value.
// * - halvingTime: param. 0 is start value (t=0), param. 1 is the half-value time expressed in percent of the full run.
// * @author mkron
// *
// */
//public abstract class AbstractAdaptiveParameters extends AbstractParameterControl implements Serializable {
//	
//	public AbstractAdaptiveParameters(AbstractParameterControl o) {
//		super(o);
//	}
//	
//	public AbstractAdaptiveParameters() {
//	}
//	
//	/**
//	 * Return the number of parameters under adaptive control.
//	 * 
//	 * @return
//	 */
//	protected int getNumParams() {
//		String[] params = getControlledParameters();
//		return params.length;
//	}
//	
//	/**
//	 * For the controlled parameter at index i return the type of adaptivitiy.
//	 * 
//	 * @param p
//	 * @return
//	 */
//	protected abstract AdaptivityEnum getAdaptivityType(int p);
//	
//	public Object[] getValues(Object obj, int iteration, int maxIteration) {
//		if (maxIteration < 0) { // there is no maxIteration known
//			System.err.println("Not changing parameters - missing iteration information!");
//			return null;
//		} else {
//			Object[] vals=new Object[getNumParams()];
//			for (int i=0; i<vals.length; i++) {
//				vals[i]=calcValueForParam(i, iteration, maxIteration);
//			}
//			return vals;
//		}
//	}
//	
//	/**
//	 * Perform the adaption calculation per parameter and type.
//	 * 
//	 * @param i
//	 * @param iteration
//	 * @param maxIteration
//	 * @param paramOne
//	 * @param paramTwo
//	 * @return
//	 */
//	private Object calcValueForParam(int i, int iteration, int maxIteration) {
//		switch (getAdaptivityType(i)) {
//		case linear:
//			return Mathematics.linearInterpolation(iteration, 0, maxIteration, getAdaptionParameter(i, 0), getAdaptionParameter(i, 1));
//		case halvingTime:
//			return getAdaptionParameter(i, 0)*Math.pow(0.5, (iteration/(double)maxIteration)*100/getAdaptionParameter(i, 1));
//		}
//		System.err.println("Error, invalid adaptivity Type");
//		return null;
//	}
//	
//	/**
//	 * Each adaptivity type requires a number of parameters,
//	 * e.g. start and end value for linear adaption.
//	 * 
//	 * @param i
//	 * @return
//	 */
//	public abstract double getAdaptionParameter(int controlledIndex, int paramIndex);
//}