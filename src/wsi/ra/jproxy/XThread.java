package wsi.ra.jproxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.io.Serializable;

/*
 *  ==========================================================================*
 *  CLASS DECLARATION
 *  ==========================================================================
 */
public class XThread extends Thread implements Serializable {
  private Object m_Object;
  private volatile Method m_Method;
  private volatile Object[] m_Para;
  //private static byte m_instances = 0;
  private static XThread[] m_Instances;
  private static int m_MAXinstances = 10;
  private static int m_index = 0;
  public static boolean TRACE = false;

  public static void init(int x) {
    m_MAXinstances = x;
    m_Instances = new XThread[m_MAXinstances];
    m_index = 0;
  }

//  private static void instup() {
//    m_instances++;
//  }
//
//  private static void instdown() {
//    m_instances--;
//  }

  public static int get() {
    int ret =0;
    if (m_Instances==null) return ret;
    for (int i=0;i<m_Instances.length;i++) {
      if (m_Instances[i]==null) continue;
      if (m_Instances[i].isAlive() == true)
        ret++;
    }
    return ret;
  }

  /**
   *
   */
  public static XThread getXThread(Object x, Method m, Object[] Para, int MAXinstances) {
	  if (TRACE) System.out.println("getXThread2 CALLLED");
    //System.out.println("waiting "+m_instances+ " on "+x.hashCode()+ " m "+m.getName()+" m_MAXinstances " +MAXinstances);
    XThread ret = null;
    if (m_Instances == null)
      init(MAXinstances);
    if (m_index >= m_Instances.length)
      m_index = 0;
    if (m_Instances[m_index] == null) {
      ret = new XThread(x, m, Para);
      m_Instances[m_index] = ret;
      m_index++;
      return ret;
    }
    int w = 1;
    while (true) {
      for (int i = 0; i < m_Instances.length; i++) {
        if (m_Instances[i] == null) {
          m_index = i;
          ret = new XThread(x, m, Para);
          m_Instances[m_index] = ret;
          m_index++;
          return ret;
        }
        if (m_Instances[i].isAlive() == false) {
          m_index = i;
          ret = new XThread(x, m, Para);
          m_Instances[m_index] = ret;
          m_index++;
          return ret;

        }
      }
      try {
        Thread.sleep(w);
        w = 2 * w; //System.out.println(""+i);
      } catch (Exception e) {
        System.err.println("Error in sleep of XThread");
      }

    } // end of while true

//    int i=1;
//    while (m_instances >= MAXinstances) {
//    //      if (i>200)
//    //     System.out.println(i+ " waiting "+m_instances+ " on "+x.hashCode()+ " m "+m.getName()+" m_MAXinstances " +MAXinstances);
//    //       pleasewait();
//      try {
//        Thread.sleep(i);
//        i=2*i; //System.out.println(""+i);
//      } catch (Exception e) {
//        System.out.println("Error in sleep of XThread");
//      }
//    }
//    instup();//m_instances++;
//    if (
//    XThread ret = new XThread(x, m, Para);
//    //     m_ThreadContainer.add(ret);
//    return ret;

  }

  /**
   *
   */
  public static XThread getXThread(Object x, String m, Object[] Para, int MAXinstances) {
	  if (TRACE) System.out.println("getXThread1 CALLLED");
//    while (m_instances >= MAXinstances) {
//      //System.out.println("waiting "+m_instances);
//      //pleasewait();
//      try {
//        Thread.sleep(50);
//      } catch (Exception e) {
//        System.out.println("Error in sleep of XThread");
//      }
//    }
//    instup(); //m_instances++;
//    //System.out.println("XThread ++"+m_instances+" m_MAXinstances " +m_MAXinstances);
//    XThread ret = new XThread(x, Method, Para);
//    //    m_ThreadContainer.add(ret);
//    return ret;
    XThread ret = null;
     if (m_Instances == null)
       init(MAXinstances);
     if (m_index >= m_Instances.length)
       m_index = 0;
     if (m_Instances[m_index] == null) {
       ret = new XThread(x, m, Para);
       m_Instances[m_index] = ret;
       m_index++;
       return ret;
     }
     int w = 1;
     while (true) {
       for (int i = 0; i < m_Instances.length; i++) {
         if (m_Instances[i] == null) {
           m_index = i;
           ret = new XThread(x, m, Para);
           m_Instances[m_index] = ret;
           m_index++;
           return ret;
         }
         if (m_Instances[i].isAlive() == false) {
           m_index = i;
           ret = new XThread(x, m, Para);
           m_Instances[m_index] = ret;
           m_index++;
           return ret;

         }
       }
       try {
         Thread.sleep(w);
         w = 2 * w; //System.out.println(""+i);
       } catch (Exception e) {
         System.err.println("Error in sleep of XThread");
       }

     } // end of while true

  }

  /**
   *
   */
//  public static synchronized void pleasewait() {
//    for (int i = 0; i < m_ThreadContainer.size(); i++) {
//      try {
//        if (((Thread) m_ThreadContainer.get(i)).isAlive()==false)
//          m_ThreadContainer.remove(i);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//        System.out.println("Error");
//      }
//    }
//  }

  private void removemyself() {
    for (int i = 0; i < m_Instances.length; i++) {
    	if (TRACE) System.out.println("TRYING TO REMOVE");
      if (this.m_Instances[i] == this) {
        this.m_Instances[i] = null;
        System.out.println("REMOVED THREAD");
        return;
      }
    }
    System.err.println("DANGER!!!!!!!!! XTHREAD ->NOT<- REMOVED");
  }
  /**
   *
   */
  private XThread(Object object, Method method, Object[] Para) {
    System.out.println("XTHREAD INSTANZIERT");
    m_Object = object;
    m_Para = Para;
    m_Method = method;
    start();
  }

  /**
   *
   */
  private XThread(Object x, String method, Object[] Para) {
    System.out.println("XTHREAD INSTANZIERT");
    m_Object = x;
    m_Para = Para;
    try {
      Method[] methods = x.getClass().getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getName().equals(method) == true) {
          m_Method = methods[i];
          break;
        }
      }
    } catch (Exception e) {
      System.out.println(" ERROR in XTHREAD +" + e.getMessage());
      e.printStackTrace();
    }
    start();
  }

  /**
   *
   */
  public void run() {
   System.out.println("XTHREAD CALLED RUN");
    if (m_Method != null) {
      //setPriority(Thread.MAX_PRIORITY);
      try {
        System.out.println("XTHREAD calling m_Method "+m_Method.getName());
        //System.out.print("--->");
        //this.setPriority(Thread.MAX_PRIORITY);
        m_Method.invoke(m_Object, m_Para);

        //instdown(); //m_instances--;
        //System.out.println("<--");
        //System.out.println("XThread --"+m_instances+" m_MAXinstances " +m_MAXinstances);
      } catch (Exception e) {
        System.out.println("ERROR +" + e.getMessage());
        e.printStackTrace();
      }
    } else {
      System.out.println("Warning Method == null !!!!! in ThreadWrapper");
    }
    removemyself();
  }


}
