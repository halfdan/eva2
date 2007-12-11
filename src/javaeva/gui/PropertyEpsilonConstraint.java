package javaeva.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.07.2005
 * Time: 16:15:47
 * To change this template use File | Settings | File Templates.
 */
public class PropertyEpsilonConstraint implements java.io.Serializable {

    public double[]     m_TargetValue;
    public int          m_OptimizeObjective;

    public PropertyEpsilonConstraint() {
    }

    public PropertyEpsilonConstraint(PropertyEpsilonConstraint e) {
        if (e.m_TargetValue != null) {
            this.m_TargetValue = new double[e.m_TargetValue.length];
            System.arraycopy(e.m_TargetValue, 0, this.m_TargetValue, 0, this.m_TargetValue.length);
        }
        this.m_OptimizeObjective = e.m_OptimizeObjective;
    }

    public Object clone() {
        return (Object) new PropertyEpsilonConstraint(this);
    }
}
