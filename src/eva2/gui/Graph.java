package eva2.gui;

import java.io.Serializable;

/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 322 $
 *            $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
public class Graph implements Serializable {
  private PlotInterface m_Plotter;
  private int m_GraphLabel;
  private String m_Info;
  /**
   *
   */
  public Graph(String Info, PlotInterface Plotter,int x) {
      m_Info = Info;
    m_Plotter = Plotter;
    m_GraphLabel = x;
    if (m_Plotter==null)
      System.out.println("In constructor m_Plotter == null");
    m_Plotter.setInfoString(m_GraphLabel,Info, (float) 1.0 );
  }
  /**
   *
   * @param Info
   * @param stroke
   */
  public String getInfo() {return m_Info;}
   /**
   *
   */
  public void setInfoString(String Info,float stroke) {
    m_Plotter.setInfoString(m_GraphLabel, Info,stroke);
  }
  /**
   *
   */
  public int getGraphLabel () {
    return m_GraphLabel;
  }
  /**
   *
   */
  public void setConnectedPoint(double x,double y) {
      m_Plotter.setConnectedPoint(x,y,m_GraphLabel);
  }
    
  /**
   *
   */
  public void clear() {
    m_Plotter.clearGraph(m_GraphLabel);
  }
  /**
   *
   */
  public void setUnconnectedPoint(double x,double y) {
      m_Plotter.setUnconnectedPoint(x,y,m_GraphLabel);
  }

  public int getPointCount() {
	  return m_Plotter.getPointCount(m_GraphLabel);
  }
  
  /**
   * Add a graph to this graph object. Uses "force" for mismatching point counts, but returns false
   * if force was used and points possibly have been lost.
   *
   * @return true if the graph could be added directly or false if the graph was added by force losing some data points
   * @see PlotInterface.addGraph
   */
  public boolean addGraph(Graph x) {
	  boolean useForce = false;
	  //System.out.println("adding graph " + x.getGraphLabel() + " to " + getGraphLabel());
	  if ((getPointCount() != 0) && (getPointCount() != x.getPointCount())) {
		  //System.err.println("mismatching graphs, point counts were " + getPointCount() + " " + x.getPointCount());
		  useForce = true;
	  }
	  m_Plotter.jump();
	  m_Plotter.addGraph(m_GraphLabel, x.getGraphLabel(), useForce);
	  return !useForce;
  }
  
   /**
   * Causes the PlotInterface to interrupt the connected painting at the
   * current position.
   */
  public void jump() {
    m_Plotter.jump();
  }
  
//  public boolean isValid() { // this was evil in RMI, use GraphWindow instead
//	  //return true;
//	  return (m_Plotter != null) && (m_Plotter.getFunctionArea() != null);
//  }
}