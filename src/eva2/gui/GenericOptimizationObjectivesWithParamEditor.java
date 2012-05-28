package eva2.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import eva2.server.go.problems.InterfaceOptimizationObjective;
import eva2.server.go.tools.AbstractObjectEditor;
import eva2.server.go.tools.GeneralGOEProperty;
import eva2.tools.BasicResourceLoader;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.01.2005
 * Time: 13:46:20
 * To change this template use File | Settings | File Templates.
 */
public class GenericOptimizationObjectivesWithParamEditor extends JPanel implements PropertyEditor, java.beans.PropertyChangeListener {

    /** Handles property change notification */
    private PropertyChangeSupport       m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                      m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyOptimizationObjectivesWithParam m_OptimizationObjectivesWithWeights;

    /** The gaphix stuff */
    private JComponent                  m_Editor;
    private JPanel                      m_TargetList;
    private JTextField[]                m_Weights;
    private JComponent[]                m_Targets;
    private JButton[]                   m_Delete;
    private JScrollPane                 m_ScrollTargets;
    private GeneralGOEProperty[]        m_Editors;
    private PropertyChangeListener      m_self;

    public GenericOptimizationObjectivesWithParamEditor() {
        m_self = this;
    }

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        m_self = this;
        this.m_Editor = new JPanel();
        this.m_Editor.setPreferredSize(new Dimension(450, 200));
        this.m_Editor.setMinimumSize(new Dimension(450, 200));

        // init the editors
        InterfaceOptimizationObjective[]   list =  this.m_OptimizationObjectivesWithWeights.getSelectedTargets();
        this.m_Editors = new GeneralGOEProperty[list.length];
        for (int i = 0; i < list.length; i++) {
            this.m_Editors[i] = new GeneralGOEProperty();
            this.m_Editors[i].m_Name   = list[i].getName();
            try {
                this.m_Editors[i].m_Value      = list[i];
                this.m_Editors[i].m_Editor     = PropertyEditorProvider.findEditor(this.m_Editors[i].m_Value.getClass());
                if (this.m_Editors[i].m_Editor == null) this.m_Editors[i].m_Editor = PropertyEditorProvider.findEditor(InterfaceOptimizationObjective.class);
                if (this.m_Editors[i].m_Editor instanceof GenericObjectEditor)
                    ((GenericObjectEditor) this.m_Editors[i].m_Editor).setClassType(InterfaceOptimizationObjective.class);
                this.m_Editors[i].m_Editor.setValue(this.m_Editors[i].m_Value);
                this.m_Editors[i].m_Editor.addPropertyChangeListener(this);
                AbstractObjectEditor.findViewFor(this.m_Editors[i]);
                if (this.m_Editors[i].m_View != null) this.m_Editors[i].m_View.repaint();
            } catch (Exception e) {
                System.out.println("Darn can't read the value...");
            }
        }
        this.m_TargetList = new JPanel();
            this.updateTargetList();
        this.m_ScrollTargets = new JScrollPane(this.m_TargetList);

        this.m_Editor.setLayout(new BorderLayout());
        this.m_Editor.add(this.m_ScrollTargets, BorderLayout.CENTER);

        // The Button Panel
        JPanel      buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,2));
        JButton     addButton = new JButton("Add Opt. Target");
        JButton     normButton = new JButton("Normalize Weights");
        normButton.setEnabled(this.m_OptimizationObjectivesWithWeights.isNormalizationEnabled());
        normButton.addActionListener(normalizeWeights);
        addButton.addActionListener(addTarget);
        buttonPanel.add(normButton);
        buttonPanel.add(addButton);

        this.m_Editor.add(buttonPanel, BorderLayout.SOUTH);

        // Some description would be nice
        JTextArea   jt          = new JTextArea();
        jt.setFont(new Font("SansSerif", Font.PLAIN,12));
	    jt.setEditable(false);
	    jt.setLineWrap(true);
	    jt.setWrapStyleWord(true);
	    jt.setText(this.m_OptimizationObjectivesWithWeights.getDescriptiveString());
        jt.setBackground(getBackground());
	    JPanel jp = new JPanel();
	    jp.setBorder(BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder("Info"),
			BorderFactory.createEmptyBorder(0, 5, 5, 5)
		));
	    jp.setLayout(new BorderLayout());
	    jp.add(jt, BorderLayout.CENTER);
        JPanel      p2  = new JPanel();
        p2.setLayout(new BorderLayout());
        JButton help = new JButton("Help");
        help.setEnabled(false);
        p2.add(help, BorderLayout.NORTH);
        jp.add(p2, BorderLayout.EAST);
	    GridBagConstraints gbConstraints = new GridBagConstraints();

        this.m_Editor.add(jp, BorderLayout.NORTH);

        this.updateEditor();
    }

    /** This method updates the server list
     *
     */
    private void updateTargetList() {
    	BasicResourceLoader             loader = BasicResourceLoader.instance();
        byte[]                          bytes;
        InterfaceOptimizationObjective[]   list =  this.m_OptimizationObjectivesWithWeights.getSelectedTargets();
        double[]                        weights = this.m_OptimizationObjectivesWithWeights.getWeights();

        this.m_TargetList.removeAll();
        this.m_TargetList.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        this.m_Weights  = new JTextField[list.length];
        this.m_Targets  = new JComponent[list.length];
        this.m_Delete   = new JButton[list.length];
        String[] cups   = new String[8];
        for (int i = 0; i < cups.length; i++) cups[i] = ""+(i+1);
        // The head title
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;
        gbc.gridx       = 0;
        gbc.weightx     = 2;
        this.m_TargetList.add(new JLabel(this.m_OptimizationObjectivesWithWeights.getWeigthsLabel()), gbc);
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.BOTH;
        gbc.gridx       = 1;
        gbc.weightx     = 10;
        this.m_TargetList.add(new JLabel("Target"), gbc);
        gbc.anchor      = GridBagConstraints.WEST;
        gbc.fill        = GridBagConstraints.REMAINDER;
        gbc.gridx       = 2;
        gbc.weightx     = 1;
        this.m_TargetList.add(new JLabel("Remove"), gbc);
        for (int i = 0; i < list.length; i++) {
            // the weight
            gbc.anchor      = GridBagConstraints.WEST;
            gbc.fill        = GridBagConstraints.BOTH;
            gbc.gridx       = 0;
            gbc.weightx     = 2;
            this.m_Weights[i] = new JTextField(""+weights[i]);
            this.m_Weights[i].addKeyListener(this.readDoubleArrayAction);
            this.m_TargetList.add(this.m_Weights[i], gbc);
            // the status indicator
            gbc.anchor      = GridBagConstraints.WEST;
            gbc.fill        = GridBagConstraints.BOTH;
            gbc.gridx       = 1;
            gbc.weightx     = 10;
            this.m_Targets[i] = this.m_Editors[i].m_View;
            this.m_TargetList.add(this.m_Targets[i], gbc);
            // The delete button
            gbc.anchor      = GridBagConstraints.WEST;
            gbc.fill        = GridBagConstraints.REMAINDER;
            gbc.gridx       = 2;
            gbc.weightx     = 1;
            bytes = loader.getBytesFromResourceLocation("images/Sub24.gif", true);
            this.m_Delete[i] = new JButton("", new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
            this.m_Delete[i].addActionListener(deleteTarget);
            this.m_TargetList.add(this.m_Delete[i], gbc);
        }
        this.m_TargetList.repaint();
        this.m_TargetList.validate();
        if (this.m_ScrollTargets != null) {
            this.m_ScrollTargets.validate();
            this.m_ScrollTargets.repaint();
        }
        if (this.m_Editor != null) {
            this.m_Editor.validate();
            this.m_Editor.repaint();
        }
    }

    /** This action listener,...
     */
    ActionListener updateTargets = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            updateTargetList();
        }
    };

    /** This action listener,...
     */
    ActionListener addTarget = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            m_OptimizationObjectivesWithWeights.addTarget((InterfaceOptimizationObjective)m_OptimizationObjectivesWithWeights.getAvailableTargets()[0].clone());
            int l = m_OptimizationObjectivesWithWeights.getSelectedTargets().length;
            GeneralGOEProperty[] newEdit = new GeneralGOEProperty[l];
            for (int i = 0; i < m_Editors.length; i++) {
                newEdit[i] = m_Editors[i];
            }
            InterfaceOptimizationObjective[] list = m_OptimizationObjectivesWithWeights.getSelectedTargets();
            l--;
            newEdit[l]          = new GeneralGOEProperty();
            newEdit[l].m_Name   = list[l].getName();
            try {
                newEdit[l].m_Value      = list[l];
                newEdit[l].m_Editor     = PropertyEditorProvider.findEditor(newEdit[l].m_Value.getClass());
                if (newEdit[l].m_Editor == null) newEdit[l].m_Editor = PropertyEditorProvider.findEditor(InterfaceOptimizationObjective.class);
                if (newEdit[l].m_Editor instanceof GenericObjectEditor)
                    ((GenericObjectEditor) newEdit[l].m_Editor).setClassType(InterfaceOptimizationObjective.class);
                newEdit[l].m_Editor.setValue(newEdit[l].m_Value);
                newEdit[l].m_Editor.addPropertyChangeListener(m_self);
                AbstractObjectEditor.findViewFor(newEdit[l]);
                if (newEdit[l].m_View != null) newEdit[l].m_View.repaint();
            } catch (Exception e) {
                System.out.println("Darn can't read the value...");
            }
            m_Editors = newEdit;
            updateTargetList();
        }
    };

    /** This action listener,...
     */
    ActionListener deleteTarget = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            int l = m_OptimizationObjectivesWithWeights.getSelectedTargets().length, j = 0;
            GeneralGOEProperty[] newEdit = new GeneralGOEProperty[l-1];
            for (int i = 0; i < m_Delete.length; i++) {
                if (event.getSource().equals(m_Delete[i])) m_OptimizationObjectivesWithWeights.removeTarget(i);
                else {
                    newEdit[j] = m_Editors[i];
                    j++;
                }
            }
            m_Editors = newEdit;
            updateTargetList();
        }
    };

    /** This action listener,...
     */
    ActionListener normalizeWeights = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            double[] newW = m_OptimizationObjectivesWithWeights.getWeights();
            double sum = 0;
            for (int i = 0; i < newW.length; i++) {
                sum += newW[i];
            }
            if (sum != 0) {
                for (int i = 0; i < newW.length; i++) {
                    newW[i] = newW[i]/sum;
                }
                m_OptimizationObjectivesWithWeights.setWeights(newW);
            }
            updateTargetList();
        }
    };

    /** This action listener reads all values
     */
    KeyListener readDoubleArrayAction = new KeyListener() {
        public void keyPressed(KeyEvent event) {
        }
        public void keyTyped(KeyEvent event) {
        }

        public void keyReleased(KeyEvent event) {
            double[] newW   = m_OptimizationObjectivesWithWeights.getWeights();

            for (int i = 0; i < newW.length; i++) {
                try {
                    double d = 0;
                    d = new Double(m_Weights[i].getText()).doubleValue();
                    newW[i] = d;
                } catch (Exception e) {
                }
            }

            m_OptimizationObjectivesWithWeights.setWeights(newW);
         }
    };

    /** The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.m_Editor != null) {
            this.m_TargetList.validate();
            this.m_TargetList.repaint();
            this.m_ScrollTargets.validate();
            this.m_ScrollTargets.repaint();
            this.m_Editor.validate();
            this.m_Editor.repaint();
        }
    }

    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof PropertyOptimizationObjectivesWithParam) {
            this.m_OptimizationObjectivesWithWeights= (PropertyOptimizationObjectivesWithParam) o;
            this.updateEditor();
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_OptimizationObjectivesWithWeights;
    }

    public String getJavaInitializationString() {
        return "TEST";
    }

    /**
     *
     */
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    public String[] getTags() {
        return null;
    }

    /** This is used to hook an action listener to the ok button
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        //m_OKButton.addActionListener(a);
    }

    /** This is used to remove an action listener from the ok button
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
        //m_OKButton.removeActionListener(a);
    }

    /** Returns true since the Object can be shown
     * @return true
     */
    public boolean isPaintable() {
        return true;
    }

    /** Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = "Optimization Targets With Weights";
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3  );
    }

    /** Returns true because we do support a custom editor.
    * @return true
    */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Returns the array editing component.
    * @return a value of type 'java.awt.Component'
    */
    public Component getCustomEditor() {
        if (this.m_Editor == null) this.initCustomEditor();
        return m_Editor;
    }

    /** This method will udate the status of the object taking the values from all
     * supsequent editors and setting them to my object.
     */
    public void updateCenterComponent(PropertyChangeEvent evt) {
        //this.updateTargetList();
        this.updateEditor();
    }

    /********************************* java.beans.PropertyChangeListener *************************/

    public void addPropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.removePropertyChangeListener(l);
    }
    /** This will wait for the GenericObjectEditor to finish
     * editing an object.
     * @param evt
     */
     public void propertyChange(PropertyChangeEvent evt) {
        Object newVal = evt.getNewValue();
        Object oldVal = evt.getOldValue();
        InterfaceOptimizationObjective[] list = this.m_OptimizationObjectivesWithWeights.getSelectedTargets();
        for (int i = 0; i < list.length; i++) {
            if (oldVal.equals(list[i])) {
                list[i] = (InterfaceOptimizationObjective)newVal;
                this.m_Editors[i].m_Name   = list[i].getName();
                try {
                    this.m_Editors[i].m_Value      = list[i];
                    this.m_Editors[i].m_Editor     = PropertyEditorProvider.findEditor(this.m_Editors[i].m_Value.getClass());
                    if (this.m_Editors[i].m_Editor == null) this.m_Editors[i].m_Editor = PropertyEditorProvider.findEditor(InterfaceOptimizationObjective.class);
                    if (this.m_Editors[i].m_Editor instanceof GenericObjectEditor)
                        ((GenericObjectEditor) this.m_Editors[i].m_Editor).setClassType(InterfaceOptimizationObjective.class);
                    this.m_Editors[i].m_Editor.setValue(this.m_Editors[i].m_Value);
                    this.m_Editors[i].m_Editor.addPropertyChangeListener(this);
                    AbstractObjectEditor.findViewFor(this.m_Editors[i]);
                    if (this.m_Editors[i].m_View != null) this.m_Editors[i].m_View.repaint();
                } catch (Exception e) {
                    System.out.println("Darn can't read the value...");
                }
                this.m_Targets[i] = this.m_Editors[i].m_View;
            }
        }
        //this.m_OptimizationTargets.setSelectedTargets(list);
        this.updateCenterComponent(evt); // Let our panel update before guys downstream
        m_Support.firePropertyChange("", m_OptimizationObjectivesWithWeights, m_OptimizationObjectivesWithWeights);
    }
}