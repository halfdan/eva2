package eva2.optimization.operators.constraint;

/** 
 * A constraint delivering a double valued degree of violation.
 */
public interface InterfaceDoubleConstraint {
    /** 
     * This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone();

    /**
     * Returns the boolean information whether the constraint is satisfied.
     * 
     * @param indy
     * @return
     */
    public boolean isSatisfied(double[] indyX);
    
    /** 
     * Return the absolute (positive) degree of violation or zero if the constraint
     * is fulfilled.
     * 
     * @param indy  The individual to check.
     * @param indyX possibly the decoded individual position
     * @return true if valid false else.
     */
    public double getViolation(double[] indyX);
}
