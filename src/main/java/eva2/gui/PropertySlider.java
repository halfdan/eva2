package eva2.gui;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

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
        slider.setValue((Integer) propertyEditor.getValue());
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
            //String x = editor.getAsText();
            slider.setValue((Integer) propertyEditor.getValue());
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     *
     */
    protected void updateEditor() {
        try {
            propertyEditor.setValue(slider.getValue());
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
