package eva2.optimization.operator.mutation;

import eva2.optimization.enums.MutateESCrossoverType;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;


/**
 *
 */
@Description("The global mutation stores only one sigma for all double attributes.")
public class MutateESGlobal implements InterfaceMutation, java.io.Serializable, InterfaceAdditionalPopulationInformer {
    protected double mutationStepSize = 0.2;
    protected double tau1 = 0.15;
    protected double lowerLimitStepSize = 0.0000005;
    protected MutateESCrossoverType crossoverType = MutateESCrossoverType.none;

    public MutateESGlobal() {
    }

    /**
     * Use given mutation step size and no crossover on strategy params.
     *
     * @param mutationStepSize
     */
    public MutateESGlobal(double mutationStepSize) {
        this(mutationStepSize, MutateESCrossoverType.none);
    }

    /**
     * Use given mutation step size and given crossover type on strategy params.
     *
     * @param mutationStepSize
     */
    public MutateESGlobal(double mutationStepSize, MutateESCrossoverType coType) {
        setMutationStepSize(mutationStepSize);
        setCrossoverType(coType);
    }

    public MutateESGlobal(MutateESGlobal mutator) {
        this.mutationStepSize = mutator.mutationStepSize;
        this.tau1 = mutator.tau1;
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
        return new MutateESGlobal(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESGlobal) {
            MutateESGlobal mut = (MutateESGlobal) mutator;
            if (this.mutationStepSize != mut.mutationStepSize) {
                return false;
            }
            if (this.tau1 != mut.tau1) {
                return false;
            }
            return this.lowerLimitStepSize == mut.lowerLimitStepSize;
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
            this.mutationStepSize *= Math.exp(this.tau1 * RNG.gaussianDouble(1));
            if (this.mutationStepSize < this.lowerLimitStepSize) {
                this.mutationStepSize = this.lowerLimitStepSize;
            }
            for (int i = 0; i < x.length; i++) {
                x[i] += ((range[i][1] - range[i][0]) / 2) * RNG.gaussianDouble(this.mutationStepSize);
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
        if (crossoverType != MutateESCrossoverType.none) {
            ArrayList<Double> tmpList = new ArrayList<>();
            if (indy1.getMutationOperator() instanceof MutateESGlobal) {
                tmpList.add(new Double(((MutateESGlobal) indy1.getMutationOperator()).mutationStepSize));
            }
            for (int i = 0; i < partners.size(); i++) {
                if (((AbstractEAIndividual) partners.get(i)).getMutationOperator() instanceof MutateESGlobal) {
                    tmpList.add(new Double(((MutateESGlobal) ((AbstractEAIndividual) partners.get(i)).getMutationOperator()).mutationStepSize));
                }
            }
            double[] list = new double[tmpList.size()];
            for (int i = 0; i < tmpList.size(); i++) {
                list[i] = tmpList.get(i).doubleValue();
            }
            if (list.length <= 1) {
                return;
            }

            switch (this.crossoverType) {
                case intermediate:
                    this.mutationStepSize = 0;
                    for (int i = 0; i < list.length; i++) {
                        this.mutationStepSize += list[i];
                    }
                    this.mutationStepSize /= (double) list.length;
                    break;
                case discrete:
                    this.mutationStepSize = list[RNG.randomInt(0, list.length - 1)];
                    break;
                case none: // do nothing
                    break;
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
        return "ES global mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "ES global mutation";
    }

    /**
     * Set the initial mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setMutationStepSize(double d) {
        if (d < 0) {
            d = this.lowerLimitStepSize;
        }
        this.mutationStepSize = d;
    }

    public double getMutationStepSize() {
        return this.mutationStepSize;
    }

    public String mutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setLowerLimitStepSize(double d) {
        if (d < 0) {
            d = 0;
        }
        this.lowerLimitStepSize = d;
    }

    public double getLowerLimitStepSize() {
        return this.lowerLimitStepSize;
    }

    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    public void setTau1(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau1 = d;
    }

    public double getTau1() {
        return this.tau1;
    }

    public String tau1TipText() {
        return "Set the value for tau1.";
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    public void setCrossoverType(MutateESCrossoverType d) {
        this.crossoverType = d;
    }

    public MutateESCrossoverType getCrossoverType() {
        return this.crossoverType;
    }

    public String crossoverTypeTipText() {
        return "Choose the crossover type for the strategy parameters.";
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
        return new String[]{"The ES global mutation step size."};
    }

    /*
     * (non-Javadoc)
     * @see eva2.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataValue(eva2.optimization.PopulationInterface)
     */
    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        return new Object[]{mutationStepSize};
    }
}
