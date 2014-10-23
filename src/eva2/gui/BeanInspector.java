package eva2.gui;

import eva2.optimization.population.Population;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.tools.Pair;
import eva2.tools.SelectedTag;
import eva2.tools.StringTools;
import eva2.tools.Tag;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.beans.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Some miscellaneous functions to help with Beans, reflection, conversion and
 * generic display.
 */
public class BeanInspector {
    private static final Logger LOGGER = Logger.getLogger(BeanInspector.class.getName());

    /**
     * Check for equality based on bean properties of two target objects.
     */
    public static boolean equalProperties(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            System.out.println("");
            return false;
        }
        System.out.println("equalProperties: " + obj1.getClass().getName() + " " + obj2.getClass().getName());
        if (!obj1.getClass().getName().equals(obj2.getClass().getName())) {
            System.out.println("");
            return false;
        }
        // compare each of the properties !!
        BeanInfo beanInfo1;
        BeanInfo beanInfo2;
        PropertyDescriptor[] Properties_1 = null;
        PropertyDescriptor[] Properties_2 = null;
        try {

            beanInfo1 = Introspector.getBeanInfo(obj1.getClass());
            beanInfo2 = Introspector.getBeanInfo(obj2.getClass());
            Properties_1 = beanInfo1.getPropertyDescriptors();
            Properties_2 = beanInfo2.getPropertyDescriptors();
            beanInfo1.getMethodDescriptors();
        } catch (IntrospectionException ex) {
            LOGGER.log(Level.FINEST, "Could not introspect object.", ex);
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
            Object args_1[] = {};
            Object args_2[] = {};
            try {
                Object value_1 = getter_1.invoke(obj1, args_1);
                Object value_2 = getter_2.invoke(obj2, args_2);
                BeansInside = true;
                if (!BeanInspector.equalProperties(value_1, value_2)) {
                    BeansEqual = false;
                }
            } catch (Exception e) {
                System.out.println(" BeanTest.equalProperties " + e.getMessage());
            }
        }
        if (BeansInside) {
            return BeansEqual;
        }
        // here we have Integer or Double ...
        if (obj1 instanceof Integer
                || obj1 instanceof Boolean
                || obj1 instanceof Float
                || obj1 instanceof Double
                || obj1 instanceof Long
                || obj1 instanceof String) {
            return obj1.equals(obj2);
        }

        System.err.println(" Attention no match !!!");
        return true;
    }

    /**
     * Produce a String representation of an arbitrary object.
     *
     * @param obj
     * @return
     * @see #toString(Object, char, boolean, String)
     */
    public static String toString(Object obj) {
        return toString(obj, ';', false, "", 1, false);
    }

    /**
     * Produce a string with newlines and indentation (easier readable for if an
     * object has many properties).
     *
     * @param obj
     * @return
     */
    public static String niceToString(Object obj) {
        return toString(obj, ';', false, "    ", 1, true);
    }

    /**
     * Collect the accessible properties of an object and their values in a
     * string with indentations. Special cases: Arrays and Lists are
     * concatenations of their elements, Population is excepted from lists. If
     * the object has its own toString method, this one is preferred. Hidden or
     * expert properties are not shown.
     *
     * @param obj an arbitrary object
     * @return a String description of the object
     */
    public static String toString(Object obj, char delim, boolean tight, String indentStr) {
        return toString(obj, delim, tight, indentStr, 1, false);
    }

    /**
     * Collect the accessible properties of an object and their values in a
     * string. Special cases: Arrays and Lists are concatenations of their
     * elements, Population is excepted from lists. If the object has its own
     * toString method, this one is preferred. Hidden or expert properties are
     * not shown.
     *
     * @param obj Description of the Parameter
     * @return Description of the Return Value
     */
    private static String toString(Object obj, char delim, boolean tight, String indentStr, int indentDepth, boolean withNewlines) {
        if (obj == null) {
            return "null";
        }
        // try the object itself
        if (obj instanceof String) {
            return (String) obj;
        } // directly return a string object
        Class<?> type = obj.getClass();

        if (type.isArray()) { // handle the array case
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[");
            if (!tight) {
                sbuf.append(" ");
            }
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (withNewlines) {
                    sbuf.append('\n');
                }
                sbuf.append(toString(Array.get(obj, i), delim, tight, indentStr, indentDepth, withNewlines));
                if (i < len - 1) {
                    if (!tight) {
                        sbuf.append(" ");
                    }
                }
            }
            if (!tight) {
                sbuf.append(" ");
            }
            sbuf.append("]");
            return sbuf.toString();
        }

        if (type.isEnum()) {
            return makeIndent(indentStr, indentDepth) + obj.toString();
        }

        if (obj instanceof List && !(obj instanceof Population)) { // handle the list case
            StringBuffer sbuf = new StringBuffer();
            if (withNewlines) {
                sbuf.append('\n');
            }
            addIndent(sbuf, indentStr, indentDepth);
            sbuf.append("[");
            if (!tight) {
                sbuf.append(" ");
            }
            List<?> lst = (List<?>) obj;
            for (Object o : lst) {
                sbuf.append(o.toString());
                sbuf.append(delim);
                if (!tight) {
                    sbuf.append(" ");
                }
            }
            if (!tight && (sbuf.charAt(sbuf.length() - 2) == delim)) {
                sbuf.setCharAt(sbuf.length() - 2, ' ');
            } // delete the delim
            sbuf.setCharAt(sbuf.length() - 1, ']');
            return sbuf.toString();
        }

        Method[] methods = obj.getClass().getDeclaredMethods();
        for (int ii = 0; ii < methods.length; ii++) { // check if the object has its own toString method, in this case use it
            if ((methods[ii].getName().equals("toString") /*|| (methods[ii].getName().equals("getStringRepresentation"))*/) && (methods[ii].getParameterTypes().length == 0)) {
                Object[] args = new Object[0];
                try {
                    String ret = (String) methods[ii].invoke(obj, args);
                    return makeIndent(indentStr, indentDepth) + ret;
                } catch (Exception e) {
                    System.err.println(" ERROR +" + e.getMessage());
                }
            }
        }

        // otherwise try introspection and collect all public properties as strings

        Pair<String[], Object[]> nameVals = getPublicPropertiesOf(obj, true, true);

        StringBuffer sbuf = new StringBuffer();
        if (withNewlines) {
            sbuf.append('\n');
        }
        addIndent(sbuf, indentStr, indentDepth);
        sbuf.append(type.getName());
        sbuf.append("{");
        for (int i = 0; i < nameVals.head.length; i++) {
            if (nameVals.head[i] != null) {
                if (withNewlines) {
                    sbuf.append('\n');
                }
                addIndent(sbuf, indentStr, indentDepth);
                sbuf.append(nameVals.head[i]);
                sbuf.append("=");
                sbuf.append(toString(nameVals.tail[i], delim, tight, indentStr, indentDepth + 1, withNewlines));
                sbuf.append(delim);
                if (!tight) {
                    sbuf.append(" ");
                }
            }
        }

        sbuf.append("}");
        return sbuf.toString();
    }

    private static void addIndent(StringBuffer sbuf, String indentStr, int indentDepth) {
        if (indentStr != null && (indentDepth > 0)) {
            for (int i = 0; i < indentDepth; i++) {
                sbuf.append(indentStr);
            }
        }
    }

    private static String makeIndent(String indentStr, int indentDepth) {
        if (indentStr != null) {
            if (indentDepth < 1) {
                return "";
            } else {
                StringBuilder sbuf = new StringBuilder(indentStr);
                for (int i = 2; i <= indentDepth; i++) {
                    sbuf.append(indentStr);
                }
                return sbuf.toString();
            }
        } else {
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println(BeanInspector.toString(new String[]{"asdf", "jdksfl", "werljk"}));

        System.out.println(BeanInspector.toString(new Population()));
        System.out.println(BeanInspector.toString(new GeneticAlgorithm()));
        System.out.println("----");
        System.out.println(BeanInspector.niceToString(new Population()));
        System.out.println(BeanInspector.niceToString(new GeneticAlgorithm()));
        System.out.println("----");
        System.out.println(BeanInspector.toString(new Population(), ';', false, ">", 1, false));
        System.out.println(BeanInspector.toString(new GeneticAlgorithm(), ';', false, ">", 1, false));
    }

    /**
     * Retrieve names and values of instance fields which are accessible by
     * getter method, optionally by both getter and setter method. The returned
     * arrays may contain null entries. Properties marked as hidden or expert
     * are skipped.
     *
     * @param target
     * @return
     */
    public static Pair<String[], Object[]> getPublicPropertiesOf(Object target, boolean requireSetter, boolean showHidden) {
        BeanInfo Info = null;
        PropertyDescriptor[] Properties = null;
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
            if ((Properties[i].isHidden() && !showHidden) || Properties[i].isExpert()) {
                continue;
            }
            String name = Properties[i].getDisplayName();
            Method getter = Properties[i].getReadMethod();
            Method setter = Properties[i].getWriteMethod();
            // Only display read/write properties.
            if (getter == null || (setter == null && requireSetter)) {
                continue;
            }
            Object args[] = {};

            try {
                nameArray[i] = name;
                valArray[i] = getter.invoke(target, args);
            } catch (Exception e) {
                System.err.println("BeanTest ERROR +" + e.getMessage());
            }
        }
        Pair<String[], Object[]> nameVals = new Pair<>(nameArray, valArray);
        return nameVals;
    }

    /**
     * @param obj Description of the Parameter
     */
    public static void showInfo(Object obj) {
        System.out.println("Inspecting " + obj.getClass().getName());
        // object itself
        try {
            if (obj instanceof java.lang.Integer) {
                System.out.println(" Prop = Integer" + obj.toString());
            }
            if (obj instanceof java.lang.Boolean) {
                System.out.println(" Prop = Boolean" + obj.toString());
            }
            if (obj instanceof java.lang.Long) {
                System.out.println(" Prop = Long" + obj.toString());
            }
            if (obj instanceof java.lang.Double) {
                System.out.println(" Prop = Long" + obj.toString());
            }
        } catch (Exception e) {
            //System.out.println(" ERROR +"+ e.getMessage());
        }
        // then the properties
        BeanInfo Info = null;
        PropertyDescriptor[] Properties = null;
        try {
            Info = Introspector.getBeanInfo(obj.getClass());
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
            Method getter = Properties[i].getReadMethod();
            Method setter = Properties[i].getWriteMethod();
            // Only display read/write properties.
            if (getter == null || setter == null) {
                continue;
            }
            Object args[] = {};
            try {
                Object value = getter.invoke(obj, args);
                System.out.println("Inspecting name = " + name);
                if (value instanceof Integer) {
                    Object args2[] = {new Integer(999)};
                    setter.invoke(obj, args2);
                }
                showInfo(value);
            } catch (Exception e) {
                System.out.println("BeanTest ERROR +" + e.getMessage());
            }
        }
    }

    /**
     * Call a method by a given name with given arguments, if the method is
     * available. Returns the return values of the call or null if it isnt
     * found. This of course means that the caller is unable to distinguish
     * between "method not found" and "method found and it returned null".
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
            } catch (Exception e) {
                System.err.println("Error on calling method " + mName + " on " + obj.getClass().getName());
                System.err.println("Object: " + obj.toString() + ", method name: " + mName);
                System.err.println("Arguments were " + BeanInspector.toString(args));
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Produce an array of Class instances matching the types of the given
     * object array.
     *
     * @param o
     * @return
     */
    public static Class[] toClassArray(Object[] o) {
        if (o == null) {
            return null;
        }
        Class[] clz = new Class[o.length];
        for (int i = 0; i < o.length; i++) {
            clz[i] = o[i].getClass();
        }
        return clz;
    }

    /**
     * Check whether an object has a method by the given name and with matching
     * signature considering the arguments. Return it if found, or null if not.
     *
     * @param obj
     * @param mName the method name
     * @param args  the arguments, null allowed if the method takes no parameters
     * @return the method or null if it isn't found
     */
    public static Method hasMethod(Object obj, String mName, Object[] args) {
        return hasMethod(obj, mName, toClassArray(args));
    }

    /**
     * Check whether an object has a method by the given name and with the given
     * parameter signature. Return it if found, or null if not.
     *
     * @param obj
     * @param mName      the method name
     * @param paramTypes the parameter types, null allowed if no parameters are
     *                   expected
     * @return the method or null if it isn't found
     */
    public static Method hasMethod(Object obj, String mName, Class[] paramTypes) {
        Class<?> cls = obj.getClass();
        Method[] meths = cls.getMethods();
        for (Method method : meths) {
            if (method.getName().equals(mName)) { // name match
                Class[] methParamTypes = method.getParameterTypes();
                if (paramTypes == null && methParamTypes.length == 0) {
                    return method;
                } // full match
                else {
                    if (paramTypes != null && (methParamTypes.length == paramTypes.length)) {
                        boolean mismatch = false;
                        int i = 0;
                        while ((i < methParamTypes.length) && (!mismatch)) {
                            if (!methParamTypes[i].isAssignableFrom(paramTypes[i]) && !isBoxableFrom(methParamTypes[i], paramTypes[i])) {
                                mismatch = true;
                            }
                            i++;
                        }
                        if (!mismatch) {
                            return method;
                        } // parameter match, otherwise search on
                    } // parameter mismatch, search on
                }
            }
        }
        return null;
    }

    /**
     * Check if the given first class is a primitive type and can be boxed to
     * match the second class.
     *
     * @param clz1
     * @param clz2
     * @return
     */
    private static boolean isBoxableFrom(Class clz1, Class clz2) {
        Class box = getBoxedType(clz1);
        return box != null && (clz2.isAssignableFrom(box));
    }

    /**
     * For a primitive type, return the boxed referenced type. Return null for
     * any non-primitive type.
     *
     * @param cls
     * @return
     */
    public static Class getBoxedType(Class cls) {
        if (cls.isPrimitive()) {
            if (cls == double.class) {
                return Double.class;
            } else if (cls == char.class) {
                return Character.class;
            } else if (cls == int.class) {
                return Integer.class;
            } else if (cls == boolean.class) {
                return Boolean.class;
            } else if (cls == byte.class) {
                return Byte.class;
            } else if (cls == short.class) {
                return Short.class;
            } else if (cls == long.class) {
                return Long.class;
            } else if (cls == float.class) {
                return Float.class;
            } else {
                return Void.class;
            }
        } else {
            return null;
        }
    }

    /**
     * For a Java primitive wrapper class return the corresponding primitive
     * class.
     */
    public static Class getUnboxedType(Class cls) {
        if (cls == Double.class) {
            return double.class;
        } else if (cls == Character.class) {
            return char.class;
        } else if (cls == Integer.class) {
            return int.class;
        } else if (cls == Boolean.class) {
            return boolean.class;
        } else if (cls == Byte.class) {
            return byte.class;
        } else if (cls == Short.class) {
            return short.class;
        } else if (cls == Long.class) {
            return long.class;
        } else if (cls == Float.class) {
            return float.class;
        } else if (cls == Void.class) {
            return void.class;
        } else {
            return null;
        }
    }

    /**
     * Just concatenates getClassDescription(obj) and getMemberDescriptions(obj,
     * withValues).
     *
     * @param obj        target object
     * @param withValues if true, member values are displayed as well
     * @return an info string about class and members of the given object
     */
    public static String getDescription(Object obj, boolean withValues) {
        StringBuilder sbuf = new StringBuilder(getClassDescription(obj));
        sbuf.append("\n");
        String[] mems = getMemberDescriptions(obj, withValues);
        for (String str : mems) {
            sbuf.append(str);
        }
        return sbuf.toString();
    }

    /**
     * Check for info methods on the object to be provided by the developer and
     * return their text as String.
     *
     * @param obj
     * @return String information about the object's class
     */
    public static String getClassDescription(Object obj) {
        StringBuilder infoBf = new StringBuilder("Type: ");
        infoBf.append(obj.getClass().getName());
        infoBf.append("\t");

        Object args[] = {};
        Object ret;

        for (String meth : new String[]{"getName", "globalInfo"}) {
            ret = callIfAvailable(obj, meth, args);
            if (ret != null) {
                infoBf.append("\t");
                infoBf.append((String) ret);
            } else {
                Description description = obj.getClass().getAnnotation(Description.class);
                if (description != null) {
                    infoBf.append("\t");
                    infoBf.append(description.value());
                }
            }

        }

        return infoBf.toString();
    }

    /**
     * Return an info string on the members of the object class, containing
     * name, type, optional value and tool tip text if available. The type is
     * accompanied by a tag "common" or "restricted", indicating whether the
     * member property is normal or hidden, meaning it may have effect depending
     * on settings of other members only, for instance.
     *
     * @param obj        target object
     * @param withValues if true, member values are displayed as well
     * @return an info string about class and members of the given object
     */
    public static String[] getMemberDescriptions(Object obj, boolean withValues) {
        BeanInfo bi;
        try {
            bi = Introspector.getBeanInfo(obj.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
        PropertyDescriptor[] propertyDescriptors = bi.getPropertyDescriptors();
        ArrayList<String> memberInfoList = new ArrayList<>();

        for (int i = 0; i < propertyDescriptors.length; i++) {
            if (propertyDescriptors[i].isExpert()) {
                continue;
            }

            String name = propertyDescriptors[i].getDisplayName();

            Method getter = propertyDescriptors[i].getReadMethod();
            Method setter = propertyDescriptors[i].getWriteMethod();
            // Only display read/write properties.
            if (getter == null || setter == null) {
                continue;
            }

            try {
                Object args[] = {};
                Object value = getter.invoke(obj, args);

                // Don't try to set null values:
                if (value == null) {
                    // If it's a user-defined property we give a warning.
                    String getterClass = propertyDescriptors[i].getReadMethod().getDeclaringClass().getName();
                    if (getterClass.indexOf("java.") != 0) {
                        System.err.println("Warning: Property \"" + name + "\" has null initial value.  Skipping.");
                    }
                    continue;
                }

                StringBuilder memberInfoBf = new StringBuilder(40);
                memberInfoBf.append("Member:\t");
                memberInfoBf.append(name);

                memberInfoBf.append("\tType: ");

                if (propertyDescriptors[i].isHidden()) {
                    memberInfoBf.append("restricted, ");
                } else {
                    memberInfoBf.append("common, ");
                }
                String typeName = value.getClass().getName();
                if (value instanceof SelectedTag) {
                    Tag[] tags = ((SelectedTag) value).getTags();
                    memberInfoBf.append("String in {");
                    for (int k = 0; k < tags.length; k++) {
                        memberInfoBf.append(tags[k].getString());
                        if (k + 1 < tags.length) {
                            memberInfoBf.append(", ");
                        }
                    }
                    memberInfoBf.append("}");
                } else {
                    memberInfoBf.append(typeName);
                }

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
                System.err.println("Skipping property " + name + " ; exception: " + ex.getMessage());
                ex.printStackTrace();
            } // end try
        }    // end for
        return memberInfoList.toArray(new String[1]);
    }

    /**
     * Take an object of primitive type (like int, Integer etc) and convert it
     * to double.
     *
     * @param val
     * @return
     * @throws IllegalArgumentException
     */
    public static double toDouble(Object val) throws IllegalArgumentException {
        if (val instanceof Integer) {
            return ((Integer) val).doubleValue();
        } else if (val instanceof Double) {
            return (Double) val;
        } else if (val instanceof Boolean) {
            return (((Boolean) val) ? 1. : 0.);
        } else if (val instanceof Character) {
            return (Character) val;
        } else if (val instanceof Byte) {
            return ((Byte) val).doubleValue();
        } else if (val instanceof Short) {
            return ((Short) val).doubleValue();
        } else if (val instanceof Long) {
            return ((Long) val).doubleValue();
        } else if (val instanceof Float) {
            return ((Float) val).doubleValue();
        } else if (val instanceof Void) {
            return 0;
        }
        throw new IllegalArgumentException("Illegal type, cant convert " + val.getClass() + " to double.");
    }

    /**
     * Take a String and convert it to a destined data type using the
     * appropriate function.
     *
     * @param str
     * @param destType
     * @return
     */
    public static Object stringToPrimitive(String str, Class<?> destType) throws NumberFormatException {
        if ((destType == Integer.class) || (destType == int.class)) {
            return Integer.valueOf(str);
        } else if ((destType == Double.class) || (destType == double.class)) {
            return Double.valueOf(str);
        } else if ((destType == Boolean.class) || (destType == boolean.class)) {
            return Boolean.valueOf(str);
        } else if ((destType == Byte.class) || (destType == byte.class)) {
            return Byte.valueOf(str);
        } else if ((destType == Short.class) || (destType == short.class)) {
            return Short.valueOf(str);
        } else if ((destType == Long.class) || (destType == long.class)) {
            return Long.valueOf(str);
        } else if ((destType == Float.class) || (destType == float.class)) {
            return Float.valueOf(str);
        } else if ((destType == Character.class) || (destType == char.class)) {
            return str.charAt(0);
        } else {
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
        if ((destType == Double.class) || (destType == double.class)) {
            return d;
        }
        if ((destType == Integer.class) || (destType == int.class)) {
            return d.intValue();
        } else if ((destType == Boolean.class) || (destType == boolean.class)) {
            return (d != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else if ((destType == Byte.class) || (destType == byte.class)) {
            return d.byteValue();
        } else if ((destType == Short.class) || (destType == short.class)) {
            return d.shortValue();
        } else if ((destType == Long.class) || (destType == long.class)) {
            return d.longValue();
        } else if ((destType == Float.class) || (destType == float.class)) {
            return d.floatValue();
        } else { // this makes hardly sense...
            System.err.println("warning: converting from double to character or void...");
            if ((destType == Character.class) || (destType == char.class)) {
                return d.toString().charAt(0);
            } else {
                return 0;
            }
        }
    }

    /**
     * Checks whether a type belongs to primitive (int, long, double, char etc.)
     * or the Java encapsulations (Integer, Long etc.)
     *
     * @param cls
     * @return
     */
    public static boolean isJavaPrimitive(Class<?> cls) {
        if (cls.isPrimitive()) {
            return true;
        }
        return (cls == Double.class) || (cls == Integer.class) || (cls == Boolean.class) || (cls == Character.class) || (cls == Void.class)
                || (cls == Byte.class) || (cls == Short.class) || (cls == Long.class) || (cls == Float.class);
    }

    /**
     * Get the primitive class of a Java primitive encapsulation, or null if not
     * applicable. E.g., returns int for Integer, long for Long, Boolean for
     * Boolean etc.
     *
     * @param cls
     * @return
     */
    public static Class getJavaPrimitive(Class<?> cls) {
        if (cls.isPrimitive()) {
            return cls;
        }
        if (cls == Double.class) {
            return double.class;
        } else if (cls == Integer.class) {
            return int.class;
        } else if (cls == Boolean.class) {
            return Boolean.class;
        } else if (cls == Byte.class) {
            return byte.class;
        } else if (cls == Short.class) {
            return short.class;
        } else if (cls == Long.class) {
            return long.class;
        } else if (cls == Float.class) {
            return float.class;
        }
        return null;
    }

    /**
     * Try to convert an object to a destination type, especially for primitive
     * types (int, double etc. but also Integer, Double etc.).
     *
     * @param destType
     * @param value
     * @return
     */
    public static Object decodeType(Class<?> destType, Object value) {
        if (destType.isAssignableFrom(value.getClass())) {
            // value is already of destType or assignable (subclass), so just return it
            return value;
        }
        if (destType == String.class || destType == SelectedTag.class) {
            if (value.getClass() == String.class) {
                return value;
            } else {
                return value.toString();
            }
        } else if (isJavaPrimitive(destType)) {
            try {
                if (value.getClass() == String.class) {
                    return stringToPrimitive((String) value, destType);
                } else {
                    return doubleToPrimitive(toDouble(value), destType);
                }
            } catch (Exception e) {
                System.err.println("Error in converting type of " + value + " to " + destType.getName() + ": " + e.getMessage());
                return null;
            }
        }
        System.err.println("Error: unknown type, skipping decode " + value.getClass().getName() + " to " + destType.getName());
        return value;
    }

    /**
     * Try to get an object member value using the default getter. Returns null
     * if not successful.
     *
     * @param obj
     * @param mem
     * @return the value if successful, else null
     */
    public static Object getMem(Object obj, String mem) {
        BeanInfo bi;
        try {
            bi = Introspector.getBeanInfo(obj.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return false;
        }
        PropertyDescriptor[] properties = bi.getPropertyDescriptors();
        Method getter = null;
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getDisplayName().equals(mem)) {
                getter = properties[i].getReadMethod();
                break;
            }
        }
        if (getter != null) {
            try {
                return getter.invoke(obj, (Object[]) null);
            } catch (Exception e) {
                System.err.println("Exception in invoking setter: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("Getter method for " + mem + " not found!");
            return null;
        }
    }

    /**
     * Try to set an object member to a given value. Returns true if successful,
     * else false. The types are adapted as generally as possible, converting
     * using the decodeType() method.
     *
     * @param obj
     * @param mem
     * @param val
     * @return true if successful, else false
     */
    public static boolean setMem(Object obj, String mem, Object val) {
        BeanInfo bi;
        try {
            bi = Introspector.getBeanInfo(obj.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return false;
        }
        PropertyDescriptor[] properties = bi.getPropertyDescriptors();
        Method setter = null;
        Class<?> type = null;
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getDisplayName().equals(mem)) {
                setter = properties[i].getWriteMethod();
                type = properties[i].getPropertyType();
                break;
            }
        }
        if (setter != null) {
            try {
                Object[] args = new Object[]{decodeType(type, val)};
                if (args[0] != null) {
                    setter.invoke(obj, args);
                    return true;
                } else {
                    System.err.println("no value to set");
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Exception in invoking setter: " + e.getMessage());
                return false;
            }
        } else {
            System.err.println("Setter method for " + mem + " not found!");
            return false;
        }
    }

    /**
     * This method simply looks for an appropriate tiptext
     *
     * @param name    The name of the property
     * @param methods A list of methods to search.
     * @param target  The target object
     * @return String for the ToolTip.
     */
    public static String getToolTipText(String name, MethodDescriptor[] methods, Object target, boolean stripToolTipToFirstPoint, int toHTMLLen) {
        String result = "";
        String tipName = name + "TipText";

        // Find by annotation
        Parameter[] parameters= target.getClass().getAnnotationsByType(Parameter.class);
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class) && field.getName().equals(name)) {
                Parameter parameter = field.getAnnotation(Parameter.class);
                return parameter.description();
            }
        }

        // Find by deprecated TipText method
        for (int j = 0; j < methods.length; j++) {
            String mname = methods[j].getDisplayName();
            Method meth = methods[j].getMethod();

            if (mname.equals(tipName)) {
                if (meth.getReturnType().equals(String.class)) {
                    try {
                        Object args[] = {};
                        String tempTip = (String) (meth.invoke(target, args));
                        result = tempTip;
                        if (stripToolTipToFirstPoint) {
                            int ci = tempTip.indexOf('.');
                            if (ci > 0) {
                                result = tempTip.substring(0, ci);
                            }
                        }
                    } catch (Exception ex) {

                    }
                    break;
                }
            }
        } // end for looking for tiptext

        if(result.isEmpty()) {
            LOGGER.fine(String.format("No ToolTip for %s.%s available.", target.getClass().getName(), name));
        }

        if (toHTMLLen > 0) {
            return StringTools.toHTML(result, toHTMLLen);
        } else {
            return result;
        }
    }

    /**
     * This method simply looks for an appropriate tool tip text
     *
     * @param name    The name of the property
     * @param methods A list of methods to search.
     * @param target  The target object
     * @return String for the ToolTip.
     */
    public static String getToolTipText(String name, MethodDescriptor[] methods, Object target) {
        return getToolTipText(name, methods, target, false, 0);
    }
}
