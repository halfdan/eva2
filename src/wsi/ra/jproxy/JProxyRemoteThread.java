package wsi.ra.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.1 $
 *            $Date: 2004/04/15 09:12:29 $
 *            $Author: ulmerh $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.io.Serializable;
import java.util.ArrayList;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class JProxyRemoteThread implements InvocationHandler,
        Serializable {
  private static ComAdapter m_ComAdapter;
  private Object m_Object;
  private RMIThreadInvocationHandler m_RMIThreadHandler;
  private String m_ObjectName;
  /**
   *
   */
  public static Object newInstance(Serializable object, String host) throws NO_RMIServerAvailable {
    return Proxy.newProxyInstance(
            object.getClass().getClassLoader(),
            object.getClass().getInterfaces(),
            new JProxyRemoteThread(object, host));
  }

  /**
   *
   */
  public static Object newInstance(Serializable object) throws NO_RMIServerAvailable {
    return Proxy.newProxyInstance(
            object.getClass().getClassLoader(),
            object.getClass().getInterfaces(),
            new JProxyRemoteThread(object));
  }

  /**
   *
   * @param c
   * @return
   */
  public static String[] getServerList() {
    if (m_ComAdapter == null) {
      //try {
      m_ComAdapter = ComAdapter.getInstance();
//      } catch (NO_RMIServerAvailable ex) {
//        ex.printStackTrace();
//
//      }
    }
    m_ComAdapter.evalAvailableHostNameList();
    return m_ComAdapter.getAvailableHostNameList();

  }

  /**
   *
   */
  public static void setComAdaper(ComAdapter x) {
    m_ComAdapter = x;
  }


  /**
   *
   */
  private JProxyRemoteThread(Serializable object, String host) throws NO_RMIServerAvailable {
    if (m_ComAdapter == null)
      m_ComAdapter = ComAdapter.getInstance();
    m_RMIThreadHandler = m_ComAdapter.getRMIThreadHandler(object, host);
  }

  /**
   *
   */
  private JProxyRemoteThread(Serializable object) throws NO_RMIServerAvailable {
    if (m_ComAdapter == null)
      m_ComAdapter = ComAdapter.getInstance();
    m_RMIThreadHandler = m_ComAdapter.getRMIThreadHandler(object);
  }

  /**
   *
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object ret = null;
    if (method.getName().equals("getServerName")) {
      ret = m_RMIThreadHandler.getServerName();
    } else {
      ret = m_RMIThreadHandler.invoke(method.getName(), args);
    }

    //long finish = System.currentTimeMillis();
    //System.out.println("Calling :"+m.getName()+" of "+m_ObjectName+ " time :"+(finish-start));
    return ret;
  }
}
