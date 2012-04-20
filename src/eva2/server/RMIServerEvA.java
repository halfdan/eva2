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
import java.util.logging.Level;

/**
 *
 */
public class RMIServerEvA extends RMIServer {

	public static RMIServerEvA getInstance() {
		if (instance==null) {
			instance = new RMIServerEvA();
		}
		return (RMIServerEvA)instance;
	}
	
	protected void createMainRemoteObject(String mainAdapterName) {
		try {
			mainRemoteObject = new EvAMainAdapterImpl();
			mainRemoteObject =
				(EvAMainAdapter) RMIProxyLocal.newInstance(
						mainRemoteObject,
						mainAdapterName + "_" + numberOfVM);
			mainRemoteObject.setRemoteThis(mainRemoteObject);
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not create main remote object!", ex);
		}
	}
}


