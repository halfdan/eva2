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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 */
public class LoggingPanel extends JPanel {
	protected static Logger logger;
	protected JTextArea loggingTextArea = new JTextArea(10,20);
	protected boolean firstMessage = true;
	protected Handler loggingHandler;
	protected JPopupMenu loggingLevelMenu;
	/**
	 *
	 */
	public LoggingPanel(Logger logger) {
		this.logger = logger;
		loggingTextArea.setEditable(false);		
		loggingTextArea.setLineWrap(true);
        loggingTextArea.setBorder(BorderFactory.createEmptyBorder());
				
		setLayout(new BorderLayout());
		
        add(new JLabel("Info"), BorderLayout.PAGE_START);
		
		this.loggingHandler = new LoggingHandler(this);
		logger.addHandler(loggingHandler);
		
		final JScrollPane scrollpane = new JScrollPane(loggingTextArea);
        scrollpane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
//		scrollpane.setAutoscrolls(false);
		add(scrollpane, BorderLayout.CENTER);
		scrollpane.getViewport().addChangeListener(new ChangeListener() {
			private int lastHeight;
			//
            @Override
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
		loggingTextArea.append(LoggingPanel.getTimestamp() + ' ' + message);
		loggingTextArea.append("\n");
	}
}

class LoggingHandler extends Handler {
	protected LoggingPanel loggingPanel;
	
	public LoggingHandler(LoggingPanel loggingPanel) {
		this.loggingPanel = loggingPanel;
	}

	@Override
	public void publish(LogRecord record) {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("[");
		sBuilder.append(record.getLevel().toString());
		sBuilder.append("] ");
		MessageFormat messageFormat = new MessageFormat(record.getMessage());
		sBuilder.append(messageFormat.format(record.getParameters()));
		// Show message on LogPanel
		this.loggingPanel.logMessage(sBuilder.toString());
	}

	@Override
	public void flush() {
		/*
		 * We do nothing here as we don't buffer the entries
		 */
	}

	@Override
	public void close() throws SecurityException {
		/*
		 * Nothing to close
		 */
	}
}