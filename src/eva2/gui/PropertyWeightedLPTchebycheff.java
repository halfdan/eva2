package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.07.2005
 * Time: 10:16:10
 * To change this template use File | Settings | File Templates.
 */
public class PropertyWeightedLPTchebycheff implements java.io.Serializable {

    public double[]     m_IdealValue;
    public double[]     m_Weights;
    public int          m_P = 0;

    public PropertyWeightedLPTchebycheff() {
    }

    public PropertyWeightedLPTchebycheff(PropertyWeightedLPTchebycheff e) {
        if (e.m_IdealValue != null) {
            this.m_IdealValue = new double[e.m_IdealValue.length];
            System.arraycopy(e.m_IdealValue, 0, this.m_IdealValue, 0, this.m_IdealValue.length);
        }
        if (e.m_Weights != null) {
            this.m_Weights = new double[e.m_Weights.length];
            System.arraycopy(e.m_Weights, 0, this.m_Weights, 0, this.m_Weights.length);
        }
        this.m_P            = e.m_P;
    }

    public Object clone() {
        return (Object) new PropertyWeightedLPTchebycheff(this);
    }
}
