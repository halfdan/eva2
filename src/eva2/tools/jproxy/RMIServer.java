package eva2.tools.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.1 $
 *            $Date: 2004/04/15 09:12:31 $
 *            $Author: ulmerh $
 */

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

/**
 *
 */
public class RMIServer {
  /* Version string of the server application. */
  public static boolean TRACE = false;
//  public static Registry m_Registry;
  protected static RMIServer m_instance;
  /* Name of host on which the server is running. */
  private String m_MyHostName = "undefined";
  /* IP of host on which the server is running. */
  private String m_MyHostIP = "undefined";
  /* MainAdapterImp object. This is need for the first
    connection between the server and the client program.  */
  public MainAdapter m_MainRemoteObject;
  /* String describing the properties of the enviroment. */
//  private ComAdapter m_ComAdapter;
  public static String m_UserName;
  public static int m_NumberOfVM = 0;
  Registry m_Registry = null;
  
  /**
   *
   */
  public static RMIServer getInstance() {
	  if (m_instance == null) {
		  m_instance = new RMIServer();
	  }
	  return m_instance;
  }
  /**
   * Constructor of EvAServer.
   * Calls RMIConnection().
   */
  protected RMIServer() {
    m_UserName = System.getProperty("user.name");
    //System.out.println(EVAHELP.getSystemPropertyString());
    initConnection();
//    m_ComAdapter = ComAdapter.getInstance();
  }

  /**
   * Main method of this class.
   * Is the starting point of the server application.
   */
  static public void main(String[] args) {
    System.out.println("Start RMIServer !");
    RMIServer Application = RMIServer.getInstance();
  }


  /**
   * Launchs the RMIRegistry and makes the registration
   * of the MainAdapterImpl class at the rmiregistry.
   * @param
   */
  private void initConnection() {
    String MainAdapterName = m_UserName + MainAdapterImpl.MAIN_ADAPTER_NAME;
    System.setProperty("java.security.policy", "server.policy");
    launchRMIRegistry();
    try {
        m_MyHostIP = InetAddress.getLocalHost().getHostAddress();
        m_MyHostName = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      System.out.println("ERROR getting HostName (RMIServer.initConnection)" + e.getMessage());
    }
    System.out.println("Start of EvA RMI-Server on host " + m_MyHostName + " = " + m_MyHostIP);
//    Object test = null;
    try {
      try {
        String[] list =
                Naming.list("rmi://localhost:" + MainAdapterImpl.PORT);
//        System.out.println("-->list");
//        for (int i = 0; i < list.length; i++)
//          System.out.println("-->RMIName" + list[i]);
        m_NumberOfVM = getNumberOfVM(list);
      } catch (RemoteException e) {
    	  System.err.println("no RMI registry available yet...");
        if (TRACE)
          System.out.println(
                  "RemoteException OK IAM the first server for this rmiregistry: "
                  + e.getMessage());
      }
    } catch (MalformedURLException ex) {
      System.out.println(
              "MalformedURLException: Error while looking up "
              + ex.getMessage());
    }
    createMainRemoteObject(MainAdapterName);
    
    System.out.println("End of RMI-Server Initialisation");
    System.out.println(" --> OK on Host: " + m_MyHostName + " = " + m_MyHostIP + ", adapter name is " + MainAdapterName);
    System.out.println("Waiting for a client ..............");
  }

  protected void createMainRemoteObject(String mainAdapterName) {
	  try {
		  m_MainRemoteObject = new MainAdapterImpl();
		  m_MainRemoteObject =
			  (MainAdapter) RMIProxyLocal.newInstance(m_MainRemoteObject,mainAdapterName + "_" + m_NumberOfVM);
		  m_MainRemoteObject.setRemoteThis(m_MainRemoteObject);
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
  
  /**
   *
   */
  public MainAdapter getMainRemoteObject() {
    return m_MainRemoteObject;
  }

  /**
   * Install RMIregistry on default port !!
   */
  private void launchRMIRegistry() {
    if (TRACE)
      System.out.println(
              "LaunchRMIRegistry on Server on PORT " + MainAdapterImpl.PORT);
    try {
      m_Registry =
              java.rmi.registry.LocateRegistry.createRegistry(
              MainAdapterImpl.PORT);
    } catch (Throwable e) {
      if (TRACE)
        System.out.println("Registry not created !!" + e.getMessage());
      m_Registry = null;
    }
    if (m_Registry == null) {
      System.out.println(
              "Try to get registry with getRegistry on PORT "
              + MainAdapterImpl.PORT);
      try {
        m_Registry =
                java.rmi.registry.LocateRegistry.getRegistry(
                MainAdapterImpl.PORT);
        if (TRACE)
          System.out.println(
                  "m_Registry.REGISTRY_PORT=" + m_Registry.REGISTRY_PORT);
      } catch (Throwable e) {
        if (TRACE)
          System.out.println(
                  "registry notcreated !!" + e.getMessage());
        m_Registry = null;
      }
    }
    if (m_Registry == null) {
    	System.err.println("--> got no RMIREGISTRY");
    } else if (TRACE) System.out.println("--> got RMIREGISTRY");
  }
  
  /**
   *
   */
  private int getNumberOfVM(String[] list) {
    int ret = 0;
    for (int i = 0; i < list.length; i++) {
      if (list[i].indexOf(MainAdapterImpl.MAIN_ADAPTER_NAME) != -1)
        ret++;
    }
    if (TRACE)
      System.out.println(" getNumberOfVM()  NumberOfVM =" + ret);
    return ret;
  }
}


