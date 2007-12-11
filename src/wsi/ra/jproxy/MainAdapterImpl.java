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
import java.io.BufferedReader;
import java.io.InputStreamReader;

/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class MainAdapterImpl implements MainAdapter {
  static final public String MAIN_ADAPTER_NAME = "MainRemoteObjectName";
  static final public int PORT = 1099;
  static public boolean TRACE = false;
  private String m_Buf = "";
  private MainAdapter m_RemoteThis;
  /**
   *
   */
  public MainAdapterImpl() {
    if (TRACE) System.out.println("Constructor MainAdapterImpl !!!!!!");
    m_RemoteThis = this;
  }
  /**
   *
   */
  public void setBuf(String s) {
    if (TRACE) System.out.println("MainAdapterImpl.setBuf:"+s);
    m_Buf = s;
  }
  /**
   *
   */
  public void restartServer() {
    System.out.println("Received message to restartServer !!!!");
    try {
      String Out ="";
      String command = "java -cp .:../lib/jdom.jar:../lib/log4j.jar javaeva.server.EvAServer &";

      System.out.println("Calling the command:"+"java javaeva.server.EvAServer");
      Process pro =  Runtime.getRuntime().exec(command);
      BufferedReader in = new  BufferedReader ( new InputStreamReader (pro.getInputStream()));
//      String line = null;
//      while((line = in.readLine()) != null ) {
//        System.out.println(line);
//        Out = Out + line;
//      }
      System.out.println("command="+command);
    } catch (Exception e) {
      System.out.println("Error in calling the command:"+e.getMessage());
    }
    killServer();
  }
  /**
   *
   */
  public void killServer() {
    //Mail.SendMail("Received message to kill EvAServer");
    System.out.println("Received message to kill EvAServer !!!!");
    KillThread x = new KillThread();
    x.start();
    return;
  }

  /**
   *
   */
  public String getBuf() {
    return m_Buf;
  }
  /**
   *
   */
  public String getExecOutput(String command) {
    String Out= new String();
    try {
      BufferedReader in = null;
      Process pro = null;
      if (TRACE) System.out.println("Calling the command:"+command);
      pro = Runtime.getRuntime().exec(command);
      in = new  BufferedReader ( new InputStreamReader (pro.getInputStream()));
      String line = null;
      while((line = in.readLine()) != null ) {
    	  if (TRACE) System.out.println(line);
        Out = Out + line;
      }
    } catch (Exception e) {
      System.err.println("Error in calling the command:"+e.getMessage());
    }
    return Out;
  }
  /**
   *
  */
  public RMIInvocationHandler getRMIHandler(Object obj) {
    System.out.println("getRMIHandler");
    RMIInvocationHandler ret = null;
    try {
      ret = new RMIInvocationHandlerImpl(obj);
    }
    catch (Exception e) {
      System.out.println("Error: RMIInvokationHandler getRMIHandler");
    }
    return ret;
  }
   /**
   *
  */
  public RMIThreadInvocationHandler getRMIThreadHandler(Object obj) {
    if (TRACE) System.out.println("getRMIThreadHandler");
    RMIThreadInvocationHandler ret = null;
    try {

      ret = new RMIThreadInvocationHandlerImpl(obj);
    }
    catch (Exception e) {
      System.out.println("Error: RMIThreadInvokationHandler getRMIThreadHandler");
    }
    return ret;
  }
  /**
   *
   */
  public void setRemoteThis (MainAdapter x) {
    m_RemoteThis = x;
  }
}
/**
 *
 */
class KillThread extends Thread {
  /**
   *
   */
  public void run() {
    try {sleep(3000);}
    catch(Exception e) {
      System.out.println("Error in sleep");
    }
    System.exit(-1);
  }
}
