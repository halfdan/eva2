package javaeva.server.go;

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


/*==========================================================================*
 * INTERFACE DECLARATION
 *==========================================================================*/
/**
 *
 */
public interface PopulationInterface {
  public IndividualInterface getBestIndividual();
  public IndividualInterface getWorstIndividual();
  public double[] getBestFitness();
  public double[] getWorstFitness();
  public double[] getMeanFitness();
  public int getFunctionCalls();
  public int getGenerations();
  /** This method returns problem specific data
   * @return double[]
   */
  public double[] getSpecificData();

  /** This method returns identifiers for the
   * specific data
   * Note: "Pareto-Front" is reserved for mulit-crit. Problems
   *        string[1] gives the dimension of the fitness values
   * @return String[]
   */
  public String[] getSpecificDataNames();
  public Object get(int i);
  public int size();
  public void clear();
}