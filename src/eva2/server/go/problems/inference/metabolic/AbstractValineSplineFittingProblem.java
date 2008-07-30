package eva2.server.go.problems.inference.metabolic;

import java.io.Serializable;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.problems.inference.metabolic.odes.AbstractValineSystem;

// Created at 2007-01-23

/**
 * @author Andreas Dr&auml;ger
 */
public abstract class AbstractValineSplineFittingProblem extends
		AbstractValineCurveFittingProblem implements Serializable {

	/**
	 * Default Serial Verison ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 *
	 * @param s
	 */
	public AbstractValineSplineFittingProblem(AbstractValineSystem s) {
		super(s);
	}

	public AbstractValineSplineFittingProblem(GMAKiProblem problem) {
		super(problem);
	}

	@Override
	public void evaluate(AbstractEAIndividual individual) {
		double fitness[] = new double[] { 0 }, result[][] = model(((InterfaceDataTypeDouble) individual)
				.getDoubleData());

		if (result == null) {
			fitness[0] = Double.MAX_VALUE;
		} else {

			if (solver != null)
				if (solver.isUnstable()) {
					fitness[0] += 10000;
				}

			for (int i = 0; i < result.length; i++) { // for all times
				double spline[] = new double[] { // get the spline values at
						// the
						// current time point.
						system.getDHIV(result[i][0]),
						system.getIPM(result[i][0]),
						system.getAcLac(result[i][0]),
						system.getVal(result[i][0]),
						system.getLeu(result[i][0]),
						system.getKIV(result[i][0]),
						system.getKIC(result[i][0]) };
				for (int j = 1; j < result[i].length
						&& (fitness[0] < Double.MAX_VALUE); j++)
					// for all metabolites
					if (spline[j - 1] != 0)
						fitness[0] += Math
								.pow(
										((result[i][j] - spline[j - 1]) / spline[j - 1]),
										2);
					else
						fitness[0] += 10000;
			}
		}

		individual.SetFitness(fitness);
		if (this.best == null) {
			this.best = (AbstractEAIndividual) individual.clone();
		} else if (this.best.getFitness(0) > individual.getFitness(0)) {
			this.best = (AbstractEAIndividual) individual.clone();
		}
	}

}
