package eva2.server.go.operators.paramcontrol;

import eva2.gui.BeanInspector;

/**
 * Convenience class. Besides the init() method, two more remain to be implemented: 
 * the first one to retrieve an array of strings with the canonical names of the controlled parameters,
 * and the second one to produce an object array of the same length with the values to be assigned
 * at the iteration. If there is no iteration known, iteration counts will be set to -1.
 * 
 * @author mkron
 *
 */
public abstract class AbstractParameterControl implements InterfaceParameterControl {
	public Object[] initialValues = null;
	protected static boolean TRACE=true;
	
	public void init(Object obj) {
		String[] params = getControlledParameters();
		if (params != null) {
			initialValues=new Object[params.length];
			for (int i=0; i<params.length; i++) initialValues[i]=BeanInspector.getMem(obj, params[i]);
		}
	}
	
	public void finish(Object obj) {
		String[] params = getControlledParameters();
		if (params != null) {
			for (int i=0; i<params.length; i++) BeanInspector.setMem(obj, params[i], initialValues[i]);
		}
	}

	public void updateParameters(Object obj, int iteration, int maxIteration) {
		String[] params = getControlledParameters();
		Object[] vals = getValues(obj, iteration, maxIteration);
		for (int i=0; i<params.length; i++) {
			if (!BeanInspector.setMem(obj, params[i], vals[i])) {
				System.err.println("Error: failed to set parameter from parameter control " + this.getClass().getName());;
				System.err.println("  Tried to set name/val: " + params[i] + " / " + BeanInspector.toString(vals[i]));
			} else {
				if (TRACE) System.out.println("Successfully set " + params[i] + " / " + BeanInspector.toString(vals[i]) + " at " + iteration);
			}
		}
	}
	
	public void updateParameters(Object obj) {
		updateParameters(obj, -1, -1);
	}
	
	/**
	 * Return a String array of canonical names of the parameters to be adapted.
	 * 
	 * @return a String array of canonical names of the parameters to be adapted
	 */
	public abstract String[] getControlledParameters();

	/**
	 * Retrieve the values of the adaptable parameters at a given iteration.
	 * If the maximum iteration is not known, both iteration and maxIteration will be set to -1.
	 * 
	 * @param obj	The instance which is controlled
	 * @param iteration	current iteration (or -1 if unknown)
	 * @param maxIteration maximum iteration count (or -1 if unknown)
	 * @return
	 */
	public abstract Object[] getValues(Object obj, int iteration, int maxIteration);
}
