package eva2.server.modules;

import wsi.ra.jproxy.MainAdapterClient;

/** This the PSO modul adapter necessary to access this implementation
 * form the JavaEvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.11.2004
 * Time: 17:58:19
 * To change this template use File | Settings | File Templates.
 */
public class PSOModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    public static String          m_Name = "Particle_Swarm_Optimization";
    
    /** Constructor of the Moduladapter
     * @param AdapterName   The AdapterName
     * @param Client        The client to serve
     */
    public PSOModuleAdapter(String adapterName, MainAdapterClient client) {
        super (adapterName, "PSO.html", client, PSOParameters.getInstance(), true);
    }
    
    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}