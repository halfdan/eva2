package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Jama.Matrix;

public class MutateESCovarianceMatrixAdaptionPlus extends
        MutateESCovarianceMatrixAdaption implements InterfaceMutation,
        InterfaceAdaptOperatorGenerational {
    protected double psuccess;
    protected double cp;
    protected double psuccesstarget = 0.44;
    protected double stepd;
    protected double pthresh;
    protected int lambda = 1;

    public MutateESCovarianceMatrixAdaptionPlus() {
        super();
    }

    public MutateESCovarianceMatrixAdaptionPlus(
            MutateESCovarianceMatrixAdaptionPlus mutator) {
        super(mutator);
        psuccess = mutator.psuccess;
        cp = mutator.cp;
        psuccesstarget = mutator.psuccesstarget;
        lambda = mutator.lambda;
        pthresh = mutator.pthresh;
        stepd = mutator.stepd;
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
     * This method allows you to initialize the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual,
                     InterfaceOptimizationProblem opt) {

        if (!(individual instanceof InterfaceESIndividual)) {
            return;
        }
        super.init(individual, opt);
        psuccesstarget = 1.0 / (5 + Math.sqrt(lambda) / 2);
        psuccess = psuccesstarget;
        stepd = 1.0 + D / (2.0 * lambda);
        cp = psuccesstarget * lambda / (2 + psuccesstarget * lambda);
        c = 2.0 / (2.0 + D);
        this.cov = 2.0 / (6.0 + Math.pow(D, 2)); // ATTN: differs from the
        // standard CMA-ES
        pthresh = 0.44;

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
        double[] step = new double[D];
        for (int i = 0; i < D; i++) {
            step[i] = ((InterfaceESIndividual) parent).getDGenotype()[i]
                    - ((InterfaceESIndividual) child).getDGenotype()[i];
        }
        updateCovariance(step);
    }

    public void updateCovariance() {
        double[] step = new double[D];
        System.arraycopy(Bz, 0, step, 0, D);
        updateCovariance(step);
    }

    /**
     * @param step
     */
    public void updateCovariance(double[] step) {
        for (int i = 0; i < D; i++) {
            if (psuccess < pthresh) {
                pathS[i] = (1.0 - c) * pathS[i]
                        + Math.sqrt(c * (2.0 - c)) * (step[i])
                        / sigmaGlobal;
            } else {
                pathS[i] = (1.0 - c) * pathS[i];
            }

        }
        if (psuccess < pthresh) {
            C = C.multi((1.0 - cov));
            C.plusEquals(Matrix.outer(pathS, pathS).multi(cov));
        } else {
            C = C.multi((1.0 - cov)).plus(
                    (Matrix.outer(pathS, pathS).plus(
                            C.multi(c * (2.0 - c))).multi(cov)));
        }
    }

    @Override
    protected void adaptStrategy() {
    }

    public int getLambda() {
        return lambda;
    }

    public void setLambda(int mLambda) {
        lambda = mLambda;
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
        return psuccess;
    }

    public void updateStepSize(double psuccess) {
        this.psuccess = (1 - cp) * this.psuccess + cp * psuccess;
        sigmaGlobal *= Math.exp(1 / stepd * (this.psuccess - psuccesstarget)
                / (1 - psuccesstarget));
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
