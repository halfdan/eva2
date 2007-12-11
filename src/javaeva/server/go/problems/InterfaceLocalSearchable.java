package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.populations.Population;

/**
 * <p>Title: The JavaEvA</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author planatsc
 * @version 1.0
 */

public interface InterfaceLocalSearchable extends InterfaceOptimizationProblem {


  public void doLocalSearch(Population pop);

  public double getLocalSearchStepFunctionCallEquivalent();

}
