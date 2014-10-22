package eva2.tools.math.interpolation;

/**
 * The minimum wrapper class for an <code>AbstractDataSet</code>.
 */
public class BasicDataSet extends AbstractDataSet {
    protected int dataType = -1;
    protected String xLabel = null;
    protected String yLabel = null;

    public BasicDataSet() {
        this(null, null, null, null);
    }

    public BasicDataSet(
            double[] xDoubleData,
            double[] yDoubleData,
            int dataType) {
        this(xDoubleData, yDoubleData, null, null);
    }

    public BasicDataSet(
            double[] xDoubleData,
            double[] yDoubleData,
            String xLabel,
            String yLabel) {
        this.xDoubleData = xDoubleData;
        this.yDoubleData = yDoubleData;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    public int getDataType() {
        return dataType;
    }

    @Override
    public String getXLabel() {
        return xLabel;
    }

    @Override
    public String getYLabel() {
        return yLabel;
    }

    public String getAdditionalInformation(String parm1) {
        return "";
    }
}
