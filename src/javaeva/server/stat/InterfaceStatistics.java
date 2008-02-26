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
import javaeva.server.go.IndividualInterface;
import javaeva.server.go.PopulationInterface;
import javaeva.server.go.problems.InterfaceAdditionalPopulationInformer;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface InterfaceStatistics {
  public void startOptPerformed(String InfoString,int runnumber); // called from processor
  public void stopOptPerformed(boolean normal); // called from processor
  public void addTextListener(InterfaceTextListener listener);
  public boolean removeTextListener(InterfaceTextListener listener);
  public void printToTextListener(String s);
  public void createNextGenerationPerformed(PopulationInterface Pop, InterfaceAdditionalPopulationInformer informer);
  public void createNextGenerationPerformed(double[] bestfit,double[] worstfit,int calls);
  public StatisticsParameter getStatisticsParameter(); // called from moduleadapter
  public IndividualInterface getBestSolution(); // returns the best overall solution
  public double[] getBestFitness(); // returns the best overall fitness
}