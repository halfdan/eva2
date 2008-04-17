package eva2.server.modules;

import eva2.gui.GenericObjectEditor;

import wsi.ra.jproxy.MainAdapterClient;

/** This the DE modul adapter necessary to access this implementation
 * form the JavaEvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:54:55
 * To change this template use File | Settings | File Templates.
 */
public class DEModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {
    public static String          m_Name = "Differential_Evolution";
    
    public DEModuleAdapter(String adapterName, MainAdapterClient client) {
    	super (adapterName, "DE.html", client, DEParameters.getInstance(), true);
    }
    
    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
    	return m_Name;
    }

}