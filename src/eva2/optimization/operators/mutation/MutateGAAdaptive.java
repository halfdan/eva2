package eva2.optimization.operators.mutation;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.populations.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/**
 * The mutation probability is adapted using a parameter tau and stored in the individual.
 * Better mutation probabilities are selected indirectly as they produce better offspring.
 * 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.05.2003
 * Time: 19:52:16
 * To change this template use Options | File Templates.
 */
public class MutateGAAdaptive implements InterfaceMutation, java.io.Serializable {
    
    protected double      m_MutationStep        = 1;
    protected double      m_Tau1                = 0.15;
    protected double      m_LowerLimitStepSize  = 0.0000005;

    public MutateGAAdaptive() {

    }
    public MutateGAAdaptive(MutateGAAdaptive mutator) {
        this.m_MutationStep         = mutator.m_MutationStep;
        this.m_Tau1                 = mutator.m_Tau1;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGAAdaptive(this);
    }
    
    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGAAdaptive) {
            MutateGAAdaptive mut = (MutateGAAdaptive)mutator;
            if (this.m_MutationStep != mut.m_MutationStep) {
                return false;
            }
            if (this.m_Tau1 != mut.m_Tau1) {
                return false;
            }
            if (this.m_LowerLimitStepSize != mut.m_LowerLimitStepSize) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceGAIndividual) {
            BitSet  tmpBitSet   = ((InterfaceGAIndividual)individual).getBGenotype();
            this.m_MutationStep *= Math.exp(this.m_Tau1 * RNG.gaussianDouble(1));
            if (this.m_MutationStep < this.m_LowerLimitStepSize) {
                this.m_MutationStep = this.m_LowerLimitStepSize;
            }
            for (int i = 0; i < ((InterfaceGAIndividual)individual).getGenotypeLength(); i++) {
                if (RNG.flipCoin(this.m_MutationStep/((InterfaceGAIndividual)individual).getGenotypeLength())) {
                    tmpBitSet.flip(i);
                }
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
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GA adaptive mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GA adaptive mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The standard mutation switches n bits of the GA genotype.";
    }

    /** Set the initial mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setMutationStep(double d) {
        if (d < 0) {
            d = this.m_LowerLimitStepSize;
        }
        this.m_MutationStep = d;
    }
    public double getMutationStepSize() {
        return this.m_MutationStep;
    }
    public String mutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /** Set the lower limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setLowerLimitStepSize(double d) {
        if (d < 0) {
            d = 0;
        }
        this.m_LowerLimitStepSize = d;
    }
    public double getLowerLimitStepSize() {
        return this.m_LowerLimitStepSize;
    }
    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }

    /** Set the value for tau1 with this method.
     * @param d   The mutation operator.
     */
    public void setTau1(double d) {
        if (d < 0) {
            d = 0;
        }
        this.m_Tau1 = d;
    }
    public double getTau1() {
        return this.m_Tau1;
    }
    public String tau1TipText() {
        return "Set the value for tau1.";
    }
}
