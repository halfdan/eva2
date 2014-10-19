package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.07.2005
 * Time: 16:15:47
 * To change this template use File | Settings | File Templates.
 */
public class PropertyEpsilonConstraint implements java.io.Serializable {

    public double[] targetValue;
    public int optimizeObjective;

    public PropertyEpsilonConstraint() {
    }

    public PropertyEpsilonConstraint(PropertyEpsilonConstraint e) {
        if (e.targetValue != null) {
            this.targetValue = new double[e.targetValue.length];
            System.arraycopy(e.targetValue, 0, this.targetValue, 0, this.targetValue.length);
        }
        this.optimizeObjective = e.optimizeObjective;
    }

    @Override
    public Object clone() {
        return new PropertyEpsilonConstraint(this);
    }
}
