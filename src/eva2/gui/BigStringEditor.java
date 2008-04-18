package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 194 $
 *            $Date: 2007-10-23 13:43:24 +0200 (Tue, 23 Oct 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import  java.beans.*;
import  java.awt.*;
import  java.awt.event.*;
import  javax.swing.*;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
public class BigStringEditor implements PropertyEditor {
  private PropertyChangeSupport m_Support = new PropertyChangeSupport(this);
  private PropertyEditor m_ElementEditor;
  private JTextArea m_TextArea;
  private JScrollPane m_ScrollPane;
  private JPanel m_Panel;
//  private Source m_Source;
  private JButton m_SetButton;
  static private boolean m_finished = false;
  /**
   *
   */
  public static void editSource (String file) {

    try {
      m_finished=false;
      BigStringEditor editor = new BigStringEditor();

      PropertyDialog frame = new PropertyDialog(editor,file, 50, 50);
      //frame.setSize(200, 200);
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing (WindowEvent e) {
          m_finished=true;
        }
      });
      while (m_finished==false) {
        try {Thread.sleep(1000);}
        catch (Exception e) {
          System.out.println("e+"+e.getMessage());
        }
      }


    } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
    }
  }
  /**
   *
   */
  public BigStringEditor () {
    super();
//    m_TextArea = new JEditTextArea();
//    m_TextArea.setTokenMarker(new JavaTokenMarker());
    m_TextArea = new JTextArea(60,60);
    m_TextArea.setEditable(true);
    m_TextArea.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
    m_ScrollPane = new JScrollPane(m_TextArea);
    m_Panel = new JPanel();
    m_Panel.setBorder(BorderFactory.createTitledBorder("Sourcecode"));
    m_Panel.setLayout(new BorderLayout());
    m_SetButton = new JButton("SET");
    m_SetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setValue(m_TextArea.getText());
      }
    });
    m_Panel.add(m_ScrollPane, BorderLayout.CENTER);
    m_Panel.add(m_SetButton, BorderLayout.SOUTH);
  }
  /**
   *
   */
  public void setValue (Object value) {
    m_ElementEditor = null;
    if (value instanceof String) {
//      m_Source.setString((String)value);
      m_TextArea.setText((String)value);

    }
/*    if (value instanceof Source) {
       // m_Source = (Source) value;
       m_TextArea.setText(((Source)value).getString());
    }*/
    m_Support.firePropertyChange("", null, null);
  }
  /**
   *
   */
  public Object getValue () {
    // m_Source.setString(m_TextArea.getText());
    return null;
  }
  /**
  *
  */
  public String getJavaInitializationString () {
      return  "null";
  }

  /**
   * Returns true to indicate that we can paint a representation of the
   * string array
   *
   * @return true
   */
  public boolean isPaintable () {
      return  true;
  }

  /**
   * Paints a representation of the current classifier.
   *
   * @param gfx the graphics context to use
   * @param box the area we are allowed to paint into
   */
  public void paintValue (Graphics gfx, Rectangle box) {
    FontMetrics fm = gfx.getFontMetrics();
    int vpad = (box.height - fm.getAscent())/2;
    //String rep = EVAHELP.cutClassName(m_ElementClass.getName());
    gfx.drawString("BigStringEditor", 2, fm.getHeight() + vpad - 3);
  }

  /**
   *
   */
  public String getAsText () {
    return  null;
  }

  /**
   *
   */
  public void setAsText (String text) throws IllegalArgumentException {
    throw  new IllegalArgumentException(text);
  }

  /**
   *
   */
  public String[] getTags () {
    return  null;
  }

  /**
   *
   */
  public boolean supportsCustomEditor () {
    return  true;
  }
  /**
   *
   */
  public Component getCustomEditor () {
    return  m_Panel;
  }

  /**
   *
   */
  public void addPropertyChangeListener (PropertyChangeListener l) {
    m_Support.addPropertyChangeListener(l);
  }
  /**
   *
   */
  public void removePropertyChangeListener (PropertyChangeListener l) {
    m_Support.removePropertyChangeListener(l);
  }

  /**
   *
   */
  public static void main (String[] args) {
    try {
      BigStringEditor editor = new BigStringEditor();

      PropertyDialog frame = new PropertyDialog(editor, "test", 50, 50);
      frame.setSize(200, 200);
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing (WindowEvent e) {
          System.exit(0);
        }
      });
//       editor.setValue(so);
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
    }
  }
}



