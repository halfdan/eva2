package javaeva.server;

/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 315 $
 *            $Date: 2007-12-04 15:23:57 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import javaeva.client.EvAClient;
import javaeva.server.modules.ModuleAdapter;
import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.MainAdapterImpl;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class EvAMainAdapterImpl extends MainAdapterImpl implements EvAMainAdapter {
  private ModuleServer m_ModulServer=null;
  /**
   *
   */
  public EvAMainAdapterImpl() {
	  super();
	  m_ModulServer = new ModuleServer(EvAClient.getProperties());
  }
  /**
   *
   */
  public String[] getModuleNameList() {
    return m_ModulServer.getModuleNameList();
  }

  /**
   *
   */
  public ModuleAdapter getModuleAdapter(String str, boolean withoutRMI, String hostAddress, MainAdapterClient client) {
    if (TRACE) System.out.println("MainAdapterImpl.GetModuleAdapter() for module " +
      str +" for Client: "+hostAddress+ " called");
    return m_ModulServer.createModuleAdapter(str,client,withoutRMI,hostAddress);
  }
}

