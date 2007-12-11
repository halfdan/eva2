package javaeva.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 14 $
 *            $Date: 2006-12-18 16:32:23 +0100 (Mon, 18 Dec 2006) $
 *            $Author: marcekro $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import javaeva.tools.EVAHELP;
import java.awt.Component;
import java.awt.Toolkit;
import java.beans.PropertyEditor;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import wsi.ra.tool.BasicResourceLoader;
import javaeva.client.EvAClient;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class PropertyDialog extends JEFrame {
  private PropertyEditor m_Editor;
  private Component m_EditorComponent;
  /**
   *
   */
  public PropertyDialog (PropertyEditor Editor,String Title, int x, int y) {
    super(EVAHELP.cutClassName (Editor.getValue().getClass().getName())); // that was the long class name !!
    BasicResourceLoader  loader  = BasicResourceLoader.instance();
    byte[] bytes   = loader.getBytesFromResourceLocation(EvAClient.iconLocation);
    try {
        setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
    } catch (java.lang.NullPointerException e) {
        System.out.println("Could not find JavaEvA icon, please move rescoure folder to working directory!");
    }
    //System.out.println("PropertyDialog.Constructor  of "+ Title);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	e.getWindow().dispose();
      }
    });
    getContentPane().setLayout(new BorderLayout());
    m_Editor = Editor;
    m_EditorComponent = Editor.getCustomEditor();
    getContentPane().add(m_EditorComponent, BorderLayout.CENTER);
    pack();
    setLocation(x, y);
    setVisible(true);
  }
  /**
   *
   */
  public PropertyEditor getEditor() {
    return m_Editor;
  }
}

