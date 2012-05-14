package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/**
 * This the SA module adapter necessary to access this implementation from the EvA top level.
 */
public class SAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Simulated_Annealing";

    /** Constructor of the Moduladapter
     * @param AdapterName   The AdapterName
     * @param Client        The client to serve
     */    
    
    public SAModuleAdapter(String adapterName, MainAdapterClient client) {
         super (adapterName, "SA.html", client, SAParameters.getInstance(), true);
     }
    
    /** 
     * This method returns the name of the ModulAdapter
     * @return The name
     */
    public static String getName() {
        return moduleName;
    }
}
