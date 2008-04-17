package eva2.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 191 $
 *            $Date: 2007-10-23 12:56:51 +0200 (Tue, 23 Oct 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.util.Date;
import java.text.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import eva2.client.EvAClient;

import java.awt.event.*;
import wsi.ra.tool.BasicResourceLoader;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class LogPanel extends JPanel {
//  protected JLabel m_Message = new JLabel("OK");
  protected JTextArea m_LogText = new JTextArea(10,20);
  protected boolean m_first = true;
  /**
   *
   */
  public LogPanel() {
    m_LogText.setEditable(false);
    m_LogText.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
//    m_Message.setBorder(BorderFactory.createCompoundBorder(
//			  BorderFactory.createTitledBorder("Message"),
//			  BorderFactory.createEmptyBorder(0,4,4,4)));
    JPanel panel_1 = new JPanel();
    panel_1.setBorder(BorderFactory.createTitledBorder("Info"));
    panel_1.setLayout(new BorderLayout());
    final JScrollPane scrollpane = new JScrollPane(m_LogText);
    panel_1.add(scrollpane, BorderLayout.CENTER);
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
    setLayout(new BorderLayout());
    add(panel_1, BorderLayout.CENTER);
    JPanel panel_2 = new JPanel();
    panel_2.setLayout(new BorderLayout());
//    panel_2.add(m_Message,BorderLayout.CENTER);
    add(panel_2, BorderLayout.SOUTH);
  }
  /**
   *
   */
  protected static String getTimestamp() {
    return (new SimpleDateFormat("HH:mm:ss:")).format(new Date());
  }
  /**
   *
   */
  public void logMessage(String message) {
    if (m_first)
      m_first = false;
    m_LogText.append("\n");
    m_LogText.append(LogPanel.getTimestamp() + ' ' + message);
  }
//  /**
//   *
//   */
//  public void statusMessage(String message) {
//    m_Message.setText(message);
//  }
  /**
   *
   */
  public static void main(String [] args) {
    try {
      final JFrame frame = new JFrame("Log_Panel_Test");
      frame.getContentPane().setLayout(new BorderLayout());
      BasicResourceLoader  loader  = BasicResourceLoader.instance();
      byte[] bytes   = loader.getBytesFromResourceLocation(EvAClient.iconLocation);
      try {
          frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
      } catch (java.lang.NullPointerException e) {
        System.out.println("Could not find EvA2 icon, please move rescoure folder to working directory!");
      }
      LogPanel panel = new LogPanel();
      frame.getContentPane().add(panel, BorderLayout.CENTER);
      frame.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  frame.dispose();
	  System.exit(0);
	}
      });
      frame.pack();
      frame.setVisible(true);
      panel.logMessage("HI!");
//      panel.statusMessage("Hi JavaEvA");
      panel.logMessage("Test");

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
    }
  }
}
