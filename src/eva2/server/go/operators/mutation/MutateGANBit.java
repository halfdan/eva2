package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/**
 * 
 */
public class MutateGANBit implements InterfaceMutation, java.io.Serializable {

    private int numberOfMutations = 1;

    public MutateGANBit() {
    }

    public MutateGANBit(MutateGANBit mutator) {
        this.numberOfMutations = mutator.numberOfMutations;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGANBit(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators are
     * actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGANBit) {
            MutateGANBit mut = (MutateGANBit) mutator;
            if (this.numberOfMutations != mut.numberOfMutations) {
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
     * @param opt The optimization problem.
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
        if (individual instanceof InterfaceGAIndividual) {
            BitSet tmpBitSet = ((InterfaceGAIndividual) individual).getBGenotype();
            int[] mutationIndices = new int[this.numberOfMutations];
            for (int i = 0; i < mutationIndices.length; i++) {
                mutationIndices[i] = RNG.randomInt(0, ((InterfaceGAIndividual) individual).getGenotypeLength());
            };
            // double instances of mutationIndices could be checked here... *sigh*
            for (int i = 0; i < mutationIndices.length; i++) {
                tmpBitSet.flip(mutationIndices[i]);
            }
            ((InterfaceGAIndividual) individual).SetBGenotype(tmpBitSet);
        }
    }

    /**
     * This method allows you to perform either crossover on the strategy
     * parameters or to deal in some other way with the crossover event.
     *
     * @param indy1 The original mother
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
        return "GA n-Bit mutation";
    }

    /**
     * ********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the name to
     * the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GA n-Bit mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Switch n bits of the GA genotype.";
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
        return "The number of bits to be mutated.";
    }
}
