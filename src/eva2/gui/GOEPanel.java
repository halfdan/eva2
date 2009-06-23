package eva2.gui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import wsi.ra.jproxy.RMIProxyLocal;
import eva2.tools.EVAHELP;
/**
*
*/
public class GOEPanel extends JPanel implements ItemListener {
	private Object m_Backup;
	private PropertyChangeSupport m_Support;
	private static boolean TRACE = false;
	
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
//	private JButton m_editSourceBut;
	/** The filechooser for opening and saving object files */
	private JFileChooser m_FileChooser;
	/** Creates the GUI editor component */
	private Vector<String> m_ClassesLongName;
	private GenericObjectEditor m_goe = null;
//	private String[] m_ClassesShortName;
//	private SourceCodeEditor m_SourceCodeEditor;
//	private PropertyDialog m_SourceCodeEditorFrame;

	/**
	 *
	 */
	public GOEPanel(Object target, Object backup, PropertyChangeSupport support, GenericObjectEditor goe) {
		Object m_Object = target;
		m_Backup = backup;
		m_Support = support;
		m_goe  = goe;
		
		//System.out.println("GOEPanel.Constructor !!");
		if (!(Proxy.isProxyClass(m_Object.getClass()))) m_Backup = copyObject(m_Object);
		m_ObjectNames = new DefaultComboBoxModel(new String [0]);
		m_ObjectChooser = new JComboBox(m_ObjectNames);
		m_ObjectChooser.setEditable(false);
		m_ChildPropertySheet = new PropertySheetPanel();
		m_ChildPropertySheet.addPropertyChangeListener(
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						m_Support.firePropertyChange("", m_Backup, m_goe.getValue());
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
					m_goe.setValue(object);
					// Need a second setValue to get property values filled in OK.
					// Not sure why.
					m_goe.setValue(object); // <- Hannes ?!?!?
				}
			}
		});

		m_SaveBut = new JButton("Save...");
		m_SaveBut.setToolTipText("Save the current configured object");
		m_SaveBut.setEnabled(true);
		m_SaveBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveObject(m_goe.getValue());
			}
		});
//
//		m_editSourceBut = new JButton("Edit Source");
//		m_editSourceBut.setToolTipText("Edit the Source");
//		m_editSourceBut.setEnabled(false);
//		m_editSourceBut.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				m_editSourceBut.setEnabled(false);
//				m_SourceCodeEditor = new SourceCodeEditor();
//				String className = m_Object.getClass().getName();
//				m_SourceCodeEditor.editSource(EvAClient.DYNAMICCLASSES_PROPERTIES.getProperty(className));
//				m_SourceCodeEditorFrame = new PropertyDialog(m_SourceCodeEditor, "test", 50, 50);
//				m_SourceCodeEditorFrame.pack();
//				m_SourceCodeEditorFrame.addWindowListener(new WindowAdapter() {
//					public void windowClosing (WindowEvent e) {
//						m_SourceCodeEditor = null;
//						m_editSourceBut.setEnabled(true);
//					}
//				});
//				m_SourceCodeEditor.addPropertyChangeListener(new
//						PropertyChangeListener() {
//					public void propertyChange(PropertyChangeEvent evt) {
//						sourceChanged();
//					}
//				}
//				);
//			}
//		});

		m_okBut = new JButton("OK");
		m_okBut.setEnabled(true);
		m_okBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Backup = copyObject(m_goe.getValue());
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
//					m_Object = copyObject(m_Backup);
					// TODO m_goe.setObject(m_Object);
					m_goe.setValue(copyObject(m_Backup));
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
//		okcButs.add(m_editSourceBut);
		//okcButs.add(m_cancelBut);
		add(okcButs, BorderLayout.SOUTH);

		if (m_goe.getClassType() != null) {
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
				if (!m_goe.getClassType().isAssignableFrom(obj.getClass())) {
					throw new Exception("Object not of type: " + m_goe.getClassType().getName());
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
		if (TRACE) System.out.println("# updating class "+m_goe.getClassType().getName());
		
		if (Proxy.isProxyClass(m_goe.getClassType())) {
			if (TRACE) System.out.println("PROXY! original was " + ((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_goe.getValue()))).getOriginalClass().getName());
			m_ClassesLongName = new Vector<String>(GenericObjectEditor.getClassesFromProperties(((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_goe.getValue()))).getOriginalClass().getName()));
		} else {		
			m_ClassesLongName = new Vector<String>(GenericObjectEditor.getClassesFromProperties(m_goe.getClassType().getName()));
		}
		m_ObjectChooser.setModel(new DefaultComboBoxModel(m_ClassesLongName));
		if (m_ClassesLongName.size() > 1)  // testhu
			add(m_ObjectChooser, BorderLayout.NORTH);
		else
			remove(m_ObjectChooser);
		if (TRACE) System.out.println("# done updating class "+m_goe.getClassType().getName());
	}

	protected void updateChooser() {
		String objectName =  /*EVAHELP.cutClassName*/ (m_goe.getValue().getClass().getName());
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
		m_ChildPropertySheet.setTarget(m_goe.getValue());
		// Adjust size of containing window if possible
		if ((getTopLevelAncestor() != null)
				&& (getTopLevelAncestor() instanceof Window)) {
			((Window) getTopLevelAncestor()).pack();
		}
	}

//
//	public void sourceChanged() {
//
//		//System.out.println("SOURCESTATECHANGED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
//		String className = (String) m_ObjectChooser.getSelectedItem();
//
////		@todohannes: hack! ausbessern
//		className = (String) m_ObjectChooser.getSelectedItem();
//		try {
//			if (m_userdefclasses == true) {
//				className = m_Object.getClass().getName();
//				Object[] para = new Object[] {};
//				Object n = (Object) CompileAndLoad.getInstanceFull(
//						EvAClient.DYNAMICCLASSES_PROPERTIES.getProperty(className),
//						className,
//						para);
//				setObject(n);
//			}
//			else {
//				System.out.println("m_userdefclasses == false!!!!!");
//			}
//		}
//		catch (Exception ex) {
//		}
//
//	}


	/**
	 * When the chooser selection is changed, ensures that the Object
	 * is changed appropriately.
	 *
	 * @param e a value of type 'ItemEvent'
	 */

	public void itemStateChanged(ItemEvent e) {
		String className = (String)m_ObjectChooser.getSelectedItem();
//		m_editSourceBut.setEnabled(false);
//		@todohannes: hack! ausbessern
//		try {
//			if (EvAClient.DYNAMICCLASSES_PROPERTIES.containsKey(className) && m_userdefclasses) {
//				m_editSourceBut.setEnabled(true);
//			}
//		} catch (Exception e1) {
//			System.out.println("Fehler !!! " + e1);
//		}

//		@todohannes: hack! ausbessern
//
//		if (this.m_SourceCodeEditorFrame != null) {
//			m_SourceCodeEditorFrame.setVisible(false);
//			m_SourceCodeEditorFrame = null;
//			m_SourceCodeEditor = null;
//		}

		if (TRACE) System.out.println("Event-Quelle: " + e.getSource().toString());
		if ((e.getSource() == m_ObjectChooser)  && (e.getStateChange() == ItemEvent.SELECTED)){
			className = (String)m_ObjectChooser.getSelectedItem();
			try {
//				if (EvAClient.DYNAMICCLASSES_PROPERTIES.containsKey(className) && m_userdefclasses) {
//					Object[] para = new Object[] {};
//					String source = EvAClient.DYNAMICCLASSES_PROPERTIES.getProperty(className);
//					Object dummy = CompileAndLoad.getInstanceFull(source,className,para);
//					setObject(dummy);
//				} else {
				if (TRACE) System.out.println(className);
//				Object n = (Object)Class.forName(className, true, this.getClass().getClassLoader()).newInstance();
				Object n = (Object)Class.forName(className).newInstance();
				m_goe.setValue(n);
				// TODO ? setObject(n);
//				}
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

