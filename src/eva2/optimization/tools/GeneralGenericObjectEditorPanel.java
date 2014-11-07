package eva2.optimization.tools;


import eva2.gui.editor.GenericObjectEditor;
import eva2.tools.EVAHELP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;

/**
 *
 */
public class GeneralGenericObjectEditorPanel extends JPanel implements ItemListener {

    // This is the Object Editor i belong too
    private AbstractObjectEditor objectEditor;
    /**
     * The chooser component
     */
    private JComboBox objectChooser;
    /**
     * The component that performs classifier customization
     */
    private JPanel propertyPanelWrapper;
    private JPanel propertyPanel;
    /**
     * The model containing the list of names to select from
     */
    private DefaultComboBoxModel objectNames;
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
    public JButton okButton;
    /**
     * cancel button
     */
    private JButton cancelButton;
    /**
     * The filechooser for opening and saving object files
     */
    private JFileChooser fileChooser;
    /**
     * Creates the GUI editor component
     */
    private ArrayList<String> longClassNames;

    /**
     * This is the general construtor method
     *
     * @param oe A link to the oe;
     */
    public GeneralGenericObjectEditorPanel(AbstractObjectEditor oe) {
        this.objectEditor = oe;
        oe.makeBackup();
        objectNames = new DefaultComboBoxModel(new String[0]);
        objectChooser = new JComboBox(objectNames);
        objectChooser.setEditable(false);
        propertyPanelWrapper = new JPanel();
        propertyPanel = this.objectEditor.getPropertyPanel();
        propertyPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                objectEditor.firePropertyChange("", null, objectEditor.getValue());
            }
        });
        openButton = new JButton("Open...");
        openButton.setToolTipText("Load a configured object");
        openButton.setEnabled(true);
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object object = openObject();
                if (object != null) {
                    // setValue takes care of: Making sure obj is of right type,
                    // and firing property change.
                    objectEditor.setValue(object);
                    // Need a second setValue to get property values filled in OK.
                    // Not sure why.
                    objectEditor.setValue(object); // <- Hannes ?!?!?
                }
            }
        });

        saveButton = new JButton("Save...");
        saveButton.setToolTipText("Save the current configured object");
        saveButton.setEnabled(true);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveObject(objectEditor.getValue());
            }
        });

        okButton = new JButton("OK");
        okButton.setEnabled(true);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectEditor.makeBackup();
                if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
                    Window w = (Window) getTopLevelAncestor();
                    w.dispose();
                }
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectEditor.undoBackup();
                updateClassType();
                updateChooser();
                updateChildPropertySheet();
                if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
                    Window w = (Window) getTopLevelAncestor();
                    w.dispose();
                }
            }
        });

        setLayout(new BorderLayout());
        add(objectChooser, BorderLayout.NORTH);  // important
        propertyPanelWrapper.add(propertyPanel);
        add(propertyPanelWrapper, BorderLayout.CENTER);

        JPanel okcButs = new JPanel();
        okcButs.setBorder(BorderFactory.createEmptyBorder());
        okcButs.setLayout(new GridLayout(1, 4, 5, 5));
        okcButs.add(openButton);
        okcButs.add(saveButton);
        okcButs.add(okButton);
        //okcButs.add(cancelButton);
        add(okcButs, BorderLayout.SOUTH);

        if (this.objectEditor.getClassType() != null) {
            updateClassType();
            updateChooser();
            updateChildPropertySheet();
        }
        objectChooser.addItemListener(this);
    }

    /**
     * Opens an object from a file selected by the user.
     *
     * @return the loaded object, or null if the operation was cancelled
     */
    protected Object openObject() {
        if (fileChooser == null) {
            createFileChooser();
        }

        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
                Object obj = oi.readObject();
                oi.close();
                if (!this.objectEditor.getClassType().isAssignableFrom(obj.getClass())) {
                    throw new Exception("Object not of type: " + this.objectEditor.getClassType().getName());
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

    /**
     * Opens an object from a file selected by the user.
     *
     * @param object the object to save.
     */
    protected void saveObject(Object object) {
        if (fileChooser == null) {
            createFileChooser();
        }
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File sFile = fileChooser.getSelectedFile();
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

    /**
     * This method creates a file chooser
     */
    protected void createFileChooser() {
        fileChooser = new JFileChooser(new File("/resources"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    /**
     * This is used to hook an action listener to the ok button
     *
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        okButton.addActionListener(a);
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
        okButton.removeActionListener(a);
    }

    /**
     * This is used to remove an action listener from the cancel button
     *
     * @param a The action listener
     */
    public void removeCancelListener(ActionListener a) {
        cancelButton.removeActionListener(a);
    }

    /**
     * This method updates the class type*
     */
    protected void updateClassType() {
        longClassNames = GenericObjectEditor.getClassesFromProperties(objectEditor.getClassType().getName(), null);
        objectChooser.setModel(new DefaultComboBoxModel(longClassNames.toArray()));
        if (longClassNames.size() > 1) {
            add(objectChooser, BorderLayout.NORTH);
        } else {
            remove(objectChooser);
        }
    }

    protected void updateChooser() {
        String objectName = /*EVAHELP.cutClassName*/ (this.objectEditor.getValue().getClass().getName());
        boolean found = false;
        for (int i = 0; i < objectNames.getSize(); i++) {
            if (objectName.equals(objectNames.getElementAt(i))) {
                found = true;
                break;
            }
        }
        if (!found) {
            objectNames.addElement(objectName);
        }
        objectChooser.getModel().setSelectedItem(objectName);
    }

    /**
     * Updates the child property sheet, and creates if needed..
     * *what!?*
     */
    public void updateChildPropertySheet() {
        this.propertyPanelWrapper.removeAll();
        this.propertyPanelWrapper.add(this.objectEditor.getPropertyPanel());
        // Adjust size of containing window if possible
        if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
            ((Window) getTopLevelAncestor()).pack();
        }
    }

    /**
     * When the chooser selection is changed, ensures that the Object
     * is changed appropriately.
     *
     * @param e a value of type 'ItemEvent'
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        String className = (String) objectChooser.getSelectedItem();

        //System.out.println("Event-Quelle: " + e.getSource().toString());
        if ((e.getSource() == objectChooser) && (e.getStateChange() == ItemEvent.SELECTED)) {
            className = (String) objectChooser.getSelectedItem();
            try {
                System.out.println(className);
                Object n = Class.forName(className).newInstance();
                this.objectEditor.setValue(n);
            } catch (Exception ex) {
                System.out.println("Exeption in itemStateChanged" + ex.getMessage());
                objectChooser.hidePopup();
                objectChooser.setSelectedIndex(0);
                JOptionPane.showMessageDialog(this,
                        "Could not create an example of\n"
                                + className + "\n"
                                + "from the current classpath",
                        "GenericObjectEditor",
                        JOptionPane.ERROR_MESSAGE);
                EVAHELP.getSystemPropertyString();
            }
        }
    }
}