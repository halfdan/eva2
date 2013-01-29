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
import java.lang.reflect.Proxy;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class ThreadProxy implements InvocationHandler,
                                    Serializable {
  private Object m_Object;
  private ThreadWrapper m_ThreadWrapper;
  private int m_maxthreads = 8;
 /**
   *
   */
  public static Object newInstance (Object obj) {
      return Proxy.newProxyInstance(
          obj.getClass().getClassLoader(),
          obj.getClass().getInterfaces(),
          new ThreadProxy(obj));
  }
  /**
   *
   */
  public static Object newInstance (Object obj,int maxthreads) {
      return Proxy.newProxyInstance(
          obj.getClass().getClassLoader(),
          obj.getClass().getInterfaces(),
          new ThreadProxy(obj,maxthreads));
  }

  /**
   *
   */
  public ThreadProxy (Object obj) {
    m_Object = obj;
  }
  /**
   *
   */
  public ThreadProxy (Object obj,int maxthreads) {
    m_Object = obj;
    m_maxthreads = maxthreads;
  }

  /**
   *
   */
    @Override
   public Object invoke (Object proxy,Method method,Object[] args) throws Throwable {
    Class rettype = method.getReturnType();
    if (rettype.equals(Void.TYPE)== true) {
      if (m_ThreadWrapper == null) {
        m_ThreadWrapper = new ThreadWrapper(m_maxthreads);
      }
      else {
        m_ThreadWrapper.pleasewait();
      }
      m_ThreadWrapper.invoke(m_Object,method,args);
      return null;
    }
    Object ret = null;
    try {
      if (method.getName().equals("isAlive")) {

        if (m_ThreadWrapper != null) {
          Boolean rret = new Boolean(m_ThreadWrapper.isAlive());
          // System.out.println("calling is alive" +rret);
          return rret;
        }
      }
      if (m_ThreadWrapper != null) {
            m_ThreadWrapper.pleasewait();
        }
      ret = method.invoke(m_Object, args);
    } catch (Exception e) {
      System.out.println("ERROR +" + e.getMessage());
      e.printStackTrace();
    }
    return ret;
  }
}
