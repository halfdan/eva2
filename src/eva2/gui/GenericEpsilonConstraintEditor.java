package eva2.gui;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.07.2005
 * Time: 16:33:45
 * To change this template use File | Settings | File Templates.
 */
public class GenericEpsilonConstraintEditor extends JPanel implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyEpsilonConstraint             m_EpsilonConstraint;

    /** The gaphix stuff */
    private JPanel                  m_CustomEditor, m_DataPanel, m_ButtonPanel, m_TargetPanel;
    private JTextField[]            m_TargetTextField;
    private JComboBox               m_Objective;
    private JButton                 m_OKButton;

    public GenericEpsilonConstraintEditor() {
        // compiled code
    }

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.m_CustomEditor     = new JPanel();
        this.m_CustomEditor.setLayout(new BorderLayout());

        // target panel
        this.m_TargetPanel = new JPanel();
        this.m_TargetPanel.setLayout(new GridLayout(1, 2));
        this.m_TargetPanel.add(new JLabel("Optimize:"));
        this.m_Objective = new JComboBox();
        for (int i = 0; i < this.m_EpsilonConstraint.m_TargetValue.length; i++) this.m_Objective.addItem("Objective "+i);
        this.m_TargetPanel.add(this.m_Objective);
        this.m_Objective.addItemListener(this.objectiveAction);
        this.m_CustomEditor.add(this.m_TargetPanel, BorderLayout.NORTH);

        // init data panel
        this.m_DataPanel = new JPanel();
        this.updateDataPanel();
        this.m_CustomEditor.add(this.m_DataPanel, BorderLayout.CENTER);

        // init button panel
        this.m_ButtonPanel = new JPanel();
        this.m_OKButton         = new JButton("OK");
        this.m_OKButton.setEnabled(true);
        this.m_OKButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            //m_Backup = copyObject(m_Object);
	            if ((m_CustomEditor.getTopLevelAncestor() != null) && (m_CustomEditor.getTopLevelAncestor() instanceof Window)) {
	                Window w = (Window) m_CustomEditor.getTopLevelAncestor();
	                w.dispose();
	            }
	        }
        });
        this.m_ButtonPanel.add(this.m_OKButton);
        this.m_CustomEditor.add(this.m_ButtonPanel, BorderLayout.SOUTH);
        this.updateEditor();
    }

    /** This action listener adds an element to DoubleArray
     */
    ItemListener objectiveAction = new ItemListener() {
        public void itemStateChanged(ItemEvent event) {
            m_EpsilonConstraint.m_OptimizeObjective =  m_Objective.getSelectedIndex();
            updateEditor();
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
            double[] tmpT   = m_EpsilonConstraint.m_TargetValue;

            for (int i = 0; i < tmpT.length; i++) {

                try {
                    double d = 0;
                    d = new Double(m_TargetTextField[i].getText()).doubleValue();
                    tmpT[i] = d;
                } catch (Exception e) {

                }
            }

            m_EpsilonConstraint.m_TargetValue   = tmpT;
         }
    };

    /** The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.m_CustomEditor != null) {
            this.updateDataPanel();
            this.m_CustomEditor.validate();
            this.m_CustomEditor.repaint();
        }
    }

    /** This method updates the data panel
     */
    private void updateDataPanel() {
        double[] tmpT   = this.m_EpsilonConstraint.m_TargetValue;
        int      obj    = this.m_EpsilonConstraint.m_OptimizeObjective;

        this.m_DataPanel.removeAll();
        this.m_DataPanel.setLayout(new GridLayout(tmpT.length+1, 2));
        this.m_DataPanel.add(new JLabel());
        this.m_DataPanel.add(new JLabel("Target Value"));
        this.m_TargetTextField = new JTextField[tmpT.length];
        for (int i = 0; i < tmpT.length; i++) {
            JLabel label = new JLabel("Objective "+i+": ");
            this.m_DataPanel.add(label);
            this.m_TargetTextField[i]   = new JTextField();
            this.m_TargetTextField[i].setText(""+tmpT[i]);
            this.m_TargetTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.m_DataPanel.add(this.m_TargetTextField[i]);
        }
        this.m_TargetTextField[obj].setEditable(false);
    }


    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof PropertyEpsilonConstraint) {
            this.m_EpsilonConstraint = (PropertyEpsilonConstraint) o;
            this.updateEditor();
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_EpsilonConstraint;
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

    public void addPropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.removePropertyChangeListener(l);
    }

    /** This is used to hook an action listener to the ok button
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        m_OKButton.addActionListener(a);
    }

    /** This is used to remove an action listener from the ok button
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
        m_OKButton.removeActionListener(a);
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
        String rep = "Edit Epsilon Constraint";
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
        if (this.m_CustomEditor == null) this.initCustomEditor();
        return m_CustomEditor;
    }
}