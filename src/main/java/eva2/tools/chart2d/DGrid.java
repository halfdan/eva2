package eva2.tools.chart2d;

import eva2.tools.math.Mathematics;

import java.awt.*;

/**
 * this class paints a grid with certain line distances on a DParent
 */
public class DGrid extends DComponent {
    /**
     * the distances between the lines
     */
    private double horDist, verDist;

    private Color DEFAULT_COLOR = Color.lightGray;

    /**
     * constructor with the size and position of the grid and the line distances
     *
     * @param rectangle the rectangle around the grid
     * @param horDist   the horizontal distance between the lines in D-coordinates,
     *                  not in pixel coordinates!
     * @param verDist   vertical distance between the lines in D-coordinates,
     *                  not in pixel coordinates!
     */
    public DGrid(DRectangle rectangle, double horDist, double verDist) {
        this.rectangle = rectangle;
        this.horDist = horDist;
        this.verDist = verDist;
        color = DEFAULT_COLOR;
    }

    /**
     * constructor with the size and position of the grid and the line distances
     *
     * @param rectangle the rectangle around the grid
     * @param horDist   the horizontal distance between the lines in D-coordinates,
     *                  not in pixel coordinates!
     * @param verDist   the vertical distance between the lines in D-coordinates,
     *                  not in pixel coordinates!
     * @param color     the color of the grid
     *                  ( can also be set by setColor( java.awt.Color ) )
     */
    public DGrid(DRectangle rectangle, double horDist, double verDist, Color color) {
        this.rectangle = rectangle;
        this.horDist = horDist;
        this.verDist = verDist;
        this.color = color;
    }

    public void setDistances(double hor, double ver) {
        horDist = hor;
        verDist = ver;
    }

    public double getHorDist() {
        return horDist;
    }

    public double getVerDist() {
        return verDist;
    }

    /**
     * paints the grid...
     *
     * @param m the {@code DMeasures} object to paint the grid
     */
    @Override
    public void paint(DMeasures m) {
        Graphics g = m.getGraphics();
        if (color != null) {
            g.setColor(color);
        }
        double minX, minY, pos;
        DPoint p1, p2;
        DLine l;

        minX = Mathematics.firstMultipleAbove(rectangle.getX(), horDist);
        minY = Mathematics.firstMultipleAbove(rectangle.getY(), verDist);

        p1 = new DPoint(0, rectangle.getY());
        p2 = new DPoint(0, rectangle.getY() + rectangle.getHeight());
        for (pos = minX; pos <= rectangle.getX() + rectangle.getWidth(); pos += horDist) {
            p1.x = p2.x = pos;
            l = new DLine(p1, p2, color);
            l.paint(m);
        }

        p1.x = rectangle.getX();
        p2.x = p1.x + rectangle.getWidth();
        pos = minY;
        while (pos <= rectangle.getY() + rectangle.getHeight()) {
            p1.y = p2.y = pos;
            l = new DLine(p1, p2, color);
            l.paint(m);
            if (pos + verDist <= pos) {
                System.err.println("Overflow error in DGrid!");
                pos *= 1.01;
            } else {
                pos += verDist;
            }
        }
    }

    @Override
    public String toString() {
        return "chart2d.DGrid[ hor: " + horDist + ", ver: " + verDist + " ]";
    }
}