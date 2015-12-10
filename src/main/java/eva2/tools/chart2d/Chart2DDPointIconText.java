package eva2.tools.chart2d;

import java.awt.*;

/**
 *
 */
public class Chart2DDPointIconText implements DPointIcon {

    private DPointIcon icon = new Chart2DDPointIconCross();
    private String text = " ";
    private Color color;

    public Chart2DDPointIconText(String s) {
        text = s;
    }

    /**
     * This method allows you to set an icon
     *
     * @param icon The new icon
     */
    public void setIcon(DPointIcon icon) {
        this.icon = icon;
    }

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint(Graphics g) {
        this.icon.paint(g);
        g.setColor(color);
        g.drawString(this.text, 4, 4);
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

    /**
     * Set the color for the text.
     *
     * @param col
     */
    public void setColor(Color col) {
        color = col;
    }
}