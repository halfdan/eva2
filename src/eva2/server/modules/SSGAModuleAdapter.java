package eva2.server.modules;

/**
 * This the SSGA module adapter necessary to access this implementation from the
 * EvA top level.
 */
public class SSGAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Steady_State_Genetic_Algorithm";

    /**
     * Constructor of the ModuleAdapter.
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public SSGAModuleAdapter(String adapterName) {
        super(adapterName, "SSGA.html", SSGAParameters.getInstance(), true);
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