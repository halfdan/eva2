package javaeva.server.modules;

import wsi.ra.jproxy.MainAdapterClient;

/** This the PBIL modul adapter necessary to access this implementation
 * form the JavaEvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:52:22
 * To change this template use File | Settings | File Templates.
 */
public class PBILModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

	public static String          m_Name = "Population_Based_Incremental_Learning";

	/** Constructor of the Moduladapter
	 * @param AdapterName   The AdapterName
	 * @param Client        The client to serve
	 */    
	public PBILModuleAdapter(String adapterName, MainAdapterClient client) {
		super (adapterName, "PBIL.html", client, PBILParameters.getInstance(), true);
	}

	/** This method returns the name of the ModulAdapters
	 * @return The name
	 */
	public static String getName() {
		return m_Name;
	}
}