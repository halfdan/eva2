package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;


/**
 * This the DE module adapter necessary to access this implementation from the EvA top level.
 */
public class DEModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Differential_Evolution";

    public DEModuleAdapter(String adapterName, MainAdapterClient client) {
        super(adapterName, "DE.html", client, DEParameters.getInstance(), true);
    }

    /**
     * This method returns the name of the ModulAdapters.
     *
     * @return The name
     */
    public static String getName() {
        return moduleName;
    }
}