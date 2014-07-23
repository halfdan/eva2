package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 *
 */
public class MutateESMutativeStepSizeControl implements InterfaceMutation, java.io.Serializable {
    protected double mutationStepSize = 0.2;
    protected double alpha = 1.2;
    protected double lowerLimitStepSize = 0.0000005;
    protected double upperLimitStepSize = 0.4;

    public MutateESMutativeStepSizeControl() {
    }

    public MutateESMutativeStepSizeControl(double initialStepSize, double lowerLimit, double upperLimit) {
        mutationStepSize = initialStepSize;
        if (lowerLimitStepSize > upperLimitStepSize) {
            System.err.println("Invalid step size bounds, switching upper and lower...");
            double tmp = upperLimit;
            upperLimit = lowerLimit;
            lowerLimit = tmp;
        }
        lowerLimitStepSize = lowerLimit;
        upperLimitStepSize = upperLimit;
        if (initialStepSize < lowerLimit || initialStepSize > upperLimit) {
            mutationStepSize = (upperLimit + lowerLimit) / 2.;
            System.err.println("Invalid initial stepsize, setting it to " + mutationStepSize);
        }
    }

    public MutateESMutativeStepSizeControl(MutateESMutativeStepSizeControl mutator) {
        this.mutationStepSize = mutator.mutationStepSize;
        this.alpha = mutator.alpha;
        this.lowerLimitStepSize = mutator.lowerLimitStepSize;
        this.upperLimitStepSize = mutator.upperLimitStepSize;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESMutativeStepSizeControl(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESMutativeStepSizeControl) {
            MutateESMutativeStepSizeControl mut = (MutateESMutativeStepSizeControl) mutator;
            if (this.mutationStepSize != mut.mutationStepSize) {
                return false;
            }
            if (this.alpha != mut.alpha) {
                return false;
            }
            if (this.lowerLimitStepSize != mut.lowerLimitStepSize) {
                return false;
            }
            if (this.upperLimitStepSize != mut.upperLimitStepSize) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to init the mutation operator
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
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual) individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual) individual).getDoubleRange();
            if (RNG.flipCoin(0.5)) {
                this.mutationStepSize *= this.alpha;
            } else {
                this.mutationStepSize /= this.alpha;
            }
            if (this.mutationStepSize < this.lowerLimitStepSize) {
                this.mutationStepSize = this.lowerLimitStepSize;
            }
            if (this.mutationStepSize > this.upperLimitStepSize) {
                this.mutationStepSize = this.upperLimitStepSize;
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
            // System.out.println("new step size: " + mutationStepSize);
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
        return "ES mutative step size control";
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
        return "MSR";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The mutative step size control mutation randomly increases/decreases the current step size.";
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
        this.lowerLimitStepSize = d;
    }

    public double getLowerLimitStepSize() {
        return this.lowerLimitStepSize;
    }

    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }

    /**
     * Set the upper limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setUpperLimitStepSize(double d) {
        this.upperLimitStepSize = d;
    }

    public double getUpperLimitStepSize() {
        return this.upperLimitStepSize;
    }

    public String upperLimitStepSizeTipText() {
        return "Set the upper limit for the mutation step size.";
    }

    /**
     * Set the value for Alpha with this method.
     *
     * @param d The mutation operator.
     */
    public void setAlpha(double d) {
        if (d < 0) {
            d = 0;
        }
        this.alpha = d;
    }

    public double getAlpha() {
        return this.alpha;
    }

    public String alphaTipText() {
        return "Set the value for alpha.";
    }
}
