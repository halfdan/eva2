package eva2.gui;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.tools.FileTools;
import eva2.tools.BasicResourceLoader;
import eva2.tools.SerializedObject;
import eva2.tools.StringTools;
import eva2.util.annotation.Description;
import eva2.yaml.BeanSerializer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 *
 */
public class OptimizationEditorPanel extends JPanel implements ItemListener {
    private static final Logger LOGGER = Logger.getLogger(OptimizationEditorPanel.class.getName());

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
    private int tipMaxLen = 100; // maximum length of tool tip

    /**
     *
     */
    public OptimizationEditorPanel(Object target, Object backup, PropertyChangeSupport support, GenericObjectEditor goe) {
        this(target, backup, support, goe, false);
    }

    /**
     *
     */
    public OptimizationEditorPanel(Object target, Object backup, PropertyChangeSupport support, GenericObjectEditor goe, boolean withCancel) {
        backupObject = backup;
        propChangeSupport = support;
        genericObjectEditor = goe;

        try {
            if (!(Proxy.isProxyClass(target.getClass()))) {
                backupObject = copyObject(target);
            }
        } catch (OutOfMemoryError err) {
            backupObject = null;
            System.gc();
            System.err.println("Could not create backup object: not enough memory (OptimizationEditorPanel backup of " + target + ")");
        }
        comboBoxModel = new DefaultComboBoxModel(new Vector<Item>());
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
                FileFilter filter = new FileNameExtensionFilter("YAML file", "yml", "yaml");
                Object object = FileTools.openObject(openButton, genericObjectEditor.getClassType(), filter);
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
                FileFilter filter = new FileNameExtensionFilter("YAML file", "yml", "yaml");
                FileTools.saveObjectWithFileChooser(saveButton, BeanSerializer.serializeObject(genericObjectEditor.getValue()), filter);
            }
        });

        okayButton = new JButton("OK");
        okayButton.setEnabled(true);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                //backupObject = copyObject(genericObjectEditor.getValue());

                updateClassType();
                updateChildPropertySheet();

                /*
                 * ToDo: This is really ugly. Find a way to make this better.
                 */
                Container container = OptimizationEditorPanel.this.getParent();
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
                    genericObjectEditor.setValue(copyObject(backupObject));
                    updateClassType();
                    updateChooser();
                    updateChildPropertySheet();
                }
                /*
                 * ToDo: This is really ugly. Find a way to make this better.
                 */
                Container container = OptimizationEditorPanel.this.getParent();
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
     * @param iconSrc Source path of icon
     * @param title Title of button
     * @return A JButton with the title and icon
     */
    private JButton makeIconButton(final String iconSrc, final String title) {
        JButton newButton;
        byte[] bytes;
        bytes = BasicResourceLoader.getInstance().getBytesFromResourceLocation(iconSrc, false);
        if (bytes == null) {
            newButton = new JButton(title);
        } else {
            newButton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        }
        return newButton;
    }

    public void setEnabledOkCancelButtons(boolean enabled) {
        okayButton.setEnabled(enabled);
        okayButton.setVisible(enabled);
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
    public void updateClassType() {
        List<String> classesLongNames;
        ArrayList<Class<?>> instances = new ArrayList<>(5);
        classesLongNames = GenericObjectEditor.getClassesFromProperties(genericObjectEditor.getClassType().getName(), instances);
        LOGGER.finest("Selected type for OptimizationEditorPanel: " + genericObjectEditor.getClassType().getName());
        if (classesLongNames.size() > 1) {
            Vector<Item> classesList = new Vector<>();
            String[] toolTips = collectComboToolTips(instances, tipMaxLen);
            int i = 0;
            for (String className : classesLongNames) {
                String displayName = StringTools.cutClassName(className);

                classesList.add(new Item(className, displayName, toolTips[i++]));
            }
            comboBoxModel = new DefaultComboBoxModel(classesList);
            objectChooser.setModel(comboBoxModel);
            objectChooser.setRenderer(new ToolTipComboBoxRenderer());
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

            String tip = null;

            Description description = instances.get(i).getAnnotation(Description.class);
            if (description != null) {
                tip = description.value();
            }

            if (tip != null) {
                if (tip.length() <= maxLen) {
                    tips[i] = tip;
                } else {
                    tips[i] = tip.substring(0, maxLen - 2) + "..";
                }
            }
        }
        return tips;
    }

    public void updateChooser() {
        String objectName = genericObjectEditor.getValue().getClass().getName();
        for (int i = 0; i < comboBoxModel.getSize(); i++) {
            Item element = (Item)comboBoxModel.getElementAt(i);

            if (objectName.equals(element.getId())) {
                objectChooser.getModel().setSelectedItem(element);
                break;
            }
        }
    }

    /**
     * Updates the child property sheet, and creates if needed
     */
    public void updateChildPropertySheet() {
        // Set the object as the target of the PropertySheet
        propertySheetPanel.setTarget(genericObjectEditor.getValue());
        // Update the chooser
        this.updateChooser();
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
            className = ((Item)objectChooser.getSelectedItem()).getId();
            try {
                Object n = Class.forName(className).newInstance();
                genericObjectEditor.setValue(n);
                // TODO ? setObject(n);
            } catch (Exception ex) {
                System.err.println("Exception in itemStateChanged " + ex.getMessage());
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
            }
        }
    }
}

class ToolTipComboBoxRenderer extends BasicComboBoxRenderer {

    private static final long serialVersionUID = -5781643352198561208L;

    public ToolTipComboBoxRenderer() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);

        if (value != null) {
            Item item = (Item)value;
            setText(item.getDisplayName());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                list.setToolTipText(item.getDescription());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
        }

        if (index == -1) {
            Item item = (Item)value;
            setText(item.getDisplayName());
        }

        setFont(list.getFont());
        return this;
    }
}

class Item
{
    private String id;
    private String displayName;
    private String description;

    public Item(String id, String displayName, String description)
    {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString()
    {
        return id;
    }
}
