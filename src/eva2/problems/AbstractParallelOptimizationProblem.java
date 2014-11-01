package eva2.problems;

import eva2.util.annotation.Parameter;

/**
 * This class is under construction.
 */
public abstract class AbstractParallelOptimizationProblem extends AbstractOptimizationProblem {
    private int localCPUs = 4;
    private boolean parallelize = false;

    @Override
    public void initializeProblem() { }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "Parallel Optimization Problem";
    }

    /**
     * This method allows you to toggle between a truly parallel
     * and a serial implementation.
     *
     * @return The current optimzation mode
     */
    public boolean getParallelize() {
        return this.parallelize;
    }

    @Parameter(description = "Toggle between parallel and serial implementation.")
    public void setParallelize(boolean b) {
        this.parallelize = b;
    }

    /**
     * This method allows you to set the number of processors in local mode
     *
     * @param n Number of processors.
     */
    @Parameter(name = "cpu", description = "Set the number of local CPUS (only active in non-parallelized mode).")
    public void setNumberLocalCPUs(int n) {
        this.localCPUs = n;
    }

    public int getNumberLocalCPUs() {
        return this.localCPUs;
    }
}
