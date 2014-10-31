package eva2.tools.chart2d;

import java.awt.*;

/**
 * A eva2.problems.simple interface which can be used to paint certain icons at DPoints
 * ( @see chart2d.DPoint.setIcon or chart2d.DPointSet.setIcon ).
 * Different points may be easier recognized in a complex graph.
 * The container does not guarantee that the whole icon is visible in the graph
 * because the icon does not concern the DRectangle of the DElement.
 */

public interface DPointIcon {

    /**
     * this method has to be overridden to paint the icon. The point itself lies
     * at coordinates (0, 0)
     */
    void paint(Graphics g);

    /**
     * the border which is necessary to be paint around the DPoint that the whole
     * icon is visible
     *
     * @return the border
     */
    DBorder getDBorder();
}

