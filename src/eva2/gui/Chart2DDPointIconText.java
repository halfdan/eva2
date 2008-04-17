package eva2.gui;

import wsi.ra.chart2d.DPointIcon;
import wsi.ra.chart2d.DBorder;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.04.2004
 * Time: 16:08:15
 * To change this template use File | Settings | File Templates.
 */
public class Chart2DDPointIconText  implements DPointIcon {

    private DPointIcon  m_Icon = new Chart2DDPointIconCross();
    private String      m_Text = " ";

    public Chart2DDPointIconText(String s) {
        m_Text = s;
    }

    /** This method allows you to set an icon
     * @param icon  The new icon
     */
    public void setIcon(DPointIcon icon) {
        this.m_Icon = icon;
    }

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    public void paint( Graphics g ){
        this.m_Icon.paint(g);
        g.drawString(this.m_Text, 4, 4);
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
}