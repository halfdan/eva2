package wsi.ra.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.1 $
 *            $Date: 2004/04/15 09:12:30 $
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

public class RMIProxyRemote implements InvocationHandler,Serializable {
  private static ComAdapter m_Adapter;
  private static boolean TRACE = true;
  private Object m_Object;
  private String m_ObjectName;
  private RMIInvocationHandler m_RMIHandler=null;
  private long m_counter=0;
  /**
   *
   */
  public static Object newInstance (Object c,MainAdapterClient Client) {
    return java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      new RMIProxyRemote(c,Client));
  }
  /**
   *
   */
  public static Object newInstance (Object c,String host) {
    return java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      new RMIProxyRemote(c,host));
  }
  /**
   *
   */
  public static Object newInstance (Object c) {
    return java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      new RMIProxyRemote(c));
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
  private RMIProxyRemote (Object c, String host) {
    m_ObjectName = c.getClass().getName();
    if (m_Adapter==null) m_Adapter = ComAdapter.getInstance();
    m_RMIHandler = m_Adapter.getRMIHandler(c,host);
    if (TRACE) System.out.println("creating RMIProxyRemote " + c.getClass() + " " + host);
  }
  /**
   *
   */
  private RMIProxyRemote (Object c, MainAdapterClient Client) {
    m_ObjectName = c.getClass().getName();
    try {
      m_RMIHandler = Client.getRMIHandler(c);
    } catch (Exception e) {
      System.out.println("RMIProxyRemote error ex "+e.getMessage());
      e.printStackTrace();
    }
    if (TRACE) System.out.println("creating RMIProxyRemote " + c.getClass() + " " + Client.getClass());
  }
  /**
   *
   */
  private RMIProxyRemote (Object c) {
    m_ObjectName = c.getClass().getName();
    if (m_Adapter==null) m_Adapter = ComAdapter.getInstance();
    m_RMIHandler = m_Adapter.getRMIHandler(c);
  }
  /**
   *
   */
   public Object invoke (Object proxy, Method m, Object[] args) throws Throwable {
    long start = System.currentTimeMillis();
    ++m_counter;
    //System.out.println("Before invoke:" +m.getName());
    Object ret = m_RMIHandler.invoke(m.getName(),args);
    long finish = System.currentTimeMillis();
    //System.out.println("Calling :"+m.getName()+" of "+m_ObjectName+ " time :"+(finish-start));
    return ret;
  }
  /**
   *
   */
//  public static void main(String[] args) {
//    Doit remotex = (Doit) RMIProxyRemote.newInstance(new DoitImpl(),"primergy4.informatik.uni-tuebingen.de");
//    remotex.doit();
//    remotex.doit();
//    remotex.doit();
//    remotex.doit();
//  }
}
