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
 * Date: 15.07.2005
 * Time: 10:32:43
 * To change this template use File | Settings | File Templates.
 */
public class GenericWeigthedLPTchebycheffEditor extends JPanel implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyWeightedLPTchebycheff  m_WLPT;

    /** The gaphix stuff */
    private JPanel                  m_CustomEditor, m_DataPanel, m_ButtonPanel, m_TargetPanel;
    private JTextField[]            m_IdealTextField, m_WeightTextField;
    private JTextField              m_PValue;
    private JButton                 m_OKButton;

    public GenericWeigthedLPTchebycheffEditor() {
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
        this.m_TargetPanel.add(new JLabel("Choose P:"));
        this.m_PValue = new JTextField(""+this.m_WLPT.m_P);
        this.m_TargetPanel.add(this.m_PValue);
        this.m_PValue.addKeyListener(this.readDoubleAction);
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

    /** This action listener reads all values
     */
    KeyListener readDoubleAction = new KeyListener() {
        public void keyPressed(KeyEvent event) {
        }
        public void keyTyped(KeyEvent event) {
        }

        public void keyReleased(KeyEvent event) {
             try {
                int d = new Integer(m_PValue.getText()).intValue();
                m_WLPT.m_P = d;
             } catch (Exception e) {

             }
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
            double[] tmpT   = m_WLPT.m_IdealValue;
            double[] tmpP   = m_WLPT.m_Weights;

            for (int i = 0; i < tmpT.length; i++) {

                try {
                    double d = 0;
                    d = new Double(m_IdealTextField[i].getText()).doubleValue();
                    tmpT[i] = d;
                } catch (Exception e) {

                }
                try {
                    double d = 0;
                    d = new Double(m_WeightTextField[i].getText()).doubleValue();
                    tmpP[i] = d;
                } catch (Exception e) {

                }
            }

            m_WLPT.m_IdealValue     = tmpT;
            m_WLPT.m_Weights        = tmpP;
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
        double[] tmpT   = this.m_WLPT.m_IdealValue;
        double[] tmpP   = this.m_WLPT.m_Weights;
        int      obj    = this.m_WLPT.m_P;

        this.m_PValue.setText(""+obj);
        this.m_DataPanel.removeAll();
        this.m_DataPanel.setLayout(new GridLayout(tmpT.length+1, 3));
        this.m_DataPanel.add(new JLabel());
        this.m_DataPanel.add(new JLabel("Ideal Value"));
        this.m_DataPanel.add(new JLabel("Weights"));
        this.m_IdealTextField = new JTextField[tmpT.length];
        this.m_WeightTextField = new JTextField[tmpT.length];
        for (int i = 0; i < tmpT.length; i++) {
            JLabel label = new JLabel("Objective "+i+": ");
            this.m_DataPanel.add(label);
            this.m_IdealTextField[i]   = new JTextField();
            this.m_IdealTextField[i].setText(""+tmpT[i]);
            this.m_IdealTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.m_DataPanel.add(this.m_IdealTextField[i]);
            this.m_WeightTextField[i]   = new JTextField();
            this.m_WeightTextField[i].setText(""+tmpP[i]);
            this.m_WeightTextField[i].addKeyListener(this.readDoubleArrayAction);
            this.m_DataPanel.add(this.m_WeightTextField[i]);
        }
    }


    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof PropertyWeightedLPTchebycheff) {
            this.m_WLPT = (PropertyWeightedLPTchebycheff) o;
            this.updateEditor();
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_WLPT;
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
        String rep = "Edit the ideal vector, p and ev. the weights.";
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