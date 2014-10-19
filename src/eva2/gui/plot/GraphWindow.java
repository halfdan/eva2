package eva2.gui.plot;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It represents one plot window in the client GUI.
 */
public class GraphWindow {
    private static final Logger LOGGER = Logger.getLogger(GraphWindow.class.getName());
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
        LOGGER.log(Level.FINEST, "Graph.getNewGraph No:{0} - {1} created.", new Object[]{graphCounter, infoString});
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
        GraphWindow temp;
        for (int i = 0; i < size(); i++) {
            temp = get(i);
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
        GraphWindow temp;
        for (int i = 0; i < size(); i++) {
            temp = get(i);
            if (name.equals(temp.getName())) {
                actualPlot = temp;
                return actualPlot;
            }
        }
        return null;
    }
}
