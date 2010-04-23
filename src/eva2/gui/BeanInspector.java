package eva2.gui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eva2.server.go.populations.Population;
import eva2.tools.Pair;
import eva2.tools.SelectedTag;
import eva2.tools.StringTools;
import eva2.tools.Tag;


/**
 * Some miscellaneous functions to help with Beans, reflection, conversion and generic display.
 * 
 * @author mkron, Holger Ulmer, Felix Streichert, Hannes Planatscher
 *
 */
public class BeanInspector {
	public static boolean TRACE = false;

	/**
	 * Check for equality based on bean properties of two target objects.
	 */
	public static boolean equalProperties(Object Target_1, Object Target_2) {
		if (Target_1 == null || Target_2 == null) {
			System.out.println("");
			return false;
		}
		System.out.println("equalProperties: " + Target_1.getClass().getName() + " " + Target_2.getClass().getName());
		if (Target_1.getClass().getName().equals(Target_2.getClass().getName()) == false) {
			System.out.println("");
			return false;
		}
		// compare each of the properties !!
		BeanInfo Info_1 = null;
		BeanInfo Info_2 = null;
		PropertyDescriptor[] Properties_1 = null;
		PropertyDescriptor[] Properties_2 = null;
		try {

			Info_1 = Introspector.getBeanInfo(Target_1.getClass());
			Info_2 = Introspector.getBeanInfo(Target_2.getClass());
			Properties_1 = Info_1.getPropertyDescriptors();
			Properties_2 = Info_2.getPropertyDescriptors();
			Info_1.getMethodDescriptors();
		} catch (IntrospectionException ex) {
			System.out.println("BeanTest: Couldn't introspect !!!!!!!!!");
			return false;
		}
		boolean BeansInside = false;
		boolean BeansEqual = true;
		for (int i = 0; i < Properties_1.length; i++) {
			if (Properties_1[i].isHidden() || Properties_1[i].isExpert()) {
				continue;
			}
			//String name = Properties_1[i].getDisplayName(); //System.out.println("name = "+name );
			//Class type = Properties_1[i].getPropertyType(); //System.out.println("type = "+type.getName() );
			Method getter_1 = Properties_1[i].getReadMethod();
			Method getter_2 = Properties_2[i].getReadMethod();
			Method setter_1 = Properties_1[i].getWriteMethod();
			// Only display read/write properties.
			if (getter_1 == null || setter_1 == null) {
				continue;
			}
			System.out.println("getter_1 = " + getter_1.getName() + " getter_2 = " + getter_2.getName());
			//System.out.println("type = "+type.getName() );
			Object args_1[] = {};
			Object args_2[] = {};
			//System.out.println("m_Target"+m_Target.toString());
			try {
				Object value_1 = getter_1.invoke(Target_1, args_1);
				Object value_2 = getter_2.invoke(Target_2, args_2);
				BeansInside = true;
				if (BeanInspector.equalProperties(value_1, value_2) == false) {
					BeansEqual = false;
				}
			} catch (Exception e) {
				System.out.println(" BeanTest.equalProperties " + e.getMessage());
			}
		}
		if (BeansInside == true) {
			return BeansEqual;
		}
		// here we have Integer or Double ...
		if (Target_1 instanceof Integer ||
				Target_1 instanceof Boolean ||
				Target_1 instanceof Float ||
				Target_1 instanceof Double ||
				Target_1 instanceof Long ||
				Target_1 instanceof String) {
			return Target_1.equals(Target_2);
		}

		System.out.println(" Attention no match !!!");
		return true;
	}

	public static String toString(Object Target) {
		return toString(Target, ';', false);
	}
	/**
	 * Collect the accessible properties of an object and their values in a string.
	 * Special cases: Arrays and Lists are concatenations of their elements, Population is excepted from lists.
	 * If the object has its own toString method, this one is preferred. Hidden or expert properties are not
	 * shown.
	 * 
	 * @param  Target  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	public static String toString(Object Target, char delim, boolean tight) {
		String ret = "";
		if (Target == null) return "null";
		// try the object itself
		if (Target instanceof String) return (String)Target; // directly return a string object
		Class<? extends Object> type = Target.getClass();

		if (type.isArray()) { // handle the array case
			StringBuffer sbuf = new StringBuffer("[");
			if (!tight) sbuf.append(" ");
			int len = Array.getLength(Target);
			for (int i=0; i<len; i++) {
				sbuf.append(toString(Array.get(Target, i)));
				if (i<len-1) {
					sbuf.append(delim);
					if (!tight) sbuf.append(" ");
				}
			}
			if (!tight) sbuf.append(" ");
			sbuf.append("]");
			return sbuf.toString();
		}
		
		if (type.isEnum()) {
			return Target.toString();
		}

		if (Target instanceof List && !(Target instanceof Population)) { // handle the list case
			StringBuffer sbuf = new StringBuffer("[");
			if (!tight) sbuf.append(" ");
			List<?> lst = (List<?>)Target;
			for (Object o : lst) {
				sbuf.append(o.toString());
				sbuf.append(delim);
				if (!tight) sbuf.append(" ");
			}
			if (!tight) sbuf.setCharAt(sbuf.length()-2, ' ');
			sbuf.setCharAt(sbuf.length()-1, ']');
			return sbuf.toString();
		}


		Method[] methods = Target.getClass().getDeclaredMethods();
		for (int ii = 0; ii < methods.length; ii++) { // check if the object has its own toString method, in this case use it
			if ((methods[ii].getName().equals("toString") /*|| (methods[ii].getName().equals("getStringRepresentation"))*/) && (methods[ii].getParameterTypes().length == 0)) {
				Object[] args = new Object[0];
				//args[0] = Target;
				try {
					ret = (String) methods[ii].invoke(Target, args);
					if (TRACE) System.out.println("toString on "+ Target.getClass() + " gave me " + ret);
					return ret;
				} catch (Exception e) {
					System.err.println(" ERROR +"+ e.getMessage());
				}
			}
		}

		// otherwise try introspection and collect all public properties as strings

		Pair<String[],Object[]> nameVals = getPublicPropertiesOf(Target, true); 

		StringBuffer sbuf = new StringBuffer(type.getName());
		sbuf.append("{");
		for (int i=0; i<nameVals.head.length; i++) {
			if (nameVals.head[i]!=null) {
				sbuf.append(nameVals.head[i]);
				sbuf.append("=");
				sbuf.append(toString(nameVals.tail[i]));
				sbuf.append(delim);
				if (!tight) sbuf.append(" ");
			}
		}

		sbuf.append("}");
		return sbuf.toString();
	}

	/**
	 * Retrieve names and values of instance fields which are accessible by getter method, optionally
	 * by both getter and setter method. The returned arrays may contain null entries.
	 * Properties marked as hidden or expert are skipped.
	 *   
	 * @param target
	 * @return
	 */
	public static Pair<String[],Object[]> getPublicPropertiesOf(Object target, boolean requireSetter) {
		BeanInfo Info = null;
		PropertyDescriptor[] Properties = null;
//		MethodDescriptor[] Methods = null;
		try {
			Info = Introspector.getBeanInfo(target.getClass());
			Properties = Info.getPropertyDescriptors();
			Info.getMethodDescriptors();
		} catch (IntrospectionException ex) {
			System.err.println("BeanTest: Couldn't introspect");
			return null;
		}

		String[] nameArray = new String[Properties.length];
		Object[] valArray = new Object[Properties.length];
		for (int i = 0; i < Properties.length; i++) {
			if (Properties[i].isHidden() || Properties[i].isExpert()) {
				continue;
			}
			String name = Properties[i].getDisplayName();
			//System.out.println("name = "+name );
			//Class type = Properties[i].getPropertyType();
			//System.out.println("type = "+type.getName() );
			Method getter = Properties[i].getReadMethod();
			Method setter = Properties[i].getWriteMethod();
			// Only display read/write properties.
			if (getter == null || (setter == null && requireSetter)) {
				continue;
			}
			//System.out.println("name = "+name );
			//System.out.println("type = "+type.getName() );
			Object args[] = {};
			//System.out.println("m_Target"+m_Target.toString());

			try {
				nameArray[i]=name;
				valArray[i] = getter.invoke(target, args);
			} catch (Exception e) {
				System.err.println("BeanTest ERROR +"+ e.getMessage());
			}
		}
		Pair<String[],Object[]> nameVals = new Pair<String[],Object[]>(nameArray, valArray);
		return nameVals;
	}
	

	/**
	 *@param  Target  Description of the Parameter
	 */
	public static void showInfo(Object Target) {
		System.out.println("Inspecting " + Target.getClass().getName());
		// object itself
		try {
			if (Target instanceof java.lang.Integer) {
				System.out.println(" Prop = Integer" + Target.toString());
			}
			if (Target instanceof java.lang.Boolean) {
				System.out.println(" Prop = Boolean" + Target.toString());
			}
			if (Target instanceof java.lang.Long) {
				System.out.println(" Prop = Long" + Target.toString());
			}
			if (Target instanceof java.lang.Double) {
				System.out.println(" Prop = Long" + Target.toString());
			}
		} catch (Exception e) {
			//System.out.println(" ERROR +"+ e.getMessage());
		}
		// then the properties
		BeanInfo Info = null;
		PropertyDescriptor[] Properties = null;
//		MethodDescriptor[] Methods = null;
		try {
			Info = Introspector.getBeanInfo(Target.getClass());
			Properties = Info.getPropertyDescriptors();
			Info.getMethodDescriptors();
		} catch (IntrospectionException ex) {
			System.err.println("BeanTest: Couldn't introspect");
			return;
		}

		for (int i = 0; i < Properties.length; i++) {
			if (Properties[i].isHidden() || Properties[i].isExpert()) {
				continue;
			}
			String name = Properties[i].getDisplayName();
			//System.out.println("name = "+name );
//			Class type = Properties[i].getPropertyType();
			//System.out.println("type = "+type.getName() );
			Method getter = Properties[i].getReadMethod();
			Method setter = Properties[i].getWriteMethod();
			// Only display read/write properties.
			if (getter == null || setter == null) {
				continue;
			}
			//System.out.println("name = "+name );
			//System.out.println("type = "+type.getName() );
			Object args[] = {};
			//System.out.println("m_Target"+m_Target.toString());
			try {
				Object value = getter.invoke(Target, args);
				System.out.println("Inspecting name = " + name);
				if (value instanceof Integer) {
					Object args2[] = {new Integer(999)};
					setter.invoke(Target, args2);
				}
				showInfo(value);
			} catch (Exception e) {
				System.out.println("BeanTest ERROR +" + e.getMessage());
			}
		}
	}

	/**
	 * Call a method by a given name with given arguments, if the method is available.
	 * Returns the return values of the call or null if it isnt found.
	 * This of course means that the caller is unable to distinguish between "method not found"
	 * and "method found and it returned null".
	 * 
	 * @param obj
	 * @param mName
	 * @param args
	 * @return the return value of the called method or null
	 */
	public static Object callIfAvailable(Object obj, String mName, Object[] args) {
		Method meth = hasMethod(obj, mName, toClassArray(args));
		if (meth != null) {
			try {
				return meth.invoke(obj, args);
			} catch(Exception e) {
				System.err.println("Error on calling method "+mName + " on " + obj.getClass().getName());
				e.printStackTrace();
				return null;
			}
		} else return null;
	}

	/**
	 * Produce an array of Class instances matching the types of 
	 * the given object array.
	 * 
	 * @param o
	 * @return
	 */
	public static Class[] toClassArray(Object[] o) {
		if (o==null) return null;
		Class[] clz = new Class[o.length];
		for (int i=0; i<o.length; i++) {
			clz[i]=o.getClass();
		}
		return clz;
	}
	
	/**
	 * Check whether an object has a method by the given name and with
	 * matching signature considering the arguments. Return
	 * it if found, or null if not.
	 *  
	 * @param obj
	 * @param mName the method name
	 * @param args the arguments, null allowed if the method takes no parameters
	 * @return the method or null if it isn't found
	 */
	public static Method hasMethod(Object obj, String mName, Object[] args) {
		return hasMethod(obj, mName, toClassArray(args));
	}
	
	/**
	 * Check whether an object has a method by the given name and
	 * with the given parameter signature. Return
	 * it if found, or null if not.
	 *  
	 * @param obj
	 * @param mName the method name
	 * @param paramTypes the parameter types, null allowed if no parameters are expected
	 * @return the method or null if it isn't found
	 */
	public static Method hasMethod(Object obj, String mName, Class[] paramTypes) {
		Class<?> cls = obj.getClass();
		Method[] meths = cls.getMethods();
		for (Method method : meths) {
			if (method.getName().equals(mName)) { // name match
				Class[] methParamTypes = method.getParameterTypes();
				if (paramTypes==null && methParamTypes.length==0) return method; // full match
				else {
					if (paramTypes!=null && (methParamTypes.length==paramTypes.length)) {
						boolean mismatch = false; int i=0;
						while ((i<methParamTypes.length) && (!mismatch)) {
							if (!methParamTypes[i].equals(paramTypes[i])) mismatch=true;
							i++;
						} 
						if (!mismatch) return method; // parameter match, otherwise search on
					} // parameter mismatch, search on
				}
			}
		}
		return null;
	}
	
	/**
	 * Just concatenates getClassDescription(obj) and getMemberDescriptions(obj, withValues).
	 * 
	 * @param obj	target object
	 * @param withValues	if true, member values are displayed as well 
	 * @return an info string about class and members of the given object
	 */
	public static String getDescription(Object obj, boolean withValues) {
		StringBuffer sbuf = new StringBuffer(getClassDescription(obj));
		sbuf.append("\n"); 
		String[] mems = getMemberDescriptions(obj, withValues);
		for (String str : mems) {
			sbuf.append(str);
		}
		return sbuf.toString();
	}
	
	/**
	 * Check for info methods on the object to be provided by the developer
	 * and return their text as String.
	 * 
	 * @param obj
	 * @return String information about the object's class
	 */
	public static String getClassDescription(Object obj) {
		StringBuffer infoBf = new StringBuffer("Type: ");
		infoBf.append(obj.getClass().getName());
		infoBf.append("\t");
		
		Object          args[]  = { };
		Object ret; 
		
		for (String meth : new String[]{"getName", "globalInfo"}) {
			ret = callIfAvailable(obj, meth, args);
			if (ret != null) {
				infoBf.append("\t");
				infoBf.append((String)ret);
			}
		}
		
		return infoBf.toString();
	}

	/**
	 * Return an info string on the members of the object class, containing name, type, optional
	 * value and tool tip text if available. The type is accompanied by a tag "common" or "restricted",
	 * indicating whether the member property is normal or hidden, meaning it may have effect depending
	 * on settings of other members only, for instance.
	 * 
	 * @param obj	target object
	 * @param withValues	if true, member values are displayed as well 
	 * @return an info string about class and members of the given object
	 */
	public static String[] getMemberDescriptions(Object obj, boolean withValues) {
		BeanInfo bi;    
		try {
			bi      = Introspector.getBeanInfo(obj.getClass());
		} catch(IntrospectionException e) {
			e.printStackTrace();
			return null;
		}
		PropertyDescriptor[] 	m_Properties = bi.getPropertyDescriptors();
		ArrayList<String> 				memberInfoList  = new ArrayList<String>();
		
		for (int i = 0; i < m_Properties.length; i++) {
			if (m_Properties[i].isExpert()) continue;
			
			String  name    = m_Properties[i].getDisplayName();
			if (TRACE) System.out.println("PSP looking at "+ name);

			Method  getter  = m_Properties[i].getReadMethod();
			Method  setter  = m_Properties[i].getWriteMethod();
			// Only display read/write properties.
			if (getter == null || setter == null) continue;
			
			try {
				Object          args[]  = { };
				Object          value   = getter.invoke(obj, args);

				// Don't try to set null values:
				if (value == null) {
					// If it's a user-defined property we give a warning.
					String getterClass = m_Properties[i].getReadMethod().getDeclaringClass().getName();
					if (getterClass.indexOf("java.") != 0) System.err.println("Warning: Property \"" + name+ "\" has null initial value.  Skipping.");
					continue;
				}

				StringBuffer memberInfoBf = new StringBuffer(40);
				memberInfoBf.append("Member:\t");
				memberInfoBf.append(name);
				
				memberInfoBf.append("\tType: ");
				
				if (m_Properties[i].isHidden()) {
					memberInfoBf.append("restricted, ");
				} else {
					memberInfoBf.append("common, ");
				}
				String typeName = value.getClass().getName();
				if (value instanceof SelectedTag) {
					Tag[] tags = ((SelectedTag)value).getTags();
					memberInfoBf.append("String in {");
					for (int k=0; k<tags.length; k++) {
						memberInfoBf.append(tags[k].getString());
						if (k+1<tags.length) memberInfoBf.append(", ");
					}
					memberInfoBf.append("}");
				} else memberInfoBf.append(typeName);
				
				if (withValues) {
					memberInfoBf.append('\t');
					memberInfoBf.append("Value: \t");
					memberInfoBf.append(toString(value));
				}
				
				// now look for a TipText method for this property
				Method tipTextMethod = hasMethod(obj, name + "TipText", null);
				if (tipTextMethod == null) {
					memberInfoBf.append("\tNo further hint.");
				} else {
					memberInfoBf.append("\tHint: ");
					memberInfoBf.append(toString(tipTextMethod.invoke(obj, args)));
				}
				
				memberInfoBf.append('\n');
				memberInfoList.add(memberInfoBf.toString());
			} catch (Exception ex) {
				System.err.println("Skipping property "+name+" ; exception: " + ex.getMessage());
				ex.printStackTrace();
			} // end try
		}	// end for
		return memberInfoList.toArray(new String[1]);
	}
	
	/**
	 * Take an object of primitive type (like int, Integer etc) and convert it to double.
	 * 
	 * @param val
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static double toDouble(Object val) throws IllegalArgumentException {
		if (val instanceof Integer) return ((Integer)val).doubleValue();
		else if (val instanceof Double) return ((Double)val).doubleValue();
		else if (val instanceof Boolean) return (((Boolean)val) ? 1. : 0.); 
		else if (val instanceof Character) return ((Character)val).charValue(); 
		else if (val instanceof Byte) return ((Byte)val).doubleValue(); 
		else if (val instanceof Short) return ((Short)val).doubleValue(); 
		else if (val instanceof Long) return ((Long)val).doubleValue(); 
		else if (val instanceof Float) return ((Float)val).doubleValue(); 
		else if (val instanceof Void) return 0;
		throw new IllegalArgumentException("Illegal type, cant convert " + val.getClass() + " to double.");
	}
	
	/**
	 * Take a String and convert it to a destined data type using the appropriate function.
	 * 
	 * @param str
	 * @param destType
	 * @return
	 */
	public static Object stringToPrimitive(String str, Class<?> destType) throws NumberFormatException {
		if ((destType == Integer.class) || (destType == int.class)) return Integer.valueOf(str);
		else if ((destType == Double.class) || (destType == double.class)) return Double.valueOf(str);
		else if ((destType == Boolean.class) || (destType == boolean.class)) return Boolean.valueOf(str); 
		else if ((destType == Byte.class) || (destType == byte.class)) return Byte.valueOf(str);
		else if ((destType == Short.class) || (destType == short.class)) return Short.valueOf(str);
		else if ((destType == Long.class) || (destType == long.class)) return Long.valueOf(str);
		else if ((destType == Float.class) || (destType == float.class)) return Float.valueOf(str);
		else if ((destType == Character.class) || (destType == char.class)) return str.charAt(0);
		else {
			// if (destType == Void.class)
			System.err.println("warning, value interpreted as void type");
			return 0;
		}
	}
	
	/**
	 * Take a double value and convert it to a primitive object.
	 * 
	 * @param d
	 * @param destType
	 * @return
	 */
	public static Object doubleToPrimitive(Double d, Class<?> destType) {
		if ((destType == Double.class) || (destType == double.class)) return d;
		if ((destType == Integer.class) || (destType == int.class)) return new Integer(d.intValue());
		else if ((destType == Boolean.class) || (destType == boolean.class)) return (d!=0) ? Boolean.TRUE : Boolean.FALSE; 
		else if ((destType == Byte.class) || (destType == byte.class)) return new Byte(d.byteValue());
		else if ((destType == Short.class) || (destType == short.class)) return new Short(d.shortValue());
		else if ((destType == Long.class) || (destType == long.class)) return new Long(d.longValue());
		else if ((destType == Float.class) || (destType == float.class)) return new Float(d.floatValue());
		else { // this makes hardly sense...
			System.err.println("warning: converting from double to character or void...");
			if ((destType == Character.class) || (destType == char.class)) return new Character(d.toString().charAt(0));
			else //if (destType == Void.class) return 0;
				return 0;
		}
	}
	
	/**
	 * Checks whether a type belongs to primitive (int, long, double, char etc.) or the Java encapsulations (Integer, Long etc.)
	 * 
	 * @param cls
	 * @return
	 */
	public static boolean isJavaPrimitive(Class<?> cls) {
		if (cls.isPrimitive()) return true;
		if ((cls == Double.class) || (cls == Integer.class) || (cls == Boolean.class) 
				|| (cls == Byte.class) || (cls == Short.class) || (cls == Long.class) || (cls == Float.class)) return true;
		return false;
	}
	
	/**
	 * Try to convert an object to a destination type, especially for primitive types (int, double etc.
	 * but also Integer, Double etc.).
	 *  
	 * @param destType
	 * @param value
	 * @return
	 */
	public static Object decodeType(Class<?> destType, Object value) {
//		System.err.println("desttype: " + destType.toString() + ", val: " + value.getClass().toString());
		if (destType.isAssignableFrom(value.getClass())) {
			// value is already of destType or assignable (subclass), so just return it
			return value;
		}
		if (destType == String.class || destType == SelectedTag.class) {
			if (value.getClass() == String.class) return value;
			else return value.toString();
		} else if (isJavaPrimitive(destType)) {
			try {
				if (value.getClass() == String.class) {
					return stringToPrimitive((String)value, destType);
				} else {
					return doubleToPrimitive(toDouble(value), destType);
				}
			} catch(Exception e) {
				System.err.println("Error in converting type of " + value + " to " + destType.getName() + ": " + e.getMessage());
				return null;
			}
		}
		System.err.println("Error: unknown type, skipping decode " + value.getClass().getName() + " to " +  destType.getName());
		return value;
	}


	/**
	 * Try to get an object member value using the default getter.
	 * Returns null if not successful.
	 * 
	 * @param obj
	 * @param mem
	 * @return the value if successful, else null
	 */
	public static Object getMem(Object obj, String mem) {
		BeanInfo bi;    
		try {
			bi      = Introspector.getBeanInfo(obj.getClass());
		} catch(IntrospectionException e) {
			e.printStackTrace();
			return false;
		}
		PropertyDescriptor[] 	m_Properties = bi.getPropertyDescriptors();
		Method getter = null;
		for (int i = 0; i < m_Properties.length; i++) {
			if (m_Properties[i].getDisplayName().equals(mem)) {
				getter  = m_Properties[i].getReadMethod();
				break;
			}
		}
		if (getter != null) {
			try {
				return getter.invoke(obj, (Object[]) null);
			} catch (Exception e) {
				System.err.println("Exception in invoking setter: "+e.getMessage());
				return null;
			} 
		} else {
			System.err.println("Getter method for " + mem + " not found!");
			return null;
		}
	}
	
	/**
	 * Try to set an object member to a given value.
	 * Returns true if successful, else false. The types are adapted as generally as possible,
	 * converting using the decodeType() method.
	 * 
	 * @param obj
	 * @param mem
	 * @param val
	 * @return true if successful, else false
	 */
	public static boolean setMem(Object obj, String mem, Object val) {
		BeanInfo bi;    
		try {
			bi      = Introspector.getBeanInfo(obj.getClass());
		} catch(IntrospectionException e) {
			e.printStackTrace();
			return false;
		}
		PropertyDescriptor[] 	m_Properties = bi.getPropertyDescriptors();
//		Method getter = null;
		Method setter = null;
		Class<?> type = null;
//		System.err.println("looking at " + toString(obj));
		for (int i = 0; i < m_Properties.length; i++) {
			if (m_Properties[i].getDisplayName().equals(mem)) {
//				System.err.println("looking at " + m_Properties[i].getDisplayName());
//				getter  = m_Properties[i].getReadMethod();
				setter  = m_Properties[i].getWriteMethod();
				type 	= m_Properties[i].getPropertyType();
				break;
			}
		}
		if (setter != null) {
			try {
//				System.out.println("setting value...");
				Object[] args = new Object[]{ decodeType(type, val) };
				if (args[0] != null) {
					setter.invoke(obj, args);
					return true;
				} else  {
					System.err.println("no value to set");
					return false;
				}
			} catch (Exception e) {
				System.err.println("Exception in invoking setter: "+e.getMessage());
//				e.printStackTrace(); 
				return false;
			} 
		} else {
			System.err.println("Setter method for " + mem + " not found!");
			return false;
		}
	}

	/** This method simply looks for an appropriate tiptext
	 * @param name      The name of the property
	 * @param methods   A list of methods to search.
	 * @param target    The target object
	 * @return String for the tooltip.
	 */
	public static String getToolTipText(String name, MethodDescriptor[] methods, Object target, boolean stripToolTipToFirstPoint, int toHTMLLen) {
	    String result   = "";
	    String tipName  = name + "TipText";
	    for (int j = 0; j < methods.length; j++) {
	        String mname    = methods[j].getDisplayName();
	        Method meth     = methods[j].getMethod();
	        if (mname.equals(tipName)) {
	            if (meth.getReturnType().equals(String.class)) {
	                try {
	                    Object  args[]  = { };
		                String  tempTip = (String)(meth.invoke(target, args));
		                result = tempTip;
		                if (stripToolTipToFirstPoint) {
		                	int     ci      = tempTip.indexOf('.');
		                	if (ci > 0) result = tempTip.substring(0, ci);
		                }
	                } catch (Exception ex) {
	                }
	                break;
	            }
	        }
	    } // end for looking for tiptext
	    if (toHTMLLen > 0) return StringTools.toHTML(result, toHTMLLen);
	    else return result;
	}

	/** 
	 * This method simply looks for an appropriate tool tip text
	 * 
	 * @param name      The name of the property
	 * @param methods   A list of methods to search.
	 * @param target    The target object
	 * @return String for the tooltip.
	 */
	public static String getToolTipText(String name, MethodDescriptor[] methods, Object target) {
		return getToolTipText(name, methods, target, false, 0);
	}
}
