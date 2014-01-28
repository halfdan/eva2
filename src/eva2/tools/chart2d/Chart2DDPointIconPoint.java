package eva2.tools.chart2d;


import java.awt.*;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 17.08.2005
 * Time: 16:52:34
 * To change this template use File | Settings | File Templates.
 */
public class Chart2DDPointIconPoint implements DPointIcon {

    private Color borderColor = Color.BLACK;
    private Color fillColor = Color.BLACK;
    private int size = 2;

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint(Graphics g) {
        Color prev = g.getColor();
        if (fillColor != null) {
            g.setColor(fillColor);
            g.fillOval(-this.size, -this.size, 2 * this.size, 2 * this.size);
        }
        if (this.borderColor != null) {
            g.setColor(borderColor);
        }
        g.drawOval(-this.size, -this.size, (2 * this.size) - 1, (2 * this.size) - 1);
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

    public void setBorderColor(Color c) {
        this.borderColor = c;
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
    }

    public void setSize(int d) {
        this.size = d;
    }

}
