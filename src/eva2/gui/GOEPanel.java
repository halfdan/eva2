package eva2.gui;

import eva2.server.go.tools.FileTools;
import eva2.tools.BasicResourceLoader;
import eva2.tools.EVAHELP;
import eva2.tools.SerializedObject;
import java.awt.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 *
 */
public class GOEPanel extends JPanel implements ItemListener {

    private Object backupObject;
    private PropertyChangeSupport propChangeSupport;    
    /**
     * The chooser component
     */
    private JComboBox objectChooser;
    /**
     * The component that performs classifier customization
     */
    private PropertySheetPanel propertySheetPanel;
    /**
     * The model containing the list of names to select from
     */
    private DefaultComboBoxModel comboBoxModel;
    /**
     * Open object from disk
     */
    private JButton openButton;
    /**
     * Save object to disk
     */
    private JButton saveButton;
    /**
     * ok button
     */
    private JButton okayButton;
    /**
     * cancel button
     */
    private JButton cancelButton;
    /**
     * Creates the GUI editor component
     */
    private GenericObjectEditor genericObjectEditor = null;
    private boolean withComboBoxToolTips = true; // should tool tips for the combo box be created?
    private int tipMaxLen = 100; // maximum length of tool tip
    private HashMap<String, String> classNameMap;

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
        backupObject = backup;
        propChangeSupport = support;
        genericObjectEditor = goe;

        try {
            if (!(Proxy.isProxyClass(m_Object.getClass()))) {
                backupObject = copyObject(m_Object);
            }
        } catch (OutOfMemoryError err) {
            backupObject = null;
            System.gc();
            System.err.println("Could not create backup object: not enough memory (GOEPanel backup of " + m_Object + ")");
        }
        comboBoxModel = new DefaultComboBoxModel(new String[0]);
        objectChooser = new JComboBox(comboBoxModel);
        objectChooser.setEditable(false);
        propertySheetPanel = new PropertySheetPanel();
        propertySheetPanel.addPropertyChangeListener(
                new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                propChangeSupport.firePropertyChange("", backupObject, genericObjectEditor.getValue());
            }
        });
        openButton = makeIconButton("images/Open16.gif", "Open");
        openButton.setToolTipText("Load a configured object");
        openButton.setEnabled(true);
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                Object object = FileTools.openObject(openButton, genericObjectEditor.getClassType());
                if (object != null) {
                    // setValue takes care of: Making sure obj is of right type,
                    // and firing property change.
                    genericObjectEditor.setValue(object);
                    // Need a second setValue to get property values filled in OK.
                    // Not sure why.
                    genericObjectEditor.setValue(object); // <- Hannes ?!?!?
                }
            }
        });

        saveButton = makeIconButton("images/Save16.gif", "Save");
        saveButton.setToolTipText("Save the current configured object");
        saveButton.setEnabled(true);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                FileTools.saveObjectWithFileChooser(saveButton, genericObjectEditor.getValue());
            }
        });

        okayButton = new JButton("OK");
        okayButton.setEnabled(true);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                backupObject = copyObject(genericObjectEditor.getValue());

                updateClassType();
                updateChildPropertySheet();

                /*
                 * ToDo: This is really ugly. Find a way to make this better.
                 */
                Container container = GOEPanel.this.getParent();
                while (!(container instanceof JDialog)) {
                    container = container.getParent();
                }
                ((JDialog) container).dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(true);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (backupObject != null) {
                    // TODO m_goe.setObject(m_Object);
                    genericObjectEditor.setValue(copyObject(backupObject));
                    updateClassType();
                    updateChooser();
                    updateChildPropertySheet();
                }
                /*
                 * ToDo: This is really ugly. Find a way to make this better.
                 */
                Container container = GOEPanel.this.getParent();
                while (!(container instanceof JDialog)) {
                    container = container.getParent();
                }
                ((JDialog) container).dispose();
            }
        });

        setLayout(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        add(objectChooser, gbConstraints);

        gbConstraints.weightx = 1.0;
        gbConstraints.weighty = 1.0;
        gbConstraints.gridy = 1;
        gbConstraints.gridheight = GridBagConstraints.RELATIVE;
        gbConstraints.fill = GridBagConstraints.BOTH;
        add(propertySheetPanel, gbConstraints);

        JToolBar buttonBar = new JToolBar();
        buttonBar.setRollover(true);
        buttonBar.setFloatable(false);
        buttonBar.add(openButton);
        buttonBar.add(saveButton);

        /* Add spacer to the end of the line */
        buttonBar.add(Box.createHorizontalGlue());

        if (withCancel) {
            buttonBar.add(cancelButton);
        }
        buttonBar.add(okayButton);

        gbConstraints.weightx = 0.0;
        gbConstraints.weighty = 0.0;
        gbConstraints.gridy = 2;
        gbConstraints.anchor = GridBagConstraints.LINE_START;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(buttonBar, gbConstraints);

        if (genericObjectEditor.getClassType() != null) {
            updateClassType();
            updateChooser();
            updateChildPropertySheet();
        }
        objectChooser.addItemListener(this);
    }

    /**
     * This method is duplicated from EvAModuleButtonPanelMaker. ToDo: Refactor
     * this.
     *
     * @param iconSrc
     * @param title
     * @return
     */
    private JButton makeIconButton(final String iconSrc, final String title) {
        JButton newButton;
        byte[] bytes;
        bytes = BasicResourceLoader.instance().getBytesFromResourceLocation(iconSrc, false);
        if (bytes == null) {
            newButton = new JButton(title);
        } else {
            newButton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        }
        return newButton;
    }

    public void setEnabledOkCancelButtons(boolean enabled) {
        okayButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }

    /**
     * Makes a copy of an object using serialization.
     *
     * @param source the object to copy
     * @return a copy of the source object
     */
    protected Object copyObject(Object source) {
        Object result = null;
        try {
//			System.out.println("Copying " + BeanInspector.toString(source));
            SerializedObject so = new SerializedObject(source);
            result = so.getObject();
            so = null;
        } catch (Exception ex) {
            System.err.println("GenericObjectEditor: Problem making backup object");
            System.err.println(source.getClass().getName());
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * This is used to hook an action listener to the ok button.
     *
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        okayButton.addActionListener(a);
    }

    /**
     * This is used to hook an action listener to the cancel button
     *
     * @param a The action listener.
     */
    public void addCancelListener(ActionListener a) {
        cancelButton.addActionListener(a);
    }

    /**
     * This is used to remove an action listener from the ok button
     *
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
        okayButton.removeActionListener(a);
    }

    /**
     * This is used to remove an action listener from the cancel button
     *
     * @param a The action listener
     */
    public void removeCancelListener(ActionListener a) {
        cancelButton.removeActionListener(a);
    }

    public void setTarget(Object o) {
        propertySheetPanel.setTarget(o);
    }

    /**
     *
     */
    protected void updateClassType() {
        List<String> classesLongNames;
        ArrayList<Class<?>> instances = new ArrayList<Class<?>>(5);
        classesLongNames = GenericObjectEditor.getClassesFromProperties(genericObjectEditor.getClassType().getName(), instances);
        if (classesLongNames.size() > 1) {
            classNameMap = new HashMap<String, String>();
            for (String className : classesLongNames) {
                classNameMap.put(EVAHELP.cutClassName(className), className);
            }
            Vector<String> classesList = new Vector<String>(classesLongNames);
            objectChooser.setModel(new DefaultComboBoxModel(classesList));
            if (withComboBoxToolTips) {
                objectChooser.setRenderer(new ToolTipComboBoxRenderer(collectComboToolTips(instances, tipMaxLen)));
            }
            GridBagConstraints gbConstraints = new GridBagConstraints();
            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            gbConstraints.gridx = 0;
            gbConstraints.gridy = 0;
            add(objectChooser, gbConstraints);
        } else {
            remove(objectChooser);
        }
    }

    private String[] collectComboToolTips(List<Class<?>> instances, int maxLen) {
        String[] tips = new String[instances.size()];
        for (int i = 0; i < tips.length; i++) {
            tips[i] = null;
            Class[] classParams = new Class[]{};
            try {
                String tip = null;
                Method giMeth = instances.get(i).getDeclaredMethod("globalInfo", classParams);
                if (Modifier.isStatic(giMeth.getModifiers())) {
                    tip = (String) giMeth.invoke(null, (Object[]) null);
                }
                if (tip != null) {
                    if (tip.length() <= maxLen) {
                        tips[i] = tip;
                    } else {
                        tips[i] = tip.substring(0, maxLen - 2) + "..";
                    }
                }
            } catch (Exception e) {
            }
        }
        return tips;
    }

    protected void updateChooser() {
        String objectName = /*
                 * EVAHELP.cutClassName
                 */ (genericObjectEditor.getValue().getClass().getName());
        boolean found = false;
        for (int i = 0; i < comboBoxModel.getSize(); i++) {
            if (objectName.equals((String) comboBoxModel.getElementAt(i))) {
                found = true;
                break;
            }
        }
        if (!found) {
            comboBoxModel.addElement(objectName);
        }
        objectChooser.getModel().setSelectedItem(objectName);
    }

    /**
     * Updates the child property sheet, and creates if needed
     */
    public void updateChildPropertySheet() {
        // Set the object as the target of the propertysheet
        propertySheetPanel.setTarget(genericObjectEditor.getValue());
        // Adjust size of containing window if possible
        if ((getTopLevelAncestor() != null)
                && (getTopLevelAncestor() instanceof Window)) {
            ((Window) getTopLevelAncestor()).pack();
        }
    }

    /**
     * When the chooser selection is changed, ensures that the Object is changed
     * appropriately.
     *
     * @param e a value of type 'ItemEvent'
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        String className;

        if ((e.getSource() == objectChooser) && (e.getStateChange() == ItemEvent.SELECTED)) {
            className = (String) objectChooser.getSelectedItem();
            //className = classNameMap.get(className);
            try {
                Object n = (Object) Class.forName(className).newInstance();
                genericObjectEditor.setValue(n);
                // TODO ? setObject(n);
            } catch (Exception ex) {
                System.err.println("Exeption in itemStateChanged " + ex.getMessage());
                System.err.println("Classpath is " + System.getProperty("java.class.path"));
                ex.printStackTrace();
                objectChooser.hidePopup();
                objectChooser.setSelectedIndex(0);
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
        toolTips = tips;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            if ((toolTips != null) && (index >= 0)) {
                if (toolTips[index] != null) {
                    list.setToolTipText(toolTips[index]);
                }
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
