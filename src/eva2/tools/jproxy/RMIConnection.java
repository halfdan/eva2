package eva2.tools.jproxy;


/**
 *
 */
public class RMIConnection {
  public static boolean TRACE = false;
  private String m_HostName;
  protected MainAdapter m_MainAdapter;
  protected MainAdapterClient m_MainAdapterClient;
  /**
   *
   */
  public RMIConnection(String HostName, MainAdapter Adapter,
                       MainAdapterClient AdapterClient) {
    m_MainAdapter = Adapter;
    m_HostName = HostName;
    m_MainAdapterClient = AdapterClient;
  }

  /**
   *
   */
  public String getHostName() {
    return m_HostName;
  }

  /**
   *
   */
  public MainAdapter getMainAdapter() {
    return m_MainAdapter;
  }

  /**
   *
   */
  public void killServer() {
    try {
      m_MainAdapter.killServer();
    } catch (Exception e) {
      System.out.println("Error while killing server: " + e.getMessage());
    }
  }

  /**
   *
   */
  public void restartServer() {
    try {
      m_MainAdapter.restartServer();
    } catch (Exception e) {
      System.out.println("Error while restartServer server: " + e.getMessage());
    }
  }

  /**
   *
   */
  public RMIInvocationHandler getRMIHandler(Object c) {
    RMIInvocationHandler ret = null;
    try {
      ret = m_MainAdapter.getRMIHandler(c);
    } catch (Exception e) {
      System.out.println("Error while getRMIHandler server: " + e.getMessage());
      e.printStackTrace();
    }
    return ret;
  }

  /**
   *
   */
  public RMIThreadInvocationHandler getRMIThreadHandler(Object c) {
    RMIThreadInvocationHandler ret = null;
    try {
      ret = m_MainAdapter.getRMIThreadHandler(c);
    } catch (Exception e) {
      System.err.println("Error while RMIThreadInvokationHandler server: " +
                         e.getMessage());
      e.printStackTrace();
    }
    return ret;
  }

  /**
   *
   */
  public String getExecOutput(String command) {
    String ret = null;
    try {
      ret = m_MainAdapter.getExecOutput(command);
    } catch (Exception e) {
      System.out.println("Error while getExecOutput server: " + e.getMessage());
    }
    return ret;
  }
}
