package eva2.optimization.modules;
import eva2.gui.EvATabbedFrameMaker;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.stat.OptimizationJob;

/**
 *
 */
public interface ModuleAdapter extends OptimizationStateListener {

    EvATabbedFrameMaker getModuleFrame();

    void startOptimization(); // called from client

    /**
     * Schedule a certain job to a job list.
     *
     * @return A new Job
     */
    OptimizationJob scheduleJob();

    void stopOptimization();

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
}