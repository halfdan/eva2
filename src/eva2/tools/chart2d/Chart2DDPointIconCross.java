package eva2.tools.chart2d;


import java.awt.*;


/**
 *
 */
public class Chart2DDPointIconCross implements DPointIcon {

    private Color color;

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint(Graphics g) {
        Color prev = g.getColor();
        g.setColor(this.color);
        g.drawLine(-1, 1, 1, -1);
        g.drawLine(-1, -1, 1, 1);
        g.setColor(prev);
    }

    /**
     * the border which is necessary to be paint around the DPoint that the whole
     * icon is visible
     *
     * @return the border
     */
    @Override
    public DBorder getDBorder() {
        return new DBorder(4, 4, 4, 4);
    }

    public void setColor(Color c) {
        this.color = c;
    }
}
