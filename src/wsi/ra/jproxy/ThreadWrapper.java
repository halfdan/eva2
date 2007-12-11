package wsi.ra.jproxy;

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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.io.Serializable;

/*
 *  ==========================================================================*
 *  CLASS DECLARATION
 *  ==========================================================================
 */
public class ThreadWrapper extends ArrayList {
  private int m_counter = 0;
  private ArrayList m_ThreadContainer = new ArrayList();
  private static int m_numberofMAXThreads = 4;
  /**
   *
   */
  public ThreadWrapper(int number) {
    m_numberofMAXThreads = number;
  }

  /**
   *
   */
  public ThreadWrapper() {
  }

  /**
   *
   */
  public synchronized void invoke(Object x, String Method, Object[] Para) {
    m_ThreadContainer.add(XThread.getXThread(x, Method, Para, m_numberofMAXThreads));
//    System.out.println("ADDED to THREADWRAPPER LIST" + m_ThreadContainer.size());
  }

  /**
   *
   */
  public synchronized void invoke(Object x, Method m, Object[] Para) {
    m_ThreadContainer.add(XThread.getXThread(x, m, Para, m_numberofMAXThreads));
//  System.out.println("ADDED to THREADWRAPPER LIST" + m_ThreadContainer.size());
  }

  /**
   *
   */
  public synchronized void pleasewait() {
    for (int i = 0; i < m_ThreadContainer.size(); i++) {
      try {
        ((Thread) m_ThreadContainer.get(i)).join();
        m_ThreadContainer.remove(i); // testhu
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.out.println("Error");
      }
    }
  }

  /**
   *
   */
  public synchronized boolean isAlive() {
    for (int i = 0; i < m_ThreadContainer.size(); i++) {
      boolean alive = ((Thread) m_ThreadContainer.get(i)).isAlive();
      if (alive == true)
        return true;
    }
    return false;
  }
}
