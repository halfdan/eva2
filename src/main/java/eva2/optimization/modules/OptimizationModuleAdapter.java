package eva2.optimization.modules;

import eva2.optimization.InterfaceOptimizationParameters;
import eva2.optimization.OptimizationParameters;

import java.io.Serializable;


/**
 * Starts a statistics GUI and the Processor thread.
 */
@SuppressWarnings("unused")
public class OptimizationModuleAdapter extends GenericModuleAdapter implements ModuleAdapter, Serializable {

    private static final String moduleName = "Genetic_Optimization";

    /**
     *
     */
    public static String getName() {
        return moduleName;
    }

    /**
     * Starts a statistics GUI and the Processor thread.
     *
     * @param adapterName the title of the ModuleAdapter
     */
    public OptimizationModuleAdapter(String adapterName) {
        super(adapterName, "", OptimizationParameters.getInstance(), false);
    }

    /**
     * Starts a statistics GUI and the Processor thread with a given OptimizationParameters file.
     *
     * @param adapterName the title of the ModuleAdapter
     * @param optimizationParameters the client instance
     * @param noGuiLogFile
     */
    public OptimizationModuleAdapter(String adapterName, InterfaceOptimizationParameters optimizationParameters, String noGuiLogFile) {
        super(adapterName, "", optimizationParameters, false, noGuiLogFile);
    }
}