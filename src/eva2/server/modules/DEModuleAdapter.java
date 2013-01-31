package eva2.server.modules;

/**
 * This the DE module adapter necessary to access this implementation from the EvA top level.
 */
public class DEModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Differential_Evolution";

    public DEModuleAdapter(String adapterName) {
        super(adapterName, "DE.html", DEParameters.getInstance(), true);
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