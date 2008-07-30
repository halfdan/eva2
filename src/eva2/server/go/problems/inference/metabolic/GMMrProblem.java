package eva2.server.go.problems.inference.metabolic;

import eva2.server.go.problems.inference.metabolic.odes.GMMrSystem;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * Created at 2007-02-04.
 *
 * @author Andreas Dr&auml;ger
 */
public class GMMrProblem extends AbstractValineCurveFittingProblem {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -3889975340670999788L;

	public GMMrProblem() {
		super(new GMMrSystem());
	}

	public GMMrProblem(GMMrProblem gmmrproblem) {
		super(gmmrproblem);
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.inference.metabolic.AbstractValineCurveFittingProblem#getParameterRanges()
	 */
	protected double[][] getParameterRanges() {
		int i;
		/*
		 * a // <- params[0..6] 7 b // <- params[7..13] 7 vm // <-
		 * params[14..17] 4 km // <- params[18..21] 4 kia // <- params[22..24] 3
		 * kib // <- params[25..27] 3 ki // <- params[28..30] 3
		 */
		double[][] range = new double[m_ProblemDimension][2];
		double min[] = { 0.000000000, 0.000000000, 0.000000000, 0.000000000,
				0.000000000, 0.000000000, 0.000000000, 0.000000000,
				0.000000000, 0.000000000, 0.000000000, 0.000000000,
				0.000000000, 0.000000000, 0.000000000, 0.000000000,
				0.000000000, 0.000000000, 0.000000000, 0.000000000,
				0.407343614, 0.059890210, 0.008423761, 0.000000000,
				0.000000000, 0.000000000, 0.432428055, 0.000000000,
				0.000000000, 0.000000000, 0.000000000 };
		double max[] = { 16.012590, 10.398386, 14.983890, 9.367657, 16.011989,
				7.229141, 409.395094, 3.348859, 65.532900, 424.407762,
				426.881040, 2.753995, 6.234599, 8.552800, 2.216418, 16.623249,
				1031.701261, 9.238867, 9.110233, 6.765434, 8.693675, 6.798338,
				5.628473, 5.364593, 8.438638, 9.185120, 6.459422, 12.470121,
				426.124248, 335.241456, 638.222487 };
		for (i = 0; i < range.length; i++) {
			// Reaktionen, bei denen CO2 entsteht, sind wahrscheinlich
			// irreversibel.
			// if (i < 18) //((i == 7) || (i == 12))
			range[i][0] = min[i]; // 0;
			/*
			 * else range[i][0] = 1E-8;/
			 */
			if ((21 < i) && (i < 28))
				range[i][1] = max[i]; // 1E+8;
			else
				range[i][1] = max[i]; // 2000;
		}
		return range;
	}


	@Override
	public Object clone() {
		return new GMMrProblem(this);
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "Parameter optimization problem for the valine and leucine biosynthesis"
				+ " in C. glutamicum, where 7 reactions are modeled using generalized"
				+ " mass-action kinetics and 3 reactions using Michaelis-Menten kinetics. "
				+ "Only two reactions are considered irreversible.";
	}

}
