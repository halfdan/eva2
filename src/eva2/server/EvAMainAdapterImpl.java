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
import eva2.tools.jproxy.MainAdapterClient;
import eva2.tools.jproxy.MainAdapterImpl;
/**
 *
 */
public class EvAMainAdapterImpl extends MainAdapterImpl implements EvAMainAdapter {

    private ModuleServer moduleServer = null;

    public EvAMainAdapterImpl() {
        super();
        moduleServer = new ModuleServer(EvAInfo.getProperties());
    }

    public String[] getModuleNameList() {
        return moduleServer.getModuleNameList();
    }

    public ModuleAdapter getModuleAdapter(String selectedModule, boolean withoutRMI, String hostAddress, MainAdapterClient client) {
        return getModuleAdapter(selectedModule, withoutRMI, hostAddress, null, null, client);
    }

    public ModuleAdapter getModuleAdapter(String selectedModule, boolean withoutRMI, String hostAddress, InterfaceGOParameters goParams, String noGuiStatsFile, MainAdapterClient client) {
        return moduleServer.createModuleAdapter(selectedModule, client, withoutRMI, hostAddress, goParams, noGuiStatsFile);
    }
}

