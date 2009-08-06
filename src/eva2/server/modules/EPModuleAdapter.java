package eva2.server.modules;

import eva2.tools.jproxy.MainAdapterClient;

/** This the EP module adapter necessary to access this implementation
 * from the EvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:54:55
 * To change this template use File | Settings | File Templates.
 */
public class EPModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    public static String          m_Name = "Evolutionary_Programming";
    
    public EPModuleAdapter(String adapterName, MainAdapterClient client) {
        super (adapterName, "EP.html", client, EPParameters.getInstance(), true);
    }
    
    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}