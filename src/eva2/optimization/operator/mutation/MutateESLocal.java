package eva2.optimization.operator.mutation;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.util.ArrayList;

/**
 *
 */
@Description("The local mutation stores n sigmas for each double attribute.")
public class MutateESLocal implements InterfaceMutation, InterfaceAdditionalPopulationInformer, java.io.Serializable {

    public enum CrossoverType { None, Intermediate, Discrete }

    protected double mutationStepSize = 0.2;
    protected double tau1 = 0.15;
    protected double lowerLimitStepSize = 0.0000005;
    private double[] sigmas;
    protected double tau2 = 0.15;
    protected CrossoverType crossoverType;

    public MutateESLocal() {
        this.sigmas = null;
        this.crossoverType = CrossoverType.None;
    }

    public MutateESLocal(MutateESLocal mutator) {
        if (mutator.sigmas != null) {
            this.sigmas = new double[mutator.sigmas.length];
            System.arraycopy(mutator.sigmas, 0, this.sigmas, 0, this.sigmas.length);
        }
        this.mutationStepSize = mutator.mutationStepSize;
        this.tau1 = mutator.tau1;
        this.tau2 = mutator.tau2;
        this.lowerLimitStepSize = mutator.lowerLimitStepSize;
        this.crossoverType = mutator.crossoverType;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESLocal(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator == this) {
            return true;
        }
        if (mutator instanceof MutateESLocal) {
            MutateESLocal mut = (MutateESLocal) mutator;
            if (this.tau1 != mut.tau1) {
                return false;
            }
            if (this.tau2 != mut.tau2) {
                return false;
            }
            if (this.lowerLimitStepSize != mut.lowerLimitStepSize) {
                return false;
            }
            if (this.sigmas != null) {
                for (int i = 0; i < this.sigmas.length; i++) {
                    if (this.sigmas[i] != mut.sigmas[i]) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to initialize the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void initialize(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        if (individual instanceof InterfaceESIndividual) {
            // initialize the Sigmas
            this.sigmas = new double[((InterfaceESIndividual) individual).getDGenotype().length];
            for (int i = 0; i < this.sigmas.length; i++) {
                this.sigmas[i] = this.mutationStepSize;
            }
        }
    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceESIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());

        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual) individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual) individual).getDoubleRange();
            double tmpR = RNG.gaussianDouble(1);

            for (int i = 0; i < x.length; i++) {
                this.sigmas[i] *= Math.exp(this.tau1 * tmpR + this.tau2 * RNG.gaussianDouble(1));
                if (this.sigmas[i] < this.lowerLimitStepSize) {
                    this.sigmas[i] = this.lowerLimitStepSize;
                }
                x[i] += ((range[i][1] - range[i][0]) / 2) * RNG.gaussianDouble(this.sigmas[i]);
                if (range[i][0] > x[i]) {
                    x[i] = range[i][0];
                }
                if (range[i][1] < x[i]) {
                    x[i] = range[i][1];
                }
            }
            ((InterfaceESIndividual) individual).setDGenotype(x);
        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
    }

    /**
     * This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     *
     * @param indy1    The original mother
     * @param partners The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        ArrayList tmpListA = new ArrayList();
        ArrayList tmpListB = new ArrayList();
        if (indy1.getMutationOperator() instanceof MutateESLocal) {
            tmpListA.add(((MutateESLocal) indy1.getMutationOperator()).mutationStepSize);
            tmpListB.add(((MutateESLocal) indy1.getMutationOperator()).sigmas);
        }
        for (int i = 0; i < partners.size(); i++) {
            if (((AbstractEAIndividual) partners.get(i)).getMutationOperator() instanceof MutateESLocal) {
                tmpListA.add(((MutateESLocal) ((AbstractEAIndividual) partners.get(i)).getMutationOperator()).mutationStepSize);
                tmpListB.add(((MutateESLocal) ((AbstractEAIndividual) partners.get(i)).getMutationOperator()).sigmas);
            }
        }
        double[] listA = new double[tmpListA.size()];
        double[][] listB = new double[tmpListA.size()][];
        for (int i = 0; i < tmpListA.size(); i++) {
            listA[i] = (Double) tmpListA.get(i);
            listB[i] = (double[]) tmpListB.get(i);
        }
        if (listA.length <= 1) {
            return;
        }
        switch (this.crossoverType) {
            case Intermediate: {
                this.mutationStepSize = 0;
                for (int i = 0; i < this.sigmas.length; i++) {
                    this.sigmas[i] = 0;
                }
                for (int i = 0; i < listA.length; i++) {
                    this.mutationStepSize += listA[i];
                    for (int j = 0; j < this.sigmas.length; j++) {
                        this.sigmas[j] += listB[i][j];
                    }
                }
                this.mutationStepSize /= (double) listA.length;
                for (int i = 0; i < this.sigmas.length; i++) {
                    this.sigmas[i] /= (double) listA.length;
                }
                break;
            }
            case Discrete: {
                int rn = RNG.randomInt(0, listA.length - 1);
                this.mutationStepSize = listA[rn];
                for (int i = 0; i < this.sigmas.length; i++) {
                    rn = RNG.randomInt(0, listA.length - 1);
                    this.sigmas[i] = listB[rn][i];
                }
                break;
            }
            default: {
                // do nothing
            }
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
        return "ES local mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "ES local mutation";
    }

    /**
     * Set the initial mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Choose the initial mutation step size sigma.")
    public void setMutationStepSize(double d) {
        if (d < 0) {
            d = this.lowerLimitStepSize;
        }
        this.mutationStepSize = d;
    }

    public double getMutationStepSize() {
        return this.mutationStepSize;
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Set the lower limit for the mutation step size.")
    public void setLowerLimitStepSize(double d) {
        if (d < 0) {
            d = 0;
        }
        this.lowerLimitStepSize = d;
    }

    public double getLowerLimitStepSize() {
        return this.lowerLimitStepSize;
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Set the value for tau1.")
    public void setTau1(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau1 = d;
    }

    public double getTau1() {
        return this.tau1;
    }

    /**
     * Set the value for tau2 with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Set the value for tau2.")
    public void setTau2(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau2 = d;
    }

    public double getTau2() {
        return this.tau2;
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Choose the crossover type for the strategy parameters.")
    public void setCrossoverType(CrossoverType d) {
        this.crossoverType = d;
    }

    public CrossoverType getCrossoverType() {
        return this.crossoverType;
    }

    /*
     * (non-Javadoc)
     * @see eva2.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataHeader()
     */
    @Override
    public String[] getAdditionalDataHeader() {
        return new String[]{"sigma"};
    }

    /*
     * (non-Javadoc)
     * @see eva2.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataInfo()
     */
    @Override
    public String[] getAdditionalDataInfo() {
        return new String[]{"The ES local mutation step sizes."};
    }

    /*
     * (non-Javadoc)
     * @see eva2.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataValue(eva2.optimization.PopulationInterface)
     */
    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        return new Object[]{sigmas};
    }
}
