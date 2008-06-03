package eva2.server.go.problems;

import eva2.server.go.populations.Population;

/**
 * <p>Title: EvA2</p>
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
