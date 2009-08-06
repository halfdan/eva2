package eva2.tools.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.1 $
 *            $Date: 2004/04/15 09:12:29 $
 *            $Author: ulmerh $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface MainAdapter {
  public String getExecOutput(String command);
  public void setBuf(String s);
  public void killServer();
  public void restartServer();
  public String getBuf();
  public RMIInvocationHandler getRMIHandler(Object obj);
  public RMIThreadInvocationHandler getRMIThreadHandler(Object obj);
  public void setRemoteThis (MainAdapter x);
}

