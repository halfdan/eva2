package eva2.optimization.go;

import eva2.optimization.OptimizationStateListener;


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
     * Adds a new OptimizationStateListener.
     *
     * @param module The module to add.
     */
    void addListener(OptimizationStateListener module);

    /**
     * Get Info String about the Optimization.
     * @return The info String
     */
    String getInfoString();
}
