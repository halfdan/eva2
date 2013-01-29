package eva2.tools.jproxy;

/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.3 $
 *            $Date: 2004/04/28 07:50:32 $
 *            $Author: ulmerh $
 */
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 */
public class ComAdapter {
	private static final Logger LOGGER = Logger.getLogger(ComAdapter.class.getName());
	public static final int PORT = 1099;
	public static final String SEP = "_";
	static protected ComAdapter m_instance = null;

	public Registry m_Registry = null;
	private ArrayList<RMIConnection> m_Connections = new ArrayList<RMIConnection>();
	private String hostName;
	private ArrayList<String> m_HostNameList = new ArrayList<String>();
	private ArrayList<String> m_AvailableHostNameList = new ArrayList<String>();
//	private String m_RemoteAdapterName;
	private String userName;
	private int m_ownHostIndex = 0;
	protected RMIServer m_RMIServer;
	private String serverListSeparator = ",";
	private boolean gettingRegistry;

	/**
	 *
	 */
	public static ComAdapter getInstance() {
		if (m_instance != null)
			return m_instance;
		m_instance = new ComAdapter();
		return m_instance;
	}

	/**
	 *
	 */
	protected ComAdapter() {
		try {
			userName = System.getProperty("user.name");			
		} catch(SecurityException ex) {
			/* This exception is expected to happen when
			 * we are using Java WebStart.
			 */			
			LOGGER.log(Level.INFO, "Username set to: WebStart", ex);
			userName = "Webstart";
		}
		hostName = "localhost"; //"192.168.0.1";

		try {
			System.setProperty("java.security.policy", "server.policy");
		} catch(AccessControlException ex) {
			// ToDo: This happens using webstart - what to do now?
		}
		launchRMIRegistry(false);
		if (!m_HostNameList.contains("localhost")) {
			// make sure localhost is in the list
			m_HostNameList.add("localhost");
		}
	}
	
	/**
	 * Parse a string of server names. The comma "," must be used as separator.
	 * 
	 * @param serverList
	 */
	public void addServersFromString(String serverList) {
		if (serverList != null) {
			// parse the servernames
			StringTokenizer st = new StringTokenizer(serverList, serverListSeparator);

			while (st.hasMoreTokens()) {
				String current = st.nextToken().trim();
				if (!m_HostNameList.contains(current)) {
					m_HostNameList.add(current);
				} else {
					LOGGER.log(Level.FINER, "Server " + current + " was already in list");
				}
			}
		}
	}
	
	/**
	 * Add a server list from a Properties instance. The key used is "ServerList".
	 * 
	 * @param props
	 */
	public void addServersFromProperties(Properties props) {
		String servs = props.getProperty("ServerList");
		addServersFromString(servs);
	}
	
	/**
	 * Set the separator for the server list string.
	 * 
	 * @param sep List separator
	 */
	public void setServerListSeparator(String sep) {
		serverListSeparator = sep;
	}
		
	/**
	 * The separator for the server list string.
	 * 
	 * @return The list separator
	 */
	public String getServerListSeparator() {
		return serverListSeparator;
	}
	
	/**
	 *
	 * @param server An array of servers
	 */
	public void setServerList(String[] server){
		m_HostNameList.clear();
		for (int i=0;i<server.length;i++) {
                m_HostNameList.add(server[i]);
            }

	}
	/**
	 *
	 *
	 */
	public RMIInvocationHandler getRMIHandler(Object c, String host) {
		System.out.println("ComAdapter.getRMIHandler() for host " + host);
		hostName = host;
		RMIInvocationHandler ret = null;
		while (ret == null) {
			ret = getConnection(hostName).getRMIHandler(c);
			if (ret == null)
				System.out.println("Error in getRMIHandler");
		}
		return ret;
	}

	/**
	 *
	 *
	 */
	public RMIThreadInvocationHandler getRMIThreadHandler(Object c, String host) {
		int cnt=0;
		hostName = host;
		RMIThreadInvocationHandler ret = null;
		while (cnt<100) { //ret == null) {
			cnt++;
			ret = getConnection(hostName).getRMIThreadHandler(c);
			if (ret == null) {
				LOGGER.log(Level.WARNING, "Error in getRMIThreadHandler");
			}
		}
		return ret;
	}

	/**
	 *
	 *
	 */
	public RMIThreadInvocationHandler getRMIThreadHandler(Object c) {
		RMIThreadInvocationHandler ret = null;
		if (m_AvailableHostNameList.size() == 0) {
			evalAvailableHostNameList();
			m_ownHostIndex = 0;
		}
		m_ownHostIndex++;
		if (m_ownHostIndex >= m_AvailableHostNameList.size()) {
			m_ownHostIndex = 0;
		}			
		hostName = (String) m_AvailableHostNameList.get(m_ownHostIndex);
		ret = getRMIThreadHandler(c, hostName);
		return ret;
	}

	/**
	 *
	 *
	 */
	public RMIInvocationHandler getRMIHandler(Object c) {
		RMIInvocationHandler ret = null;
		while (m_AvailableHostNameList.size() == 0) {
			evalAvailableHostNameList();
			if (m_AvailableHostNameList.size() == 0) {
				LOGGER.log(Level.WARNING, "No host availabe waiting..");
			}
			m_ownHostIndex = 0;
		}
		m_ownHostIndex++;
		if (m_ownHostIndex >= m_AvailableHostNameList.size()) {
			m_ownHostIndex = 0;
		}
		hostName = (String) m_AvailableHostNameList.get(m_ownHostIndex);
		ret = getRMIHandler(c, hostName);
		return ret;
	}

//	/**
//	 *
//	 */
//	public String getLoad() {
//		if (TRACE)
//			System.out.println("ComAdapter.getLoad()");
//		String Load = null;
//		Load = getConnection(m_ownHostName).getExecOutput("rup " + m_ownHostName);
//		if (TRACE)
//			System.out.println("Load of Modules on Server !! :" + Load);
////		if (m_LogPanel != null)
////		m_LogPanel.logMessage("Load of Modules on Server !! :" + Load);
//		return Load;
//	}

//	/**
//	*
//	*/
//	public String gettokens() {
//	if (TRACE)
//	System.out.println("ComAdapter.gettokens()");
//	String Tokens = null;
//	Tokens = getConnection(m_ActualHostName).getExecOutput("tokens");
//	if (TRACE)
//	System.out.println("tokens on Server !! :" + Tokens);
//	Tokens = getConnection(m_ActualHostName).getExecOutput(
//	"klog -principal ulmerh -password ");
//	if (TRACE)
//	System.out.println("KLOG !!! !! :" + Tokens);
//	Tokens = getConnection(m_ActualHostName).getExecOutput("tokens");
//	if (TRACE)
//	System.out.println("tokens on Server !! :" + Tokens);
//	return Tokens;
//	}

	/**
	 *
	 */
	public void evalAvailableHostNameList() {
		long time = System.currentTimeMillis();
		m_AvailableHostNameList.clear();
		for (int i = 0; i < m_HostNameList.size(); i++) {
			if (rmiPing((String) m_HostNameList.get(i)) == true) {
				m_AvailableHostNameList.add((String) m_HostNameList.get(i));
			}
			String testurl = (String) m_HostNameList.get(i);
			for (int j = 1; j < 3; j++) {
				if (rmiPing(testurl + "_" + j) == true) {
					LOGGER.log(Level.INFO, "Found EvAServer on: " + testurl);
					m_AvailableHostNameList.add(testurl + "_" + j);
				}
			}
		}
		time = System.currentTimeMillis() - time;		

	}

	/**
	 *
	 */
	public String[] getAvailableHostNameList() {
		if (m_AvailableHostNameList.size() == 0)
			evalAvailableHostNameList();
		String[] ret = new String[m_AvailableHostNameList.size()];
		m_AvailableHostNameList.toArray(ret);
		return ret;
	}

	/**
	 *
	 */
	public String[] getHostNameList() {
		String[] x = new String[m_HostNameList.size()];
		m_HostNameList.toArray(x);
		return x;
	}

	/**
	 * Returns the current hostName set for the adapter.
     * 
     * @return The current hostName.
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Sets hostName and writes it to a file.
     * 
     * @param newHost The new hostName.
	 */
	public void setHostName(final String newHost) {
		hostName = newHost;
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot();
            prefs.put("hostname", hostName);
        } catch (SecurityException ex) {
            LOGGER.log(Level.WARNING, "Can't write user preference.", ex);
        }
	}


	/**
	 *  Creates a RMI-MainAdapter to host.
	 *  @return
	 */
	protected MainAdapter createRMIMainConnect(String HostToConnect) {
		int len = HostToConnect.indexOf(SEP);
		String Host = HostToConnect;
		String Number = SEP + "0";
		if (len != -1) {
			StringTokenizer st = new StringTokenizer(HostToConnect, SEP);
			Host = st.nextToken().trim();
			Number = SEP + st.nextToken().trim();
		}
		String MainAdapterName = userName + MainAdapterImpl.MAIN_ADAPTER_NAME + Number; // attention
		
		LOGGER.info("RMIConnect to " + HostToConnect);
		MainAdapter MainRemoteObject = null;
		try {
			RMIInvocationHandler invocHandler = (RMIInvocationHandler) Naming.lookup(
					"rmi://" + Host + ":" + MainAdapterImpl.PORT + "/"
					+ MainAdapterName);
			MainRemoteObject = getMainAdapter(invocHandler);

			MainRemoteObject.setBuf("Ok.");
			LOGGER.info("RMIConnect " + MainRemoteObject.getBuf());
		} catch (MalformedURLException ex) {
			LOGGER.log(Level.WARNING, "MalformedURLException: Error while looking up " + ex.getMessage(), ex);
		} catch (NotBoundException ex) {
			LOGGER.log(Level.WARNING, "NotBoundException: Error while looking up " + ex.getMessage(), ex);
		} catch (RemoteException ex) {
			LOGGER.log(Level.WARNING, "Error while connecting Host: " + HostToConnect, ex);
			return null;
		}
		return MainRemoteObject;
	}

	protected MainAdapter getMainAdapter(RMIInvocationHandler invocHandler) throws RemoteException {
		return (MainAdapter) invocHandler.getWrapper();
	}

	/**
	 *
	 */
	protected int getNumberOfServersonHost(String testurl) {
//		String MainAdapterName = m_UserName + MainAdapterImpl.MAIN_ADAPTER_NAME;
		String[] list = null;
		try {
			list = Naming.list("rmi://" + testurl + ":" + MainAdapterImpl.PORT);
		} catch (Exception e) {
			System.err.println ("Exception : "+testurl  );
			return 0;
		}
		int ret = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i].indexOf(MainAdapterImpl.MAIN_ADAPTER_NAME) != -1)
				ret++;
		}
		System.err.println("error in ComAdapter.getNumberOfServersonHost");
		//System.out.println(" ret ===        "+ret);
		return ret;
	}

	/**
	 *
	 */
	private boolean rmiPing(String testurl) {
		MainAdapter Test = null;
		int len = testurl.indexOf(SEP);
		String Host = testurl;
		String Number = "_0";
		if (len != -1) {
			StringTokenizer st = new StringTokenizer(testurl, SEP);
			Host = st.nextToken().trim();
			Number = SEP + st.nextToken().trim();
		}
		String mainAdapterName = userName + MainAdapterImpl.MAIN_ADAPTER_NAME +
		Number;
		try {
			RMIInvocationHandler x = (RMIInvocationHandler) Naming.lookup("rmi://" +
					Host + ":" + MainAdapterImpl.PORT + "/" + mainAdapterName); // attention !!
			Test = (MainAdapter) x.getWrapper();
		} catch (Exception ex) {
			LOGGER.log(Level.INFO, "No connection to : " + testurl, ex);
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	public void killServer(String ServerToKill) {
		RMIConnection myConnection = getConnection(ServerToKill);
		myConnection.killServer();
		this.m_Connections.remove(myConnection);
	}

	/**
	 *
	 */
	public void killAllServers() {
		for (int i = 0; i < m_AvailableHostNameList.size(); i++) {
			RMIConnection myConnection = getConnection((String)
					m_AvailableHostNameList.get(i));
			myConnection.killServer();
			this.m_Connections.remove(myConnection);
		}
	}

	/**
	 *
	 */
	public void restartServerAllServer() {
		for (int i = 0; i < m_AvailableHostNameList.size(); i++) {
			RMIConnection myConnection = getConnection((String)
					m_AvailableHostNameList.get(i));
			myConnection.restartServer();
			m_Connections.remove(myConnection);
		}
	}

	/**
	 *
	 */
	public void restartServer(String host) {
		hostName = host;
		restartServer();
	}

	/**
	 *
	 */
	public void restartServer() {
		if (hostName.equals("localhost")) { // TODO whats this?
			return;
		}
		RMIConnection x = getConnection(hostName);
		x.restartServer();
		m_Connections.remove(x);
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			System.err.println("Error in sleep of ExitThread");
		}
	}

	public void setRMIRegistry(Registry reg) {
		m_Registry = reg;
		gettingRegistry = false;
	}
	
	/**
	 * Install RMIregistry on default port !!
	 */
	protected void launchRMIRegistry(boolean ownThread) {
		if (ownThread) {
			Thread t = new Thread(new RMIRegistration(this));
			gettingRegistry = true;
			t.start();
		} else {
			RMIRegistration rmiReg = new RMIRegistration(this);
			gettingRegistry = true;
			rmiReg.run();
		}
	}
	/**
	 *
	 */
	public void setLocalRMIServer(RMIServer Server) {
		m_RMIServer = Server;
	}

	/**
	 * Just the main for testing the class.
	 */
	public static void main(String[] args) {
		ComAdapter TestApp = ComAdapter.getInstance();
		TestApp.evalAvailableHostNameList();
		TestApp.killAllServers();
		//TestApp.restartServerAllServer();
	}

	/**
	 *
	 */
	protected RMIConnection getConnection(String Host) {
		for (int i = 0; i < this.m_Connections.size(); i++) {
			// search for an already established connection to the given host
			// and return it if found
			RMIConnection ret = (RMIConnection) m_Connections.get(i);
			if (Host.equals(ret.getHostName())) return ret;
		}
		// else create and add new RMIConnection
		RMIConnection ret = null;
		if (Host.equals("localhost") && m_RMIServer != null) {
			// this uses rmi on the localhost
			ret = createRMIConnection(Host, m_RMIServer.getMainRemoteObject(),
					new MainAdapterClientImpl("localhost"));
		} else {
			MainAdapter Adapter = (MainAdapter)createRMIMainConnect(Host);
			if (Adapter != null)
				ret = createRMIConnection(Host, Adapter,
						(MainAdapterClient) RMIProxyLocal.newInstance(new MainAdapterClientImpl(Host)));
		}
		if (ret != null) m_Connections.add(ret);
		else System.err.println("Warning no valid connection !!");
		return ret;
	}	
	
	protected RMIConnection createRMIConnection(String Host, MainAdapter mainRemoteObject, MainAdapterClient client) { 
		return new RMIConnection(Host, mainRemoteObject, client);
	}
	
//	private RMIConnection getConnection(String Host) {
//		if (TRACE == true) System.out.println("ComAdapter.getConnection for host :" + Host);
//		RMIConnection ret = null;
//		for (int i = 0; i < this.m_Connections.size(); i++) {
//			ret = (RMIConnection) m_Connections.get(i);
//			if (Host.equals(ret.getHostName()) == true)
//				return ret;
//		}
//		// create and add new RMIConnection
//		if (Host.equals("localhost") && (m_RMIServer != null)) {
//			ret = new RMIConnection(Host, m_RMIServer.getMainRemoteObject(),
//					new MainAdapterClientImpl());
//		} else {
//			MainAdapter Adapter = createRMIMainConnect(Host);
//			if (Adapter != null)
//				ret = new RMIConnection(Host, Adapter,
//						(MainAdapterClient)
//						RMIProxyLocal.newInstance(new MainAdapterClientImpl()));
//		}
//		if (ret != null) m_Connections.add(ret);
//		else System.err.println("Warning no valid connection !!");
//		return ret;
//	}
}

class RMIRegistration implements Runnable {
	ComAdapter comAd = null;
	private boolean TRACE = false;
	Registry reg = null;
	
	public RMIRegistration(ComAdapter comAdapter) {
		comAd = comAdapter;
	}

    @Override
	public void run() {
		if (TRACE)
			System.out.println("LaunchRMIRegistry on Client on PORT " + ComAdapter.PORT);
		try {
			reg = java.rmi.registry.LocateRegistry.createRegistry(ComAdapter.PORT);
		}
		catch (Throwable e) {
			if (TRACE) System.out.println("Registry notcreated !!" + e.getMessage());
			reg = null;
		}
		if (reg == null) {
			if (TRACE) System.out.println("Try to get registry with getRegistry on PORT " + ComAdapter.PORT);
			try {
				reg = java.rmi.registry.LocateRegistry.getRegistry(ComAdapter.PORT);
			}
			catch (Throwable e) {
				if (TRACE) System.out.println("registry not created !!" + e.getMessage());
				reg = null;
			}
		}
		if (reg != null && TRACE) System.out.println("--> got RMIREGISTRY");
		comAd.setRMIRegistry(reg);
	}
	
}