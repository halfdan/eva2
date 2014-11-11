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
    // when in use for a DArea:
    Graphics g;
    // when in use for a ScaledBorder:
    ScaledBorder sb;
    // for both:
    DFunction xScale, yScale;
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
            if (xScale != null) {
                x = xScale.getSourceOf(x);
            }
            if (yScale != null) {
                y = yScale.getSourceOf(y);
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
            if (xScale != null) {
                dx = xScale.getImageOf(dx);
            }
            if (yScale != null) {
                dy = yScale.getImageOf(dy);
            }
        } catch (IllegalArgumentException nde) {
            return null;
        }
        return new DPoint(dx, dy);
    }


    /**
     * Returns the visible rectangle in D-coordinates of the shown component as slim structure.
     *
     * @return the visible rectangle
     */
    public SlimRect getSlimRectangle() {
        if (sb != null) {
            return getImageOf(sb.srcRect.x, sb.srcRect.y, sb.srcRect.width, sb.srcRect.height);
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
        xScale = sb.xScale;
        yScale = sb.yScale;
    }

    private Dimension getInner() {
        Dimension d = comp.getSize();
        Insets insets = getInsets();
        d.width -= insets.left + insets.right;
        d.height -= insets.top + insets.bottom;
        return d;
    }

    /**
     * method returns the image rectangle of the given rectangle
     * they differ if there are scale functions selected which are not the identity
     * if the given rectangle does not belong to the definition area of the scale
     * functions, the method returns <code>null</code>
     *
     * @return the source of it
     */
    SlimRect getImageOf(double xpos, double ypos, double width, double height) {
        if (xScale == null && yScale == null) {
            return new SlimRect(xpos, ypos, width, height);
        }
        double x1 = xpos, y1 = ypos, x2 = xpos + width, y2 = ypos + height;

        try {
            if (xScale != null) {
                x1 = xScale.getImageOf(x1);
                x2 = xScale.getImageOf(x2);
            }
            if (yScale != null) {
                y1 = yScale.getImageOf(y1);
                y2 = yScale.getImageOf(y2);
            }
        } catch (IllegalArgumentException nde) {
            return null;
        }
        return new SlimRect(x1, y1, x2 - x1, y2 - y1);
    }

    SlimRect getImageOf(SlimRect srect) {
        return getImageOf(srect.x, srect.y, srect.width, srect.height);
    }

    /**
     * method returns the source rectangle of the given rectangle
     * they differ if there are scale functions selected which are not the identity
     * if the given rectangle does not belong to the image area of the scale
     * functions, the method returns <code>null</code>
     * <p>
     * Tuning: rect must not be empty
     *
     * @param rect the image rectangle
     * @return the source of it
     */
    SlimRect getSourceOf(double x, double y, double width, double height) {
        if (!getSlimRectangle().contains(x, y, width, height)) {
            return null;
        }

        if (xScale == null && yScale == null) {
            return new SlimRect(x, y, width, height);
        }

        double x1 = x, y1 = y, x2 = x + width, y2 = y + height;
        try {
            if (xScale != null) {
                x1 = xScale.getSourceOf(x1);
                x2 = xScale.getSourceOf(x2);
            }
            if (yScale != null) {
                y1 = yScale.getSourceOf(y1);
                y2 = yScale.getSourceOf(y2);
            }
        } catch (IllegalArgumentException nde) {
            return null;
        } catch (NullPointerException npe) {
            return null;
        }
        return new SlimRect(x1, y1, x2 - x1, y2 - y1);
    }

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