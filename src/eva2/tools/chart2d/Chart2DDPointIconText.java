package eva2.tools.chart2d;


import java.awt.*;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.04.2004
 * Time: 16:08:15
 * To change this template use File | Settings | File Templates.
 */
public class Chart2DDPointIconText implements DPointIcon {

    private DPointIcon m_Icon = new Chart2DDPointIconCross();
    private String m_Text = " ";
    private Color m_Color;

    public Chart2DDPointIconText(String s) {
        m_Text = s;
    }

    /**
     * This method allows you to set an icon
     *
     * @param icon The new icon
     */
    public void setIcon(DPointIcon icon) {
        this.m_Icon = icon;
    }

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    @Override
    public void paint(Graphics g) {
        this.m_Icon.paint(g);
        g.setColor(m_Color);
        g.drawString(this.m_Text, 4, 4);
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
        m_Color = col;
    }
}