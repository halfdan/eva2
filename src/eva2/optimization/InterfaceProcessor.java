package eva2.optimization;

import eva2.optimization.OptimizationStateListener;


/**
 * Interface for Optimization Processor.
 */
public interface InterfaceProcessor {

    /**
     * Start optimization.
     */
    void startOptimization();

    /**
     * Restart optimization.
     */
    void restartOptimization();

    /**
     * Stop optimization if running.
     */
    void stopOptimization();

    /**
     * Adds a new OptimizationStateListener.
     *
     * @param module The module to add.
     */
    void addListener(OptimizationStateListener module);

    /**
     * Get Info String about the Optimization.
     *
     * @return The info String
     */
    String getInfoString();
}
