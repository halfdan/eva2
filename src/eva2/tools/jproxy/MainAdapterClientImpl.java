package eva2.tools.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.1 $
 *            $Date: 2004/04/15 09:12:30 $
 *            $Author: ulmerh $
 */

import java.net.InetAddress;
/**
 *
 */
public class MainAdapterClientImpl implements MainAdapterClient {
  static final public String MAIN_ADAPTER_CLIENT_NAME = "Main_Remote_Object_Client_Name";
  static public boolean TRACE = false;
  private String m_HostName = "not__defined__";
  /**
   *
   */
  public MainAdapterClientImpl() {
    try {
      m_HostName  =  InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      System.out.println("ERROR getting HostName MainAdapterClientImpl "+e.getMessage());
    }
  }

  /**
  *
  */
 public MainAdapterClientImpl(String hostName) {
	 m_HostName = hostName;
 }
  
  /**
   *
   */
    @Override
  public RMIInvocationHandler getRMIHandler(Object obj) {
    if (TRACE) System.out.println("MainAdapterClientImpl.getRMIHandler");
    RMIInvocationHandler ret = null;
    try {
      ret = new RMIInvocationHandlerImpl(obj);
    } catch (Exception e) {
      System.out.println(" Error  ret= new RMIInvokationHandlerImpl(obj);"+e.getMessage());
    }
    return ret;
  }
  /**
   *
   */
    @Override
  public RMIThreadInvocationHandler getRMIThreadHandler(Object obj) {
    if (TRACE) System.out.println("MainAdapterClientImpl.getRMIThreadHandler");
    RMIThreadInvocationHandler ret = null;
    try {
      ret = new RMIThreadInvocationHandlerImpl(obj);
    } catch (Exception e) {
      System.out.println(" Error  ret= new getRMIThreadHandler(obj);"+e.getMessage());
    }
    return ret;
  }
  /**
   *
   */
    @Override
  public String getHostName () {
    return m_HostName;
  }
}
