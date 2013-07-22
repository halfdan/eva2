package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 319 $
 *            $Date: 2007-12-05 11:29:32 +0100 (Wed, 05 Dec 2007) $
 *            $Author: mkron $
 */
import eva2.gui.editor.GenericObjectEditor;
import eva2.tools.EVAHELP;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
/**
 * There are some trick methods interpreted here. Check EvA2Notes.txt.
 */
public class PropertySheetPanel extends JPanel implements PropertyChangeListener {

    public final static Logger LOGGER = Logger.getLogger(PropertySheetPanel.class.getName());
    /**
     * The target object being edited.
     */
    private Object targetObject;
    /**
     * Holds properties of the target.
     */
    private PropertyDescriptor propertyDescriptors[];
    /**
     * Holds the methods of the target.
     */
    private MethodDescriptor methodDescriptors[];
    /**
     * Holds property editors of the object.
     */
    private PropertyEditor propertyEditors[];
    /**
     * Holds current object values for each property.
     */
    private Object objectValues[];
    /**
     * Stores GUI components containing each editing component.
     */
    private JComponent views[];
    private JComponent viewWrappers[];
    /**
     * The labels for each property.
     */
    private JLabel propertyLabels[];
    /**
     * The tool tip text for each property.
     */
    private String toolTips[];
    /**
     * StringBuffer containing help text for the object being edited
     */
//    private StringBuffer            m_HelpText;
    private String className;
    /**
     * Button to pop up the full help text in a separate frame.
     */
    private JButton helpButton;
    /**
     * A count of the number of properties we have an editor for.
     */
    private int numEditableProperties = 0;
    /**
     * How long should a tip text line be (translated to HTML).
     */
    private int tipTextLineLen = 50;
    /**
     * A support object for handling property change listeners.
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * set true to use the GOE by default if no other editor is registered.
     */
    // If true, tool tips are used up to the first point only. 
    boolean stripToolTipToFirstPoint = false;
    private JTable propertyTable;
    private DefaultTableModel propertyTableModel;
    
    /**
     * Creates the property sheet panel.
     */
    public PropertySheetPanel() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
    }

    /**
     * Updates the property sheet panel with a changed property and also passed
     * the event along.
     *
     * @param evt a value of type 'PropertyChangeEvent'
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        wasModified(evt); // Let our panel update before guys downstream
        propertyChangeSupport.removePropertyChangeListener(this);
        propertyChangeSupport.firePropertyChange("", null, targetObject);
        propertyChangeSupport.addPropertyChangeListener(this);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
    }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
    }
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /**
     * Create a fitting viewer component for an editor instance. If none can be
     * identified, null is returned.
     * 
     * @param editor
     * @return
     */
    public static JComponent getView(PropertyEditor editor) {
    	JComponent view = null;
    	// Now figure out how to display it...
    	if (editor.isPaintable() && editor.supportsCustomEditor()) {
    		view = new PropertyPanel(editor);
    	} else {
    		String[] tags = editor.getTags();
            if (tags != null) {
                if ((tags.length == 2) && (tags[0].equals("True")) && (tags[1].equals("False"))) {
                    view = new PropertyBoolSelector(editor);
    		} else {
                    view = new PropertyValueSelector(editor);
                }
            } else {
    			if (editor.getAsText() != null) {
    				view = new PropertyText(editor);
    			} else {
                    view = null;
    			}
    		}
    	}
    	return view;
    }
    
    /**
     * Sets a new target object for customisation.
     *
     * @param targ a value of type 'Object'
     */
    public synchronized void setTarget(Object targ) {
        propertyTableModel = new DefaultTableModel();
        propertyTableModel.addColumn("Key");
        propertyTableModel.addColumn("Value");        
        propertyTable = new JTable(propertyTableModel);
        propertyTable.setDefaultRenderer(Object.class, new PropertyCellRenderer());
        propertyTable.setDefaultEditor(Object.class, new PropertyCellEditor());
        propertyTable.setRowHeight(20);
        propertyTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Close any child windows at this point
        removeAll();
        setLayout(new GridBagLayout());
        setVisible(false);
        numEditableProperties = 0;
        targetObject = targ;
        try {
            BeanInfo bi = Introspector.getBeanInfo(targetObject.getClass());
            propertyDescriptors = bi.getPropertyDescriptors();
            methodDescriptors = bi.getMethodDescriptors();
        } catch (IntrospectionException ex) {
            System.err.println("PropertySheetPanel.setTarget(): Couldn't introspect");
            return;
        }

        int rowHeight = 12;
        
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.fill = GridBagConstraints.BOTH;

        // Look for a globalInfo method that returns a string
        // describing the target
        int methsFound = 0; // dont loop too long, so count until all found
        for (MethodDescriptor methodDescriptor : methodDescriptors) {
            String name = methodDescriptor.getDisplayName();
            Method meth = methodDescriptor.getMethod();
            if (name.equals("globalInfo")) {
                JPanel infoPanel = makeInfoPanel(meth, targ, rowHeight);
                if (infoPanel != null) {
                    gbConstraints.gridx = 0;
                    gbConstraints.gridy = 0;
                    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
                    gbConstraints.anchor = GridBagConstraints.PAGE_START;
                    add(infoPanel, gbConstraints);
            	}
                methsFound++;
            } // end if (name.equals("globalInfo")) {
            else if (name.equals("hideHideable")) {
                Object args[] = {};
                try {
                    meth.invoke(targetObject, args);
                } catch (Exception ex) {
                }
            	methsFound++;
            } else if (name.equals("customPropertyOrder")) {
            	methsFound++;
            	reorderProperties(meth);
            }
            if (methsFound == 3) {
                break; // small speed-up
            }
        } // end for (int i = 0; i < m_Methods.length; i++) {
        // restore hide states of all properties
//        GenericObjectEditor.setHideProperties(m_Target.getClass(), hideStateBackup);
        
        // Now lets search for the individual properties, their
        // values, views and editors...
        propertyEditors = new PropertyEditor[propertyDescriptors.length];
        // collect property values if possible
        objectValues = getValues(targetObject, propertyDescriptors, true, false, true);
        views = new JComponent[propertyDescriptors.length];
        viewWrappers = new JComponent[propertyDescriptors.length];
        propertyLabels = new JLabel[propertyDescriptors.length];
        toolTips = new String[propertyDescriptors.length];

        
        for (int i = 0; i < propertyDescriptors.length; i++) {
            // For each property do this
            // Don't display hidden or expert properties.
            // if (m_Properties[i].isHidden() || m_Properties[i].isExpert()) continue;
            // we now look at hidden properties, they can be shown or hidden dynamically (MK)
            String name = propertyDescriptors[i].getDisplayName();
            if (objectValues[i] == null) {
                continue; // expert, hidden, or no getter/setter available
            }
            JComponent newView = null;
            try {

                propertyEditors[i] = makeEditor(propertyDescriptors[i], name, objectValues[i]);
                if (propertyEditors[i] == null) {
                    continue;
                }
                toolTips[i] = BeanInspector.getToolTipText(name, methodDescriptors, targetObject, stripToolTipToFirstPoint, tipTextLineLen);

                newView = getView(propertyEditors[i]);
                if (newView == null) {
                    System.err.println("Warning: Property \"" + name + "\" has non-displayabale editor.  Skipping.");
                    continue;
                }
            } catch (Exception ex) {
                System.out.println("Skipping property " + name + " ; exception: " + ex);
                ex.printStackTrace();
                continue;
            } // end try

            // Add some specific display for some greeks here
            name = translateGreek(name);
            name = eva2.tools.StringTools.humaniseCamelCase(name);
            propertyTableModel.addRow(new Object[]{name, newView});
        }
        
        JScrollPane scrollableTable = new JScrollPane(propertyTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 1;
        gbConstraints.weightx = 1.0;
        gbConstraints.weighty = 1.0;
        gbConstraints.fill = GridBagConstraints.BOTH;
        scrollableTable.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scrollableTable, gbConstraints);

        validate();
        setVisible(true);
    }

    public static PropertyDescriptor[] getProperties(Object target) {
        BeanInfo bi;
		try {
			bi = Introspector.getBeanInfo(target.getClass());
		} catch (IntrospectionException e) {
			e.printStackTrace();
			return null;
		}
        return bi.getPropertyDescriptors();
    }
    
    public static String[] getPropertyNames(Object target) {
        return getNames(getProperties(target));
    }
    
    public static Object[] getPropertyValues(Object target, boolean omitExpert, boolean omitHidden, boolean onlySetAndGettable) {
    	return getValues(target, getProperties(target), omitExpert, omitHidden, onlySetAndGettable);
    }
    
    public static String[] getNames(PropertyDescriptor[] props) {
        String[] names = new String[props.length];
        for (int i = 0; i < props.length; i++) {
            names[i] = props[i].getDisplayName();
    	}
    	return names;
    }
    
    /**
     * Cycle the properties and request the value of each in an array. Null
     * values may indicate missing getter/setter, expert flag or hidden flag set
     * depending on the parameters. Note that to show hidden properties
     * dynamically, views may need be constructed for them anyways, so do not
     * omit them here.
     * 
     * @param props
     * @param omitExpert
     * @param omitHidden
     * @param onlySetAndGettable
     * @return
     */
    public static Object[] getValues(Object target, PropertyDescriptor[] props, boolean omitExpert, boolean omitHidden, boolean onlySetAndGettable) {
        Object[] values = new Object[props.length];
        for (int i = 0; i < props.length; i++) {
            // For each property do this
            // Don't display hidden or expert properties.
            // if (m_Properties[i].isHidden() || m_Properties[i].isExpert()) continue;
        	// we now look at hidden properties, they can be shown or hidden dynamically (MK)
            String name = props[i].getDisplayName();
            if (props[i].isExpert() && omitExpert) {
                continue;
            }
            if (props[i].isHidden() && omitHidden) {
                continue; // TOOD this might be a problem - hidden values which can be shown dynamically will need a viewer even if hidden
            }
            Method getter = props[i].getReadMethod();
            Method setter = props[i].getWriteMethod();
            // Only display read/write properties.
            if (onlySetAndGettable && (getter == null || setter == null)) {
                continue;
            }
            Object args[] = {};
	            Object value = null;
				try {
					value = getter.invoke(target, args);
				} catch (Exception ex) {
		            System.out.println("Exception on getting value for property " + name + " on target " + target.toString());
		            ex.printStackTrace();
                values[i] = null;
				}
//	            PropertyEditor  editor  = null;
	            //Class           pec     = m_Properties[i].getPropertyEditorClass();
            values[i] = value;

        } // end for each property
        return values;
    }
    
    /**
     * Create a label to be shown if no other properties are shown.
     * 
     * @param componentOffset
     * @param gbLayout
     * @return
     */
	private JLabel createDummyLabel(int componentOffset, GridBagLayout gbLayout) {
        JLabel empty = new JLabel("No editable properties", SwingConstants.CENTER);
		Dimension d = empty.getPreferredSize();
		empty.setPreferredSize(new Dimension(d.width * 2, d.height * 2));
		empty.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 10));
		GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.anchor = GridBagConstraints.CENTER;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.gridy = componentOffset;
        gbConstraints.gridx = 0;
		gbLayout.setConstraints(empty, gbConstraints);
		return empty;
	}

    private PropertyEditor makeEditor(PropertyDescriptor property, String name, Object value) {
    	PropertyEditor editor = PropertyEditorProvider.findEditor(property, value);
        if (editor == null) {
            return null;
        }
        
        // Don't try to set null values:
        if (value == null) {
            // If it's a user-defined property we give a warning.
            String getterClass = property.getReadMethod().getDeclaringClass().getName();
            if (getterClass.indexOf("java.") != 0) {
                System.out.println("Warning: Property \"" + name + "\" of class " + targetObject.getClass() + " has null initial value.  Skipping.");
            }
            return null;
        }
        editor.setValue(value);

//        System.out.println("PSP editor class: " + editor.getClass());
        
        editor.addPropertyChangeListener(this);
        return editor;
    }
    
	private void addLabelView(int componentOffset, GridBagLayout gbLayout,
			int i, String name, JComponent newView) {
                        
        propertyLabels[i] = makeLabel(name);
        views[i] = newView;
        viewWrappers[i] = new JPanel();
        viewWrappers[i].setLayout(new BorderLayout());

        gbLayout.setConstraints(propertyLabels[i], makeLabelConstraints(i + componentOffset));
        add(propertyLabels[i]);
        JPanel newPanel = makeViewPanel(toolTips[i], propertyLabels[i], views[i], viewWrappers[i]);
        gbLayout.setConstraints(newPanel, makeViewConstraints(i + componentOffset));
		add(newPanel);
	}

	private JLabel makeLabel(String name) {
		JLabel label = new JLabel(name, SwingConstants.RIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 5));
		return label;
	}
   
	private static JPanel makeViewPanel(String tipText, JLabel label,
			JComponent view, JComponent viewWrapper) {
		JPanel newPanel = new JPanel();
		if (tipText != null) {
			label.setToolTipText(tipText);
			view.setToolTipText(tipText);
		}
        newPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 10));
		newPanel.setLayout(new BorderLayout());
		// @todo: Streiche here i could add the ViewWrapper
		viewWrapper.add(view, BorderLayout.CENTER);
		newPanel.add(viewWrapper, BorderLayout.CENTER);
		return newPanel;
	}
	
	private GridBagConstraints makeLabelConstraints(int componentIndex) {
		GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.anchor = GridBagConstraints.EAST;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.gridy = componentIndex;
        gbConstraints.gridx = 0;
		return gbConstraints;
	}

	private GridBagConstraints makeViewConstraints(int componentIndex) {
		GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.anchor = GridBagConstraints.WEST;
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.gridy = componentIndex;
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 100;
		return gbConstraints;
	}
	
	/**
     * Be sure to give a clone
     *
     * @param oldProps
     * @param meth
     * @return
     */
    private PropertyDescriptor[] reorderProperties(Method meth) {
//        Mathematics.revertArray(oldProps, newProps);
        Object[] args = {};
        Object retV = null;
       	PropertyDescriptor[] newProps = null;
        try {
            retV = meth.invoke(targetObject, args); // should return String[] to be interpreted as a list of ordered properties
        } catch (Exception ex) {
        }
        if (retV != null) {
            try {
            	if (retV.getClass().isArray()) { // reorder the properties
                    String[] swProps = (String[]) retV;
                    PropertyDescriptor[] oldProps = propertyDescriptors.clone();
                    newProps = new PropertyDescriptor[oldProps.length];
            		//int findFirst=findFirstProp(props[0], oldProps);
                    int firstNonNull = 0;
                    for (int i = 0; i < oldProps.length; i++) {
                        if (i < swProps.length) {
                            int pInOld = findProp(oldProps, swProps[i]);
                            newProps[i] = oldProps[pInOld];
                            oldProps[pInOld] = null;
            			} else {
            				firstNonNull = findFirstNonNullAfter(oldProps, firstNonNull);
                            newProps[i] = oldProps[firstNonNull];
            				firstNonNull++;
            			}
            		}
                    propertyDescriptors = newProps;
            	}
            } catch (Exception e) {
            	System.err.println("Error during reordering properties: " + e.getMessage());
                return propertyDescriptors;
            }
        }
		return newProps;
	}

    /**
     * Find the first non-null entry in an Array at or after the given index and
     * return its index. If only null entries are found, -1 is returned.
     * 
     * @param arr
     * @param firstLook
     * @return
     */
    private int findFirstNonNullAfter(PropertyDescriptor[] arr,
			int firstLook) {
        for (int i = firstLook; i < arr.length; i++) {
            if (arr[i] != null) {
                return i;
            }
        }
		return -1;
	}

	/**
     * Find a string property in an array and return its index or -1 if not
     * found.
     * 
     * @param oldProps
     * @param string
     * @return
     */
	private int findProp(PropertyDescriptor[] oldProps, String string) {
        for (int i = 0; i < oldProps.length; i++) {
            if (oldProps[i] == null) {
                continue;
		}
            String name = oldProps[i].getDisplayName();
            if (name.compareTo(string) == 0) {
                return i;
            }
        }
		System.err.println("Error, property not found: " + string);
		return -1;
	}

    private JPanel makeInfoPanel(Method meth, Object targ, int rowHeight) {
        if (meth.getReturnType().equals(String.class)) {
            try {
                Object args[] = {};
                String globalInfo = (String) (meth.invoke(targetObject, args));

                className = targ.getClass().getName();
                helpButton = new JButton("Help");
                helpButton.setToolTipText("More information about " + className);
                helpButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent event) {
                        openHelpFrame();
                    }
                });

                JTextArea infoTextArea = new JTextArea();
                infoTextArea.setText(globalInfo);
                infoTextArea.setFont(new Font("SansSerif", Font.PLAIN, rowHeight));
                infoTextArea.setEditable(false);
                infoTextArea.setLineWrap(true);
                infoTextArea.setWrapStyleWord(true);
                infoTextArea.setBackground(getBackground());
                //infoTextArea.setSize(infoTextArea.getPreferredSize());

                JPanel infoPanel = new JPanel();
                infoPanel.setBorder(BorderFactory.createTitledBorder("Info"));
                infoPanel.setLayout(new BorderLayout());
                infoPanel.add(infoTextArea, BorderLayout.CENTER);
                
                if (HtmlDemo.resourceExists(getHelpFileName())) {
                	// this means that the expected URL really exists
                    infoPanel.add(helpButton, BorderLayout.LINE_END);
                } else {
                    LOGGER.log(Level.FINE, "Not adding help button because of missing {0}", getHelpFileName());
                }
                return infoPanel;
            } catch (Exception ex) {
            }
        }
        return null;
    }
    
    private String translateGreek(String name) {
        // Add some specific display for some greeks here
        if (name.equalsIgnoreCase("alpha")) {
            return "\u03B1";
        }
        if (name.equalsIgnoreCase("beta")) {
            return "\u03B2";
        }
        if (name.equalsIgnoreCase("gamma")) {
            return "\u03B3";
        }
        if (name.equalsIgnoreCase("gammab")) {
            return "\u0393";
        }
        if (name.equalsIgnoreCase("delta")) {
            return "\u03B4";
        }
        if (name.equalsIgnoreCase("deltab")) {
            return "\u0394";
        }
        if ((name.equalsIgnoreCase("epsi")) || (name.equalsIgnoreCase("epsilon"))) {
            return "\u03B5";
        }
        if (name.equalsIgnoreCase("zeta")) {
            return "\u03B6";
        }
        if (name.equalsIgnoreCase("theta")) {
            return "\u03D1";
        }
        if (name.equalsIgnoreCase("thetab")) {
            return "\u0398";
        }
        if (name.equalsIgnoreCase("iota")) {
            return "\u03B9";
        }
        if (name.equalsIgnoreCase("kappa")) {
            return "\u03BA";
        }
        if (name.equalsIgnoreCase("lambda")) {
            return "\u03BB";
        }
        if (name.equalsIgnoreCase("lambdab")) {
            return "\u039B";
        }
        if (name.equalsIgnoreCase("rho")) {
            return "\u03C1";
        }
        if (name.equalsIgnoreCase("sigma")) {
            return "\u03C3";
        }
        if (name.equalsIgnoreCase("sigmab")) {
            return "\u03A3";
        }
        if (name.equalsIgnoreCase("tau")) {
            return "\u03C4";
        }
        if (name.equalsIgnoreCase("upsilon")) {
            return "\u03C5";
        }
        if (name.equalsIgnoreCase("upsilonb")) {
            return "\u03D2";
        }
        if (name.equalsIgnoreCase("omega")) {
            return "\u03C9";
        }
        if (name.equalsIgnoreCase("omegab")) {
            return "\u03A9";
        }

        // these are too small
        if (name.equalsIgnoreCase("eta")) {
            return "\u03B7";
        }
        if (name.equalsIgnoreCase("psi")) {
            return "\u03C8";
        }
        if (name.equalsIgnoreCase("psib")) {
            return "\u03A8";
        }
        if (name.equalsIgnoreCase("phi")) {
            return "\u03D5";
        }
        if (name.equalsIgnoreCase("phib")) {
            return "\u03A6";
        }
        if (name.equalsIgnoreCase("chi")) {
            return "\u03C7";
        }
        if ((name.equalsIgnoreCase("mu")) || (name.equalsIgnoreCase("my")) || (name.equalsIgnoreCase("myu"))) {
            return "\u03BC";
        }
        if (name.equalsIgnoreCase("nu")) {
            return "\u03BD";
        }
        if (name.equalsIgnoreCase("xi")) {
            return "\u03BE";
        }
        if (name.equalsIgnoreCase("xib")) {
            return "\u039E";
        }
        if (name.equalsIgnoreCase("pi")) {
            return "\u03C0";
        }
        if (name.equalsIgnoreCase("pib")) {
            return "\u03A0";
        }
        
        return name;
	}

	/**
     * Get the html help file name.
     * 
     * @return
     */
    protected String getHelpFileName() {
        return EVAHELP.cutClassName(className) + ".html";
    }
    
    /**
     * This method opens a help frame.
     */
    protected void openHelpFrame() {
        HtmlDemo temp = new HtmlDemo(getHelpFileName());
        temp.show();
    }

    /**
     * Gets the number of editable properties for the current target.
     *
     * @return the number of editable properties.
     */
    public int editableProperties() {
        return numEditableProperties;
    }
    
    /**
     * Return true if the modification was successful.
     * 
     * @param i
     * @param newValue
     * @return
     */
    synchronized boolean updateValue(int i, Object newValue) {
        PropertyDescriptor property = propertyDescriptors[i];
        Method getter = propertyDescriptors[i].getReadMethod();
        objectValues[i] = newValue;
        Method setter = property.getWriteMethod();
        // @todo: Streiche so something was changed, i could check if i have to change the editor

        PropertyEditor tmpEdit = null;
        // the findEditor method using properties may retrieve a primitive editor, the other one, for obscure reasons, cant.
        // so Ill use the mightier first.
        tmpEdit = PropertyEditorProvider.findEditor(propertyDescriptors[i], newValue);
        if (tmpEdit == null) {
            tmpEdit = PropertyEditorProvider.findEditor(propertyDescriptors[i].getPropertyType());
        }
        if (tmpEdit.getClass() != propertyEditors[i].getClass()) {
            objectValues[i] = newValue;
            propertyEditors[i] = tmpEdit;
            if (tmpEdit instanceof GenericObjectEditor) {
                ((GenericObjectEditor) tmpEdit).setClassType(propertyDescriptors[i].getPropertyType());
            }
            propertyEditors[i].setValue(newValue);
        	JComponent newView = null;
        	newView = getView(tmpEdit);
            if (newView == null) {
                System.err.println("Warning: Property \"" + propertyDescriptors[i].getDisplayName() + "\" has non-displayable editor.  Skipping.");
        		return false;
        	}
            propertyEditors[i].addPropertyChangeListener(this);
            views[i] = newView;
            if (toolTips[i] != null) {
                views[i].setToolTipText(toolTips[i]);
            }
            viewWrappers[i].removeAll();
            viewWrappers[i].setLayout(new BorderLayout());
            viewWrappers[i].add(views[i], BorderLayout.CENTER);
            viewWrappers[i].repaint();
        }
        
//        System.out.println("Value: "+value +" / m_Values[i]: " + m_Values[i]);
        // Now try to update the target with the new value of the property
        // and allow the target to do some changes to the value, therefore
        // reread the new value from the target
        try {
            Object args[] = {newValue};
            args[0] = newValue;
            Object args2[] = {};
            // setting the current value to the target object
            setter.invoke(targetObject, args);
            // i could also get the new value
            //value = getter.invoke(m_Target, args2);
            // Now i'm reading the set value from the target to my local values
            objectValues[i] = getter.invoke(targetObject, args2);

            if (newValue instanceof Integer) {
                // This could check whether i have to set the value back to
                // the editor, this would allow to check myu and lambda
                // why shouldn't i do this for every property!?
                if (((Integer) newValue).intValue() != ((Integer) objectValues[i]).intValue()) {
                    propertyEditors[i].setValue(objectValues[i]);
                }
            }
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof PropertyVetoException) {
                System.out.println("PropertySheetPanel.wasModified(): WARNING: Vetoed; reason is: " + ex.getTargetException().getMessage());
            } else {
                System.out.println("PropertySheetPanel.wasModified(): InvocationTargetException while updating " + property.getName());
                System.out.println("PropertySheetPanel.wasModified(): " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            System.out.println("PropertySheetPanel.wasModified(): Unexpected exception while updating " + property.getName());
        }
        if (views[i] != null && views[i] instanceof PropertyPanel) {
            //System.err.println("Trying to repaint the property canvas");
            views[i].repaint();
            revalidate();
        }
        return true;
    }

    /**
     * Updates the propertysheet when a value has been changed (from outside the
     * propertysheet?).
     *
     * @param evt a value of type 'PropertyChangeEvent'
     */
    synchronized void wasModified(PropertyChangeEvent evt) {
        int propIndex = -1;
        if (evt.getSource() instanceof PropertyEditor) {
            PropertyEditor editor = (PropertyEditor) evt.getSource();
            for (int i = 0; i < propertyEditors.length; i++) {
                if (propertyEditors[i] == editor) {
	            	propIndex = i;
                    if (wasModified(i, editor.getValue(), true)) {
                        break;
	            }
            }
    }
            if (propIndex == -1) {
                System.err.println("error: could not identify event editor! (PropertySheetPanel)");
            }
        } else {
            System.err.println("unknown event source! (PropertySheetPanel)");
        }
    }
    
    /**
     * Updates the propertysheet when a value has been changed (from outside the
     * propertysheet?).
     *
     * @param evt a value of type 'PropertyChangeEvent'
     */
    synchronized boolean wasModified(int propIndex, Object value, boolean followDependencies) {
        if (!updateValue(propIndex, value)) {
            return false;
        }	            	
        
        boolean doRepaint = false;

        for (int i = 0; i < propertyEditors.length; i++) { // check the views for out-of-date information. this is different than checking the editors
        	if (i != propIndex) {
                if (updateFieldView(i)) {
                    doRepaint = true;
                }
        	}// end if (m_Editors[i] == editor) {
        } // end for (int i = 0 ; i < m_Editors.length; i++) {	
        if (doRepaint) {	// some components have been hidden or reappeared
        	// MK this finally seems to work right, with a scroll pane, too.
            Container p = this;
        	while (p != null && (!p.getSize().equals(p.getPreferredSize()))) {
        		p.setSize(p.getPreferredSize());
        		p = p.getParent();
        	}
        }

        // Now re-read all the properties and update the editors
        // for any other properties that have changed.
        for (int i = 0; i < propertyDescriptors.length; i++) {
            Object o;
            Method getter = null;
            if (propertyEditors[i] == null) {
                continue; /// TODO: MK: Im not quite sure this is all good, but it avoids a latency problem 
            }
            try {
                getter = propertyDescriptors[i].getReadMethod();
                Object args[] = {};
                o = getter.invoke(targetObject, args);
            } catch (Exception ex) {
	            o = null;
	            System.err.println(ex.getMessage());
	            ex.printStackTrace();
            }
            if ((o != null) && o == objectValues[i] && (BeanInspector.isJavaPrimitive(o.getClass()))) {
	            // The property is equal to its old value.
	            continue;
            }
            if (o != null && o.equals(objectValues[i])) {
	            // The property is equal to its old value.
	            continue;
            }
            objectValues[i] = o;
            // Make sure we have an editor for this property...
            if (propertyEditors[i] == null) {
	            continue;
            }
            // The property has changed!  Update the editor.
            propertyEditors[i].removePropertyChangeListener(this);
            propertyEditors[i].setValue(o);
            propertyEditors[i].addPropertyChangeListener(this);
            if (views[i] != null) {
	            //System.out.println("Trying to repaint " + (i + 1));
                views[i].repaint();
            }
        }

        if (followDependencies) {
        	// Handle the special method getGOEPropertyUpdateLinks which returns a list of pairs
        	// of strings indicating that on an update of the i-th property, the i+1-th property
        	// should be updated. This is useful for changes within sub-classes of the target
        	// which are not directly displayed in this panel but in sub-panels (and there have an own view etc.)
            Object o = BeanInspector.callIfAvailable(targetObject, "getGOEPropertyUpdateLinks", null);
        	if ((o != null) && (o instanceof String[])) {
                maybeTriggerUpdates(propIndex, (String[]) o);
        	}
        }
        
        // Make sure the target bean gets repainted.
        if (Beans.isInstanceOf(targetObject, Component.class)) {
            //System.out.println("Beans.getInstanceOf repaint ");
            ((Component) (Beans.getInstanceOf(targetObject, Component.class))).repaint();
        }
        return true;
    }

    /**
     * Check a property for consistency with the object data and update the view
     * if necessary. Return true if a repaint is necessary.
     *
     * @param i
     * @return
     */
    private boolean updateFieldView(int i) {
    	// looking at another field (not changed explicitly, maybe implicitly
    	boolean valChanged = false;
    	boolean doRepaint = false;
        Object args[] = {};
        Method getter = propertyDescriptors[i].getReadMethod();
        if (propertyDescriptors[i].isHidden() || propertyDescriptors[i].isExpert()) {
            if ((propertyLabels[i] != null) && (propertyLabels[i].isVisible())) {
        		// something is set to hidden but was visible up to now
                viewWrappers[i].setVisible(false);
                views[i].setVisible(false);
                propertyLabels[i].setVisible(false);
    			doRepaint = true;
    		}
    		return doRepaint;
    	} else {
            if ((propertyLabels[i] != null) && !(propertyLabels[i].isVisible())) {
    			 // something is invisible but set to not hidden in the mean time
                viewWrappers[i].setVisible(true);
                views[i].setVisible(true);
                propertyLabels[i].setVisible(true);
    			doRepaint = true;
    		}
    	}
    	try {	// check if view i is up to date and in sync with the value of the getter
            if (views[i] != null) {
                Object val = getter.invoke(targetObject, args);
                if (views[i] instanceof PropertyBoolSelector) {
                    valChanged = (((PropertyBoolSelector) views[i]).isSelected() != ((Boolean) val));
                    if (valChanged) {
                        ((PropertyBoolSelector) views[i]).setSelected(((Boolean) val));
    					}
                } else if (views[i] instanceof PropertyText) {
                    valChanged = !(((PropertyText) views[i]).getText()).equals(val.toString());
                    if (valChanged) {
                        ((PropertyText) views[i]).setText(val.toString());
    				}    				
                } else if (views[i] instanceof PropertyPanel) {
                    valChanged = false;//!((PropertyPanel)m_Views[i]).equals(value);
                    // disregard whole panels and hope for the best
                } else if (views[i] instanceof PropertyValueSelector) {
    				//changed = !((SelectedTag)val).isSelectedString((String)((PropertyValueSelector)m_Views[i]).getSelectedItem());
    				// interestingly there seems to be an implicit update of the ValueSelector, possible changes
    				// are already applied, all we need to see it is a repaint
                    views[i].repaint();
    			} else {
    				System.out.println("Warning: Property \"" + i
    						+ "\" not recognized.  Skipping.");
    			}
        	}
        } catch (Exception exc) {
        	System.err.println("Exception in PropertySheetPanel");
    	}
    	return doRepaint;
	}

    /**
     * Check the given link list and trigger updates of indicated properties.
     * 
     * @param propIndex
     * @param links
     */
	private void maybeTriggerUpdates(int propIndex, String[] links) {
    	int max = links.length;
    	if (max % 2 == 1) {
    		System.err.println("Error in PropertySheetPanel:maybeTriggerUpdates: odd number of strings provided!");
    		max -= 1;
    	}
        for (int i = 0; i < max; i += 2) {
            if (links[i].equals(propertyDescriptors[propIndex].getName())) {
                updateLinkedProperty(links[i + 1]);
			}
		}
	}

	private void updateLinkedProperty(String propName) {
        for (int i = 0; i < propertyDescriptors.length; i++) {
            if (propertyDescriptors[i].getName().equals(propName)) {
                Method getter = propertyDescriptors[i].getReadMethod();
	            Object val = null;
				try {
                    val = getter.invoke(targetObject, (Object[]) null);
				} catch (Exception e) {
					val = null;
					e.printStackTrace();
				}
				if (val != null) {
                    propertyEditors[i].setValue(val);
                } else {
                    System.err.println("Error in PropertySheetPanel:updateLinkedProperty");
                }
				return;
			}
		}
	}
}

class PropertyTableModel extends DefaultTableModel implements TableModel {


}

class PropertyCellRenderer implements TableCellRenderer {
    JLabel empty = new JLabel();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return empty;
        } else if (value instanceof String) {
            return new JLabel(value.toString());
        } else if (value instanceof eva2.gui.PropertyPanel) {
            PropertyPanel propertyPanel = (PropertyPanel) value;
            return propertyPanel;
        } else if (value instanceof PropertyText) {
            return (PropertyText) value;
        } else if (value instanceof PropertyBoolSelector) {
            return (PropertyBoolSelector) value;
        } else if (value instanceof PropertyValueSelector) {
            return (PropertyValueSelector) value;
        }
        
        throw new UnsupportedOperationException("Not supported yet.");
        
    }
    
}

class PropertyCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JLabel empty = new JLabel();
    private Object value;

    @Override
    public JComponent getTableCellEditorComponent(JTable table, final Object value, boolean isSelected, int row, int column) {
        this.value = value;
        JComponent component;
        if (value == null) {
            component = empty;
        } else if (value instanceof String) {
            component = new JLabel(value.toString());
        } else if (value instanceof PropertyPanel) {
            component = new JPanel();
            component.setLayout(new GridBagLayout());
            GridBagConstraints gbConstraints = new GridBagConstraints();
            gbConstraints.gridx = 0;
            gbConstraints.gridy = 0;
            gbConstraints.weightx = 1.0;
            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            component.add((PropertyPanel) value, gbConstraints);
            JButton dialogButton = new JButton("...");
            dialogButton.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.LIGHT_GRAY));
            dialogButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    ((PropertyPanel) value).showDialog(0, 0);
                    fireEditingStopped();
                }
            });
            gbConstraints = new GridBagConstraints();
            gbConstraints.weighty = 1.0;
            gbConstraints.fill = GridBagConstraints.VERTICAL;
            gbConstraints.anchor = GridBagConstraints.LINE_END;
            gbConstraints.gridy = 0;
            gbConstraints.gridx = 1;
            component.add(dialogButton, gbConstraints);
        } else if (value instanceof PropertyText) {
            component = (PropertyText) value;
        } else if (value instanceof PropertyBoolSelector) {
            component = (PropertyBoolSelector) value;
        } else if (value instanceof PropertyValueSelector) {
            component = (PropertyValueSelector) value;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        if (isSelected) {
            component.setForeground(table.getSelectionForeground());
            component.setBackground(table.getSelectionBackground());
        } else {
            component.setForeground(table.getForeground());
            component.setBackground(table.getBackground());
        }
        return component;
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    @Override
    public boolean isCellEditable(final EventObject anEvent) {
        /* Event should always be triggered by a JTable */
        JTable sourceTable = (JTable) anEvent.getSource();
        int selectedColumn = sourceTable.getSelectedColumn();
        String columnName = sourceTable.getColumnName(selectedColumn);
        /* If the columnName equals Key it holds the keys */
        if ("Key".equals(columnName)) {
            return false;
        }
        return true;
    }


    
}