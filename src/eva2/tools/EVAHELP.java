package eva2.tools;
/**
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version: $Revision: 255 $
 *            $Date: 2007-11-15 14:58:12 +0100 (Thu, 15 Nov 2007) $
 *            $Author: mkron $
 */

import java.io.*;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;

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
    public static String getmyRUP() {
        String Out = new String();
        String HostName = "";
        try {
            HostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            System.out.println("ERROR getting HostName EVAHELP " + e.getMessage());
        }

        try {
            BufferedReader in = null;
            Process pro = null;
            String command = "rup " + HostName;
            pro = Runtime.getRuntime().exec(command);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                //System.out.println(line);
                Out += line;
            }
        } catch (Exception e) {
            System.out.println("Error in calling the command:" + e.getMessage());
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
        return System.currentTimeMillis() - m_TimeStamp;
    }

    /**
     *
     */
    public static void returnForExit() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("return for exit:");
        try {
            in.readLine();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     */
    public static String getSystemPropertyString() {
        StringBuffer sBuf = new StringBuffer();
        Properties prop = System.getProperties();
        Enumeration<?> list = prop.propertyNames();
        while (list.hasMoreElements()) {
            Object o = list.nextElement();
            //System.out.println("o="+o.toString());
            sBuf.append(o.toString());
            sBuf.append(" = ");
            sBuf.append(System.getProperty(o.toString()));
            sBuf.append("\n");
        }
        return sBuf.toString();
    }

    /**
     * @param longName The FQDN of a class
     * @return Returns the class Name without package.
     */
    public static String cutClassName(final String longName) {
        final int dotPos = longName.lastIndexOf('.');
        String className = longName;
        if (dotPos != -1) {
            className = className.substring(dotPos + 1);
        }
        return className; // now is shortName
    }

    /**
     *
     */
    public static void freeMemory() {
        Runtime currR = Runtime.getRuntime();
        long freeM = currR.freeMemory();
        freeM = (long) (freeM / 1024);
        //System.out.println("Available memory : "+freeM+" Kbytes");
        System.gc();
        currR = Runtime.getRuntime();
        freeM = currR.freeMemory();
        freeM = (long) (freeM / 1024);
        //System.out.println("after gc:Available memory : "+freeM+" bytes");
    }

    /**
     * Log a String to a log-file indicated by the file name. If the file
     * exists, the String is appended.
     *
     * @param msg
     * @param fileName
     */
    public static void logString(String msg, String fileName) {
        final File logFile = new File(fileName);
        try {
            BufferedWriter bW = new BufferedWriter(
                    new PrintWriter(new FileOutputStream(logFile, logFile.exists())));
            bW.write(msg);
            bW.close();
        } catch (IOException ex) {
            System.err.println("couldnt log to destination " + fileName + ": " + ex.getMessage());
        }
    }

    /**
     * Deletes the given file in the current directory. If the file does not
     * exist nothing happens.
     *
     * @param fileName The name of the log file
     */
    public static void clearLog(String fileName) {
        File logFile = new File(fileName);
        if (logFile.exists()) {
            logFile.delete();
        }
    }
}