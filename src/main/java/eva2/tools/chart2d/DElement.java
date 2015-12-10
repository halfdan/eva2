package eva2.tools.chart2d;

import java.awt.*;

/**
 * some useful methods for objects which should be paintable in a scaled area
 */
public interface DElement {
    Color DEFAULT_COLOR = Color.black;

    DRectangle getRectangle();

    void setDParent(DParent parent);

    DParent getDParent();

    void paint(DMeasures m);

    void repaint();

    void setVisible(boolean aFlag);

    boolean isVisible();

    void toggleVisible();

    void setColor(Color color);

    Color getColor();

    void setDBorder(DBorder b);

    DBorder getDBorder();
}