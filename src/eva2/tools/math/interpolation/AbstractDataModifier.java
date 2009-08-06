///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: AbstractDataModifier.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:24:58 $
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

/**
 * The minimal set of functions which should implemented in a data modifier for
 * <code>AbstractDataSet</code>.
 */
public abstract class AbstractDataModifier
{

	/*-------------------------------------------------------------------------*
	 * abstract methods
	 *-------------------------------------------------------------------------*/

	/**
	* Modifies the X data.
	*/
	public abstract void modifyX(double[] setX);
	/**
	* Modifies the Y data.
	*/
	public abstract void modifyY(double[] setY);
	/**
	* Modifies the data.
	*/
	public abstract void modify(double[] setX, double[] setY);
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/