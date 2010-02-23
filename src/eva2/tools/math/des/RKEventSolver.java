package eva2.tools.math.des;

import java.io.Serializable;

import eva2.tools.math.Mathematics;

/**
 * Title: JAVA-EVA Description: Runge-Kutta Method with Events Copyright:
 * Copyright (c) 2002 Company: University of T&uuml;bingen, Computer
 * Architecture
 * 
 * @author Hannes Planatscher
 * @author Andreas Dr&auml;ger
 * @author Marcel Kronfeld
 * @author Alexander D&ouml;rr
 * @version 1.1 Status: works, but numerical inaccurate. Modified to process
 *          events, e.g. sudden changes in the absolute values of the system
 */
public class RKEventSolver extends AbstractDESSolver implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3383457963743552159L;
	/**
	 * 
	 */
	private static boolean useLinearCalc = true;

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		new RKSolver(0.01);
	}

	/**
	 * 
	 */
	transient protected double[] k0tmp, k1tmp, k2tmp;
	/**
	 * 
	 */
	transient protected double[][] kVals = null;
	/**
	 * 
	 */
	boolean nonnegative = true;
	/**
	 * 
	 */
	double stepSize = 0.01;
	/**
	 * 
	 */
	boolean unstableFlag;

	/**
	 * 
	 */
	public RKEventSolver() {

	}

	/**
	 * A constructor.
	 * 
	 * @param withLinearCalc
	 *            set whether the linear or old calculation method will be used.
	 */
	public RKEventSolver(boolean withLinearCalc) {
		useLinearCalc = withLinearCalc;

	}

	/**
	 * put your documentation comment here
	 */
	public RKEventSolver(double stepSize) {
		this.stepSize = stepSize;

	}

	/**
	 * @return
	 */
	public double getStepSize() {
		return stepSize;
	}

	/**
	 * @return
	 */
	public boolean isUnstable() {
		return unstableFlag;
	}

	/**
	 * @param DES
	 * @param h
	 * @param x
	 * @param Ytemp
	 * @return
	 */
	public double[] rkTerm(EventDESystem EDES, double h, double x,
			double[] Ytemp) {
		double[][] K = new double[4][];
		K[0] = Mathematics.svMult(h, EDES.getValue(x, Ytemp));
		K[1] = Mathematics.svMult(h, EDES.getValue(x + h / 2, Mathematics
				.vvAdd(Ytemp, Mathematics.svMult(0.5, K[0]))));
		K[2] = Mathematics.svMult(h, EDES.getValue(x + h / 2, Mathematics
				.vvAdd(Ytemp, Mathematics.svMult(0.5, K[1]))));
		K[3] = Mathematics.svMult(h, EDES.getValue(x + h, Mathematics.vvAdd(
				Ytemp, K[2])));

		double[] change = Mathematics.svDiv(6, Mathematics.vvAdd(K[0],
				Mathematics.vvAdd(Mathematics.svMult(2, K[1]), Mathematics
						.vvAdd(Mathematics.svMult(2, K[2]), K[3]))));
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
	public void rkTerm2(EventDESystem EDES, double h, double x, double[] Ytemp,
			double[] res) {
		if (kVals == null) { // "static" vectors which are allocated only once
			k0tmp = new double[EDES.getDESystemDimension()];
			k1tmp = new double[EDES.getDESystemDimension()];
			k2tmp = new double[EDES.getDESystemDimension()];
			kVals = new double[4][EDES.getDESystemDimension()];
		}

		// double[][] K = new double[4][];
		EDES.getValue(x, Ytemp, kVals[0]);
		Mathematics.svMult(h, kVals[0], kVals[0]);

		// K[0] = svMult(h, DES.getValue(x, Ytemp));

		Mathematics.svMult(0.5, kVals[0], k0tmp);
		Mathematics.vvAdd(Ytemp, k0tmp, k0tmp);
		EDES.getValue(x + h / 2, k0tmp, kVals[1]);
		Mathematics.svMult(h, kVals[1], kVals[1]);

		// K[1] = svMult(h, DES.getValue(x + h / 2, vvAdd(Ytemp, svMult(0.5,
		// K[0]))));

		Mathematics.svMult(0.5, kVals[1], k1tmp);
		Mathematics.vvAdd(Ytemp, k1tmp, k1tmp);
		EDES.getValue(x + h / 2, k1tmp, kVals[2]);
		Mathematics.svMult(h, kVals[2], kVals[2]);

		// K[2] = svMult(h, DES.getValue(x + h / 2, vvAdd(Ytemp, svMult(0.5,
		// K[1]))));

		Mathematics.vvAdd(Ytemp, kVals[2], k2tmp);
		EDES.getValue(x + h, k2tmp, k1tmp);
		Mathematics.svMult(h, k1tmp, kVals[3]);

		// K[3] = svMult(h, DES.getValue(x + h, vvAdd(Ytemp, K[2])));

		Mathematics.svMult(2, kVals[2], k0tmp);
		Mathematics.vvAdd(k0tmp, kVals[3], k0tmp);

		Mathematics.svMult(2, kVals[1], k1tmp);
		Mathematics.vvAdd(k1tmp, k0tmp, k2tmp);

		Mathematics.vvAdd(kVals[0], k2tmp, k1tmp);
		Mathematics.svDiv(6, k1tmp, res);

		// double[] change = svDiv(6, vvAdd(K[0], vvAdd(svMult(2, K[1]),
		// vvAdd(svMult(2, K[2]), K[3]))));
		// for (int i=0; i<res.length; i++) {
		// double diff = Math.abs(res[i]-change[i]);
		// if (diff > 0.00000001) System.out.println("!!! ");
		// }

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

	/**
	 * @param stepSize
	 */
	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}

	/**
	 * @param unstableFlag
	 */
	public void setUnstableFlag(boolean unstableFlag) {
		this.unstableFlag = unstableFlag;
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

	public double[][] solveAtTimePoints(EventDESystem EDES,
			double[] initialValues, double[] timePoints) {
		return solveAtTimePoints(EDES, initialValues, timePoints, false);
	}

	/**
	 * This method returns a matrix in which the first column includes all time
	 * points. Every row is composed as time and all values at this time point.
	 * It uses the same integration method than the regular
	 * <code>solveatTimepoints</code> method.
	 * 
	 * @param DES
	 * @param initialValues
	 * @param timePoints
	 * @return
	 */
	public double[][] solveAtTimePointsIncludingTime(EventDESystem EDES,
			double[] initialValues, double[] timePoints) {
		return solveAtTimePoints(EDES, initialValues, timePoints, true);
	}

	/**
   *
   */
	public double[][] solveAtTimePointsWithInitialConditions(
			EventDESystem EDES, double[][] initConditions, double[] timePoints) {
		int order = EDES.getDESystemDimension();
		double[][] result = new double[timePoints.length][order];
		result[0] = initConditions[0];
		double x = timePoints[0];
		for (int i = 1; i < timePoints.length; i++) {
			double h = stepSize;
			double[] Ytemp = new double[order];
			int inbetweensteps = (int) Math
					.floor((timePoints[i] - timePoints[i - 1]) / h);
			Ytemp = (double[]) initConditions[i - 1].clone();
			for (int j = 0; j < inbetweensteps; j++) {
				double change[] = rkTerm(EDES, h, x, Ytemp);
				Ytemp = Mathematics.vvAdd(Ytemp, change);
				x += h;
			}

			h = timePoints[i] - x;
			double change[] = rkTerm(EDES, h, x, Ytemp);

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
	 * @param initialValues
	 * @param timeBegin
	 * @param timeEnd
	 * @return
	 */
	public double[][] solveByStepSize(EventDESystem EDES,
			double[] initialValues, double timeBegin, double timeEnd) {
		return solveByStepSize(EDES, initialValues, timeBegin, timeEnd, false);
	}

	/**
	 * @param DES
	 * @param initialValues
	 * @param timeBegin
	 * @param timeEnd
	 * @return
	 */
	public double[][] solveByStepSizeIncludingTime(EventDESystem EDES,
			double[] initialValues, double timeBegin, double timeEnd) {
		return solveByStepSize(EDES, initialValues, timeBegin, timeEnd, true);
	}

	/**
	 * Processes sudden changes in the system due to events in the EDES
	 * 
	 * @param time
	 * @param Ytemp
	 * @param EDES
	 * @param change
	 * @return
	 */
	public double[] processEvents(double time, double[] Ytemp,
			EventDESystem EDES, double[] change) {
		double[] newYtemp = new double[Ytemp.length];
		int index;
		for (DESAssignment event : EDES.processEvents(time, Ytemp)) {
			index = event.getIndex();

			// newYtemp[index] = event.getValue() - (Ytemp[index]);
			newYtemp[index] = event.getValue() - (Ytemp[index] - change[index]);
			System.out
					.printf(
							"time %s: \tYtemp[%s]_old = %s\tYtemp[%s]_new = %s\t change %s \n",
							time, index, Ytemp[index], index,
							(event.getValue() - (Ytemp[index])), change[index]);
		}

		return newYtemp;

	}

	/**
	 * 
	 * @param time
	 * @param Ytemp
	 * @param EDES
	 * @return
	 */
	public void processRules(double time, double[] Ytemp, EventDESystem EDES) {
		int index;
		for (DESAssignment event : EDES.processAssignmentRules(time, Ytemp)) {
			index = event.getIndex();
			Ytemp[index] = event.getValue();
		}

	}

	/**
	 * When set to <code>TRUE</code>, <code>includeTimes</code> will make the
	 * solver to return a matrix with the first column containing the times. By
	 * default the result of the ODE solver just returns the values for Y.
	 * 
	 * @param includeTimes
	 */
	private double[][] solveAtTimePoints(EventDESystem EDES,
			double[] initialValues, double[] timePoints, boolean includeTimes) {
		// sorted timepoints!!!!!!!!!!!!!!!!!!!!!
		int order = EDES.getDESystemDimension();
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

			// int inbetweensteps = (int) Math.round((timePoints[i] -
			// timePoints[i -
			// 1]) / h + 1);
			int inbetweensteps = (int) Math
					.floor((timePoints[i] - timePoints[i - 1]) / h);

			// System.out.println("inbetweensteps at " + i + ": " +
			// inbetweensteps);
			if (includeTimes)
				System.arraycopy(result[i - 1], 1, Ytemp, 0,
						result[i - 1].length - 1);
			else
				Ytemp = result[i - 1].clone();

			// Ytemp = processEventsVoid(x, Ytemp,EDES);

			for (int j = 0; j < inbetweensteps; j++) {

				if (useLinearCalc)
					rkTerm2(EDES, h, x, Ytemp, change);
				else
					change = rkTerm(EDES, h, x, Ytemp);
				// System.out.println("aft change 0 " + change[0]);

				Mathematics.vvAdd(Ytemp, change, Ytemp);

				if (this.nonnegative) {
					for (int k = 0; k < Ytemp.length; k++) {
						if (Ytemp[k] < 0)
							Ytemp[k] = 0;
					}
				}

				x += h;

				// process events
				double[] YtempClone2 = processEvents(x, Ytemp, EDES, change);
				Mathematics.vvAdd(Ytemp, YtempClone2, Ytemp);
				processRules(x, Ytemp, EDES);

			}

			h = timePoints[i] - x;

			if (useLinearCalc)
				rkTerm2(EDES, h, x, Ytemp, change);
			else
				change = rkTerm(EDES, h, x, Ytemp);

			Mathematics.vvAdd(Ytemp, change, Ytemp);

			if (this.nonnegative) {
				for (int k = 0; k < Ytemp.length; k++) {
					if (Ytemp[k] < 0)
						Ytemp[k] = 0;
				}
			}
			// process events
			double[] YtempClone = processEvents(x, Ytemp, EDES, change);
			Mathematics.vvAdd(Ytemp, YtempClone, Ytemp);
			processRules(x, Ytemp, EDES);

			if (includeTimes) {
				result[i][0] = timePoints[i];
				System.arraycopy(Ytemp, 0, result[i], 1, Ytemp.length);
			} else
				result[i] = Ytemp;
			x += h;

		}

		return result;
	}

	/**
	 * @param DES
	 * @param initialValues
	 * @param timeBegin
	 * @param timeEnd
	 * @return
	 */
	private double[][] solveByStepSize(EventDESystem EDES,
			double[] initialValues, double timeBegin, double timeEnd,
			boolean time) {
		int numsteps = (int) Math.round(((timeEnd - timeBegin) / stepSize) + 1);
		unstableFlag = false;
		// System.out.println(numsteps);
		int order = EDES.getDESystemDimension(), i;
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
				System.arraycopy(result[i - 1], 1, tmp, 0,
						result[i - 1].length - 1);
				change = rkTerm(EDES, h, x, tmp);
				Ytemp = Mathematics.vvAdd(tmp, change);
			} else {
				change = rkTerm(EDES, h, x, result[i - 1]);
				Ytemp = Mathematics.vvAdd(result[i - 1], change);
			}
			if (this.nonnegative) {
				for (int k = 0; k < Ytemp.length; k++) {
					if (Ytemp[k] < 0)
						Ytemp[k] = 0;
				}
			}
			x += h;
			if (time) {
				System.arraycopy(Ytemp, 0, result[i], 1, Ytemp.length);
				result[i][0] = x;
			} else
				result[i] = Ytemp;
		}

		return result;
	}

	// @Override
	public double[][] solveAtTimePoints(DESystem DES, double[] initialvalue,
			double[] timepoints) {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	public double[][] solveAtTimePointsWithInitialConditions(DESystem DES,
			double[][] initconditions, double[] timepoints) {
		// TODO Auto-generated method stub
		return null;
	}

}
