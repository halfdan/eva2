package eva2.tools.math.des;

import java.io.Serializable;

/**
 * Title: JAVA-EVA Description: Copyright: Copyright (c) 2002 Company:
 * University of T&uuml;bingen, Computer Architecture
 *
 * @author Hannes Planatscher
 * @version 1.0
 */

public interface DESystem extends Serializable {

	/**
	 * Returns the number of dimensions of this ODE system.
	 *
	 * @return Returns the number of dimensions of this ODE system.
	 */
	public int getDESystemDimension();

	/**
	 * Returns the value of the ODE system at the time t given the current
	 * values of Y
	 *
	 * @param t
	 * @param Y
	 * @return
	 * @deprecated use getValue(double t, double[] Y, double[] res) to avoid
	 *             array reallocations and gain speed
	 */
	public double[] getValue(double t, double[] Y);

	/**
	 * Returns the value of the ODE system at the time t given the current
	 * values of Y within resultVector.
	 *
	 * @param t
	 * @param Y
	 * @param resultVector
	 */
	public void getValue(double t, double[] Y, double[] resultVector);
}
