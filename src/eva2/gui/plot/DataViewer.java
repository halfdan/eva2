package eva2.gui.plot;

import java.util.ArrayList;

/**
 * It represents one plot window in the client GUI.
 */
public class DataViewer implements DataViewerInterface {

    static private int graphCounter = -1;
    static private ViewContainer viewContainer;
    private String name;
    private Plot plot;

    /**
     *
     */
    public static DataViewerInterface getInstance(String graphWindowName) {
        if (viewContainer == null) {
            viewContainer = new ViewContainer();
        }
        DataViewerInterface ret = null;
        try {
            if (!viewContainer.containsName(graphWindowName)) {
                ret = new DataViewer(graphWindowName, true);
                viewContainer.add(ret);
            } else {
                ret = viewContainer.getPlot(graphWindowName);
            }
        } catch (Exception ee) {
            System.out.println("GraphWindow ERROR : " + ee.getMessage());
            ee.printStackTrace();
        }
        return ret;
    }

    /**
     *
     */
    private DataViewer(String PlotName, boolean initflag) {
        name = PlotName;
        if (initflag) {
            this.init();
        }
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
    @Override
    public Graph getNewGraph(String InfoString) {
        graphCounter++;
        return new Graph(InfoString, plot, graphCounter);
    }

    /**
     *
     */
    @Override
    public void init() {
        plot = new Plot(name, "x", "y", true);
    }
}

/**
 *
 */
class ViewContainer extends ArrayList {

    private DataViewer m_actualPlot;

    /**
     *
     */
    public ViewContainer() {
    }

    /**
     *
     */
    public boolean containsName(String name) {
        DataViewer temp = null;
        for (int i = 0; i < size(); i++) {
            temp = (DataViewer) (get(i));
            if (name.equals(temp.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    public DataViewer getPlot(String name) {
        if (m_actualPlot != null) {
            if (m_actualPlot.getName().equals(name)) {
                return m_actualPlot;
            }
        }
        DataViewer temp = null;
        for (int i = 0; i < size(); i++) {
            temp = (DataViewer) (get(i));
            if (name.equals(temp.getName())) {
                m_actualPlot = temp;
                return m_actualPlot;
            }
        }
        return null;
    }
}
