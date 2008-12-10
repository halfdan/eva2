package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 57 $
 *            $Date: 2007-05-04 14:22:16 +0200 (Fri, 04 May 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class PropertyText extends JTextField {
  private PropertyEditor m_Editor;
  /**
   *
   */
  public PropertyText(PropertyEditor pe) {
    super(pe.getAsText());
    m_Editor = pe;
//    m_Editor.addPropertyChangeListener(new PropertyChangeListener() {
//      public void propertyChange(PropertyChangeEvent evt) {
//	updateUs();
//      }
//    });
    addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        //if (e.getKeyCode() == KeyEvent.VK_ENTER)
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
  public void updateUs() {
    try {
      String x = m_Editor.getAsText();
      setText(x);
    } catch (IllegalArgumentException ex) {}
  }
  /**
   *
   */
  protected void updateEditor() {
    try {
      String x = getText();
      if (!m_Editor.getAsText().equals(x)) m_Editor.setAsText(x);
    } catch (IllegalArgumentException ex) {}
  }
}
