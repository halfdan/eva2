package eva2.server;
/**
 * Title:        EvA2
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 259 $
 *            $Date: 2007-11-16 17:25:09 +0100 (Fri, 16 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import eva2.server.go.InterfaceGOParameters;
import eva2.server.modules.ModuleAdapter;
import eva2.tools.jproxy.MainAdapter;
import eva2.tools.jproxy.MainAdapterClient;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface EvAMainAdapter extends MainAdapter {

  public String[] getModuleNameList();
  // returns the corresponding ModuleAdapter
  public ModuleAdapter getModuleAdapter(String selectedModuleName,
		  boolean withoutRMI,
		  String hostAddress,
		  MainAdapterClient client);
  
  public ModuleAdapter getModuleAdapter(String selectedModuleName,
		  boolean withoutRMI,
		  String hostAddress,
		  InterfaceGOParameters goParams,
		  String noGuiStatsFile,
		  MainAdapterClient client);
}
