package eva2.optimization.operator.terminators;

import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

/**
 * This terminator stops the optimization after maximumTime seconds have passed.
 *
 * Note: Actual execution time depends on when the termination criteria is checked
 * (usually after a generation has been performed) and on the precision of the system
 * clock. Runs will terminate after a minimum of maximumTime seconds.
 */
@Description("Terminate if a maximum time (seconds) was reached.")
public class MaximumTimeTerminator implements InterfaceTerminator {
    private int maximumTime = 5;

    private long startTime;

    @Override
    public boolean isTerminated(PopulationInterface pop) {
        return (startTime/1000.0) + maximumTime < (System.currentTimeMillis()/1000.0);
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet pop) {
        return (startTime/1000.0) + maximumTime < (System.currentTimeMillis()/1000.0);
    }

    @Override
    public String lastTerminationMessage() {
        return "Maximum Time of " + maximumTime + " seconds reached";
    }

    @Override
    public void initialize(InterfaceOptimizationProblem prob) {
        startTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "MaximumTimeTerminator,maximumTime=" + maximumTime;
    }

    public int getMaximumTime() {
        return maximumTime;
    }

    @Parameter(name = "time", description = "Maximum time in seconds")
    public void setMaximumTime(int maximumTime) {
        this.maximumTime = maximumTime;
    }
}
