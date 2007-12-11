package javaeva.tools;
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
}
