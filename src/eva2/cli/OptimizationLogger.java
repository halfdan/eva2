package eva2.cli;

import eva2.optimization.OptimizationStateListener;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.population.InterfacePopulationChangedEventListener;

import java.io.OutputStream;
import java.util.LinkedHashMap;

/**
 *
 */
public class OptimizationLogger implements InterfacePopulationChangedEventListener, OptimizationStateListener {
    private final OutputStream outputStream;
    private final InterfaceOptimizationParameters optimizationParameters;
    private LinkedHashMap<String, Object> optimizationData;

    public OptimizationLogger(InterfaceOptimizationParameters optimizationParameters, OutputStream outputStream) {
        this.optimizationParameters = optimizationParameters;
        this.outputStream = outputStream;
        this.optimizationData = new LinkedHashMap<>(5);
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
