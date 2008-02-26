package javaeva.gui;
/*
 * Title:        JavaEvA
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
import java.io.Serializable;
import java.net.InetAddress;

import javaeva.server.modules.ModuleAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import wsi.ra.jproxy.RMIProxyLocal;
import wsi.ra.jproxy.RemoteStateListener;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class JModuleGeneralPanel implements RemoteStateListener, Serializable  {
	public static boolean TRACE = false;
	private String m_Name ="undefined";
	private ModuleAdapter m_Adapter;
	private boolean m_State;
	private JButton m_RunButton;
//	private JButton m_RestartButton;
	private JButton m_actStop;
//	private JButton m_actExitMod;
	private JButton m_JHelpButton;
	private JPanel m_Panel;
	private String m_HelperFileName;

	/**
	 *
	 */
	public JModuleGeneralPanel(ModuleAdapter Adapter, boolean state) {
		m_Name = "GENERAL";
		m_State = state;
		if (TRACE) System.out.println("Constructor JModuleGeneralPanel:");
		m_Adapter = Adapter;
	}
	/**
	 *
	 */
	public JComponent installActions() {
		String myhostname = null;

		m_Panel= new JPanel();
		if(TRACE) System.out.println("JModuleGeneral.installAction()");
		if (m_Adapter.hasConnection()) { // we might be in rmi mode
			try {
				myhostname  =  InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				System.err.println("ERROR getting myhostname "+e.getMessage());
			}
		}
		if (!m_Adapter.hasConnection()) {
			m_Adapter.addRemoteStateListener((RemoteStateListener)(this));
		} else {// there is a network RMI connection
			m_Adapter.addRemoteStateListener((RemoteStateListener)RMIProxyLocal.newInstance(this));
		}

		//////////////////////////////////////////////////////////////
		m_RunButton= new JButton("Start Optimization");
		m_RunButton.setToolTipText("Start the current optimization run.");
		//System.out.println("Start tm_RunButton.addActionListener Run Opt pressed ====================!!!!!!!!!!!!!!!!!!");
		m_RunButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//System.out.println("Run Opt pressed !!!!!!!!!!!!!!!!======================!!");
				try {
					m_Adapter.startOpt();
					m_actStop.setEnabled(true);
					m_RunButton.setEnabled(false);
//					m_RestartButton.setEnabled(false);
					m_JHelpButton.setEnabled(true);
				} catch (Exception ee) {
					ee.printStackTrace();
					System.out.print ("Error in run: " +ee +" : " + ee.getMessage() );
				}
			}
		}
		);

		if (m_State == false )
			m_RunButton.setEnabled(true);
		else
			m_RunButton.setEnabled(false);
		m_Panel.add(m_RunButton);
//		m_Panel.setBorder(BorderFactory.createTitledBorder("general action buttons"));
//		//////////////////////////////////////////////////////////////
//		m_RestartButton= new JButton("Restart Optimization");
//		m_RestartButton.setToolTipText("Restart the current optimization run.");
//		m_RestartButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e){
//				try {
//					m_Adapter.restartOpt();
//					m_actStop.setEnabled(true);
//					m_RunButton.setEnabled(false);
//					m_RestartButton.setEnabled(false);
//				} catch (Exception ee) {
//					ee.printStackTrace();
//					System.out.println("Error in run: " +ee +" : " + ee.getMessage() );
//				}
//			}
//		}
//		);
//		if (m_State == false )
//			m_RestartButton.setEnabled(false);
//		else
//			m_RestartButton.setEnabled(false);
//		m_Panel.add(m_RestartButton);
		//////////////////////////////////////////////////////////////
		m_actStop= new JButton("Stop");
		m_actStop.setToolTipText("Stop the current optimization run.");
		m_actStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				try {
					m_Adapter.stopOpt();	// this means user break
				} catch (Exception ee) { System.out.print ("Error in stop: " + ee.getMessage() ); }
			}
		}
		);
//		if (m_State == false )
//			m_RestartButton.setEnabled(false);
//		else
//			m_RestartButton.setEnabled(true);
		m_Panel.add(m_actStop);
		if (m_HelperFileName.equals("")== false) {
			m_JHelpButton= new JButton("Description");
			m_JHelpButton.setToolTipText("Description of the current optimization algorithm.");
			m_JHelpButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					//System.out.println("Run Opt pressed !!!!!!!!!!!!!!!!======================!!");
					try {
						if (m_HelperFileName!=null) {
							HtmlDemo temp = new HtmlDemo(m_HelperFileName);
							temp.show();
						}
						m_JHelpButton.setEnabled(true);
					} catch (Exception ee) {
						ee.printStackTrace();
						System.out.print ("Error in run: " +ee +" : " + ee.getMessage() );
					}
				}
			}
			);
			m_Panel.add(m_JHelpButton);
		}
//		m_actExitMod = new JButton("Exit Module");
//		m_actExitMod.setToolTipText("todo !!.");// TODO
		return m_Panel;
	}

	/**
	 *
	 */
	public void performedStop() {
		if (TRACE) System.out.println("JModuleGeneralPanel.stopOptPerformed");
		m_RunButton.setEnabled(true);
//		m_RestartButton.setEnabled(true);
		m_RunButton.repaint();
		m_actStop.setEnabled(false);
		m_Panel.repaint();
	}

	public void performedStart(String infoString) {
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
	public void setHelperFilename (String s) {
		m_HelperFileName = s;
	}
}

