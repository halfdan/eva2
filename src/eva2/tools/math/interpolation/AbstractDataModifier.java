package eva2.tools.math.interpolation;

/**
 * The minimal set of functions which should implemented in a data modifier for
 * <code>AbstractDataSet</code>.
 */
public abstract class AbstractDataModifier {

    /**
     * Modifies the X data.
     */
    public abstract void modifyX(double[] setX);

    /**
     * Modifies the Y data.
     */
    public abstract void modifyY(double[] setY);

    /**
     * Modifies the data.
     */
    public abstract void modify(double[] setX, double[] setY);
}