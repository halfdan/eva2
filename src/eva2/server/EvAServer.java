package eva2.server;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of
 * Tuebingen, Computer Architecture @author Holger Ulmer, Felix Streichert,
 * Hannes Planatscher @version: $Revision: 320 $ $Date: 2007-12-06 16:05:11
 * +0100 (Thu, 06 Dec 2007) $ $Author: mkron $
 */

import eva2.EvAInfo;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class EvAServer {
    /*
     * MainAdapterImp object. This is need for the first connection between the
     * server and the client program.
     */
    private EvAMainAdapter mainRemoteObject;
    //private EvAComAdapter m_ComAdapter;
    private static String userName;
    private RMIServerEvA rmiServer;
    private static final Logger LOGGER = Logger.getLogger(EvAInfo.defaultLogger);

    /**
     * Constructor of EvAServer. Calls RMIConnection().
     */
    public EvAServer(boolean insideClient, boolean restart) {
        LOGGER.log(Level.INFO, "Number of CPUs :{0}", Runtime.getRuntime().availableProcessors());
        LOGGER.log(Level.INFO, "This is EvA Server Version: {0}", EvAInfo.getVersion());
        LOGGER.log(Level.INFO, "Java Version: {0}", System.getProperty("java.version"));
        try {
            userName = System.getProperty("user.name");
        } catch (SecurityException ex) {
            /*
             * This exception is expected to happen in Java WebStart
             */
            LOGGER.log(Level.WARNING, "Could not fetch username property. Setting username to 'WebStart'", ex);
            userName = "WebStart";
        }

        rmiServer = RMIServerEvA.getInstance();
    }

    /**
     * Main method of this class. Is the starting point of the server
     * application.
     */
    public static void main(String[] args) {
        boolean restart = false;
        boolean noMulti = false;
        for (int i = 0; i < args.length; i++) {
            System.out.println("args = " + args[i]);
            if (args[i].equals("restart")) {
                restart = true;
            }
            if (args[i].equals("nomulti")) {
                noMulti = true;
            }

        }
        //Runtime.getRuntime().addShutdownHook(new ExitThread());
        if (restart == true) {
            String hostName = "Host";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                System.out.println("ERROR getting HostName (EvAServer.main) " + e.getMessage());
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Error in sleep of ExitThread");
            }

            try {
                System.setOut(new PrintStream(
                        new FileOutputStream(hostName + "_server.txt")));
            } catch (FileNotFoundException e) {
                System.out.println("System.setOut" + e.getMessage());
            }
        }
        EvAServer evaServer = new EvAServer(false, restart); // false => started not inside the client, solo server
//   if (nomulti==false)
//      evaServer.multiServers(1);


    }

    /**
     *
     */
    public void multiServers(int size) {
        for (int i = 0; i < size; i++) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                System.out.println("Error in sleep of ExitThread");
            }
            try {
                String cmd = "java -cp \".\" eva2.server.EvAServer nomulti";
                System.out.println("Calling the command:" + cmd);
                Process pro = Runtime.getRuntime().exec(cmd);
                //Process pro = Runtime.getRuntime().exec("server");
                BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                //pro
                String line = null;
                while (true) {
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                }
                //System.out.println("");
            } catch (Exception e) {
                System.out.println("Error in calling the command:" + e.getMessage());
            }
        }
    }

    /**
     *
     */
    public RMIServerEvA getRMIServer() {
        return rmiServer;
    }

    /**
     *
     */
    private int getNumberOfVM(String[] list) {
        int ret = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].indexOf(EvAMainAdapterImpl.MAIN_ADAPTER_NAME) != -1) {
                ret++;
            }
        }
        return ret;
    }
}
