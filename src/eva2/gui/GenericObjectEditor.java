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


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;


import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import eva2.EvAInfo;
import eva2.client.EvAClient;
import eva2.tools.EVAHELP;
import eva2.tools.ReflectPackage;

import wsi.ra.jproxy.RMIProxyLocal;


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
	 *
	 */
	public class GOEPanel extends JPanel implements ItemListener {
		/** The chooser component */
		private JComboBox m_ObjectChooser;
		/** The component that performs classifier customization */
		private PropertySheetPanel m_ChildPropertySheet;
		/** The model containing the list of names to select from */
		private DefaultComboBoxModel m_ObjectNames;
		/** Open object from disk */
		private JButton m_OpenBut;
		/** Save object to disk */
		private JButton m_SaveBut;
		/** ok button */
		public JButton m_okBut;
		/** cancel button */
		private JButton m_cancelBut;
		/** edit source button */
//		private JButton m_editSourceBut;
		/** The filechooser for opening and saving object files */
		private JFileChooser m_FileChooser;
		/** Creates the GUI editor component */
		private Vector<String> m_ClassesLongName;
//		private String[] m_ClassesShortName;
//		private SourceCodeEditor m_SourceCodeEditor;
//		private PropertyDialog m_SourceCodeEditorFrame;

		/**
		 *
		 */
		public GOEPanel() {
			//System.out.println("GOEPanel.Constructor !!");
			if (!(Proxy.isProxyClass(m_Object.getClass()))) m_Backup = copyObject(m_Object);
			m_ObjectNames = new DefaultComboBoxModel(new String [0]);
			m_ObjectChooser = new JComboBox(m_ObjectNames);
			m_ObjectChooser.setEditable(false);
			m_ChildPropertySheet = new PropertySheetPanel();
			m_ChildPropertySheet.addPropertyChangeListener(
					new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							m_Support.firePropertyChange("", m_Backup, m_Object);
						}
					});
			m_OpenBut = new JButton("Open...");
			m_OpenBut.setToolTipText("Load a configured object");
			m_OpenBut.setEnabled(true);
			m_OpenBut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object object = openObject();
					if (object != null) {
						// setValue takes care of: Making sure obj is of right type,
						// and firing property change.
						setValue(object);
						// Need a second setValue to get property values filled in OK.
						// Not sure why.
						setValue(object); // <- Hannes ?!?!?
					}
				}
			});

			m_SaveBut = new JButton("Save...");
			m_SaveBut.setToolTipText("Save the current configured object");
			m_SaveBut.setEnabled(true);
			m_SaveBut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveObject(m_Object);
				}
			});
//
//			m_editSourceBut = new JButton("Edit Source");
//			m_editSourceBut.setToolTipText("Edit the Source");
//			m_editSourceBut.setEnabled(false);
//			m_editSourceBut.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					m_editSourceBut.setEnabled(false);
//					m_SourceCodeEditor = new SourceCodeEditor();
//					String className = m_Object.getClass().getName();
//					m_SourceCodeEditor.editSource(EvAClient.DYNAMICCLASSES_PROPERTIES.getProperty(className));
//					m_SourceCodeEditorFrame = new PropertyDialog(m_SourceCodeEditor, "test", 50, 50);
//					m_SourceCodeEditorFrame.pack();
//					m_SourceCodeEditorFrame.addWindowListener(new WindowAdapter() {
//						public void windowClosing (WindowEvent e) {
//							m_SourceCodeEditor = null;
//							m_editSourceBut.setEnabled(true);
//						}
//					});
//					m_SourceCodeEditor.addPropertyChangeListener(new
//							PropertyChangeListener() {
//						public void propertyChange(PropertyChangeEvent evt) {
//							sourceChanged();
//						}
//					}
//					);
//				}
//			});

			m_okBut = new JButton("OK");
			m_okBut.setEnabled(true);
			m_okBut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					m_Backup = copyObject(m_Object);
					if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
						Window w = (Window) getTopLevelAncestor();
						w.dispose();
					}
				}
			});

			m_cancelBut = new JButton("Cancel");
			m_cancelBut.setEnabled(false);
			m_cancelBut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (m_Backup != null) {
						m_Object = copyObject(m_Backup);
						setObject(m_Object);
						updateClassType();
						updateChooser();
						updateChildPropertySheet();
					}
					if ((getTopLevelAncestor() != null)
							&& (getTopLevelAncestor() instanceof Window)) {
						Window w = (Window) getTopLevelAncestor();
						w.dispose();
					}
				}
			});

			setLayout(new BorderLayout());
			add(m_ObjectChooser, BorderLayout.NORTH);  // important
			add(m_ChildPropertySheet, BorderLayout.CENTER);
			// Since we resize to the size of the property sheet, a scrollpane isn't
			// typically needed
			// add(new JScrollPane(m_ChildPropertySheet), BorderLayout.CENTER);

			JPanel okcButs = new JPanel();
			okcButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			okcButs.setLayout(new GridLayout(1, 4, 5, 5));
			okcButs.add(m_OpenBut);
			okcButs.add(m_SaveBut);
			okcButs.add(m_okBut);
//			okcButs.add(m_editSourceBut);
			//okcButs.add(m_cancelBut);
			add(okcButs, BorderLayout.SOUTH);

			if (m_ClassType != null) {
				updateClassType();
				updateChooser();
				updateChildPropertySheet();
			}
			m_ObjectChooser.addItemListener(this);
		}

		/**
		 * Opens an object from a file selected by the user.
		 *
		 * @return the loaded object, or null if the operation was cancelled
		 */
		protected Object openObject() {
			if (m_FileChooser == null) {
				createFileChooser();
			}
			int returnVal = m_FileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selected = m_FileChooser.getSelectedFile();
				try {
					ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
					Object obj = oi.readObject();
					oi.close();
					if (!m_ClassType.isAssignableFrom(obj.getClass())) {
						throw new Exception("Object not of type: " + m_ClassType.getName());
					}
					return obj;
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Couldn't read object: "
							+ selected.getName()
							+ "\n" + ex.getMessage(),
							"Open object file",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			return null;
		}

		/** Saves the current object to a file selected by the user.
		 * @param object    The object to save.
		 */
		protected void saveObject(Object object) {

			if (m_FileChooser == null) {
				createFileChooser();
			}
			int returnVal = m_FileChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File sFile = m_FileChooser.getSelectedFile();
				try {
					ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
					oo.writeObject(object);
					oo.close();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Couldn't write to file: "
							+ sFile.getName()
							+ "\n" + ex.getMessage(),
							"Save object",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		protected void createFileChooser() {
			m_FileChooser = new JFileChooser(new File("/resources"));
			m_FileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}

		/**
		 * Makes a copy of an object using serialization
		 * @param source the object to copy
		 * @return a copy of the source object
		 */
		protected Object copyObject(Object source) {
			Object result = null;
			try {
				SerializedObject so = new SerializedObject(source);
				result = so.getObject();
			} catch (Exception ex) {
				System.err.println("GenericObjectEditor: Problem making backup object");
				System.err.println(source.getClass().getName());
				ex.printStackTrace();
			}
			return result;
		}

		/**
		 * This is used to hook an action listener to the ok button
		 * @param a The action listener.
		 */
		public void addOkListener(ActionListener a) {
			m_okBut.addActionListener(a);
		}

		/**
		 * This is used to hook an action listener to the cancel button
		 * @param a The action listener.
		 */
		public void addCancelListener(ActionListener a) {
			m_cancelBut.addActionListener(a);
		}

		/**
		 * This is used to remove an action listener from the ok button
		 * @param a The action listener
		 */
		public void removeOkListener(ActionListener a) {
			m_okBut.removeActionListener(a);
		}

		/**
		 * This is used to remove an action listener from the cancel button
		 * @param a The action listener
		 */
		public void removeCancelListener(ActionListener a) {
			m_cancelBut.removeActionListener(a);
		}
		/**
		 *
		 */
		protected void updateClassType() {
			if (TRACE) System.out.println("# updating class "+m_ClassType.getName());
			
			if (Proxy.isProxyClass(m_ClassType)) {
				if (TRACE) System.out.println("PROXY! original was " + ((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_Object))).getOriginalClass().getName());
				m_ClassesLongName = new Vector<String>(getClassesFromProperties(((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_Object))).getOriginalClass().getName()));
			} else {		
				m_ClassesLongName = new Vector<String>(getClassesFromProperties(m_ClassType.getName()));
			}
			m_ObjectChooser.setModel(new DefaultComboBoxModel(m_ClassesLongName));
			if (m_ClassesLongName.size() > 1)  // testhu
				add(m_ObjectChooser, BorderLayout.NORTH);
			else
				remove(m_ObjectChooser);
			if (TRACE) System.out.println("# done updating class "+m_ClassType.getName());
		}

		protected void updateChooser() {
			String objectName =  /*EVAHELP.cutClassName*/ (m_Object.getClass().getName());
			boolean found = false;
			for (int i = 0; i < m_ObjectNames.getSize(); i++) {
				if (TRACE) System.out.println("in updateChooser: looking at "+(String)m_ObjectNames.getElementAt(i));
				if (objectName.equals((String)m_ObjectNames.getElementAt(i))) {
					found = true;
					break;
				}
			}
			if (!found)
				m_ObjectNames.addElement(objectName);
			m_ObjectChooser.getModel().setSelectedItem(objectName);
		}


		/** Updates the child property sheet, and creates if needed */
		public void updateChildPropertySheet() {
			//System.err.println("GOE::updateChildPropertySheet()");
			// Set the object as the target of the propertysheet
			m_ChildPropertySheet.setTarget(m_Object);
			// Adjust size of containing window if possible
			if ((getTopLevelAncestor() != null)
					&& (getTopLevelAncestor() instanceof Window)) {
				((Window) getTopLevelAncestor()).pack();
			}
		}

//
//		public void sourceChanged() {
//
//			//System.out.println("SOURCESTATECHANGED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
//			String className = (String) m_ObjectChooser.getSelectedItem();
//
////			@todohannes: hack! ausbessern
//			className = (String) m_ObjectChooser.getSelectedItem();
//			try {
//				if (m_userdefclasses == true) {
//					className = m_Object.getClass().getName();
//					Object[] para = new Object[] {};
//					Object n = (Object) CompileAndLoad.getInstanceFull(
//							EvAClient.DYNAMICCLASSES_PROPERTIES.getProperty(className),
//							className,
//							para);
//					setObject(n);
//				}
//				else {
//					System.out.println("m_userdefclasses == false!!!!!");
//				}
//			}
//			catch (Exception ex) {
//			}
//
//		}


		/**
		 * When the chooser selection is changed, ensures that the Object
		 * is changed appropriately.
		 *
		 * @param e a value of type 'ItemEvent'
		 */

		public void itemStateChanged(ItemEvent e) {
			String className = (String)m_ObjectChooser.getSelectedItem();
//			m_editSourceBut.setEnabled(false);
//			@todohannes: hack! ausbessern
//			try {
//				if (EvAClient.DYNAMICCLASSES_PROPERTIES.containsKey(className) && m_userdefclasses) {
//					m_editSourceBut.setEnabled(true);
//				}
//			} catch (Exception e1) {
//				System.out.println("Fehler !!! " + e1);
//			}

//			@todohannes: hack! ausbessern
//
//			if (this.m_SourceCodeEditorFrame != null) {
//				m_SourceCodeEditorFrame.setVisible(false);
//				m_SourceCodeEditorFrame = null;
//				m_SourceCodeEditor = null;
//			}

			if (TRACE) System.out.println("Event-Quelle: " + e.getSource().toString());
			if ((e.getSource() == m_ObjectChooser)  && (e.getStateChange() == ItemEvent.SELECTED)){
				className = (String)m_ObjectChooser.getSelectedItem();
				try {
//					if (EvAClient.DYNAMICCLASSES_PROPERTIES.containsKey(className) && m_userdefclasses) {
//						Object[] para = new Object[] {};
//						String source = EvAClient.DYNAMICCLASSES_PROPERTIES.getProperty(className);
//						Object dummy = CompileAndLoad.getInstanceFull(source,className,para);
//						setObject(dummy);
//					} else {
					if (TRACE) System.out.println(className);
					Object n = (Object)Class.forName(className, true, this.getClass().getClassLoader()).newInstance();
					n = (Object)Class.forName(className).newInstance();
					setObject(n);
//					}
				} catch (Exception ex) {
					System.err.println("Exeption in itemStateChanged "+ex.getMessage());
					System.err.println("Classpath is " + System.getProperty("java.class.path"));
					ex.printStackTrace();
					m_ObjectChooser.hidePopup();
					m_ObjectChooser.setSelectedIndex(0);
					JOptionPane.showMessageDialog(this,
							"Could not create an example of\n"
							+ className + "\n"
							+ "from the current classpath. Is it abstract? Is the default constructor missing?",
							"GenericObjectEditor",
							JOptionPane.ERROR_MESSAGE);
					EVAHELP.getSystemPropertyString();
				}
			}
		}
	} // end of inner class
	
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
	
	public static ArrayList<String> getClassesFromClassPath(String className) { 
		ArrayList<String> classes = new ArrayList<String>();
		int dotIndex = className.lastIndexOf('.');
		if (dotIndex <= 0) {
			System.err.println("warning: " + className + " is not a package!");
		} else {		
			String pckg = className.substring(0, className.lastIndexOf('.'));
			Class<?>[] clsArr; 
			try {
				clsArr = ReflectPackage.getAssignableClassesInPackage(pckg, Class.forName(className), true, true);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				clsArr = null;
			}
			if (clsArr == null) {
				System.out.println("Warning: No configuration property found in: "
						+EvAInfo.propertyFile + " "+"for "+className);
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
					} catch (Exception e) {}
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
			System.err.println("exception in setHideProperty: " + e.getMessage());
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
			System.err.println("exception in setHideProperty: " + e.getMessage());
			return false;
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

	/**
	 * Sets the current object to be the default, taken as the first item in
	 * the chooser
	 */
	public void setDefaultValue() {
		if (m_ClassType == null) {
			System.err.println("No ClassType set up for GenericObjectEditor!!");
			return;
		}
		
		Vector<String> v;
		if (Proxy.isProxyClass(m_ClassType)) {
			if (TRACE) System.out.println("PROXY! original was " + ((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_Object))).getOriginalClass().getName());
			v = new Vector<String>(getClassesFromProperties(((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_Object))).getOriginalClass().getName()));
		} else {		
			v = new Vector<String>(getClassesFromProperties(m_ClassType.getName()));
		}
				
		v = new Vector<String>(getClassesFromProperties(m_ClassType.getName()));
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
				System.out.println("PropertySheetPanel.setTarget(): Couldn't introspect");
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
			m_EditorComponent = new GOEPanel();
		return m_EditorComponent;
	}
	/**
	 *
	 */
	public void disableOK() {
		if (m_EditorComponent == null)
			m_EditorComponent = new GOEPanel();
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
