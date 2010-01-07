package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 26.03.2004
 * Time: 11:18:36
 * To change this template use File | Settings | File Templates.
 */
public class MutateESMainVectorAdaption implements InterfaceMutation, java.io.Serializable  {

    private boolean             m_CheckConstraints  = true;
    static public final String  Name                = "MVA";
    private int                 N;
    private double[]            m_Z;
    private double              m_SigmaScalar       = 1.0;
    private double              m_InitSigmaScalar   = 1.0;
    private double              m_c;
    private double              m_cov;
    private double              m_Beta;
    private double[]            s_N;
    private double[]            s_d_N;
    private double[]            m_main_v;
    private double              xi_dach;
    private double              Z1;
    private double              w_v                 = 3.0;

    public MutateESMainVectorAdaption() {

    }
    public MutateESMainVectorAdaption(MutateESMainVectorAdaption mutator) {
        this.N              = mutator.N;
        this.m_SigmaScalar  = mutator.m_SigmaScalar;
        this.m_InitSigmaScalar = mutator.m_InitSigmaScalar;
        this.m_c            = mutator.m_c;
        this.m_cov          = mutator.m_cov;
        this.m_Beta         = mutator.m_Beta;
        this.xi_dach        = mutator.xi_dach;
        this.Z1             = mutator.Z1;
        this.w_v            = mutator.w_v;
        if (mutator.m_main_v != null)   this.m_main_v   = (double[]) mutator.m_main_v.clone();
        if (mutator.m_Z != null)        this.m_Z        = (double[]) mutator.m_Z.clone();
        if (mutator.s_N != null)        this.s_N        = (double[]) mutator.s_N.clone();
        if (mutator.s_d_N != null)      this.s_d_N      = (double[]) mutator.s_d_N.clone();
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESMainVectorAdaption(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
    	if (this==mutator) return true;
        if (mutator instanceof MutateESMainVectorAdaption) {
            MutateESMainVectorAdaption mut = (MutateESMainVectorAdaption)mutator;
            // i assume if the main_V is equal then the mutation operators are equal
            if (this.m_main_v==mut.m_main_v) return true;
            if (this.m_main_v != null) {
                for (int i = 0; i < this.m_main_v.length; i++) {
                    if (this.m_main_v[i] != mut.m_main_v[i]) return false;
                }
            } else return false;
            return true;
        }
        else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        if (!(individual instanceof InterfaceESIndividual)) return;
        double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
        double[][]  ranges  = ((InterfaceESIndividual)individual).getDoubleRange();
        this.m_SigmaScalar  = this.m_InitSigmaScalar;
        this.N              = x.length;
        this.m_c            = Math.sqrt(1.0 / (double) this.N); // Sigma-Path-Constant
        this.m_Beta         = this. m_c;
        this.m_cov          = 2.0 / ( (double) this.N * (double) this.N);
        this.m_Z            = new double[this.N];
        this.s_N            = new double[this.N];
        this.s_d_N          = new double[this.N];
        this.m_main_v       = new double[this.N];
        for (int i = 0; i < this.N; i++) {
            this.s_N[i]         = 0;
            this.s_d_N[i]       = 0;
            this.m_main_v[i]    = 0;
        };
        this.xi_dach    = Math.sqrt(this.N - 0.5);
        for (int i = 0; i < this.N; i++) this.m_Z[i] = RNG.gaussianDouble(1.0);
        this.Z1 = RNG.gaussianDouble(1.0);
        evaluateNewObjectX(x, ranges);
    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceESIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
            double[][]  ranges  = ((InterfaceESIndividual)individual).getDoubleRange();
            this.adaptStrategy();
            for (int i = 0; i < N; i++) m_Z[i] = RNG.gaussianDouble(1.0);
            Z1 = RNG.gaussianDouble(1.0);
            evaluateNewObjectX(x, ranges);

            ((InterfaceESIndividual)individual).SetDGenotype(x);
        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    private void adaptStrategy() {
        double length = 0.0;
        for (int i = 0; i < this.N; i++) {
            this.s_d_N[i]   = (1.0 - this.m_c) * this.s_d_N[i] + Math.sqrt(this.m_c*(2.0-this.m_c)) * this.m_Z[i]; // PATH
            length          = length + this.s_d_N[i] * this.s_d_N[i];
        }
        this.m_SigmaScalar = this.m_SigmaScalar * Math.exp(this.m_Beta * this.m_c * (Math.sqrt(length) - this.xi_dach));
        // SIGN OF SCALAR PRODUCT
        double Product = 0.0;
        for (int i = 0; i < this.N; i++) Product = Product + this.s_N[i] * this.m_main_v[i];
        if (Product < 0.0)  Product = -1.0;
        else                Product = 1.0;
        for (int i = 0; i < this.N; i++) { // ADAPT MAIN VECTOR
            this.s_N[i]       = (1.0 - this.m_c) * this.s_N[i] + Math.sqrt(this.m_c*(2.0-this.m_c)) * (this.m_Z[i] + this.Z1 * this.w_v * this.m_main_v[i]); // PATH MAIN VECTOR
            this.m_main_v[i]  = (1.0 - this.m_cov) * Product * this.m_main_v[i] + this.m_cov * this.s_N[i];
        }
    }

    private void evaluateNewObjectX(double[] x, double[][] range) {
        if (Double.isNaN((x[0]))) System.err.println("treffer in mva "+ x[0]);

//        boolean constraint = false;
//        int counter = 0;
//        while (constraint == false) {
//            for (int i = 0; i < x.length; i++) x[i] = x[i] + m_SigmaScalar * (m_Z[i] + Z1 * w_v * m_main_v[i]);
//            constraint = true;
//            if (counter++ > 30)  break;
//            if (m_CheckConstraints == true) {
//                for (int i = 0; i < x.length; i++) {
//                    if (x[i] < range[i][0]) x[i] = range[i][0];
//                    if (x[i] > range[i][1]) x[i] = range[i][1];
//
//                }
//            }
//        }
        double[]    v       =(double[])this.m_main_v.clone();
        double[]    grad    = new double[x.length];
        double      vl      = 0;
        double      gradl   = 0;
        for (int i=0;i < x.length; i++) {
            grad[i] = 2*x[i];
            vl      = vl + v[i]*v[i];
            gradl   = gradl + grad[i]*grad[i];
        }
        vl      = Math.sqrt(vl);
        gradl   = Math.sqrt(gradl);
        for (int i = 0; i < x.length; i++) {
            v[i] = v[i] / vl; grad[i] = grad[i] / gradl;
            x[i] = x[i] + this.m_SigmaScalar * (this.m_Z[i] + this.Z1 * this.w_v * this.m_main_v[i]);
        }
        if (getCheckConstraints()) { // MK: lets actually do a constraint check
        	Mathematics.projectToRange(x, range);
        }
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "MVA mutation";
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "MVA mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is the most sophisticated MVA mutation.";
    }

    /** Use only positive numbers this limits the freedom of effect.
     * @param bit     The new representation for the inner constants.
      */
    public void setCheckConstraints(boolean bit) {
        this.m_CheckConstraints = bit;
    }
    public boolean getCheckConstraints() {
        return this.m_CheckConstraints;
    }
    public String checkConstraintsTipText() {
        return "Toggle check constraints.";
    }
}