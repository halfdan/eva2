package eva2.gui;
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

import javax.swing.JFrame;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.*;
import java.awt.*;

/**
 * Title:        The JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:
 * @author
 * @version 1.0
 */

public class JEFrame extends JFrame {

  public JEFrame() {
    super();
    register();
  }

  public JEFrame(String name) {
    super(name);
    register();
  }

  private void register() {
    JEFrameRegister.register(this);

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        JEFrameRegister.unregister((JEFrame) e.getWindow());
      }
      public void windowClosed(WindowEvent e) {
       JEFrameRegister.unregister((JEFrame) e.getWindow());
      }
    });
    this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_F,Event.CTRL_MASK),
      "ctrlFpressed"
    );
    this.getRootPane().getActionMap().put(
      "ctrlFpressed",
      new AbstractAction("ctrlFpressed") {
        public void actionPerformed( ActionEvent actionEvent ) {
          ((JEFrame) JEFrameRegister.getFrameList()[0]).setExtendedState(JEFrame.NORMAL);
          ((JEFrame) JEFrameRegister.getFrameList()[0]).toFront();
       }
      }
    );
    this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_O,Event.CTRL_MASK),
      "ctrlOpressed"
    );
    this.getRootPane().getActionMap().put(
      "ctrlOpressed",
      new AbstractAction("ctrlOpressed") {
        public void actionPerformed( ActionEvent actionEvent ) {
          Object[] fl = JEFrameRegister.getFrameList();
          for (int i = 0; i < fl.length; i++) {
                      ((JEFrame) JEFrameRegister.getFrameList()[i]).setExtendedState(JEFrame.NORMAL);
                      ((JEFrame) JEFrameRegister.getFrameList()[i]).toFront();
          }

       }
      }
    );
    this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_LESS , Event.CTRL_MASK),
      "ctrlSmallerpressed"
    );
    final JEFrame self = this;
    this.getRootPane().getActionMap().put(
      "ctrlSmallerpressed",
      new AbstractAction("ctrlSmallerpressed") {
        public void actionPerformed( ActionEvent actionEvent ) {
          JEFrameRegister.setFocusToNext(self);
       }
      }
    );


    }



}
