package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 */
@Description("This mutation operator inserts or adds elements to the array.")
public class MutateGIInsertDelete implements InterfaceMutation, java.io.Serializable {

    int maxLengthOfInsDel = 2;

    public MutateGIInsertDelete() {

    }

    public MutateGIInsertDelete(MutateGIInsertDelete mutator) {
        this.maxLengthOfInsDel = mutator.maxLengthOfInsDel;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGIInsertDelete();
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGIInsertDelete) {
            MutateGIInsertDelete mut = (MutateGIInsertDelete) mutator;
            return this.maxLengthOfInsDel == mut.maxLengthOfInsDel;
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
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceGIIndividual) {
            int[] x = ((InterfaceGIIndividual) individual).getIGenotype();
            int[][] range = ((InterfaceGIIndividual) individual).getIntRange();
            int[] newX;
            int[][] newRange;
            int length, position;
            //this.pintInt("Before ", x);
            length = RNG.randomInt(1, this.maxLengthOfInsDel);
            boolean insert = RNG.flipCoin(0.5);
            if ((!insert) && (length >= x.length - 1)) {
                insert = true;
            }
            if (insert) {
                // insert
                position = RNG.randomInt(0, x.length - 1);
                newX = new int[x.length + length];
                newRange = new int[range.length + length][2];
                for (int i = 0; i < position; i++) {
                    newX[i] = x[i];
                    newRange[i] = range[i];
                }
                for (int i = position; i < position + length; i++) {
                    newX[i] = RNG.randomInt(range[position][0], range[position][1]);
                    newRange[i][0] = range[position][0];
                    newRange[i][1] = range[position][1];
                }
                for (int i = position; i < x.length; i++) {
                    newX[i + length] = x[i];
                    newRange[i + length] = range[i];
                }
            } else {
                // delete
                position = RNG.randomInt(0, x.length - 1 - length);
                newX = new int[x.length - length];
                newRange = new int[range.length - length][2];
                if (newX.length <= 1) {
                    return;
                }
                for (int i = 0; i < position; i++) {
                    newX[i] = x[i];
                    newRange[i] = range[i];
                }
                for (int i = position + length; i < x.length; i++) {
                    newX[i - length] = x[i];
                    newRange[i - length] = range[i];
                }
            }
            if (newX.length <= 1) {
                System.out.println("newX " + newX.length);
            }
            ((InterfaceGIIndividual) individual).setIntegerDataLength(newX.length);
            ((InterfaceGIIndividual) individual).setIGenotype(newX);
            ((InterfaceGIIndividual) individual).setIntRange(newRange);
            newX = ((InterfaceGIIndividual) individual).getIGenotype();
            if (newX.length <= 1) {
                System.out.println("newX " + newX.length);
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
        return "GI insert/delete mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GI insert/delete mutation";
    }

    /**
     * This method allows you to set the max length of the insert or deletion.
     *
     * @param n The max length of invert
     */
    public void setMaxLengthOfInsDel(int n) {
        this.maxLengthOfInsDel = n;
    }

    public int getMaxLengthOfInsDel() {
        return this.maxLengthOfInsDel;
    }

    public String maxLengthOfInsDelTipText() {
        return "Gives the maximum length of an inserted or deleted segment.";
    }
}