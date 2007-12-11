package javaeva.server.modules;

import wsi.ra.jproxy.MainAdapterClient;

/** This the SSGA modul adapter necessary to access this implementation
 * form the JavaEvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 15:44:53
 * To change this template use File | Settings | File Templates.
 */
public class SSGAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    public static String          m_Name = "Steady_State_Genetic_Algorithm";

    /** Constructor of the Moduladapter
     * @param AdapterName   The AdapterName
     * @param Client        The client to serve
     */	
    public SSGAModuleAdapter(String adapterName, MainAdapterClient client) {
 		super (adapterName, "SSGA.html", client, SSGAParameters.getInstance(), true);
 	}
    
    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}