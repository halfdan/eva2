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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MainAdapterImpl implements MainAdapter {

    private static final Logger LOGGER = Logger.getLogger(MainAdapterImpl.class.getName());
    public static final String MAIN_ADAPTER_NAME = "MainRemoteObjectName";
    public static final int PORT = 1099;
    private String m_Buf = "";
    private MainAdapter remoteThis;

    /**
     *
     */
    public MainAdapterImpl() {
        remoteThis = this;
    }

    /**
     *
     */
    public void setBuf(String s) {
        m_Buf = s;
    }

    /**
     *
     */
    public void restartServer() {
        LOGGER.log(Level.INFO, "Received a Message to restart the server.");
        try {
            String command = "java -cp . eva2.server.EvAServer &";

            LOGGER.log(Level.INFO, "Calling the command:" + "java eva2.server.EvAServer");
            Process pro = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error while restarting the server.", ex);
        }
        killServer();
    }

    /**
     *
     */
    public void killServer() {
        LOGGER.log(Level.INFO, "Received a Message to kill the server.");
        KillThread x = new KillThread();
        x.start();
    }

    /**
     *
     */
    public String getBuf() {
        return m_Buf;
    }

    /**
     *
     */
    public String getExecOutput(String command) {
        StringBuffer output = new StringBuffer();
        try {
            BufferedReader in = null;
            Process pro = null;
            pro = Runtime.getRuntime().exec(command);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                output.append(line);
            }
        } catch (Exception e) {
            System.err.println("Error in calling the command:" + e.getMessage());
        }
        return output.toString();
    }

    /**
     *
     */
    public RMIInvocationHandler getRMIHandler(Object obj) {
        System.out.println("getRMIHandler");
        RMIInvocationHandler ret = null;
        try {
            ret = new RMIInvocationHandlerImpl(obj);
        } catch (Exception e) {
            System.out.println("Error: RMIInvokationHandler getRMIHandler");
        }
        return ret;
    }

    /**
     *
     */
    public RMIThreadInvocationHandler getRMIThreadHandler(Object obj) {
        RMIThreadInvocationHandler ret = null;
        try {

            ret = new RMIThreadInvocationHandlerImpl(obj);
        } catch (Exception e) {
            System.out.println("Error: RMIThreadInvokationHandler getRMIThreadHandler");
        }
        return ret;
    }

    /**
     *
     */
    public void setRemoteThis(MainAdapter x) {
        remoteThis = x;
    }
}

/**
 *
 */
class KillThread extends Thread {

    /**
     *
     */
    public void run() {
        try {
            sleep(3000);
        } catch (Exception e) {
            System.out.println("Error in sleep");
        }
        System.exit(-1);
    }
}
