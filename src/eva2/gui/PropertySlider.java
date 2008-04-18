package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
class PropertySlider extends JPanel {
  private PropertyEditor m_Editor;
  private JSlider m_Slider;
  /**
   *
   */
  PropertySlider(PropertyEditor pe) {
    //super(pe.getAsText());
    m_Editor = pe;
    //setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    //setBorder(new TitledBorder(getString("SliderDemo.plain")));
    m_Slider = new JSlider(-10, 90, 20);
    //s.getAccessibleContext().setAccessibleName(getString("SliderDemo.plain"));
    //s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.a_plain_slider"));
    m_Slider.addChangeListener(new SliderListener());
    m_Slider.setValue(((Integer)m_Editor.getValue()).intValue());
    m_Slider.setPaintTicks(true);
    m_Slider.setMajorTickSpacing(20);
    m_Slider.setMinorTickSpacing(5);
    m_Slider.setPaintLabels( true );
    this.add(m_Slider);
    m_Editor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
	updateUs();
      }
    });
    addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
	// if (e.getKeyCode() == KeyEvent.VK_ENTER)
	updateEditor();
      }
    });
    addFocusListener(new FocusAdapter() {
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
      m_Slider.setValue(((Integer)m_Editor.getValue()).intValue());
    } catch (IllegalArgumentException ex) {}
  }
  /**
   *
   */
  protected void updateEditor() {
    try {
      m_Editor.setValue(new Integer (m_Slider.getValue()));
    } catch (IllegalArgumentException ex) {}
  }
  /**
   *
   */
  class SliderListener implements ChangeListener {
    /**
     *
     */
    public SliderListener() {}
      public void stateChanged(ChangeEvent e) {
	    JSlider s1 = (JSlider)e.getSource();
	    System.out.println("slider"+s1.getValue());
            updateEditor();
	}
    }
}
