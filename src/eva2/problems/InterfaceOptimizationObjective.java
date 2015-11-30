package eva2.problems;

/**
 *
 */
public interface InterfaceOptimizationObjective {
    Object clone();

    /**
     * This Method returns the name for the optimization target
     *
     * @return the name
     */
    String getName();

    /**
     * This method allows you to retrieve the name of the optimization target
     *
     * @return The name
     */
    String getIdentName();

    /**
     * This method allows you to retrieve the current optimization mode
     * The modes include
     * - Objective
     * - Objective + Constraint
     * - Constraint
     * (-Goal !?)
     *
     * @return The mode as string
     */
    String getOptimizationMode();

    void SetOptimizationMode(String d);

    /**
     * This method allows you to retrieve the constraint/goal
     *
     * @return The cosntraint/goal
     */
    double getConstraintGoal();

    /**
     * This method allows you to set the constraint/goal
     *
     * @param d the constraint/goal
     */
    void SetConstraintGoal(double d);

    /**
     * This method returns whether or not the given objective is to be minimized
     *
     * @return True if to be minimized false else.
     */
    boolean is2BMinimized();
}
