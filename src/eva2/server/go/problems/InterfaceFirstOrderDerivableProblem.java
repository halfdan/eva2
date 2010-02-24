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
	 * If you implement this, be aware that some types of AbstractProblemDouble may be rotated,
	 * so you may have to use x = rotateMaybe(x) first.
	 * @param x
	 * @return the first order gradients of this problem
	 */
  public double[] getFirstOrderGradients(double[] x);
//  public double getFirstOrderGradient(int paramindex,double[] x);

}
