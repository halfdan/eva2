///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: AbstractDataSet.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:25:04 $
//            $Author: wegnerj $
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
///////////////////////////////////////////////////////////////////////////////

package eva2.tools.math.interpolation;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/

public abstract class AbstractDataSet {
    /*-------------------------------------------------------------------------*
     * public member variables
	 *-------------------------------------------------------------------------*/

	/*--------------------------------------------------------------o-----------*
     * protected member variables
	 *-------------------------------------------------------------------------*/
    /**
     * double array of X data.
     *
     * @see #yDoubleData
     */
    protected double[] xDoubleData = {-1, 1};
    /**
     * double array of Y data.
     *
     * @see #xDoubleData
     */
    protected double[] yDoubleData = {1, 1};

	/*-------------------------------------------------------------------------*
	 * abstract methods
	 *-------------------------------------------------------------------------*/

    /**
     * Returns the length of the data set
     *
     * @return the length of the data set
     */
    public int getLength() {
        return xDoubleData.length;
    }

    /**
     * Returns an array of the X data
     *
     * @return the array of the X data
     */
    public double[] getXData() {
        return xDoubleData;
    }

    /**
     * Returns an array of the Y data
     *
     * @return the array of the Y data
     */
    public double[] getYData() {
        return yDoubleData;
    }

    /**
     * Returns the X label of the data set
     *
     * @return the X label of the data set
     */
    public abstract String getXLabel();

    /**
     * Returns the Y label of the data set
     *
     * @return the Y label of the data set
     */
    public abstract String getYLabel();

    /**
     * Modifies the X data.
     *
     * @param the data modifier
     */
    public void modifyXData(AbstractDataModifier modifier) {
        modifier.modifyX(xDoubleData);
    }

    /**
     * Modifies the Y data.
     *
     * @param the data modifier
     */
    public void modifyYData(AbstractDataModifier modifier) {
        modifier.modifyY(yDoubleData);
    }

    /**
     * Modifies the data.
     *
     * @param the data modifier
     */
    public void modifyData(AbstractDataModifier modifier) {
        modifier.modify(xDoubleData, yDoubleData);
    }
}
/****************************************************************************
 * END OF FILE
 ****************************************************************************/
