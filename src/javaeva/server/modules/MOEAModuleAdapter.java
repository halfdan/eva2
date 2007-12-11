package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.stat.StatisticsWithGUI;
import javaeva.server.stat.StatisticsParameter;
import javaeva.server.EvAServer;
import javaeva.gui.JTabbedModuleFrame;
import javaeva.gui.JModuleGeneralPanel;
import javaeva.gui.JParaPanel;

import java.io.Serializable;
import java.util.ArrayList;

import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.RMIProxyLocal;

/** This the MOEA modul adapter necessary to access this implementation
 * form the JavaEvA top level.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:54:55
 * To change this template use File | Settings | File Templates.
 */
public class MOEAModuleAdapter extends GenericModuleAdapter implements Serializable, ModuleAdapter  {

    public static String          m_Name = "Multi-Objective_Evolutionary_Algorithms";

    /** Constructor of the Moduladapter
     * @param AdapterName   The AdapterName
     * @param Client        The client to serve
     */
    public MOEAModuleAdapter(String adapterName, MainAdapterClient client) {
        super (adapterName, "MOEA.html", client, MOEAParameters.getInstance(), true);
    }

    /** This method returns the name of the ModulAdapters
     * @return The name
     */
    public static String getName() {
        return m_Name;
    }
}