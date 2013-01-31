package eva2.server.modules;


/**
 * This the PBIL module adapter necessary to access this implementation from the EvA top level.
 */
public class PBILModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Population_Based_Incremental_Learning";

    /**
     * Constructor of the ModuleAdapter
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public PBILModuleAdapter(String adapterName) {
        super(adapterName, "PBIL.html", PBILParameters.getInstance(), true);
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