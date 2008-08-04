package eva2.tools.des;

import eva2.tools.Mathematics;

/**
 * Title: JAVA-EVA Description: Runge-Kutta Method Copyright: Copyright (c) 2002
 * Company: University of T&uuml;bingen, Computer Architecture
 *
 * @author Hannes Planatscher
 * @author Andreas Dr&auml;ger
 * @author Marcel Kronfeld
 * @version 1.0 Status: works, but numerical inaccurate
 */
public class RKSolver implements DESSolver, java.io.Serializable {
  double                         stepSize    = 0.01;
  boolean                        nonnegative = true;
  boolean                        unstableFlag;
  transient protected double[][] kVals       = null;
  transient protected double[]   k0tmp, k1tmp, k2tmp;
  private static boolean useLinearCalc = true;

  /**
   *
   */
  public RKSolver() {
  }

  /**
   * A constructor.
   *
   * @param withLinearCalc set whether the linear or old calculation method will be used.
   */
  public RKSolver(boolean withLinearCalc) {
	  useLinearCalc = withLinearCalc;
  }

  
  /**
   * put your documentation comment here
   */
  public RKSolver(double stepSize) {
    this.stepSize = stepSize;
  }
  
  /**
   * Set whether the linear or old calculation method will be used.
   *
   * @param withLinearCalc
   */
  public void setWithLinearCalc(boolean withLinearCalc) {
	  useLinearCalc = withLinearCalc;
  }

  /**
   * put your documentation comment here
   *
   * @param DES
   * @param initialValues
   * @param x
   * @param h
   * @param steps
   * @return
   */
  public double[][] solve(DESystem DES, double[] initialValues, double x,
      double h, int steps) {
    double[] timeVector = new double[steps];
    for (int i = 0; i < steps; i++)
      timeVector[i] = x + i * h;
    return solveAtTimePoints(DES, initialValues, timeVector);
  }

  /**
   * @param DES
   * @param initialValues
   * @param timeBegin
   * @param timeEnd
   * @return
   */
  public double[][] solveByStepSize(DESystem DES, double[] initialValues,
      double timeBegin, double timeEnd) {
    return solveByStepSize(DES, initialValues, timeBegin, timeEnd, false);
  }

  /**
   * @param DES
   * @param initialValues
   * @param timeBegin
   * @param timeEnd
   * @return
   */
  public double[][] solveByStepSizeIncludingTime(DESystem DES,
      double[] initialValues, double timeBegin, double timeEnd) {
    return solveByStepSize(DES, initialValues, timeBegin, timeEnd, true);
  }

  /**
   * @param DES
   * @param initialValues
   * @param timeBegin
   * @param timeEnd
   * @return
   */
  private double[][] solveByStepSize(DESystem DES, double[] initialValues,
      double timeBegin, double timeEnd, boolean time) {
    int numsteps = (int) Math.round(((timeEnd - timeBegin) / stepSize) + 1);
    unstableFlag = false;
    // System.out.println(numsteps);
    int order = DES.getDESystemDimension(), i;
    double[][] result;
    if (time) {
      result = new double[numsteps][order + 1];
      result[0][0] = timeBegin;
      for (i = 0; i < order; i++)
        result[0][i + 1] = initialValues[i];
    } else {
      result = new double[numsteps][order];
      for (i = 0; i < order; i++)
        result[0][i] = initialValues[i];
    }
    double x = timeBegin;
    for (i = 1; i < numsteps; i++) {
      double h = stepSize, change[] = null, Ytemp[] = null;
      if (time) {
        double tmp[] = new double[result[i - 1].length - 1];
        System.arraycopy(result[i - 1], 1, tmp, 0, result[i - 1].length - 1);
        change = rkTerm(DES, h, x, tmp);
        Ytemp = Mathematics.vvAdd(tmp, change);
      } else {
        change = rkTerm(DES, h, x, result[i - 1]);
        Ytemp = Mathematics.vvAdd(result[i - 1], change);
      }
      if (this.nonnegative) {
        for (int k = 0; k < Ytemp.length; k++) {
          if (Ytemp[k] < 0) Ytemp[k] = 0;
        }
      }
      x += h;
      if (time) {
        System.arraycopy(Ytemp, 0, result[i], 1, Ytemp.length);
        result[i][0] = x;
      } else result[i] = Ytemp;
    }

    return result;
  }

  public double[][] solveAtTimePoints(DESystem DES, double[] initialValues,
      double[] timePoints) {
    return solveAtTimePoints(DES, initialValues, timePoints, false);
  }

  /**
   * This method returns a matrix in which the first column includes all time
   * points. Every row is composed as time and all values at this time point. It
   * uses the same integration method than the regular
   * <code>solveatTimepoints</code> method.
   *
   * @param DES
   * @param initialValues
   * @param timePoints
   * @return
   */
  public double[][] solveAtTimePointsIncludingTime(DESystem DES,
      double[] initialValues, double[] timePoints) {
    return solveAtTimePoints(DES, initialValues, timePoints, true);
  }

  /**
   * When set to <code>TRUE</code>, <code>includeTimes</code> will make the
   * solver to return a matrix with the first column containing the times. By
   * default the result of the ODE solver just returns the values for Y.
   *
   * @param includeTimes
   */
  private double[][] solveAtTimePoints(DESystem DES, double[] initialValues,
      double[] timePoints, boolean includeTimes) {
    // sorted timepoints!!!!!!!!!!!!!!!!!!!!!
    int order = DES.getDESystemDimension();
    double result[][], x = timePoints[0];
    if (includeTimes) {
      result = new double[timePoints.length][order + 1];
      result[0][0] = timePoints[0];
      for (int i = 1; i <= order; i++)
        result[0][i] = initialValues[i - 1];
    } else {
      result = new double[timePoints.length][order];
      for (int i = 0; i < order; i++)
        result[0][i] = initialValues[i];
    }
    // System.out.println("JavaCalled");
    unstableFlag = false;

    double h = stepSize;
    double change[] = new double[order];
    double[] Ytemp = new double[order];

    for (int i = 1; i < timePoints.length; i++) {
      h = stepSize;

//       int inbetweensteps = (int) Math.round((timePoints[i] - timePoints[i -
//       1]) / h + 1);
      int inbetweensteps = (int) Math.floor((timePoints[i] - timePoints[i - 1])
          / h);

      //System.out.println("inbetweensteps at " + i + ": " + inbetweensteps);
      if (includeTimes)
        System.arraycopy(result[i - 1], 1, Ytemp, 0, result[i - 1].length - 1);
      else Ytemp = result[i - 1].clone();

      for (int j = 0; j < inbetweensteps; j++) {
          if (useLinearCalc) rkTerm2(DES, h, x, Ytemp, change);
          else change = rkTerm(DES, h, x, Ytemp);
//      	System.out.println("aft change 0 " + change[0]);

          Mathematics.vvAdd(Ytemp, change, Ytemp);

        if (this.nonnegative) {
          for (int k = 0; k < Ytemp.length; k++) {
            if (Ytemp[k] < 0) Ytemp[k] = 0;
          }
        }

        x += h;
      }

      h = timePoints[i] - x;

      if (useLinearCalc) rkTerm2(DES, h, x, Ytemp, change);
      else change = rkTerm(DES, h, x, Ytemp);

      Mathematics.vvAdd(Ytemp, change, Ytemp);

      if (this.nonnegative) {
        for (int k = 0; k < Ytemp.length; k++) {
          if (Ytemp[k] < 0) Ytemp[k] = 0;
        }
      }

      if (includeTimes) {
        result[i][0] = timePoints[i];
        System.arraycopy(Ytemp, 0, result[i], 1, Ytemp.length);
      } else result[i] = Ytemp;
      x += h;

    }

    return result;
  }

  /**
   *
   */
  public double[][] solveAtTimePointsWithInitialConditions(DESystem DES,
      double[][] initConditions, double[] timePoints) {
    int order = DES.getDESystemDimension();
    double[][] result = new double[timePoints.length][order];
    result[0] = initConditions[0];
    double x = timePoints[0];
    for (int i = 1; i < timePoints.length; i++) {
      double h = stepSize;
      double[] Ytemp = new double[order];
      int inbetweensteps = (int) Math.floor((timePoints[i] - timePoints[i - 1])
          / h);
      Ytemp = (double[]) initConditions[i - 1].clone();
      for (int j = 0; j < inbetweensteps; j++) {
        double change[] = rkTerm(DES, h, x, Ytemp);
        Ytemp = Mathematics.vvAdd(Ytemp, change);
        x += h;
      }

      h = timePoints[i] - x;
      double change[] = rkTerm(DES, h, x, Ytemp);

      Ytemp = Mathematics.vvAdd(Ytemp, change);

      if (this.nonnegative) {
        for (int k = 0; k < Ytemp.length; k++) {
          if (Ytemp[k] < 0) {
            Ytemp[k] = 0;
          }
        }
      }
      result[i] = Ytemp;
      x += h;
    }

    return result;

  }

  /**
   * @param DES
   * @param h
   * @param x
   * @param Ytemp
   * @return
   */
  public double[] rkTerm(DESystem DES, double h, double x, double[] Ytemp) {
    double[][] K = new double[4][];
    K[0] = Mathematics.svMult(h, DES.getValue(x, Ytemp));
    K[1] = Mathematics.svMult(h, DES.getValue(x + h / 2, Mathematics.vvAdd(Ytemp, Mathematics.svMult(0.5, K[0]))));
    K[2] = Mathematics.svMult(h, DES.getValue(x + h / 2, Mathematics.vvAdd(Ytemp, Mathematics.svMult(0.5, K[1]))));
    K[3] = Mathematics.svMult(h, DES.getValue(x + h, Mathematics.vvAdd(Ytemp, K[2])));
    
    double[] change = Mathematics.svDiv(6, Mathematics.vvAdd(K[0], Mathematics.vvAdd(Mathematics.svMult(2, K[1]), Mathematics.vvAdd(Mathematics.svMult(
        2, K[2]), K[3]))));
    for (int k = 0; k < change.length; k++) {
      if (Double.isNaN(change[k])) {
        unstableFlag = true;
        change[k] = 0;
        // return result;
      }
    }
    return change;
  }

  /**
   * Linearized code for speed-up (no allocations).
   *
   * @param DES
   * @param h
   * @param x
   * @param Ytemp
   * @return
   */
  public void rkTerm2(DESystem DES, double h, double x, double[] Ytemp,
      double[] res) {
    if (kVals == null) { // "static" vectors which are allocated only once
      k0tmp = new double[DES.getDESystemDimension()];
      k1tmp = new double[DES.getDESystemDimension()];
      k2tmp = new double[DES.getDESystemDimension()];
      kVals = new double[4][DES.getDESystemDimension()];
    }

//    double[][] K = new double[4][];
    DES.getValue(x, Ytemp, kVals[0]);
    Mathematics.svMult(h, kVals[0], kVals[0]);
    
//    K[0] = svMult(h, DES.getValue(x, Ytemp));

    Mathematics.svMult(0.5, kVals[0], k0tmp);
    Mathematics.vvAdd(Ytemp, k0tmp, k0tmp);
    DES.getValue(x + h / 2, k0tmp, kVals[1]);
    Mathematics.svMult(h, kVals[1], kVals[1]);
    
//    K[1] = svMult(h, DES.getValue(x + h / 2, vvAdd(Ytemp, svMult(0.5, K[0]))));

    Mathematics.svMult(0.5, kVals[1], k1tmp);
    Mathematics.vvAdd(Ytemp, k1tmp, k1tmp);
    DES.getValue(x + h / 2, k1tmp, kVals[2]);
    Mathematics.svMult(h, kVals[2], kVals[2]);
    
//    K[2] = svMult(h, DES.getValue(x + h / 2, vvAdd(Ytemp, svMult(0.5, K[1]))));

    Mathematics.vvAdd(Ytemp, kVals[2], k2tmp);
    DES.getValue(x + h, k2tmp, k1tmp);
    Mathematics.svMult(h, k1tmp, kVals[3]);
    
//    K[3] = svMult(h, DES.getValue(x + h, vvAdd(Ytemp, K[2])));
    
    Mathematics.svMult(2, kVals[2], k0tmp);
    Mathematics.vvAdd(k0tmp, kVals[3], k0tmp);

    Mathematics.svMult(2, kVals[1], k1tmp);
    Mathematics.vvAdd(k1tmp, k0tmp, k2tmp);

    Mathematics.vvAdd(kVals[0], k2tmp, k1tmp);
    Mathematics.svDiv(6, k1tmp, res);

//    double[] change = svDiv(6, vvAdd(K[0], vvAdd(svMult(2, K[1]), vvAdd(svMult(2, K[2]), K[3]))));
//    for (int i=0; i<res.length; i++) {
//    	double diff = Math.abs(res[i]-change[i]);
//    	if (diff > 0.00000001) System.out.println("!!! ");
//    }

    // double[] change = svdiv(6, vvadd(kVals[0], vvadd(svmult(2, kVals[1]),
    // vvadd(svmult(2, kVals[2]), kVals[3]))));
    for (int k = 0; k < res.length; k++) {
      if (Double.isNaN(res[k])) {
        unstableFlag = true;
        res[k] = 0;
        // return result;
      }
    }
  }

  public static void main(String args[]) {
    new RKSolver(0.01);
  }

  /**
   * @return
   */
  public boolean isUnstable() {
    return unstableFlag;
  }

  /**
   * @param unstableFlag
   */
  public void setUnstableFlag(boolean unstableFlag) {
    this.unstableFlag = unstableFlag;
  }

  /**
   * @return
   */
  public double getStepSize() {
    return stepSize;
  }

  /**
   * @param stepSize
   */
  public void setStepSize(double stepSize) {
    this.stepSize = stepSize;
  }

}
