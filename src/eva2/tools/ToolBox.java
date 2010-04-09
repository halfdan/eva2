package eva2.tools;

import java.util.ArrayList;
import java.util.List;

import eva2.gui.BeanInspector;

/**
 * Collection of miscellaneous static helper methods.
 * 
 * @author mkron
 *
 */
public class ToolBox {

	/**
	 * Convert all items of an enum to a String array and append the given String array at the end.
	 * 
	 * @param additionals
	 * @return
	 */
	public static String[] appendEnumAndArray(Enum<?> e, String[] additionals) {
		Enum<?>[] fields = e.getClass().getEnumConstants();
		int enumLen = fields.length; //values().length;
		int len = enumLen+additionals.length;
		String[] ret = new String[len];
		for (int i=0; i<enumLen; i++) ret[i]=fields[i].toString();
		for (int i=enumLen; i<ret.length; i++) ret[i] = additionals[i-enumLen];
		return ret;
	}
	
	public static String[] appendArrays(String[] strArr1,
			String[] strArr2) {
		String[] ret = new String[strArr1.length + strArr2.length];
		System.arraycopy(strArr1, 0, ret, 0, strArr1.length);
		System.arraycopy(strArr2, 0, ret, strArr1.length, strArr2.length);
		return ret;
	}	
	
	public static Object[] appendArrays(Object[] strArr1,
			Object[] strArr2) {
		Object[] ret = new Object[strArr1.length + strArr2.length];
		System.arraycopy(strArr1, 0, ret, 0, strArr1.length);
		System.arraycopy(strArr2, 0, ret, strArr1.length, strArr2.length);
		return ret;
	}
	
	/**
	 * For a list of objects, generate an array of Double which contains thee.getClass().getEnumConstants()
	 * converted double arrays whenever this is directly possible, or null otherwise.
	 * The length of the array will correspond to the length of the given list.
	 * 
	 * @see BeanInspector.toString(Object)
	 * @param l
	 * @return
	 */
	public static Double[] parseDoubles(List<Object> l) {
		ArrayList<Double> vals = new ArrayList<Double>();
		for (Object o : l) {
			vals.add(toDouble(o)); // null if unsuccessfull
		}
		return vals.toArray(new Double[vals.size()]);
	}
	
	/**
	 * Try to convert a Double from a given Object. Return null
	 * if conversion fails (e.g. because the Object is a complex data type
	 * which has no straight-forward numeric representation, e.g. an array).
	 *  
	 * @param o
	 * @return
	 */
	public static Double toDouble(Object o) {
		if (o instanceof Number) return ((Number)o).doubleValue();
		else try {
			Double d = Double.parseDouble(BeanInspector.toString(o));
			return d;
		} catch(Exception e) { }
		return null;
	}
	
	/**
	 * For an array of objects, generate an array of Double which contains the
	 * converted double arrays whenever this is directly possible, or null otherwise.
	 * 
	 * @see BeanInspector.toString(Object)
	 * @param l
	 * @return
	 */
	public static Double[] parseDoubles(Object [] os) {
		Double[] vals = new Double[os.length];
		for (int i=0; i<os.length; i++) {
			vals[i]=toDouble(os[i]);
		}
		return vals;
	}
}
