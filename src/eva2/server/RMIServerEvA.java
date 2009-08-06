package eva2.server;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 250 $
 *            $Date: 2007-11-13 10:32:19 +0100 (Tue, 13 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import eva2.tools.jproxy.RMIProxyLocal;
import eva2.tools.jproxy.RMIServer;

/**
 *
 */
public class RMIServerEvA extends RMIServer {

	public static RMIServerEvA getInstance() {
		if (m_instance==null) {
			m_instance = new RMIServerEvA();
		}
		return (RMIServerEvA)m_instance;
	}
	
	protected void createMainRemoteObject(String mainAdapterName) {
		try {
			m_MainRemoteObject = new EvAMainAdapterImpl();
			m_MainRemoteObject =
				(EvAMainAdapter) RMIProxyLocal.newInstance(
						m_MainRemoteObject,
						mainAdapterName + "_" + m_NumberOfVM);
			m_MainRemoteObject.setRemoteThis(m_MainRemoteObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


