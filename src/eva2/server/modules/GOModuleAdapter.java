package eva2.server.modules;

import eva2.server.go.InterfaceGOParameters;
import java.io.Serializable;


/**
 * Starts a statistics GUI and the GOProcessor thread.
 */
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
     * @param AdapterName	the title of the ModulAdapter
     * @param Client	the client instance
     */
    public GOModuleAdapter(String adapterName) {
        super(adapterName, "", GOParameters.getInstance(), false);
    }

    /**
     * Starts a statistics GUI and the GOProcessor thread with a given GOParameters file.
     *
     * @param AdapterName	the title of the ModulAdapter
     * @param Client	the client instance
     */
    public GOModuleAdapter(String adapterName, InterfaceGOParameters goParams, String noGuiLogFile) {
        super(adapterName, "", goParams, false, noGuiLogFile);
    }
//  /**
//   * Starts a statistics GUI and the GOProcessor thread with a given GOParameters file.
//   * 
//   * @param AdapterName	the title of the ModulAdapter
//   * @param Client	the client instance
//   */
//  public GOModuleAdapter(String adapterName, String serParamsFile, String noGuiLogFile, MainAdapterClient client) {
//       //super(adapterName, "", client, GOParameters.getInstance(serParamsFile, false), false);
//       super(adapterName, "", client, GOParameters.getInstance(serParamsFile, serParamsFile==null), false, noGuiLogFile);
//   }
}