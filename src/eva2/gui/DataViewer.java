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
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import eva2.tools.jproxy.MainAdapterClient;
import eva2.tools.jproxy.RMIProxyRemote;
import java.net.InetAddress;
import java.util.ArrayList;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 * It represents one plot window in the client GUI.
 */
public class DataViewer implements DataViewerInterface {
  public static boolean TRACE = false;

  static private int m_GraphCounter = -1;
  static private ViewContainer m_ViewContainer;
  private MainAdapterClient m_MainAdapterClient;
  private static String m_MyHostName;
  private PlotInterface m_Plotter;
  private String m_Name;
  private Plot m_Plot;
  /**
   *
   */
  public static DataViewerInterface getInstance (MainAdapterClient client,String GraphWindowName) {
    if (m_ViewContainer == null)
      m_ViewContainer = new ViewContainer();
    DataViewerInterface ret =null;
    try {
      if (!m_ViewContainer.containsName(GraphWindowName)) {
        ////////////////////////////////////////////////
        try {
          m_MyHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
          System.out.println("InetAddress.getLocalHost().getHostAddress() --> ERROR" + e.getMessage());
          e.printStackTrace();
        }
        if (client != null && client.getHostName().equals(m_MyHostName)== true) {
          if (TRACE) System.out.println("no RMI");
          ret = new DataViewer(GraphWindowName,true);
        }
        else {
          ret = (DataViewerInterface) RMIProxyRemote.newInstance(new DataViewer(GraphWindowName,false), client);
          ret.init();
          if (TRACE) System.out.println("with RMI");
        }


        ////////////////////////////////////
        m_ViewContainer.add(ret);
      }
      else {
        ret = m_ViewContainer.getPlot(GraphWindowName);
      }
    } catch (Exception ee) {
      System.out.println("GraphWindow ERROR : "+ee.getMessage());
      ee.printStackTrace();
    }
    return ret;
  }
  /**
   *
   */
  private DataViewer(String PlotName,boolean initflag){
    if (TRACE) System.out.println("Constructor DataViewer");
    m_Name = PlotName;
    if(initflag)
      this.init();
  }
  /**
   *
   */
  public String getName () {
    return m_Name;
  }
  /**
   *
   */
    @Override
  public Graph getNewGraph(String InfoString) {
    m_GraphCounter++;
    if (TRACE) System.out.println("Graph.getNewGraph No:"+m_GraphCounter);
    return new Graph (InfoString,m_Plot,m_GraphCounter);
  }
  /**
   *
   */
    @Override
  public void init() {
    m_Plot = new Plot(m_Name,"x","y", true);
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
  public ViewContainer() {}
  /**
   *
   */
  public boolean containsName (String name) {
    DataViewer temp = null;
    for (int i=0;i<size();i++) {
      temp = (DataViewer)(get(i));
      if (name.equals(temp.getName())) {
	return true;
      }
    }
    return false;
  }
  /**
   *
   */
  public DataViewer getPlot (String name) {
    if (m_actualPlot!=null)
      if (m_actualPlot.getName().equals(name))
        return m_actualPlot;
    DataViewer temp = null;
    for (int i=0;i<size();i++) {
      temp = (DataViewer)(get(i));
      if (name.equals(temp.getName())) {
	m_actualPlot = temp;
	return m_actualPlot;
      }
    }
    return null;
  }
}
