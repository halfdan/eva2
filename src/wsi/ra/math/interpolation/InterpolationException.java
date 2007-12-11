///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: InterpolationException.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:25:17 $
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
 * Exception for interpolation error.
 */
public class InterpolationException extends Exception
{

	public InterpolationException()
	{
		super();
	}
	public InterpolationException(String s)
	{
		super(s);
	}
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/