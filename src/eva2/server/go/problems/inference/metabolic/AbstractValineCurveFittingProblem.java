package eva2.server.go.problems.inference.metabolic;

import java.io.Serializable;
import java.util.LinkedList;

import wsi.ra.math.RNG;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.inference.des.RKSolver;
import eva2.server.go.problems.inference.metabolic.odes.AbstractValineSystem;

/**
 * Super class for many simulation problems of the valine and leucine reaction
 * network in C. glutamicum.
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Sep 6, 2007
 */
public abstract class AbstractValineCurveFittingProblem extends
		AbstractCurveFittingProblem implements Serializable {

	/**
	 * Start values for: DHIV, IPM, AcLac, Val, Leu, KIV, KIC in this order.
	 */
	protected final double[] y = { 0.132, 0.0227, 0.236, 29.4, 0.209, 13.1,
			0.0741 };

	protected AbstractValineSystem system;

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = -194867821991525140L;

	public AbstractValineCurveFittingProblem(
			AbstractValineCurveFittingProblem avcfp) {
		if (avcfp.m_Template != null)
			this.m_Template = (AbstractEAIndividual) ((AbstractEAIndividual) avcfp.m_Template)
					.clone();
		this.system = avcfp.system;
		this.m_ProblemDimension = this.system.getNumberOfParameters();
	}

	/**
	 * Default constructor for simulation of the valine data.
	 */
	public AbstractValineCurveFittingProblem(AbstractValineSystem system) {
		this.solver = new RKSolver(.01);
		this.system = system;
		this.m_ProblemDimension = system.getNumberOfParameters();
		solver.setWithLinearCalc(false);
		m_Template = new ESIndividualDoubleData();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eva2.server.go.OptimizationProblems.AbstractOptimizationProblem#initProblem()
	 */
	public void initProblem() {
		this.best = null;
		this.metabolites = new LinkedList<String>();

		this.timeSeries = new TimeSeries();

		// this.metabolites.add("PYR"); // 46
		// this.metabolites.add("AKG"); // 47
		// this.metabolites.add("ALA"); // 47
		// this.metabolites.add("NAD"); // 47
		this.metabolites.add("DHIV"); // 44
		// this.metabolites.add("nadp"); // 45
		// this.metabolites.add("Glut"); // 47
		this.metabolites.add("2IPM"); // 45
		this.metabolites.add("AcLac"); // 40
		this.metabolites.add("Val"); // 46
		this.metabolites.add("Leu"); // 47
		this.metabolites.add("KIV"); // 47
		this.metabolites.add("KIC"); // 47

		this.t0 = -3.894;
		this.t1 = 20.643;

		this.xOffSet = RNG.gaussianDouble(0.1);
		this.yOffSet = RNG.gaussianDouble(0.1);
		this.noise = RNG.gaussianDouble(0.1);
		initRange = new double[m_ProblemDimension][2];
		for (int i = 0; i < initRange.length; i++) {
			initRange[i][0] = 0;
			initRange[i][1] = 2;
		}
		this.SetInitRange(initRange);
	}

	public void initPopulation(Population population) {
		int i;
		AbstractEAIndividual tmpIndy;

		best = null;
		population.clear();

		((InterfaceDataTypeDouble) m_Template)
				.setDoubleDataLength(initRange.length);

		// set the range
		double[][] range = getParameterRanges();
		((InterfaceDataTypeDouble) m_Template).SetDoubleRange(range);

		// for (i = 0; i < population.getPopulationSize(); i++) {
		Population tmpPop = new Population();
		if (population.getPopulationSize() < 250)
			tmpPop.setPopulationSize(250);
		else
			tmpPop.setPopulationSize(population.getPopulationSize());
		for (i = 0; i < tmpPop.getPopulationSize(); i++) {
			tmpIndy = (AbstractEAIndividual) ((AbstractEAIndividual) m_Template)
					.clone();
			// tmpIndy.init(this);

			// ////// B E G I N N ///////////////
			int j;
			double params[] = new double[m_ProblemDimension];

			for (j = 0; j < params.length; j++) {
				if (monteCarlo)
					params[j] = RNG.randomDouble(range[j][0], range[j][1]);
				else
					params[j] = RNG.gaussianDouble(1) + 1;
				if (params[j] < range[j][0]) {
					params[j] = range[j][0];
				}
				if (params[j] > range[j][1]) {
					params[j] = range[j][1];
				}
			}

			tmpIndy.initByValue(params, this);
			// ////////E N D E /////////////////

			tmpPop.add(tmpIndy);
			// population.add(tmpIndy);

			// /////// B E G I N N //////////////
			((InterfaceDataTypeDouble) tmpIndy).SetDoublePhenotype(params);
			tmpIndy.resetConstraintViolation();
			params = ((InterfaceDataTypeDouble) tmpIndy).getDoubleData();
			for (j = 0; j < params.length; j++)
				if ((params[j] < ((InterfaceDataTypeDouble) m_Template)
						.getDoubleRange()[j][0])
						|| (params[j] > ((InterfaceDataTypeDouble) m_Template)
								.getDoubleRange()[j][1]))
					System.out.println("Verletzung!\t" + params[j]);// */
			if (tmpIndy.violatesConstraint())
				System.out.println("Constraint violation: "
						+ tmpIndy.getConstraintViolation());
			// ////// E N D E /////////////////
		}
		tmpPop.init();

		for (i = 0; i < population.getPopulationSize(); i++)
			population.add(tmpPop.remove(tmpPop.getIndexOfBestIndividual()));
		population.init();
	}

	/**
	 * This method returns a matrix whose number of lines equals the number of
	 * parameters and which has two columns. In each line of this matrix the
	 * lower and the upper bound for the corresponding parameter are defined.
	 *
	 * @return
	 */
	protected abstract double[][] getParameterRanges();

	/*
	 * (non-Javadoc)
	 *
	 * @see simulation.AbstractCurveFittingProblem#model(double[])
	 */
	public double[][] model(double[] x) {
		try {
			system.setParameters(x);
			return solver.solveAtTimePointsIncludingTime(system, y,
					this.timeSeries.getTimes());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
