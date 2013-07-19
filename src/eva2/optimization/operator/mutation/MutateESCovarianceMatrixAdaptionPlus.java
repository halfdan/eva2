package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Jama.Matrix;

public class MutateESCovarianceMatrixAdaptionPlus extends
		MutateESCovarianceMatrixAdaption implements InterfaceMutation,
		InterfaceAdaptOperatorGenerational {
	protected double m_psuccess;
	protected double m_cp;
	protected double m_psuccesstarget = 0.44;
	protected double m_stepd;
	protected double m_pthresh;
	protected int m_lambda = 1;

	public MutateESCovarianceMatrixAdaptionPlus() {
		super();
	}

	public MutateESCovarianceMatrixAdaptionPlus(
			MutateESCovarianceMatrixAdaptionPlus mutator) {
		super(mutator);
		m_psuccess = mutator.m_psuccess;
		m_cp = mutator.m_cp;
		m_psuccesstarget = mutator.m_psuccesstarget;
		m_lambda = mutator.m_lambda;
		m_pthresh = mutator.m_pthresh;
		m_stepd = mutator.m_stepd;
	}

	/**
	 * This method will enable you to clone a given mutation operator
	 * 
	 * @return The clone
	 */
    @Override
	public Object clone() {
		return new MutateESCovarianceMatrixAdaptionPlus(this);
	}

	/**
	 * This method allows you to init the mutation operator
	 * 
	 * @param individual
	 *            The individual that will be mutated.
	 * @param opt
	 *            The optimization problem.
	 */
    @Override
	public void init(AbstractEAIndividual individual,
			InterfaceOptimizationProblem opt) {

		if (!(individual instanceof InterfaceESIndividual)) {
                                return;
                            }
		super.init(individual, opt);
		m_psuccesstarget = 1.0 / (5 + Math.sqrt(m_lambda) / 2);
		m_psuccess = m_psuccesstarget;
		m_stepd = 1.0 + m_D / (2.0 * m_lambda);
		m_cp = m_psuccesstarget * m_lambda / (2 + m_psuccesstarget * m_lambda);
		m_c = 2.0 / (2.0 + m_D);
		this.cov = 2.0 / (6.0 + Math.pow(m_D, 2)); // ATTN: differs from the
													// standard CMA-ES
		m_pthresh = 0.44;

	}

	protected void adaptStrategyGen(AbstractEAIndividual child,
			AbstractEAIndividual parent) {
		if (child.getFitness(0) <= parent.getFitness(0)) {
			// updatecov
			updateCovariance(child, parent);
			// updateCovariance();
		}

	}

	/**
	 * @param parent
	 * @param child
	 */
	public void updateCovariance(AbstractEAIndividual child,
			AbstractEAIndividual parent) {
		double[] step = new double[m_D];
		for (int i = 0; i < m_D; i++) {
			step[i] = ((InterfaceESIndividual) parent).getDGenotype()[i]
					- ((InterfaceESIndividual) child).getDGenotype()[i];
		}
		updateCovariance(step);
	}

	public void updateCovariance() {
		double[] step = new double[m_D];
		for (int i = 0; i < m_D; i++) {
			step[i] = Bz[i];
		}
		updateCovariance(step);
	}

	/**
	 * @param step
	 * 
	 */
	public void updateCovariance(double[] step) {
		for (int i = 0; i < m_D; i++) {
			if (m_psuccess < m_pthresh) {
				m_PathS[i] = (1.0 - m_c) * m_PathS[i]
						+ Math.sqrt(m_c * (2.0 - m_c)) * (step[i])
						/ m_SigmaGlobal;
			} else {
				m_PathS[i] = (1.0 - m_c) * m_PathS[i];
			}

		}
		if (m_psuccess < m_pthresh) {
			m_C = m_C.multi((1.0 - cov));
			m_C.plusEquals(Matrix.outer(m_PathS, m_PathS).multi(cov));
		} else {
			m_C = m_C.multi((1.0 - cov)).plus(
					(Matrix.outer(m_PathS, m_PathS).plus(
							m_C.multi(m_c * (2.0 - m_c))).multi(cov)));
		}
	}

    @Override
	protected void adaptStrategy() {
	}

	public int getLambda() {
		return m_lambda;
	}

	public void setLambda(int mLambda) {
		m_lambda = mLambda;
	}

	@Override
	public void crossoverOnStrategyParameters(AbstractEAIndividual indy1,
			Population partners) {
		// ATTN: Crossover is not defined for this

	}

	// @Override
    @Override
	public void adaptAfterSelection(Population oldPop, Population selectedPop) {
		// TODO Auto-generated method stub

	}

	// @Override
    @Override
	public void adaptGenerational(Population selectedPop, Population parentPop,
			Population newPop, boolean updateSelected) {
		double rate = 0.;
		for (int i = 0; i < parentPop.size(); i++) {
			// calculate success rate
			// System.out.println("new fit / old fit: " +
			// BeanInspector.toString(newPop.getEAIndividual(i).getFitness()) +
			// " , " +
			// BeanInspector.toString(parentPop.getEAIndividual(i).getFitness()));
			if (newPop.getEAIndividual(i).getFitness(0) < parentPop
					.getEAIndividual(i).getFitness(0)) {
                        rate++;
                    }
		}
		            rate /= parentPop.size();

		if (updateSelected) {
                                for (int i = 0; i < selectedPop.size(); i++) { // applied to the old
                                                                                                                                // population as
                                                                                                                                // well in case of
                                                                                                                                // plus strategy
                                        MutateESCovarianceMatrixAdaptionPlus mutator = (MutateESCovarianceMatrixAdaptionPlus) ((AbstractEAIndividual) selectedPop
                                                        .get(i)).getMutationOperator();
                                        updateMutator(rate, mutator);
                                        if (selectedPop.getEAIndividual(i).getFitness(0) <= parentPop
                                                        .getEAIndividual(0).getFitness(0)) {
                                                mutator.adaptStrategyGen(selectedPop.getEAIndividual(i),
                                                                parentPop.getEAIndividual(0));
                                        }
                                        // System.out.println("old pop step size " + mutator.getSigma()+
                                        // " (" + mutator+ ")");
                                }
                            }
		for (int i = 0; i < newPop.size(); i++) {
			MutateESCovarianceMatrixAdaptionPlus mutator = (MutateESCovarianceMatrixAdaptionPlus) ((AbstractEAIndividual) newPop
					.get(i)).getMutationOperator();
			updateMutator(rate, mutator);
			if (newPop.getEAIndividual(i).getFitness(0) <= parentPop
					.getEAIndividual(0).getFitness(0)) {
				mutator.adaptStrategyGen(newPop.getEAIndividual(i), parentPop
						.getEAIndividual(0));
			}
			// System.out.println("new pop step size " + mutator.getSigma()+
			// " (" + mutator+ ")");
		}
	}

	private void updateMutator(double rate,
			MutateESCovarianceMatrixAdaptionPlus mutator) {
		mutator.updateStepSize(rate);
	}

	public double getPSuccess() {
		return m_psuccess;
	}

	public void updateStepSize(double psuccess) {
		this.m_psuccess = (1 - m_cp) * m_psuccess + m_cp * psuccess;
		m_SigmaGlobal *= Math.exp(1 / m_stepd * (m_psuccess - m_psuccesstarget)
                                               / (1 - m_psuccesstarget));
	}

    @Override
	public String getName() {
		return "CMA mutation for plus Strategies";
	}

	/**
	 * This method returns a global info string
	 * 
	 * @return description
	 */
	public static String globalInfo() {
		return "This is the CMA mutation according to Igel,Hansen,Roth 2007";
	}

	@Override
	public String getStringRepresentation() {
		// TODO Auto-generated method stub
		return "CMA-plus mutation";
	}

}
