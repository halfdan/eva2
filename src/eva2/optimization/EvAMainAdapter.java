package eva2.optimization;
/**
 * Title:        EvA2
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 259 $
 *            $Date: 2007-11-16 17:25:09 +0100 (Fri, 16 Nov 2007) $
 *            $Author: mkron $
 */
import eva2.optimization.go.InterfaceGOParameters;
import eva2.optimization.modules.ModuleAdapter;
/**
 *
 */
public interface EvAMainAdapter {

    public String[] getModuleNameList();
    // returns the corresponding ModuleAdapter

    ModuleAdapter getModuleAdapter(String selectedModuleName);

    ModuleAdapter getModuleAdapter(String selectedModuleName, InterfaceGOParameters goParams, String noGuiStatsFile);
}
