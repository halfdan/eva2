package eva2.optimization.tools;


import eva2.gui.BeanInspector;
import eva2.gui.HtmlDemo;
import eva2.gui.PropertyEditorProvider;
import eva2.gui.PropertySheetPanel;
import eva2.gui.editor.GenericObjectEditor;
import eva2.tools.EVAHELP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.Hashtable;

/**
 * TODO
 * This class should be unified with GenericObjectEditor.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.08.2004
 * Time: 13:39:42
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractObjectEditor implements PropertyEditor, java.beans.PropertyChangeListener {
    /**
     * Handles property change notification
     */
    public PropertyChangeSupport m_Support = new PropertyChangeSupport(this);
    /**
     * The Object that is to be edited
     */
    public Object m_Object;
    public Object m_Backup;
    public GeneralGenericObjectEditorPanel objectEditorPanel;
    public Hashtable m_Editors = new Hashtable();

    /**
     * ****************************** java.beans.PropertyChangeListener ************************
     */

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.removePropertyChangeListener(l);
    }

    /**
     * This will wait for the GenericObjectEditor to finish
     * editing an object.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("------------- here-----------------");
        this.updateCenterComponent(evt); // Let our panel update before guys downstream
        m_Support.firePropertyChange("", m_Backup, m_Object);
    }

    /********************************* PropertyEditor *************************/
    /**
     * Returns true since the Object can be shown
     *
     * @return true
     */
    @Override
    abstract public boolean isPaintable();

    /**
     * Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    abstract public void paintValue(Graphics gfx, Rectangle box);

    /**
     * Returns true because we do support a custom editor.
     *
     * @return true
     */
    @Override
    abstract public boolean supportsCustomEditor();

    /**
     * Returns the array editing component.
     *
     * @return a value of type 'java.awt.Component'
     */
    @Override
    abstract public Component getCustomEditor();

    @Override
    abstract public String getAsText();

    @Override
    abstract public void setAsText(String text) throws IllegalArgumentException;

    @Override
    abstract public String getJavaInitializationString();

    @Override
    abstract public String[] getTags();

    /********************************* AbstractObjectEditor *************************/
    /**
     * This method will make a back up of the current
     * object value
     */
    abstract public void makeBackup();

    /**
     * This method will use the backup to undo the
     * last action.
     */
    abstract public void undoBackup();

    /**
     * This method will fire a property change event
     *
     * @param s   A describtive string.
     * @param old old
     * @param n   new
     */
    public void firePropertyChange(String s, Object old, Object n) {
        m_Support.firePropertyChange(s, old, n);
    }

    /**
     * This method allows you to set the current value
     *
     * @param obj The new value
     */
    @Override
    abstract public void setValue(Object obj);

    @Override
    public Object getValue() {
        return this.m_Object;
    }

    /**
     * This method returns the class type
     *
     * @return Class
     */
    abstract public Class getClassType();

//    abstract public Vector getClassesFromProperties();

    /**
     * This method returns a property panel to
     * edit the parameters
     *
     * @return panel
     */
    public JPanel getPropertyPanel() {
        GridBagLayout gbLayout = new GridBagLayout();
        JPanel result = new JPanel();
        JPanel centerWrapper = new JPanel();
        this.m_Editors.clear();

        result.setLayout(new BorderLayout());
        result.setVisible(true);
        // Define the upper area containing the desription and the help button
        String globalInfo = this.getGlobalInfo();
        JTextArea jt = new JTextArea();
        StringBuffer m_HelpText;
        String m_ClassName;
        JButton m_HelpBut;
        m_ClassName = "Genetic Algorithm";
        m_HelpText = new StringBuffer("NAME\n");
        m_HelpText.append(m_ClassName).append("\n\n");
        m_HelpText.append("SYNOPSIS\n").append(globalInfo).append("\n\n");
        m_HelpBut = new JButton("Help");
        m_HelpBut.setToolTipText("More information about " + m_ClassName);
        m_HelpBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                HtmlDemo temp = new HtmlDemo(EVAHELP.cutClassName(m_Object.getClass().getName()) + ".html");
                temp.show();
            }
        });
        jt.setFont(new Font("SansSerif", Font.PLAIN, 12));
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
        gbConstraints.insets = new Insets(0, 5, 0, 5);
        gbLayout.setConstraints(jp, gbConstraints);
        result.add(jp, BorderLayout.NORTH);

        result.add(this.getCenterComponent(), BorderLayout.CENTER);

        result.validate();
        return result;
    }

    public String getHelpFileName() {
        return EVAHELP.cutClassName(m_Object.getClass().getName()) + ".html";
    }

    /**
     * This method return the global info on the current
     * object.
     *
     * @return The global info.
     */
    abstract public String getGlobalInfo();

    /**
     * This method returns the central editing panel for the current
     * object.
     *
     * @return The center component.
     */
    abstract public JComponent getCenterComponent();

    /**
     * This method will udate the status of the object taking the values from all
     * supsequent editors and setting them to my object.
     */
    abstract public void updateCenterComponent(PropertyChangeEvent evt);

    /**
     * This method should return the editor for a given property
     * from a list of property elements
     *
     * @param prop  The property to search for
     * @param propertyDescriptors All properties.
     * @return PropertyEditor
     */
    public GeneralOptimizationEditorProperty getEditorFor(String prop, PropertyDescriptor[] propertyDescriptors, MethodDescriptor[] methods, Object target) {
        GeneralOptimizationEditorProperty result = null;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equalsIgnoreCase(prop)) {
                result = new GeneralOptimizationEditorProperty();
                Object args[] = {};
                result.m_getMethod = propertyDescriptor.getReadMethod();
                result.m_setMethod = propertyDescriptor.getWriteMethod();
                result.m_PropertyType = propertyDescriptor.getPropertyType();
                result.m_Name = propertyDescriptor.getDisplayName();
                result.m_Label = new JLabel(result.m_Name, SwingConstants.RIGHT);
                result.m_TipText = BeanInspector.getToolTipText(result.m_Name, methods, target);
                try {
                    result.m_Value = result.m_getMethod.invoke(target, args);
                    result.m_Editor = PropertyEditorProvider.findEditor(propertyDescriptor, result.m_Value);
                    if (result.m_Editor == null) {
                        result.m_Editor = PropertyEditorProvider.findEditor(result.m_PropertyType);
                    }
                    if (result.m_Editor instanceof GenericObjectEditor) {
                        ((GenericObjectEditor) result.m_Editor).setClassType(result.m_PropertyType);
                    }


                    result.m_Editor.setValue(result.m_Value);
                    result.m_Editor.addPropertyChangeListener(this);
                    this.findViewFor(result);
                    if (result.m_View != null) {
                        result.m_View.repaint();
                    }
                } catch (Exception e) {
                    System.out.println("Darn cant read the value...");
                }
                return result;
            }
        }
        if (result == null) {
            System.err.println("Warning: unknown property or unable to create editor for property " + prop + ", object " + this.getClass().getName());
        }
        return result;
    }

    /**
     * This method tries to find a suitable view for a given Property
     *
     * @param editor The property the select a view for.
     */
    public static void findViewFor(GeneralOptimizationEditorProperty editor) {
        editor.m_View = PropertySheetPanel.getView(editor.m_Editor);
        if (editor.m_View == null) {
            System.out.println("Warning: Property \"" + editor.m_Name
                    + "\" has non-displayabale editor.  Skipping.");
        }
    }
}