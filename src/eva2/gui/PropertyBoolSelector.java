package eva2.gui;
/*
 * Title:        JavaEvA
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
import sun.beans.editors.BoolEditor;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class PropertyBoolSelector extends JCheckBox {
  private BoolEditor m_Editor;
  /**
   *
   */
  public PropertyBoolSelector(PropertyEditor pe) {
    super();
    m_Editor = (BoolEditor) pe;
    if (m_Editor.getAsText().equals("True"))
      setSelected(true);
    else
      setSelected(false);

    addItemListener(new ItemListener () {
        public void itemStateChanged (ItemEvent evt) {
          if (evt.getStateChange() == evt.SELECTED) {
            m_Editor.setValue(Boolean.TRUE);
          }
          if (evt.getStateChange() == evt.DESELECTED) {
            m_Editor.setValue(Boolean.FALSE);
          }
        }
    });
  }
}
