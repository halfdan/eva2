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
import eva2.gui.LoggingPanel;
import eva2.optimization.EvAMainAdapter;
import eva2.optimization.EvAMainAdapterImpl;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.modules.ModuleAdapter;

/**
 *
 */
public class EvAComAdapter {

    private LoggingPanel loggingPanel;
    private EvAMainAdapterImpl localMainAdapter;
    private boolean runLocally = false;
    private static EvAComAdapter m_instance;

    /**
     *
     */
    public void setLogPanel(LoggingPanel loggingPanel) {
        this.loggingPanel = loggingPanel;
    }

    /**
     *
     */
    public static EvAComAdapter getInstance() {
        if (m_instance == null) {
            m_instance = new EvAComAdapter();
        }
        return (EvAComAdapter) m_instance;
    }

    /**
     * Creates the ModulAdapters RMI Object on the server
     *
     * @return
     */
    public ModuleAdapter getModuleAdapter(String selectedModuleName, InterfaceOptimizationParameters goParams, String noGuiStatsFile) {
        ModuleAdapter newModuleAdapter;
        newModuleAdapter = getLocalMainAdapter().getModuleAdapter(selectedModuleName, goParams, noGuiStatsFile);
        
        return newModuleAdapter;
    }

    public void updateLocalMainAdapter() {
        localMainAdapter = new EvAMainAdapterImpl();
    }

    private EvAMainAdapter getLocalMainAdapter() {
        if (localMainAdapter == null) {
            localMainAdapter = new EvAMainAdapterImpl();
        }
        return localMainAdapter;
    }

    /**
     * Returns a list of modules available on the server.
     *
     * @return
     */
    public String[] getModuleNameList() {
        String[] list;
        list = getLocalMainAdapter().getModuleNameList();
        if (loggingPanel != null) {
            loggingPanel.logMessage("List of modules available:");
        }
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                if ((String) list[i] != null && loggingPanel != null) {
                    loggingPanel.logMessage((String) list[i]);
                }
            }
        }
        return list;
    }
}