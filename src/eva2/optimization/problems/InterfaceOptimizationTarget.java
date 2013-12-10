package eva2.optimization.problems;

/**
 *
 */
public interface InterfaceOptimizationTarget {

    public Object clone();

    /**
     * This method allows you to retrieve the name of the optimization target
     *
     * @return The name
     */
    public String getName();
}
