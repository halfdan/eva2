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

import javax.swing.JFrame;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;

public class JEFrame extends JFrame {
	private boolean closeAllOnClose = false;
	
	public JEFrame() {
		super();
		init();
	}

	public JEFrame(String name) {
		super(name);
		init();
	}
	
	/**
	 * Set to true if all registered JEFrames should be closed if this frame is closed.
	 *
	 * @param c
	 */
	public void setCloseAllOnClosed(boolean c) {
		closeAllOnClose = c;
	}

	@Override
	public void addWindowListener(WindowListener l) {
		super.addWindowListener(l);
	}

	private void init() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				JEFrameRegister.unregister((JEFrame) e.getWindow());
				if (closeAllOnClose) {
                    JEFrameRegister.closeAll();
                }
				//       ((JFrame) e.getWindow()).dispose();
			}
            
			@Override
			public void windowOpened(WindowEvent e) {
				super.windowOpened(e);
				JEFrameRegister.register((JEFrame) e.getWindow());
			}
            
			@Override
			public void windowActivated(WindowEvent e) {
				JEFrameRegister.register((JEFrame) e.getWindow());
				super.windowActivated(e);
			}
			
		});
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK),
				"ctrlFpressed"
		);
		this.getRootPane().getActionMap().put(
				"ctrlFpressed",
				new AbstractAction("ctrlFpressed") {
					public void actionPerformed(ActionEvent actionEvent) {
						((JEFrame) JEFrameRegister.getFrameList()[0]).setExtendedState(JEFrame.NORMAL);
						((JEFrame) JEFrameRegister.getFrameList()[0]).toFront();
					}
				}
		);
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK),
				"ctrlOpressed"
		);
		this.getRootPane().getActionMap().put(
				"ctrlOpressed",
				new AbstractAction("ctrlOpressed") {
					public void actionPerformed(ActionEvent actionEvent) {
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
					public void actionPerformed(ActionEvent actionEvent) {
						JEFrameRegister.setFocusToNext(self);
					}
				}
		);
	}
}
