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
public class EvaluationTerminator implements TerminatorInterface,
                                              Serializable {
  /**
   * Number of fitness calls on the problem which is optimized.
   */
  protected int m_FitnessCalls = 1000;
  /**
   *
   */
  public EvaluationTerminator() {
  }
   public void init(){}
   /**
   *
   */
  public String globalInfo() {
    return "Terminates after the given number of fitness calls.";
  }
  /**
   *
   */
  public EvaluationTerminator(int x) {
    m_FitnessCalls = x;
  }
  /**
   *
   */
  public boolean isTerminated(PopulationInterface Pop) {
    //System.out.println("m_FitnessCalls="+m_FitnessCalls);
    if (m_FitnessCalls>Pop.getFunctionCalls())
      return false;
    return true;
  }
  /**
   *
   */
  public String toString() {
    String ret = "\r\nEvaluationTerminator fitness calls="+m_FitnessCalls;
    return ret;
  }
  /**
   *
   */
  public void setFitnessCalls(int x) {
    //System.out.println("setFitnessCalls"+x);
    m_FitnessCalls = x;
  }
  /**
   *
   */
  public int getFitnessCalls() {
    //System.out.println("getFitnessCalls"+m_FitnessCalls);
    return m_FitnessCalls;
  }
   /**
   * Returns the tip text for this property
   * @return tip text for this property
   */
  public String fitnessCallsTipText() {
    return "number of calls to fitness function.";
  }

}