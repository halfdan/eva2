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
import eva2.server.go.InterfaceGOParameters;
import eva2.server.modules.ModuleAdapter;
/**
 *
 */
public interface EvAMainAdapter {

    public String[] getModuleNameList();
    // returns the corresponding ModuleAdapter

    ModuleAdapter getModuleAdapter(String selectedModuleName);

    ModuleAdapter getModuleAdapter(String selectedModuleName, InterfaceGOParameters goParams, String noGuiStatsFile);
}
