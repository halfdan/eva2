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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
/**
 *
 */
public abstract class ExtAction extends AbstractAction {
 public final static String CAPTION = "Caption";
  public final static String MNEMONIC = "Mnemonic";
  public final static String TOOLTIP = "ToolTip";
  public final static String KEYSTROKE = "KeyStroke";
  /**
   *
   */
  private void setValues(String s, String toolTip){
    Mnemonic m = new Mnemonic(s);
    putValue(MNEMONIC, new Character(m.getMnemonic()));
    putValue(Action.NAME, m.getText());
    putValue(TOOLTIP, toolTip);
  }
  /**
   *
   */
  public ExtAction(String s, Icon i, String toolTip, KeyStroke key){
    this(s, i, toolTip);
    if (i==null)
      System.out.println("Icon == null");
    putValue(KEYSTROKE, key);
  }
  /**
   *
   */
  public ExtAction(String s, Icon i, String toolTip){
    super(null, i);
    if (i==null)
      System.out.println("Icon == null");
    setValues(s, toolTip);
  }
  /**
   *
   */
  public ExtAction(String s, String toolTip, KeyStroke key){
    this(s, toolTip);
    putValue(KEYSTROKE, key);
  }
  /**
   *
   */
  public ExtAction(String s, String toolTip){
    super();
    setValues(s, toolTip);
  }
}

