package eva2.server.modules;

import wsi.ra.jproxy.MainAdapterClient;

/** This the GA module adapter necessary to access this implementation
 * from the EvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:29:20
 * To change this template use File | Settings | File Templates.
 */
public class GAModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    public static String          m_Name = "Genetic_Algorithm";

    public GAModuleAdapter(String adapterName, MainAdapterClient client) {
        super (adapterName, "GA.html", client, GAParameters.getInstance(), true);
    }

    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}