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
public class RMIProxyLocalThread implements InvocationHandler,Serializable {
  private RMIInvocationHandler m_RMIHandler;
  private final static boolean TRACE = true; 
  /**
   *
   */
  public static Object newInstance (Object c,String RMIName) {
    //System.out.println("RMIProxyLocal.newInstance !!!!!!!!!!!!!!!");
    RMIProxyLocalThread Proxy = new RMIProxyLocalThread(c,RMIName);
    Object ret = java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      Proxy);
    Proxy.setWrapper(ret);
    if (TRACE) {
          System.out.println("RMIProxyLocalThread "+c.getClass() + " ret " + ret.getClass());
      }
    return ret;
  }
  /**
   *
   */
  public static Object newInstance (Object c) {
    RMIProxyLocalThread Proxy = new RMIProxyLocalThread(c);
    Object ret = java.lang.reflect.Proxy.newProxyInstance (
      c.getClass().getClassLoader(),
      c.getClass().getInterfaces(),
      Proxy);
    Proxy.setWrapper(ret);
    if (TRACE) {
          System.out.println("RMIProxyLocalThread "+c.getClass() + " ret " + ret.getClass());
      }
    return ret;
  }
  /**
   *
   */
  private RMIProxyLocalThread (Object c) {
     System.out.println("RMIProxyLocal:"+c.getClass().getName());
    try {
      m_RMIHandler = new RMIInvocationHandlerImpl(ThreadProxy.newInstance(c));
    } catch (Exception e) {
      System.out.println("Error in m_RMIHandler = new RMIInvokationHandlerImpl(c)");
      e.printStackTrace();
    }
  }
  /**
   *
   */
  private RMIProxyLocalThread (Object c,String RMIName) {
    System.out.println("RMIProxyLocal:"+RMIName);
    System.out.println("RMIProxyLocal:"+c.getClass().getName());

    try {
      m_RMIHandler = new RMIInvocationHandlerImpl(ThreadProxy.newInstance(c),RMIName);
    } catch (Exception e) {
      System.out.println("Error in m_RMIHandler = new RMIInvokationHandlerImpl(c)");
       e.printStackTrace();
    }
  }
  /**
   *
   */
  public void setWrapper(Object Wrapper) {
    try {
      m_RMIHandler.setWrapper(Wrapper);
    } catch (Exception e) {
      System.out.println("Error in setWrapper "+e.getMessage());
    }
  }

  /**
   *
   */
    @Override
   public Object invoke (Object proxy, Method m, Object[] args) throws Throwable {
    //long start = System.currentTimeMillis();
    //System.out.println("Before invoke:" +m.getName());
    //Object ret = m_RMIHandler.invoke(m.getName(),args);
    //long finish = System.currentTimeMillis();
    //System.out.println("Calling :"+m.getName()+" of "+m_ObjectName+ " time :"+(finish-start));
    //return ret;
    return m_RMIHandler.invoke(m.getName(),args);
  }
  /**
   *
   */
  public static void main(String[] args) {
      //
    //Doit x = new DoitImpl();
//    Doit xx = (Doit) RMIProxyRemote.newInstance(x,"primergy4.informatik.uni-tuebingen.de");
//    Doit2 yy = (Doit2) RMIProxyLocal.newInstance(new Doit2Impl());
//    System.out.println(" jhdfjldf");
//    yy.doit();
//    yy.doit();

//    xx.newdoit(yy);
  }
}
