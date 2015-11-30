package eva2.optimization.operator.constraint;

/**
 * A constraint delivering a double valued degree of violation.
 */
public interface InterfaceDoubleConstraint {
    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    Object clone();

    /**
     * Returns the boolean information whether the constraint is satisfied.
     *
     * @param indyX
     * @return
     */
    boolean isSatisfied(double[] indyX);

    /**
     * Return the absolute (positive) degree of violation or zero if the constraint
     * is fulfilled.
     *
     * @param indyX possibly the decoded individual position
     * @return true if valid false else.
     */
    double getViolation(double[] indyX);
}
