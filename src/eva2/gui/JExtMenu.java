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
import java.beans.*;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
/**
 *
 */
public class JExtMenu extends JMenu{
  public final static String ACTION = "Action";
  /**
   *
   */
  public JExtMenu(){
    //super();
    //Mnemonic m = new Mnemonic(s);
    //setText(m.getText());
    //setMnemonic(m.getMnemonic());
  }
  /**
   *
   */
  public JExtMenu(String s){
    super();
    Mnemonic m = new Mnemonic(s);
    setText(m.getText());
    setMnemonic(m.getMnemonic());
  }
  /**
   *
   */
    @Override
  public JMenuItem add(Action a){
    JMenuItem item = super.add(a);
    Object o;
    o = a.getValue(ExtAction.MNEMONIC);
    if(o != null) item.setMnemonic(((Character)o).charValue());
    o = a.getValue(ExtAction.TOOLTIP);
    if(o != null) item.setToolTipText((String)o);
    o = a.getValue(ExtAction.KEYSTROKE);
    if(o != null) item.setAccelerator((KeyStroke)o);
    return item;
  }
  /**
   *
   */
    @Override
  protected PropertyChangeListener createActionChangeListener(JMenuItem b){
    return new ExtActionChangedListener(b){
            @Override
      public void propertyChange(PropertyChangeEvent e) {
        JMenuItem menuItem = (JMenuItem)component;
        if(menuItem == null) return;
        String propertyName = e.getPropertyName();
        if(propertyName.equals(Action.NAME)) menuItem.setText((String)e.getNewValue());
        else if(propertyName.equals("enabled")) menuItem.setEnabled(((Boolean)e.getNewValue()).booleanValue());
        else if(propertyName.equals(Action.SMALL_ICON)){
          Icon icon = (Icon)e.getNewValue();
          menuItem.setIcon(icon);
          menuItem.invalidate();
          menuItem.repaint();
        }
        else if(propertyName.equals(ExtAction.MNEMONIC)) menuItem.setMnemonic(((Character)e.getNewValue()).charValue());
        else if(propertyName.equals(ExtAction.TOOLTIP)) menuItem.setToolTipText((String)e.getNewValue());
        else if(propertyName.equals(ExtAction.KEYSTROKE)) menuItem.setAccelerator((KeyStroke)e.getNewValue());
      }
    };
  }
}
