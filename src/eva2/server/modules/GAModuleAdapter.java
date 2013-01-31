package eva2.server.modules;


/**
 * This the GA module adapter necessary to access this implementation from the EvA top level.
 */
public class GAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Genetic_Algorithm";

    public GAModuleAdapter(String adapterName) {
        super(adapterName, "GA.html", GAParameters.getInstance(), true);
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
