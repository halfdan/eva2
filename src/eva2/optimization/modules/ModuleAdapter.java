package eva2.optimization.modules;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 272 $
 *            $Date: 2007-11-21 18:06:36 +0100 (Wed, 21 Nov 2007) $
 *            $Author: mkron $
 */

import eva2.gui.EvATabbedFrameMaker;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.stat.OptimizationJob;

/**
 *
 */
public interface ModuleAdapter extends OptimizationStateListener {

    EvATabbedFrameMaker getModuleFrame();

    void startOpt(); // called from client

    /**
     * Schedule a certain job to a job list.
     *
     * @return A new Job
     */
    OptimizationJob scheduleJob();

    void restartOpt();

    void stopOpt();

    //void runScript();

    /**
     * Return true if post processing is available in principle, else false.
     *
     * @return true if post processing is available in principle, else false
     */
    boolean hasPostProcessing();

    /**
     * Return true if post processing was performed, else false.
     *
     * @return true if post processing was performed, else false
     */
    boolean startPostProcessing();

    void addOptimizationStateListener(OptimizationStateListener x);

    String getAdapterName();

    void setConnection(boolean flag);

    boolean hasConnection();

    void setRemoteThis(ModuleAdapter x);

    String getHostName();
}