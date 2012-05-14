package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/**
 * This the MC module adapter necessary to access this implementation from the EvA top level.
 */
public class MCModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Monte_Carlo_Search";

    /**
     * Constructor of the ModuleAdapter.
     *
     * @param adapterName The AdapterName
     * @param client The client to serve
     */
    public MCModuleAdapter(String adapterName, MainAdapterClient client) {
        super(adapterName, "MC.html", client, MCParameters.getInstance(), true);
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