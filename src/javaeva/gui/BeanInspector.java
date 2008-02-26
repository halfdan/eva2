package javaeva.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 202 $
 *            $Date: 2007-10-25 16:12:49 +0200 (Thu, 25 Oct 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.beans.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/*
 *  ==========================================================================*
 *  CLASS DECLARATION
 *  ==========================================================================
 */
public class BeanInspector {
	public static boolean TRACE = false;

//	public static int  step = 0;
//	public static String check(String s) {
//
//		s=s.replace('$','_');
//		s=s.replace(';','_');
////		String ret = null;
////		try {
////		RE r = new RE("\\[");
////		ret = r.subst(s,"");
////		//ret.substring();
////		//ret
////		} catch (Exception e) {e.getMessage();};
////		System.out.println("s="+s+"  ret"+ret);
//		if (s.equals("[D")) return "Double_Array";
//		if (s.startsWith("[D")) return s.substring(2);
//		if (s.startsWith("[L")) return s.substring(2);
//
//		return s;
//	}

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


	/**
	 * Collect the accessible properties of an object and their values in a string.
	 * 
	 * @param  Target  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	public static String toString(Object Target) {
		String ret = "";
		if (Target == null) return "null";
		// try the object itself
		if (Target instanceof String) return (String)Target; // directly return a string object
		Class<? extends Object> type = Target.getClass();
		
		if (type.isArray()) { // handle the array case
			StringBuffer sbuf = new StringBuffer("[ ");
			int len = Array.getLength(Target);
			for (int i=0; i<len; i++) {
				sbuf.append(toString(Array.get(Target, i)));
				if (i<len-1) sbuf.append("; ");
			}
			sbuf.append(" ]");
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
		BeanInfo Info = null;
		PropertyDescriptor[] Properties = null;
//		MethodDescriptor[] Methods = null;
		try {
			Info = Introspector.getBeanInfo(Target.getClass());
			Properties = Info.getPropertyDescriptors();
			Info.getMethodDescriptors();
		} catch (IntrospectionException ex) {
			System.err.println("BeanTest: Couldn't introspect");
			return ret;
		}

		StringBuffer sbuf = new StringBuffer(type.getName());
		sbuf.append("{");
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
			if (getter == null || setter == null) {
				continue;
			}
			//System.out.println("name = "+name );
			//System.out.println("type = "+type.getName() );
			Object args[] = {};
			//System.out.println("m_Target"+m_Target.toString());

			try {
				Object value = getter.invoke(Target, args);
				sbuf.append(name);
				sbuf.append("=");
				sbuf.append(toString(value));
				sbuf.append("; ");
			} catch (Exception e) {
				System.err.println("BeanTest ERROR +"+ e.getMessage());
				return sbuf.toString();
			}
		}
		sbuf.append("}");
		return sbuf.toString();
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
	
    
    public static Object callIfAvailable(Object obj, String mName, Object[] args) {
    	Method meth = hasMethod(obj, mName);
    	if (meth != null) {
    		try {
    			return meth.invoke(obj, args);
    		} catch(Exception e) {
    			System.err.println("Error on calling method "+mName + " on " + obj.getClass().getName());
    			return null;
    		}
    	} else return null;
    }
    
    public static Method hasMethod(Object obj, String mName) {
    	Class cls = obj.getClass();
    	Method[] meths = cls.getMethods();
    	for (Method method : meths) {
			if (method.getName().equals(mName)) return method;
		}
    	return null;
    }
}

