package eva2.optimization;

/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version: $Revision: 315 $
 *            $Date: 2007-12-04 15:23:57 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

import eva2.EvAInfo;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.modules.ModuleAdapter;

/**
 *
 */
public class EvAMainAdapterImpl implements EvAMainAdapter {

    private ModuleServer moduleServer = null;

    public EvAMainAdapterImpl() {
        super();
        moduleServer = new ModuleServer(EvAInfo.getProperties());
    }

    @Override
    public String[] getModuleNameList() {
        return moduleServer.getModuleNameList();
    }

    @Override
    public ModuleAdapter getModuleAdapter(String selectedModule) {
        return getModuleAdapter(selectedModule);
    }

    @Override
    public ModuleAdapter getModuleAdapter(String selectedModule, InterfaceOptimizationParameters goParams, String noGuiStatsFile) {
        return moduleServer.createModuleAdapter(selectedModule, goParams, noGuiStatsFile);
    }
}

