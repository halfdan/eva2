package javaeva.server.stat;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.util.List;

import javaeva.tools.SelectedTag;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface StatisticsParameter {
  public String getName();
  public void saveInstance();
  public String globalInfo();
//  public void setTextoutput(int i);
  public void setPlotoutput(int i);
  public int GetPlotoutput();
//  public int GetTextoutput();
//  public String textoutputTipText();
//  public String plotFrequencyTipText();
  public void setMultiRuns(int x);
  public int getMultiRuns();
  public String multiRunsTipText();

  public String GetInfoString();
  public void setInfoString(String s);
  
  public boolean GetuseStatPlot();
  public void setuseStatPlot(boolean x);
  
  public List<String[]> getPlotDescriptions();

  public SelectedTag getPlotFitness();
  public void setPlotFitness(SelectedTag newMethod);

  public String getResultFileName();
  public void setResultFileName(String x);
  
  public void setConvergenceRateThreshold(double x);
  public double getConvergenceRateThreshold();
  
  public boolean isShowTextOutput();
  public void setShowTextOutput(boolean bShow);
//  public String showTextOutputTipText();
}