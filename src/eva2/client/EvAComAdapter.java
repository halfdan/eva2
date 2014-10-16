package eva2.client;


import eva2.gui.LoggingPanel;
import eva2.optimization.EvAMainAdapter;
import eva2.optimization.EvAMainAdapterImpl;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.modules.ModuleAdapter;

import java.util.logging.Logger;

/**
 *
 */
public class EvAComAdapter {
    private final static Logger LOGGER = Logger.getLogger(EvAComAdapter.class.getName());
    private LoggingPanel loggingPanel;
    private EvAMainAdapterImpl localMainAdapter;
    private static EvAComAdapter instance;

    /**
     *
     */
    public static EvAComAdapter getInstance() {
        if (instance == null) {
            instance = new EvAComAdapter();
        }
        return instance;
    }

    /**
     * Creates the ModuleAdapters RMI Object on the server
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
        System.out.println("SAYYYYY WHAAAAAT?");
        String[] list = getLocalMainAdapter().getModuleNameList();
        LOGGER.info("List of modules available:");

        if (list != null) {
            for (String item : list) {
                if (!item.isEmpty() && loggingPanel != null) {
                    LOGGER.info(item);
                }
            }
        }
        return list;
    }
}