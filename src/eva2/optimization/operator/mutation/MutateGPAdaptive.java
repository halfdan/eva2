package eva2.optimization.operator.mutation;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.individuals.InterfaceGPIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.06.2003
 * Time: 16:52:48
 * To change this template use Options | File Templates.
 */
public class MutateGPAdaptive implements InterfaceMutation, java.io.Serializable {
    protected double mutationStep = 1;
    protected double tau1 = 0.15;
    protected double tau2 = 0.15;
    protected double lowerLimitStepSize = 0.0000005;

    public MutateGPAdaptive() {

    }

    public MutateGPAdaptive(MutateGPAdaptive mutator) {
        this.mutationStep = mutator.mutationStep;
        this.tau1 = mutator.tau1;
        this.tau2 = mutator.tau2;
        this.lowerLimitStepSize = mutator.lowerLimitStepSize;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGPAdaptive(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGPAdaptive) {
            MutateGPAdaptive mut = (MutateGPAdaptive) mutator;
            if (this.mutationStep != mut.mutationStep) {
                return false;
            }
            if (this.tau1 != mut.tau1) {
                return false;
            }
            if (this.tau2 != mut.tau2) {
                return false;
            }
            if (this.lowerLimitStepSize != mut.lowerLimitStepSize) {
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
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceGPIndividual) {
            this.mutationStep *= Math.exp(this.tau1 * RNG.gaussianDouble(1) + this.tau2 * RNG.gaussianDouble(1));
            if (this.mutationStep < this.lowerLimitStepSize) {
                this.mutationStep = this.lowerLimitStepSize;
            }
            if (this.mutationStep > 1) {
                this.mutationStep = 1;
            }
            if (RNG.flipCoin(this.mutationStep)) {
                ((IndividualInterface) individual).defaultMutate();
            }
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
        // nothing to do here
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GP adaptive mutation";
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
    public String getName() {
        return "GP adaptive mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This mutation replaces a random node with a new random subtree.";
    }

    /**
     * Set the initial mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setMutationStep(double d) {
        if (d < 0) {
            d = this.lowerLimitStepSize;
        }
        this.mutationStep = d;
    }

    public double getMutationStepSize() {
        return this.mutationStep;
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
     * Set the value for tau2 with this method.
     *
     * @param d The mutation operator.
     */
    public void setTau2(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau2 = d;
    }

    public double getTau2() {
        return this.tau2;
    }

    public String tau2TipText() {
        return "Set the value for tau2.";
    }
}
