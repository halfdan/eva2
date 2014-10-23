package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;

import java.io.Serializable;


/**
 * Starts a statistics GUI and the GOProcessor thread.
 */
@SuppressWarnings("unused")
public class GOModuleAdapter extends GenericModuleAdapter implements ModuleAdapter, Serializable {

    private static final String moduleName = "Genetic_Optimization";

    /**
     *
     */
    public static String getName() {
        return moduleName;
    }

    /**
     * Starts a statistics GUI and the GOProcessor thread.
     *
     * @param AdapterName the title of the ModulAdapter
     * @param Client      the client instance
     */
    public GOModuleAdapter(String adapterName) {
        super(adapterName, "", OptimizationParameters.getInstance(), false);
    }

    /**
     * Starts a statistics GUI and the GOProcessor thread with a given OptimizationParameters file.
     *
     * @param AdapterName the title of the ModulAdapter
     * @param Client      the client instance
     */
    public GOModuleAdapter(String adapterName, InterfaceOptimizationParameters goParams, String noGuiLogFile) {
        super(adapterName, "", goParams, false, noGuiLogFile);
    }
}