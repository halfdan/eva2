package eva2.gui;
/*
 * Title:        EvA2
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Produces the main EvA2 frame and a tool bar instance.
 */
public class EvATabbedFrameMaker implements Serializable, PanelMaker {
	private ArrayList<PanelMaker>         guiContainer;
	private JExtToolBar       m_BarStandard;
	EvAModuleButtonPanelMaker butPanelMkr=null;
	
	public EvATabbedFrameMaker(ArrayList<PanelMaker> GUIContainer) {
		guiContainer = GUIContainer;
	}

	public JPanel makePanel() {
		JPanel m_SuperPanel = new JPanel();
		m_SuperPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbconst = new GridBagConstraints();
		gbconst.fill        = GridBagConstraints.BOTH;
		gbconst.weightx     = 1;
		gbconst.weighty     = 1;
		gbconst.gridwidth   = GridBagConstraints.REMAINDER;

		JTabbedPane m_MainPanel = new JTabbedPane();
		
		m_BarStandard = new JExtToolBar();
		m_BarStandard.setFloatable(false);

		for (int i=0;i<guiContainer.size();i++) {
			PanelMaker element = guiContainer.get(i);
			if (element instanceof EvAModuleButtonPanelMaker) {
				m_BarStandard.add(element.makePanel());
				butPanelMkr=(EvAModuleButtonPanelMaker)element;
			} else if (element instanceof JParaPanel) {
				m_MainPanel.addTab (((JParaPanel)element).getName(), element.makePanel());
			}
		}
		m_SuperPanel.add(m_MainPanel, gbconst);

		return m_SuperPanel;
	}
	
	public JExtToolBar getToolBar() {
		return m_BarStandard;
	}
	
	/**
	 * Emulate pressing the start button.
	 */
	public void onUserStart() {
		if (butPanelMkr!=null) {
			butPanelMkr.onUserStart();
		} else System.err.println("Error: button panel was null (EvATabbedFrameMaker)");
	}
}
