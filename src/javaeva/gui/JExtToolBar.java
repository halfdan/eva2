package javaeva.gui;
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
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.Insets;
import java.beans.*;
/**
 *
 */
public class JExtToolBar extends JToolBar{
  public JButton add(Action a){
    JButton button = super.add(a);
    button.setText(null);
    button.setMargin(new Insets(0, 0, 0, 0));

    Object o;
    o = a.getValue(ExtAction.TOOLTIP);
    String toolTip = o != null ? (String)o : "";

    o = a.getValue(ExtAction.KEYSTROKE);
    button.setToolTipText(toolTip + getKeyText((KeyStroke)o));

    return button;
  }

  private String getKeyText(KeyStroke k){
    StringBuffer result = new StringBuffer();

    if(k != null){
      int modifiers = k.getModifiers();
      if(modifiers > 0) result.append(KeyEvent.getKeyModifiersText(modifiers) + "+");
      result.append(KeyEvent.getKeyText(k.getKeyCode()));
    }
    if(result.length() > 0){
      result.insert(0, " [");
      result.append("]");
    }

    return result.toString();
  }

  protected PropertyChangeListener createActionChangeListener(JButton b){
    return new ExtActionChangedListener(b){
      public void propertyChange(PropertyChangeEvent e){
        JButton button = (JButton)component;

        String propertyName = e.getPropertyName();
        if(propertyName.equals(Action.NAME)){
          /* Nichts tun! */
        }
        else if(propertyName.equals("enabled")){
          button.setEnabled(((Boolean)e.getNewValue()).booleanValue());
          button.repaint();
        }
        else if(e.getPropertyName().equals(Action.SMALL_ICON)){
          button.setIcon((Icon)e.getNewValue());
          button.invalidate();
          button.repaint();
        }
        else if(propertyName.equals(ExtAction.TOOLTIP) || propertyName.equals(ExtAction.KEYSTROKE)){
          Action source = (Action)e.getSource();

          Object o = source.getValue(ExtAction.TOOLTIP);
          String toolTip = o != null ? (String)o : "";
          o = source.getValue(ExtAction.KEYSTROKE);
          button.setToolTipText(toolTip + getKeyText((KeyStroke)o));
        }
      }
    };
  }
}
