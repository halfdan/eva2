///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: BasicDataSet.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:25:11 $
//            $Author: wegnerj $
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
///////////////////////////////////////////////////////////////////////////////

package eva2.tools.math.interpolation;

/**
 * The minimum wrapper class for an <code>AbstractDataSet</code>.
 */
public class BasicDataSet extends AbstractDataSet {
	protected int dataType = -1;
	protected String xLabel = null;
	protected String yLabel = null;

	public BasicDataSet()
	{
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

	public int getDataType()
	{
		return dataType;
	}

    @Override
	public String getXLabel()
	{
		return xLabel;
	}

    @Override
	public String getYLabel()
	{
		return yLabel;
	}

	public String getAdditionalInformation(String parm1)
	{
		return new String();
	}
}
