package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;


/**
 * Mutate an integer individual by shifting a connected subsequence within the genotype. The sequence
 * length is chosen uniformly randomly up to an upper limit. The destination position may either be
 * fully randomly or also limited to a maximal distance.
 * <p/>
 * User: mkron, streiche
 */
public class MutateGITranslocate implements InterfaceMutation, java.io.Serializable {

    int m_MaxLengthOfTranslocate = 4;
    int m_maxTransLocDistance = -1;

    public MutateGITranslocate() {
    }

    public MutateGITranslocate(int maxTranslLen) {
        this();
        setMaxLengthOfTranslocate(maxTranslLen);
    }

    public MutateGITranslocate(int maxTranslLen, int maxTransDist) {
        this();
        setMaxLengthOfTranslocate(maxTranslLen);
        setMaxTranslocationDist(maxTransDist);
    }

    public MutateGITranslocate(MutateGITranslocate mutator) {
        this.m_MaxLengthOfTranslocate = mutator.m_MaxLengthOfTranslocate;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGITranslocate();
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGITranslocate) {
            MutateGITranslocate mut = (MutateGITranslocate) mutator;
            if (this.m_MaxLengthOfTranslocate != mut.m_MaxLengthOfTranslocate) {
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
     * doesn't implement InterfaceGIIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceGIIndividual) {
            int[] x = ((InterfaceGIIndividual) individual).getIGenotype();
            int from, to, length;
            length = RNG.randomInt(1, this.m_MaxLengthOfTranslocate);
            if (x.length < length + 2) {
                return;
            }
            from = RNG.randomInt(0, x.length - 1 - length);
            if (m_maxTransLocDistance <= 0) {
                to = RNG.randomInt(0, x.length - 1 - length);
            } else {
                int minTo = Math.max(0, from - m_maxTransLocDistance);
                int maxTo = Math.min(x.length - 1 - length, from + m_maxTransLocDistance);
//    			System.out.println("min/max-to: " + minTo + ", " + maxTo);
                to = RNG.randomInt(minTo, maxTo);
//    			System.out.println("to is " + to);
            }
//    		this.printInt("####\nBefore ", x);
            int[] tmp = new int[x.length];
            int[] without = new int[x.length - length];
            int[] insert = new int[length];
            for (int i = 0; i < length; i++) {
                insert[i] = x[i + from];
            }
            for (int i = 0; i < without.length; i++) {
                if (i < from) {
                    without[i] = x[i];
                } else {
                    without[i] = x[i + length];
                }
            }
            for (int i = 0; i < to; i++) {
                tmp[i] = without[i];
            }
            for (int i = to; i < to + length; i++) {
                tmp[i] = insert[i - to];
            }
            for (int i = to + length; i < x.length; i++) {
                tmp[i] = without[i - length];
            }
//    		System.out.println(""+from+"/"+to+"/"+length);
//    		this.printInt("After  ", tmp);
            ((InterfaceGIIndividual) individual).setIGenotype(tmp);
        }
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

//    private void printInt(String s, int[] x) {
//        String tmp = "{"+x[0];
//        for (int i = 1; i < x.length; i++) tmp += ", "+x[i];
//        System.out.println(s+tmp+"}");
//    }
//
//    public static void main(String args[]) {
//    	GIIndividualIntegerData indy = new GIIndividualIntegerData();
//    	indy.defaultInit(new I1Problem());
//    	indy.setMutationProbability(1);
//    	indy.setMutationOperator(new MutateGITranslocate(5,2));
//    	System.out.println(indy.getStringRepresentation());
//    	for (int i=1; i<100; i++) indy.mutate();
//    	System.out.println(indy.getStringRepresentation());
//    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GI translocation mutation";
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
        return "GI translocation mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This mutation translocates a segment of the int[].";
    }

    /**
     * This method allows you to set the max length of invert.
     *
     * @param n The max length of invert
     */
    public void setMaxLengthOfTranslocate(int n) {
        this.m_MaxLengthOfTranslocate = n;
    }

    public int getMaxLengthOfTranslocate() {
        return this.m_MaxLengthOfTranslocate;
    }

    public String maxLengthOfTranslocateTipText() {
        return "Gives the maximum length of the translocated segment.";
    }

    /**
     * This method allows you to set the max length of invert.
     *
     * @param n The max length of invert
     */
    public void setMaxTranslocationDist(int n) {
        this.m_maxTransLocDistance = n;
    }

    public int getMaxTranslocationDist() {
        return this.m_maxTransLocDistance;
    }

    public String maxTranslocationDistTipText() {
        return "Gives the maximum distance by which a segment may be translocated.";
    }
}