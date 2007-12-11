package javaeva.server.stat;
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
import javaeva.server.go.PopulationInterface;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface Statistics {
  public void startOptPerformed(String InfoString,int runnumber); // called from processor
  public void stopOptPerformed(boolean normal); // called from processor
  public void printToTextListener(String s);
  public void createNextGenerationPerformed(PopulationInterface Pop);
  public void createNextGenerationPerformed(double[] bestfit,double[] worstfit,int calls);
  public StatisticsParameter getStatisticsParameter(); // called from moduleadapter
  public Object getBestSolution(); // returns the best overall solution
  public double[] getBestFitness(); // returns the best overall fitness
}