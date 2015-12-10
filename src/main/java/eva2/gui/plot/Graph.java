package eva2.gui.plot;

import java.io.Serializable;

/**
 *
 */
public class Graph implements Serializable {

    private PlotInterface plotter;
    private int graphLabel;
    private String infoString;

    /**
     *
     */
    public Graph(String info, PlotInterface plotter, int x) {
        infoString = info;
        this.plotter = plotter;
        graphLabel = x;
        if (plotter == null) {
            System.out.println("In constructor plotter == null");
        }
        plotter.setInfoString(graphLabel, info, (float) 1.0);
    }

    /**
     *
     */
    public String getInfo() {
        return infoString;
    }

    /**
     *
     */
    public void setInfoString(String info, float stroke) {
        plotter.setInfoString(graphLabel, info, stroke);
    }

    /**
     *
     */
    public int getGraphLabel() {
        return graphLabel;
    }

    /**
     *
     */
    public void setConnectedPoint(double x, double y) {
        plotter.setConnectedPoint(x, y, graphLabel);
    }

    /**
     *
     */
    public void clear() {
        plotter.clearGraph(graphLabel);
    }

    /**
     *
     */
    public void setUnconnectedPoint(double x, double y) {
        plotter.setUnconnectedPoint(x, y, graphLabel);
    }

    public int getPointCount() {
        return plotter.getPointCount(graphLabel);
    }

    /**
     * Add a graph to this graph object. Uses "force" for mismatching point counts, but returns
     * false if force was used and points possibly have been lost.
     *
     * @return true if the graph could be added directly or false if the graph was added by force
     *         losing some data points
     * @see PlotInterface#addGraph
     */
    public boolean addGraph(Graph x) {
        boolean useForce = false;
        //System.out.println("adding graph " + x.getGraphLabel() + " to " + getGraphLabel());
        if ((getPointCount() != 0) && (getPointCount() != x.getPointCount())) {
            //System.err.println("mismatching graphs, point counts were " + getPointCount() + " " + x.getPointCount());
            useForce = true;
        }
        plotter.jump();
        plotter.addGraph(graphLabel, x.getGraphLabel(), useForce);
        return !useForce;
    }

    /**
     * Causes the PlotInterface to interrupt the connected painting at the current position.
     */
    public void jump() {
        plotter.jump();
    }

    public void setColorByIndex(int j) {
        ((Plot) plotter).setColorByIndex(graphLabel, j);
    }
}