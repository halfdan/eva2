package eva2.server.modules;

import wsi.ra.jproxy.MainAdapterClient;

/** This the HC module adapter necessary to access this implementation
 * from the EvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:17:02
 * To change this template use File | Settings | File Templates.
 */
public class HCModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    public static String          m_Name = "Hill_Climber";
    
    /** Constructor of the Moduladapter
     * @param AdapterName   The AdapterName
     * @param Client        The client to serve
     */
    public HCModuleAdapter(String adapterName, MainAdapterClient client) {
        super (adapterName, "HC.html", client, HCParameters.getInstance(), true);
    }

    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}