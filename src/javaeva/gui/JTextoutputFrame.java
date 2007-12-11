package javaeva.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 255 $
 *            $Date: 2007-11-15 14:58:12 +0100 (Thu, 15 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.Serializable;
import wsi.ra.tool.BasicResourceLoader;
import javaeva.client.EvAClient;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class JTextoutputFrame implements JTextoutputFrameInterface,
                                         Serializable {
  public static boolean TRACE = false;
  protected String m_Name ="undefined";
  private transient JTextArea m_TextArea;
  private boolean m_firstprint = true;
  /**
   *
   */
  public JTextoutputFrame(String Title) {
    if (TRACE) System.out.println("JTextoutputFrame Constructor");
    m_Name = Title;
  }
  /**
   *
   */
  public void print (String Text) {
    //System.out.println("Print:"+Text);
    if (m_firstprint==true) {
      m_firstprint = false;
      createFrame();
    }
    m_TextArea.append (Text+"\n");
    m_TextArea.repaint();
  }
  /**
   *
   */
  private void createFrame() {
    if (TRACE) System.out.println("JTextoutputFrame createFrame");
    m_TextArea = new JTextArea(10,80);
    m_TextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    m_TextArea.setLineWrap(true);
    m_TextArea.setWrapStyleWord(true);
    m_TextArea.setEditable(false);
    m_TextArea.setCaretPosition(0);
    final JFrame frame = new JEFrame(m_Name);
    BasicResourceLoader  loader  = BasicResourceLoader.instance();
    byte[] bytes   = loader.getBytesFromResourceLocation(EvAClient.iconLocation);
    try {
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
      } catch (java.lang.NullPointerException e) {
        System.out.println("Could not find JavaEvA icon, please move rescoure folder to working directory!");
      } 
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.dispose();
      }
    });
    frame.getContentPane().setLayout(new BorderLayout());
    //frame.getContentPane().add(new JScrollPane(m_TextArea), BorderLayout.CENTER);
    final JScrollPane scrollpane = new JScrollPane(m_TextArea);
    frame.getContentPane().add(scrollpane, BorderLayout.CENTER);
    scrollpane.getViewport().addChangeListener(new ChangeListener() {
      private int lastHeight;
      //
      public void stateChanged(ChangeEvent e) {
	JViewport viewport = (JViewport)e.getSource();
	int Height = viewport.getViewSize().height;
	if (Height != lastHeight) {
	  lastHeight = Height;
	  int x = Height - viewport.getExtentSize().height;
	  viewport.setViewPosition(new Point(0, x));
	}
      }
    });
    frame.pack();
    frame.setSize(800, 400);
    frame.setVisible(true);
    frame.setState(Frame.ICONIFIED);
  }
  /**
   *
   */
  public static void main( String[] args ){
    JTextoutputFrame test = new JTextoutputFrame("hi");
    while(true)
    test.print("Test 12345");
   }
}
