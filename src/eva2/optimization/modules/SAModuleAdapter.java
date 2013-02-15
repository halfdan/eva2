package eva2.optimization.modules;

/**
 * This the SA module adapter necessary to access this implementation from the
 * EvA top level.
 */
public class SAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Simulated_Annealing";

    /**
     * Constructor of the ModulAdapter.
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public SAModuleAdapter(String adapterName) {
        super(adapterName, "SA.html", SAParameters.getInstance(), true);
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
