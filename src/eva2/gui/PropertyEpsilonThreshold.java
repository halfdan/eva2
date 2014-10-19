package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 14:56:58
 * To change this template use File | Settings | File Templates.
 */
public class PropertyEpsilonThreshold implements java.io.Serializable {

    public double[] punishment;
    public double[] targetValue;
    public int optimizeObjective;

    public PropertyEpsilonThreshold() {
    }

    public PropertyEpsilonThreshold(PropertyEpsilonThreshold e) {
        if (e.targetValue != null) {
            this.targetValue = new double[e.targetValue.length];
            System.arraycopy(e.targetValue, 0, this.targetValue, 0, this.targetValue.length);
        }
        if (e.punishment != null) {
            this.punishment = new double[e.punishment.length];
            System.arraycopy(e.punishment, 0, this.punishment, 0, this.punishment.length);
        }
        this.optimizeObjective = e.optimizeObjective;
    }

    @Override
    public Object clone() {
        return new PropertyEpsilonThreshold(this);
    }
}
