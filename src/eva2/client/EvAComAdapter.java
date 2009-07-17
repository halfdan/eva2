package eva2.client;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 320 $
 *            $Date: 2007-12-06 16:05:11 +0100 (Thu, 06 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import eva2.gui.LogPanel;
import eva2.server.EvAMainAdapter;
import eva2.server.EvAMainAdapterImpl;
import eva2.server.RMIServerEvA;
import eva2.server.modules.ModuleAdapter;

import wsi.ra.jproxy.ComAdapter;
import wsi.ra.jproxy.MainAdapter;
import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.MainAdapterClientImpl;
import wsi.ra.jproxy.RMIConnection;
import wsi.ra.jproxy.RMIInvocationHandler;
import wsi.ra.jproxy.RMIProxyLocal;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class EvAComAdapter extends ComAdapter {
	private LogPanel m_LogPanel;
	private EvAMainAdapterImpl localMainAdapter;
	private boolean runLocally = false;
	
	/**
	 *
	 */
	public void setLogPanel(LogPanel OutputFrame) {
		m_LogPanel = OutputFrame;
	}
	/**
	 *
	 */
	public static EvAComAdapter getInstance() {
		if (m_instance==null) {
			m_instance = new EvAComAdapter();
			m_instance.addServersFromProperties(EvAClient.getProperties());
		}
		return (EvAComAdapter)m_instance;
	}

	/**
	 * Creates the ModulAdapters RMI Object on the server
	 * @return
	 */
	public ModuleAdapter getModuleAdapter(String selectedModuleName, String paramsFile, String noGuiStatsFile) {
		ModuleAdapter newModuleAdapter;
		if ((m_RMIServer == null) && isRunLocally()) {
			//ret = evaAdapter.getModuleAdapter(Modul, hostAdd, this.m_MainAdapterClient);
			newModuleAdapter = getLocalMainAdapter().getModuleAdapter(selectedModuleName, true, getHostName(), paramsFile, noGuiStatsFile, null);
		} else {
			newModuleAdapter = ((RMIConnectionEvA)getConnection(getHostName())).getModuleAdapter(selectedModuleName);
			if (newModuleAdapter == null) System.err.println("RMI Error for getting ModuleAdapterObject : " + selectedModuleName);
		}
		return newModuleAdapter;
	}
	
	public void updateLocalMainAdapter() {
		localMainAdapter = new EvAMainAdapterImpl();
	}
	
	private EvAMainAdapter getLocalMainAdapter() { 
		if (localMainAdapter == null) localMainAdapter = new EvAMainAdapterImpl();
		return localMainAdapter;
	}
	
	/**
	 *  Returns a list of modules available on the server.
	 *  @return
	 */
	public String[] getModuleNameList() {
		String[] list;
		if (TRACE) System.out.println("ComAdapter.GetModuleNameList()");
		
		if ((m_RMIServer == null) && isRunLocally()) {
			list = getLocalMainAdapter().getModuleNameList();
		} else { 
			RMIConnectionEvA Connection = (RMIConnectionEvA)getConnection(getHostName());
			if (Connection == null) {
				System.err.println("Couldnt create RMIConnection in EvAComAdapter.getModuleNameList");
				return null;
			}
			list = ((EvAMainAdapter)Connection.getMainAdapter()).getModuleNameList();
		}
		if (m_LogPanel != null)
			m_LogPanel.logMessage("List of modules on server:");
		if (list != null)
			for (int i = 0; i < list.length; i++) {
				if ( (String) list[i] != null && m_LogPanel != null)
					m_LogPanel.logMessage( (String) list[i]);
			}
		return list;
	}
		
	protected MainAdapter getMainAdapter(RMIInvocationHandler invocHandler) throws RemoteException {
		try {
			return (EvAMainAdapter) invocHandler.getWrapper();
		} catch (ClassCastException e) {
			System.err.println("Warning: cannot cast to EvAMainAdapter in EvAComAdapter.. trying MainAdapter...");
		}
		return (MainAdapter) invocHandler.getWrapper();
	}

	protected void logInfo(String msg) {
		if (m_LogPanel != null) {
			m_LogPanel.logMessage(msg);
		} else super.logInfo(msg);
	}
	
	protected RMIConnection createRMIConnection(String Host, MainAdapter mainRemoteObject, MainAdapterClient client) { 
		return new RMIConnectionEvA(Host, mainRemoteObject, client);
	}
	/**
	 * @return the runLocally
	 */
	public boolean isRunLocally() {
		return runLocally;
	}
	/**
	 * @param runLocally the runLocally to set
	 */
	public void setRunLocally(boolean runLocally) {
		this.runLocally = runLocally;
	}
}
//

