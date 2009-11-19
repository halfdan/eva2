package eva2.server.go.problems;

/**
 * An interface for first-order derivable problems which can be used for gradient descent.
 * 
 * @author hplanatscher, mkron
 *
 */
public interface InterfaceFirstOrderDerivableProblem {

	/**
	 * Calculate the first order gradients of this problem.
	 * @param x
	 * @return the first order gradients of this problem
	 */
  public double[] getFirstOrderGradients(double[] x);
//  public double getFirstOrderGradient(int paramindex,double[] x);

}
