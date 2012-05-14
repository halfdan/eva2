package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/**
 * This the PSO module adapter necessary to access this implementation from the EvA top level.
 */
public class PSOModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Particle_Swarm_Optimization";

    /**
     * Constructor of the Moduladapter
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public PSOModuleAdapter(String adapterName, MainAdapterClient client) {
        super(adapterName, "PSO.html", client, PSOParameters.getInstance(), true);
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