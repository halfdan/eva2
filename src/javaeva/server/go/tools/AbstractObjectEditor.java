package javaeva.server.go.tools;

import javaeva.client.EvAClient;
import javaeva.gui.*;
import javaeva.tools.EVAHELP;

import javax.swing.*;
import java.beans.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * TODO
 * This class should be unified with GenericObjectEditor.
 * 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.08.2004
 * Time: 13:39:42
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractObjectEditor implements PropertyEditor, java.beans.PropertyChangeListener {
    /** Handles property change notification */
    public PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The Object that is to be edited*/
    public Object                                  m_Object;
    public Object                                  m_Backup;
    public GeneralGenericObjectEditorPanel         m_EditorComponent;
    public Hashtable                               m_Editors = new Hashtable();

    /********************************* java.beans.PropertyChangeListener *************************/

    public void addPropertyChangeListener(PropertyChangeListener l) {
        m_Support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        m_Support.removePropertyChangeListener(l);
    }
    /** This will wait for the GenericObjectEditor to finish
     * editing an object.
     * @param evt
     */
     public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("------------- here-----------------");
        this.updateCenterComponent(evt); // Let our panel update before guys downstream
        m_Support.firePropertyChange("", m_Backup, m_Object);
    }

    /********************************* PropertyEditor *************************/
   /** Returns true since the Object can be shown
     * @return true
     */
    abstract public boolean isPaintable();

    /** Paints a representation of the current classifier.
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    abstract public void paintValue(Graphics gfx, Rectangle box);

    /** Returns true because we do support a custom editor.
    * @return true
    */
    abstract public boolean supportsCustomEditor();

    /** Returns the array editing component.
    * @return a value of type 'java.awt.Component'
    */
    abstract public Component getCustomEditor();

    abstract public String getAsText();
    abstract public void setAsText(String text) throws IllegalArgumentException;
    abstract public String getJavaInitializationString();
    abstract public String[] getTags();

    /********************************* AbstractObjectEditor *************************/
    /** This method will make a back up of the current
     * object value
     */
    abstract public void makeBackup();

    /** This method will use the backup to undo the
     * last action.
     */
    abstract public void undoBackup();

    /** This method will fire a property change event
     * @param s     A describtive string.
     * @param old   old
     * @param n     new
     */
    public void firePropertyChange(String s, Object old, Object n) {
         m_Support.firePropertyChange(s, old, n);
    }

    /** This method allows you to set the current value
     * @param obj   The new value
     */
    abstract public void setValue(Object obj);
    public Object getValue() {
        return this.m_Object;
    }

    /** This method returns the class type
     * @return Class
     */
    abstract public Class getClassType();

//    abstract public Vector getClassesFromProperties();
    
    /** This method returns a property panel to
     * edit the parameters
     * @return panel
     */
    public JPanel getPropertyPanel() {
        GridBagLayout   gbLayout        = new GridBagLayout();
        JPanel          result          = new JPanel();
        JPanel          centerWrapper   = new JPanel();
        this.m_Editors.clear();

        result.setLayout(new BorderLayout());
        result.setVisible(true);
        // Define the upper area containing the desription and the help button
        String globalInfo               = this.getGlobalInfo();
        JTextArea       jt              = new JTextArea();
        StringBuffer    m_HelpText;
        String          m_ClassName;
        JButton         m_HelpBut;
	    m_ClassName = "Genetic Algorithm";
        m_HelpText  = new StringBuffer("NAME\n");
        m_HelpText.append(m_ClassName).append("\n\n");
        m_HelpText.append("SYNOPSIS\n").append(globalInfo).append("\n\n");
        m_HelpBut   = new JButton("Help");
	    m_HelpBut.setToolTipText("More information about " + m_ClassName);
	    m_HelpBut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                HtmlDemo temp = new HtmlDemo(EVAHELP.cutClassName(m_Object.getClass().getName())+".html");
                temp.show();
            }
        });
        jt.setFont(new Font("SansSerif", Font.PLAIN,12));
	    jt.setEditable(false);
	    jt.setLineWrap(true);
	    jt.setWrapStyleWord(true);
	    jt.setText(globalInfo);
        jt.setBackground(result.getBackground());
	    JPanel jp = new JPanel();
	    jp.setBorder(BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Info"),
			BorderFactory.createEmptyBorder(0, 5, 5, 5)));
	    jp.setLayout(new BorderLayout());
	    jp.add(jt, BorderLayout.CENTER);
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        if (HtmlDemo.resourceExists(getHelpFileName())) {
        	p2.add(m_HelpBut, BorderLayout.NORTH);
        }
        jp.add(p2, BorderLayout.EAST); 
	    GridBagConstraints gbConstraints = new GridBagConstraints();
	    //gbConstraints.anchor = GridBagConstraints.EAST;
	    gbConstraints.fill = GridBagConstraints.BOTH;
	    //gbConstraints.gridy = 0;     gbConstraints.gridx = 0;
	    gbConstraints.gridwidth = 2;
	    gbConstraints.insets = new Insets(0,5,0,5);
	    gbLayout.setConstraints(jp, gbConstraints);
	    result.add(jp, BorderLayout.NORTH);

        result.add(this.getCenterComponent(), BorderLayout.CENTER);

        result.validate();
        return result;
    }

    public String getHelpFileName() {
    	return EVAHELP.cutClassName(m_Object.getClass().getName())+".html";
    }
    
    /** This method return the global info on the current
     * object.
     * @return The global info.
     */
    abstract public String getGlobalInfo();

    /** This method returns the central editing panel for the current
     * object.
     * @return The center component.
     */
    abstract public JComponent getCenterComponent();

    /** This method will udate the status of the object taking the values from all
     * supsequent editors and setting them to my object.
     */
    abstract public void updateCenterComponent(PropertyChangeEvent evt);

    /** This method should return the editor for a given property
     * from a list of property elements
     * @param prop  The property to search for
     * @param props All properties.
     * @return PropertyEditor
     */
    public GeneralGOEProperty getEditorFor(String prop, PropertyDescriptor[] props, MethodDescriptor[] methods, Object target) {
        GeneralGOEProperty result = null;
        for (int i = 0; i < props.length; i++) {
            if (props[i].getName().equalsIgnoreCase(prop)) {
                result = new GeneralGOEProperty();
                Object          args[]  = { };
                result.m_getMethod      = props[i].getReadMethod();
                result.m_setMethod      = props[i].getWriteMethod();
                result.m_PropertyType   = props[i].getPropertyType();
                result.m_Name           = props[i].getDisplayName();
                result.m_Label          = new JLabel(result.m_Name, SwingConstants.RIGHT);
                result.m_TipText        = this.getToolTipText(result.m_Name, methods, target);
                try {
                    result.m_Value      = result.m_getMethod.invoke(target, args);
//                    result.m_Editor     = PropertyEditorProvider.findEditor(result.m_Value.getClass());
//                    if (result.m_Editor == null) result.m_Editor = PropertyEditorProvider.findEditor(result.m_PropertyType);
//                    if (result.m_Editor instanceof GenericObjectEditor)
//                        ((GenericObjectEditor) result.m_Editor).setClassType(result.m_PropertyType);
//                    result.m_Editor.setValue(result.m_Value);
//                    result.m_Editor.addPropertyChangeListener(this);
                  result.m_Editor     = PropertyEditorProvider.findEditor(props[i], result.m_Value);
                  if (result.m_Editor == null) result.m_Editor = PropertyEditorProvider.findEditor(result.m_PropertyType);
                  if (result.m_Editor instanceof GenericObjectEditor)
                      ((GenericObjectEditor) result.m_Editor).setClassType(result.m_PropertyType);
                  
                  
                  result.m_Editor.setValue(result.m_Value);
                  result.m_Editor.addPropertyChangeListener(this);
                    this.findViewFor(result);
                    if (result.m_View != null) result.m_View.repaint();
                } catch (Exception e) {
                    System.out.println("Darn cant read the value...");
                }
                return result;
            }
        }
        return result;
    }

    /** This method simply looks for an appropriate tiptext
     * @param name      The name of the property
     * @param methods   A list of methods to search.
     * @param target    The target object
     * @return String for the tooltip.
     */
    public String getToolTipText(String name, MethodDescriptor[] methods, Object target) {
        String result   = "No tooltip available.";
        String tipName  = name + "TipText";
	    for (int j = 0; j < methods.length; j++) {
	        String mname    = methods[j].getDisplayName();
	        Method meth     = methods[j].getMethod();
	        if (mname.equals(tipName)) {
	            if (meth.getReturnType().equals(String.class)) {
	                try {
                        Object  args[]  = { };
		                String  tempTip = (String)(meth.invoke(target, args));
		                int     ci      = tempTip.indexOf('.');
		                if (ci < 0) result = tempTip;
		                else        result = tempTip.substring(0, ci);
	                } catch (Exception ex) {
                    }
	                return result;
	            }
	        }
	    } // end for looking for tiptext
        return result;
    }

    /** This method tries to find a suitable view for a given Property
     * @param editor    The property the select a view for.
     */
    public void findViewFor(GeneralGOEProperty editor) {
        if (editor.m_Editor instanceof sun.beans.editors.BoolEditor) {
            editor.m_View = new PropertyBoolSelector(editor.m_Editor);
        } else {
            if (editor.m_Editor instanceof sun.beans.editors.DoubleEditor) {
                editor.m_View = new PropertyText(editor.m_Editor);
            } else {
                if (editor.m_Editor.isPaintable() && editor.m_Editor.supportsCustomEditor()) {
                    editor.m_View = new PropertyPanel(editor.m_Editor);
                } else {
                    if (editor.m_Editor.getTags() != null ) {
                        editor.m_View = new PropertyValueSelector(editor.m_Editor);
                    } else {
                        if (editor.m_Editor.getAsText() != null) {
                            editor.m_View = new PropertyText(editor.m_Editor);
                        } else {
                            System.out.println("Warning: Property \"" + editor.m_Name
                                 + "\" has non-displayabale editor.  Skipping.");
                        }
                    }
                }
            }
        }
    }
}