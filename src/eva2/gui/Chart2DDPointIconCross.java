package eva2.gui;


import java.awt.*;

import eva2.tools.chart2d.DBorder;
import eva2.tools.chart2d.DPointIcon;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.04.2004
 * Time: 10:00:50
 * To change this template use File | Settings | File Templates.
 */
public class Chart2DDPointIconCross implements DPointIcon {

    private Color       m_Color;

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    public void paint( Graphics g ){
        Color prev = g.getColor();
        g.setColor(this.m_Color);
        g.drawLine(-1, 1, 1,-1);
        g.drawLine(-1,-1, 1, 1);
        g.setColor(prev);
    }

    /**
     * the border which is necessary to be paint around the DPoint that the whole
     * icon is visible
     *
     * @return the border
     */
    public DBorder getDBorder() {
        return new DBorder(4, 4, 4, 4);
    }

    public void setColor(Color c) {
        this.m_Color = c;
    }
}
