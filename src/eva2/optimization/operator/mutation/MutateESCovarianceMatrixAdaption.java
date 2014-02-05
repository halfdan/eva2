package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Jama.EigenvalueDecomposition;
import eva2.tools.math.Jama.Matrix;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.05.2003
 * Time: 17:47:45
 * To change this template use Options | File Templates.
 */

public class MutateESCovarianceMatrixAdaption implements InterfaceMutation, java.io.Serializable {

    protected int D;
    protected double[] Z;
    protected double sigmaGlobal = 1;
    protected double initSigmaScalar = -1;
    protected double c;
    protected double cu;
    protected double cov;
    protected double Beta;
    protected double[] s_N;
    protected double[] pathS;
    protected double[] Bz;
    protected double xi_dach;
    protected Matrix C;
    protected Matrix B;
    protected boolean checkConstraints = false;
    protected int constraintMaxTries = 50;
    protected int counter;
    protected int frequency = 1;
    protected double[] eigenValues;

    public MutateESCovarianceMatrixAdaption() {

    }

    public MutateESCovarianceMatrixAdaption(MutateESCovarianceMatrixAdaption mutator) {
        this.counter = mutator.counter;
        this.frequency = mutator.frequency;
        this.initSigmaScalar = mutator.initSigmaScalar;
        this.constraintMaxTries = mutator.constraintMaxTries;
        this.checkConstraints = mutator.checkConstraints;
        this.D = mutator.D;
        this.sigmaGlobal = mutator.sigmaGlobal;
        this.c = mutator.c;
        this.cu = mutator.cu;
        this.cov = mutator.cov;
        this.Beta = mutator.Beta;
        this.xi_dach = mutator.xi_dach;
        if (mutator.s_N != null) {
            this.s_N = (double[]) mutator.s_N.clone();
        }
        if (mutator.pathS != null) {
            this.pathS = (double[]) mutator.pathS.clone();
        }
        if (mutator.Bz != null) {
            this.Bz = (double[]) mutator.Bz.clone();
        }
        if (mutator.C != null) {
            this.C = (Matrix) mutator.C.clone();
        }
        if (mutator.B != null) {
            this.B = (Matrix) mutator.B.clone();
        }
        if (mutator.Z != null) {
            this.Z = (double[]) mutator.Z.clone();
        }
        if (mutator.eigenValues != null) {
            this.eigenValues = (double[]) mutator.eigenValues.clone();
        }
    }

    /**
     * Constructor allowing to set the range check option.
     *
     * @param doCheckConstraints if true, the range constraints are enforced on mutation.
     */
    public MutateESCovarianceMatrixAdaption(boolean doCheckConstraints) {
        setCheckConstraints(doCheckConstraints);
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESCovarianceMatrixAdaption(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator == this) {
            return true;
        }
        if (mutator instanceof MutateESCovarianceMatrixAdaption) {
            MutateESCovarianceMatrixAdaption mut = (MutateESCovarianceMatrixAdaption) mutator;
            // i assume if the C Matrix is equal then the mutation operators are equal
            try {
                if (this.C == mut.C) {
                    return true;
                }
                double[][] c1 = this.C.getArray();
                double[][] c2 = mut.C.getArray();
                if (c1 == c2) {
                    return true;
                }
                for (int i = 0; i < c1.length; i++) {
                    for (int j = 0; j < c1[i].length; j++) {
                        if (c1[i][j] != c2[i][j]) {
                            return false;
                        }
                    }
                }
            } catch (java.lang.NullPointerException e) {
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
        if (!(individual instanceof InterfaceESIndividual)) {
            return;
        }
        double[] x = ((InterfaceESIndividual) individual).getDGenotype();
        double[][] ranges = ((InterfaceESIndividual) individual).getDoubleRange();

        this.counter = this.frequency;
        if (initSigmaScalar > 0) {
            this.sigmaGlobal = this.initSigmaScalar;
        } else {
            double avgRange = Mathematics.getAvgRange(ranges);
            this.sigmaGlobal = 0.25 * avgRange;
        }
        System.out.println("Init sigma: " + sigmaGlobal);
        this.D = x.length;
        this.C = Matrix.identity(this.D, this.D);
        EigenvalueDecomposition helper = new EigenvalueDecomposition(this.C);
        this.B = helper.getV();
        this.c = Math.sqrt(1.0 / (double) this.D);
        this.cu = Math.sqrt((2.0 - this.c) / this.c);
        this.Beta = this.c;
        this.cov = 2.0 / ((double) this.D * (double) this.D);
        this.Z = new double[this.D];
        this.s_N = new double[this.D];
        this.Bz = new double[this.D];
        this.pathS = new double[this.D];
        for (int i = 0; i < this.D; i++) {
            this.s_N[i] = 0;
            this.Bz[i] = 0;
            this.pathS[i] = 0;
        }
        this.xi_dach = Math.sqrt(this.D - 0.5);
        evaluateNewObjectX(x, ranges);
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
            this.adaptStrategy();
            this.evaluateNewObjectX(x, ranges);
            for (int i = 0; i < x.length; i++) {
                if (x[i] < ranges[i][0]) {
                    x[i] = ranges[i][0];
                }
                if (x[i] > ranges[i][1]) {
                    x[i] = ranges[i][1];
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

    protected void adaptStrategy() {
        double Cij;
        double Bz_d;
        double pathLen = 0.0;
        for (int i = 0; i < this.D; i++) {
            this.s_N[i] = (1.0 - this.c) * this.s_N[i] + this.c * this.cu * this.Bz[i];
        }
//        System.out.println("C bef:\n" + c.toString());
        // ADAPT COVARIANCE
        for (int i = 0; i < this.D; i++) {
            for (int j = i; j < this.D; j++) {
                Cij = (1.0 - this.cov) * this.C.get(i, j) + this.cov * this.s_N[i] * this.s_N[j];
                this.C.set(i, j, Cij);
                this.C.set(j, i, Cij);
            }
        }
//        System.out.println("C aft:\n" + c.toString());
        // ADAPT GLOBAL STEPSIZE
        for (int i = 0; i < this.D; i++) {
            Bz_d = 0.0;
            for (int j = 0; j < this.D; j++) {
                Bz_d += this.B.get(i, j) * this.Z[j];
            }
            this.pathS[i] = (1.0 - this.c) * this.pathS[i] + this.c * this.cu * Bz_d;
            pathLen += this.pathS[i] * this.pathS[i];
        }
        this.sigmaGlobal *= Math.exp(this.Beta * this.c * (Math.sqrt(pathLen) - this.xi_dach));
    }

    protected void evaluateNewObjectX(double[] x, double[][] range) {
        double[] oldX = (double[]) x.clone();

        for (int i = 0; i < this.D; i++) {
            this.Z[i] = RNG.gaussianDouble(1.0);
        }

        this.C = (this.C.plus(this.C.transpose()).times(0.5)); // MAKE C SYMMETRIC
        this.counter++;
        if (this.counter >= this.frequency) {
            EigenvalueDecomposition helper;
            this.counter = 0;
            helper = new EigenvalueDecomposition(this.C);
            this.B = helper.getV();
            this.eigenValues = helper.getRealEigenvalues();

        }
        boolean isNewPosFeasible = false;
        int counter = 0;
        while (!isNewPosFeasible && counter < this.constraintMaxTries) {
            for (int i = 0; i < this.D; i++) {
                this.Bz[i] = 0;
                for (int j = 0; j < this.D; j++) {
                    this.Bz[i] += Math.sqrt(Math.abs(this.eigenValues[j])) * this.B.get(i, j) * this.Z[j];
                }
                x[i] += this.sigmaGlobal * this.Bz[i]; // here is the new value
            }
            isNewPosFeasible = true;
            if (this.checkConstraints == true) {
                for (int i = 0; i < D; i++) {
                    if (x[i] < range[i][0] || x[i] > range[i][1]) {
                        // undo the step and try new Z
                        for (int j = 0; j < this.D; j++) {
                            x[j] = oldX[j] - this.sigmaGlobal * this.Bz[j];
                        }
                        this.sigmaGlobal *= 0.5;
                        isNewPosFeasible = false;
                        counter++;
                        break;
                    }
                }
            }
        }
        if (counter > 0) {
//        	System.out.print("CMA ES Req " + counter + " ");
//        	if (counter > 15) System.out.println(BeanInspector.toString(x));
//        	else System.out.println();
        }
        if (this.checkConstraints && !isNewPosFeasible) { // use force
            Mathematics.projectToRange(x, range);
//        	System.err.println("PROJECTING BY FORCE");
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
        return "CMA mutation";
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
        return "CMA mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is the most sophisticated CMA mutation.";
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

    /**
     * This method allows you to set the initial sigma value.
     *
     * @param d The initial sigma value.
     */
    public void setInitSigmaScalar(double d) {
        this.initSigmaScalar = d;
    }

    public double getInitSigmaScalar() {
        return this.initSigmaScalar;
    }

    public String initSigmaScalarTipText() {
        return "Set the initial sigma value, or -1 to use quarter average range of the problem.";
    }
}
