package eva2.optimization.operator.mutation;

import eva2.gui.PropertyEditorProvider;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.tools.AbstractObjectEditor;
import eva2.optimization.tools.GeneralGEOFaker;
import eva2.optimization.tools.GeneralOptimizationEditorProperty;
import eva2.tools.BasicResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

/**
 *
 */
public class PropertyMutationMixerEditor extends JPanel implements PropertyEditor, java.beans.PropertyChangeListener {

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
    private PropertyMutationMixer mutatorsWithWeights;

    /**
     * The gaphix stuff
     */
    private JComponent editorComponent;
    private JPanel targetListPanel;
    private JTextField[] weights;
    private JComponent[] targets;
    private JButton[] deleteButtons;
    private JScrollPane scrolltargetPanel;
    private GeneralOptimizationEditorProperty[] editors;
    private GeneralGEOFaker component;
    private PropertyChangeListener self;

    public PropertyMutationMixerEditor() {
        self = this;
    }

    /**
     * This method will initialize the CustomEditor Panel
     */
    private void initCustomEditor() {
        self = this;
        this.editorComponent = new JPanel();
        this.editorComponent.setPreferredSize(new Dimension(450, 200));
        this.editorComponent.setMinimumSize(new Dimension(450, 200));

        // initialize the editors
        InterfaceMutation[] list = this.mutatorsWithWeights.getSelectedMutators();
        this.editors = new GeneralOptimizationEditorProperty[list.length];
        for (int i = 0; i < list.length; i++) {
            this.editors[i] = new GeneralOptimizationEditorProperty();
            this.editors[i].name = list[i].getStringRepresentation();
            try {
                this.editors[i].value = list[i];
                this.editors[i].editor = PropertyEditorProvider.findEditor(this.editors[i].value.getClass());
                if (this.editors[i].editor == null) {
                    this.editors[i].editor = PropertyEditorProvider.findEditor(InterfaceMutation.class);
                }
                if (this.editors[i].editor instanceof GenericObjectEditor) {
                    ((GenericObjectEditor) this.editors[i].editor).setClassType(InterfaceMutation.class);
                }
                this.editors[i].editor.setValue(this.editors[i].value);
                this.editors[i].editor.addPropertyChangeListener(this);
                AbstractObjectEditor.findViewFor(this.editors[i]);
                if (this.editors[i].view != null) {
                    this.editors[i].view.repaint();
                }
            } catch (Exception e) {
                System.out.println("Darn can't read the value...");
            }
        }
        this.targetListPanel = new JPanel();
        this.updateTargetList();
        this.scrolltargetPanel = new JScrollPane(this.targetListPanel);

        this.editorComponent.setLayout(new BorderLayout());
        this.editorComponent.add(this.scrolltargetPanel, BorderLayout.CENTER);

        // The Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        JButton addButton = new JButton("Add Mutator");
        JButton normButton = new JButton("Normalize Propabilities");
        normButton.setEnabled(true);
        normButton.addActionListener(normalizeWeights);
        addButton.addActionListener(addTarget);
        buttonPanel.add(normButton);
        buttonPanel.add(addButton);

        this.editorComponent.add(buttonPanel, BorderLayout.SOUTH);

        // Some description would be nice
        JTextArea jt = new JTextArea();
        jt.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jt.setEditable(false);
        jt.setLineWrap(true);
        jt.setWrapStyleWord(true);
        jt.setText(this.mutatorsWithWeights.getDescriptiveString());
        jt.setBackground(getBackground());
        JPanel jp = new JPanel();
        jp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Info"),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)
        ));
        jp.setLayout(new BorderLayout());
        jp.add(jt, BorderLayout.CENTER);
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        JButton help = new JButton("Help");
        help.setEnabled(false);
        p2.add(help, BorderLayout.NORTH);
        jp.add(p2, BorderLayout.EAST);
        GridBagConstraints gbConstraints = new GridBagConstraints();

        this.editorComponent.add(jp, BorderLayout.NORTH);

        this.updateEditor();
    }

    /**
     * This method updates the server list
     */
    private void updateTargetList() {
        BasicResourceLoader loader = BasicResourceLoader.getInstance();
        byte[] bytes;
        InterfaceMutation[] list = this.mutatorsWithWeights.getSelectedMutators();
        double[] weights = this.mutatorsWithWeights.getWeights();

        this.targetListPanel.removeAll();
        this.targetListPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        this.weights = new JTextField[list.length];
        this.targets = new JComponent[list.length];
        this.deleteButtons = new JButton[list.length];
        String[] cups = new String[8];
        for (int i = 0; i < cups.length; i++) {
            cups[i] = "" + (i + 1);
        }
        // The head title
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.weightx = 2;
        this.targetListPanel.add(new JLabel(this.mutatorsWithWeights.getWeigthsLabel()), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.weightx = 10;
        this.targetListPanel.add(new JLabel("Target"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.REMAINDER;
        gbc.gridx = 2;
        gbc.weightx = 1;
        this.targetListPanel.add(new JLabel("Remove"), gbc);
        for (int i = 0; i < list.length; i++) {
            // the weight
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.weightx = 2;
            this.weights[i] = new JTextField("" + weights[i]);
            this.weights[i].addKeyListener(this.readDoubleArrayAction);
            this.targetListPanel.add(this.weights[i], gbc);
            // the status indicator
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 1;
            gbc.weightx = 10;
            this.targets[i] = this.editors[i].view;
            this.targetListPanel.add(this.targets[i], gbc);
            // The delete button
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.REMAINDER;
            gbc.gridx = 2;
            gbc.weightx = 1;
            bytes = loader.getBytesFromResourceLocation("images/Sub24.gif", true);
            this.deleteButtons[i] = new JButton("", new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
            this.deleteButtons[i].addActionListener(deleteTarget);
            this.targetListPanel.add(this.deleteButtons[i], gbc);
        }
        this.targetListPanel.repaint();
        this.targetListPanel.validate();
        if (this.scrolltargetPanel != null) {
            this.scrolltargetPanel.validate();
            this.scrolltargetPanel.repaint();
        }
        if (this.editorComponent != null) {
            this.editorComponent.validate();
            this.editorComponent.repaint();
        }
    }

    /**
     * This action listener,...
     */
    ActionListener updateTargets = event -> updateTargetList();

    /**
     * This action listener,...
     */
    ActionListener addTarget = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            mutatorsWithWeights.addMutator((InterfaceMutation) mutatorsWithWeights.getAvailableMutators()[0].clone());
            int l = mutatorsWithWeights.getSelectedMutators().length;
            GeneralOptimizationEditorProperty[] newEdit = new GeneralOptimizationEditorProperty[l];
            System.arraycopy(editors, 0, newEdit, 0, editors.length);
            InterfaceMutation[] list = mutatorsWithWeights.getSelectedMutators();
            l--;
            newEdit[l] = new GeneralOptimizationEditorProperty();
            newEdit[l].name = list[l].getStringRepresentation();
            try {
                newEdit[l].value = list[l];
                newEdit[l].editor = PropertyEditorProvider.findEditor(newEdit[l].value.getClass());
                if (newEdit[l].editor == null) {
                    newEdit[l].editor = PropertyEditorProvider.findEditor(InterfaceMutation.class);
                }
                if (newEdit[l].editor instanceof GenericObjectEditor) {
                    ((GenericObjectEditor) newEdit[l].editor).setClassType(InterfaceMutation.class);
                }
                newEdit[l].editor.setValue(newEdit[l].value);
                newEdit[l].editor.addPropertyChangeListener(self);
                AbstractObjectEditor.findViewFor(newEdit[l]);
                if (newEdit[l].view != null) {
                    newEdit[l].view.repaint();
                }
            } catch (Exception e) {
                System.out.println("Darn can't read the value...");
            }
            editors = newEdit;
            updateTargetList();
        }
    };

    /**
     * This action listener,...
     */
    ActionListener deleteTarget = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            int l = mutatorsWithWeights.getSelectedMutators().length, j = 0;
            GeneralOptimizationEditorProperty[] newEdit = new GeneralOptimizationEditorProperty[l - 1];
            for (int i = 0; i < deleteButtons.length; i++) {
                if (event.getSource().equals(deleteButtons[i])) {
                    mutatorsWithWeights.removeMutator(i);
                } else {
                    newEdit[j] = editors[i];
                    j++;
                }
            }
            editors = newEdit;
            updateTargetList();
        }
    };

    /**
     * This action listener,...
     */
    ActionListener normalizeWeights = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            mutatorsWithWeights.normalizeWeights();
            updateTargetList();
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
            double[] newW = mutatorsWithWeights.getWeights();

            for (int i = 0; i < newW.length; i++) {
                try {
                    double d = 0;
                    d = Double.parseDouble(weights[i].getText());
                    newW[i] = d;
                } catch (Exception e) {
                }
            }

            mutatorsWithWeights.setWeights(newW);
        }
    };

    /**
     * The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.editorComponent != null) {
            this.targetListPanel.validate();
            this.targetListPanel.repaint();
            this.scrolltargetPanel.validate();
            this.scrolltargetPanel.repaint();
            this.editorComponent.validate();
            this.editorComponent.repaint();
        }
    }

    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyMutationMixer) {
            this.mutatorsWithWeights = (PropertyMutationMixer) o;
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
        return this.mutatorsWithWeights;
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

    /**
     * This is used to hook an action listener to the ok button
     *
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
    }

    /**
     * This is used to remove an action listener from the ok button
     *
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
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
        String rep = "Mixed Mutators";
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
        if (this.component == null) {
            this.initCustomEditor();
            this.component = new GeneralGEOFaker(this, (JPanel) this.editorComponent);
        }
        return this.component;
    }

    /**
     * This method will udate the status of the object taking the values from all
     * supsequent editors and setting them to my object.
     */
    public void updateCenterComponent(PropertyChangeEvent evt) {
        //this.updateTargetList();
        this.updateEditor();
    }

    /**
     * ****************************** java.beans.PropertyChangeListener ************************
     */

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
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
        Object newVal = evt.getNewValue();
        Object oldVal = evt.getOldValue();
        InterfaceMutation[] list = this.mutatorsWithWeights.getSelectedMutators();
        for (int i = 0; i < list.length; i++) {
            if (oldVal.equals(list[i])) {
                list[i] = (InterfaceMutation) newVal;
                this.editors[i].name = list[i].getStringRepresentation();
                try {
                    this.editors[i].value = list[i];
                    this.editors[i].editor = PropertyEditorProvider.findEditor(this.editors[i].value.getClass());
                    if (this.editors[i].editor == null) {
                        this.editors[i].editor = PropertyEditorProvider.findEditor(InterfaceMutation.class);
                    }
                    if (this.editors[i].editor instanceof GenericObjectEditor) {
                        ((GenericObjectEditor) this.editors[i].editor).setClassType(InterfaceMutation.class);
                    }
                    this.editors[i].editor.setValue(this.editors[i].value);
                    this.editors[i].editor.addPropertyChangeListener(this);
                    AbstractObjectEditor.findViewFor(this.editors[i]);
                    if (this.editors[i].view != null) {
                        this.editors[i].view.repaint();
                    }
                } catch (Exception e) {
                    System.out.println("Darn can't read the value...");
                }
                this.targets[i] = this.editors[i].view;
                i = list.length;
            }
        }
        this.updateCenterComponent(evt); // Let our panel update before guys downstream
        propertyChangeSupport.firePropertyChange("", mutatorsWithWeights, mutatorsWithWeights);
    }
}