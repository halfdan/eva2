///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: PolynomialInterpolationResult.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:25:36 $
//            $Author: wegnerj $
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
///////////////////////////////////////////////////////////////////////////////

package wsi.ra.math.interpolation;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * The result data for a polynomial interpolation.
 */
public class PolynomialInterpolationResult
{
	/*-------------------------------------------------------------------------*
	 * public member variables
	 *-------------------------------------------------------------------------*/

	public double y = Double.NaN;
	public double yError = Double.NaN;

	/*------------------------------------------------------------------------*
	 * constructor
	 *------------------------------------------------------------------------*/

	public PolynomialInterpolationResult()
	{
		y = Double.NaN;
		yError = Double.NaN;
	}

	public PolynomialInterpolationResult(double y, double yError)
	{
		this.y = y;
		this.yError = yError;
	}
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/