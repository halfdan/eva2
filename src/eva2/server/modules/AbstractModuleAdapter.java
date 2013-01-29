package eva2.server.modules;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
import java.io.Serializable;
import java.net.InetAddress;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfaceProcessor;
import eva2.tools.jproxy.MainAdapterClient;
import eva2.tools.jproxy.RemoteStateListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The module server expects a constructor with two arguments: String adapterName and MainAdapterClient client.
 * 
 * 
 */
abstract public class AbstractModuleAdapter implements ModuleAdapter, Serializable {

    private static int instanceCounter;
    protected int instanceNumber;
    protected String adapterName;
    protected InterfaceProcessor processor;
    protected String hostName = "not defined";
    protected boolean hasConnection = true;
    protected ModuleAdapter remoteModuleAdapter = null;
    protected boolean useRMI = true;
    protected MainAdapterClient mainAdapterClient; // connection to client
    private List<RemoteStateListener> remoteStateListeners;

    protected AbstractModuleAdapter(MainAdapterClient client) {
        instanceCounter++;
        instanceNumber = instanceCounter;

        if (client != null) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                System.out.println("InetAddress.getLocalHost().getHostAddress() --> ERROR" + e.getMessage());
            }
        } else {
            hostName = "localhost";
        }

        if ((client == null) || client.getHostName().equals(hostName)) {
            useRMI = false;
        } else {
            /* we use RMI */
            useRMI = true;
        }
        remoteStateListeners = new ArrayList<RemoteStateListener>();
    }

    /**
     * From the interface RemoteStateListener. Added this method to make progress bar possible.
     */
    @Override
    public void updateProgress(final int percent, String msg) {
        for (RemoteStateListener listener : remoteStateListeners) {
            listener.updateProgress(percent, msg);
        }
    }

    /**
     * Get the name of the current adapter.
     * 
     * @return The adapter name
     */
    @Override
    public String getAdapterName() {
        return adapterName;
    }

    /**
     * Start optimization on processor.
     */
    @Override
    public void startOpt() {
        processor.startOpt();
    }

    /**
     * Restart optimization on processor.
     */
    @Override
    public void restartOpt() {
        processor.restartOpt();
    }

    /**
     * Stop optimization on processor.
     */
    @Override
    public void stopOpt() {
        // This means user break
        processor.stopOpt();
    }

    /**
     * Returns whether the current optimization provides post processing.
     * 
     * @return true if post processing is available
     */
    @Override
    public boolean hasPostProcessing() {
        return ((processor instanceof Processor) && ((Processor) processor).getGOParams().getPostProcessParams().isDoPostProcessing());
    }

    /**
     * Starts post processing if available.
     * 
     * @return true if post processing was performed, false otherwise.
     */
    @Override
    public boolean startPostProcessing() {
        if (hasPostProcessing() && ((Processor) processor).getGOParams().getPostProcessParams().isDoPostProcessing()) {
            ((Processor) processor).performPostProcessing();
            return true;
        } else {
            return false;
        }
    }

    public InterfaceGOParameters getGOParameters() {
        if ((processor != null) && (processor instanceof Processor)) {
            return ((Processor) processor).getGOParams();
        } else {
            return null;
        }
    }

    public void setGOParameters(InterfaceGOParameters goParams) {
        if ((processor != null) && (processor instanceof Processor)) {
            ((Processor) processor).setGOParams(goParams);
        }
    }

    public boolean isOptRunning() {
        if ((processor != null) && (processor instanceof Processor)) {
            return ((Processor) processor).isOptRunning();
        } else {
            return false;
        }
    }

    /**
     * Adds a remote state listener.
     */
    @Override
    public void addRemoteStateListener(RemoteStateListener remoteListener) {
        remoteStateListeners.add(remoteListener);
    }

    /**
     *
     */
    @Override
    public void setConnection(boolean flag) {
        hasConnection = flag;
    }

    /**
     * Returns whether the module has a connection.
     * 
     * @return true if the adapter has a connection.
     */
    @Override
    public boolean hasConnection() {
        return hasConnection;
    }

    /**
     *
     */
    @Override
    public void setRemoteThis(ModuleAdapter x) {
        remoteModuleAdapter = x;
    }

    /**
     * Returns the host name.
     * 
     * @return The host name
     */
    @Override
    public String getHostName() {
        return hostName;
    }

    /**
     *
     */
    @Override
    public void performedStop() {
        for (RemoteStateListener listener : remoteStateListeners) {
            listener.performedStop();
        }
    }

    @Override
    public void performedStart(String infoString) {
        for (RemoteStateListener listener : remoteStateListeners) {
            listener.performedStart(infoString);
        }
    }

    @Override
    public void performedRestart(String infoString) {
        for (RemoteStateListener listener : remoteStateListeners) {
            listener.performedRestart(infoString);
        }
    }
}