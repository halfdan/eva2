package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This is a discrete n-point crossover between m ES individuals.")
public class CrossoverESNPointDiscrete implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem optimizationProblem;
    private int numberOfCrossovers = 3;

    public CrossoverESNPointDiscrete() {

    }

    public CrossoverESNPointDiscrete(CrossoverESNPointDiscrete mutator) {
        this.optimizationProblem = mutator.optimizationProblem;
        this.numberOfCrossovers = mutator.numberOfCrossovers;
    }

    /**
     * This method will enable you to clone a given crossover operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverESNPointDiscrete(this);
    }

    /**
     * This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
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

        if ((indy1 instanceof InterfaceESIndividual) && (partners.get(0) instanceof InterfaceESIndividual)) {
            int length = ((InterfaceESIndividual) result[0]).getDGenotype().length;
            int mixer = RNG.randomInt(0, partners.size());
            int[] crossoverPoints = new int[this.numberOfCrossovers];
            parents = new double[partners.size() + 1][];
            children = new double[partners.size() + 1][];
            for (int i = 0; i < result.length; i++) {
                parents[i] = new double[((InterfaceESIndividual) result[i]).getDGenotype().length];
                children[i] = new double[parents[i].length];
                System.arraycopy(((InterfaceESIndividual) result[i]).getDGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceESIndividual) result[i]).getDGenotype(), 0, children[i], 0, parents[i].length);
            }
            for (int i = 0; i < this.numberOfCrossovers; i++) {
                crossoverPoints[i] = RNG.randomInt(0, length - 1);
            }
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < this.numberOfCrossovers; j++) {
                    if (i == crossoverPoints[j]) {
                        mixer++;
                    }
                }
                for (int j = 0; j < children.length; j++) {
                    children[j][i] = parents[(j + mixer) % parents.length][i];
                }
            }

            // write the result back
            for (int i = 0; i < result.length; i++) {
                ((InterfaceESIndividual) result[i]).setDGenotype(children[i]);
            }
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
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
        if (crossover instanceof CrossoverESNPointDiscrete) {
            CrossoverESNPointDiscrete cross = (CrossoverESNPointDiscrete) crossover;
            return this.numberOfCrossovers == cross.numberOfCrossovers;
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
        this.optimizationProblem = opt;
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
        return "ES discrete n-point crossover";
    }

    /**
     * This method allows you to set the number of crossovers that occur in the
     * genotype.
     *
     * @param crossovers The number of crossovers.
     */
    public void setNumberOfCrossovers(int crossovers) {
        if (crossovers < 0) {
            crossovers = 0;
        }
        this.numberOfCrossovers = crossovers;
    }

    public int getNumberOfCrossovers() {
        return this.numberOfCrossovers;
    }

    public String numberOfCrossoversTipText() {
        return "The number of crossoverpoints.";
    }
}
