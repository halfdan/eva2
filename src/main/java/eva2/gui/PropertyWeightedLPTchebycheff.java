package eva2.gui;

/**
 *
 */
public class PropertyWeightedLPTchebycheff implements java.io.Serializable {

    public double[] idealValue;
    public double[] weights;
    public int p = 0;

    public PropertyWeightedLPTchebycheff() {
    }

    public PropertyWeightedLPTchebycheff(PropertyWeightedLPTchebycheff e) {
        if (e.idealValue != null) {
            this.idealValue = new double[e.idealValue.length];
            System.arraycopy(e.idealValue, 0, this.idealValue, 0, this.idealValue.length);
        }
        if (e.weights != null) {
            this.weights = new double[e.weights.length];
            System.arraycopy(e.weights, 0, this.weights, 0, this.weights.length);
        }
        this.p = e.p;
    }

    @Override
    public Object clone() {
        return new PropertyWeightedLPTchebycheff(this);
    }
}
