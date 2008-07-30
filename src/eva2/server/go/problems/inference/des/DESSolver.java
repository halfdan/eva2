/**
 * Title: JAVA-EVA Description: Copyright: Copyright (c) 2002 Company:
 * University of T&uuml;bingen, Computer Architecture
 *
 * @author
 * @version 1.0
 */

package eva2.server.go.problems.inference.des;

import java.io.Serializable;

/**
 *
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 * @date Sep 10, 2007
 *
 */
public interface DESSolver extends Serializable {

  /**
   * put your documentation comment here
   *
   * @param DES
   * @param initalvalue
   * @param x
   * @param h
   * @param steps
   * @return
   */
  public double[][] solve(DESystem DES, double[] initalvalue, double x,
      double h, int steps);

  public double[][] solveAtTimePoints(DESystem DES, double[] initialvalue,
      double[] timepoints);

  public double[][] solveAtTimePointsWithInitialConditions(DESystem DES,
      double[][] initconditions, double[] timepoints);

  public boolean isUnstable();

  /*
   * public double[][] solveatTimepointsSSystem (double[] param, int order,
   * double[] initialvalue, double[] timepoints); public boolean
   * lastDESystemInvalid(); public void plotY(); public double[][]
   * solveatTimepointsSSystemSeparated (double[] param, int order, double[]
   * initialvalue, double[] timepoints, GEdata gedata, int tooptimize);
   */
}
