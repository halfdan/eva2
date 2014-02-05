package eva2.gui.editor;

import eva2.gui.PropertyWeightedLPTchebycheff;

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
public class GenericWeigthedLPTchebycheffEditor extends JPanel implements PropertyEditor {

    /**
     * Handles property change notification
     */
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    /**
     * The label for when we can't edit that type
     */
    private JLabel label = new JLabel("Can't edit", SwingConstants.CENTER);
    /**
     * The filePath that is to be edited
     */
    private PropertyWeightedLPTchebycheff propertyWeightedLPTchebycheff;

    /**
     * The gaphix stuff
     */
    private JPanel customEditor, dataPanel, buttonPanel, targetPanel;
    private JTextField[] idealTextField, weightTextField;
    private JTextField pvalueTextField;
    private JButton okButton;

    public GenericWeigthedLPTchebycheffEditor() {
        // compiled code
    }

    /**
     * This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.customEditor = new JPanel();
        this.customEditor.setLayout(new BorderLayout());

        // target panel
        this.targetPanel = new JPanel();
        this.targetPanel.setLayout(new GridLayout(1, 2));
        this.targetPanel.add(new JLabel("Choose P:"));
        this.pvalueTextField = new JTextField("" + this.propertyWeightedLPTchebycheff.p);
        this.targetPanel.add(this.pvalueTextField);
        this.pvalueTextField.addKeyListener(this.readDoubleAction);
        this.customEditor.add(this.targetPanel, BorderLayout.NORTH);

        // init data panel
        this.dataPanel = new JPanel();
        this.updateDataPanel();
        this.customEditor.add(this.dataPanel, BorderLayout.CENTER);

        // init button panel
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
    KeyListener readDoubleAction = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent event) {
        }

        @Override
        public void keyTyped(KeyEvent event) {
        }

        @Override
        public void keyReleased(KeyEvent event) {
            try {
                int d = new Integer(pvalueTextField.getText()).intValue();
                propertyWeightedLPTchebycheff.p = d;
            } catch (Exception e) {

            }
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
            double[] tmpT = propertyWeightedLPTchebycheff.idealValue;
            double[] tmpP = propertyWeightedLPTchebycheff.weights;

            for (int i = 0; i < tmpT.length; i++) {

                try {
                    double d = 0;
                    d = new Double(idealTextField[i].getText()).doubleValue();
                    tmpT[i] = d;
                } catch (Exception e) {

                }
                try {
                    double d = 0;
                    d = new Double(weightTextField[i].getText()).doubleValue();
                    tmpP[i] = d;
                } catch (Exception e) {

                }
            }

            propertyWeightedLPTchebycheff.idealValue = tmpT;
            propertyWeightedLPTchebycheff.weights = tmpP;
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
        double[] tmpT = this.propertyWeightedLPTchebycheff.idealValue;
        double[] tmpP = this.propertyWeightedLPTchebycheff.weights;
        int obj = this.propertyWeightedLPTchebycheff.p;

        this.pvalueTextField.setText("" + obj);
        this.dataPanel.removeAll();
        this.dataPanel.setLayout(new GridLayout(tmpT.length + 1, 3));
        this.dataPanel.add(new JLabel());
        this.dataPanel.add(new JLabel("Ideal Value"));
        this.dataPanel.add(new JLabel("Weights"));
        this.idealTextField = new JTextField[tmpT.length];
        this.weightTextField = new JTextField[tmpT.length];
        for (int i = 0; i < tmpT.length; i++) {
            JLabel label = new JLabel("Objective " + i + ": ");
            this.dataPanel.add(label);
            this.idealTextField[i] = new JTextField();
            this.idealTextField[i].setText("" + tmpT[i]);
            this.idealTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.dataPanel.add(this.idealTextField[i]);
            this.weightTextField[i] = new JTextField();
            this.weightTextField[i].setText("" + tmpP[i]);
            this.weightTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.dataPanel.add(this.weightTextField[i]);
        }
    }


    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyWeightedLPTchebycheff) {
            this.propertyWeightedLPTchebycheff = (PropertyWeightedLPTchebycheff) o;
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
        return this.propertyWeightedLPTchebycheff;
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
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.removePropertyChangeListener(l);
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
        String rep = "Edit the ideal vector, p and ev. the weights.";
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