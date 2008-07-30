package eva2.server.go.problems.inference.metabolic;

import eva2.server.go.problems.inference.metabolic.odes.GMAKiSystem;
import eva2.server.go.strategies.InterfaceOptimizer;

// Created at 2007-02-02

/**
 * @author Andreas Dr&auml;ger
 */
public class GMAKiProblem extends AbstractValineSplineFittingProblem {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -3635573692736142863L;

	/**
	 * Default constructor for simulation of the valine data.
	 */
	public GMAKiProblem() {
		super(new GMAKiSystem());
		m_ProblemDimension = system.getNumberOfParameters();
	}

	public GMAKiProblem(GMAKiProblem gmakiProblem) {
		super(gmakiProblem);
		this.m_ProblemDimension = system.getNumberOfParameters();
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.inference.metabolic.AbstractValineCurveFittingProblem#getParameterRanges()
	 */
	protected double[][] getParameterRanges() {
		int i;
		// set the range
		double[][] range = new double[this.m_ProblemDimension][2];
		for (i = 0; i < 12; i++) {
			range[i][0] = 0;
			range[i][1] = 2000;
		}
		for (i = 12; i < this.m_ProblemDimension; i++) {
			range[i][0] = 0;
			range[i][1] = 8;
		}
		return range;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eva2.server.go.problems.AbstractOptimizationProblem#clone()
	 */
	public Object clone() {
		return new GMAKiProblem(this);
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "Parameter optimization problem for the valine and leucine biosynthesis"
				+ " in C. glutamicum, where all reactions are modeled using generalized"
				+ " mass-action kinetics. Only two reactions are considered irreversible.";
	}

}
