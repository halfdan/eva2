package javaeva.gui;
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
public interface PlotInterface {
  public void setConnectedPoint (double x,double y,int GraphLabel);
  
  /**
   * Add two graphs to form an average graph
   * 
   * @param g1	graph object one
   * @param g2  graph object two
   * @param forceAdd if the graph mismatch in point counts, try to add them anyway in a useful manner.
   */
  public void addGraph (int g1,int g2, boolean forceAdd);
  public void setUnconnectedPoint (double x, double y,int GraphLabel);
  public void clearAll ();
  public void clearGraph (int GraphNumber);
  public void setInfoString (int GraphLabel, String Info, float stroke);
  public void jump ();
  public String getName();
  public int getPointCount(int graphLabel);
//  public FunctionArea getFunctionArea(); // this is bad for RMI
  public boolean isValid();
  public void init();
}

