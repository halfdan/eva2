/**
 *  Filename: $RCSfile: DFunction.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.1.1 $
 *            $Date: 2003/07/03 14:59:41 $
 *            $Author: ulmerh $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.Graphics;
import java.awt.Point;

/*==========================================================================*
 * ABSTRACT CLASS DECLARATION
 *==========================================================================*/

public abstract class DFunction extends DComponent
{
  public DFunction(){
    rectangle = DRectangle.getAll();
  }

  public abstract boolean isDefinedAt( double source );
  public abstract boolean isInvertibleAt( double image );
  public abstract double getSourceOf( double image ) throws IllegalArgumentException;
  public abstract double getImageOf( double source ) throws IllegalArgumentException;

  public void paint( DMeasures m ){
    Graphics g = m.getGraphics();
    if( color != null ) g.setColor( color );

    SlimRect rect = m.getSlimRectangle(),
               src_rect = m.getSourceOf( rect );
    Point sw = m.getPoint( rect.x, rect.y ),
          ne = m.getPoint( rect.x + rect.width, rect.y + rect.height );
    int int_w = ne.x - sw.x;
    Point last = null, next;
    for( int i = 0; i < int_w; i++ ){
      double x_val = src_rect.x + i / (double)int_w * src_rect.width ;
      if( m.x_scale != null ) x_val = m.x_scale.getImageOf( x_val );
      if( isDefinedAt( x_val ) ){
        next = m.getPoint( x_val, getImageOf( x_val ) );
        if( last != null ) g.drawLine( last.x, last.y, next.x, next.y );
        last = next;
      }
      else last = null;
    }
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
