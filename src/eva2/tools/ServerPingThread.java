package eva2.tools;

import java.util.StringTokenizer;
import java.rmi.Naming;

import eva2.server.EvAMainAdapter;
import eva2.server.EvAMainAdapterImpl;
import eva2.tools.jproxy.RMIInvocationHandler;

/**
 * <p>Title: EvA2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author planatsc
 * @version 1.0
 */

public class ServerPingThread extends Thread {

  boolean isServerAlive;
  String hostname;

  public ServerPingThread(String hostname) {
    this.isServerAlive = false;
    this.hostname = hostname;
  }

  public void run() {
    while (true) {
      try {
        isServerAlive = rmiPing(hostname);
        this.sleep(3000);
      } catch (Exception ex) {
        System.out.println(ex);
      }
    }
  }

  public static void main(String[] args) {
  }

  public boolean rmiPing(String testurl) {
    EvAMainAdapter Test = null;
    String SEP = "_";
    int len = testurl.indexOf(SEP);
    String Host = testurl;
    String Number = "_0";
    if (len != -1) {
      StringTokenizer st = new StringTokenizer(testurl, SEP);
      Host = st.nextToken().trim();
      Number = SEP + st.nextToken().trim();
    }
    String UserName = System.getProperty("user.name");
    String MainAdapterName = UserName + EvAMainAdapterImpl.MAIN_ADAPTER_NAME + Number;
    try {
      RMIInvocationHandler x = (RMIInvocationHandler) Naming.lookup("rmi://" + Host + ":" + EvAMainAdapterImpl.PORT + "/" + MainAdapterName); // attention !!
      Test = (EvAMainAdapter) x.getWrapper();
    }
    catch (Exception e) {
      return false;
    }
    return true;
  }
  public boolean isServerAlive() {
    return isServerAlive;
  }


}
