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

package wsi.ra.math.interpolation;

import wsi.ra.sort.XYDoubleArray;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

/**
 * The minimum wrapper class for an <code>AbstractDataSet</code>.
 */
public class BasicDataSet extends AbstractDataSet
{
	/*-------------------------------------------------------------------------*
	 * protected member variables
	 *-------------------------------------------------------------------------*/
	protected int dataType = -1;
	protected String xLabel = null;
	protected String yLabel = null;

	/*------------------------------------------------------------------------*
	 * constructor
	 *------------------------------------------------------------------------*/
	public BasicDataSet()
	{
		this(null, null, null, null);
	}

	public BasicDataSet(XYDoubleArray data)
	{
		this(data.x, data.y, null, null);
	}

	public BasicDataSet(XYDoubleArray data, String xLabel, String yLabel)
	{
		this(data.x, data.y, xLabel, yLabel);
	}

	public BasicDataSet(
		double[] xDoubleData,
		double[] yDoubleData,
		int dataType)
	{
		this(xDoubleData, yDoubleData, null, null);
	}

	public BasicDataSet(
		double[] xDoubleData,
		double[] yDoubleData,
		String xLabel,
		String yLabel)
	{
		this.xDoubleData = xDoubleData;
		this.yDoubleData = yDoubleData;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
	}

	/*-------------------------------------------------------------------------*
	* public methods
	*-------------------------------------------------------------------------*/

	public int getDataType()
	{
		return dataType;
	}

	public String getXLabel()
	{
		return xLabel;
	}

	public String getYLabel()
	{
		return yLabel;
	}

	public String getAdditionalInformation(String parm1)
	{
		return new String();
	}
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/