package eva2.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import wsi.ra.chart2d.*;
import java.awt.*;

import eva2.server.go.problems.Interface2DBorderProblem;

import wsi.ra.diagram.ColorBarCalculator;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class TopoPlot extends Plot {

  public int gridx = 50;
  public int gridy = 50;
  int colorScale = ColorBarCalculator.BLUE_TO_RED;

  /**
   *
   */
  public TopoPlot(String PlotName,String xname,String yname) {
    super(PlotName, xname, yname);
    //if (TRACE) System.out.println("Constructor TopoPlot "+PlotName);
  }
  public TopoPlot(String PlotName,String xname,String yname,double[] a, double[] b) {
    super(PlotName, xname, yname, a, b);
    //if (TRACE) System.out.println("Constructor TopoPlot "+PlotName);
  }
   /**
   * Defines parameters used for drawing the topology.
   * @param gridX the x-resolution of the topology, higher value means higher resolution
   * @param gridY the y-resolution of the topology, higher value means higher resolution
   * @param color_scale the topologies color coding. Values (0-3) are valid. @See ColorBarCalculator.
   */
  public void setParams(int gridX, int gridY, int color_scale) {
    if (gridX>m_Frame.getWidth())
       gridX = m_Frame.getWidth();
    if (gridY>m_Frame.getHeight())
       gridY = m_Frame.getHeight();
    gridx = gridX;
    gridy = gridY;
    colorScale = color_scale;
  }


   /**
   * Defines the topology (by setting a specific problem) and draws the topology
   */
  public void setTopology(Interface2DBorderProblem problem) {
    double sizeX = java.lang.Math.abs( problem.get2DBorder()[0][1]-problem.get2DBorder()[0][0]);
    double sizeY = java.lang.Math.abs( problem.get2DBorder()[1][1]-problem.get2DBorder()[1][0]);
    double rx, ry;
    double rw = sizeX/gridx;
    double rh = sizeY/gridy;
    double[] pos = new double[2];
    //double fitRange = java.lang.Math.abs(problem.getMinFitness()-problem.getMaxFitness() );
    double fitRange = 0, max = -Double.MAX_VALUE, min = Double.MAX_VALUE, tmp;
    for (int x=0; x<gridx; x++) {
      for (int y=0; y<gridy; y++) {
            rx = problem.get2DBorder()[0][0]+x*rw;
            ry = problem.get2DBorder()[1][0]+y*rh;
            pos[0] = rx; pos[1] = ry;
            tmp = (float)(problem.functionValue(pos));
            if (tmp < min) min = tmp;
            if (tmp > max) max = tmp;
      } // for y
    } // for x
    fitRange = java.lang.Math.abs(max - min);
    ColorBarCalculator colorBar = new ColorBarCalculator(colorScale);

    m_Frame.setVisible(false);
    for (int x=0; x<gridx; x++) {
      for (int y=0; y<gridy; y++) {
        rx = problem.get2DBorder()[0][0]+x*rw;
        ry = problem.get2DBorder()[1][0]+y*rh;
        pos[0] = rx; pos[1] = ry;
        DRectangle rect = new DRectangle(rx,ry,rw,rh);
        Color color = new Color(colorBar.getRGB((float)((problem.functionValue(pos)-min)/fitRange)));
        // Color color = new Color(255,(int)(problem.doEvaluation(pos)[0]/fitRange*255),(int)(problem.doEvaluation(pos)[0]/fitRange*255));
//        Color color = new Color(colorBar.getRGB((float)(problem.functionValue(pos)/fitRange))); // Color color = new Color(255,(int)(problem.doEvaluation(pos)[0]/fitRange*255),(int)(problem.doEvaluation(pos)[0]/fitRange*255));
        rect.setColor(color);
        rect.setFillColor(color);
        m_PlotArea.addDElement(rect);
      } // for y
    } // for x
    m_Frame.setVisible(true);

  } // setTopology



} // class
