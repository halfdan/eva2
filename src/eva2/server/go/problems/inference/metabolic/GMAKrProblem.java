package eva2.server.go.problems.inference.metabolic;

import eva2.server.go.problems.InterfaceMultimodalProblem;
import eva2.server.go.problems.inference.metabolic.odes.GMAKrSystem;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * In this problem, the valine/leucine reaction network in C. glutamicum is
 * simulated using a generalized mass-action approach.
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Sep 6, 2007
 */
public class GMAKrProblem extends AbstractValineCurveFittingProblem implements
		InterfaceMultimodalProblem {

	/**
	 * Generated id.
	 */
	private static final long serialVersionUID = 3423204672190772267L;

	/**
	 * Default constructor for simulation of the valine data.
	 */
	public GMAKrProblem() {
		super(new GMAKrSystem());
	}

	public GMAKrProblem(GMAKrProblem gmakrprob) {
		super(gmakrprob);
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.inference.metabolic.AbstractValineCurveFittingProblem#getParameterRanges()
	 */
	protected double[][] getParameterRanges() {
		int i;
		double[][] range = new double[m_ProblemDimension][2];
		for (i = 0; i < 18; i++) {
			range[i][0] = 0;
			range[i][1] = 2000;
		}
		for (i = 18; i < m_ProblemDimension; i++) {
			range[i][0] = 0;
			range[i][1] = 2000;
		}
		return range;
	}

	@Override
	public Object clone() {
		return new GMAKrProblem(this);
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "Parameter optimization problem for the valine and leucine biosynthesis"
				+ " in C. glutamicum, where all reactions are modeled using generalized"
				+ " mass-action kinetics. Only two reactions are considered irreversible.";
	}

}
