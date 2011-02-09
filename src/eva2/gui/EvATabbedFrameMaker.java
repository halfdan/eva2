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
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import eva2.server.go.InterfaceNotifyOnInformers;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;

/**
 * Produces the main EvA2 frame and a tool bar instance. 
 * TODO This class should be removed alltogether. 
 */
public class EvATabbedFrameMaker implements Serializable, PanelMaker, InterfaceNotifyOnInformers {
	private static final long serialVersionUID = 2637376545826821423L;
	private ArrayList<PanelMaker>         pmContainer = null;
	private JExtToolBar       m_BarStandard;
	EvAModuleButtonPanelMaker butPanelMkr=null;
	
	public EvATabbedFrameMaker() {
		pmContainer = null;
	}
	
	public void addPanelMaker(PanelMaker pm) {
		if (pmContainer==null) pmContainer = new ArrayList<PanelMaker>(2);
		pmContainer.add(pm);
	}

	public JPanel makePanel() {
		JPanel m_SuperPanel = new JPanel();
		m_SuperPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbconst = new GridBagConstraints();
		gbconst.fill        = GridBagConstraints.BOTH;
		gbconst.weightx     = 1;
		gbconst.weighty     = 1;
		gbconst.gridwidth   = GridBagConstraints.REMAINDER;

		final JTabbedPane m_MainPanel = new JTabbedPane();
//		m_MainPanel.addChangeListener(new ChangeListener() {
//			/*
//			 * This listener was added to catch the switch to the statistics panel. In that event,
//			 * the stats selection string may have to be updated.
//			 */
//			public void stateChanged(ChangeEvent e) {
////				System.out.println("AAAA " + e.toString());
//				if (m_MainPanel.getSelectedIndex()==1) {
//					// the statistics panel is being activated!
////					System.out.println(guiContainer);
//					// the third object should be the statistics panel, refer to GenericModuleAdapter
//					JParaPanel statsPan = (JParaPanel) guiContainer.get(2);
////					System.out.println(statsPan.m_LocalParameter);
////					statsPan.m_Editor.setValue(statsPan.m_Editor.getValue()); // really update the contents of the stats panel -- 
				// this is now done in a cleaner way using this class as a listener from AbstractGOParameters
//				}
//			}});
		
		m_BarStandard = new JExtToolBar();
		m_BarStandard.setFloatable(false);
		
		for (int i=0;i<pmContainer.size();i++) {
			PanelMaker element = pmContainer.get(i);
			JComponent panel = element.makePanel();
			if (element instanceof EvAModuleButtonPanelMaker) {
				m_BarStandard.add(panel);
				butPanelMkr=(EvAModuleButtonPanelMaker)element;
			} else if (element instanceof JParaPanel) {
				m_MainPanel.addTab (((JParaPanel)element).getName(), panel);
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

	public void refreshPanels() {
		for (PanelMaker jpp : pmContainer) {
			if (jpp instanceof JParaPanel) ((JParaPanel)jpp).m_Editor.setValue(((JParaPanel)jpp).m_Editor.getValue());
		}
	}
	
	public void setInformers(
			List<InterfaceAdditionalPopulationInformer> informers) {
		// if the informers have changed, update the GUI element which displays them
		try {
			JParaPanel statsPan = getStatsPanel();
			if (statsPan.m_Editor!=null) {
				statsPan.m_Editor.setValue(statsPan.m_Editor.getValue()); // really update the contents of the stats panel
//				System.out.println("OOO setting informers to stats panel succeeded!");
			}
		} catch(Exception e) {
			System.err.println("Failed to update statistics panel from " + this.getClass());
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	public JParaPanel getGOPanel() {
		try {
			JParaPanel sP = (JParaPanel) pmContainer.get(1);
			return sP;
		} catch(Exception e) {
			System.err.println("Failed to get GO panel from " + this.getClass());
		}
		return null;
	}
	
	public JParaPanel getStatsPanel() {
		try {
			JParaPanel sP = (JParaPanel) pmContainer.get(2);
			return sP;
		} catch(Exception e) {
			System.err.println("Failed to get statistics panel from " + this.getClass());
		}
		return null;
	}
}
