package eva2.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import eva2.server.go.tools.FileTools;
import eva2.tools.EVAHELP;
import eva2.tools.SerializedObject;
import eva2.tools.jproxy.RMIProxyLocal;
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
	private JButton m_okBut;
	/** cancel button */
	private JButton m_cancelBut;
	/** edit source button */
//	private JButton m_editSourceBut;
	/** Creates the GUI editor component */
//	private Vector<String> m_ClassesLongName;
	private GenericObjectEditor m_goe = null;
	private boolean withComboBoxToolTips = true; // should tool tips for the combo box be created?
	private int tipMaxLen = 100; // maximum length of tool tip

	/**
	 *
	 */
	public GOEPanel(Object target, Object backup, PropertyChangeSupport support, GenericObjectEditor goe) {
		this(target, backup, support, goe, false);
	}
	
	/**
	 *
	 */
	public GOEPanel(Object target, Object backup, PropertyChangeSupport support, GenericObjectEditor goe, boolean withCancel) {
		Object m_Object = target;
		m_Backup = backup;
		m_Support = support;
		m_goe  = goe;
		
//		System.out.println("GOEPanel.Constructor !! " + this);
		try {
		if (!(Proxy.isProxyClass(m_Object.getClass()))) m_Backup = copyObject(m_Object);
		} catch(OutOfMemoryError err) {
			m_Backup=null;
			System.gc();
			System.err.println("Could not create backup object: not enough memory (GOEPanel backup of " + m_Object + ")");
		}
		m_ObjectNames = new DefaultComboBoxModel(new String [0]);
		m_ObjectChooser = new JComboBox(m_ObjectNames);
		m_ObjectChooser.setEditable(false);
		m_ChildPropertySheet = new PropertySheetPanel();
		m_ChildPropertySheet.addPropertyChangeListener(
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if (TRACE) System.out.println("GOE Property Change Listener: " + evt);
						m_Support.firePropertyChange("", m_Backup, m_goe.getValue());
					}
				});
		m_OpenBut = new JButton("Open");
		m_OpenBut.setToolTipText("Load a configured object");
		m_OpenBut.setEnabled(true);
		m_OpenBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object object = FileTools.openObject(m_OpenBut, m_goe.getClassType());
//				Object object = openObject();
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

		m_SaveBut = new JButton("Save");
		m_SaveBut.setToolTipText("Save the current configured object");
		m_SaveBut.setEnabled(true);
		m_SaveBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTools.saveObjectWithFileChooser(m_SaveBut, m_goe.getValue());
//				saveObject(m_goe.getValue());
			}
		});

		m_okBut = new JButton("OK");
		m_okBut.setEnabled(true);
		m_okBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Backup = copyObject(m_goe.getValue());
//				System.out.println("Backup is now " + BeanInspector.toString(m_Backup));
				if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
					Window w = (Window) getTopLevelAncestor();
					w.dispose();
				}
			}
		});

		m_cancelBut = new JButton("Cancel");
		m_cancelBut.setEnabled(true);
		m_cancelBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (m_Backup != null) {
//					m_Object = copyObject(m_Backup);
					// TODO m_goe.setObject(m_Object);
//					System.out.println("Backup was " + BeanInspector.toString(m_Backup));
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
		//add(m_ChildPropertySheet, BorderLayout.CENTER);
		// Since we resize to the size of the property sheet, a scrollpane isn't
		// typically needed (O  Rly?)
		JScrollPane myScrollPane =new JScrollPane(m_ChildPropertySheet,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


		myScrollPane.setBorder(null);
		add(myScrollPane, BorderLayout.CENTER);
		

		JPanel okcButs = new JPanel();
		okcButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		okcButs.setLayout(new GridLayout(1, 4, 5, 5));
		okcButs.add(m_OpenBut);
		okcButs.add(m_SaveBut);
//		okcButs.add(m_editSourceBut);
		if (withCancel) okcButs.add(m_cancelBut);
		okcButs.add(m_okBut);

		add(okcButs, BorderLayout.SOUTH);

		if (m_goe.getClassType() != null) {
			updateClassType();
			updateChooser();
			updateChildPropertySheet();
		}
		m_ObjectChooser.addItemListener(this);
	}

	public void setEnabledOkCancelButtons(boolean enabled) {
		m_okBut.setEnabled(enabled);
		m_cancelBut.setEnabled(enabled);
	}
	
	/**
	 * Makes a copy of an object using serialization
	 * @param source the object to copy
	 * @return a copy of the source object
	 */
	protected Object copyObject(Object source) {
		Object result = null;
		try {
//			System.out.println("Copying " + BeanInspector.toString(source));
			SerializedObject so = new SerializedObject(source);
			result = so.getObject();
			so=null;
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
	
	public void setTarget(Object o) {
		m_ChildPropertySheet.setTarget(o);
	}
	
	/**
	 *
	 */
	protected void updateClassType() {
		if (TRACE) System.out.println("# updating class "+m_goe.getClassType().getName());
		Vector<String> classesLongNames;
		ArrayList<Class<?>> instances = new ArrayList<Class<?>>(5);
		if (Proxy.isProxyClass(m_goe.getClassType())) {
			if (TRACE) System.out.println("PROXY! original was " + ((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_goe.getValue()))).getOriginalClass().getName());
			classesLongNames = new Vector<String>(GenericObjectEditor.getClassesFromProperties(((RMIProxyLocal)Proxy.getInvocationHandler(((Proxy)m_goe.getValue()))).getOriginalClass().getName(), null));
		} else {		
			classesLongNames = new Vector<String>(GenericObjectEditor.getClassesFromProperties(m_goe.getClassType().getName(), instances));
		}
		if (classesLongNames.size() > 1) {
			m_ObjectChooser.setModel(new DefaultComboBoxModel(classesLongNames));
			if (withComboBoxToolTips) m_ObjectChooser.setRenderer(new ToolTipComboBoxRenderer(collectComboToolTips(instances, tipMaxLen) ));
			add(m_ObjectChooser, BorderLayout.NORTH);
		} else remove(m_ObjectChooser);
		if (TRACE) System.out.println("# done updating class "+m_goe.getClassType().getName());
	}

	private String[] collectComboToolTips(List<Class<?>> instances, int maxLen) {
		String[] tips = new String[instances.size()];
		for (int i=0; i<tips.length; i++) {
			tips[i]=null;
			Class[] classParams = new Class[]{};
			try {
				String tip=null;
				Method giMeth = instances.get(i).getDeclaredMethod("globalInfo", classParams);
				if (Modifier.isStatic(giMeth.getModifiers())) {
					tip = (String)giMeth.invoke(null, (Object[])null);
				}
				if (tip!=null) {
					if (tip.length()<=maxLen) tips[i]=tip;
					else tips[i] = tip.substring(0,maxLen-2)+"..";
				}
			} catch (Exception e) {}
		}
		return tips;
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

	/**
	 * When the chooser selection is changed, ensures that the Object
	 * is changed appropriately.
	 *
	 * @param e a value of type 'ItemEvent'
	 */

	public void itemStateChanged(ItemEvent e) {
		String className = (String)m_ObjectChooser.getSelectedItem();

		if (TRACE) System.out.println("Event-Quelle: " + e.getSource().toString());
		if ((e.getSource() == m_ObjectChooser)  && (e.getStateChange() == ItemEvent.SELECTED)){
			className = (String)m_ObjectChooser.getSelectedItem();
			try {
				if (TRACE) System.out.println(className);
//				Object n = (Object)Class.forName(className, true, this.getClass().getClassLoader()).newInstance();
				Object n = (Object)Class.forName(className).newInstance();
				m_goe.setValue(n);
				// TODO ? setObject(n);
			} catch (Exception ex) {
				System.err.println("Exeption in itemStateChanged "+ex.getMessage());
				System.err.println("Classpath is " + System.getProperty("java.class.path"));
				ex.printStackTrace();
				m_ObjectChooser.hidePopup();
				m_ObjectChooser.setSelectedIndex(0);
				JOptionPane.showMessageDialog(this,
						"Could not create an example of\n"
						+ className + "\n"
						+ "from the current classpath. Is the resource folder at the right place?\nIs the class abstract or the default constructor missing?",
						"GenericObjectEditor",
						JOptionPane.ERROR_MESSAGE);
				EVAHELP.getSystemPropertyString();
			}
		}
	}
}

class ToolTipComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = -5781643352198561208L;
	String[] toolTips = null;

	public ToolTipComboBoxRenderer(String[] tips) {
		super();
		toolTips=tips;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			if ((toolTips!=null) && (index >= 0)) {
				if (toolTips[index]!=null) list.setToolTipText(toolTips[index]);
			}
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setFont(list.getFont());
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
