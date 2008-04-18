package eva2.gui;
/*
 * Title:        EvA2
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
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;


import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import eva2.client.EvAClient;
import eva2.tools.MultirunRefiner;

import wsi.ra.tool.BasicResourceLoader;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class JTextoutputFrame implements JTextoutputFrameInterface,
ActionListener,
Serializable {
	public static boolean TRACE = false;
	protected String m_Name ="undefined";
	private transient JTextArea m_TextArea = null;
//	private boolean m_firstprint = true;
	private final JFrame frame;

	JPopupMenu popup;
	/**
	 *
	 */
	public JTextoutputFrame(String Title) {
		if (TRACE) System.out.println("JTextoutputFrame Constructor");
		m_Name = Title;
		frame = new JEFrame(m_Name);
		m_TextArea = null;
	}
	/**
	 *
	 */
	public void print(String Text) {
		//System.out.println("Print:"+Text);
		if (m_TextArea == null) {
			createFrame();
		}
		m_TextArea.append(Text);
		m_TextArea.repaint();
	}

	public void println(String txt) {
		print(txt+'\n');
	}

	public void setShow(boolean bShow) {
		if (frame.isVisible() != bShow) {
			if (frame.isVisible()) {
				frame.dispose();
				m_TextArea.setText(null);
			} else {
				if (m_TextArea == null) createFrame();
				else frame.setVisible(true);
				frame.setEnabled(true);
			}
		}
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

		BasicResourceLoader  loader  = BasicResourceLoader.instance();
		byte[] bytes   = loader.getBytesFromResourceLocation(EvAClient.iconLocation);
		try {
			frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
		} catch (java.lang.NullPointerException e) {
			System.out.println("Could not find EvA2 icon, please move resource folder to working directory!");
		} 
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				frame.setEnabled(false);
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
		makePopupMenu();
		frame.pack();
		frame.setSize(800, 400);
		frame.setVisible(true);
		frame.setState(Frame.ICONIFIED);
	}
	
	/**
	 *output
	 */
	public static void main( String[] args ){
		JTextoutputFrame test = new JTextoutputFrame("hi");
		while (test.frame.isEnabled()) {
			test.print("Test 12345");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Done!");
	}


	void makePopupMenu() {
		//Create the popup menu.
		popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Clear");
		menuItem.addActionListener(this);
		popup.add(menuItem);
//		menuItem = new JMenuItem("Refine Multiruns");
//		menuItem.addActionListener(this);
//		popup.add(menuItem);

		//Add listener to components that can bring up popup menus.
		MouseListener popupListener = new PopupListener(popup);
//		frame.addMouseListener(popupListener);
		m_TextArea.addMouseListener(popupListener);
//		menuBar.addMouseListener(popupListener);


	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == popup.getComponent(0)) {
			m_TextArea.setText(null);
		} 
//		else if (e.getSource() == popup.getComponent(1)) {
//			m_TextArea.append(MultirunRefiner.refineToText(m_TextArea.getText()));
//		} 
		else System.out.println("no popup component!");
	}
}

/**
 * A popup listener opening a popup menu on right clicks.
 * 
 * @author mkron
 */
class PopupListener extends MouseAdapter {
	JPopupMenu popup;

	public PopupListener(JPopupMenu pm) {
		popup = pm;
	}
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popup.show(e.getComponent(),
					e.getX(), e.getY());
		}
	}
}