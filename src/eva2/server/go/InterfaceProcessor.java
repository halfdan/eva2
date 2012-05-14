package eva2.server.go;

import eva2.tools.jproxy.RemoteStateListener;

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

/**
 * Interface for Optimization Processor.
 */
public interface InterfaceProcessor {

    /**
     * Start optimization.
     */
    void startOpt();

    /**
     * Restart optimization.
     */
    void restartOpt();

    /**
     * Stop optimization if running.
     */
    void stopOpt();

    /**
     * Adds a new RemoteStateListener.
     *
     * @param module The module to add.
     */
    void addListener(RemoteStateListener module);

    /**
     * Get Info String about the Optimization.
     * @return The info String
     */
    String getInfoString();
}
