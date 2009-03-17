package eva2.server.go.operators.paramcontrol;

import eva2.server.go.populations.Population;

/**
 * Interface for dynamic changes to object parameters.
 * When an optimizer has the getter method getParamControl retrieving an instance of this interface,
 * an update is triggered after every optimize call. This allows parameters of the object to be
 * adapted dynamically, such as linearly decreasing weights or control parameters. In case of PSO,
 * this may be the linearly decreasing inertia weight or the activation feedback control mechanism.
 * 
 * @author mkron
 *
 */
public interface InterfaceParameterControl {
	/**
	 * Initialize the parameter control instance before a run.
	 * 
	 * @param obj The controlled object.
	 */
	public void init(Object obj);
	
	/**
	 * After an optimization run, finalizing stuff may be done.
	 * 
	 * @param obj  The controlled object.
	 */
	public void finish(Object obj);
	
	/**
	 * For a given runtime (maxIteration) and current iteration, update the parameters of the object.
	 * 
	 * @param obj
	 * @param iteration
	 * @param maxIteration
	 */
	public void updateParameters(Object obj, int iteration, int maxIteration);
	
	/**
	 * If no runtime in terms of iterations can be specified, the parameter control may try to infer
	 * the state from the object itself.
	 * 
	 * @param obj
	 */
	public void updateParameters(Object obj);
}
