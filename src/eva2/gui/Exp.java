package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import wsi.ra.chart2d.DFunction;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 *
 */
public class Exp extends DFunction{
  public boolean isDefinedAt( double source ){ return true; }
  public boolean isInvertibleAt( double image ){ return image > 0; }
  public double getImageOf( double source ){ return Math.exp( source ); }
  public double getSourceOf( double target ){
    if( target <= 0 )  { throw
      new IllegalArgumentException(
        "Can not calculate log on values smaller than or equal 0 --> target = "+target
      );
    }
    return Math.log( target );
  }
}

