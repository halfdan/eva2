package javaeva.gui;
import java.util.ArrayList;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */


public class JEFrameRegister {

  private static ArrayList JEFrameList;

  static {

    JEFrameList = new ArrayList();
  }

  public static void register(JEFrame jf) {
    JEFrameList.add(jf);
//    System.out.println("reg  ! JEFSIZE :" + JEFrameList.size());
  }

  public static void unregister(JEFrame jf) {
    JEFrameList.remove(jf);
//    System.out.println("unreg! JEFSIZE :" + JEFrameList.size());
  }

  public static Object[] getFrameList() {
    return JEFrameList.toArray();
  }

  public static void setFocusToNext(JEFrame jf) {
    int idx = JEFrameList.indexOf(jf);
    idx = (idx + 1) % JEFrameList.size();
    JEFrame toset =    ((JEFrame) JEFrameList.get(idx));
    toset.setExtendedState(JEFrame.NORMAL);
    toset.toFront();
  }


}