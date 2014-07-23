package eva2.problems;

/**
 * This class is under construction.
 */
public abstract class AbstractParallelOptimizationProblem extends AbstractOptimizationProblem {
    private int localCPUs = 4;
    private boolean parallelize = false;

    @Override
    public void initializeProblem() {
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a framework for parallelizing expensive optimization problems.";
    }

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

    public void setParallelize(boolean b) {
        this.parallelize = b;
    }

    public String parallelizeTipText() {
        return "Toggle between parallel and serial implementation.";
    }

    /**
     * This method allows you to set the number of processors in local mode
     *
     * @param n Number of processors.
     */
    public void setNumberLocalCPUs(int n) {
        this.localCPUs = n;
    }

    public int getNumberLocalCPUs() {
        return this.localCPUs;
    }

    public String numberLocalCPUsTipText() {
        return "Set the number of local CPUS (only active in non-parallelized mode).";
    }
}
