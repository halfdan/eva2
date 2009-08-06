package eva2.tools.jproxy;
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

/**
 * This exception will be thrown when no
 * RMIServer con be found by the ComAdapter.
 */
public class NO_RMIServerAvailable extends Exception {
  NO_RMIServerAvailable() {
    printStackTrace();
  }

}