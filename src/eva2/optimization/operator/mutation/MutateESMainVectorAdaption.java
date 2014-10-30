package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This is the most sophisticated MVA mutation.")
public class MutateESMainVectorAdaption implements InterfaceMutation, java.io.Serializable {

    private boolean checkConstraints = true;
    static public final String Name = "MVA";
    private int N;
    private double[] Z;
    private double sigmaScalar = 1.0;
    private double initSigmaScalar = 1.0;
    private double c;
    private double cov;
    private double beta;
    private double[] sN;
    private double[] dN;
    private double[] mainV;
    private double xi_dach;
    private double Z1;
    private double w_v = 3.0;

    public MutateESMainVectorAdaption() {

    }

    public MutateESMainVectorAdaption(MutateESMainVectorAdaption mutator) {
        this.N = mutator.N;
        this.sigmaScalar = mutator.sigmaScalar;
        this.initSigmaScalar = mutator.initSigmaScalar;
        this.c = mutator.c;
        this.cov = mutator.cov;
        this.beta = mutator.beta;
        this.xi_dach = mutator.xi_dach;
        this.Z1 = mutator.Z1;
        this.w_v = mutator.w_v;
        if (mutator.mainV != null) {
            this.mainV = mutator.mainV.clone();
        }
        if (mutator.Z != null) {
            this.Z = mutator.Z.clone();
        }
        if (mutator.sN != null) {
            this.sN = mutator.sN.clone();
        }
        if (mutator.dN != null) {
            this.dN = mutator.dN.clone();
        }
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESMainVectorAdaption(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (this == mutator) {
            return true;
        }
        if (mutator instanceof MutateESMainVectorAdaption) {
            MutateESMainVectorAdaption mut = (MutateESMainVectorAdaption) mutator;
            // i assume if the main_V is equal then the mutation operators are equal
            if (this.mainV == mut.mainV) {
                return true;
            }
            if (this.mainV != null) {
                for (int i = 0; i < this.mainV.length; i++) {
                    if (this.mainV[i] != mut.mainV[i]) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
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
        if (!(individual instanceof InterfaceESIndividual)) {
            return;
        }
        double[] x = ((InterfaceESIndividual) individual).getDGenotype();
        double[][] ranges = ((InterfaceESIndividual) individual).getDoubleRange();
        this.sigmaScalar = this.initSigmaScalar;
        this.N = x.length;
        this.c = Math.sqrt(1.0 / (double) this.N); // Sigma-Path-Constant
        this.beta = this.c;
        this.cov = 2.0 / ((double) this.N * (double) this.N);
        this.Z = new double[this.N];
        this.sN = new double[this.N];
        this.dN = new double[this.N];
        this.mainV = new double[this.N];
        for (int i = 0; i < this.N; i++) {
            this.sN[i] = 0;
            this.dN[i] = 0;
            this.mainV[i] = 0;
        }
        this.xi_dach = Math.sqrt(this.N - 0.5);
        for (int i = 0; i < this.N; i++) {
            this.Z[i] = RNG.gaussianDouble(1.0);
        }
        this.Z1 = RNG.gaussianDouble(1.0);
        evaluateNewObjectX(x, ranges);
    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceESIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual) individual).getDGenotype();
            double[][] ranges = ((InterfaceESIndividual) individual).getDoubleRange();
            this.adaptStrategy();
            for (int i = 0; i < N; i++) {
                Z[i] = RNG.gaussianDouble(1.0);
            }
            Z1 = RNG.gaussianDouble(1.0);
            evaluateNewObjectX(x, ranges);

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

    private void adaptStrategy() {
        double length = 0.0;
        for (int i = 0; i < this.N; i++) {
            this.dN[i] = (1.0 - this.c) * this.dN[i] + Math.sqrt(this.c * (2.0 - this.c)) * this.Z[i]; // PATH
            length += this.dN[i] * this.dN[i];
        }
        this.sigmaScalar *= Math.exp(this.beta * this.c * (Math.sqrt(length) - this.xi_dach));
        // SIGN OF SCALAR PRODUCT
        double Product = 0.0;
        for (int i = 0; i < this.N; i++) {
            Product += this.sN[i] * this.mainV[i];
        }
        if (Product < 0.0) {
            Product = -1.0;
        } else {
            Product = 1.0;
        }
        for (int i = 0; i < this.N; i++) { // ADAPT MAIN VECTOR
            this.sN[i] = (1.0 - this.c) * this.sN[i] + Math.sqrt(this.c * (2.0 - this.c)) * (this.Z[i] + this.Z1 * this.w_v * this.mainV[i]); // PATH MAIN VECTOR
            this.mainV[i] = (1.0 - this.cov) * Product * this.mainV[i] + this.cov * this.sN[i];
        }
    }

    private void evaluateNewObjectX(double[] x, double[][] range) {
        if (Double.isNaN((x[0]))) {
            System.err.println("treffer in mva " + x[0]);
        }

//        boolean constraint = false;
//        int counter = 0;
//        while (constraint == false) {
//            for (int i = 0; i < x.length; i++) x[i] = x[i] + sigmaScalar * (Z[i] + Z1 * w_v * mainV[i]);
//            constraint = true;
//            if (counter++ > 30)  break;
//            if (checkConstraints == true) {
//                for (int i = 0; i < x.length; i++) {
//                    if (x[i] < range[i][0]) x[i] = range[i][0];
//                    if (x[i] > range[i][1]) x[i] = range[i][1];
//
//                }
//            }
//        }
        double[] v = this.mainV.clone();
        double[] grad = new double[x.length];
        double vl = 0;
        double gradl = 0;
        for (int i = 0; i < x.length; i++) {
            grad[i] = 2 * x[i];
            vl += v[i] * v[i];
            gradl += grad[i] * grad[i];
        }
        vl = Math.sqrt(vl);
        gradl = Math.sqrt(gradl);
        for (int i = 0; i < x.length; i++) {
            v[i] /= vl;
            grad[i] /= gradl;
            x[i] += this.sigmaScalar * (this.Z[i] + this.Z1 * this.w_v * this.mainV[i]);
        }
        if (getCheckConstraints()) { // MK: lets actually do a constraint check
            Mathematics.projectToRange(x, range);
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
        return "MVA mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "MVA mutation";
    }

    /**
     * Use only positive numbers this limits the freedom of effect.
     *
     * @param bit The new representation for the inner constants.
     */
    public void setCheckConstraints(boolean bit) {
        this.checkConstraints = bit;
    }

    public boolean getCheckConstraints() {
        return this.checkConstraints;
    }

    public String checkConstraintsTipText() {
        return "Toggle check constraints.";
    }
}