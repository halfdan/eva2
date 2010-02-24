/**
 *  Filename: $RCSfile: DRectangle.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.1.1 $
 *            $Date: 2003/07/03 14:59:42 $
 *            $Author: ulmerh $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package eva2.tools.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.* ;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

public class DRectangle extends DComponent
{
  private double  x, y;
  private double width, height;
  public static final int PART = 0, ALL = 1, EMPTY = 2;
  protected int status;
  protected Color fillColor;

  private DRectangle( int status ){
    super(true);
    this.status = status;
  }

  public DRectangle( double x, double y, double width, double height ){
    super(true);
    this.x = x;
    this.y = y;
    if( width < 0  || Double.isInfinite(width) || Double.isNaN(width)) throw
      new IllegalArgumentException("Width of a DRectangle is invalid (" + width + ")");
    this.width = width;
    if( height < 0 || Double.isInfinite(height) || Double.isNaN(height)) throw
      new IllegalArgumentException("Height of a DRectangle is invalid (" + height + ")");
    this.height = height;
    status = PART;
  }

  public DRectangle getRectangle(){ return this; }

  public void paint( DMeasures m ){
    if( isEmpty() ) return;
    Graphics g = m.getGraphics();
    Color old_color = g.getColor();
    SlimRect rect = m.getSlimRectangle().getIntersection(this);
    Point p1 = m.getPoint( rect.x, rect.y ),
          p2 = m.getPoint( rect.x + rect.width, rect.y + rect.height );
    if( fillColor != null ){
      g.setColor( fillColor );
      g.fillRect( p1.x, p2.y, p2.x - p1.x, p1.y - p2.y );
    }
    if( !isAll() ){
      if( color != null ) g.setColor( color );
      else g.setColor( DEFAULT_COLOR );
      g.drawRect( p1.x, p2.y, p2.x - p1.x, p1.y - p2.y );
    }
    g.setColor( old_color );
  }

  public boolean contains( DPoint p ){
    if( status == ALL ) return true;
    if( status == EMPTY ) return false;
    if( p.x < x ) return false;
    if( p.y < y ) return false;
    if( p.x > x + width ) return false;
    if( p.y > y + height ) return false;
    return true;
  }
  
  public double getHeight() { return height; }
  public double getWidth() { return width; }
  public void setHeight(double h) {
	  if (Double.isInfinite(h) || Double.isNaN(h)) {
		  System.err.println("Warning, infinite vaule for height!");
	  } else height = h;
  }
  public void setWidth(double w) {
	  if (Double.isInfinite(w) || Double.isNaN(w)) {
		  System.err.println("Warning, infinite vaule for width!");
	  } else width = w;
  }
  public double getX() { return x; } 
  public double getY() { return y; } 
  public void setX(double v) {
	  if (Double.isInfinite(v) || Double.isNaN(v)) {
		  System.err.println("Warning, infinite vaule for x!");
	  } else x = v;
  }
  public void setY(double v) {
	  if (Double.isInfinite(v) || Double.isNaN(v)) {
		  System.err.println("Warning, infinite vaule for y!");
	  } else y = v;
  }
  
  /**
   * Faster contains withouth checking for ALL or EMPTY status.
   * 
   * @param ox
   * @param oy
   * @return
   */
  private boolean contains( double ox, double oy ){
	  if (( ox < x ) || ( oy < y ) || ( ox > x + width ) || ( oy > y + height )) return false;
	  else return true;
  }

  public boolean contains( DRectangle rect ){
    if( status == ALL || rect.isEmpty() ) return true;
    if( status == EMPTY || rect.isAll() ) return false;
    if( !contains(rect.x, rect.y ) ) return false;
    if( !contains(rect.x + rect.width, rect.y + rect.height ) ) return false;
    return true;
  }

  public boolean contains( double ox, double oy, double width, double heigth){
	    if( status == ALL) return true;
	    if( status == EMPTY) return false;
	    if( !contains(ox, oy ) ) return false;
	    if( !contains(ox + width, oy + height ) ) return false;
	    return true;
   }
  
  public DRectangle getIntersection( DRectangle r ){
    if( status == ALL ) return (DRectangle)r.clone();
    if( status == EMPTY ) return DRectangle.getEmpty();
    if( r.status == ALL ) return (DRectangle)clone();
    if( r.status == EMPTY ) return DRectangle.getEmpty();
    DRectangle s = (DRectangle)this.clone();
    if( s.x < r.x ){
      s.x = r.x;
      s.width -= r.x - s.x;
    }
    if( s.y < r.y ){
      s.y = r.y;
      s.height -= r.y - s.y;
    }
    if( s.x + s.width > r.x + r.width )
      s.width = r.x + r.width - s.x;
    if( s.y + s.height > r.y + r.height )
      s.height = r.y + r.height - s.y;
    if( s.width < 0 || s.height < 0 ) return DRectangle.getEmpty();
    else return s;
  }

  /**
   * method resizes the rectangle to insert p
   *
   * @param the dPoint p to insert
   * @return true when the size of the rectangle changed
   */
  public boolean insert( DPoint p ){
    if( p.x == Double.NaN || p.y == Double.NaN || Double.isInfinite(p.x) || Double.isInfinite(p.y)) {
    	return false;
    }
    if( isAll() ) return false;
    if( contains( p ) ) return false;
    if( isEmpty() ){
      x = p.x; y = p.y; width = height = 0;
      status = PART;
      return true;
    }
    if( p.x < x ) {
      width += x - p.x;
      x = p.x;
    }
    else if( p.x > x + width ) width = p.x - x;
    if( p.y < y ) {
      height += y - p.y;
      y = p.y;
    }
    else if( p.y > y + height ) height = p.y - y;
    return true;
  }

  /**
   * method inserts the given rectangle to this instance of it
   * and returns true when the size changed
   *
   * @param rect the rectangle to inserts
   * @return true if the size changed
   */
  public boolean insert( DRectangle rect ){
    if( isAll() || rect.isEmpty() ) return false;
    if( rect.isAll() ){ status = ALL; return true; }
    if( isEmpty() ){
      x = rect.x; y = rect.y; width = rect.width; height = rect.height;
      status = PART;
      return true;
    }
    boolean changed = false;
    changed = insert( new DPoint( rect.x, rect.y ) );
    changed = insert( new DPoint( rect.x + rect.width, rect.y + rect.height ) )? true : changed;
    return changed;
  }

  public Object clone(){
    DRectangle copy = new DRectangle( x, y, width, height );
    copy.status = status;
    if( color != null ) copy.color = new Color( color.getRGB() );
    return copy;
  }

  public String toString(){
    String text = "DRectangle[ ";
    switch( status ){
      case ALL   : text += "all"; break;
      case EMPTY : text += "empty"; break;
      case PART  : text += x+", "+y+", "+width+", "+height;
    }
    text += " ]";
    return text;
  }

  public boolean equals( DRectangle r ){
    if( r.status != status ) return false;
    if( r.x != x ) return false;
    if( r.y != y ) return false;
    if( r.width != width ) return false;
    if( r.height != height ) return false;
    return true;
  }

  public void setFillColor( Color fill_color ){
    if( fillColor == null || !fillColor.equals( fill_color ) ){
      fillColor = fill_color;
      repaint();
    }
  }

  public Color getFillColor(){
    return fillColor;
  }

  public static DRectangle getAll(){ return new DRectangle( ALL ); }
  public boolean isAll(){ return status == ALL; }
  public static DRectangle getEmpty(){ return new DRectangle( EMPTY ); }
  public boolean isEmpty(){ return status == EMPTY; }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
