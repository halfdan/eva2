package eva2.server.go.operators.paramcontrol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import eva2.gui.BeanInspector;
import eva2.server.go.populations.Population;
import eva2.server.modules.Processor;
import eva2.tools.Pair;

/**
 * The ParameterControlManager handles an array of ParamAdaption instances which dynamically adapt
 * single properties of an object. To make single parameter adaption available for an object
 * (e.g. Problem or Optimizer instance) add a member of type ParameterControlManager. The
 * corresponding getter and setter may either return the ParameterControlManager itself
 * or the array of singleAdapters directly. In the latter case, the GUI is simpler (because theres
 * one fewer window layer) but there must be an additional method getParamControl implemented
 * which returns the ParameterControlManager to make it available to the Processor.
 *  
 * @see ParamAdaption
 * @see Processor
 * @see AbstractParameterControl
 * 
 * @author mkron
 *
 */
public class ParameterControlManager implements InterfaceParameterControl, Serializable {
	public Object[] initialValues = null;
	private ParamAdaption[] singleAdapters = new ParamAdaption[]{};
	protected static boolean TRACE=false;
	
	public ParameterControlManager() {	}
	
	public ParameterControlManager(ParamAdaption adaptor) {
		singleAdapters = new ParamAdaption[]{adaptor};
	}
	
	public ParameterControlManager(ParamAdaption[] adaptors) {
		singleAdapters = adaptors;
	}
	
	public ParameterControlManager(ParameterControlManager o) {
		if (o.initialValues!=null) initialValues = o.initialValues.clone();
		else initialValues=null;
		if (o.singleAdapters != null) {
			singleAdapters = new ParamAdaption[o.singleAdapters.length];
			for (int i=0; i<singleAdapters.length; i++) singleAdapters[i]=(ParamAdaption)o.singleAdapters[i].clone();
		}
	}
	
	public Object clone() {
		return new ParameterControlManager(this);
	}
	
	public void init(Object obj, Population initialPop) {
		String[] params = getControlledParameters();
		if (params != null) {
			initialValues=new Object[params.length];
			for (int i=0; i<params.length; i++) initialValues[i]=BeanInspector.getMem(obj, params[i]);
		}
		for (ParamAdaption prm : singleAdapters) {
			prm.init(obj, initialPop, initialValues);
		}
	}
	
	public void finish(Object obj, Population finalPop) {
		String[] params = getControlledParameters();
		for (ParamAdaption prm : singleAdapters) {
			prm.finish(obj, finalPop);
		}
		if (params != null) {
			for (int i=0; i<params.length; i++) BeanInspector.setMem(obj, params[i], initialValues[i]);
		}
	}

	public void updateParameters(Object obj, Population pop, int iteration, int maxIteration) {
		String[] params = getControlledParameters();
		Object[] vals = getValues(obj, pop, iteration, maxIteration);
		for (int i=0; i<params.length; i++) {
			Object oldVal=BeanInspector.getMem(obj, params[i]);
			if (oldVal==null) System.err.println("Error, unable to compare old and new value (ParameterControlManager");
			if (!BeanInspector.setMem(obj, params[i], vals[i])) {
				System.err.println("Error: failed to set parameter from parameter control " + this.getClass().getName());;
				System.err.println("  Tried to set name/val: " + params[i] + " / " + BeanInspector.toString(vals[i]));
			} else {
				if (TRACE) System.out.println("Successfully set " + params[i] + " / " + BeanInspector.toString(vals[i]) + " at " + iteration);
//				if (!oldVal.equals(vals[i])) {
//					if (obj instanceof InterfaceParamControllable) ((InterfaceParamControllable)obj).notifyParamChanged(params[i], oldVal, vals[i]);
//				}
			}
		}
	}
	
	public void updateParameters(Object obj) {
		updateParameters(obj, null, -1, -1);
	}
	
	/**
	 * Return a String array of canonical names of the parameters to be adapted.
	 * 
	 * @return a String array of canonical names of the parameters to be adapted
	 */
	public String[] getControlledParameters() {
		if (singleAdapters != null) {
			Vector<String> names = new Vector<String>(singleAdapters.length);
			for (int i = 0; i < singleAdapters.length; i++) {
				String prm=singleAdapters[i].getControlledParam();
				if (prm!=null) names.add(prm);
			}
			return names.toArray(new String[names.size()]);
		} else return null;
	}

	/**
	 * Retrieve the values of the adaptable parameters at a given iteration.
	 * If the maximum iteration is not known, both iteration and maxIteration will be set to -1.
	 * 
	 * @param obj	The instance which is controlled
	 * @param iteration	current iteration (or -1 if unknown)
	 * @param maxIteration maximum iteration count (or -1 if unknown)
	 * @return
	 */
	public Object[] getValues(Object obj, Population pop, int iteration, int maxIteration) {
		if (singleAdapters != null) {
			Object[] vals = new Object[singleAdapters.length];
			for (int i = 0; i < vals.length; i++) {
				vals[i]=singleAdapters[i].calcValue(obj, pop, iteration, maxIteration);
			}
			return vals;
		} else return null;
	}

	public ParamAdaption[] getSingleAdapters() {
		return singleAdapters;
	}

	public void setSingleAdapters(ParamAdaption[] singleAdapters) {
		this.singleAdapters = singleAdapters;
	}
	
	public String globalInfo() {
		return "Define a list of dynamically adapted parameters.";
	}
	
	public String getName() {
		return "ParameterControlManager";
	}

	/**
	 * Retrieve a list of objects which are properties of the given target object (retrievable by a 
	 * getter method) and which implement the getParamControl method.
	 * 
	 * @param target
	 * @return
	 */
	public static Object[] arrayOfControllables(
			Object target) {
		Pair<String[],Object[]> propsNamesVals = BeanInspector.getPublicPropertiesOf(target, true);
		ArrayList<Object> controllables = new ArrayList<Object>();
		Object[] objs = propsNamesVals.tail;
		for (int i=0; i<objs.length; i++) {
			if (objs[i]!=null) {
				// TODO avoid hasMethod recreate some interface for this??
				if (BeanInspector.hasMethod(objs[i], "getParamControl")!=null) controllables.add(objs[i]);
			}
		}
		return controllables.toArray(new Object[controllables.size()]);
	}
}
