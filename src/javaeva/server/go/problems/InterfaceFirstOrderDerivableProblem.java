package javaeva.server.go.problems;

/**
 * <p>Title: The JavaEvA</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface InterfaceFirstOrderDerivableProblem {

  public double[] getFirstOrderGradients(double[] x);
  public double getFirstOrderGradient(int paramindex,double[] x);


}
