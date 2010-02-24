package eva2.gui;
/*
 * Title:        EvA2
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
import java.awt.Color;

import eva2.server.go.problems.Interface2DBorderProblem;
import eva2.server.go.problems.InterfaceFirstOrderDerivableProblem;
import eva2.tools.chart2d.DRectangle;
import eva2.tools.diagram.ColorBarCalculator;
import eva2.tools.math.Mathematics;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class TopoPlot extends Plot {

  private int gridx = 50;
  private int gridy = 50;
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
   * Defines parameters used for drawing the topology.
   * @param gridX the x-resolution of the topology, higher value means higher resolution
   * @param gridY the y-resolution of the topology, higher value means higher resolution
   */
  public void setParams(int gridX, int gridY) {
    setParams(gridX, gridY, colorScale);
  }

  /**
   * Defines the topology (by setting a specific problem) and draws the topology
   */
  public void setTopology(Interface2DBorderProblem problem) {
	  setTopology(problem, problem.get2DBorder(), false);
  }
   /**
   * Defines the topology (by setting a specific problem) and draws the topology
   */
  public void setTopology(Interface2DBorderProblem problem, double[][] border, boolean withGradientsIfAvailable) {
	double[] sizeXY=Mathematics.getAbsRange(border);
    double deltaX = sizeXY[0]/gridx;
    double deltaY = sizeXY[1]/gridy;
    double maxDeriv=0;
    double[] pos = new double[2];
    boolean TRACEMETH=false;
    //double fitRange = java.lang.Math.abs(problem.getMinFitness()-problem.getMaxFitness() );
    double fitRange = 0, max = -Double.MAX_VALUE, min = Double.MAX_VALUE, tmp;
    for (int x=0; x<gridx; x++) {
    	for (int y=0; y<gridy; y++) {
    		pos[0] = border[0][0]+x*deltaX;
    		pos[1] = border[1][0]+y*deltaY;
    		tmp = (float)(problem.functionValue(pos));
    		if (TRACEMETH) System.out.println(pos[0] + " " + pos[1] + " " + tmp);
    		if (tmp < min) min = tmp;
    		if (tmp > max) max = tmp;
    		if (withGradientsIfAvailable && (problem instanceof InterfaceFirstOrderDerivableProblem)) {
    			double[] deriv = ((InterfaceFirstOrderDerivableProblem)problem).getFirstOrderGradients(pos);
    			for (int i=0; i<2;i++) maxDeriv=Math.max(maxDeriv, Math.abs(deriv[i])); // maximum deriv of first 2 dims
    		}

    	} // for y
    } // for x
    fitRange = java.lang.Math.abs(max - min);
    ColorBarCalculator colorBar = new ColorBarCalculator(colorScale);

    m_Frame.setVisible(false);
    for (int x=0; x<gridx; x++) {
    	for (int y=0; y<gridy; y++) {
    		pos[0]  = border[0][0]+x*deltaX;
    		pos[1]  = border[1][0]+y*deltaY;
    		DRectangle rect = new DRectangle(pos[0]-(deltaX/2),pos[1]-(deltaY/2),deltaX,deltaY);
    		Color color = new Color(colorBar.getRGB((float)((problem.functionValue(pos)-min)/fitRange)));
    		// Color color = new Color(255,(int)(problem.doEvaluation(pos)[0]/fitRange*255),(int)(problem.doEvaluation(pos)[0]/fitRange*255));
    		//  	  Color color = new Color(colorBar.getRGB((float)(problem.functionValue(pos)/fitRange))); // Color color = new Color(255,(int)(problem.doEvaluation(pos)[0]/fitRange*255),(int)(problem.doEvaluation(pos)[0]/fitRange*255));
    		rect.setColor(color);
    		rect.setFillColor(color);
    		m_PlotArea.addDElement(rect);
    	} // for y
    } // for x
    if (withGradientsIfAvailable && (problem instanceof InterfaceFirstOrderDerivableProblem)) {
    	for (int x=0; x<gridx; x++) {
    		for (int y=0; y<gridy; y++) {
    			pos[0]  = border[0][0]+x*deltaX;
    			pos[1]  = border[1][0]+y*deltaY;
    			double[] derivPos = ((InterfaceFirstOrderDerivableProblem)problem).getFirstOrderGradients(pos);
    			Mathematics.svDiv(1.1*(2*maxDeriv/Math.max(deltaX, deltaY)), derivPos, derivPos);
    			Mathematics.vvAdd(pos, derivPos, derivPos);
    			getFunctionArea().drawLine(pos, derivPos);
    			getFunctionArea().drawIcon(1, "", derivPos, 0);
    		} // for y
    	} // for x
    }
    m_Frame.setVisible(true);

  } // setTopology



} // class
