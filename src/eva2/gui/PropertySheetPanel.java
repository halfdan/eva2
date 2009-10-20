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
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import eva2.tools.EVAHELP;
import eva2.tools.StringTools;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 * There are some trick methods interpreted here. Check EvA2Notes.txt.
 */
public class PropertySheetPanel extends JPanel implements PropertyChangeListener {
    public final static boolean     TRACE       = false;
    /** The target object being edited */
    private Object                  m_Target;
    /** Holds properties of the target */
    private PropertyDescriptor      m_Properties[];
    /** Holds the methods of the target */
    private MethodDescriptor        m_Methods[];
    /** Holds property editors of the object */
    private PropertyEditor          m_Editors[];
    /** Holds current object values for each property */
    private Object                  m_Values[];
    /** Stores GUI components containing each editing component */
    private JComponent              m_Views[];
    private JComponent              m_ViewWrapper[];
    /** The labels for each property */
    private JLabel                  m_Labels[];
    /** The tool tip text for each property */
    private String                  m_TipTexts[];
    /** StringBuffer containing help text for the object being edited */
//    private StringBuffer            m_HelpText;
    private String                  m_ClassName;
    /** Button to pop up the full help text in a separate frame */
    private JButton                 m_HelpBut;
    /** A count of the number of properties we have an editor for */
    private int                     m_NumEditable = 0;
    /** How long should a tip text line be (translated to HTML) */
    private int 					tipTextLineLen = 50;
    /** A support object for handling property change listeners */
    private PropertyChangeSupport   m_support = new PropertyChangeSupport(this);
    /** set true to use the GOE by default if no other editor is registered **/

    // If true, tool tips are used up to the first point only. 
    boolean stripToolTipToFirstPoint=false;
    
    /** Creates the property sheet panel.
     */
    public PropertySheetPanel() {
        //    setBorder(BorderFactory.createLineBorder(Color.red));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        if (TRACE) System.out.println("PropertySheetPanel(): NEW PropertySheetPanel");
    }

    /** Updates the property sheet panel with a changed property and also passed
     * the event along.
     * @param evt a value of type 'PropertyChangeEvent'
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (TRACE) System.out.println("PropertySheetPanel.propertyChange() "+m_Target.getClass()+": calling wasModified");
        // GOEPanel gp=(GOEPanel)this.getParent();
        // gp.validateTarget(this); // Once trying to find an irreproducible bug
        
        wasModified(evt); // Let our panel update before guys downstream
        m_support.removePropertyChangeListener(this);
        m_support.firePropertyChange("", null, m_Target);
        m_support.addPropertyChangeListener(this);
    }

    /** Adds a PropertyChangeListener.
     * @param l a value of type 'PropertyChangeListener'
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        m_support.addPropertyChangeListener(l);
    }

    /** Removes a PropertyChangeListener.
     * @param l a value of type 'PropertyChangeListener'
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        m_support.removePropertyChangeListener(l);
    }
    
   
    /** Sets a new target object for customisation.
     * @param targ a value of type 'Object'
     */
    public synchronized void setTarget(Object targ) {
        if (TRACE) System.out.println("PropertySheetPanel.setTarget(): "+targ.getClass().getName());
        int             componentOffset = 0;
        GridBagLayout   gbLayout        = new GridBagLayout();

        // Close any child windows at this point
        removeAll();
        setLayout(gbLayout);
        setVisible(false);
        m_NumEditable   = 0;
        m_Target        = targ;
        try {
            BeanInfo    bi      = Introspector.getBeanInfo(m_Target.getClass());
            m_Properties        = bi.getPropertyDescriptors();
            m_Methods           = bi.getMethodDescriptors();
        } catch (IntrospectionException ex) {
            System.err.println("PropertySheetPanel.setTarget(): Couldn't introspect");
            return;
        }

        int         rowHeight   = 12;
//        JScrollPane js          = null;
//        JTextArea   jt          = new JTextArea();
//        m_HelpText              = null;

        // Look for a globalInfo method that returns a string
        // describing the target
        int methsFound = 0; // dont loop too long, so count until all found
        GenericObjectEditor.setHideAllProperties(m_Target.getClass(), false);
        for (int i = 0; i < m_Methods.length; i++) {
            String name = m_Methods[i].getDisplayName();
            Method meth = m_Methods[i].getMethod();
            if (name.equals("globalInfo")) {
            	JPanel jp = makeInfoPanel(meth, targ, rowHeight, gbLayout);
            	if (jp!=null) {
            		add(jp);
                    componentOffset = 1;
            	}
                methsFound++;
            } // end if (name.equals("globalInfo")) {
            else if (name.equals("hideHideable")) {
                Object  args[]      = { };
                try {
                	meth.invoke(m_Target, args);
                } catch(Exception ex) {}
            	methsFound++;
            } else if (name.equals("customPropertyOrder")) {
            	methsFound++;
            	reorderProperties(meth);
            }
            if (methsFound == 3) break; // small speed-up
        } // end for (int i = 0; i < m_Methods.length; i++) {

        // Now lets search for the individual properties, their
        // values, views and editors...
        m_Editors   = new PropertyEditor[m_Properties.length];
        m_Values    = new Object[m_Properties.length];
        m_Views     = new JComponent[m_Properties.length];
        m_ViewWrapper= new JComponent[m_Properties.length];
        m_Labels    = new JLabel[m_Properties.length];
        m_TipTexts  = new String[m_Properties.length];

//        boolean     firstTip = true;
        for (int i = 0; i < m_Properties.length; i++) {
            // For each property do this
            // Don't display hidden or expert properties.
            // if (m_Properties[i].isHidden() || m_Properties[i].isExpert()) continue;
        	// we now look at hidden properties, they can be shown or hidden dynamically (MK)
            String  name    = m_Properties[i].getDisplayName();
            if (TRACE) System.out.println("PSP looking at "+ name);

            if (m_Properties[i].isExpert()) continue;
            Method  getter  = m_Properties[i].getReadMethod();
            Method  setter  = m_Properties[i].getWriteMethod();
            // Only display read/write properties.
            if (getter == null || setter == null) continue;
            JComponent NewView = null;
            try {
	            Object          args[]  = { };
	            Object          value   = getter.invoke(m_Target, args);
	            PropertyEditor  editor  = null;
	            //Class           pec     = m_Properties[i].getPropertyEditorClass();
	            m_Values[i]     = value;
//	            ////////////////// refactored by MK

	            editor = PropertyEditorProvider.findEditor(m_Properties[i], value);
	            m_Editors[i] = editor;
	            if (editor == null) continue;
	            
	            ////////////////////


	            // Don't try to set null values:
	            if (value == null) {
	                // If it's a user-defined property we give a warning.
	                String getterClass = m_Properties[i].getReadMethod().getDeclaringClass().getName();
	                if (getterClass.indexOf("java.") != 0) System.out.println("Warning: Property \"" + name+ "\" has null initial value.  Skipping.");
	                continue;
	            }
                editor.setValue(value);
                
                m_TipTexts[i] = getToolTipText(name, m_Methods, m_Target, tipTextLineLen);

                // Now figure out how to display it...
                if (editor instanceof sun.beans.editors.BoolEditor) {
                    NewView = new PropertyBoolSelector(editor);
	            } else {
                    if (editor instanceof sun.beans.editors.DoubleEditor) {
                        NewView = new PropertyText(editor);
	                } else {
	                    if (editor.isPaintable() && editor.supportsCustomEditor()) {
	                        NewView = new PropertyPanel(editor);
                        } else {
                            if (editor.getTags() != null ) {
                                NewView = new PropertyValueSelector(editor);
                            } else {
                                if (editor.getAsText() != null) {
                                    NewView = new PropertyText(editor);
                                } else {
                                    System.out.println("Warning: Property \"" + name
                                        + "\" has non-displayabale editor.  Skipping.");
                                    continue;
                                }
                            }
                        }
                    }
                }
                editor.addPropertyChangeListener(this);
            } catch (InvocationTargetException ex) {
	            System.out.println("InvocationTargetException " + name
		            + " on target: "
	                + ex.getTargetException());
	            ex.getTargetException().printStackTrace();
	            continue;
            } catch (Exception ex) {
	            System.out.println("Skipping property "+name+" ; exception: " + ex);
	            ex.printStackTrace();
	            continue;
            } // end try

            // Add some specific display for some greeks here
            name = translateGreek(name);

            m_Labels[i]         = new JLabel(name, SwingConstants.RIGHT);
            m_Labels[i].setBorder(BorderFactory.createEmptyBorder(10,10,0,5));
            m_Views[i]          = NewView;
            m_ViewWrapper[i]    = new JPanel();
            m_ViewWrapper[i].setLayout(new BorderLayout());
            GridBagConstraints gbConstraints = new GridBagConstraints();
            gbConstraints.anchor    = GridBagConstraints.EAST;
            gbConstraints.fill      = GridBagConstraints.HORIZONTAL;
            gbConstraints.gridy     = i+componentOffset;
            gbConstraints.gridx     = 0;
            gbLayout.setConstraints(m_Labels[i], gbConstraints);
            add(m_Labels[i]);
            JPanel newPanel = new JPanel();
            if (m_TipTexts[i] != null) {
	            m_Views[i].setToolTipText(m_TipTexts[i]);
                m_Labels[i].setToolTipText(m_TipTexts[i]);
             }
            newPanel.setBorder(BorderFactory.createEmptyBorder(10,5,0,10));
            newPanel.setLayout(new BorderLayout());
            // @todo: Streiche here i could add the ViewWrapper
            m_ViewWrapper[i].add(m_Views[i], BorderLayout.CENTER);
            newPanel.add(m_ViewWrapper[i], BorderLayout.CENTER);
            gbConstraints           = new GridBagConstraints();
            gbConstraints.anchor    = GridBagConstraints.WEST;
            gbConstraints.fill      = GridBagConstraints.BOTH;
            gbConstraints.gridy     = i+componentOffset;
            gbConstraints.gridx     = 1;
            gbConstraints.weightx   = 100;
            gbLayout.setConstraints(newPanel, gbConstraints);
            add(newPanel);
            m_NumEditable++;
            if (m_Properties[i].isHidden()) {
    			m_ViewWrapper[i].setVisible(false);
    			m_Labels[i].setVisible(false);
            }
        }
        if (m_NumEditable == 0) {
            JLabel empty = new JLabel("No editable properties",SwingConstants.CENTER);
            Dimension d = empty.getPreferredSize();
            empty.setPreferredSize(new Dimension(d.width * 2, d.height * 2));
            empty.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 10));
            GridBagConstraints gbConstraints = new GridBagConstraints();
            gbConstraints.anchor    = GridBagConstraints.CENTER;
            gbConstraints.fill      = GridBagConstraints.HORIZONTAL;
            gbConstraints.gridy     = componentOffset;
            gbConstraints.gridx     = 0;
            gbLayout.setConstraints(empty, gbConstraints);
            add(empty);
        }
//    	Container p=this;
//		while (p != null) {
//			p.setSize(p.getPreferredSize());
//			p = p.getParent();
//		}
        validate();
        setVisible(true);
    }
   
    /**
     * Be sure to give a clone
     * @param oldProps
     * @param meth
     * @return
     */
    private PropertyDescriptor[] reorderProperties(Method meth) {
    	PropertyDescriptor[] oldProps = m_Properties.clone();
        PropertyDescriptor[] newProps = new PropertyDescriptor[oldProps.length];
//        Mathematics.revertArray(oldProps, newProps);
       	Object[] args      = { };
       	Object retV=null;
        try {
        	retV = meth.invoke(m_Target, args);
        } catch(Exception ex) {}
        if (retV!=null) {
            try {
            	if (retV.getClass().isArray()) {
            		String[] swProps=(String[])retV;
            		//int findFirst=findFirstProp(props[0], oldProps);
            		int firstNonNull=0;
            		for (int i=0; i<oldProps.length; i++) {
            			if (i<swProps.length) {
            				int pInOld=findProp(oldProps, swProps[i]);
            				newProps[i]=oldProps[pInOld];
            				oldProps[pInOld]=null;
            			} else {
            				firstNonNull = findFirstNonNullAfter(oldProps, firstNonNull);
            				newProps[i]=oldProps[firstNonNull];
            				firstNonNull++;
            			}
            		}
            		m_Properties=newProps;
            	}
            } catch (Exception e) {
            	System.err.println("Error during reordering properties: " + e.getMessage());
            	return m_Properties;
            }
        }
		return newProps;
	}

    /**
     * Find the first non-null entry in an Array at or after the given index and return its index.
     * If only null entries are found, -1 is returned.
     * 
     * @param arr
     * @param firstLook
     * @return
     */
    private int findFirstNonNullAfter(PropertyDescriptor[] arr,
			int firstLook) {
    	for (int i=firstLook; i<arr.length; i++) if (arr[i]!=null) return i;
		return -1;
	}

	/**
     * Find a string property in an array and return its index or -1 if not found.
     * 
     * @param oldProps
     * @param string
     * @return
     */
	private int findProp(PropertyDescriptor[] oldProps, String string) {
		for (int i=0; i<oldProps.length; i++) {
			if (oldProps[i]==null) continue;
			String  name    = oldProps[i].getDisplayName();
			if (name.compareTo(string)==0) return i;
		}
		return -1;
	}

	private JPanel makeInfoPanel(Method meth, Object targ, int rowHeight, GridBagLayout gbLayout) {
    	if (TRACE) System.out.println("found globalInfo method for " + targ.getClass().toString());
        if (meth.getReturnType().equals(String.class)) {
            try {
                Object  args[]      = { };
                String  globalInfo  = (String)(meth.invoke(m_Target, args));
                String  summary     = globalInfo;
//                int     ci          = globalInfo.indexOf('.');
//                if (ci != -1) {	
//                	// this shortens the displayed text, using only the first "sentence".
//                	// May cause problems, if the dot belongs to a number, for example,
//                	// so I deactivated it (MK).
//                    summary = globalInfo.substring(0, ci + 1);
//                }
                m_ClassName = targ.getClass().getName();
//                m_HelpText  = new StringBuffer("NAME\n");
//                m_HelpText.append(m_ClassName).append("\n\n");
//                m_HelpText.append("SYNOPSIS\n").append(globalInfo).append("\n\n");
                m_HelpBut   = new JButton("Help");
                m_HelpBut.setToolTipText("More information about " + m_ClassName);
                m_HelpBut.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        openHelpFrame();
                        //m_HelpBut.setEnabled(false);
                    }
                });

                JTextArea jt = new JTextArea();
                jt.setText(summary);
                jt.setFont(new Font("SansSerif", Font.PLAIN, rowHeight));
                jt.setEditable(false);
                jt.setLineWrap(true);
                jt.setWrapStyleWord(true);
                jt.setBackground(getBackground());
                jt.setSize(jt.getPreferredSize());

                JPanel jp = new JPanel();
                jp.setBorder(BorderFactory.createCompoundBorder(
	                BorderFactory.createTitledBorder("Info"),
	                BorderFactory.createEmptyBorder(0, 5, 5, 5)
                ));
                jp.setLayout(new BorderLayout());
                jp.add(jt, BorderLayout.CENTER);
                JPanel      p2  = new JPanel();
                p2.setLayout(new BorderLayout());
                
                if (HtmlDemo.resourceExists(getHelpFileName())) {
                	// this means that the expected URL really exists
                	p2.add(m_HelpBut, BorderLayout.NORTH);
                } else {
                	if (TRACE) System.out.println("not adding help button because of missing " + getHelpFileName());
                }
                jp.add(p2, BorderLayout.EAST);
                GridBagConstraints gbConstraints = new GridBagConstraints();
                //gbConstraints.anchor = GridBagConstraints.EAST;
                gbConstraints.fill = GridBagConstraints.BOTH;
                //gbConstraints.gridy = 0;
                //gbConstraints.gridx = 0;
                gbConstraints.gridwidth = 2;
                gbConstraints.insets = new Insets(0,5,0,5);
                gbLayout.setConstraints(jp, gbConstraints);
                return jp;
            } catch (Exception ex) {
            } // end try
        } // end if (meth.getReturnType().equals(String.class)) {
        return null;
    }
    
    private String translateGreek(String name) {
        // Add some specific display for some greeks here
        if (name.equalsIgnoreCase("alpha"))
            return "\u03B1";
        if (name.equalsIgnoreCase("beta"))
            return "\u03B2";
        if (name.equalsIgnoreCase("gamma"))
            return "\u03B3";
        if (name.equalsIgnoreCase("gammab"))
            return "\u0393";
        if (name.equalsIgnoreCase("delta"))
            return "\u03B4";
        if (name.equalsIgnoreCase("deltab"))
            return "\u0394";
        if ((name.equalsIgnoreCase("epsi")) || (name.equalsIgnoreCase("epsilon")))
            return "\u03B5";
        if (name.equalsIgnoreCase("zeta"))
            return "\u03B6";
        if (name.equalsIgnoreCase("theta"))
            return "\u03D1";
        if (name.equalsIgnoreCase("thetab"))
            return "\u0398";
        if (name.equalsIgnoreCase("iota"))
            return "\u03B9";
        if (name.equalsIgnoreCase("kappa"))
            return "\u03BA";
        if (name.equalsIgnoreCase("lambda"))
            return "\u03BB";
        if (name.equalsIgnoreCase("lambdab"))
            return "\u039B";
        if (name.equalsIgnoreCase("rho"))
            return "\u03C1";
        if (name.equalsIgnoreCase("sigma"))
            return "\u03C3";
        if (name.equalsIgnoreCase("sigmab"))
            return "\u03A3";
        if (name.equalsIgnoreCase("tau"))
            return "\u03C4";
        if (name.equalsIgnoreCase("upsilon"))
            return "\u03C5";
        if (name.equalsIgnoreCase("upsilonb"))
            return "\u03D2";
        if (name.equalsIgnoreCase("omega"))
            return "\u03C9";
        if (name.equalsIgnoreCase("omegab"))
            return "\u03A9";

        // these are too small
        if (name.equalsIgnoreCase("eta"))
            return "\u03B7";
        if (name.equalsIgnoreCase("psi"))
            return "\u03C8";
        if (name.equalsIgnoreCase("psib"))
            return "\u03A8";
        if (name.equalsIgnoreCase("phi"))
            return "\u03D5";
        if (name.equalsIgnoreCase("phib"))
            return "\u03A6";
        if (name.equalsIgnoreCase("chi"))
            return "\u03C7";
        if ((name.equalsIgnoreCase("mu")) || (name.equalsIgnoreCase("my")) || (name.equalsIgnoreCase("myu")))
            return "\u03BC";
        if (name.equalsIgnoreCase("nu"))
            return "\u03BD";
        if (name.equalsIgnoreCase("xi"))
            return "\u03BE";
        if (name.equalsIgnoreCase("xib"))
            return "\u039E";
        if (name.equalsIgnoreCase("pi"))
            return "\u03C0";
        if (name.equalsIgnoreCase("pib"))
            return "\u03A0";
        
        return name;
	}

	/**
     * Get the html help file name.
     * 
     * @return
     */
    protected String getHelpFileName() {
    	return EVAHELP.cutClassName(m_ClassName)+".html";
    }
    
    /** This method opens a help frame.
     */
    protected void openHelpFrame() {
        HtmlDemo temp = new HtmlDemo(getHelpFileName());
        temp.show();
//    JTextArea ta = new JTextArea();
//    ta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//    ta.setLineWrap(true);
//    ta.setWrapStyleWord(true);
//    ta.setEditable(false);
//    ta.setText(m_HelpText.toString());
//    ta.setCaretPosition(0);
//    final JFrame jf = new JFrame("Information");
//    jf.addWindowListener(new WindowAdapter() {
//      public void windowClosing(WindowEvent e) {
//        jf.dispose();
//        if (m_HelpFrame == jf) {
//          m_HelpBut.setEnabled(true);
//        }
//      }
//    });
//    jf.getContentPane().setLayout(new BorderLayout());
//    jf.getContentPane().add(new JScrollPane(ta), BorderLayout.CENTER);
//    jf.pack();
//    jf.setSize(400, 350);
//    jf.setLocation(getTopLevelAncestor().getLocationOnScreen().x
//                   + getTopLevelAncestor().getSize().width,
//                   getTopLevelAncestor().getLocationOnScreen().y);
//    jf.setVisible(true);
//    m_HelpFrame = jf;
    }

    /** Gets the number of editable properties for the current target.
     * @return the number of editable properties.
     */
    public int editableProperties() {
        return m_NumEditable;
    }
    
    /**
     * Return true if the modification was successful.
     * 
     * @param i
     * @param newValue
     * @return
     */
    synchronized boolean updateValue(int i, Object newValue) {
    	PropertyDescriptor property = m_Properties[i];
        Method getter   = m_Properties[i].getReadMethod();
        m_Values[i]     = newValue;
        Method setter   = property.getWriteMethod();
        // @todo: Streiche so something was changed, i could check if i have to change the editor

        if (TRACE) System.out.println("Updating prop index " + i + " with " + newValue);
        PropertyEditor  tmpEdit     = null;
        // the findEditor method using properties may retrieve a primitive editor, the other one, for obscure reasons, cant.
        // so Ill use the mightier first.
        tmpEdit = PropertyEditorProvider.findEditor(m_Properties[i], newValue);
        if (tmpEdit == null)    tmpEdit = PropertyEditorProvider.findEditor(m_Properties[i].getPropertyType());
        if (tmpEdit.getClass() != m_Editors[i].getClass()) {
        	m_Values[i]     = newValue;
        	m_Editors[i]    = tmpEdit;
        	if (tmpEdit instanceof GenericObjectEditor) ((GenericObjectEditor) tmpEdit).setClassType(m_Properties[i].getPropertyType());
        	m_Editors[i].setValue(newValue);
        	JComponent NewView = null;
        	if (tmpEdit instanceof sun.beans.editors.BoolEditor) {
        		NewView = new PropertyBoolSelector(tmpEdit);
        	} else {
        		if (tmpEdit instanceof sun.beans.editors.DoubleEditor) {
        			NewView = new PropertyText(tmpEdit);
        		} else {
        			if (tmpEdit.isPaintable() && tmpEdit.supportsCustomEditor()) {
        				NewView = new PropertyPanel(tmpEdit);
        			} else {
        				if (tmpEdit.getTags() != null ) {
        					NewView = new PropertyValueSelector(tmpEdit);
        				} else {
        					if (tmpEdit.getAsText() != null) {
        						NewView = new PropertyText(tmpEdit);
        					} else {
        						System.out.println("Warning: Property \"" + m_Properties[i].getDisplayName()
        								+ "\" has non-displayabale editor.  Skipping.");
        						return false;
        					}
        				}
        			}
        		}
        	}
        	m_Editors[i].addPropertyChangeListener(this);
        	m_Views[i] = NewView;
        	if (m_TipTexts[i] != null) m_Views[i].setToolTipText(m_TipTexts[i]);
        	m_ViewWrapper[i].removeAll();
        	m_ViewWrapper[i].setLayout(new BorderLayout());
        	m_ViewWrapper[i].add(m_Views[i], BorderLayout.CENTER);
        	m_ViewWrapper[i].repaint();
        }
        
//        System.out.println("Value: "+value +" / m_Values[i]: " + m_Values[i]);
        // Now try to update the target with the new value of the property
        // and allow the target to do some changes to the value, therefore
        // reread the new value from the target
        try {
            Object  args[]  = { newValue };
            args[0]         = newValue;
            Object  args2[] = { };
            // setting the current value to the target object
            setter.invoke(m_Target, args);
            // i could also get the new value
            //value = getter.invoke(m_Target, args2);
            // Now i'm reading the set value from the target to my local values
            m_Values[i] = getter.invoke(m_Target, args2);

            if (newValue instanceof Integer) {
                // This could check whether i have to set the value back to
                // the editor, this would allow to check myu and lambda
                // why shouldn't i do this for every property!?
//                System.out.println("value: "+((Integer)value).intValue());
//                System.out.println(" m_Values[i]: "+ ((Integer) m_Values[i]).intValue());
                if (((Integer)newValue).intValue() != ((Integer) m_Values[i]).intValue()) {
                	m_Editors[i].setValue(m_Values[i]);
                }
            }
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof PropertyVetoException) {
                System.out.println("PropertySheetPanel.wasModified(): WARNING: Vetoed; reason is: " + ex.getTargetException().getMessage());
            } else {
                System.out.println("PropertySheetPanel.wasModified(): InvocationTargetException while updating " + property.getName());
                System.out.println("PropertySheetPanel.wasModified(): "+ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            System.out.println("PropertySheetPanel.wasModified(): Unexpected exception while updating " + property.getName());
        }
        //revalidate();
        if (m_Views[i] != null && m_Views[i] instanceof PropertyPanel) {
            //System.err.println("Trying to repaint the property canvas");
            m_Views[i].repaint();
            revalidate();
        }
        return true;
    }

    /** Updates the propertysheet when a value has been changed (from outside
     * the propertysheet?).
     * @param evt a value of type 'PropertyChangeEvent'
     */
    synchronized void wasModified(PropertyChangeEvent evt) {
        if (TRACE) {
            System.out.println("*********** PropertySheetPanel.wasModified(): My Target is "+this.m_Target.getClass());
            System.out.println("PropertySheetPanel.wasModified(): "+evt.toString()+" - "+evt.getNewValue());
        }
        int propIndex=-1;
        if (evt.getSource() instanceof PropertyEditor) {
            PropertyEditor editor = (PropertyEditor) evt.getSource();
            for (int i = 0 ; i < m_Editors.length; i++) {
	            if (m_Editors[i] == editor) {
	            	propIndex = i;
	            	if (wasModified(i, editor.getValue(), true)) break;
	            }
            }
            if (propIndex == -1) System.err.println("error: could not identify event editor! (PropertySheetPanel)");
        } else System.err.println("unknown event source! (PropertySheetPanel)");
    }
    
    /** Updates the propertysheet when a value has been changed (from outside
     * the propertysheet?).
     * @param evt a value of type 'PropertyChangeEvent'
     */
    synchronized boolean wasModified(int propIndex, Object value, boolean followDependencies) {
        if (TRACE) {
            System.out.println("****PropertySheetPanel.wasModified(): My Target is "+ m_Properties[propIndex].getName() + ", new val: " + BeanInspector.toString(value));
        }	            	
        
        if (!updateValue(propIndex, value)) return false;
        
        boolean doRepaint = false;

        for (int i = 0 ; i < m_Editors.length; i++) { // check the views for out-of-date information. this is different than checking the editors
        	if (i != propIndex) {
        		if (updateFieldView(i)) doRepaint = true;
        	}// end if (m_Editors[i] == editor) {
        } // end for (int i = 0 ; i < m_Editors.length; i++) {	
        if (doRepaint) {	// some components have been hidden or reappeared
        	// MK this finally seems to work right, with a scroll pane, too.
        	Container p=this;
        	while (p != null && (!p.getSize().equals(p.getPreferredSize()))) {
        		p.setSize(p.getPreferredSize());
        		p = p.getParent();
        	}
        }

        // Now re-read all the properties and update the editors
        // for any other properties that have changed.
        for (int i = 0; i < m_Properties.length; i++) {
            Object  o;
            Method  getter = null;
            if (m_Editors[i]==null) continue; /// TODO: MK: Im not quite sure this is all good, but it avoids a latency problem 
            try {
                getter = m_Properties[i].getReadMethod();
	            Object args[] = { };
	            o = getter.invoke(m_Target, args);
            } catch (Exception ex) {
	            o = null;
	            System.err.println(ex.getMessage());
	            ex.printStackTrace();
            }
            if (TRACE) System.out.println("# cmp " + BeanInspector.toString(o) + "\n# vs. " + BeanInspector.toString(m_Values[i]));
            if (o == m_Values[i] && (BeanInspector.isJavaPrimitive(o.getClass()))) {
	            // The property is equal to its old value.
	            continue;
            }
            if (o != null && o.equals(m_Values[i])) {
	            // The property is equal to its old value.
	            continue;
            }
            m_Values[i] = o;
            // Make sure we have an editor for this property...
            if (m_Editors[i] == null) {
	            continue;
            }
            // The property has changed!  Update the editor.
            m_Editors[i].removePropertyChangeListener(this);
            m_Editors[i].setValue(o);
            m_Editors[i].addPropertyChangeListener(this);
            if (m_Views[i] != null) {
	            //System.out.println("Trying to repaint " + (i + 1));
	            m_Views[i].repaint();
            }
        }

        if (followDependencies) {
        	// Handle the special method getGOEPropertyUpdateLinks which returns a list of pairs
        	// of strings indicating that on an update of the i-th property, the i+1-th property
        	// should be updated. This is useful for changes within sub-classes of the target
        	// which are not directly displayed in this panel but in sub-panels (and there have an own view etc.)
        	Object o = BeanInspector.callIfAvailable(m_Target, "getGOEPropertyUpdateLinks", null);
        	if ((o != null) && (o instanceof String[])) {
        		maybeTriggerUpdates(propIndex, (String[])o);
        	}
        }
        
        // Make sure the target bean gets repainted.
        if (Beans.isInstanceOf(m_Target, Component.class)) {
            //System.out.println("Beans.getInstanceOf repaint ");
            ((Component)(Beans.getInstanceOf(m_Target, Component.class))).repaint();
        }
        return true;
    }

    /**
     * Check a property for consistency with the object data and update the
     * view if necessary. Return true if a repaint is necessary.
     * @param i
     * @return
     */
    private boolean updateFieldView(int i) {
    	// looking at another field (not changed explicitly, maybe implicitely
    	boolean valChanged = false;
    	boolean doRepaint = false;
    	Object  args[]  = { };
    	Method getter   = m_Properties[i].getReadMethod();
    	if (m_Properties[i].isHidden() || m_Properties[i].isExpert()) {
    		if ((m_Labels[i] != null) && (m_Labels[i].isVisible())) {
        		// something is set to hidden but was visible up to now
    			m_ViewWrapper[i].setVisible(false);
    			m_Labels[i].setVisible(false);
    			doRepaint = true;
    		}
    		return doRepaint;
    	} else {
    		if ((m_Labels[i] != null) && !(m_Labels[i].isVisible())) {
    			 // something is invisible but set to not hidden in the mean time
    			m_ViewWrapper[i].setVisible(true);
    			m_Labels[i].setVisible(true);
    			doRepaint = true;
    		}
    	}
    	try {	// check if view i is up to date and in sync with the value of the getter
    		if (m_Views[i] != null) {
    			Object val = getter.invoke(m_Target, args);
    			if (m_Views[i] instanceof PropertyBoolSelector) {
    				valChanged = (((PropertyBoolSelector)m_Views[i]).isSelected() != ((Boolean)val));
    				if (valChanged) ((PropertyBoolSelector)m_Views[i]).setSelected(((Boolean)val));
    			} else if (m_Views[i] instanceof PropertyText) {
    				valChanged = !(((PropertyText)m_Views[i]).getText()).equals(val.toString());
    				if (valChanged) ((PropertyText)m_Views[i]).setText(val.toString());
    			} else if (m_Views[i] instanceof PropertyPanel) {
    				valChanged = false;//!((PropertyPanel)m_Views[i]).equals(value);
    				// disregard whole panels and hope for the best
    				if (TRACE) {
    					System.out.println("not checking for internal change of PropertyPanel " + !((PropertyPanel)m_Views[i]).equals(val));
    					if (!((PropertyPanel)m_Views[i]).equals(val)) {
    						System.out.println("# " + BeanInspector.toString(m_Views[i]));
    						System.out.println("# " + BeanInspector.toString(val));
    						System.out.println("Ed.: " + BeanInspector.toString(((PropertyPanel)m_Views[i]).getEditor()));
    					}
    				}    				
    			} else if (m_Views[i] instanceof PropertyValueSelector) {
    				//changed = !((SelectedTag)val).isSelectedString((String)((PropertyValueSelector)m_Views[i]).getSelectedItem());
    				// interestingly there seems to be an implicit update of the ValueSelector, possible changes
    				// are already applied, all we need to see it is a repaint
    				m_Views[i].repaint();
    			} else {
    				System.out.println("Warning: Property \"" + i
    						+ "\" not recognized.  Skipping.");
    			}
        	}
    	} catch(Exception exc) {
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
    	if (TRACE) System.out.println("maybeTriggerUpdates: " + BeanInspector.toString(links));
		for (int i = 0; i<max; i+=2) {
			if (links[i].equals(m_Properties[propIndex].getName())) {
				if (TRACE) System.out.println("updating linked property " + links[i+1]);
				updateLinkedProperty(links[i+1]);
			}
		}
	}

	private void updateLinkedProperty(String propName) {
		for (int i=0; i<m_Properties.length; i++) {
			if (m_Properties[i].getName().equals(propName)) {
				if (TRACE) System.out.println("Found linked property " + propName);
		        Method getter   = m_Properties[i].getReadMethod();
	            Object val = null;
				try {
					val = getter.invoke(m_Target, (Object[])null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					val = null;
					e.printStackTrace();
				}
				if (val != null) {
					m_Editors[i].setValue(val);
//					wasModified(i, val, false);
				} else System.err.println("Error in PropertySheetPanel:updateLinkedProperty");
				return;
			}
		}
	}

	/** This method simply looks for an appropriate tiptext
     * @param name      The name of the property
     * @param methods   A list of methods to search.
     * @param target    The target object
     * @return String for the tooltip.
     */
    private String getToolTipText(String name, MethodDescriptor[] methods, Object target, int toHTMLLen) {
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

}


