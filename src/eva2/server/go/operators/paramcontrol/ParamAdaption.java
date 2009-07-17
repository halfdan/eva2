package eva2.server.go.operators.paramcontrol;

import eva2.server.go.populations.Population;

/**
 * Adapt exactly one parameter with a generic method.
 * @author mkron
 *
 */
public interface ParamAdaption {

	public Object clone();
	
	public String getControlledParam();
	
	/**
	 * Perform the adaption.
	 * 
	 * @param iteration
	 * @param maxIteration
	 * @return
	 */
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration);
	
	public void init(Object obj, Population pop, Object[] initialValues);
	
	public void finish(Object obj, Population pop);
	
}
