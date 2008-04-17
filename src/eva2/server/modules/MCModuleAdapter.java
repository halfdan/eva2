package eva2.server.modules;

import wsi.ra.jproxy.MainAdapterClient;

/** This the MC modul adapter necessary to access this implementation
 * form the JavaEvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:00:48
 * To change this template use File | Settings | File Templates.
 */
public class MCModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {
    public static String          m_Name = "Monte_Carlo_Search";

    /** Constructor of the Moduladapter
     * @param AdapterName   The AdapterName
     * @param Client        The client to serve
     */
    public MCModuleAdapter(String adapterName, MainAdapterClient client) {
        super (adapterName, "MC.html", client, MCParameters.getInstance(), true);
    }


    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}