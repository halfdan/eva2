package eva2.server.modules;


/**
 * This the PSO module adapter necessary to access this implementation from the EvA top level.
 */
public class PSOModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Particle_Swarm_Optimization";

    /**
     * Constructor of the ModulAdapter.
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public PSOModuleAdapter(String adapterName) {
        super(adapterName, "PSO.html", PSOParameters.getInstance(), true);
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