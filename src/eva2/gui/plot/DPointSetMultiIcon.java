package eva2.gui.plot;


import eva2.tools.chart2d.*;

import java.awt.*;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.04.2004
 * Time: 16:17:35
 * To change this template use File | Settings | File Templates.
 */
public class DPointSetMultiIcon extends DComponent {
    //~ Instance fields ////////////////////////////////////////////////////////

    /**
     * this class stores the jump positions (see this.jump)
     */
    class JumpManager {
        protected int index = -1;
        protected ArrayList<Integer> jumps = new ArrayList<>();

        public void addJump() {
            jumps.add(getSize());
        }

        public boolean hasMoreIntervals() {
            return index < jumps.size();
        }

        public int[] nextInterval() {
            int no_jumps = jumps.size();

            if (index >= no_jumps) {
                throw new ArrayIndexOutOfBoundsException(
                        "No more intervals in JumpManager");
            }

            int[] inter = new int[2];

            if (index == -1) {
                inter[0] = 0;
            } else {
                inter[0] = jumps.get(index);
            }

            index++;

            if (index < no_jumps) {
                inter[1] = jumps.get(index);
            } else {
                inter[1] = getSize();
            }

            return inter;
        }

        public void reset() {
            index = -1;
            jumps.clear();
        }

        public void restore() {
            index = -1;
        }
    }

    protected boolean connectedMI;
    protected DPointIcon iconMI = null;
    protected DPointSetMultiIcon.JumpManager jumperMI = new DPointSetMultiIcon.JumpManager();
    protected ArrayList<DPointIcon> iconsMI = new ArrayList<>();
    protected Stroke strokeMI = new BasicStroke();
    protected DIntDoubleMap xMI;

    //~ Constructors ///////////////////////////////////////////////////////////

    protected DIntDoubleMap yMI;

    public DPointSetMultiIcon() {
        this(10, 2);
    }

    public DPointSetMultiIcon(DIntDoubleMap x_values, DIntDoubleMap y_values) {
        if (x_values.getSize() != y_values.getSize()) {
            throw new IllegalArgumentException(
                    "The number of x-values has to be the same than the number of y-values");
        }

        xMI = x_values;
        yMI = y_values;
        restore();
        setDBorder(new DBorder(1, 1, 1, 1));
    }

    public DPointSetMultiIcon(int initial_capacity) {
        this(initial_capacity, 2);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public DPointSetMultiIcon(int initial_capacity, int length_multiplier) {
        this(new DArray(initial_capacity, length_multiplier),
                new DArray(initial_capacity, length_multiplier));
    }

    public void addDPoint(double x, double y) {
        addDPoint(new DPoint(x, y));
    }

    public void addDPoint(DPoint p) {
        xMI.addImage(p.x);
        yMI.addImage(p.y);
        iconsMI.add(p.getIcon());
        rectangle.insert(p);
        repaint();
    }

    /**
     * method returns the DPoint at the given index
     *
     * @param index the index of the DPoint
     * @return the DPoint at the given index
     */
    public DPoint getDPoint(int index) {
        if (index >= xMI.getSize()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        DPoint p = new DPoint(xMI.getImage(index), yMI.getImage(index));
        p.setIcon(iconMI);
        p.setColor(color);

        return p;
    }

    public DPointSet getDPointSet() {
        return new DPointSet(xMI, yMI);
    }

    /**
     * method returns the current icon of the point set
     *
     * @return the DPointIcon
     */
    public DPointIcon getIcon() {
        return iconMI;
    }

    public ArrayList<DPointIcon> getIconsMI() {
        return this.iconsMI;
    }

    /**
     * method returns the nearest <code>DPoint</code> in this <code>DPointSet</code>.
     *
     * @return the nearest <code>DPoint</code>
     */
    public DPoint getNearestDPoint(DPoint point) {
        int minIndex = getNearestDPointIndex(point);

        if (minIndex == -1) {
            return null;
        } else {
            DPoint result = new DPoint(xMI.getImage(minIndex),
                    yMI.getImage(minIndex));
            result.setIcon(this.iconsMI.get(minIndex));

            return result;
        }
    }

    /**
     * method returns the index to the nearest <code>DPoint</code> in this <code>DPointSet</code>.
     *
     * @return the index to the nearest <code>DPoint</code>. -1 if no nearest <code>DPoint</code> was found.
     */
    public int getNearestDPointIndex(DPoint point) {
        double minValue = Double.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0; i < xMI.getSize(); i++) {
            double dx = point.x - xMI.getImage(i);
            double dy = point.y - yMI.getImage(i);
            double dummy = (dx * dx) + (dy * dy);

            if (dummy < minValue) {
                minValue = dummy;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public int getSize() {
        int size = Math.min(xMI.getSize(), yMI.getSize());

        //    int size = x.getSize();
        //    if( size != y.getSize() ) throw
        //      new ArrayStoreException(
        //        "The number of x-values is not equal to the number of y-values.\n"
        //        +"The size of the DPointSet isn�t clear."
        //      );
        return size;
    }

    /**
     * method returns the current stroke of the line
     *
     * @return the stroke
     */
    public Stroke getStroke() {
        return strokeMI;
    }

    /**
     * This method causes the DPointSet to interupt the connected painting at the
     * current position.
     */
    public void jump() {
        jumperMI.addJump();
    }

    @Override
    public void paint(DMeasures m) {
        try {
            Graphics2D g = (Graphics2D) m.getGraphics();
            g.setStroke(strokeMI);

            if (color != null) {
                g.setColor(color);
            }

            int size = getSize();

            if (connectedMI && (size > 1)) {
                jumperMI.restore();

                while (jumperMI.hasMoreIntervals()) {
                    int[] interval = jumperMI.nextInterval();
                    Point p1 = null;
                    Point p2;

                    for (int i = interval[0]; i < interval[1]; i++) {
                        p2 = m.getPoint(xMI.getImage(i), yMI.getImage(i));
                        if (p1 != null) {
                            if (p2 != null) {
                                g.drawLine(p1.x, p1.y, p2.x, p2.y);
                            }
                        }

                        if ((i < this.iconsMI.size()) && (this.iconsMI.get(i) != null)) {
                            g.setStroke(new BasicStroke());
                            g.translate(p2.x, p2.y);
                            this.iconsMI.get(i).paint(g);
                            g.translate(-p2.x, -p2.y);
                            g.setStroke(strokeMI);
                        } else {
                            if (iconMI != null) {
                                g.setStroke(new BasicStroke());
                                g.translate(p2.x, p2.y);
                                iconMI.paint(g);
                                g.translate(-p2.x, -p2.y);
                                g.setStroke(strokeMI);
                            }
                        }

                        p1 = p2;
                    }
                }
            } else {
                Point p;

                //for (int i = 0; i < size; i++)
                // @todo Streiche: Mal wieder eine index out of bounds exception, dass ist einfach mist...
                for (int i = 0; i < this.iconsMI.size(); i++) {
                    try {
                        p = m.getPoint(xMI.getImage(i), yMI.getImage(i));

                        if (p == null) {
                            continue;
                        }
                        if (this.iconsMI.get(i) != null) {
                            g.setStroke(new BasicStroke());
                            g.translate(p.x, p.y);
                            this.iconsMI.get(i).paint(g);
                            g.translate(-p.x, -p.y);
                            g.setStroke(strokeMI);
                        } else {
                            if (iconMI == null) {
                                g.drawLine(p.x - 1, p.y - 1, p.x + 1, p.y + 1);
                                g.drawLine(p.x + 1, p.y - 1, p.x - 1, p.y + 1);
                            } else {
                                g.setStroke(new BasicStroke());
                                g.translate(p.x, p.y);
                                iconMI.paint(g);
                                g.translate(-p.x, -p.y);
                            }
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        System.out.println(
                                "The rectangle lies not in the currently painted rectangle.");
                    }
                }
            }

            g.setStroke(new BasicStroke());
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            // *pff*
        }
    }

    public void removeAllPoints() {
        if (xMI.getSize() == 0) {
            return;
        }

        xMI.reset();
        yMI.reset();
        jumperMI.reset();
        repaint();
        rectangle = DRectangle.getEmpty();
    }

    /**
     * method removes all jump positions
     * if the DPointSet is connected, all points will be painted connected to
     * their following point
     */
    public void removeJumps() {
        jumperMI.reset();
    }

    protected void restore() {
        if (getSize() == 0) {
            rectangle = DRectangle.getEmpty();

            return;
        }

        double min_x = xMI.getMinImageValue();
        double max_x = xMI.getMaxImageValue();
        double min_y = yMI.getMinImageValue();
        double max_y = yMI.getMaxImageValue();
        rectangle = new DRectangle(min_x, min_y, max_x - min_x, max_y - min_y);
    }

    public void setConnected(boolean aFlag) {
        boolean changed = !(aFlag == connectedMI);
        connectedMI = aFlag;

        if (changed) {
            repaint();
        }
    }

    /**
     * method puts the given DPoint at the given position in the set
     *
     * @param index the index of the point
     * @param p     the point to insert
     */
    public void setDPoint(int index, DPoint p) {
        if (index >= xMI.getSize()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        rectangle.insert(p);
        xMI.setImage(index, p.x);
        yMI.setImage(index, p.y);
        iconsMI.set(index, p.getIcon());
        restore();
        repaint();
    }

    /**
     * method sets an icon for a better displaying of the point set
     *
     * @param icon the DPointIcon
     */
    public void setIcon(DPointIcon icon) {
        this.iconMI = icon;

        if (icon == null) {
            setDBorder(new DBorder(1, 1, 1, 1));
        } else {
            setDBorder(icon.getDBorder());
        }
    }

    /**
     * method sets the stroke of the line
     * if the points were not connected, they now will be connected
     *
     * @param s the new stroke
     */
    public void setStroke(Stroke s) {
        if (s == null) {
            s = new BasicStroke();
        }

        strokeMI = s;
        repaint();
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String text = "eva2.tools.chart2d.DPointSet[size:" + getSize();

        for (int i = 0; i < xMI.getSize(); i++) {
            text += (",(" + xMI.getImage(i) + "," + yMI.getImage(i) + ")");
        }

        text += "]";

        return text;
    }
}
///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
