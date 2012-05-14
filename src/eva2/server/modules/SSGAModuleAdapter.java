package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/**
 * This the SSGA module adapter necessary to access this implementation from the EvA top level.
 */
public class SSGAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Steady_State_Genetic_Algorithm";

    /**
     * Constructor of the ModuleAdapter
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public SSGAModuleAdapter(String adapterName, MainAdapterClient client) {
        super(adapterName, "SSGA.html", client, SSGAParameters.getInstance(), true);
    }

    /**
     * This method returns the name of the ModulAdapters
     *
     * @return The name
     */
    public static String getName() {
        return moduleName;
    }
}