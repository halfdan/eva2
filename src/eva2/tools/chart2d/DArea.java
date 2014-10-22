/**
 *  Filename: $RCSfile: DArea.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.1.1 $
 *            $Date: 2003/07/03 14:59:41 $
 *            $Author: ulmerh $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package eva2.tools.chart2d;

import eva2.tools.print.PagePrinter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * DArea is the crossing of the <code>JComponent</code>s and the
 * <code>DComponent</code>s. It's the <code>DParent</code> which can be added to
 * <code>JComponent</code>s
 */
public class DArea extends JComponent implements DParent, Printable {
    /**
     * the default minimal rectangle which is shown
     */
    public static final DRectangle DEFAULT_MIN_RECT = new DRectangle(-1, -1, 2, 2);

    /**
     *
     */
    private static final long serialVersionUID = 1461387400381365146L;

    private boolean autoFocus = false, autoGrid = false, gridToFront = false;

    /**
     * the container in which all DElements of the area are contained except the
     * grid
     */
    private DContainer container;

    private DBorder dborder = new DBorder();

    /**
     * the grid of the area
     */
    private DGrid grid;

    /**
     * maximal number of grid lines
     */
    private int maxGrid = 10;

    /**
     * the measures of the area it calculates the coordinates
     */
    protected DMeasures measures;

    /**
     * min_rectangle is set, when all elements are removed the intersection of
     * visibleRect and max_rectangle is the currently visible rectangle
     */
    protected DRectangle minRect = DEFAULT_MIN_RECT,
            visibleRect = DEFAULT_MIN_RECT;

    protected Double minX, minY, maxX, maxY;

    /**
     * initializes the DArea with the initial capacity of 10 components
     */
    public DArea() {
        this(10);
    }

    /**
     * initializes the DArea with the specialized initial capacity of components
     * (@see java.util.Vector)
     *
     * @param initialCapacity the initial capacity
     */
    public DArea(int initialCapacity) {
        container = new DContainer();
        container.setDParent(this);
        grid = new DGrid(visibleRect, 1, 1);
        grid.setVisible(false);
        grid.setDParent(this);
        measures = new DMeasures(this);
    }

    /**
     * method 'adds' a certain border around the contained rectangle that means
     * that it takes the old border and takes the maxima of the old and the new
     * values
     *
     * @param clip the java.awt.Insets object of the new clip
     */
    @Override
    public void addDBorder(DBorder b) {
        dborder.insert(b);
    }

    /**
     * adds a new component to the area
     *
     * @param e the new DElement
     */
    @Override
    public void addDElement(DElement e) {
        container.addDElement(e);
    }

    /**
     * method returns true, if the given element is contained in the container
     *
     * @param e the element
     * @return if it is contained
     */
    @Override
    public boolean contains(DElement e) {
        return container.contains(e);
    }

    /**
     * method returns the current border around the rectangle
     *
     * @return the border
     */
    public DBorder getDBorder() {
        return dborder;
    }

    /**
     * returnes all DElements in the container
     *
     * @return the elements of the container
     */
    @Override
    public DElement[] getDElements() {
        return container.getDElements();
    }

    /**
     * returns the measures of the DArea The DMeasures object calculates the
     * different coodinates and contains a Graphics object to paint the
     * DElements of the area
     *
     * @return the measures of the DArea
     */
    public DMeasures getDMeasures() {
        return measures;
    }

    /**
     * returns the currently visible rectangle in DArea coordinates
     *
     * @return DRectangle the size and position of the visible area
     */
    public DRectangle getDRectangle() {
        DRectangle rect = (DRectangle) visibleRect.clone();
        if (minX != null) {
            rect.setX(Math.max(rect.getX(), getMinX()));
        }
        if (minY != null) {
            rect.setY(Math.max(rect.getY(), getMinY()));
        }
        if (maxX != null) {
            rect.setWidth(Math.min(rect.getWidth(), getMaxX() - getMinX()));
        }
        if (maxY != null) {
            rect.setHeight(Math.min(rect.getHeight(), getMaxY() - getMinY()));
        }
        return rect;
    }

    /**
     * method returns the maximal rectangle of the area
     *
     * @return the maximal rectangle
     * @deprecated see getMaxX, getMaxY, getMinX, getMinY
     */
    public DRectangle getMaxRectangle() {
        return new DRectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * method returns the maxmal x-value which can be displayed in the DArea
     *
     * @return the maxmal x-value
     */
    public double getMaxX() {
        if (maxX != null) {
            return maxX;
        }
        return 0;
    }

    /**
     * method returns the maximal y-value which can be displayed in the DArea
     *
     * @return the maximal y-value
     */
    public double getMaxY() {
        if (maxY != null) {
            return maxY;
        }
        return 0;
    }

    /**
     * method returns the minimal rectangle which is set as the visible when all
     * elements are removed and the area is on auto focus
     *
     * @return the minmal rectangle
     */
    public DRectangle getMinRectangle() {
        return (DRectangle) minRect.clone();
    }

    /**
     * method returns the minmal x-value which can be displayed in the DArea
     *
     * @return the minmal x-value
     */
    public double getMinX() {
        if (minX != null) {
            return minX;
        }
        return 0;
    }

    /**
     * method returns the minmal y-value which can be displayed in the DArea
     *
     * @return the minmal y-value
     */
    public double getMinY() {
        if (minY != null) {
            return minY;
        }
        return 0;
    }

    /**
     * returns the currently visible rectangle in DArea coordinates
     *
     * @return DRectangle the size and position of the visible area
     */
    public SlimRect getSlimRectangle() {
        SlimRect srect = new SlimRect(visibleRect.getX(), visibleRect.getY(),
                visibleRect.getWidth(), visibleRect.getHeight());
        if (minX != null) {
            srect.x = Math.max(srect.x, getMinX());
        }
        if (minY != null) {
            srect.y = Math.max(srect.y, getMinY());
        }
        if (maxX != null) {
            srect.width = Math.min(srect.width, getMaxX() - getMinX());
        }
        if (maxY != null) {
            srect.height = Math.min(srect.height, getMaxY() - getMinY());
        }
        return srect;
    }

    public DFunction getYScale() {
        return measures.yScale;
    }

    /**
     * returns if the auto grid is switched on
     *
     * @return true if the grid is on, else false
     */
    public boolean hasAutoGrid() {
        return autoGrid;
    }

    /**
     * returns if the grid is visible <code>true</code> if the grid is visible
     * or <code>false</code> if not
     *
     * @return true or false
     */
    public boolean isGridVisible() {
        return grid.isVisible();
    }

    /**
     * returns whether the DArea's auto focus is on or not
     *
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isOnAutoFocus() {
        return autoFocus;
    }

    /**
     * Method to check whether or not this {@link DArea} adds a grid to its
     * plot.
     *
     * @return true if this {@link DArea} shows a grid and false otherwise.
     */
    public boolean isShowGrid() {
        return grid.isVisible();
    }

    /**
     * paints the DArea by a Graphics object
     *
     * @param g the java.awt.Graphics object
     */
    @Override
    public void paint(Graphics g) {
        if (autoFocus) {
            container.restore();
            visibleRect = (DRectangle) container.getRectangle().clone();
            // grid.updateDistByRect(visibleRect);
        }
        if (visibleRect.isEmpty()) {
            visibleRect = (DRectangle) minRect.clone();
            // grid.updateDistByRect(visibleRect);
        }
        super.paint(g);

        measures.setGraphics(g);
        if (grid.isVisible() && !gridToFront) {
            paintGrid(measures);
        }
        container.paint(measures);
        if (grid.isVisible() && gridToFront) {
            paintGrid(measures);
        }
    }

    /**
     * method paints the grid how the method paints the grid depends on whether
     * the area is wrapped in a <code>ScaledBorder</code> or not and on the
     * autoGrid option
     */
    private void paintGrid(DMeasures m) {
        grid.rectangle = getDRectangle();
        if (autoGrid) {
            Border b = getBorder();
            if (b instanceof ScaledBorder) {
                ScaledBorder sb = (ScaledBorder) b;
                paintGrid(sb, m);
                return;
            } else {
                grid.setDistances(ScaledBorder.aBitBigger(grid.rectangle
                        .getWidth()
                        / maxGrid), ScaledBorder.aBitBigger(grid.rectangle
                        .getHeight()
                        / maxGrid));
            }
        }
        grid.paint(m);
    }

    /**
     * paints the grid when autoGrid is selected and the area is surrounded by
     * an instance of ScaledBorder
     *
     * @param sb the ScaledBorder around the area
     * @param m  the measures of the area
     */
    private void paintGrid(ScaledBorder sb, DMeasures m) {
        Dimension d = getSize();
        FontMetrics fm = m.getGraphics().getFontMetrics();
        grid.setDistances(sb.getSrcdX(fm, d), sb.getSrcdY(fm, d));

        if (m.xScale == null && m.yScale == null) {
            grid.paint(m);
        } else {
            Graphics g = m.g;
            g.setColor(grid.getColor());

            SlimRect rect = getSlimRectangle();
            SlimRect src_rect = m.getSourceOf(rect);

            int x = (int) (src_rect.x / grid.getHorDist()), y = (int) (src_rect.y / grid
                    .getVerDist());
            if (x * grid.getHorDist() < src_rect.x) {
                x++;
            }
            if (y * grid.getVerDist() < src_rect.y) {
                y++;
            }

            // DPoint min = new DPoint( rect.x, rect.y ),
            // max = new DPoint( min.x + rect.width, min.y + rect.height );
            double minx = rect.x, miny = rect.y, maxx = minx + rect.width, maxy = miny
                    + rect.height;

            double pos;

            for (; (pos = x * grid.getHorDist()) < src_rect.x + src_rect.width; x++) {
                if (m.xScale != null) {
                    pos = m.xScale.getImageOf(pos);
                }
                Point p1 = m.getPoint(pos, miny), p2 = m.getPoint(pos, maxy);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            for (; (pos = y * grid.getVerDist()) < src_rect.y + src_rect.height; y++) {
                if (m.yScale != null) {
                    pos = m.yScale.getImageOf(pos);
                }
                Point p1 = m.getPoint(minx, pos), p2 = m.getPoint(maxx, pos);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    /**
     * prints the area and it's content
     *
     * @param g  the Graphics object
     * @param pf the @see java.awt.print.PageFormat
     * @param pi the page index
     * @return int @see java.awt.print.Printable
     * @see java.awt.print.Printable and
     * @see java.awt.print.PrintJob
     */
    @Override
    public int print(Graphics g, PageFormat pf, int pi) {
        if (pi > 0) {
            return Printable.NO_SUCH_PAGE;
        }

        Border sb = getBorder();
        if (!(sb instanceof ScaledBorder)) {
            sb = null;
        } else {
            ((ScaledBorder) sb).showOuterBorder = false;
        }
        PagePrinter printer = new PagePrinter(this, g, pf);
        int ret = printer.print();
        if (sb != null) {
            ((ScaledBorder) sb).showOuterBorder = true;
        }
        return ret;
    }

    /**
     * method resets the maximal x-value
     */
    public void releaseMaxX() {
        maxX = null;
    }

    /**
     * method resets the maximal y-value
     */
    public void releaseMaxY() {
        maxY = null;
    }

    /**
     * method resets the minimal x-value
     */
    public void releaseMinX() {
        minX = null;
    }

    /**
     * method resets the minimal y-value
     */
    public void releaseMinY() {
        minY = null;
    }

    /**
     * removes all elements from the area
     */
    public void removeAllDElements() {
        visibleRect = (DRectangle) minRect.clone();
        container.removeAllDElements();
    }

    /**
     * removes a certain element from the area
     *
     * @param e the element to remove
     */
    @Override
    public boolean removeDElement(DElement e) {
        return container.removeDElement(e);
    }

    /**
     * repaints a part of the visible area
     *
     * @param r the rectangle to repaint
     */
    @Override
    public void repaint(DRectangle r) {
        if (r == null) {
            throw new IllegalArgumentException(
                    "Cannot repaint a null DRectangle");
        }
        if (r.isAll() || autoFocus) {
            repaint();
        } else {
            Point p1 = measures.getPoint(r.getX(), r.getY()), p2 = measures
                    .getPoint(r.getX() + r.getWidth(), r.getY() + r.getHeight());
            if (p1 == null || p2 == null) {
                repaint();
            } else {
                DBorder b = getDBorder();
                repaint(p1.x - b.left, p2.y - b.top, p2.x - p1.x + 1 + b.left
                        + b.right, p1.y - p2.y + 1 + b.top + b.bottom);
            }
        }
    }

    @Override
    public void restoreBorder() {
        dborder = container.getDBorder();
    }

    /**
     * switches the auto focus of this DArea on or off
     *
     * @param b on or off
     */
    public void setAutoFocus(boolean b) {
        boolean old = autoFocus;
        autoFocus = b;
        if (old != b) {
            repaint();
        }
    }

    /**
     * sets the auto grid on or off if it's on, the grid's distances (@see
     * #setGrid(double, double)) are automatically calculated
     *
     * @param b auto grid on or not
     */
    public void setAutoGrid(boolean b) {
        if (b) {
            grid.rectangle = getDRectangle();
            grid.setVisible(true);
        }
        if (b == autoGrid) {
            return;
        }
        autoGrid = b;
        repaint();
    }

    /**
     * sets the grid's horizontal and vertical distance that means that the
     * grid's lines will have these distances in area coordinates
     *
     * @param hor_dist the horizontal distance
     * @param ver_dist the vertical distance
     */
    public void setGrid(double hor_dist, double ver_dist) {
        grid = new DGrid(visibleRect, hor_dist, ver_dist);
        grid.setDParent(this);
        autoGrid = false;
        repaint();
    }

    /**
     * sets the color of the grid
     *
     * @param color The new grid color
     */
    public void setGridColor(Color color) {
        grid.setColor(color);
    }

    /**
     * sets the grid to the front that means that the grid is painted as last
     * element default value is <code>false</code>
     *
     * @param aFlag grid t front or not
     */
    public void setGridToFront(boolean aFlag) {
        boolean old = gridToFront;
        gridToFront = aFlag;
        if (old != aFlag && grid.isVisible()) {
            repaint();
        }
    }

    /**
     * sets the grid visible or not
     *
     * @param aFlag visible or not
     */
    public void setGridVisible(boolean aFlag) {

        grid.rectangle = getDRectangle();
        grid.setVisible(aFlag);
    }

    /**
     * sets the maximal number of grid lines default value is 10
     *
     * @param no maximal number of grid lines
     */
    public void setMaxGrid(int no) {
        if (no < 1) {
            return;
        }
        int old = maxGrid;
        maxGrid = no;
        if (old != no) {
            repaint();
        }
    }

    /**
     * method sets the maximal x-value which can be displayed by the DArea might
     * be helpful, if scale functions are used which are not defined overall
     *
     * @param max the maximal x-value
     */
    public void setMaxX(double max) {
        if (max < minRect.getX() + minRect.getWidth()) {
            throw new IllegalArgumentException(
                    "Maximal x-value axes intersects minmal rectangle.");
        }
        maxX = max;
    }

    /**
     * method sets the maximal y-values which can be displayed by the DArea
     * might be helpful, if scale functions are used which are not defined
     * overall
     *
     * @param may the maximal y-value
     */
    public void setMaxY(double may) {
        if (may < minRect.getY() + minRect.getHeight()) {
            throw new IllegalArgumentException(
                    "Maximal y-value axes intersects minmal rectangle.");
        }
        maxY = may;
    }

    /**
     * sets the minimal rectangle
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setMinRectangle(double x, double y, double width, double height) {
        setMinRectangle(new DRectangle(x, y, width, height));
    }

    /**
     * sets the minimal rectangle
     *
     * @param rect the visible <code>DRectangle</code> in DArea coordinates
     */
    public void setMinRectangle(DRectangle rect) {
        if (rect.isEmpty()) {
            minRect = DEFAULT_MIN_RECT;
        } else {
            minRect = (DRectangle) rect.clone();
        }
    }

    /**
     * method sets the minimal x-value which can be displayed by the DArea might
     * be helpful, if scale functions are used which are not defined overall
     *
     * @param mix the minimal x-value
     */
    public void setMinX(double mix) {
        if (mix > minRect.getX()) {
            throw new IllegalArgumentException(
                    "Mimimal y-value axes intersects minmal rectangle.");
        }
        minX = mix;
    }

    /**
     * method sets the minimal y-value which can be displayed by the DArea might
     * be helpful, if scale functions are used which are not defined overall
     *
     * @param miy the minimal y-value
     */
    public void setMinY(double miy) {
        if (miy > minRect.getY()) {
            throw new IllegalArgumentException(
                    "Mimimal y-value axes intersects minmal rectangle.");
        }
        minY = miy;
    }

    /**
     * sets the visible rectangle to this size
     *
     * @param x      the x coordinate of the left border
     * @param y      the y coordinate of the bottom border
     * @param width  the width of the area
     * @param height the height of the area
     */
    public void setVisibleRectangle(double x, double y, double width,
                                    double height) {
        // System.out.println("DArea.setVisibleRectangle(...)");
        setVisibleRectangle(new DRectangle(x, y, width, height));
    }

    /**
     * sets the visible rectangle
     *
     * @param rect the visible <code>DRectangle</code> in DArea coordinates
     */
    public void setVisibleRectangle(DRectangle rect) {
        if (rect.isEmpty()) {
            throw new IllegalArgumentException(
                    "You should never try to set an empty rectangle\n"
                            + "as the visible rectangle of an DArea");
        }

        if (!rect.equals(visibleRect) && rect.getWidth() > 0
                && rect.getHeight() > 0) {
            autoFocus = false;
            visibleRect = (DRectangle) rect.clone();
            repaint();
        }
    }

    /**
     * sets a new scale function to the x-axis That means that not the standard
     * linear scale is shown but the image of the linear scale under the given
     * function
     *
     * @param x_s the scale function for the x-axis
     */
    public void setXScale(DFunction x_s) {
        if (x_s == null && measures.xScale == null) {
            return;
        }
        measures.xScale = x_s;
        repaint();
    }

    /**
     * sets a new scale function to the y-axis That means that not the standard
     * linear scale is shown but the image of the linear scale under the given
     * function
     *
     * @param y_s the scale function for the y-axis
     */
    public void setYScale(DFunction y_s) {
        if (y_s == null && measures.yScale == null) {
            return;
        }
        measures.yScale = y_s;
        repaint();
    }
}