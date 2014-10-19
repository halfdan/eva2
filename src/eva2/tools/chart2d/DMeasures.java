package eva2.tools.chart2d;

import java.awt.*;
import java.io.Serializable;

/**
 * A class mainly for coordinate conversion. Replaced usage of DRectangle by SlimRect
 * which makes it more efficient.
 *
 * @author Fabian Hennecke, ulmerh, mkron
 */
public class DMeasures implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 243092480517044848L;
    private boolean under_construction = false;
    // when in use for a DArea:
    Graphics g;
    // when in use for a ScaledBorder:
    ScaledBorder sb;
    // for both:
    DFunction x_scale, y_scale;
    Component comp;
    Insets insets;

    /**
     * package private constructor for the DMeasures object
     * the object can be obtained by calling the method getDMeasures of an DArea
     * object
     *
     * @param area the DArea object
     */
    DMeasures(DArea area) {
        comp = area;
    }

    DMeasures(ScaledBorder sb) {
        this.sb = sb;
    }

    /**
     * method returns the pixel-point which belongs to the given D-coordinates
     * it says where to paint a certain DPoint
     * returns <code>null</code> if the double coordinates do not belong to the
     * image of the scale functions
     *
     * @param x the double x D-coordinate
     * @param y the double y D-coordinate
     * @return the coresponding pixel Point
     */
    public Point getPoint(double x, double y) {
        SlimRect rect = getSourceOf(getSlimRectangle());
        if (rect == null) {
            return null;
        }
        try {
            if (x_scale != null) {
                x = x_scale.getSourceOf(x);
            }
            if (y_scale != null) {
                y = y_scale.getSourceOf(y);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        Point dp = new Point();
        Dimension dim = getInner();
        Insets insets = getInsets();
        dp.x = (int) (dim.width * (x - rect.x) / rect.width) + insets.left;
        dp.y = (int) (dim.height * (1 - (y - rect.y) / rect.height)) + insets.top;
        return dp;
    }

    /**
     * method returns the point in D-coordinates which corresponds to the
     * given pixel-coordinates
     * returns <code>null</code> if the given coordinates can not be calculated to
     * the double coordinates, when they represent a point outside of the definition
     * area of the scale functions
     *
     * @param x x-pixel coordinate
     * @param y y-pixel coordinate
     * @return the coresponding DPoint
     */
    public DPoint getDPoint(int x, int y) {
        SlimRect rect = getSourceOf(getSlimRectangle());
        Dimension dim = getInner();
        Insets insets = getInsets();
        x -= insets.left;
        y -= insets.top;
        double dx, dy;
        dx = rect.x + rect.width * x / (double) dim.width;
        dy = rect.y + rect.height * (1 - y / (double) dim.height);
        try {
            if (x_scale != null) {
                dx = x_scale.getImageOf(dx);
            }
            if (y_scale != null) {
                dy = y_scale.getImageOf(dy);
            }
        } catch (IllegalArgumentException nde) {
            return null;
        }
        return new DPoint(dx, dy);
    }

//  /**
//   * returns the visible rectangle in D-coordinates of the shown component
//   *
//   * @return the visible rectangle
//   */
//  public DRectangle getDRectangle(){
//    if( under_construction ) System.out.println("DMeasures.getDRectangle");
//    if( sb != null ) return getImageOf( sb.src_rect );
//    return ((DArea)comp).getDRectangle();
//  }

    /**
     * Returns the visible rectangle in D-coordinates of the shown component as slim structure.
     *
     * @return the visible rectangle
     */
    public SlimRect getSlimRectangle() {
//	    if( under_construction ) System.out.println("DMeasures.getDRectangle");
        if (sb != null) {
            return getImageOf(sb.src_rect.x, sb.src_rect.y, sb.src_rect.width, sb.src_rect.height);
        }
        return ((DArea) comp).getSlimRectangle();
    }

    /**
     * returns the current Graphics object, which might be used by components to
     * paint themselves
     * the method sets the clipping area of the Graphics object to the currently
     * visible rectangle
     *
     * @return the Graphics object ( or null if no object was set )
     */
    public Graphics getGraphics() {
        if (under_construction) {
            System.out.println("DMeasures.getGraphics");
        }
        if (g != null) {
            Dimension d = comp.getSize();
            Insets insets = getInsets();
            g.setClip(insets.left + 1, // dann sieht man noch was von der linken Achse
                    insets.top,
                    d.width - insets.left - insets.right,
                    d.height - insets.top - insets.bottom);
        }
        return g;
    }

    /**
     * used by DArea to set a new Graphics object
     */
    void setGraphics(Graphics g) {
        if (under_construction) {
            System.out.println("DMeasures.setGraphics");
        }
        this.g = g;
    }

    /**
     * used by ScaledBorder to update the DMeasures object
     *
     * @param c the parent component the border
     */
    void update(Component c, Insets insets) {
        this.comp = c;
        this.insets = insets;
        x_scale = sb.x_scale;
        y_scale = sb.y_scale;
    }

    private Dimension getInner() {
        Dimension d = comp.getSize();
        Insets insets = getInsets();
        d.width -= insets.left + insets.right;
        d.height -= insets.top + insets.bottom;
        return d;
    }

//  /**
//   * method returns the image rectangle of the given rectangle
//   * they differ if there are scale functions selected which are not the identity
//   * if the given rectangle does not belong to the defintion area of the scale
//   * functions, the method returns <code>null</code>
//   *
//   * @param rect the source rectangle
//   * @return the source of it
//   */
//  DRectangle getImageOf( DRectangle rect ){
//    if( under_construction ) System.out.println("DMeasures.getImageOf: "+rect);
//    if( x_scale == null && y_scale == null ) return rect;
//    if( rect.isEmpty() ) return (DRectangle)rect.clone();
//    DPoint p1 = new DPoint( rect.x, rect.y ),
//           p2 = new DPoint( rect.x + rect.width, rect.y + rect.height );
//    try{
//      if( x_scale != null ){
//        p1.x = x_scale.getImageOf( p1.x );
//        p2.x = x_scale.getImageOf( p2.x );
//      }
//      if( y_scale != null ){
//        p1.y = y_scale.getImageOf( p1.y );
//        p2.y = y_scale.getImageOf( p2.y );
//      }
//    }
//    catch( IllegalArgumentException nde ){ return null; }
//    return new DRectangle( p1.x, p1.y, p2.x - p1.x, p2.y - p1.y );
//  }

    /**
     * method returns the image rectangle of the given rectangle
     * they differ if there are scale functions selected which are not the identity
     * if the given rectangle does not belong to the definition area of the scale
     * functions, the method returns <code>null</code>
     *
     * @param rect the source rectangle
     * @return the source of it
     */
    SlimRect getImageOf(double xpos, double ypos, double width, double height) {
//    if( under_construction ) System.out.println("DMeasures.getImageOf: "+rect);

        if (x_scale == null && y_scale == null) {
            return new SlimRect(xpos, ypos, width, height);
        }
        double x1 = xpos, y1 = ypos, x2 = xpos + width, y2 = ypos + height;

        try {
            if (x_scale != null) {
                x1 = x_scale.getImageOf(x1);
                x2 = x_scale.getImageOf(x2);
            }
            if (y_scale != null) {
                y1 = y_scale.getImageOf(y1);
                y2 = y_scale.getImageOf(y2);
            }
        } catch (IllegalArgumentException nde) {
            return null;
        }
        return new SlimRect(x1, y1, x2 - x1, y2 - y1);
    }

    SlimRect getImageOf(SlimRect srect) {
        return getImageOf(srect.x, srect.y, srect.width, srect.height);
    }

//  /**
//   * method returns the source rectangle of the given rectangle
//   * they differ if there are scale functions selected which are not the identity
//   * if the given rectangle does not belong to the image area of the scale
//   * functions, the method returns <code>null</code>
//   *
//   * @param rect the image rectangle
//   * @return the source of it
//   */
//  DRectangle getSourceOf( DRectangle rect ){
//    if( under_construction ) System.out.println("DMeasures.getSourceOf: "+rect);
//    if( !getDRectangle().contains( rect ) ) {
////    	System.out.println("case not contains");
//    	return null;
//    	//throw new IllegalArgumentException("The rectangle lies not in the currently painted rectangle");
//    }
//      
//    if( x_scale == null && y_scale == null ) {
////    	System.out.println("Case scale null");
//    	return rect;
//    }
//    if( rect.isEmpty() ) {
////    	System.out.println("Case isEmpty");
//    	return (DRectangle)rect.clone();
//    }
//    DPoint p1 = new DPoint( rect.x, rect.y ),
//           p2 = new DPoint( rect.x + rect.width, rect.y + rect.height );
//    try{
//      if( x_scale != null ){
//        p1.x = x_scale.getSourceOf( p1.x );
//        p2.x = x_scale.getSourceOf( p2.x );
//      }
//      if( y_scale != null ){
//        p1.y = y_scale.getSourceOf( p1.y );
//        p2.y = y_scale.getSourceOf( p2.y );
//      }
//    }
//    catch( IllegalArgumentException nde ){ return null; }
//    return new DRectangle( p1.x, p1.y, p2.x - p1.x, p2.y - p1.y );
//  }

    /**
     * method returns the source rectangle of the given rectangle
     * they differ if there are scale functions selected which are not the identity
     * if the given rectangle does not belong to the image area of the scale
     * functions, the method returns <code>null</code>
     * <p/>
     * Tuning: rect must not be empty
     *
     * @param rect the image rectangle
     * @return the source of it
     */
    SlimRect getSourceOf(double x, double y, double width, double height) {
//	    if( under_construction ) System.out.println("DMeasures.getSourceOf: "+rect);
        if (!getSlimRectangle().contains(x, y, width, height)) {
//	    	System.out.println("case not contains");
            return null;
            //throw new IllegalArgumentException("The rectangle lies not in the currently painted rectangle");
        }

        if (x_scale == null && y_scale == null) {
//	    	System.out.println("Case scale null");
            return new SlimRect(x, y, width, height);
        }

        double x1 = x, y1 = y, x2 = x + width, y2 = y + height;
        try {
            if (x_scale != null) {
                x1 = x_scale.getSourceOf(x1);
                x2 = x_scale.getSourceOf(x2);
            }
            if (y_scale != null) {
                y1 = y_scale.getSourceOf(y1);
                y2 = y_scale.getSourceOf(y2);
            }
        } catch (IllegalArgumentException nde) {
            return null;
        } catch (NullPointerException npe) {
            return null;
        }
        return new SlimRect(x1, y1, x2 - x1, y2 - y1);
    }
//  SlimRect getSourceOf( double xpos, double ypos, double width, double height){
//    if( !getSlimRectangle().contains( xpos, ypos, width, height ) ) {
//    	System.err.println("The rectangle lies not in the currently painted rectangle");
//    	return null;
//    	//throw new IllegalArgumentException("The rectangle lies not in the currently painted rectangle");
//    }
//     // MK: this now strangely does the same as getImageOf
//    return getImageOf(xpos, ypos, width, height);
//  }

    SlimRect getSourceOf(SlimRect srect) {
        return getSourceOf(srect.x, srect.y, srect.width, srect.height);
    }

    private Insets getInsets() {
        if (sb != null) {
            return insets;
        }
        return ((DArea) comp).getInsets();
    }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
