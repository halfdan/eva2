package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 12.09.2005
 * Time: 10:18:50
 * To change this template use File | Settings | File Templates.
 */
public class PropertyIntArray implements java.io.Serializable {
    public int[] m_IntArray;

    public PropertyIntArray(int[] d) {
        this.m_IntArray = d;
    }

    public PropertyIntArray(PropertyIntArray d) {
        this.m_IntArray = new int[d.m_IntArray.length];
        System.arraycopy(d.m_IntArray, 0, this.m_IntArray, 0, this.m_IntArray.length);
    }

    @Override
    public Object clone() {
        return (Object) new PropertyIntArray(this);
    }

    /**
     * This method will allow you to set the value of the double array
     *
     * @param d The int[]
     */
    public void setIntArray(int[] d) {
        this.m_IntArray = d;
    }

    /**
     * This method will return the int array
     *
     * @return The int array
     */
    public int[] getIntArray() {
        return this.m_IntArray;
    }
}