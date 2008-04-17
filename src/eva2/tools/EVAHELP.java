package eva2.tools;
/**
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 255 $
 *            $Date: 2007-11-15 14:58:12 +0100 (Thu, 15 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;

import eva2.client.EvAClient;


import wsi.ra.tool.BasicResourceLoader;


/**
 *
 */
public class EVAHELP {
  private static long m_TimeStamp;
  /**
   *
   */
  /**
  *
  */
 static public Properties readProperties(String resourceName) throws Exception {
   if (EvAClient.TRACE) System.out.println("readProperties of file: " + resourceName);
   Properties prop = new Properties();
   BasicResourceLoader loader = BasicResourceLoader.instance();
   byte bytes[] = loader.getBytesFromResourceLocation(resourceName);
   if (bytes != null) {
     ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
     prop.load(bais);
   }
   if (prop != null)
     return prop;
   /////////////

   int slInd = resourceName.lastIndexOf('/');
   if (slInd != -1)
     resourceName = resourceName.substring(slInd + 1);
   Properties userProps = new Properties();
   File propFile = new File(File.separatorChar + "resources" +
                            File.separatorChar + resourceName);
   if (propFile.exists()) {
     try {
       userProps.load(new FileInputStream(propFile));
     }
     catch (Exception ex) {
       System.out.println("Problem reading user properties: " + propFile);
     }
   }
   return userProps;
 }

  /**
   *
   */
  public static String getmyRUP() {
   String Out= new String();
   String HostName = "";
      try {
        HostName = InetAddress.getLocalHost().getHostName();
      }
      catch (Exception e) {
        System.out.println("ERROR getting HostName EVAHELP " + e.getMessage());
      }

   try {
     BufferedReader in = null;
     Process pro = null;
     String command = "rup "+HostName;
     pro = Runtime.getRuntime().exec(command);
     in = new  BufferedReader ( new InputStreamReader (pro.getInputStream()));
     String line = null;
     while((line = in.readLine()) != null ) {
       //System.out.println(line);
       Out = Out + line;
     }
   } catch (Exception e) {
     System.out.println("Error in calling the command:"+e.getMessage());
   }
   return Out;
 }

  /**
   *
   */
  public static void setTimeStamp() {
    m_TimeStamp = System.currentTimeMillis();
  }
  /**
   *
   */
  public static long getTimeStamp() {
     return System.currentTimeMillis() -m_TimeStamp;
  }
  /**
   *
   */
  public static void returnForExit() {
    BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
    System.out.println("return for exit:");
    try {
       in.readLine();
    } catch (Exception e) {
      System.out.println(""+e.getMessage());
    }
  }
  /**
   *
   */
  public static String getSystemPropertyString() {
    String ret = "";
    Properties prop = System.getProperties();
    Enumeration list = prop.propertyNames();
    while (list.hasMoreElements()) {
      Object o = list.nextElement();
      //System.out.println("o="+o.toString());
      ret = ret + o.toString() + " = " +System.getProperty(o.toString()) +"\n";
    }
    return ret;
  }
  /**
   *
   */
  public static String cutClassName (String longName) {
    int dotPos = longName.lastIndexOf('.');
    if (dotPos != -1)
      longName = longName.substring(dotPos + 1);
    return longName; // now is shortName
  }
  /**
   *
   */
  public static void freeMemory() {
    Runtime currR = Runtime.getRuntime();
    long freeM = currR.freeMemory();
    freeM = (long)(freeM /1024);
    //System.out.println("Available memory : "+freeM+" Kbytes");
    System.gc();
    currR = Runtime.getRuntime();
    freeM = currR.freeMemory();
    freeM = (long)(freeM /1024);
    //System.out.println("after gc:Available memory : "+freeM+" bytes");
  }
}