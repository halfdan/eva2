package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 191 $
 *            $Date: 2007-10-23 12:56:51 +0200 (Tue, 23 Oct 2007) $
 *            $Author: mkron $
 */
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import eva2.EvAInfo;
import eva2.tools.BasicResourceLoader;
/**
 *
 */
public class LogPanel extends JPanel {
//	protected JLabel m_Message = new JLabel("OK");
	protected JTextArea loggingTextArea = new JTextArea(10,20);
	protected boolean m_first = true;
	/**
	 *
	 */
	public LogPanel() {
		loggingTextArea.setEditable(false);
		loggingTextArea.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder("Info"));
		mainPanel.setLayout(new BorderLayout());
		final JScrollPane scrollpane = new JScrollPane(loggingTextArea);
//		scrollpane.setAutoscrolls(false);
		mainPanel.add(scrollpane, BorderLayout.CENTER);
		scrollpane.getViewport().addChangeListener(new ChangeListener() {
			private int lastHeight;
			//
			public void stateChanged(ChangeEvent e) {
				JViewport viewport = (JViewport)e.getSource();
				int height = viewport.getViewSize().height;
				if (height != lastHeight) {
					lastHeight = height;
					int x = height - viewport.getExtentSize().height;
					viewport.setViewPosition(new Point(0, x));
				}
			}
		});
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BorderLayout());
//		panel_2.add(m_Message,BorderLayout.CENTER);
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
		loggingTextArea.append("\n");
		loggingTextArea.append(LogPanel.getTimestamp() + ' ' + message);
	}

        
	/**
	 *
	 */
	public static void main(String [] args) {
		try {
			final JFrame frame = new JFrame("Log_Panel_Test");
			frame.getContentPane().setLayout(new BorderLayout());
			BasicResourceLoader  loader  = BasicResourceLoader.instance();
			byte[] bytes   = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
			frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
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
			panel.logMessage("Test ------------------------------------------------------------------------");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
