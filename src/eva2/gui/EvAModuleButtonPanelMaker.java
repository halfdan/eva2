package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 284 $
 *            $Date: 2007-11-27 14:37:05 +0100 (Tue, 27 Nov 2007) $
 *            $Author: mkron $
 */

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import eva2.EvAInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import eva2.server.modules.ModuleAdapter;
import eva2.server.stat.EvAJob;
import eva2.tools.BasicResourceLoader;
import eva2.tools.jproxy.RMIProxyLocal;
import eva2.tools.jproxy.RemoteStateListener;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Contains the GUI elements of start and stop buttons and optionally a help button.
 */
public class EvAModuleButtonPanelMaker implements RemoteStateListener, Serializable, PanelMaker  {
	private static final Logger logger = Logger.getLogger(EvAInfo.defaultLogger);
	private String m_Name = "undefined";
	private ModuleAdapter moduleAdapter;
	private boolean runningState;
	private JButton runButton;
	private JButton postProcessButton;
	private JButton stopButton;
	private JButton scheduleButton;
//	private JButton m_actExitMod;
	private JButton helpButton;
//	private JButton m_ShowSolButton;
	private JToolBar toolBar;
	private String helpFileName;

	/**
	 *
	 */
	public EvAModuleButtonPanelMaker(ModuleAdapter adapter, boolean state) {
		m_Name = "GENERAL";
		runningState = state;
		moduleAdapter = adapter;
	}

	public JToolBar makePanel() {
		String myhostname = null;

		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		if (moduleAdapter.hasConnection()) { // we might be in rmi mode
			try {
				myhostname  =  InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {                 
				logger.log(Level.WARNING, "Could not get hostname", e);
			}
		}
		if (!moduleAdapter.hasConnection()) {
			moduleAdapter.addRemoteStateListener((RemoteStateListener)(this));
		} else {// there is a network RMI connection
			moduleAdapter.addRemoteStateListener((RemoteStateListener)RMIProxyLocal.newInstance(this));
		}

		//////////////////////////////////////////////////////////////
		runButton = makeIconButton("resources/images/Play24.gif", "Start");
		runButton.setToolTipText("Start the current optimization run.");
		runButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				//Run Opt pressed !
				onUserStart();
			}
		});

		runButton.setEnabled(!runningState); // enabled if not running

		toolBar.add(runButton);

		stopButton = makeIconButton("resources/images/Stop24.gif", "Stop");
		stopButton.setToolTipText("Stop the current optimization run.");
		stopButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				try {
					// this means user break
					moduleAdapter.stopOpt();
				} catch (Exception ee) {
					logger.log(Level.WARNING, "Error while stopping job.", ee);
				}
			}
		});

		stopButton.setEnabled(runningState);
		toolBar.add(stopButton);

		postProcessButton = makeIconButton("resources/images/History24.gif", "Post Process");
		postProcessButton.setToolTipText("Start post processing according to available parameters.");
		postProcessButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (!moduleAdapter.startPostProcessing()) {
						JOptionPane.showMessageDialog(null, "Post processing seems deactivated! Check the settings.", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				} catch (Exception ee) {
					logger.log(Level.WARNING, "Error in run", ee);
				}
			}
		});
		postProcessButton.setEnabled(runningState && moduleAdapter.hasPostProcessing());
		toolBar.add(postProcessButton);
//		//////////////////////////////////////////////////////////////
		scheduleButton= makeIconButton("resources/images/Server24.gif", "Schedule");
		scheduleButton.setToolTipText("Schedule the currently configured optimization as a job.");
		scheduleButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				EvAJob job = moduleAdapter.scheduleJob();
				if (job == null) {
					logger.log(Level.WARNING, "There was an error on scheduling your job");
				}
			}
		});
		scheduleButton.setEnabled(true);
		toolBar.add(scheduleButton);

		makeHelpButton();

		return toolBar;
	}
	
	private JButton makeIconButton(final String iconSrc, final String title) {
		JButton newButton;
		byte[] bytes;
		bytes = BasicResourceLoader.instance().getBytesFromResourceLocation(iconSrc, false);
		if (bytes == null) {
			newButton = new JButton(title);			
		} else {
			newButton = new JButton(title, new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
		}
		return newButton;
	}
	
	public void onUserStart() {
		try {
			moduleAdapter.startOpt();
			stopButton.setEnabled(true);
			runButton.setEnabled(false);
			postProcessButton.setEnabled(false);
//			m_RestartButton.setEnabled(false);
//			m_JHelpButton.setEnabled(true);
		} catch (Exception ee) {
			ee.printStackTrace();
			System.err.print ("Error in run: " +ee +" : " + ee.getMessage() );
		}
	}
	
	private void makeHelpButton() {
		///////////////////////////////////////////////////////////////
		if (helpFileName!=null && (!helpFileName.equals(""))) {
			helpButton= new JButton("Description");
			helpButton.setToolTipText("Description of the current optimization algorithm.");
			helpButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					//System.out.println("Run Opt pressed !!!!!!!!!!!!!!!!======================!!");
					try {
						if (helpFileName!=null) {
							HtmlDemo temp = new HtmlDemo(helpFileName);
							temp.show();
						}
						helpButton.setEnabled(true);
					} catch (Exception ee) {
						ee.printStackTrace();
						System.out.print ("Error in run: " +ee +" : " + ee.getMessage() );
					}
				}
			}
			);
			toolBar.add(helpButton);
		}
	}
    
	/**
	 *
	 */
	public void performedStop() {
		runButton.setEnabled(true);
		postProcessButton.setEnabled(true);
		runButton.repaint();
		stopButton.setEnabled(false);
		toolBar.repaint();
	}

	public void performedStart(String infoString) {
//		m_ShowSolButton.setEnabled(true);
	}


	public void performedRestart(String infoString) {
	}
	
	public void updateProgress(final int percent, String msg) {
	}
	
	/**
	 *
	 */
	public String getName() {
		return m_Name;
	}
	/**
	 *
	 */
	public void setHelperFilename(String fileName) {
		if ((fileName == null) && (fileName == helpFileName)) {
			return; // both are null, do nothing
		}
		if (fileName != null) {
			if (helpFileName == null) {
				// only old is null, nothing to be removed
				helpFileName = fileName;
				makeHelpButton();
			} else {
				if (!helpFileName.equals(fileName)) {
					toolBar.remove(helpButton);
					helpFileName = fileName;
					makeHelpButton();
				} //else // both are equal, do nothing				
			}
		} else { // s is null, so just remove
			toolBar.remove(helpButton);
			helpFileName = fileName;
		}
	}
}

