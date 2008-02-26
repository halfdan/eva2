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
public interface IndividualInterface {
  public IndividualInterface getClone();
  public double[] getFitness();
  public void SetFitness (double[] fit);
  public double[] getDoubleArray();
  public boolean isDominant(double[] otherFitness);
  public boolean isDominant(IndividualInterface other);
}