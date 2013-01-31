package eva2.gui;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of Tuebingen, Computer
 * Architecture @author Holger Ulmer, Felix Streichert, Hannes Planatscher @version: $Revision: 320
 * $ $Date: 2007-12-06 16:05:11 +0100 (Thu, 06 Dec 2007) $ $Author: mkron $
 */

import java.util.ArrayList;

/**
 * It represents one plot window in the client GUI.
 */
public class GraphWindow {

    public static boolean TRACE = false;
    static private int graphCounter = -1;
    static private PlotContainer plotContainer;
    private PlotInterface plotter;
    private String name;

    /**
     *
     */
    public static GraphWindow getInstance(String graphWindowName,
            String strx, String stry) {
        if (plotContainer == null) {
            plotContainer = new PlotContainer();
        }
        GraphWindow ret = null;
        try {
            if (plotContainer.containsName(graphWindowName)) {
                ret = plotContainer.getPlot(graphWindowName);
            }
            if ((ret == null) || !(ret.isValid())) {
                if (ret != null) {
                    plotContainer.remove(ret); // remove if not valid any more
                }
                ret = new GraphWindow(graphWindowName, strx, stry);
                plotContainer.add(ret);
            }
        } catch (Exception ee) {
            System.out.println("GraphWindow ERROR : " + ee.getMessage());
            ee.printStackTrace();
        }
        return ret;
    }

    public boolean isValid() {
        return (plotter != null) && (plotter.isValid());
    }

    public PlotInterface getPlotter() {
        return plotter;
    }

    /**
     *
     */
    private GraphWindow(String plotName, String strx, String stry) {
        if (TRACE) {
            System.out.println("Constructor GraphWindow");
        }
        name = plotName;
        plotter = new Plot(plotName, strx, stry, true);
    }

    /**
     *
     */
    public String getName() {
        return name;
    }

    /**
     *
     */
    public Graph getNewGraph(String infoString) {
        graphCounter++;
        if (TRACE) {
            System.out.println("Graph.getNewGraph No:" + graphCounter + " - " + infoString);
        }
        return new Graph(infoString, plotter, graphCounter);
    }
}

/**
 *
 */
class PlotContainer extends ArrayList<GraphWindow> {

    /**
     *
     */
    private static final long serialVersionUID = 4194675772084989958L;
    private GraphWindow actualPlot;

    /**
     *
     */
    public PlotContainer() {
    }

    /**
     *
     */
    public boolean containsName(String name) {
        GraphWindow temp = null;
        for (int i = 0; i < size(); i++) {
            temp = (GraphWindow) (get(i));
            if (name.equals(temp.getName())) {
                return true;
            }
        }
        return false;
    }

    public void remove(GraphWindow gw) {
        super.remove(gw);
        if (actualPlot == gw) {
            actualPlot = null;
        }
    }

    /**
     *
     */
    public GraphWindow getPlot(String name) {
        if ((actualPlot != null) && actualPlot.isValid()) {
            if (actualPlot.getName().equals(name)) {
                return actualPlot;
            }
        }
        GraphWindow temp = null;
        for (int i = 0; i < size(); i++) {
            temp = (GraphWindow) (get(i));
            if (name.equals(temp.getName())) {
                actualPlot = temp;
                return actualPlot;
            }
        }
        return null;
    }
}
