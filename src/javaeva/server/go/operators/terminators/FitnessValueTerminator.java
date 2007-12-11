package javaeva.server.go.operators.terminators;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 319 $
 *            $Date: 2007-12-05 11:29:32 +0100 (Wed, 05 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.Serializable;

import javaeva.server.go.PopulationInterface;
import javaeva.server.go.TerminatorInterface;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class FitnessValueTerminator implements TerminatorInterface,
                                               Serializable {
  protected double[] m_FitnessValue;
  /**
   *
   */
  public FitnessValueTerminator() {
    m_FitnessValue = new double []{0.1};
  }

 public void init(){}
  /**
   *
   */
  public String globalInfo() {
    return "Terminate if a certain fitness value has been reached.";
  }
  /**
   *
   */
  public FitnessValueTerminator( double[] x) {
    m_FitnessValue = (double[])x.clone();
  }
  /**
   *
   */
  public boolean isTerminated(PopulationInterface Pop) {
    if (m_FitnessValue[0]<Pop.getBestFitness()[0])
      return false;
    return true;
  }
  /**
   *
   */
  public String toString() {
    String ret = "\r\nFitnessValueTerminator m_FitnessValue ="+m_FitnessValue;
    return ret;
  }
  /**
   *
   */
  public void  setFitnessValue(double[] x) {
    m_FitnessValue = x;
  }
  /**
   *
   */
  public double[] getFitnessValue() {
    return m_FitnessValue;
  }
}