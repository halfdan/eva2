package eva2.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 15:17:09
 * To change this template use File | Settings | File Templates.
 */
public class GenericEpsilonThresholdEditor extends JPanel implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyEpsilonThreshold             m_EpsilonThreshhold;

    /** The gaphix stuff */
    private JPanel                  m_CustomEditor, m_DataPanel, m_ButtonPanel, m_TargetPanel;
    private JTextField[]            m_TargetTextField, m_PunishTextField;
    private JComboBox               m_Objective;
    private JButton                 m_OKButton;

    public GenericEpsilonThresholdEditor() {
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
        for (int i = 0; i < this.m_EpsilonThreshhold.m_TargetValue.length; i++) {
            this.m_Objective.addItem("Objective "+i);
        }
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
            @Override
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
        @Override
        public void itemStateChanged(ItemEvent event) {
            m_EpsilonThreshhold.m_OptimizeObjective =  m_Objective.getSelectedIndex();
            updateEditor();
        }
    };

    /** This action listener reads all values
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
            double[] tmpT   = m_EpsilonThreshhold.m_TargetValue;
            double[] tmpP   = m_EpsilonThreshhold.m_Punishment;

            for (int i = 0; i < tmpT.length; i++) {

                try {
                    double d = 0;
                    d = new Double(m_TargetTextField[i].getText()).doubleValue();
                    tmpT[i] = d;
                } catch (Exception e) {

                }
                try {
                    double d = 0;
                    d = new Double(m_PunishTextField[i].getText()).doubleValue();
                    tmpP[i] = d;
                } catch (Exception e) {

                }
            }

            m_EpsilonThreshhold.m_TargetValue   = tmpT;
            m_EpsilonThreshhold.m_Punishment    = tmpP;
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
        double[] tmpT   = this.m_EpsilonThreshhold.m_TargetValue;
        double[] tmpP   = this.m_EpsilonThreshhold.m_Punishment;
        int      obj    = this.m_EpsilonThreshhold.m_OptimizeObjective;

        this.m_DataPanel.removeAll();
        this.m_DataPanel.setLayout(new GridLayout(tmpT.length+1, 3));
        this.m_DataPanel.add(new JLabel());
        this.m_DataPanel.add(new JLabel("Target Value"));
        this.m_DataPanel.add(new JLabel("Punishment"));
        this.m_TargetTextField = new JTextField[tmpT.length];
        this.m_PunishTextField = new JTextField[tmpT.length];
        for (int i = 0; i < tmpT.length; i++) {
            JLabel label = new JLabel("Objective "+i+": ");
            this.m_DataPanel.add(label);
            this.m_TargetTextField[i]   = new JTextField();
            this.m_TargetTextField[i].setText(""+tmpT[i]);
            this.m_TargetTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.m_DataPanel.add(this.m_TargetTextField[i]);
            this.m_PunishTextField[i]   = new JTextField();
            this.m_PunishTextField[i].setText(""+tmpP[i]);
            this.m_PunishTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.m_DataPanel.add(this.m_PunishTextField[i]);
        }
        this.m_TargetTextField[obj].setEditable(false);
        this.m_PunishTextField[obj].setEditable(false);
    }


    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyEpsilonThreshold) {
            this.m_EpsilonThreshhold = (PropertyEpsilonThreshold) o;
            this.updateEditor();
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    @Override
    public Object getValue() {
        return this.m_EpsilonThreshhold;
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
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.addPropertyChangeListener(l);
    }

    @Override
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
    @Override
    public boolean isPaintable() {
        return true;
    }

    /** Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = "Edit Epsilon Threshhold";
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3  );
    }

    /** Returns true because we do support a custom editor.
    * @return true
    */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Returns the array editing component.
    * @return a value of type 'java.awt.Component'
    */
    @Override
    public Component getCustomEditor() {
        if (this.m_CustomEditor == null) this.initCustomEditor();
        return m_CustomEditor;
    }
}