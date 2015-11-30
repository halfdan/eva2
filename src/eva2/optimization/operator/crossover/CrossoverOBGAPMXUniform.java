package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceOBGAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 */
@Description("The infamous PMX uniform crossover for Permutations.")
public class CrossoverOBGAPMXUniform implements InterfaceCrossover, java.io.Serializable {

    public CrossoverOBGAPMXUniform() {

    }

    public CrossoverOBGAPMXUniform(CrossoverOBGAPMXUniform c) {
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverOBGAPMXUniform(this);
    }

    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[partners.size() + 1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i + 1] = (AbstractEAIndividual) partners.get(i).clone();
        }


        if ((indy1 instanceof InterfaceOBGAIndividual) && (partners.get(0) instanceof InterfaceOBGAIndividual)) {
            int[][] pperm1;
            int[][] pperm2;
            pperm1 = new int[((InterfaceOBGAIndividual) result[0]).getOBGenotype().length][];
            pperm2 = new int[((InterfaceOBGAIndividual) result[1]).getOBGenotype().length][];

            for (int i = 0; i < ((InterfaceOBGAIndividual) result[0]).getOBGenotype().length; i++) {
                int[] perm1 = ((InterfaceOBGAIndividual) result[0]).getOBGenotype()[i].clone();
                int[] perm2 = ((InterfaceOBGAIndividual) result[1]).getOBGenotype()[i].clone();

                int crossnumber = RNG.randomInt(1, perm1.length);

                for (int iter = 0; iter < crossnumber; iter++) {
                    int crosspoint = RNG.randomInt(0, perm1.length - 1);
                    int p1inp2 = 0;
                    int p2inp1 = 0;
                    while (perm1[p2inp1] != perm2[crosspoint]) p2inp1++;
                    while (perm2[p1inp2] != perm1[crosspoint]) p1inp2++;
                    perm1[crosspoint] = perm2[crosspoint];
                    perm2[crosspoint] = perm2[p1inp2];
                    perm1[p2inp1] = perm2[crosspoint];
                    perm2[p1inp2] = perm1[crosspoint];
                }
                pperm1[i] = perm1;
                pperm2[i] = perm2;
            }
            ((InterfaceOBGAIndividual) result[0]).setOBGenotype(pperm1);
            ((InterfaceOBGAIndividual) result[1]).setOBGenotype(pperm2);
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) {
            result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        }
        return result;

    }

    /**
     * This method allows you to evaluate wether two crossover operators
     * are actually the same.
     *
     * @param crossover The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        return crossover instanceof CrossoverOBGAPMXUniform;
    }


    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        // nothing to initialize!
    }

    @Override
    public String getStringRepresentation() {
        return this.getName();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "OBGA PMX uniform crossover";
    }

}
