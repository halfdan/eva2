package eva2.json;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OptimizationRun {
    List<Double> fitness;

    public OptimizationRun() {
        this.fitness = new ArrayList<Double>();
    }

    public void addFitnessValue(double value) {
        this.fitness.add(value);
    }
}
