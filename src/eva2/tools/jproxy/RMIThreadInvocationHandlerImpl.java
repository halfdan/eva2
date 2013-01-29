package eva2.tools.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.2 $
 *            $Date: 2004/04/15 12:28:34 $
 *            $Author: ulmerh $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import eva2.tools.math.RNG;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class RMIThreadInvocationHandlerImpl extends UnicastRemoteObject implements RMIThreadInvocationHandler {
  public static final boolean TRACE = false;
  private Object m_Object;
  private String m_AdapterName;
  private Object m_Wrapper;
  private Method[] m_list;
  private String m_hostname;
  /**
   *
   * @return
   */
    @Override
  public String getServerName() {
    return m_hostname;
  }
  /**
   *
   */
  public RMIThreadInvocationHandlerImpl(Object obj)  throws RemoteException {
    try {
      m_hostname = InetAddress.getLocalHost().getHostName();
    }
    catch (Exception e) {
      System.out.println("ERROR getting HostName (RMIThreadInvokationHandlerImpl(obj))" + e.getMessage());
    }

    m_AdapterName = obj.getClass().getName()+"_"+RNG.randomInt(0,10000);
    try {
      m_Object = ThreadProxy.newInstance(obj);
      m_list = m_Object.getClass().getMethods();
       if (TRACE) {
            System.out.println(" --> rebind : "+m_AdapterName+" RMIThreadInvokationHandlerImpl of object "+obj.getClass().getName());
        }
      Naming.rebind(m_AdapterName,this);
    } catch (Exception e) {
      System.out.println(" Naming.rebind --> ERROR" + e.getMessage());
      e.printStackTrace();
    }
  }
  /**
   *
   */
  public RMIThreadInvocationHandlerImpl(Object obj,String AdapterName)  throws RemoteException {
    try {
      m_hostname = InetAddress.getLocalHost().getHostName();
    }
    catch (Exception e) {
      System.out.println("ERROR getting HostName (RMIThreadInvokationHandlerImpl(obj,adapt))" + e.getMessage());
    }

    m_AdapterName = AdapterName;
    try {
       m_Object = ThreadProxy.newInstance(obj);
       m_list = m_Object.getClass().getMethods();
      if (TRACE) {
            System.out.println(" -----> rebind : "+m_AdapterName+" "+this.getClass().getName()+" of object "+obj.getClass().getName());
        }
      Naming.rebind(m_AdapterName,this);
    } catch (Exception e) {
      System.out.println(" Naming.rebind --> ERROR" + e.getMessage());
    }
  }
  /**
   *
   */
    @Override
  public void setWrapper(Object Wrapper)  throws RemoteException {
    m_Wrapper = Wrapper;
  }
  /**
   *
   */
    @Override
  public Object getWrapper () throws RemoteException {
    return m_Wrapper;
  }
  /**
   *
   */
    @Override
  public Object invoke (String m, Object[] args) throws RemoteException {
    Object ret=null;
    String Name = "";
    if (TRACE) {
          Name = Thread.currentThread().getName();
      }
    try {
      if (TRACE) {
            System.out.println( Name+" Before invoke on server :" +m);
        }
      //Method[] list = m_Object.getClass().getMethods();
      boolean invoked = false;
      for (int i=0;i<m_list.length;i++) {
        //if (TRACE)  System.out.println(Name+" list[i].getName() "+list[i].getName());
        if (m.startsWith("free") == true) {
//          System.out.println("called free !!!!!!!!!!!!!!!!!!!!!!!!!" + m_AdapterName);
          String[] list = Naming.list("rmi://localhost:" + MainAdapterImpl.PORT);
//          System.out.println("-->list :" + list.length);
          for (int j = 0; j < list.length; j++) {
//            System.out.println("list=" + list[j] + "--" + m_AdapterName);
            if (list[j].lastIndexOf(m_AdapterName) != -1) {
//              System.out.println("unbind !!!!!!!!!!!!!!!!!!!!!!!!!!");
              Naming.unbind(list[j]);
              return null;
            }

          }
          System.out.println("Object not found !!!!!");
          return null;
        }

        if (m.equals(m_list[i].getName())==true) {
          //if (TRACE)System.out.println(Name+" find "+m);
          //if (args==null) System.out.println(Name+" args==null ");
          ret = m_list[i].invoke(m_Object,args);
          invoked= true;
          break;
        }
      }
      if (invoked==false) {
            System.out.println(Name+ " No memberfunction found !!!!!!!!!!!!!!!!!");
        }
    }
    catch ( InvocationTargetException e) {
      e.printStackTrace();
      System.out.println(Name+" RMIThreadInvokationHandlerImpl InvocationTargetException "+e.getMessage());
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println(Name+" Exception :"+e.getMessage());
    }
    return ret;
  }
}
