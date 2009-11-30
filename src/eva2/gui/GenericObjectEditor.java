package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 266 $
 *            $Date: 2007-11-20 14:33:48 +0100 (Tue, 20 Nov 2007) $
 *            $Author: mkron $
 */


import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import eva2.EvAInfo;
import eva2.client.EvAClient;
import eva2.tools.ReflectPackage;
import eva2.tools.jproxy.RMIProxyLocal;



/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
public class GenericObjectEditor implements PropertyEditor {
	static final public boolean TRACE = false;

	private Object m_Object;
	private Object m_Backup;
	private PropertyChangeSupport m_Support = new PropertyChangeSupport(this);
	private Class<?> m_ClassType;
	private GOEPanel m_EditorComponent;
	private boolean m_Enabled = true;
	
	/**
	 * Read the classes available for user selection from the properties or the classpath respectively
	 */
	public static ArrayList<String> getClassesFromProperties(String className) {
		if (TRACE) System.out.println("getClassesFromProperties - requesting className: "+className);
		
		// Try to read the predefined classes from the props file.
		String typeOptions = EvAClient.getProperty(className);
		if (typeOptions == null) {
			// If none are defined, all assignable classes are searched the hard way, using the ReflectPackage 
			return getClassesFromClassPath(className);
		} else {
			StringTokenizer st = new StringTokenizer(typeOptions, ", ");
			ArrayList<String> classes = new ArrayList<String>();
			while (st.hasMoreTokens()) {
				String current = st.nextToken().trim();
				//System.out.println("current ="+current);
				try {
					Class.forName(current); // test for instantiability
					classes.add(current);
				} catch (Exception ex) {
					System.err.println("Couldn't load class with name: " + current);
					System.err.println("ex:"+ex.getMessage());
					ex.printStackTrace();
				}
			}
			return classes;
		}
	}
	
	/**
	 * Return the names of all classes in the same package that are assignable
	 * from the named class, and that can be loaded through the classpath.
	 * If a class has a declared field called "hideFromGOE" this method will skip it.
	 * Abstract classes and interfaces will be skipped as well.
	 * 
	 * @see ReflectPackage.getAssignableClassesInPackage
	 * @param className
	 * @return
	 */
	public static ArrayList<String> getClassesFromClassPath(String className) { 
		ArrayList<String> classes = new ArrayList<String>();
		Class<?>[] clsArr;
		clsArr=ReflectPackage.getAssignableClasses(className, true, true);
		if (clsArr == null) {
			System.err.println("Warning: No assignable classes found in property file or on classpath: "
					+EvAInfo.propertyFile + " for "+className);
			classes.add(className);
		} else {
			for (Class<?> class1 : clsArr) {
				int m = class1.getModifiers();
				try {
					// a field allowing a class to indicate it doesnt want to be displayed
					Field f = class1.getDeclaredField("hideFromGOE");
					if (f.getBoolean(class1) == true) {
						if (TRACE) System.out.println("Class " + class1 + " wants to be hidden from GOE, skipping...");
						continue;
					}
				} catch (Exception e) {

				} catch (Error e) {
					System.err.println("Error on checking fields of " + class1 + ": " + e);
					continue;
				}
				//					if (f)
				if (!Modifier.isAbstract(m) && !class1.isInterface()) {	// dont take abstract classes or interfaces
					try {
						Class<?>[] params = new Class[0];
						class1.getConstructor(params);
						classes.add(class1.getName());
					} catch (NoSuchMethodException e) {
						System.err.println("GOE warning: Class " + class1.getName() + " has no default constructor, skipping...");
					}
				}
			}
		}
		return classes;
	}
	
	/**
	 * Hide or show the editable property of a class, this makes sense for classes
	 * which are represented visually using the GenericObjectEditor.
	 * Returns false, if an error occurs, else true.
	 * An instance may call this statically on itself by means of this.getClass().
	 * Actually this only sets the hidden property of the java bean which is checked in the
	 * wasModified method of PropertySheetPanel.
	 *
	 * @param cls	class the property belongs to
	 * @param property	string name of the property	
	 * @param hide	desired value to set, true for hidden, false for visible 
	 * @return false, if an error occurs, else true
	 */
	public static boolean setExpertProperty(Class<?> cls, String property, boolean expertValue) {
		try {
			BeanInfo    bi      = Introspector.getBeanInfo(cls);
			PropertyDescriptor[] props = bi.getPropertyDescriptors();
			for (int i=0; i<props.length; i++) {
				if ((props[i].getName().equals(property))) { 
					if (expertValue != props[i].isExpert()) props[i].setExpert(expertValue);
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println("exception in setHideProperty for " + cls.getName() + "/" + property + " : " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Hide or show the editable property of a class, this makes sense for classes
	 * which are represented visually using the GenericObjectEditor.
	 * Returns false, if an error occurs, else true.
	 * An instance may call this statically on itself by means of this.getClass().
	 * Actually this only sets the hidden property of the java bean which is checked in the
	 * wasModified method of PropertySheetPanel.
	 *
	 * @param cls	class the property belongs to
	 * @param property	string name of the property	
	 * @param hide	desired value to set, true for hidden, false for visible 
	 * @return false, if an error occurs, else true
	 */
	public static boolean setHideProperty(Class<?> cls, String property, boolean hide) {
		try {
			BeanInfo    bi      = Introspector.getBeanInfo(cls);
			PropertyDescriptor[] props = bi.getPropertyDescriptors();
			for (int i=0; i<props.length; i++) {
				if ((props[i].getName().equals(property))) { 
					if (hide != props[i].isHidden()) {
						props[i].setHidden(hide);
					}
					return true;
				}
			}
			System.err.println("Error: property " + property + " not found!");
			return false;
		} catch (Exception e) {
			System.err.println("exception in setHideProperty for " + cls.getName() + "/" + property + " : " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Hide or unhide all properties of a given class. Added to avoid the problem with hidden
	 * properties of inherited classes hide the property for all classes within the same inheritance tree.
	 * 
	 * @param cls
	 * @param hide
	 */
	public static void setHideAllProperties(Class<?> cls, boolean hide) {
		try {
			BeanInfo    bi      = Introspector.getBeanInfo(cls);
			PropertyDescriptor[] props = bi.getPropertyDescriptors();
			for (int i=0; i<props.length; i++) {
				props[i].setHidden(hide);
			}
		} catch (Exception e) {
			System.err.println("exception in setHideProperty for " + cls.getName() + "/all : " + e.getMessage());
		}
	}
	
	/**
	 * Convenience-method. See setHideProperty.
	 * 
	 * @param cls
	 * @param property
	 * @param show
	 * @return
	 */
	public static boolean setShowProperty(Class<?> cls, String property, boolean show) {
		return GenericObjectEditor.setHideProperty(cls, property, !show);
	}
	
	/**
	 * Sets whether the editor is "enabled", meaning that the current
	 * values will be painted.
	 *
	 * @param newVal a value of type 'boolean'
	 */
	public void setEnabled(boolean newVal) {
		if (newVal != m_Enabled) {
			m_Enabled = newVal;
		}
	}

	/**
	 * Sets the class of values that can be edited.
	 *
	 * @param type a value of type 'Class'
	 */
	public void setClassType(Class<?> type) {
		if (TRACE) System.out.println("GOE setClassType("+ (type == null? "<null>" : type.getName()) + ")");
		m_ClassType = type;
		if (m_EditorComponent != null)
			m_EditorComponent.updateClassType();

	}

	public Class<?> getClassType() {
		return m_ClassType;
	}
	
	/**
	 * Sets the current object to be the default, taken as the first item in
	 * the chooser
	 */
	public void setDefaultValue() {
		if (m_ClassType == null) {
			System.err.println("No ClassType set up for GenericObjectEditor!!");
			return;
		}
		
		Vector<String> v=null;
		if (Proxy.isProxyClass(m_ClassType)) {
			if (TRACE) System.out.println("PROXY! original was " + ((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_Object))).getOriginalClass().getName());
			v = new Vector<String>(getClassesFromProperties(((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_Object))).getOriginalClass().getName()));
		} else {		
			v = new Vector<String>(getClassesFromProperties(m_ClassType.getName()));
		}
				
//		v = new Vector<String>(getClassesFromProperties(m_ClassType.getName()));
		try {
			if (v.size() > 0)
				setObject((Object)Class.forName((String)v.get(0)).newInstance());
		} catch (Exception ex) {
			System.err.println("Exception in setDefaultValue !!!"+ex.getMessage());
		}
	}

	/**
	 * Sets the current Object. If the Object is in the
	 * Object chooser, this becomes the selected item (and added
	 * to the chooser if necessary).
	 *
	 * @param o an object that must be a Object.
	 */
	public void setValue(Object o) {
		//System.err.println("setValue()" + m_ClassType.toString());

		if (m_ClassType == null) {
			System.err.println("No ClassType set up for GenericObjectEditor!!");
			return;
		}
		if (!m_ClassType.isAssignableFrom(o.getClass())) {
			if (m_ClassType.isPrimitive()) {
				System.err.println("setValue object not of correct type! Expected "+m_ClassType.getName()+", got " + o.getClass().getName());
				System.err.println("setting primitive type");
				setObject((Object)o);
				//throw new NullPointerException("ASDF");
			} else {
				System.err.println("setValue object not of correct type! Expected "+m_ClassType.getName()+", got " + o.getClass().getName());
			}
			return;
		}

		setObject((Object)o);
		if (m_EditorComponent != null)
			m_EditorComponent.updateChooser();

	}

	/**
	 * Sets the current Object, but doesn't worry about updating
	 * the state of the Object chooser.
	 *
	 * @param c a value of type 'Object'
	 */
	private void setObject(Object c) {
		// This should really call equals() for comparison.
		if (TRACE) System.out.println("setObject "+ c.getClass().getName());
		boolean trueChange = (c != getValue());
		//System.err.println("Didn't even try to make a Object copy!! "+ "(using original)");

		m_Backup = m_Object;
		m_Object = c;

		if (m_EditorComponent != null) {
			m_EditorComponent.updateChildPropertySheet();
			if (trueChange)
				m_Support.firePropertyChange("", m_Backup, m_Object);
		}
	}


	/**
	 * Gets the current Object.
	 *
	 * @return the current Object
	 */
	public Object getValue() {
		return m_Object;
	}

	/**
	 * Supposedly returns an initialization string to create a Object
	 * identical to the current one, including it's state, but this doesn't
	 * appear possible given that the initialization string isn't supposed to
	 * contain multiple statements.
	 *
	 * @return the java source code initialization string
	 */
	public String getJavaInitializationString() {
		return "new " + m_Object.getClass().getName() + "()";
	}

	/**
	 * Returns true to indicate that we can paint a representation of the
	 * Object.
	 *
	 * @return true
	 */
	public boolean isPaintable() {
		return true;
	}

	/** Paints a representation of the current Object.
	 *
	 * @param gfx the graphics context to use
	 * @param box the area we are allowed to paint into
	 */
	public void paintValue(Graphics gfx,Rectangle box) {
		if (m_Enabled && m_Object != null) {
			int getNameMethod = -1;
			MethodDescriptor[]  methods;
			String rep = "";
			try {
				BeanInfo            bi      = Introspector.getBeanInfo(m_Object.getClass());
				methods = bi.getMethodDescriptors();
				for (int i = 0; i < methods.length; i++) {
					if (methods[i].getName().equalsIgnoreCase("getName")) getNameMethod = i;
				}
			} catch (IntrospectionException ex) {
				System.err.println("PropertySheetPanel.setTarget(): Couldn't introspect");
				return;
			}
			if (getNameMethod >= 0) {
				try {
					rep = (String)methods[getNameMethod].getMethod().invoke(m_Object, (Object[])null);
				} catch (java.lang.IllegalAccessException e1) {

				} catch (java.lang.reflect.InvocationTargetException e2) {

				}
			}
			if (rep.length() <= 0) {
				rep = m_Object.getClass().getName();
				int dotPos = rep.lastIndexOf('.');
				if (dotPos != -1)
					rep = rep.substring(dotPos + 1);
			}
			FontMetrics fm = gfx.getFontMetrics();
			int vpad = (box.height - fm.getHeight()) / 2;
			gfx.drawString(rep, 2, fm.getHeight() + vpad -2 );
		} else {
		}
	}


	/**
	 * Returns null as we don't support getting/setting values as text.
	 *
	 * @return null
	 */
	public String getAsText() {
		return null;
	}

	/**
	 * Returns null as we don't support getting/setting values as text.
	 *
	 * @param text the text value
	 * @exception IllegalArgumentException as we don't support
	 * getting/setting values as text.
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		throw new IllegalArgumentException(text);
	}

	/**
	 * Returns null as we don't support getting values as tags.
	 *
	 * @return null
	 */
	public String[] getTags() {
		return null;
	}

	/**
	 * Returns true because we do support a custom editor.
	 *
	 * @return true
	 */
	public boolean supportsCustomEditor() {
		return true;
	}

	/**
	 * Returns the array editing component.
	 *
	 * @return a value of type 'java.awt.Component'
	 */
	public Component getCustomEditor() {
		if (m_EditorComponent == null)
			m_EditorComponent = new GOEPanel(m_Object, m_Backup, m_Support, this);
		return m_EditorComponent;
	}
	/**
	 *
	 */
	public void disableOK() {
		if (m_EditorComponent == null)
			m_EditorComponent = new GOEPanel(m_Object, m_Backup, m_Support, this);
		m_EditorComponent.m_okBut.setEnabled(false);
	}
	public void addPropertyChangeListener(PropertyChangeListener l) {
		m_Support.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		m_Support.removePropertyChangeListener(l);
	}
	/**
	 *
	 */
//	public static void main(String [] args) {
//		try {
//			PropertyEditorManager.registerEditor(SelectedTag.class,TagEditor.class);
//			PropertyEditorManager.registerEditor(double[].class,GenericArrayEditor.class);
//			GenericObjectEditor editor = new GenericObjectEditor();
//			editor.setClassType(StatisticsParameter.class);
//			editor.setValue(new StatisticsParameterImpl());
//			PropertyDialog pd = new PropertyDialog(editor,EVAHELP.cutClassName(editor.getClass().getName()),110, 120);
//			pd.addWindowListener(new WindowAdapter() {
//				public void windowClosing(WindowEvent e) {
//					PropertyEditor pe = ((PropertyDialog)e.getSource()).getEditor();
//					Object c = (Object)pe.getValue();
//					String options = "";
//					if (TRACE) System.out.println(c.getClass().getName() + " " + options);
//					System.exit(0);
//				}
//			});
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			System.out.println(ex.getMessage());
//		}
//	}
}
