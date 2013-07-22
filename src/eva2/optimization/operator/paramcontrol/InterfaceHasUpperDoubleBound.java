package eva2.optimization.operator.paramcontrol;

/**
 * Interface for an instance which has a real valued upper bound of some kind which
 * can be mutated.
 *
 * @author mkron
 */
public interface InterfaceHasUpperDoubleBound {
    public double getUpperBnd();

    public void SetUpperBnd(double u);
}
