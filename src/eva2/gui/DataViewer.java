package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 320 $
 *            $Date: 2007-12-06 16:05:11 +0100 (Thu, 06 Dec 2007) $
 *            $Author: mkron $
 */

import java.util.ArrayList;

/**
 * It represents one plot window in the client GUI.
 */
public class DataViewer implements DataViewerInterface {

    public static boolean TRACE = false;
    static private int m_GraphCounter = -1;
    static private ViewContainer viewContainer;
    private String m_Name;
    private Plot m_Plot;

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
        if (TRACE) {
            System.out.println("Constructor DataViewer");
        }
        m_Name = PlotName;
        if (initflag) {
            this.init();
        }
    }

    /**
     *
     */
    public String getName() {
        return m_Name;
    }

    /**
     *
     */
    @Override
    public Graph getNewGraph(String InfoString) {
        m_GraphCounter++;
        if (TRACE) {
            System.out.println("Graph.getNewGraph No:" + m_GraphCounter);
        }
        return new Graph(InfoString, m_Plot, m_GraphCounter);
    }

    /**
     *
     */
    @Override
    public void init() {
        m_Plot = new Plot(m_Name, "x", "y", true);
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
