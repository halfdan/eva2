package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

import java.util.BitSet;

/**
 *
 */
public class MutateGAInvertBits implements InterfaceMutation, java.io.Serializable {

    private int numberOfMutations = 1;
    private int maxInveredBits = 5;

    public MutateGAInvertBits() {
    }

    public MutateGAInvertBits(MutateGAInvertBits mutator) {
        this.numberOfMutations = mutator.numberOfMutations;
        this.maxInveredBits = mutator.maxInveredBits;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGAInvertBits(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators are
     * actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGAInvertBits) {
            MutateGAInvertBits mut = (MutateGAInvertBits) mutator;
            if (this.numberOfMutations != mut.numberOfMutations) {
                return false;
            }
            if (this.maxInveredBits != mut.maxInveredBits) {
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
        if (individual instanceof InterfaceGAIndividual) {
            BitSet tmpBitSet = ((InterfaceGAIndividual) individual).getBGenotype();
            int[][] mutationIndices = new int[this.numberOfMutations][2];
            for (int i = 0; i < mutationIndices.length; i++) {
                mutationIndices[i][0] = RNG.randomInt(0, ((InterfaceGAIndividual) individual).getGenotypeLength());
                ;
                mutationIndices[i][1] = RNG.randomInt(0, this.maxInveredBits);
                ;
            }
            // ToDo: double instances of mutationIndices could be checked here... *sigh*
            for (int i = 0; i < mutationIndices.length; i++) {
                tmpBitSet.flip(mutationIndices[i][0], Math.min(((InterfaceGAIndividual) individual).getGenotypeLength(), mutationIndices[i][0] + mutationIndices[i][1]));
                if ((mutationIndices[i][0] + mutationIndices[i][1]) > ((InterfaceGAIndividual) individual).getGenotypeLength()) {
                    tmpBitSet.flip(0, (mutationIndices[i][0] + mutationIndices[i][1]) - ((InterfaceGAIndividual) individual).getGenotypeLength());
                }
            }
            ((InterfaceGAIndividual) individual).setBGenotype(tmpBitSet);
        }
    }

    /**
     * This method allows you to perform either crossover on the strategy
     * parameters or to deal in some other way with the crossover event.
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
        return "GA inversion mutation";
    }

    /**
     * These are for GUI
     */

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the name to
     * the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GA invert n bits mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This mutation operator inverts n successive bits.";
    }

    /**
     * This method allows you to set the number of mutations that occur in the
     * genotype.
     *
     * @param mutations The number of mutations.
     */
    public void setNumberOfMutations(int mutations) {
        if (mutations < 0) {
            mutations = 0;
        }
        this.numberOfMutations = mutations;
    }

    public int getNumberOfMutations() {
        return this.numberOfMutations;
    }

    public String numberOfMutationsTipText() {
        return "The number of inversion events.";
    }

    /**
     * This method allows you to set the macimun number if succesively inverted
     * bits
     *
     * @param mutations The number of successively inverted bits.
     */
    public void setMaxInveredBits(int mutations) {
        if (mutations < 0) {
            mutations = 0;
        }
        this.maxInveredBits = mutations;
    }

    public int getMaxInveredBits() {
        return this.maxInveredBits;
    }

    public String maxInveredBitsTipText() {
        return "The number of successive bits to be inverted.";
    }
}