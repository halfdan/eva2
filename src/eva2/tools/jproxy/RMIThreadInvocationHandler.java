package eva2.tools.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.1 $
 *            $Date: 2004/04/15 09:12:31 $
 *            $Author: ulmerh $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.rmi.Remote;
import java.rmi.RemoteException;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface RMIThreadInvocationHandler extends Remote {
  public Object invoke (String m, Object[] args)throws RemoteException;
  public Object getWrapper () throws RemoteException;
  public void setWrapper(Object Wrapper)  throws RemoteException;
  public String getServerName()throws RemoteException;
}