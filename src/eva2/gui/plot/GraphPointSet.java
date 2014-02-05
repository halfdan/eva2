package eva2.gui.plot;


import eva2.tools.chart2d.*;
import eva2.tools.math.Mathematics;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;


/**
 *
 */
public class GraphPointSet {
    // Color sequence of the plot graphs
    public static Color[] colorSequence =
            new Color[]{
                    Color.black, Color.red, Color.blue, Color.pink, Color.green,
                    Color.gray, Color.magenta, Color.cyan, Color.orange,
                    new Color(148, 0, 211), // dark violet,
                    new Color(72, 209, 204), // turquoise
                    new Color(128, 128, 0), // olive
                    new Color(34, 139, 34), // forest green
                    new Color(100, 149, 237) // cornflower
            };

    /**
     *
     */
    class PointSet implements Serializable {
        /**
         * Generated serial version identifier
         */
        private static final long serialVersionUID = -5863595580492128866L;
        private Color color;
        private double[] x;
        private double[] y;

        /**
         * @param pointset
         */
        public PointSet(DPointSet pointset) {
            color = pointset.getColor();
            x = new double[pointset.getSize()];
            y = new double[pointset.getSize()];
            for (int i = 0; i < pointset.getSize(); i++) {
                DPoint point = pointset.getDPoint(i);
                x[i] = point.x;
                y[i] = point.y;
            }
        }

        /**
         * @return
         */
        public DPointSet getDPointSet() {
            DPointSet ret = new DPointSet(100);
            ret.setColor(color);
            for (int i = 0; i < x.length; i++) {
                ret.addDPoint(x[i], y[i]);
            }
            return ret;
        }

        /**
         * @return
         */
        public int getSize() {
            return x.length;
        }

        // /**
        // *
        // */
        // public DPointSet printPoints() {
        // for (int i = 0; i < connectedPointSet.getSize();i++) {
        // DPoint p = connectedPointSet.getDPoint(i);
        // double x = p.x;
        // double y = p.y;
        // //System.out.println("point "+i+ " x= "+x+"y= "+y);
        // }
        // return connectedPointSet.getDPointSet();
        // }

    }

    private int colorOffset = 0;
    private DArea area;
    private int cacheIndex = 0;
    private int cacheSize = 0;
    private double[] cachex;
    private double[] cachey;
    private Color color;
    private DPointSetMultiIcon connectedPointSet;
    private int graphLabel;
    private DPointIcon icon;
    private String infoString = "Incomplete_Run";
    private boolean isStatisticsGraph = false;
    private ArrayList<PointSet> pointSetContainer = new ArrayList<PointSet>();

    private float stroke = (float) 1.0;

    /**
     *
     */
    public GraphPointSet(/* int size */int GraphLabel, DArea Area) {
        // System.out.println("Constructor GraphPointSet "+ GraphLabel);
        cachex = new double[cacheSize];
        cachey = new double[cacheSize];
        area = Area;
        graphLabel = GraphLabel;
        connectedPointSet = new DPointSetMultiIcon(100);
        connectedPointSet.setStroke(new BasicStroke(stroke));
        connectedPointSet.setConnected(true);

        setColor(indexToColor(GraphLabel));
        initGraph(Area);
    }

    /**
     * @param size
     * @param GraphLabel
     */
    private GraphPointSet(int size, int GraphLabel) {
        graphLabel = GraphLabel;
        cachex = new double[cacheSize];
        cachey = new double[cacheSize];
        connectedPointSet = new DPointSetMultiIcon(100);
        connectedPointSet.setStroke(new BasicStroke(stroke));

        connectedPointSet.setConnected(true);
        color = Color.black;

        color = indexToColor(GraphLabel);

        connectedPointSet.setColor(color);

    }

    /**
     * @param x
     * @param y
     */
    public void addDPoint(double x, double y) {
        // System.out.println(" "+x+" "+y);
        if (cacheIndex == cacheSize) {
            for (int i = 0; i < cacheSize; i++) {
                connectedPointSet.addDPoint(cachex[i], cachey[i]);
            }
            connectedPointSet.addDPoint(x, y);
            cacheIndex = 0;
        } else {
            cachex[cacheIndex] = x;
            cachey[cacheIndex] = y;
            cacheIndex++;
        }
    }

    /**
     * @param p
     */
    public void addDPoint(DPoint p) {
        connectedPointSet.addDPoint(p);
    }

    /**
     * Add a graph to another one forming a statistics graph if it isnt one
     * already.
     *
     * @param set
     * @param measures
     * @param useForce forces the add even if point counts mismatch, maybe losing
     *                 some data points
     */
    public void addGraph(GraphPointSet set, DMeasures measures, boolean useForce) {
        if (set.connectedPointSet.getSize() != connectedPointSet.getSize()
                && connectedPointSet.getSize() != 0 && !useForce) {
            System.err
                    .println("WARNING addGraph not possible, lost last graph");
            System.err.println(" connectedPointSet.getSize() "
                    + connectedPointSet.getSize());
            return;
        }
        if (set.getPointSet().getSize() == 0) {
            System.err.println("Refusing to add empty graph...");
            return;
        }
        isStatisticsGraph = true;
        removeAllPoints();
        connectedPointSet.setColor(set.getColor());

        pointSetContainer.add(set.getPointSet());
        int[] index = new int[pointSetContainer.size()];
        int[] GraphSize = new int[pointSetContainer.size()];
        for (int i = 0; i < pointSetContainer.size(); i++) {
            GraphSize[i] = ((PointSet) pointSetContainer.get(i)).getSize();
            if (GraphSize[i] <= 0) {
                System.err.println("Warning: invalid graph size of "
                        + GraphSize[i] + " at " + i
                        + "!  (GraphPointSet.addGraph)");
            }
        }
        if (Mathematics.sum(GraphSize) == 0) {
            System.err
                    .println("Error: not adding empty graphs... (GraphPointSet.addGraph)");
            return;
        }
        boolean allSetsHaveMorePoints = true;
        double nextXValue;
        double[] y = new double[pointSetContainer.size()];
        while (allSetsHaveMorePoints) { // Loop over all point sets, add them up
            // and calc. mean
            // this is a bit more complicated because it is allowed that the
            // point sets are asynchronouos
            // in the sense that the x values do not have to match - y values
            // for any x value found are averaged
            // over all points. However curves may look strange if this happens,
            // since they consist of
            // heterogenous points.
            nextXValue = pointSetContainer.get(0).x[index[0]];
            // System.out.println("pointSetContainer.size()"+pointSetContainer.size());
            for (int i = 1; i < pointSetContainer.size(); i++) { // search for
                // smalles x
                // value at
                // next
                // index
                // System.out.println("i="+i);
                if (nextXValue > pointSetContainer.get(i).x[index[i]]) {
                    nextXValue = pointSetContainer.get(i).x[index[i]];
                }
            }
            // Stelle nextXValue wird gezeichnet. jetzt alle y werte dazu finden
            int numberofpoints = 0;
            for (int i = 0; i < pointSetContainer.size(); i++) { // collect
                // all
                // points at
                // next
                // x-value
                if (nextXValue == pointSetContainer.get(i).x[index[i]]) {
                    y[i] = pointSetContainer.get(i).y[index[i]];
                    index[i]++;
                    numberofpoints++;
                } else {
                    y[i] = 0;
                }
            }
            double ymean = Mathematics.sum(y) / numberofpoints;
            // compute median double median = getMedian(y);
            addDPoint(nextXValue, ymean);// System.out.println("ymean "+ymean+"  y.length "+
            // y.length);
            // addDPoint(minx,median);//
            // System.out.println("ymean "+ymean+"  y.length "+ y.length);
            for (int i = 0; i < pointSetContainer.size(); i++) { // Stop if
                // one of
                // the point
                // sets has
                // no more
                // points
                if (GraphSize[i] <= index[i]) {
                    allSetsHaveMorePoints = false;
                    break;
                }
            }
        }
    }

    /**
     * @return
     */
    public Color getColor() {
        return connectedPointSet.getColor();
    }

    /**
     * @return
     */
    public DPointSet getConnectedPointSet() {
        return connectedPointSet.getDPointSet();
    }

    /**
     * @return
     */
    public int getGraphLabel() {
        return graphLabel;
    }


    /**
     *
     */
    public String getInfoString() {
        return infoString;
    }

    /**
     * @param p
     * @return
     */
    public DPoint getNearestDPoint(DPoint p) {
        return connectedPointSet.getNearestDPoint(p);
    }

    /**
     * @return
     */
    public int getPointCount() {
        return connectedPointSet.getSize();
    }

    /**
     * @return
     */
    public PointSet getPointSet() {
        return new PointSet(this.connectedPointSet.getDPointSet());
    }

    /**
     * @return
     */
    public DPointSetMultiIcon getReference2ConnectedPointSet() {
        return connectedPointSet;
    }

    /**
     * Increase the color sequentially.
     */
    public void incColor() {
        colorOffset++;
        setColor(indexToColor(graphLabel + colorOffset));
    }

    /**
     * @param index
     * @return
     */
    private Color indexToColor(int index) {
        int k = index % colorSequence.length;
        return colorSequence[k];
    }

    /**
     * @param Area
     */
    public void initGraph(DArea Area) {
        area = Area;
        area.addDElement(connectedPointSet);
        ((FunctionArea) area).addGraphPointSet(this);
    }

    /**
     * @return
     */
    public boolean isStatisticsGraph() {
        return isStatisticsGraph;
    }

    /**
     * Causes the PointSet to interrupt the connected painting at the current
     * position.
     */
    public void jump() {
        connectedPointSet.jump();
    }

    /**
     * @return
     */
    public DPointSet printPoints() {
        for (int i = 0; i < connectedPointSet.getSize(); i++) {
            DPoint p = connectedPointSet.getDPoint(i);
            double x = p.x;
            double y = p.y;
            System.out.println("point " + i + " x = " + x + "y = " + y);
        }
        return connectedPointSet.getDPointSet();
    }

    /**
     *
     */
    public void removeAllPoints() {
        connectedPointSet.removeAllPoints();
    }

    /**
     * @param x
     */
    public void removePoint(DPoint x) {
        System.out.println("removePoint " + x.x + " " + x.y);
        DPoint[] buf = new DPoint[connectedPointSet.getSize()];
        for (int i = 0; i < connectedPointSet.getSize(); i++) {
            buf[i] = connectedPointSet.getDPoint(i);
        }
        connectedPointSet.removeAllPoints();
        for (int i = 0; i < buf.length; i++) {
            if (buf[i].x == x.x && buf[i].y == x.y) {
                System.out.println("point found");
            } else {
                connectedPointSet.addDPoint(buf[i]);
            }

        }
    }

    /**
     * @param c
     */
    public void setColor(Color c) {
        color = c;
        connectedPointSet.setColor(color);
    }

    public void setColorByIndex(int i) {
        setColor(indexToColor(i));
    }

    /**
     * @param p
     */
    public void setConnectedMode(boolean p) {
        connectedPointSet.setConnected(p);
    }

    /**
     * @param p
     */
    public void setIcon(DPointIcon p) {
        this.icon = p;
        this.connectedPointSet.setIcon(p);
    }

    /**
     * @param x
     * @param stroke
     */
    public void setInfoString(String x, float stroke) {
        infoString = x;
        this.stroke = stroke;
        // setStroke(new BasicStroke( stroke ));
    }

    /**
     * Sets the info string without changing the stroke.
     *
     * @param x
     */
    public void setInfoString(String x) {
        infoString = x;
    }

    /**
     * Retrieve the median point of this point set.
     *
     * @return the median point of this point set or null if it is empty
     */
    public DPoint getMedPoint() {
        if (connectedPointSet == null) {
            return null;
        }
        int medX = connectedPointSet.getSize() / 2;
        return connectedPointSet.getDPoint(medX);
    }
}
