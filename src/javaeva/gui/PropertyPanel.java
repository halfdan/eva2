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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import javaeva.tools.EVAHELP;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class PropertyPanel extends JPanel {
  private PropertyEditor m_PropertyEditor;
  private PropertyDialog m_PropertyDialog;
  /**
   *
   */
  public PropertyPanel(PropertyEditor Editor) {
    setBorder(BorderFactory.createEtchedBorder());
    setToolTipText("Click to edit properties for this object");
    setOpaque(true);
    m_PropertyEditor = Editor;
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
	if (m_PropertyEditor.getValue() != null) {
	  if (m_PropertyDialog == null) {
	    int x = getLocationOnScreen().x;
	    int y = getLocationOnScreen().y;
	    m_PropertyDialog = new PropertyDialog(m_PropertyEditor, EVAHELP.cutClassName(m_PropertyEditor.getClass().getName()) , x, y);
	  }
	  else {
		  m_PropertyDialog.updateFrameTitle(m_PropertyEditor);
	    m_PropertyDialog.setVisible(true);
	  }
	}
      }
    });
    Dimension newPref = getPreferredSize();
    newPref.height = getFontMetrics(getFont()).getHeight() * 6 / 4;  //6 / 4;
    newPref.width = newPref.height * 6; //5
    setPreferredSize(newPref);
  }
  /**
   *
   */
  public void removeNotify() {
    if (m_PropertyDialog != null) {
      //System.out.println("  m_PropertyDialog.dispose();");
      m_PropertyDialog.dispose();
      m_PropertyDialog = null;
    }
  }
  /**
   *
   */
  public void paintComponent(Graphics g) {
    Insets i = getInsets();
    Rectangle box = new Rectangle(i.left, i.top,
      getSize().width - i.left - i.right ,
      getSize().height - i.top - i.bottom);
    g.clearRect(i.left, i.top,
      getSize().width - i.right - i.left,
      getSize().height - i.bottom - i.top);
    m_PropertyEditor.paintValue(g, box);

//    Rectangle box = new Rectangle(i.left,i.top,
//					  this.getWidth() - i.right,
//					  this.getHeight() - i.bottom );
  }
}
