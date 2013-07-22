package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Default 1-point-Crossover on InterfaceESIndividual instances.
 * <p/>
 * User: streiche
 * Date: 25.03.2003
 * Time: 11:16:39
 * To change this template use Options | File Templates.
 */
public class CrossoverESDefault implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem m_OptimizationProblem;

    public CrossoverESDefault() {

    }

    public CrossoverESDefault(CrossoverESDefault c) {
        this.m_OptimizationProblem = c.m_OptimizationProblem;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverESDefault(this);
    }

    /**
     * This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceESIndividual, then nothing will happen.
     *
     * @param indy1    The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[] result = null;
        double[][] parents, children;


        result = new AbstractEAIndividual[partners.size() + 1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i + 1] = (AbstractEAIndividual) ((AbstractEAIndividual) partners.get(i)).clone();
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());

        if (partners == null || (partners.size() == 0)) {
            throw new RuntimeException("Error, empty partner population for crossover!");
        }
        if ((indy1 instanceof InterfaceESIndividual) && (partners.get(0) instanceof InterfaceESIndividual)) {
            int crossoverpoint = RNG.randomInt(0, ((InterfaceESIndividual) indy1).getDGenotype().length - 1);
            boolean switcher = RNG.randomBoolean();
            parents = new double[result.length][];
            children = new double[result.length][];
            for (int i = 0; i < result.length; i++) {
                // first clone all data arrays
                parents[i] = new double[((InterfaceESIndividual) result[i]).getDGenotype().length];
                children[i] = new double[parents[i].length];
                System.arraycopy(((InterfaceESIndividual) result[i]).getDGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceESIndividual) result[i]).getDGenotype(), 0, children[i], 0, parents[i].length);
            }

            for (int i = 0; i < children[0].length; i++) {
                if ((i < crossoverpoint) ^ (switcher)) {
                    // do nothing
                } else {
                    // exchange circular among the parents
                    for (int j = 0; j < children.length - 1; j++) {
                        children[j][i] = parents[j + 1][i];
                    }
                    children[children.length - 1][i] = parents[0][i];
                }
            }
            // write the result back
            for (int i = 0; i < result.length; i++) {
                ((InterfaceESIndividual) result[i]).SetDGenotype(children[i]);
            }
        }
        //in case the crossover was successful lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) {
            result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        }
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
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
        if (crossover instanceof CrossoverESDefault) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.m_OptimizationProblem = opt;
    }

    @Override
    public String getStringRepresentation() {
        return this.getName();
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
        return "ES discrete one-point crossover";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a discrete one-point crossover between m ES individuals.";
    }
}
