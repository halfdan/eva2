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
public class GenerationTerminator implements  TerminatorInterface,
                                              Serializable {
  /**
   * Number of fitnness calls on the problem which is optimized
   */
  protected int m_Generations = 100;
   public void init(){}
   /**
   *
   */
  public String globalInfo() {
    return "Terminate after the given number of generations";
  }
  /**
   *
   */
  public GenerationTerminator() {
  }
  /**
   *
   */
  public boolean isTerminated(PopulationInterface Pop) {
    if (m_Generations<Pop.getGenerations())
      return true;
    return false;
  }
  /**
   *
   */
  public String toString() {
    String ret = "\r\nGenerations calls="+m_Generations;
    return ret;
  }
  /**
   *
   */
  public void setGenerations(int x) {
    m_Generations = x;
  }
  /**
   *
   */
  public int getGenerations() {
    return m_Generations;
  }
   /**
   * Returns the tip text for this property
   * @return tip text for this property
   */
  public String generationsTipText() {
    return "number of generations to evaluate.";
  }
}