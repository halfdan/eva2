package eva2.gui.editor;


import eva2.gui.PropertyEditorProvider;
import eva2.gui.PropertyOptimizationObjectives;
import eva2.optimization.tools.AbstractObjectEditor;
import eva2.optimization.tools.GeneralOptimizationEditorProperty;
import eva2.problems.InterfaceOptimizationObjective;
import eva2.problems.InterfaceOptimizationTarget;
import eva2.tools.BasicResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;


/**
 *
 */
public class OptimizationObjectivesEditor extends JPanel implements PropertyEditor, java.beans.PropertyChangeListener {

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
    private PropertyOptimizationObjectives optimizationObjectives;

    /**
     * The gaphix stuff
     */
    private JComponent editor;
    private JPanel targetList;
    private JComponent[] targets;
    private JButton[] deleteButton;
    private JScrollPane scrollTargets;
    private GeneralOptimizationEditorProperty[] editors;
    private PropertyChangeListener self;

    public OptimizationObjectivesEditor() {
        self = this;

    }

    /**
     * This method will initialize the CustomEditor Panel
     */
    private void initCustomEditor() {
        self = this;
        this.editor = new JPanel();
        this.editor.setPreferredSize(new Dimension(400, 200));
        this.editor.setMinimumSize(new Dimension(400, 200));

        // initialize the editors
        InterfaceOptimizationObjective[] list = this.optimizationObjectives.getSelectedTargets();
        this.editors = new GeneralOptimizationEditorProperty[list.length];
        for (int i = 0; i < list.length; i++) {
            this.editors[i] = new GeneralOptimizationEditorProperty();
            this.editors[i].name = list[i].getName();
            try {
                this.editors[i].value = list[i];
                this.editors[i].editor = PropertyEditorProvider.findEditor(this.editors[i].value.getClass());
                if (this.editors[i].editor == null) {
                    this.editors[i].editor = PropertyEditorProvider.findEditor(InterfaceOptimizationObjective.class);
                }
                if (this.editors[i].editor instanceof GenericObjectEditor) {
                    ((GenericObjectEditor) this.editors[i].editor).setClassType(InterfaceOptimizationTarget.class);
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
        this.targetList = new JPanel();
        this.updateTargetList();
        this.scrollTargets = new JScrollPane(this.targetList);

        this.editor.setLayout(new BorderLayout());
        this.editor.add(this.scrollTargets, BorderLayout.CENTER);

        // the add button
        JButton addButton = new JButton("Add Opt. Target");
        addButton.addActionListener(addTarget);
        this.editor.add(addButton, BorderLayout.SOUTH);

        // Some description would be nice
        JTextArea jt = new JTextArea();
        jt.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jt.setEditable(false);
        jt.setLineWrap(true);
        jt.setWrapStyleWord(true);
        jt.setText("Choose and parameterize optimization objectives.");
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
        this.editor.add(jp, BorderLayout.NORTH);

        this.updateEditor();
    }

    /**
     * This method updates the server list
     */
    private void updateTargetList() {
        BasicResourceLoader loader = BasicResourceLoader.getInstance();
        byte[] bytes;
        InterfaceOptimizationObjective[] list = this.optimizationObjectives.getSelectedTargets();

        this.targetList.removeAll();
        this.targetList.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        this.targets = new JComponent[list.length];
        this.deleteButton = new JButton[list.length];
        String[] cups = new String[8];
        for (int i = 0; i < cups.length; i++) {
            cups[i] = "" + (i + 1);
        }
        // The head title
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.weightx = 10;
        this.targetList.add(new JLabel("Target"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.REMAINDER;
        gbc.gridx = 1;
        gbc.weightx = 1;
        this.targetList.add(new JLabel("Remove"), gbc);
        for (int i = 0; i < list.length; i++) {
            // the status indicator
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.weightx = 10;
//            this.targets[i] = new JButton(""+list[i].getName());
//            this.targets[i].setEnabled(false);
            this.targets[i] = this.editors[i].view;
            this.targetList.add(this.targets[i], gbc);
            // The delete button
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.REMAINDER;
            gbc.gridx = 1;
            gbc.weightx = 1;
            bytes = loader.getBytesFromResourceLocation("images/Sub24.gif", true);
            this.deleteButton[i] = new JButton("", new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
            this.deleteButton[i].addActionListener(deleteTarget);
            this.targetList.add(this.deleteButton[i], gbc);
        }
        this.targetList.repaint();
        this.targetList.validate();
        if (this.scrollTargets != null) {
            this.scrollTargets.validate();
            this.scrollTargets.repaint();
        }
        if (this.editor != null) {
            this.editor.validate();
            this.editor.repaint();
        }
    }

    /**
     * This action listener,...
     */
    ActionListener updateTargets = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateTargetList();
        }
    };

    /**
     * This action listener,...
     */
    ActionListener addTarget = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            optimizationObjectives.addTarget((InterfaceOptimizationObjective) optimizationObjectives.getAvailableTargets()[0].clone());
            int l = optimizationObjectives.getSelectedTargets().length;
            GeneralOptimizationEditorProperty[] newEdit = new GeneralOptimizationEditorProperty[l];
            System.arraycopy(editors, 0, newEdit, 0, editors.length);
            InterfaceOptimizationObjective[] list = optimizationObjectives.getSelectedTargets();
            l--;
            newEdit[l] = new GeneralOptimizationEditorProperty();
            newEdit[l].name = list[l].getName();
            try {
                newEdit[l].value = list[l];
                newEdit[l].editor = PropertyEditorProvider.findEditor(newEdit[l].value.getClass());
                if (newEdit[l].editor == null) {
                    newEdit[l].editor = PropertyEditorProvider.findEditor(InterfaceOptimizationObjective.class);
                }
                if (newEdit[l].editor instanceof GenericObjectEditor) {
                    ((GenericObjectEditor) newEdit[l].editor).setClassType(InterfaceOptimizationTarget.class);
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
            int l = optimizationObjectives.getSelectedTargets().length, j = 0;
            GeneralOptimizationEditorProperty[] newEdit = new GeneralOptimizationEditorProperty[l - 1];
            for (int i = 0; i < deleteButton.length; i++) {
                if (event.getSource().equals(deleteButton[i])) {
                    optimizationObjectives.removeTarget(i);
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
     * The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.editor != null) {
            this.targetList.validate();
            this.targetList.repaint();
            this.scrollTargets.validate();
            this.scrollTargets.repaint();
            this.editor.validate();
            this.editor.repaint();
        }
    }


    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyOptimizationObjectives) {
            this.optimizationObjectives = (PropertyOptimizationObjectives) o;
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
        return this.optimizationObjectives;
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
        String rep = "Optimization Targets";
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
        if (this.editor == null) {
            this.initCustomEditor();
        }
        return editor;
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
        Object newVal = evt.getNewValue();
        Object oldVal = evt.getOldValue();
        InterfaceOptimizationObjective[] list = this.optimizationObjectives.getSelectedTargets();
        for (int i = 0; i < list.length; i++) {
            if (oldVal.equals(list[i])) {
                list[i] = (InterfaceOptimizationObjective) newVal;
                this.editors[i].name = list[i].getName();
                try {
                    this.editors[i].value = list[i];
                    this.editors[i].editor = PropertyEditorProvider.findEditor(this.editors[i].value.getClass());
                    if (this.editors[i].editor == null) {
                        this.editors[i].editor = PropertyEditorProvider.findEditor(InterfaceOptimizationObjective.class);
                    }
                    if (this.editors[i].editor instanceof GenericObjectEditor) {
                        ((GenericObjectEditor) this.editors[i].editor).setClassType(InterfaceOptimizationTarget.class);
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
            }
        }
        this.updateCenterComponent(evt); // Let our panel update before guys downstream
        propertyChangeSupport.firePropertyChange("", optimizationObjectives, optimizationObjectives);
    }
}