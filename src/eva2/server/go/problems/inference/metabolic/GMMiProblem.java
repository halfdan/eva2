package eva2.server.go.problems.inference.metabolic;

import eva2.server.go.problems.inference.metabolic.odes.GMMiSystem;
import eva2.server.go.strategies.InterfaceOptimizer;

// Created at 2007-01-18

/**
 * @author Andreas Dr&auml;ger
 */
public class GMMiProblem extends AbstractValineCurveFittingProblem {

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = -7344294007783094303L;

	public GMMiProblem() {
		super(new GMMiSystem());
	}

	public GMMiProblem(GMMiProblem gmmiproblem) {
		super(gmmiproblem);
		this.m_ProblemDimension = system.getNumberOfParameters();
	}
	
	public String getName() {
		return "GMMiProblem";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eva2.server.go.problems.inference.metabolic.AbstractValineCurveFittingProblem#getParameterRanges()
	 */
	protected double[][] getParameterRanges() {
		int i;
		double[][] range = new double[m_ProblemDimension][2];

		// BRENDA: k+1 k+2 k+4 k+5 k+7 k+8 k+9 l1 l2 Ki1 Ki2 Ki3 vm1 vm2 vm3
		// kia1
		// kia2 kia3 kib1 kib2 kib3 km1 km2 km3
		double[] min = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 1E-8, 1E-8, 1E-8 }, max = { 2000, 2000, 2000, 2000,
				2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000,
				2000, 1E+8, 1E+8, 1E+8, 1E+8, 1E+8, 1E+8, 2000, 2000, 2000 };

		/*
		 * for (i = 0; i < 7; i++) { // k, rate constants for forward reaction
		 * range[i][0] = 0.01; range[i][1] = 2000; } for (i = 7; i < 9; i++) { //
		 * l, rate constants for backward reaction range[i][0] = 0.01;
		 * range[i][1] = 2000; } for (i = 9; i < 12; i++) { // ki, Rate constant
		 * for inhibition of uncertain mechanism range[i][0] = 0.01; range[i][1] =
		 * 100; } for (i = 12; i < 15; i++) { // vm, Maximal reaction velocity
		 * range[i][0] = 0.1; range[i][1] = 2000; } for (i = 15; i < 18; i++) { //
		 * kia' == km/kia, Rate constant for inhibition of the enzyme
		 * range[i][0] = 0; range[i][1] = 100; } for (i = 18; i < 21; i++) { //
		 * kib' == 1/kib, Rate constant for inhibition of the enzyme substrate
		 * complex range[i][0] = 0; range[i][1] = 100; } for (i = 21; i < 24;
		 * i++) { // km, the Michaelis-Menten constant range[i][0] = 0.01;
		 * range[i][1] = 2000; } // AcCo, Constant specifying the concentration
		 * of Acetyl-CoA // This is not needed because p_4 already includes
		 * Acetyl-CoA implicitely. //range[24][0] = 0.01; //range[24][1] = 100; //
		 */
		for (i = 0; i < range.length; i++) {
			range[i][0] = min[i];
			range[i][1] = max[i];
		}

		/*
		 * double initial[] = new double[] { 15.625991725476922, 2000.0, 0.0010,
		 * 0.0010, 0.0014167666438540366, 0.03914648793059087,
		 * 0.0012346931715111, 0.0010, 0.002922248833329009, 50.00050096623751,
		 * 25.00075000582071, 87.5001338911313, 0.0010, 0.0010, 0.0010,
		 * 99.99692409764904, 99.9470190144952, 100.0, 99.99999925494194,
		 * 99.99994374811648, 99.9999922933057, 2000.0, 1999.9999939464062,
		 * 2000.0,
		 *
		 * 30.76270291064184, 1000.0004988358473, 0.0010, 0.0010,
		 * 0.0017147897187887668, 0.04677587864891996, 0.0010852159729891494,
		 * 0.0010, 0.0025497199896605963, 96.87503280922212, 12.500875002910353,
		 * 93.75044398773962, 0.0010, 0.0010, 0.0010, 99.9999962747097,
		 * 99.98389044776184, 100.0, 99.90234374997726, 99.9999973224476,
		 * 99.99389646109051, 1999.9999701976926, 1999.9999664724041, 2000.0
		 *
		 *
		 * //10.773224990548886, 505.8899553355387, 0.0010, 0.0010, 0.0010,
		 * 0.0010, 0.0010, 0.0010, 0.0010, 100.0, //100.0, 100.0, 0.0010,
		 * 0.0010, 0.0010, 100.0, 0.0, 100.0, 100.0, 100.0, 0.0, 2000.0, 2000.0,
		 * 2000.0,
		 *
		 * //0.016, 6.1, 0.001, 0.001, 0.001, 0.01, 0.005, 0.7, 0.011, 0.011,
		 * 0.012, 35.3, 0.4, 0.001, 0.001, 0.001, 5, 5, 0.001, 5, 5, 10.9, 5, 5 };
		 * double choice = RandomNumberGenerator.randomDouble(0, 1);//
		 */
		return range;
	}

	@Override
	public Object clone() {
		return new GMMiProblem(this);
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "Parameter optimization problem for the valine and leucine biosynthesis"
				+ " in C. glutamicum, where 7 reactions are modeled using generalized"
				+ " mass-action kinetics and 3 reactions using Michaelis-Menten kinetics. "
				+ "Only two reactions are considered reversible.";
	}

}
