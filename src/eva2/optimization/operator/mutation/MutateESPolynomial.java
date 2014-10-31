package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("The polynomial mutation alters all elements according to a polynomial distribution")
public class MutateESPolynomial implements InterfaceMutation, java.io.Serializable {
    private double eta = 0.2;

    public MutateESPolynomial() {
    }

    public MutateESPolynomial(MutateESPolynomial mutator) {
        this.eta = mutator.eta;
    }

    public MutateESPolynomial(double eta) {
        this.eta = eta;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESPolynomial(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESPolynomial) {
            MutateESPolynomial mut = (MutateESPolynomial) mutator;
            return this.eta == mut.eta;
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

                double r = RNG.randomDouble();
                double delta = 0;
                if (r < 0.5) {
                    delta = Math.pow((2 * r), (1 / (eta + 1))) - 1;
                } else {
                    delta = 1 - Math.pow((2 * (1 - r)), (1 / (eta + 1)));
                }

                x[i] += delta;
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
        return "ES fixed step size mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "ES polynomial mutation";
    }

    /**
     * This method allows you to set the number of crossovers that occur in the
     * genotype.
     *
     * @param a The number of crossovers.
     */
    public void setEta(double a) {
        if (a < 0) {
            a = 0;
        }
        this.eta = a;
    }

    public double getEta() {
        return this.eta;
    }

    public String etaTipText() {
        return "Set the Eta_c value (the larger the value, the more restricted the search).";
    }
}
