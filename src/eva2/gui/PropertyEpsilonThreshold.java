package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 14:56:58
 * To change this template use File | Settings | File Templates.
 */
public class PropertyEpsilonThreshold implements java.io.Serializable {

    public double[]     m_Punishment;
    public double[]     m_TargetValue;
    public int          m_OptimizeObjective;

    public PropertyEpsilonThreshold() {
    }

    public PropertyEpsilonThreshold(PropertyEpsilonThreshold e) {
        if (e.m_TargetValue != null) {
            this.m_TargetValue = new double[e.m_TargetValue.length];
            System.arraycopy(e.m_TargetValue, 0, this.m_TargetValue, 0, this.m_TargetValue.length);
        }
        if (e.m_Punishment != null) {
            this.m_Punishment = new double[e.m_Punishment.length];
            System.arraycopy(e.m_Punishment, 0, this.m_Punishment, 0, this.m_Punishment.length);
        }
        this.m_OptimizeObjective = e.m_OptimizeObjective;
    }

    @Override
    public Object clone() {
        return (Object) new PropertyEpsilonThreshold(this);
    }
}
