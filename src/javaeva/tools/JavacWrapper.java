package javaeva.tools;
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
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
/**
 *
 */
public class JavacWrapper  {
  static final public String m_userdefclasses = "userdefclasses";
  static StringBuffer m_error = new StringBuffer();
  static StringBuffer m_output = new StringBuffer();
  static boolean compiling = false;
  /**
   *
   */
  public JavacWrapper() {}
  /**
   *
   */
  public static String getErrors() {
    return m_error.toString();
  }
  /**
   *
   */
  public static boolean compiling() {
    return compiling;
  }
  /**
   *
   */
  public static void compile(final String javafile) {
    compiling = true;
    m_error.delete(0,m_error.length());
    m_output.delete(0,m_error.length());
    String base = System.getProperty("user.dir");
    String m_ClassPath = System.getProperty("java.class.path");
    String target = base+"/"+m_userdefclasses+"/";

     String tmpS = javafile.replaceAll("\\/","/");
    File temp = new File(target);
    if (temp.isDirectory()==false) temp.mkdir();
    try {
      System.out.println("javac -classpath " + m_ClassPath + " -d " + target + " " + tmpS);
      Process pc = Runtime.getRuntime().exec("javac -classpath " + m_ClassPath + " -d " + target + " " + tmpS);
      final BufferedReader estream = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
      Thread errThread = new Thread( new Runnable () {
         public void run() {
          try {
            String outstr = estream.readLine();
            while (outstr != null) {
              m_error.append(outstr + "\n");
              outstr = estream.readLine();
            }
          } catch (Exception e) { e.printStackTrace(); }
         }
         }
      );
      final BufferedReader ostream = new BufferedReader(new InputStreamReader(pc.getInputStream()));
      Thread outThread = new Thread( new Runnable () {
         public void run() {
          try {
            String outstr = ostream.readLine();
            while (outstr != null) {
              m_output.append(outstr + "\n");
              outstr = ostream.readLine();
            }
          } catch (Exception e) { e.printStackTrace(); }
         }
         }
      );
      outThread.start();
      errThread.start();
      pc.waitFor();
    } catch (Exception e){
      e.printStackTrace();
    }
    compiling = false;
  }
  /**
   *
   */
  public static void main(String[] args) {
  }
}