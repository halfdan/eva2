package javaeva.gui;
/*
 * Title:        JavaEvA
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
import java.net.InetAddress;
import java.util.ArrayList;

import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.RMIProxyRemote;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 * It represents one plot window in the client GUI.
 */
public class GraphWindow {
  public static boolean TRACE = false;

  static private int m_GraphCounter = -1;
  static private PlotContainer m_PlotContainer;
  private MainAdapterClient m_MainAdapterClient;
  private PlotInterface m_Plotter;
  private String m_Name;
  /**
   *
   */
  public static GraphWindow getInstance (MainAdapterClient client,String GraphWindowName,
    String strx,String stry) {
    if (m_PlotContainer == null)
      m_PlotContainer = new PlotContainer();
    GraphWindow ret =null;
    try {
//      if (!m_PlotContainer.containsName(GraphWindowName)) {
//        ret = new GraphWindow(client,GraphWindowName,strx,stry);
//        m_PlotContainer.add(ret);
//      }
//      else {
//        ret = m_PlotContainer.getPlot(GraphWindowName);
//      }
        if (m_PlotContainer.containsName(GraphWindowName)) {
            ret = m_PlotContainer.getPlot(GraphWindowName);
        }
        if ((ret == null) || !(ret.isValid())) {
        	if (ret != null) {
        		m_PlotContainer.remove(ret); // remove if not valid any more
        	}
            ret = new GraphWindow(client,GraphWindowName,strx,stry);
            m_PlotContainer.add(ret);
        }
    } catch (Exception ee) {
      System.out.println("GraphWindow ERROR : "+ee.getMessage());
      ee.printStackTrace();
    }
    return ret;
  }
  
  public boolean isValid() {
	  return (m_Plotter != null) && (m_Plotter.isValid());
  }

  /**
   *
   */
  private GraphWindow(MainAdapterClient client,String PlotName,String strx,String stry){
	  if (TRACE) System.out.println("Constructor GraphWindow");
	  m_MainAdapterClient = client;
	  m_Name = PlotName;
	  try {
		  if ((client==null) || client.getHostName().equals(InetAddress.getLocalHost().getHostName())) {
			  if (TRACE) System.out.println("no RMI");
			  m_Plotter = new Plot(PlotName,strx,stry);
		  }
		  else {
			  m_Plotter = (PlotInterface) RMIProxyRemote.newInstance(new Plot(PlotName,strx,stry,false), m_MainAdapterClient);
			  m_Plotter.init();
			  if (TRACE) System.out.println("with RMI");
		  }
	  } catch (Exception e) {
		  System.err.println("InetAddress.getLocalHost().getHostAddress() --> ERROR" + e.getMessage());
	  }
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
  public Graph getNewGraph(String InfoString) {
    m_GraphCounter++;
    if (TRACE) System.out.println("Graph.getNewGraph No:"+m_GraphCounter);
    return new Graph (InfoString,m_Plotter,m_GraphCounter);
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
	private GraphWindow m_actualPlot;
  /**
   *
   */
  public PlotContainer() {}
  /**
   *
   */
  public boolean containsName (String name) {
    GraphWindow temp = null;
    for (int i=0;i<size();i++) {
      temp = (GraphWindow)(get(i));
      if (name.equals(temp.getName())) {
	return true;
      }
    }
    return false;
  }
  
  public void remove(GraphWindow gw) {
	  super.remove(gw);
	  if (m_actualPlot == gw) m_actualPlot=null;
  }
  
  /**
   *
   */
  public GraphWindow getPlot (String name) {
    if ((m_actualPlot!=null) && m_actualPlot.isValid()) {
      if (m_actualPlot.getName().equals(name))
        return m_actualPlot;
    }
    GraphWindow temp = null;
    for (int i=0;i<size();i++) {
      temp = (GraphWindow)(get(i));
      if (name.equals(temp.getName())) {
	m_actualPlot = temp;
	return m_actualPlot;
      }
    }
    return null;
  }  
}
