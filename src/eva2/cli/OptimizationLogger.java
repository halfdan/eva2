package eva2.cli;

import eva2.optimization.OptimizationStateListener;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.population.InterfacePopulationChangedEventListener;

import java.io.OutputStream;

/**
 *
 */
public class OptimizationLogger implements InterfacePopulationChangedEventListener, OptimizationStateListener {
    private final OutputStream outputStream;
    private final InterfaceOptimizationParameters optimizationParameters;

    public OptimizationLogger(InterfaceOptimizationParameters optimizationParameters, OutputStream outputStream) {
        this.optimizationParameters = optimizationParameters;
        this.outputStream = outputStream;
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {

    }

    @Override
    public void performedStop() {

    }

    @Override
    public void performedStart(String infoString) {

    }

    @Override
    public void performedRestart(String infoString) {

    }

    @Override
    public void updateProgress(int percent, String msg) {

    }
}
