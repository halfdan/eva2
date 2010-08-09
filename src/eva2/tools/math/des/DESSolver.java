/**
 * Title: JAVA-EVA Description: Copyright: Copyright (c) 2002 Company:
 * University of T&uuml;bingen, Computer Architecture
 *
 * @author
 * @version 1.0
 */

package eva2.tools.math.des;

import java.io.Serializable;

/**
 * 
 * TODO: comment missing
 * 
 * @since 2.0
 * @version Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *          JDK 1.6.0
 * @date Sep 10, 2007
 * @depend - <call> - DESystem
 */
public interface DESSolver extends Serializable {
	
	/**
	 * 
	 * @return
	 */
	public boolean isUnstable();

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

	/**
	 * 
	 * @param DES
	 * @param initialvalue
	 * @param timepoints
	 * @return
	 */
	public double[][] solveAtTimePoints(DESystem DES, double[] initialvalue,
			double[] timepoints);
	
	/**
	 * 
	 * @param DES
	 * @param initconditions
	 * @param timepoints
	 * @return
	 */
	public double[][] solveAtTimePointsWithInitialConditions(DESystem DES,
			double[][] initconditions, double[] timepoints);
}
