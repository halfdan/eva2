package eva2.tools.jproxy;

import java.io.Serializable;
import java.lang.reflect.Method;

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
  }

  /**
   *
   */
  public static XThread getXThread(Object x, String m, Object[] Para, int MAXinstances) {
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

  private void removemyself() {
    for (int i = 0; i < m_Instances.length; i++) {
      if (this.m_Instances[i] == this) {
        this.m_Instances[i] = null;
        return;
      }
    }
    System.err.println("DANGER!!!!!!!!! XTHREAD ->NOT<- REMOVED");
  }
  /**
   *
   */
  private XThread(Object object, Method method, Object[] Para) {
    m_Object = object;
    m_Para = Para;
    m_Method = method;
    start();
  }

  /**
   *
   */
  private XThread(Object x, String method, Object[] Para) {
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
      System.err.println(" ERROR in XTHREAD +" + e.getMessage());
      e.printStackTrace();
    }
    start();
  }

  /**
   *
   */
    @Override
  public void run() {
    if (m_Method != null) {
      //setPriority(Thread.MAX_PRIORITY);
      try {
        m_Method.invoke(m_Object, m_Para);
      } catch (Exception e) {
        System.err.println("ERROR +" + e.getMessage());
        e.printStackTrace();
      }
    } else {
      System.err.println("Warning Method == null !!!!! in ThreadWrapper");
    }
    removemyself();
  }


}
