package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

/**
 * ES mutation with path length control. The step size (single sigma) is
 * adapted using the evolution path length by adapting the real path length
 * to the expected path length in for uncorrelated single steps.
 * See Hansen&Ostermeier 2001, Eqs. 16,17.
 */
public class MutateESPathLengthAdaption implements InterfaceMutation, java.io.Serializable {

    private int dim;
    private double[] randZ;
    private double[] path;
    private double sigmaGlobal = 1.0;
    private double c;
    private boolean usePath = true;
    private double dampening = 1;
    private double expectedPathLen = -1;
    private double cu;

    public MutateESPathLengthAdaption() {

    }

    public MutateESPathLengthAdaption(MutateESPathLengthAdaption mutator) {
        this.usePath = true;
        this.dim = mutator.dim;
        this.sigmaGlobal = mutator.sigmaGlobal;
        this.c = mutator.c;
        this.dampening = mutator.dampening;
        this.expectedPathLen = mutator.expectedPathLen;
        this.cu = mutator.cu;
        if (mutator.randZ != null) {
            this.randZ = (double[]) mutator.randZ.clone();
        }
        if (mutator.path != null) {
            this.path = (double[]) mutator.path.clone();
        }
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESPathLengthAdaption(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESPathLengthAdaption) {
            MutateESPathLengthAdaption mut = (MutateESPathLengthAdaption) mutator;
            // i assume if the C Matrix is equal then the mutation operators are equal
            if (this.dim != mut.dim) {
                return false;
            }
            if (this.sigmaGlobal != mut.sigmaGlobal) {
                return false;
            }
            if (this.c != mut.c) {
                return false;
            }
            if ((this.randZ != null) && (mut.randZ != null)) {
                for (int i = 0; i < this.randZ.length; i++) {
                    if (this.randZ[i] != mut.randZ[i]) {
                        return false;
                    }
                }
            }
            if ((this.path != null) && (mut.path != null)) {
                for (int i = 0; i < this.path.length; i++) {
                    if (this.path[i] != mut.path[i]) {
                        return false;
                    }
                }
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
        if (!(individual instanceof InterfaceESIndividual)) {
            return;
        }
        double[] x = ((InterfaceESIndividual) individual).getDGenotype();
        double[][] ranges = ((InterfaceESIndividual) individual).getDoubleRange();
        this.dim = x.length;
//        if (this.usePath) this.c = Math.sqrt(1.0 / (double) this.dim);

        this.randZ = new double[this.dim];
        this.path = new double[this.dim];
        for (int i = 0; i < this.dim; i++) {
            this.randZ[i] = RNG.gaussianDouble(1.0);
//            this.path[i]=1;
        }

        if (this.usePath) {
            this.c = 4. / (dim + 4);
        } else {
            this.c = 1.0;
        }

        expectedPathLen = Math.sqrt(dim) * (1 - (1. / (4 * dim)) + (1. / (21 * dim * dim)));
        dampening = (1. / c) + 1;
        cu = Math.sqrt(c * (2.0 - c));

        mutateX(x, ranges, true);
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
            double[][] ranges = ((InterfaceESIndividual) individual).getDoubleRange();

            this.adaptStrategy(); // this updates the path using the old step and adapts sigma

            this.calculateNewStep();

            this.mutateX(x, ranges, true); // this performs new mutation

            ((InterfaceESIndividual) individual).setDGenotype(x);
        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
    }

    private void checkRange(double[] x, double[][] ranges) {
        for (int i = 0; i < x.length; i++) {
            if (x[i] < ranges[i][0]) {
                x[i] = ranges[i][0];
            }
            if (x[i] > ranges[i][1]) {
                x[i] = ranges[i][1];
            }
        }
    }

    private void calculateNewStep() {
        for (int i = 0; i < dim; i++) {
            randZ[i] = RNG.gaussianDouble(1.0);
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

    private void adaptStrategy() {
        // remember the path taken. randZ is at this time the last step before selection.
        for (int i = 0; i < dim; i++) {
            path[i] = (1.0 - c) * path[i] + cu * randZ[i];
        }
        double pathLen = Mathematics.norm(path);

//        double expectedPathLen = Math.sqrt(((double)dim)+0.5);
//        double kappa_d          = ((double)dim)/4.0+1.0;

        double exp = (pathLen - expectedPathLen) / (dampening * expectedPathLen);
        sigmaGlobal *= Math.exp(exp);
    }

    private void mutateX(double[] x, double[][] range, boolean checkRange) {
        for (int i = 0; i < x.length; i++) {
            x[i] += sigmaGlobal * randZ[i];
        }
        if (checkRange) {
            checkRange(x, range);
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
        return "Mutation/Path-Length-Control";
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
        return "Mutation/Path-Length-Control";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The single step size is controlled using the evolution path.";
    }

//    /** Use only positive numbers this limits the freedom of effect.
//     * @param bit     The new representation for the inner constants.
//      */
//    public void setUsePath(boolean bit) {
//        this.usePath = bit;
//    }
//    public boolean getUsePath() {
//        return this.usePath;
//    }
//    public String usePathTipText() {
//        return "Use path.";
//    }

    /**
     * This method allows you to set the initial sigma value.
     *
     * @param d The initial sigma value.
     */
    public void setSigmaGlobal(double d) {
        this.sigmaGlobal = d;
    }

    public double getSigmaGlobal() {
        return this.sigmaGlobal;
    }

    public String initSigmaGlobalTipText() {
        return "Set the initial global step size.";
    }
}