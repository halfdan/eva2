package eva2.gui;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of Tuebingen, Computer
 * Architecture @author Holger Ulmer, Felix Streichert, Hannes Planatscher @version: $Revision: 10 $
 * $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $ $Author: streiche $
 */
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 */
class PropertySlider extends JPanel {

    private PropertyEditor propertyEditor;
    private JSlider slider;

    /**
     *
     */
    PropertySlider(PropertyEditor pe) {
        //super(pe.getAsText());
        propertyEditor = pe;
        //setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        //setBorder(new TitledBorder(getString("SliderDemo.plain")));
        slider = new JSlider(-10, 90, 20);
        //s.getAccessibleContext().setAccessibleName(getString("SliderDemo.plain"));
        //s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.a_plain_slider"));
        slider.addChangeListener(new SliderListener());
        slider.setValue(((Integer) propertyEditor.getValue()).intValue());
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.setPaintLabels(true);
        this.add(slider);
        propertyEditor.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateUs();
            }
        });
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                // if (e.getKeyCode() == KeyEvent.VK_ENTER)
                updateEditor();
            }
        });
        addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                updateEditor();
            }
        });
    }

    /**
     *
     */
    protected void updateUs() {
        try {
            //String x = m_Editor.getAsText();
            slider.setValue(((Integer) propertyEditor.getValue()).intValue());
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     *
     */
    protected void updateEditor() {
        try {
            propertyEditor.setValue(new Integer(slider.getValue()));
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     *
     */
    class SliderListener implements ChangeListener {

        /**
         *
         */
        public SliderListener() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider s1 = (JSlider) e.getSource();
            System.out.println("slider" + s1.getValue());
            updateEditor();
        }
    }
}
