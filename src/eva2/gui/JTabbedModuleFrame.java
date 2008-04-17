package eva2.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 272 $
 *            $Date: 2007-11-21 18:06:36 +0100 (Wed, 21 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.* ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import eva2.client.EvAClient;
import eva2.gui.JModuleGeneralPanel;
import eva2.gui.JParaPanel;
import eva2.server.modules.ModuleAdapter;

import java.util.ArrayList;
import wsi.ra.tool.BasicResourceLoader;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class JTabbedModuleFrame implements Serializable {
	private ArrayList         m_GUIContainer;
//	private JTabbedPane       m_MainPanel;
	private ModuleAdapter      m_ProxyModuleAdapter;
	private JPanel            m_PanelTool;
//	private JPanel            m_SuperPanel;
	private JExtToolBar       m_BarStandard;
	private JFrame            m_Frame;
	private String            m_AdapterName;
	private String            m_Host;
	/**
	 *
	 */
	public JTabbedModuleFrame(ModuleAdapter newModuleAdapter,String AdapterName,String Host,ArrayList GUIContainer) {
		m_ProxyModuleAdapter = newModuleAdapter;
		m_AdapterName = AdapterName;
		m_Host = Host;
		m_GUIContainer = GUIContainer;
	}

	public JPanel createContentPane() {
		JPanel m_SuperPanel = new JPanel();
		m_SuperPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbconst = new GridBagConstraints();
		gbconst.fill        = GridBagConstraints.BOTH;
		gbconst.weightx     = 1;
		gbconst.weighty     = 1;
		gbconst.gridwidth   = GridBagConstraints.REMAINDER;

		JTabbedPane m_MainPanel = new JTabbedPane();
		buildToolBar();

		for (int i=0;i<m_GUIContainer.size();i++) {
			Object TEMP = m_GUIContainer.get(i);
			if (TEMP instanceof JModuleGeneralPanel) {
				m_BarStandard.add((JPanel) ((JModuleGeneralPanel)TEMP).installActions());
				continue;
			}
			if (TEMP instanceof JParaPanel) {
				JPanel tmpPanel = (JPanel)((JParaPanel)TEMP).installActions();
				m_MainPanel.addTab (((JParaPanel)TEMP).getName(), tmpPanel);
			}
		}
		m_MainPanel.addChangeListener(new ChangeListener () {
			public void stateChanged (ChangeEvent evt) {
				//Dimension d = (m_MainPanel.getSelectedComponent()).getPreferredSize();
				//System.out.println("HERETETETETE " + d.height + " " + d.width + " " + m_MainPanel.getSelectedIndex());
				//m_Frame.setSize(d);
				//m_MainPanel.validate();
				//m_Frame.pack();
				//m_Frame.validate();
			}
		});
		m_SuperPanel.add(m_MainPanel, gbconst);

		return m_SuperPanel;
	}
	
	public JExtToolBar getToolBar() {
		return m_BarStandard;
	}
	
	/**
	 *
	 */
	public void createGUI() {
		m_Frame = new  JEFrame (m_AdapterName+" on "+m_Host);

		BasicResourceLoader  loader  = BasicResourceLoader.instance();
		byte[] bytes   = loader.getBytesFromResourceLocation(EvAClient.iconLocation);
		try {
			m_Frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
		} catch (java.lang.NullPointerException e) {
			System.out.println("Could not find EvA2 icon, please move resource folder to working directory!");
		}     

		JPanel m_SuperPanel = createContentPane();

		m_Frame.getContentPane().add(m_SuperPanel);
		m_Frame.getContentPane().add(m_PanelTool, BorderLayout.NORTH);
		
		m_Frame.pack();
		m_Frame.setVisible(true);
		System.out.println(m_Frame.getPreferredSize().toString());

		m_Frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		m_Frame.toFront();
	}

//	/**
//	*
//	*/
//	private void removeGUI() {
//	System.out.println("removeGUI");
//	for (int i=0;i< m_GUIContainer.size();i++) {
//	Object TEMP = m_GUIContainer.get(i);
//	if (TEMP instanceof JPanel)
//	m_MainPanel.remove((JPanel)TEMP);
//	}
//	m_Frame.repaint();
//	}
	/**
	 *
	 */
	private void buildToolBar(){
		m_PanelTool = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_BarStandard = new JExtToolBar();
		m_BarStandard.setFloatable(false);
//		m_BarStandard.setBorder(BorderFactory.createRaisedBevelBorder());
		m_PanelTool.add(m_BarStandard);
//		m_Frame.getContentPane().add(m_PanelTool, BorderLayout.NORTH);
	}
}
