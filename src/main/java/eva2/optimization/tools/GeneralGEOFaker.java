package eva2.optimization.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.*;

/**
 *
 */
public class GeneralGEOFaker extends JPanel {

    private JButton openButton, saveButton, okButton, editButton;
    private JFileChooser fileChooser;
    private PropertyEditor editor;
    private JPanel interiorPanel;
    private Class classType;

    public GeneralGEOFaker(PropertyEditor e, JPanel i) {
        this.interiorPanel = i;
        this.editor = e;
        this.classType = this.editor.getValue().getClass();
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());

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
                    editor.setValue(object);
                    // Need a second setValue to get property values filled in OK.
                    // Not sure why.
                    editor.setValue(object); // <- Hannes ?!?!?
                }
            }
        });

        saveButton = new JButton("Save...");
        saveButton.setToolTipText("Save the current configured object");
        saveButton.setEnabled(true);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveObject(editor.getValue());
            }
        });

        editButton = new JButton("Edit Source");
        editButton.setToolTipText("Edit the Source");
        editButton.setEnabled(false);

        okButton = new JButton("OK");
        okButton.setEnabled(true);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
                    Window w = (Window) getTopLevelAncestor();
                    w.dispose();
                }
            }
        });
        setLayout(new BorderLayout());
        add(this.interiorPanel, BorderLayout.CENTER);
        JPanel okcButs = new JPanel();
        okcButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        okcButs.setLayout(new GridLayout(1, 4, 5, 5));
        okcButs.add(openButton);
        okcButs.add(saveButton);
        okcButs.add(okButton);
        okcButs.add(editButton);
        add(okcButs, BorderLayout.SOUTH);
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
                if (!classType.isAssignableFrom(obj.getClass())) {
                    throw new Exception("Object not of type: " + classType.getName());
                }
                return obj;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Couldn't read object: " + selected.getName() + "\n" + ex.getMessage(), "Open object file", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    /**
     * Saves the current object to a file selected by the user.
     *
     * @param object The object to save.
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
                JOptionPane.showMessageDialog(this, "Couldn't write to file: " + sFile.getName() + "\n" + ex.getMessage(), "Save object", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void createFileChooser() {
        fileChooser = new JFileChooser(new File("/resources"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }
}
