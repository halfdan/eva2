package eva2.optimization.tools;


import eva2.gui.BeanInspector;
import eva2.gui.HtmlDemo;
import eva2.gui.PropertyEditorProvider;
import eva2.gui.PropertySheetPanel;
import eva2.gui.editor.GenericObjectEditor;
import eva2.tools.EVAHELP;
import eva2.util.annotation.Parameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.Hashtable;

/**
 * TODO
 * This class should be unified with GenericObjectEditor.
 */

public abstract class AbstractObjectEditor implements PropertyEditor, java.beans.PropertyChangeListener {
    /**
     * Handles property change notification
     */
    public PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * The Object that is to be edited
     */
    public Object object;
    public Object backupObject;
    public GeneralGenericObjectEditorPanel objectEditorPanel;
    public Hashtable editorTable = new Hashtable();

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
     * This will wait for the GenericObjectEditor to finish
     * editing an object.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("------------- here-----------------");
        this.updateCenterComponent(evt); // Let our panel update before guys downstream
        propertyChangeSupport.firePropertyChange("", backupObject, object);
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
        propertyChangeSupport.firePropertyChange(s, old, n);
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
        return this.object;
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
        this.editorTable.clear();

        result.setLayout(new BorderLayout());
        result.setVisible(true);
        // Define the upper area containing the desription and the help button
        String globalInfo = this.getGlobalInfo();
        JTextArea jt = new JTextArea();
        StringBuffer helpText;
        String className;
        JButton helpButton;
        className = "Genetic Algorithm";
        helpText = new StringBuffer("NAME\n");
        helpText.append(className).append("\n\n");
        helpText.append("SYNOPSIS\n").append(globalInfo).append("\n\n");
        helpButton = new JButton("Help");
        helpButton.setToolTipText("More information about " + className);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                HtmlDemo temp = new HtmlDemo(EVAHELP.cutClassName(object.getClass().getName()) + ".html");
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
            p2.add(helpButton, BorderLayout.NORTH);
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
        return EVAHELP.cutClassName(object.getClass().getName()) + ".html";
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
                result.getMethod = propertyDescriptor.getReadMethod();
                result.setMethod = propertyDescriptor.getWriteMethod();
                result.propertyType = propertyDescriptor.getPropertyType();
                result.name = propertyDescriptor.getDisplayName();
                result.label = new JLabel(result.name, SwingConstants.RIGHT);
                // If the property's setter has the Parameter annotation use the description as tipText
                if (propertyDescriptor.getWriteMethod() != null && propertyDescriptor.getWriteMethod().isAnnotationPresent(Parameter.class)) {
                    Parameter parameter = propertyDescriptor.getWriteMethod().getAnnotation(Parameter.class);
                    result.tipText = parameter.description();
                } else {
                    result.tipText = BeanInspector.getToolTipText(result.name, methods, target);
                }
                try {
                    result.value = result.getMethod.invoke(target, args);
                    result.editor = PropertyEditorProvider.findEditor(propertyDescriptor, result.value);
                    if (result.editor == null) {
                        result.editor = PropertyEditorProvider.findEditor(result.propertyType);
                    }
                    if (result.editor instanceof GenericObjectEditor) {
                        ((GenericObjectEditor) result.editor).setClassType(result.propertyType);
                    }


                    result.editor.setValue(result.value);
                    result.editor.addPropertyChangeListener(this);
                    AbstractObjectEditor.findViewFor(result);
                    if (result.view != null) {
                        result.view.repaint();
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
        editor.view = PropertySheetPanel.getView(editor.editor);
        if (editor.view == null) {
            System.out.println("Warning: Property \"" + editor.name
                    + "\" has non-displayabale editor.  Skipping.");
        }
    }
}