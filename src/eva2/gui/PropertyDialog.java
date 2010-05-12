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
import java.beans.PropertyEditor;

import eva2.EvAInfo;
import eva2.tools.BasicResourceLoader;
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
    byte[] bytes   = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
    setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
      
    //System.out.println("PropertyDialog.Constructor  of "+ Title);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());
    m_Editor = editor;
    m_EditorComponent = editor.getCustomEditor();
    getContentPane().add(m_EditorComponent, BorderLayout.CENTER);
    pack();
    setLocation(x, y);
    setVisible(true);
  }
  
  protected static String getFrameNameFromEditor(PropertyEditor editor) {
	  if (editor.getValue().getClass().isArray()) {
		  return "Array of " + EVAHELP.cutClassName(editor.getValue().getClass().getComponentType().getName());
	  } else return EVAHELP.cutClassName(editor.getValue().getClass().getName());
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

