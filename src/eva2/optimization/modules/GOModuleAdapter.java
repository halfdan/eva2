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
     * @param adapterName the title of the ModuleAdapter
     */
    public GOModuleAdapter(String adapterName) {
        super(adapterName, "", OptimizationParameters.getInstance(), false);
    }

    /**
     * Starts a statistics GUI and the GOProcessor thread with a given OptimizationParameters file.
     *
     * @param adapterName the title of the ModuleAdapter
     * @param optimizationParameters the client instance
     * @param noGuiLogFile
     */
    public GOModuleAdapter(String adapterName, InterfaceOptimizationParameters optimizationParameters, String noGuiLogFile) {
        super(adapterName, "", optimizationParameters, false, noGuiLogFile);
    }
}