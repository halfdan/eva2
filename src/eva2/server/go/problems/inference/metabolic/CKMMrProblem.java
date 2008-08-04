package eva2.server.go.problems.inference.metabolic;

import eva2.server.go.problems.InterfaceMultimodalProblem;
import eva2.server.go.problems.inference.metabolic.odes.CKMMrSystem;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * This class is a problem of the valine/leucine biosynthesis in C. glutamicum
 * and uses a combination of Michaelis-Menten and Convenience Kinetic, both
 * reversible.
 *
 * @date 2007-02-05
 * @author Andreas Dr&auml;ger
 */
public class CKMMrProblem extends AbstractValineCurveFittingProblem implements
		InterfaceMultimodalProblem {

	/**
	 *
	 */
	private static final long serialVersionUID = -3120557413810402464L;

	public CKMMrProblem() {
		super(new CKMMrSystem());
	}

	public CKMMrProblem(CKMMrProblem ckmmrprob) {
		super(ckmmrprob);
	}

	public String getName() {
		return "CKMMrProblem";
	}
	
	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.inference.metabolic.AbstractValineCurveFittingProblem#getParameterRanges()
	 */
	public double[][] getParameterRanges() {
		int i;

		// double[] kcat = null, // <- params[0..6]
		// bcat = null, // <- params[7..13]
		// km = null, // <- params[14..43]
		// ki = null, // <- params[44..46]
		// vm = null, // <- params[47..50]
		// kia = null, // <- params[51..53]
		// kib = null; // <params[54..56]
		// double acCoA = 0, // <- params[57]
		// coA = 0; // <- params[58]

		// set the range
		double[][] range = new double[m_ProblemDimension][2];
		for (i = 0; i < range.length; i++) {
			// Reaktionen, bei denen CO2 entsteht, sind wahrscheinlich
			// irreversibel.
			if ((i < 14) || ((43 < i) && (i < 51)))
				range[i][0] = 0;
			else
				range[i][0] = 1E-8;
			if ((i > 50) && (i < 57))
				range[i][1] = 1E+8;
			else
				range[i][1] = 2000;
		}
		return range;
	}

	@Override
	public Object clone() {
		return new CKMMrProblem(this);
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "Parameter optimization problem for the valine and leucine biosynthesis"
				+ " in C. glutamicum, where 7 reactions are modeled using convenience kinetics"
				+ " and 3 reactions with Michaelis Menten kinetics. Only two reactions"
				+ " are considered irreversible.";
	}
}
