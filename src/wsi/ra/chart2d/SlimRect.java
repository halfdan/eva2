package wsi.ra.chart2d;

/**
 * Encapsulates a slim rectangle structure with x, y, width and height and
 * nothing else. Makes some painting calculations quite a bit faster.
 *  
 * @author mkron
 *
 */
public class SlimRect {
	double x, y, width, height;

	public SlimRect(double xpos, double ypos, double wd, double ht) {
		x=xpos;
		y=ypos;
		width = wd;
		height = ht;
	}

	public SlimRect(SlimRect o) {
		x=o.x;
		y=o.y;
		width = o.width;
		height = o.height;
	}
	/**
	 * Check whether a given point lies within the rectangle.
	 * 
	 * @param ox
	 * @param oy
	 * @return true if the given point lies within the rectangle, else false
	 */
	public boolean contains( double ox, double oy ){
		if (( ox < x ) || ( oy < y ) || ( ox > x + width ) || ( oy > y + height )) return false;
		else return true;
	}

	/**
	 * Check whether a given rectangle lies within this rectangle.
	 * 
	 * @param xpos
	 * @param ypos
	 * @param wd
	 * @param ht
	 * @return true if the given rectangle lies within the rectangle, else false
	 */
	public boolean contains(double xpos, double ypos, double wd, double ht) {
		return (contains(xpos,ypos) && contains(xpos+wd, ypos+ht));
	}
	
	public String toString() {
		return "SlimRect["+x+","+y+"/"+width+","+height+"]";
	}

	/**
	 * Intersect two rectangles. If the intersection is empty, null is returned.
	 * 
	 * @param r
	 * @return A rectangle representing the intersection or null
	 */
	public SlimRect getIntersection( SlimRect r ){
		return getIntersection(r.x, r.y, r.width, r.height);
	}
	
	/**
	 * Intersect two rectangles. If the intersection is empty, null is returned.
	 * 
	 * @param r
	 * @return A rectangle representing the intersection or null
	 */
	public SlimRect getIntersection( DRectangle r ){
		return getIntersection(r.x, r.y, r.width, r.height);
	}
	
	/**
	 * Intersect two rectangles. If the intersection is empty, null is returned.
	 * 
	 * @param rx
	 * @param ry
	 * @param rwidth
	 * @param rheight
	 * @return A rectangle representing the intersection or null
	 */
	public SlimRect getIntersection(double rx, double ry, double rwidth, double rheight){
		SlimRect s = new SlimRect(this);
		if( s.x < rx ){
			s.x = rx;
			s.width -= rx - s.x;
		}
		if( s.y < ry ){
			s.y = ry;
			s.height -= ry - s.y;
		}
		if( s.x + s.width > rx + rwidth )
			s.width = rx + rwidth - s.x;
		if( s.y + s.height > ry + rheight )
			s.height = ry + rheight - s.y;
		if( s.width < 0 || s.height < 0 ) return null;
		else return s;
	}
	
	/**
	 * Check for empty intersection.
	 * @param r
	 * @return true if the two rectangles do not intersect, else false
	 */
	public boolean hasEmptyIntersection(DRectangle r){
		return (getIntersection(r.x, r.y, r.width, r.height)==null);
	}
	
	/**
	 * Check for empty intersection.
	 * @param r
	 * @return true if the two rectangles do not intersect, else false
	 */
	public boolean hasEmptyIntersection(SlimRect r){
		return (getIntersection(r)==null);
	}
}
