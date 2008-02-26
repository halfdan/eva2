package javaeva.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javaeva.server.EvAMainAdapter;
import javaeva.server.modules.ModuleAdapter;
import wsi.ra.jproxy.MainAdapter;
import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.RMIConnection;

/**
 *
 */
class RMIConnectionEvA extends RMIConnection {
	/**
	 *
	 */
	public RMIConnectionEvA(String HostName, MainAdapter Adapter,
			MainAdapterClient AdapterClient) {
		super(HostName, Adapter, AdapterClient);
	}
	/**
	 *
	 */
	public ModuleAdapter getModuleAdapter(String Modul) {
		if (m_MainAdapter instanceof EvAMainAdapter) {
			EvAMainAdapter evaAdapter = (EvAMainAdapter)m_MainAdapter;
			ModuleAdapter ret = null;
			if (TRACE) System.out.println("ComAdapter.getModuleAdapter(" + Modul + ")");
			String hostAdd = "";
			try {
				hostAdd = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				hostAdd = "unknown host";
			}
			if (TRACE) {
				System.out.println(" Client is = " + hostAdd);
			}
			m_MainAdapter.setBuf("Test_1");

			ret = evaAdapter.getModuleAdapter(Modul, false, hostAdd, this.m_MainAdapterClient);

			return ret;	  
		} else {
			System.err.println("error, couldnt get module adapter in EvAComAdapter.getModuleAdapter. Main adapter is not of type EvAMainAdapter!");
			return null;
		}

	}

}
