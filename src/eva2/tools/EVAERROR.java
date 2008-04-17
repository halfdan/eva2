package eva2.tools;

import java.util.HashMap;
/**
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 245 $
 *            $Date: 2007-11-08 17:24:53 +0100 (Thu, 08 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class EVAERROR {
  static boolean MAIL_ON_ERROR = false;
  static boolean MAIL_ON_WARNING = false;
  transient static HashMap<String, Boolean> errMap = null;
  /**
   *
   */
  static public void EXIT(String Message) {
 //   if (MAIL_ON_ERROR)
 //     EVAMail.SendMail("JavaEva EXIT on ERROR !! ",Message,"ulmerh@informatik.uni-tuebingen.de");
    System.err.println("ERROR: "+Message);
    System.out.flush();
    System.err.flush();
    System.exit(-1);
  }
  /**
   *
   */
  static public void EXIT(String Message, Exception e) {

    System.out.println("ERROR: "+Message);
    System.out.println("Exception: ");
    e.printStackTrace();
//    if (MAIL_ON_ERROR)
//      EVAMail.SendMail("JavaEva EXIT on ERROR !! ",Message+e.toString(),"ulmerh@informatik.uni-tuebingen.de");
    System.out.flush();
    System.err.flush();
    System.exit(-1);
  }
  /**
   *
   */
  static public void WARNING(String Message) {
//     if (MAIL_ON_WARNING)
//      EVAMail.SendMail("JavaEva EXIT on ERROR !! ",Message,"ulmerh@informatik.uni-tuebingen.de");
    System.err.println("WARNING: "+Message);
    System.out.flush();
    System.err.flush();
  }
  
  /**
   * Send a message only once to System.err. Once means that the msg is
   * stored for the lifetime of the VM or until clearMsgCache() is called.
   * 
   * @param msg
   */
  static public void errorMsgOnce(String msg) {
	  if (errMap == null) errMap = new HashMap<String, Boolean>();
	  
	  if (!errMap.containsKey(msg)) {
		  System.err.println(msg);
		  errMap.put(msg, new Boolean(true));
	  }
  }
  
  /**
   * Clear the error message cache, so that any error messages are
   * displayed at least once.
   */
  static public void clearMsgCache() {
	  if (errMap != null) errMap.clear();
  }
}
