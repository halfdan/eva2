package eva2.gui.editor;

import eva2.gui.PropertyIntArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

/**
 *
 */
public class GenericIntArrayEditor extends JPanel implements PropertyEditor {

    /**
     * Handles property change notification
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * The label for when we can't edit that type
     */
    private JLabel label = new JLabel("Can't edit", SwingConstants.CENTER);
    /**
     * The filePath that is to be edited
     */
    private PropertyIntArray intArray;

    /**
     * The gaphix stuff
     */
    private JPanel customEditor, dataPanel, buttonPanel;
    private JTextField[] inputTextField;
    private JButton okButton;

    public GenericIntArrayEditor() {
        // compiled code
    }

    /**
     * This method will initialize the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.customEditor = new JPanel();
        this.customEditor.setLayout(new BorderLayout());

        this.customEditor.add(new JLabel("Current Int Array:"), BorderLayout.NORTH);

        // initialize data panel
        this.dataPanel = new JPanel();
        this.updateDataPanel();
        this.customEditor.add(this.dataPanel, BorderLayout.CENTER);

        // initialize button panel
        this.buttonPanel = new JPanel();
        this.okButton = new JButton("OK");
        this.okButton.setEnabled(true);
        this.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //backupObject = copyObject(object);
                if ((customEditor.getTopLevelAncestor() != null) && (customEditor.getTopLevelAncestor() instanceof Window)) {
                    Window w = (Window) customEditor.getTopLevelAncestor();
                    w.dispose();
                }
            }
        });
        this.buttonPanel.add(this.okButton);
        this.customEditor.add(this.buttonPanel, BorderLayout.SOUTH);
        this.updateEditor();
    }

    /**
     * This action listener reads all values
     */
    KeyListener readIntArrayAction = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent event) {
        }

        @Override
        public void keyTyped(KeyEvent event) {
        }

        @Override
        public void keyReleased(KeyEvent event) {
            int[] tmpD = new int[inputTextField.length];

            for (int i = 0; i < tmpD.length; i++) {
                try {
                    int d = 0;
                    d = Integer.parseInt(inputTextField[i].getText());
                    tmpD[i] = d;
                } catch (Exception e) {

                }
            }
            intArray.setIntArray(tmpD);
        }
    };

    /**
     * The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.customEditor != null) {
            this.updateDataPanel();
            this.customEditor.validate();
            this.customEditor.repaint();
        }
    }

    /**
     * This method updates the data panel
     */
    private void updateDataPanel() {
        int[] tmpD = this.intArray.getIntArray();

        this.dataPanel.removeAll();
        this.dataPanel.setLayout(new GridLayout(tmpD.length, 2));
        this.inputTextField = new JTextField[tmpD.length];
        for (int i = 0; i < tmpD.length; i++) {
            JLabel label = new JLabel("Value X" + i + ": ");
            this.dataPanel.add(label);
            this.inputTextField[i] = new JTextField();
            this.inputTextField[i].setText("" + tmpD[i]);
            this.inputTextField[i].addKeyListener(this.readIntArrayAction);
            this.dataPanel.add(this.inputTextField[i]);
        }
    }


    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyIntArray) {
            this.intArray = (PropertyIntArray) o;
            this.updateEditor();
        }
    }

    /**
     * Returns the current object.
     *
     * @return the current object
     */
    @Override
    public Object getValue() {
        return this.intArray;
    }

    @Override
    public String getJavaInitializationString() {
        return "TEST";
    }

    /**
     *
     */
    @Override
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    @Override
    public String[] getTags() {
        return null;
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
     * This is used to hook an action listener to the ok button
     *
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        okButton.addActionListener(a);
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
     * Returns true since the Object can be shown
     *
     * @return true
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /**
     * Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = "Edit int[]";
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3);
    }

    /**
     * Returns true because we do support a custom editor.
     *
     * @return true
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns the array editing component.
     *
     * @return a value of type 'java.awt.Component'
     */
    @Override
    public Component getCustomEditor() {
        if (this.customEditor == null) {
            this.initCustomEditor();
        }
        return customEditor;
    }
}