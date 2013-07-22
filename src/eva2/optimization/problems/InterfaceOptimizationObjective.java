package eva2.optimization.problems;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.08.2005
 * Time: 13:53:00
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceOptimizationObjective {
    public Object clone();

    /**
     * This Method returns the name for the optimization target
     *
     * @return the name
     */
    public String getName();

    /**
     * This method allows you to retrieve the name of the optimization target
     *
     * @return The name
     */
    public String getIdentName();

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
    public String getOptimizationMode();

    public void SetOptimizationMode(String d);

    /**
     * This method allows you to retrieve the constraint/goal
     *
     * @return The cosntraint/goal
     */
    public double getConstraintGoal();

    /**
     * This method allows you to set the constraint/goal
     *
     * @param d the constraint/goal
     */
    public void SetConstraintGoal(double d);

    /**
     * This method returns whether or not the given objective is to be minimized
     *
     * @return True if to be minimized false else.
     */
    public boolean is2BMinimized();
}
