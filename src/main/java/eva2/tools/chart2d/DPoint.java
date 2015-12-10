package eva2.tools.chart2d;

import java.awt.*;

public class DPoint extends DComponent {
    public double x, y;
    public String label;
    protected DPointIcon icon = null;

    public DPoint() {
    }

    public void initpoint(double x, double y) {
        this.x = x;
        this.y = y;
        rectangle = new DRectangle(x, y, 0, 0);
    }

    public DPoint(double x, double y) {
        this.x = x;
        this.y = y;
        rectangle = new DRectangle(x, y, 0, 0);
    }

    @Override
    public void paint(DMeasures m) {
        Graphics g = m.getGraphics();
        if (color != null) {
            g.setColor(color);
        }
        Point dp = m.getPoint(this.x, this.y);
        if (label != null) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label,
                    dp.x - fm.stringWidth(label) / 2,
                    dp.y + fm.getAscent()
            );
        }
        if (icon == null) {
            g.drawRect(dp.x, dp.y, 1, 1);
        } else {
            g.translate(dp.x, dp.y);
            icon.paint(g);
            g.translate(-dp.x, -dp.y);
        }
    }

    /**
     * method sets an icon for a better displaying of the point
     *
     * @param icon the DPointIcon
     */
    public void setIcon(DPointIcon icon) {
        this.icon = icon;
        if (icon == null) {
            setDBorder(new DBorder(1, 1, 1, 1));
        } else {
            setDBorder(icon.getDBorder());
        }
    }

    /**
     * method returns the current icon of the point
     *
     * @return the DPointIcon
     */
    public DPointIcon getIcon() {
        return icon;
    }

    @Override
    public Object clone() {
        DPoint copy = new DPoint(x, y);
        copy.color = color;
        return copy;
    }

    @Override
    public String toString() {
        String text = "DPoint[";
        if (label != null) {
            text += label + ", ";
        }
        text += "x: " + x + ", y: " + y + ", color: " + color + "]";
        return text;
    }
}