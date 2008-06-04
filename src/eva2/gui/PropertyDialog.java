package eva2.gui;
/*
 * Title:        EvA2
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditor;

import wsi.ra.tool.BasicResourceLoader;
import eva2.EvAInfo;
import eva2.tools.EVAHELP;
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
  public PropertyDialog (PropertyEditor editor,String Title, int x, int y) {
    super(getFrameNameFromEditor(editor)); // that was the long class name !!
    BasicResourceLoader  loader  = BasicResourceLoader.instance();
    byte[] bytes   = loader.getBytesFromResourceLocation(EvAInfo.iconLocation);
    try {
        setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
    } catch (java.lang.NullPointerException e) {
        System.out.println("Could not find EvA2 icon, please move resources folder to working directory!");
    }
    //System.out.println("PropertyDialog.Constructor  of "+ Title);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	e.getWindow().dispose();
      }
    });
    getContentPane().setLayout(new BorderLayout());
    m_Editor = editor;
    m_EditorComponent = editor.getCustomEditor();
    getContentPane().add(m_EditorComponent, BorderLayout.CENTER);
    pack();
    setLocation(x, y);
    setVisible(true);
  }
  
  protected static String getFrameNameFromEditor(PropertyEditor editor) {
	  return EVAHELP.cutClassName(editor.getValue().getClass().getName());
  }
  
  /**
   * Update the name of the dialogue from an editor instance.
   * 
   * @param editor
   */
  public void updateFrameTitle(PropertyEditor editor) {
	  setTitle(getFrameNameFromEditor(editor));
  }
  
  /**
   *
   */
  public PropertyEditor getEditor() {
    return m_Editor;
  }
}

