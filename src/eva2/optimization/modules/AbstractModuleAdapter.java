package eva2.optimization.modules;


import eva2.optimization.InterfaceProcessor;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.InterfaceOptimizationParameters;
import eva2.optimization.Processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The module server expects a constructor with two arguments: String adapterName and MainAdapterClient client.
 */
abstract public class AbstractModuleAdapter implements ModuleAdapter, Serializable {

    private static int instanceCounter;
    protected int instanceNumber;
    protected String adapterName;
    protected InterfaceProcessor processor;
    protected ModuleAdapter remoteModuleAdapter = null;
    private List<OptimizationStateListener> optimizationStateListeners;

    protected AbstractModuleAdapter() {
        instanceCounter++;
        instanceNumber = instanceCounter;
        optimizationStateListeners = new ArrayList<>();
    }

    /**
     * From the interface OptimizationStateListener. Added this method to make progress bar possible.
     */
    @Override
    public void updateProgress(final int percent, String msg) {
        for (OptimizationStateListener listener : optimizationStateListeners) {
            listener.updateProgress(percent, msg);
        }
    }

    /**
     * Start optimization on processor.
     */
    @Override
    public void startOptimization() {
        processor.startOptimization();
    }

    /**
     * Stop optimization on processor.
     */
    @Override
    public void stopOptimization() {
        // This means user break
        processor.stopOptimization();
    }

    /**
     * Returns whether the current optimization provides post processing.
     *
     * @return true if post processing is available
     */
    @Override
    public boolean hasPostProcessing() {
        return ((processor instanceof Processor) && ((Processor) processor).getOptimizationParameters().getPostProcessParams().isDoPostProcessing());
    }

    /**
     * Starts post processing if available.
     *
     * @return true if post processing was performed, false otherwise.
     */
    @Override
    public boolean startPostProcessing() {
        if (hasPostProcessing() && ((Processor) processor).getOptimizationParameters().getPostProcessParams().isDoPostProcessing()) {
            ((Processor) processor).performPostProcessing();
            return true;
        } else {
            return false;
        }
    }

    public InterfaceOptimizationParameters getOptimizationParameters() {
        if ((processor != null) && (processor instanceof Processor)) {
            return ((Processor) processor).getOptimizationParameters();
        } else {
            return null;
        }
    }

    public void setOptimizationParameters(InterfaceOptimizationParameters goParams) {
        if ((processor != null) && (processor instanceof Processor)) {
            ((Processor) processor).setOptimizationParameters(goParams);
        }
    }

    /**
     * Adds a remote state listener.
     */
    @Override
    public void addOptimizationStateListener(OptimizationStateListener remoteListener) {
        optimizationStateListeners.add(remoteListener);
    }
    /**
     *
     */
    @Override
    public void performedStop() {
        for (OptimizationStateListener listener : optimizationStateListeners) {
            listener.performedStop();
        }
    }

    @Override
    public void performedStart(String infoString) {
        for (OptimizationStateListener listener : optimizationStateListeners) {
            listener.performedStart(infoString);
        }
    }

    @Override
    public void performedRestart(String infoString) {
        for (OptimizationStateListener listener : optimizationStateListeners) {
            listener.performedRestart(infoString);
        }
    }
}