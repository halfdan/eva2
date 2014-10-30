package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;


/**
 * Mutate an integer individual by shifting a connected subsequence within the genotype. The sequence
 * length is chosen uniformly randomly up to an upper limit. The destination position may either be
 * fully randomly or also limited to a maximal distance.
 */
@Description("This mutation translocates a segment of the int[].")
public class MutateGITranslocate implements InterfaceMutation, java.io.Serializable {

    int maxLengthOfTranslocate = 4;
    int maxTransLocDistance = -1;

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
        this.maxLengthOfTranslocate = mutator.maxLengthOfTranslocate;
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
            return this.maxLengthOfTranslocate == mut.maxLengthOfTranslocate;
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
     * doesn't implement InterfaceGIIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceGIIndividual) {
            int[] x = ((InterfaceGIIndividual) individual).getIGenotype();
            int from, to, length;
            length = RNG.randomInt(1, this.maxLengthOfTranslocate);
            if (x.length < length + 2) {
                return;
            }
            from = RNG.randomInt(0, x.length - 1 - length);
            if (maxTransLocDistance <= 0) {
                to = RNG.randomInt(0, x.length - 1 - length);
            } else {
                int minTo = Math.max(0, from - maxTransLocDistance);
                int maxTo = Math.min(x.length - 1 - length, from + maxTransLocDistance);
//    			System.out.println("min/max-to: " + minTo + ", " + maxTo);
                to = RNG.randomInt(minTo, maxTo);
//    			System.out.println("to is " + to);
            }
//    		this.printInt("####\nBefore ", x);
            int[] tmp = new int[x.length];
            int[] without = new int[x.length - length];
            int[] insert = new int[length];
            System.arraycopy(x, 0 + from, insert, 0, length);
            for (int i = 0; i < without.length; i++) {
                if (i < from) {
                    without[i] = x[i];
                } else {
                    without[i] = x[i + length];
                }
            }
            System.arraycopy(without, 0, tmp, 0, to);
            System.arraycopy(insert, to - to, tmp, to, to + length - to);
            System.arraycopy(without, to + length - length, tmp, to + length, x.length - (to + length));
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
     * This method allows you to set the max length of invert.
     *
     * @param n The max length of invert
     */
    public void setMaxLengthOfTranslocate(int n) {
        this.maxLengthOfTranslocate = n;
    }

    public int getMaxLengthOfTranslocate() {
        return this.maxLengthOfTranslocate;
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
        this.maxTransLocDistance = n;
    }

    public int getMaxTranslocationDist() {
        return this.maxTransLocDistance;
    }

    public String maxTranslocationDistTipText() {
        return "Gives the maximum distance by which a segment may be translocated.";
    }
}