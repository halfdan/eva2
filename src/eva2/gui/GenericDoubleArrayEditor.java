package eva2.gui;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 13:51:14
 * To change this template use File | Settings | File Templates.
 */
public class GenericDoubleArrayEditor extends JPanel implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyDoubleArray             m_DoubleArray;

    /** The gaphix stuff */
    private JPanel                  m_CustomEditor, m_DataPanel, m_ButtonPanel;
    private JTextField[]            m_InputTextField;
    private JButton                 m_OKButton, m_AddButton, m_DeleteButton, m_NormalizeButton;

    public GenericDoubleArrayEditor() {
        // compiled code
    }

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.m_CustomEditor     = new JPanel();
        this.m_CustomEditor.setLayout(new BorderLayout());

        this.m_CustomEditor.add(new JLabel("Current Double Array:"), BorderLayout.NORTH);

        // init data panel
        this.m_DataPanel = new JPanel();
        this.updateDataPanel();
        this.m_CustomEditor.add(this.m_DataPanel, BorderLayout.CENTER);

        // init button panel
        this.m_ButtonPanel = new JPanel();
        this.m_AddButton = new JButton("Add");
        this.m_AddButton.addActionListener(this.addAction);
        this.m_DeleteButton = new JButton("Delete");
        this.m_DeleteButton.addActionListener(this.deleteAction);
        this.m_NormalizeButton = new JButton("Normalize");
        this.m_NormalizeButton.addActionListener(this.mormalizeAction);
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
        this.m_ButtonPanel.add(this.m_AddButton);
        this.m_ButtonPanel.add(this.m_DeleteButton);
        this.m_ButtonPanel.add(this.m_NormalizeButton);
        this.m_ButtonPanel.add(this.m_OKButton);
        this.m_CustomEditor.add(this.m_ButtonPanel, BorderLayout.SOUTH);
        this.updateEditor();
    }

    /** This action listener adds an element to DoubleArray
     */
    ActionListener addAction = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            double[] tmpD = m_DoubleArray.getDoubleArray();
            double[] newD = new double[tmpD.length+1];

            for (int i = 0; i < newD.length; i++) newD[i] = 1.0;
            for (int i = 0; i < tmpD.length; i++) newD[i] = tmpD[i];
            m_DoubleArray.setDoubleArray(newD);
            updateEditor();
        }
    };
    /** This action listener removes an element to DoubleArray
     */
    ActionListener deleteAction = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            double[] tmpD = m_DoubleArray.getDoubleArray();
            double[] newD = new double[tmpD.length-1];

            for (int i = 0; i < newD.length; i++) newD[i] = tmpD[i];
            m_DoubleArray.setDoubleArray(newD);
            updateEditor();
        }
    };
    /** This action listener nomalizes the values of the DoubleArray
     */
    ActionListener mormalizeAction = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            double[]    tmpD    = m_DoubleArray.getDoubleArray();
            double      sum     = 0;

            for (int i = 0; i < tmpD.length; i++) sum += tmpD[i];
            if (sum == 0) return;
            for (int i = 0; i < tmpD.length; i++) tmpD[i] = tmpD[i]/sum;
            m_DoubleArray.setDoubleArray(tmpD);
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
            double[]    tmpD    = new double[m_InputTextField.length];

            for (int i = 0; i < tmpD.length; i++) {

                try {
                    double d = 0;
                    d = new Double(m_InputTextField[i].getText()).doubleValue();
                    tmpD[i] = d;
                } catch (Exception e) {

                }
                //tmpD[i] = new Double(m_InputTextField[i].getText()).doubleValue();
            }

            m_DoubleArray.setDoubleArray(tmpD);
            //updateEditor();
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
        double[] tmpD = this.m_DoubleArray.getDoubleArray();

        this.m_DataPanel.removeAll();
        this.m_DataPanel.setLayout(new GridLayout(tmpD.length, 2));
        this.m_InputTextField = new JTextField[tmpD.length];
        for (int i = 0; i < tmpD.length; i++) {
            JLabel label = new JLabel("Value X"+i+": ");
            this.m_DataPanel.add(label);
            this.m_InputTextField[i]   = new JTextField();
            this.m_InputTextField[i].setText(""+tmpD[i]);
            this.m_InputTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.m_DataPanel.add(this.m_InputTextField[i]);
        }
    }


    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof PropertyDoubleArray) {
            this.m_DoubleArray = (PropertyDoubleArray) o;
            this.updateEditor();
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_DoubleArray;
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

    /**
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        m_Support.addPropertyChangeListener(l);
    }

    /**
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
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
        String rep = "Edit double[]";
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