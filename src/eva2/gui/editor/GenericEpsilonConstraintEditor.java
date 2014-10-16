package eva2.gui.editor;

import eva2.gui.PropertyEpsilonConstraint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.07.2005
 * Time: 16:33:45
 * To change this template use File | Settings | File Templates.
 */
public class GenericEpsilonConstraintEditor extends JPanel implements PropertyEditor {

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
    private PropertyEpsilonConstraint epsilonConstraint;

    /**
     * The gaphix stuff
     */
    private JPanel customEditor, dataPanel, buttonPanel, targetPanel;
    private JTextField[] targetTextField;
    private JComboBox objectiveComboBox;
    private JButton okButton;

    public GenericEpsilonConstraintEditor() {
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
        for (int i = 0; i < this.epsilonConstraint.targetValue.length; i++) {
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
     * This action listener adds an element to DoubleArray
     */
    ItemListener objectiveAction = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            epsilonConstraint.optimizeObjective = objectiveComboBox.getSelectedIndex();
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
            double[] tmpT = epsilonConstraint.targetValue;

            for (int i = 0; i < tmpT.length; i++) {

                try {
                    double d = 0;
                    d = new Double(targetTextField[i].getText()).doubleValue();
                    tmpT[i] = d;
                } catch (Exception e) {

                }
            }

            epsilonConstraint.targetValue = tmpT;
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
        double[] tmpT = this.epsilonConstraint.targetValue;
        int obj = this.epsilonConstraint.optimizeObjective;

        this.dataPanel.removeAll();
        this.dataPanel.setLayout(new GridLayout(tmpT.length + 1, 2));
        this.dataPanel.add(new JLabel());
        this.dataPanel.add(new JLabel("Target Value"));
        this.targetTextField = new JTextField[tmpT.length];
        for (int i = 0; i < tmpT.length; i++) {
            JLabel label = new JLabel("Objective " + i + ": ");
            this.dataPanel.add(label);
            this.targetTextField[i] = new JTextField();
            this.targetTextField[i].setText("" + tmpT[i]);
            this.targetTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.dataPanel.add(this.targetTextField[i]);
        }
        this.targetTextField[obj].setEditable(false);
    }


    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyEpsilonConstraint) {
            this.epsilonConstraint = (PropertyEpsilonConstraint) o;
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
        return this.epsilonConstraint;
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
        String rep = "Edit Epsilon Constraint";
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