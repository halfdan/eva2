package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/**
 * This the EP module adapter necessary to access this implementation from the EvA top level.
 */
public class EPModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Evolutionary_Programming";

    public EPModuleAdapter(String adapterName, MainAdapterClient client) {
        super(adapterName, "EP.html", client, EPParameters.getInstance(), true);
    }

    /**
     * This method returns the name of the ModulAdapter
     *
     * @return The name
     */
    public static String getName() {
        return moduleName;
    }
}