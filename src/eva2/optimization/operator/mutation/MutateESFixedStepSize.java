package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.09.2004
 * Time: 17:05:23
 * To change this template use File | Settings | File Templates.
 */
public class MutateESFixedStepSize implements InterfaceMutation, java.io.Serializable {
    protected double sigma = 0.005;

    public MutateESFixedStepSize() {
    }

    public MutateESFixedStepSize(MutateESFixedStepSize mutator) {
        this.sigma = mutator.sigma;
    }

    public MutateESFixedStepSize(double sigma) {
        this.sigma = sigma;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESFixedStepSize(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESFixedStepSize) {
            MutateESFixedStepSize mut = (MutateESFixedStepSize) mutator;
            return this.sigma == mut.sigma;
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
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual) individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual) individual).getDoubleRange();
            for (int i = 0; i < x.length; i++) {
                x[i] += ((range[i][1] - range[i][0]) / 2) * RNG.gaussianDouble(this.sigma);
                if (range[i][0] > x[i]) {
                    x[i] = range[i][0];
                }
                if (range[i][1] < x[i]) {
                    x[i] = range[i][1];
                }
            }
            ((InterfaceESIndividual) individual).setDGenotype(x);

        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
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
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "ES fixed step size mutation " + getSigma();
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
        return "ES fixed step size mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The fixed step size mutation alters all elements with a fixed mutation step size.";
    }

    /**
     * Set the value for Sigma with this method.
     *
     * @param d The mutation operator.
     */
    public void setSigma(double d) {
        if (d < 0) {
            d = 0;
        }
        this.sigma = d;
    }

    public double getSigma() {
        return this.sigma;
    }

    public String sigmaTipText() {
        return "Set the value for the fixed sigma.";
    }
}
