package javaeva.server.go.operators.mutation;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceGAIndividual;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 03.04.2003
 * Time: 10:03:37
 * To change this template use Options | File Templates.
 */
public class MutateGAStandard implements InterfaceMutation, java.io.Serializable {
    private int         m_NumberOfMutations = 1;

    public MutateGAStandard() {

    }
    public MutateGAStandard(MutateGAStandard mutator) {
        this.m_NumberOfMutations     = mutator.m_NumberOfMutations;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateGAStandard(this);
    }
    
    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGAStandard) {
            MutateGAStandard mut = (MutateGAStandard)mutator;
            if (this.m_NumberOfMutations != mut.m_NumberOfMutations) return false;
            return true;
        } else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceGAIndividual) {
            BitSet  tmpBitSet   = ((InterfaceGAIndividual)individual).getBGenotype();
            int[]   mutationIndices = new int[this.m_NumberOfMutations];
            for (int i = 0; i < mutationIndices.length; i++) mutationIndices[i] = RandomNumberGenerator.randomInt(0, ((InterfaceGAIndividual)individual).getGenotypeLength());;
            // double instances of mutationIndices could be checked here... *sigh*
            for (int i = 0; i < mutationIndices.length; i++) {
                tmpBitSet.flip(mutationIndices[i]);
            }
            ((InterfaceGAIndividual)individual).SetBGenotype(tmpBitSet);
        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "GA standard mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GA standard mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The standard mutation switches n bits of the GA genotype.";
    }

    /** This method allows you to set the number of mutations that occur in the
     * genotype.
     * @param mutations   The number of mutations.
     */
    public void setNumberOfMutations(int mutations) {
        if (mutations < 0) mutations = 0;
        this.m_NumberOfMutations = mutations;
    }
    public int getNumberOfMutations() {
        return this.m_NumberOfMutations;
    }
    public String numberOfMutationsTipText() {
        return "The number of bits to be mutated.";
    }
}
