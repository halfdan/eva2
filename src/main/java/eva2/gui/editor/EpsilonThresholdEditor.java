package eva2.gui.editor;

import eva2.gui.PropertyEpsilonThreshold;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

/**
 *
 */
public class EpsilonThresholdEditor extends JPanel implements PropertyEditor {

    /**
     * Handles property change notification
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * The filePath that is to be edited
     */
    private PropertyEpsilonThreshold epsilonThreshhold;

    /**
     * The gaphix stuff
     */
    private JPanel customEditor, dataPanel, buttonPanel, targetPanel;
    private JTextField[] targetTextField, punishTextField;
    private JComboBox objectiveComboBox;
    private JButton okButton;

    public EpsilonThresholdEditor() {
        // compiled code
    }

    /**
     * This method will initialize the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.customEditor = new JPanel();
        this.customEditor.setLayout(new BorderLayout());

        // target panel
        this.targetPanel = new JPanel();
        this.targetPanel.setLayout(new GridLayout(1, 2));
        this.targetPanel.add(new JLabel("Optimize:"));
        this.objectiveComboBox = new JComboBox();
        for (int i = 0; i < this.epsilonThreshhold.targetValue.length; i++) {
            this.objectiveComboBox.addItem("Objective " + i);
        }
        this.targetPanel.add(this.objectiveComboBox);
        this.objectiveComboBox.addItemListener(this.objectiveAction);
        this.customEditor.add(this.targetPanel, BorderLayout.NORTH);

        // initialize data panel
        this.dataPanel = new JPanel();
        this.updateDataPanel();
        this.customEditor.add(this.dataPanel, BorderLayout.CENTER);

        // initialize button panel
        this.buttonPanel = new JPanel();
        this.okButton = new JButton("OK");
        this.okButton.setEnabled(true);
        this.okButton.addActionListener(e -> {
            //backupObject = copyObject(object);
            if ((customEditor.getTopLevelAncestor() != null) && (customEditor.getTopLevelAncestor() instanceof Window)) {
                Window w = (Window) customEditor.getTopLevelAncestor();
                w.dispose();
            }
        });
        this.buttonPanel.add(this.okButton);
        this.customEditor.add(this.buttonPanel, BorderLayout.SOUTH);
        this.updateEditor();
    }

    /**
     * This action listener adds an element to DoubleArray
     */
    ItemListener objectiveAction = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            epsilonThreshhold.optimizeObjective = objectiveComboBox.getSelectedIndex();
            updateEditor();
        }
    };

    /**
     * This action listener reads all values
     */
    KeyListener readDoubleArrayAction = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent event) {
        }

        @Override
        public void keyTyped(KeyEvent event) {
        }

        @Override
        public void keyReleased(KeyEvent event) {
            double[] tmpT = epsilonThreshhold.targetValue;
            double[] tmpP = epsilonThreshhold.punishment;

            for (int i = 0; i < tmpT.length; i++) {

                try {
                    double d = 0;
                    d = Double.parseDouble(targetTextField[i].getText());
                    tmpT[i] = d;
                } catch (Exception e) {

                }
                try {
                    double d = 0;
                    d = Double.parseDouble(punishTextField[i].getText());
                    tmpP[i] = d;
                } catch (Exception e) {

                }
            }

            epsilonThreshhold.targetValue = tmpT;
            epsilonThreshhold.punishment = tmpP;
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
        double[] tmpT = this.epsilonThreshhold.targetValue;
        double[] tmpP = this.epsilonThreshhold.punishment;
        int obj = this.epsilonThreshhold.optimizeObjective;

        this.dataPanel.removeAll();
        this.dataPanel.setLayout(new GridLayout(tmpT.length + 1, 3));
        this.dataPanel.add(new JLabel());
        this.dataPanel.add(new JLabel("Target Value"));
        this.dataPanel.add(new JLabel("Punishment"));
        this.targetTextField = new JTextField[tmpT.length];
        this.punishTextField = new JTextField[tmpT.length];
        for (int i = 0; i < tmpT.length; i++) {
            JLabel label = new JLabel("Objective " + i + ": ");
            this.dataPanel.add(label);
            this.targetTextField[i] = new JTextField();
            this.targetTextField[i].setText("" + tmpT[i]);
            this.targetTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.dataPanel.add(this.targetTextField[i]);
            this.punishTextField[i] = new JTextField();
            this.punishTextField[i].setText("" + tmpP[i]);
            this.punishTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.dataPanel.add(this.punishTextField[i]);
        }
        this.targetTextField[obj].setEditable(false);
        this.punishTextField[obj].setEditable(false);
    }


    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyEpsilonThreshold) {
            this.epsilonThreshhold = (PropertyEpsilonThreshold) o;
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
        return this.epsilonThreshhold;
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
        String rep = "Edit Epsilon Threshhold";
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