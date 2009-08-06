package eva2.tools.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.2 $
 *            $Date: 2004/04/28 07:50:33 $
 *            $Author: ulmerh $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class RMIProxyRemoteThread implements InvocationHandler,
                                             Serializable {
  private static ComAdapter m_Adapter;
  private RMIThreadInvocationHandler m_RMIThreadHandler;
  /**
   *
   */
  public static Object newInstance (Object c,MainAdapterClient Client) {
    return java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      new RMIProxyRemoteThread(c,Client));
  }
  /**
   *
   */
  public static Object newInstance (Object c,String host) {
    return java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      new RMIProxyRemoteThread(c,host));
  }
  
  private static void maybeLoadAdapter() {
	  if (m_Adapter==null) 
		  m_Adapter = ComAdapter.getInstance();
  }
  
  /**
   *
   * @param c
   * @return
   */
  public static String[] getServerList() {
	  maybeLoadAdapter();
      m_Adapter.evalAvailableHostNameList();
    return m_Adapter.getAvailableHostNameList();

  }
  public static void setServerList(String[] servers ) {
	  maybeLoadAdapter();

    m_Adapter.setServerList(servers);
     m_Adapter.evalAvailableHostNameList();

  }

  /**
   *
   */
  public static Object newInstance (Object c) {
    return java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      new RMIProxyRemoteThread(c));
  }
  /**
   *
   */
  public static void setComAdaper(ComAdapter x) {
    m_Adapter = x;
  }
  /**
   *
   */
  private RMIProxyRemoteThread (Object c, String host) {
//    m_ObjectName = c.getClass().getName();
    maybeLoadAdapter();
    m_RMIThreadHandler = m_Adapter.getRMIThreadHandler(c,host);
  }
  /**
   *
   */
  private RMIProxyRemoteThread (Object c, MainAdapterClient Client) {
//    m_ObjectName = c.getClass().getName();
    try {
      m_RMIThreadHandler = Client.getRMIThreadHandler(c);
    } catch (Exception e) {
      System.out.println("RMIProxyRemoteThreaderror ex "+e.getMessage());
      e.printStackTrace();
    }
  }
  /**
   *
   */
  private RMIProxyRemoteThread (Object c) {
//    m_ObjectName = c.getClass().getName();
    maybeLoadAdapter();
    m_RMIThreadHandler = m_Adapter.getRMIThreadHandler(c);
  }
  /**
   *
   */
   public Object invoke (Object proxy, Method m, Object[] args) throws Throwable {
    //long start = System.currentTimeMillis();
    //System.out.println("Before invoke:" +m.getName());
    Object ret = null;
    if (m.getName().equals("getServerName")) {
      ret = m_RMIThreadHandler.getServerName();
    } else {
      ret = m_RMIThreadHandler.invoke(m.getName(), args);
    }
    //long finish = System.currentTimeMillis();
    //System.out.println("Calling :"+m.getName()+" of "+m_ObjectName+ " time :"+(finish-start));
    return ret;
  }
  /**
   *
   */
//  public static void main(String[] args) {
//    Doit remotex = (Doit) RMIProxyRemoteThread.newInstance(new DoitImpl(),
//      "ranode1.informatik.uni-tuebingen.de");
//    remotex.doit();
//    remotex.doit();
//    remotex.doit();
//    remotex.doit();
//  }
}
