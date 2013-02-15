package eva2.optimization.modules;

import java.io.Serializable;


/**
 * This the MOEA module adapter necessary to access this implementation from the EvA top level.
 */
public class MOEAModuleAdapter extends GenericModuleAdapter implements Serializable, ModuleAdapter {

    private static final String moduleName = "Multi-Objective_Evolutionary_Algorithms";

    /**
     * Constructor of the ModuleAdapter.
     *
     * @param AdapterName The AdapterName
     * @param Client The client to serve
     */
    public MOEAModuleAdapter(String adapterName) {
        super(adapterName, "MOEA.html", MOEAParameters.getInstance(), true);
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
