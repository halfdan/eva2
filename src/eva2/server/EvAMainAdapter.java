package eva2.server;
/**
 * Title:        javaeva
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

import eva2.server.modules.ModuleAdapter;
import wsi.ra.jproxy.MainAdapter;
import wsi.ra.jproxy.MainAdapterClient;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface EvAMainAdapter extends MainAdapter {

  public String[] getModuleNameList();
  // returns the corresponding ModuleAdapter
  public ModuleAdapter getModuleAdapter(String str,
		  boolean withoutRMI,
		  String hostAddress,
		  MainAdapterClient client);

}
