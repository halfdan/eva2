package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/**
 * This the HC module adapter necessary to access this implementation from the EvA top level.
 */
public class HCModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Hill_Climber";

    /**
     * Constructor of the ModuleAdapter
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public HCModuleAdapter(String adapterName, MainAdapterClient client) {
        super(adapterName, "HC.html", client, HCParameters.getInstance(), true);
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