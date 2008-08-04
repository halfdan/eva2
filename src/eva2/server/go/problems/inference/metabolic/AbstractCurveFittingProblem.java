package eva2.server.go.problems.inference.metabolic;

import java.io.Serializable;
import java.util.LinkedList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.InterfaceHasInitRange;
import eva2.tools.Mathematics;
import eva2.tools.des.RKSolver;

/**
 * This class provides a collection of methods to compare two curves one against
 * the other and to optimize the parameters of a model to be fitted as close as
 * possible to given measurements.
 *
 * @author Andreas Dr&auml;ger
 * @date Sept. 18 2006
 */
public abstract class AbstractCurveFittingProblem extends
		AbstractOptimizationProblem implements InterfaceHasInitRange,
		Serializable {

	protected AbstractEAIndividual best = null;

	protected LinkedList<String> metabolites;

	protected TimeSeries timeSeries;

	protected double xOffSet, noise, yOffSet;

	protected int m_ProblemDimension;

	protected double t0, t1;

	protected RKSolver solver;

	protected boolean monteCarlo = false;

	protected double[][] initRange;

	public AbstractCurveFittingProblem() {
		m_Template = new ESIndividualDoubleData();
	}

	public void setIndividualTemplate(AbstractEAIndividual indy) {
		m_Template = indy;
	}

	/**
	 * This method allows you to swich to an initialization that is completely
	 * by chance instead of a certain more sophisticated initialization
	 * procedure.
	 *
	 * @param mc
	 *            If true, a Monte Carlo initialization will be performed. Else
	 *            another possibly more sophisticated procedure will be started.
	 */
	public void setMonteCarloInitialization(boolean mc) {
		this.monteCarlo = mc;
	}

	/**
	 * @param individual
	 */
	public void evaluate(AbstractEAIndividual individual) {
		double fitness[] = this
				.doEvaluation(((InterfaceDataTypeDouble) individual)
						.getDoubleData());
		individual.SetFitness(fitness);
		if (this.best == null) {
			this.best = (AbstractEAIndividual) individual.clone();
		} else if (this.best.getFitness(0) > individual.getFitness(0)) {
			this.best = (AbstractEAIndividual) individual.clone();
		}
	}

	/**
	 * @param x
	 * @return
	 */
	public double[] doEvaluation(double[] x) {
		double freturn[] = new double[1], fitness = 0;
		double[][] result = model(x);

		if (result == null) {
			freturn[0] = Double.MAX_VALUE;
			return freturn;
		}

		freturn[0] = 0;
		if (this.solver != null)
			if (this.solver.isUnstable()) {
				freturn[0] += 10000;
			}

		// for all metabolites
		for (int i = 0; (i < metabolites.size())
				&& (freturn[0] < Double.MAX_VALUE); i++)
			try {
				/*
				 * Metabolites, which have not been considered in the
				 * measurements should also be able to be simulated. They
				 * shouldn't contribute to the overall fitness.
				 */
				if (!timeSeries.containsMetabolite(metabolites.get(i)
						.toString())
						|| (timeSeries.numberOfMeasurements(metabolites.get(i)
								.toString()) == 0))
					continue;

				double[] times = this.timeSeries.getTimes(this.metabolites.get(
						i).toString()), values = this.timeSeries
						.getValues(this.metabolites.get(i).toString()), mvalue = new double[times.length];
				// model
				// values

				// tests:
				// boolean name = false;
				// for all measured times for the current metabolite
				for (int j = 0, min = 0, max = result.length - 1; j < times.length; j++) {
					/*
					 * This happens, if the parameters are that bad that the
					 * model function doesn't even produce sensefull values.
					 */
					if (result[result.length - 1][0] < times[j]) {
						mvalue[j] = Math.max(10000, 10000 * values[j]);
						continue;
					}

					if (times[j] != result[j][0]) {
						/*
						 * Seek the right time value from the model with an
						 * approach in logarithmic time. The variables min and
						 * max have been defined as an additional start
						 * condition of the for loop.
						 */
						int t = Math.round((max + min) / 2), t_old = t;
						do {
							t_old = t;
							if (result[t][0] < times[j]) {
								if (t < max)
									min = t + 1;
								else
									min = t;
							} else if (result[t][0] > times[j]) {
								if (t > min)
									max = t - 1;
								else
									max = t;
							} else
								break;
							t = Math.round((max + min) / 2);
						} while (t != t_old);
						/*
						 * Set min and max for the search of the next time
						 * (measured times are assumed to be ordered).
						 */
						min = t;
						max = result.length - 1;

						/*
						 * The model value for the i-th metabolite at time j is
						 * given by result[t][i+1] because the first column of
						 * result is the time.
						 */
						mvalue[j] = result[t][i + 1];

						/*
						 * These steps are necessary if the resulting model
						 * contains more than one value for one time step. In
						 * this case for the fitness the model value with the
						 * greatest distance to the measured value should be
						 * considered.
						 */
						int up = t, down = t;
						while (up < result.length - 1)
							if (result[up][0] == result[up + 1][0])
								up++;
							else
								break;
						while (down > 0)
							if (result[down][0] == result[down - 1][0])
								down--;
							else
								break;
						if (up != down)
							for (int k = down; k < up; k++)
								if (Math.abs(result[k][i + 1] - values[j]) > Math
										.abs(mvalue[j] - values[j]))
									mvalue[j] = result[k][i + 1];

						if ((result[t][0] > times[j]) && (t > 0)) {
							if (result[t - 1][0] < times[j]) {
								/*
								 * if (!name) {
								 * System.out.println(metabolites.get(i)+":");
								 * name=true; } System.out.println(
								 * result[t-1][0]+" = result["+(t-1)+"][0] <
								 * times["+j+"] = "+times[j]+" & "
								 * +result[t][0]+" = result["+t+"][0] >
								 * times["+j+"] = "+times[j]);//
								 */
								mvalue[j] = Mathematics.linearInterpolation(
										times[j], result[t - 1][0],
										result[t][0], result[t - 1][i + 1],
										mvalue[j]);
							} // otherwise we don't want to interpolate with
							// the value before
							// down.
						} else if ((result[t][0] < times[j])
								&& (t < result.length - 1)) {
							if (result[t + 1][0] > times[j]) {
								mvalue[j] = Mathematics.linearInterpolation(
										times[j], result[t][0],
										result[t + 1][0], mvalue[j],
										result[t + 1][i + 1]);
							} /*
								 * we don't want to interpolate with the value
								 * after up. This would propably smooth the
								 * error function.
								 */
						}
						/*
						 * We are lucky: the model already contains this time
						 * point. So we just need to use the given model value.
						 */
					} else {
						mvalue[j] = result[j][i + 1];
					}

					/*
					 * This also happens with bad parameter values. Give a heavy
					 * weight to the current metabolite.
					 */
					if ((mvalue[j] == Double.NaN) || (mvalue[j] < 0))
						mvalue[j] = Math.max(10000, 10000 * values[j]);
				} // end for all times

				// average distance over all times
				/*
				 * fitness = Mathematics.dist(mvalue, values, 2)/times.length;
				 * freturn[0] += fitness;//
				 */

				// fitness = Mathematics.dist(mvalue, values, 2);
				fitness = Mathematics.relDist(mvalue, values, 10000);
				System.out.println(metabolites.get(i) + "\t" + fitness);

				if (!Double.isNaN(fitness) && !Double.isInfinite(fitness)) {
					freturn[0] += fitness;
				} else
					freturn[0] += 99999999999999999.; // TODO this should be
				// managed
				// differently?

			} catch (Exception exc) {
				exc.printStackTrace();
			}
		if (Double.isInfinite(freturn[0])
				|| (freturn[0] > 99999999999999999999.))
			freturn[0] = 99999999999999999999.;
		return freturn;
	}

	/**
	 * This method returns the best individual of the complete optimization
	 * process, which is not neccessarily identical to the best individual of
	 * the final population.
	 *
	 * @return best The best Individual of the complete optimization process
	 */
	public AbstractEAIndividual getOverallBestIndividual() {
		return this.best;
	}

	/**
	 * Computes the differential equation describing the decrease or increase of
	 * the current metabolite in the metabolic network.
	 *
	 * @param x
	 *            The current parameters of the differential equation describing
	 *            the metabolic network.
	 * @return
	 */
	public abstract double[][] model(double[] x);


	public int getProblemDimension() {
		return m_ProblemDimension;
	}

	public double[][] getInitRange() {
		return initRange;
	}

	public void SetInitRange(double[][] initRange) {
		this.initRange = initRange;
	}

}
