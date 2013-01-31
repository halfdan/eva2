package eva2.server;

/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 315 $
 *            $Date: 2007-12-04 15:23:57 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
import eva2.EvAInfo;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.modules.ModuleAdapter;
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
    public ModuleAdapter getModuleAdapter(String selectedModule, InterfaceGOParameters goParams, String noGuiStatsFile) {
        return moduleServer.createModuleAdapter(selectedModule, goParams, noGuiStatsFile);
    }
}

