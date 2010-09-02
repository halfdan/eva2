package eva2.server.modules;

import java.io.Serializable;

import eva2.server.go.InterfaceGOParameters;
import eva2.tools.jproxy.MainAdapterClient;


/**
 * Starts a statistics GUI and the GOProcessor thread.
 * 
 * User: streiche
 * Date: 11.05.2003
 * Time: 13:08:38
 * To change this template use Options | File Templates.
 */
public class GOModuleAdapter extends GenericModuleAdapter implements ModuleAdapter, Serializable {
  public static String      m_Name = "Genetic_Optimization";

  /**
   *
   */
  public static String getName() {
    return m_Name;
  }

  /**
   * Starts a statistics GUI and the GOProcessor thread.
   * 
   * @param AdapterName	the title of the ModulAdapter
   * @param Client	the client instance
   */
  public GOModuleAdapter(String adapterName, MainAdapterClient client) {
       super(adapterName, "", client, GOParameters.getInstance(), false);
   }  
  
  /**
   * Starts a statistics GUI and the GOProcessor thread with a given GOParameters file.
   * 
   * @param AdapterName	the title of the ModulAdapter
   * @param Client	the client instance
   */
  public GOModuleAdapter(String adapterName, InterfaceGOParameters goParams, String noGuiLogFile, MainAdapterClient client) {
       //super(adapterName, "", client, GOParameters.getInstance(serParamsFile, false), false);
       super(adapterName, "", client, goParams, false, noGuiLogFile);
   }
  
//  /**
//   * Starts a statistics GUI and the GOProcessor thread with a given GOParameters file.
//   * 
//   * @param AdapterName	the title of the ModulAdapter
//   * @param Client	the client instance
//   */
//  public GOModuleAdapter(String adapterName, String serParamsFile, String noGuiLogFile, MainAdapterClient client) {
//       //super(adapterName, "", client, GOParameters.getInstance(serParamsFile, false), false);
//       super(adapterName, "", client, GOParameters.getInstance(serParamsFile, serParamsFile==null), false, noGuiLogFile);
//   }
}