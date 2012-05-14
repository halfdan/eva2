package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/**
 * This the GA module adapter necessary to access this implementation from the EvA top level.
 */
public class GAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Genetic_Algorithm";

    public GAModuleAdapter(String adapterName, MainAdapterClient client) {
        super(adapterName, "GA.html", client, GAParameters.getInstance(), true);
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
