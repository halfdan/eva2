/**
 *  Filename: $RCSfile: DGrid.java,v $
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

import java.awt.Color ;
import java.awt.Graphics ;

import eva2.tools.Mathematics;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * this class paints a grid with certain line distances on a DParent
 */
public class DGrid extends DComponent
{
  /**
   * the distances between the lines
   */
  private double hor_dist, ver_dist;

  private Color DEFAULT_COLOR = Color.lightGray;

  /**
   * constructor with the size and position of the grid and the line distances
   *
   * @param rectangle the rectangle around the grid
   * @param hor_dist the horizontal distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   * @param ver_dist vertical distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   */
  public DGrid( DRectangle rectangle, double hor_dist, double ver_dist ){
    this.rectangle = rectangle;
    this.hor_dist = hor_dist;
    this.ver_dist = ver_dist;
    color = DEFAULT_COLOR;
  }

  /**
   * constructor with the size and position of the grid and the line distances
   *
   * @param rectangle the rectangle around the grid
   * @param hor_dist the horizontal distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   * @param ver_dist the vertical distance between the lines in D-coordinates,
   *        not in pixel coordinates!
   * @param color the color of the grid
   *        ( can also be set by setColor( java.awt.Color ) )
   */
  public DGrid( DRectangle rectangle, double hor_dist, double ver_dist, Color color ){
    this.rectangle = rectangle;
    this.hor_dist = hor_dist;
    this.ver_dist = ver_dist;
    this.color = color;
  }
  
  public void setDistances(double hor, double ver) {
	  hor_dist=hor;
	  ver_dist=ver;
//	  System.out.println("set new Grid distances " + this.toString());
  }

  public double getHorDist() {
	  return hor_dist;
  }
  
  public double getVerDist() {
	  return ver_dist;
  }
  /**
   * paints the grid...
   *
   * @param m the <code>DMeasures</code> object to paint the grid
   */
  public void paint( DMeasures m ){
    Graphics g = m.getGraphics();
    if( color != null ) g.setColor( color );
    double minX, minY, pos;
    DPoint p1, p2;
    DLine l;

    minX=Mathematics.firstMultipleAbove(rectangle.x, hor_dist);
    minY=Mathematics.firstMultipleAbove(rectangle.y, ver_dist);
//    minX = (int)( rectangle.x / hor_dist );
//    if( minX * hor_dist <= rectangle.x ) minX++;
//    minX *= hor_dist;
//    minY = (int)( rectangle.y / ver_dist );
//    if( minY * ver_dist <= rectangle.y ) minY++;
//    minY *= ver_dist;

    p1 = new DPoint( 0, rectangle.y );
    p2 = new DPoint( 0, rectangle.y + rectangle.height );
    for( pos = minX; pos<=rectangle.x + rectangle.width; pos += hor_dist ){
      p1.x = p2.x = pos;
      l = new DLine( p1, p2, color );
      l.paint( m );
    }

    p1.x = rectangle.x;
    p2.x = p1.x + rectangle.width;
    pos = minY;
    while ( pos<=rectangle.y + rectangle.height){
      p1.y = p2.y = pos;
      l = new DLine( p1, p2, color );
      l.paint( m );
      if (pos+ver_dist<=pos) {
    	  System.err.println("Overflow error in DGrid!");
    	  pos *= 1.01;
      } else pos += ver_dist;
//      System.out.println("pos is " + pos + ", loop until " + rectangle.y + rectangle.height);
    }
  }

  public String toString(){
    return "chart2d.DGrid[ hor: "+hor_dist+", ver: "+ver_dist+" ]";
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
