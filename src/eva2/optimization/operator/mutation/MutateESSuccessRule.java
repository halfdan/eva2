package eva2.optimization.operator.mutation;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * Success rule implementation.
 */
@Description("The 1/5 success rule works only together with an ES optimizer.")
public class MutateESSuccessRule extends MutateESFixedStepSize implements InterfaceMutation, InterfaceAdaptOperatorGenerational, java.io.Serializable {
    protected double successRate = 0.2;
    protected double alpha = 1.2;

    public MutateESSuccessRule() {
    }

    public MutateESSuccessRule(MutateESSuccessRule mutator) {
        super(mutator);
        this.successRate = mutator.successRate;
        this.alpha = mutator.alpha;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESSuccessRule(this);
    }

    /**
     * This method allows you to evaluate whether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESSuccessRule) {
            MutateESSuccessRule mut = (MutateESSuccessRule) mutator;
            if (this.sigma != mut.sigma) {
                return false;
            }
            if (this.successRate != mut.successRate) {
                return false;
            }
            return this.alpha == mut.alpha;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "ES 1/5 Success Rule mutation";
    }

    /**
     * This method increases the mutation step size.
     */
    public void increaseMutationStepSize() {
        this.sigma *= this.alpha;
    }

    /**
     * This method decrease the mutation step size.
     */
    public void decreaseMutationStepSize() {
        this.sigma /= this.alpha;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "ES 1/5 Success Rule mutation";
    }

    public String mutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /**
     * Set success rate (0.2 is default).
     *
     * @param d The mutation operator.
     */
    public void setSuccessRate(double d) {
        if (d < 0) {
            d = 0;
        }
        if (d > 1) {
            d = 1;
        }
        this.successRate = d;
    }

    public double getSuccessRate() {
        return this.successRate;
    }

    public String successRateTipText() {
        return "Set success rate (0.2 is default).";
    }

    /**
     * Choose the factor by which the mutation step size is to be increased/decrease.
     *
     * @param d The mutation operator.
     */
    public void setAlpha(double d) {
        if (d < 1) {
            d = 1;
        }
        this.alpha = d;
    }

    public double getAlpha() {
        return this.alpha;
    }

    public String alphaTipText() {
        return "Choose the factor > 1 by which the mutation step size is to be increased/decreased.";
    }

    @Override
    public String sigmaTipText() {
        return "The initial step size.";
    }

    @Override
    public void adaptAfterSelection(Population oldGen, Population selected) {
        // nothing to do here
    }

    @Override
    public void adaptGenerational(Population selectedPop, Population parentPop, Population newPop, boolean updateSelected) {
        double rate = 0.;
        for (int i = 0; i < parentPop.size(); i++) {
            // calculate success rate
//            System.out.println("new fit / old fit: " + BeanInspector.toString(newPop.getEAIndividual(i).getFitness()) + " , " + BeanInspector.toString(parentPop.getEAIndividual(i).getFitness()));
            if (newPop.getEAIndividual(i).getFitness(0) < parentPop.getEAIndividual(i).getFitness(0)) {
                rate++;
            }
        }
        rate /= parentPop.size();

        if (updateSelected) {
            for (int i = 0; i < selectedPop.size(); i++) { // applied to the old population as well in case of plus strategy
                MutateESSuccessRule mutator = (MutateESSuccessRule) selectedPop.get(i).getMutationOperator();
                updateMutator(rate, mutator);
//            System.out.println("old pop step size " + mutator.getSigma()+ " (" + mutator+ ")");
            }
        }
        for (int i = 0; i < newPop.size(); i++) {
            MutateESSuccessRule mutator = (MutateESSuccessRule) newPop.get(i).getMutationOperator();
            updateMutator(rate, mutator);
//            System.out.println("new pop step size " + mutator.getSigma()+ " (" + mutator+ ")");
        }
    }

    private void updateMutator(double rate, MutateESSuccessRule mutator) {
        if (rate < mutator.getSuccessRate()) {
            mutator.decreaseMutationStepSize();
        } else {
            mutator.increaseMutationStepSize();
        }
    }
}