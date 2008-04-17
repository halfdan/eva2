package eva2.server.modules;

import wsi.ra.jproxy.MainAdapterClient;

/** This the SA modul adapter necessary to access this implementation
 * form the JavaEvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:25:00
 * To change this template use File | Settings | File Templates.
 */
public class SAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    public static String          m_Name = "Simulated_Annealing";

    /** Constructor of the Moduladapter
     * @param AdapterName   The AdapterName
     * @param Client        The client to serve
     */    
    
    public SAModuleAdapter(String adapterName, MainAdapterClient client) {
         super (adapterName, "SA.html", client, SAParameters.getInstance(), true);
     }
    
    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}