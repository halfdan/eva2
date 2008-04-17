package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 13:48:47
 * To change this template use File | Settings | File Templates.
 */
public class PropertyDoubleArray implements java.io.Serializable {
    public double[] m_DoubleArray;

    public PropertyDoubleArray(double[] d) {
        this.m_DoubleArray = d;
    }

    public PropertyDoubleArray(PropertyDoubleArray d) {
        this.m_DoubleArray = new double[d.m_DoubleArray.length];
        System.arraycopy(d.m_DoubleArray, 0, this.m_DoubleArray, 0, this.m_DoubleArray.length);
    }

    public Object clone() {
        return (Object) new PropertyDoubleArray(this);
    }

    /** This method will allow you to set the value of the double array
     * @param d     The double[]
     */
    public void setDoubleArray(double[] d) {
        this.m_DoubleArray = d;
    }

    /** This method will return the complete name of the file
     * which filepath
     * @return The complete filename with path.
     */
    public double[] getDoubleArray() {
        return this.m_DoubleArray;
    }
}