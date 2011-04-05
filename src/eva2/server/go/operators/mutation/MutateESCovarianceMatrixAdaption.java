package eva2.server.go.operators.mutation;

import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.tools.math.Jama.EigenvalueDecomposition;
import eva2.tools.math.Jama.Matrix;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.05.2003
 * Time: 17:47:45
 * To change this template use Options | File Templates.
 */

public class MutateESCovarianceMatrixAdaption implements InterfaceMutation, java.io.Serializable  {
    
    protected int                 m_D;
    protected double[]            m_Z;
    protected double              m_SigmaGlobal       = 1;
    protected double              m_InitSigmaScalar   = -1;
    protected double              m_c;
    protected double              cu;
    protected double              cov;
    protected double              Beta;
    protected double[]            s_N;
    protected double[]            m_PathS;
    protected double[]             Bz;
    protected double              xi_dach;
    protected Matrix              m_C;
    protected Matrix              B;
    protected boolean             m_CheckConstraints  = false;
    protected int                 m_constraintMaxTries        = 50;
    protected int                 m_Counter;
    protected int                 m_frequency         = 1;
    protected double[]            m_Eigenvalues;

    public MutateESCovarianceMatrixAdaption() {

    }
    public MutateESCovarianceMatrixAdaption(MutateESCovarianceMatrixAdaption mutator) {
        this.m_Counter          = mutator.m_Counter;
        this.m_frequency        = mutator.m_frequency;
        this.m_InitSigmaScalar  = mutator.m_InitSigmaScalar;
        this.m_constraintMaxTries       = mutator.m_constraintMaxTries;
        this.m_CheckConstraints = mutator.m_CheckConstraints;
        this.m_D                = mutator.m_D;
        this.m_SigmaGlobal      = mutator.m_SigmaGlobal;
        this.m_c                = mutator.m_c;
        this.cu                 = mutator.cu;
        this.cov                = mutator.cov;
        this.Beta               = mutator.Beta;
        this.xi_dach            = mutator.xi_dach;
        if (mutator.s_N != null)    this.s_N    = (double[]) mutator.s_N.clone();
        if (mutator.m_PathS != null)  this.m_PathS  = (double[]) mutator.m_PathS.clone();
        if (mutator.Bz != null)     this.Bz     = (double[]) mutator.Bz.clone();
        if (mutator.m_C != null)    this.m_C    = (Matrix) mutator.m_C.clone();
        if (mutator.B != null)      this.B      = (Matrix) mutator.B.clone();
        if (mutator.m_Z != null)    this.m_Z    = (double[]) mutator.m_Z.clone();
        if (mutator.m_Eigenvalues != null) this.m_Eigenvalues = (double[]) mutator.m_Eigenvalues.clone();
    }

    /**
	 * Constructor allowing to set the range check option.
	 * 
	 * @param doCheckConstraints if true, the range constraints are enforced on mutation.
	 */
    public MutateESCovarianceMatrixAdaption(boolean doCheckConstraints) {
		setCheckConstraints(doCheckConstraints);
	}
    
	/** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESCovarianceMatrixAdaption(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
    	if (mutator==this) return true;
        if (mutator instanceof MutateESCovarianceMatrixAdaption) {
            MutateESCovarianceMatrixAdaption mut = (MutateESCovarianceMatrixAdaption)mutator;
            // i assume if the C Matrix is equal then the mutation operators are equal
            try {
            	if (this.m_C==mut.m_C) return true;
                double[][] c1 = this.m_C.getArray();
                double[][] c2 = mut.m_C.getArray();
                if (c1==c2) return true;
                for (int i = 0; i < c1.length; i++) {
                    for(int j = 0; j < c1[i].length; j++) {
                        if (c1[i][j] != c2[i][j]) return false;
                    }
                }
            } catch (java.lang.NullPointerException e) {
                return false;
            }
            return true;
        }
        else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual        The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        if (!(individual instanceof InterfaceESIndividual)) return;
        double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
        double[][]  ranges  = ((InterfaceESIndividual)individual).getDoubleRange();

        this.m_Counter      = this.m_frequency;
        if (m_InitSigmaScalar>0) this.m_SigmaGlobal  = this.m_InitSigmaScalar;
        else {
        	double avgRange = Mathematics.getAvgRange(ranges);
        	this.m_SigmaGlobal  = 0.25*avgRange;
        }
        System.out.println("Init sigma: " + m_SigmaGlobal);
        this.m_D            = x.length;
        this.m_C            = Matrix.identity(this.m_D, this.m_D);
        EigenvalueDecomposition helper = new EigenvalueDecomposition(this.m_C);
        this.B              = helper.getV();
        this.m_c            = Math.sqrt(1.0 / (double) this.m_D);
        this.cu             = Math.sqrt( (2.0 - this.m_c) / this.m_c);
        this.Beta           = this.m_c;
        this.cov            = 2.0 / ( (double) this.m_D * (double) this.m_D);
        this.m_Z            = new double[this.m_D];
        this.s_N            = new double[this.m_D];
        this.Bz             = new double[this.m_D];
        this.m_PathS          = new double[this.m_D];
        for (int i = 0; i < this.m_D; i++) {
            this.s_N[i]     = 0;
            this.Bz[i]      = 0;
            this.m_PathS[i]   = 0;
        }
        this.xi_dach = Math.sqrt(this.m_D - 0.5);
        evaluateNewObjectX(x, ranges);
    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
            double[][]  ranges  = ((InterfaceESIndividual)individual).getDoubleRange();
            this.adaptStrategy();
            this.evaluateNewObjectX(x, ranges);
            for (int i = 0; i < x.length; i++) {
                if (x[i]  < ranges[i][0]) x[i] = ranges[i][0];
                if (x[i]  > ranges[i][1]) x[i] = ranges[i][1];
            }
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

    protected void adaptStrategy() {
        double  Cij;
        double  Bz_d;
        double  pathLen = 0.0;
        for (int i = 0; i < this.m_D; i++)
            this.s_N[i] = (1.0 - this.m_c) * this.s_N[i] + this.m_c * this.cu * this.Bz[i];
//        System.out.println("C bef:\n" + m_C.toString());
        // ADAPT COVARIANCE
        for (int i = 0; i <this. m_D; i++) {
            for (int j = i; j < this.m_D; j++) {
                Cij = (1.0 - this.cov) * this.m_C.get(i, j) + this.cov * this.s_N[i] * this.s_N[j];
                this.m_C.set(i, j, Cij);
                this.m_C.set(j, i, Cij);
            }
        }
//        System.out.println("C aft:\n" + m_C.toString());
        // ADAPT GLOBAL STEPSIZE
        for (int i = 0; i < this.m_D; i++) {
            Bz_d            = 0.0;
            for (int j = 0; j < this.m_D; j++) Bz_d = Bz_d + this.B.get(i, j) * this.m_Z[j];
            this.m_PathS[i]   = (1.0 - this.m_c) * this.m_PathS[i] + this.m_c * this.cu * Bz_d;
            pathLen          = pathLen + this.m_PathS[i] * this.m_PathS[i];
        }
        this.m_SigmaGlobal = this.m_SigmaGlobal * Math.exp(this.Beta * this.m_c * (Math.sqrt(pathLen) - this.xi_dach));
  }

    protected void evaluateNewObjectX(double[] x,double[][] range) {
//        if (Double.isNaN((x[0]))) System.out.println("treffer in cma "+ x[0]);
//        if (Double.isNaN((m_C.get(0,0)))) System.out.println("treffer in cma");
//        for (int i=0;i<N;i++)   {   // evaluate new random values
//            m_Z[i] = RNG.gaussianDouble(1.0);
//        }
//        m_C = (m_C.plus(m_C.transpose()).times(0.5)); // MAKE C SYMMETRIC
//        EigenvalueDecomposition helper = new EigenvalueDecomposition(m_C);
//        B = helper.getV();
//        double [] Eigenvalues = helper.getRealEigenvalues();
//        double[]    tmpD = new double[x.length];
//        boolean constraint = false;
//        while (constraint == false) {
//
//            for (int i=0;i<N;i++) {
//                Bz[i] = 0;
//                for (int j=0;j<N;j++) {
//                    Bz[i] = Bz[i] + Math.sqrt(Math.abs(Eigenvalues[j])) * B.get(i,j)*m_Z[j];
//                }
//                tmpD[i]=x[i]+m_SigmaScalar*Bz[i]; // here is the new value
//            }
//            constraint = true;
//            if (this.m_CheckConstraints) {
//                for (int i=0;i<N;i++) {
//                    if ((tmpD[i]<range[i][0]) || (tmpD[i]>range[i][1])) constraint = false;
//                }
//                if (!constraint) this.m_SigmaScalar = this.m_SigmaScalar/2;
//                if (this.m_SigmaScalar < 1.e-20) {
//                    this.m_SigmaScalar = 1.e-10;
//                    return;
//                }
//            }
//        }
//        for (int i = 0; i < N; i++) x[i] = tmpD[i];
        // conservation of mutation direction:
        //double[] oldZ = (double[]) this.m_Z.clone();
        double[] oldX = (double[])x.clone();
        
        for (int i = 0; i < this.m_D; i++) this.m_Z[i] = RNG.gaussianDouble(1.0);

        this.m_C = (this.m_C.plus(this.m_C.transpose()).times(0.5)); // MAKE C SYMMETRIC
        this.m_Counter++;
        if (this.m_Counter >= this.m_frequency) {
            EigenvalueDecomposition helper;
            this.m_Counter      = 0;
            helper              = new EigenvalueDecomposition(this.m_C);
            this.B              = helper.getV();
            this.m_Eigenvalues  = helper.getRealEigenvalues();

        }
        boolean isNewPosFeasible  = false;
        int     counter     = 0;
        while (!isNewPosFeasible && counter < this.m_constraintMaxTries) {
            for (int i = 0; i < this.m_D; i++) {
                this.Bz[i] = 0;
                for (int j = 0; j < this.m_D; j++) {
                    this.Bz[i] = this.Bz[i] + Math.sqrt(Math.abs(this.m_Eigenvalues[j])) * this.B.get(i, j) * this.m_Z[j];
                }
                x[i] = x[i] + this.m_SigmaGlobal * this.Bz[i]; // here is the new value
            }
            isNewPosFeasible = true;
            if (this.m_CheckConstraints == true) {
                for (int i = 0; i < m_D; i++) {
                    if (x[i] < range[i][0] || x[i] > range[i][1]) {
                    	// undo the step and try new Z
                        for (int j = 0; j < this.m_D; j++) x[j] = oldX[j] - this.m_SigmaGlobal * this.Bz[j];
                        this.m_SigmaGlobal*=0.5;
                        isNewPosFeasible = false;
                        counter++;
                        break;
                    }
                }
            }
        }
        if (counter>0) {
//        	System.out.print("CMA ES Req " + counter + " ");
//        	if (counter > 15) System.out.println(BeanInspector.toString(x));
//        	else System.out.println();
        }
        if (this.m_CheckConstraints && !isNewPosFeasible) { // use force
        	Mathematics.projectToRange(x, range);
//        	System.err.println("PROJECTING BY FORCE");
        }
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "CMA mutation";
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "CMA mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is the most sophisticated CMA mutation.";
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

    /** This method allows you to set the initial sigma value.
     * @param d     The initial sigma value.
      */
    public void setInitSigmaScalar(double d) {
        this.m_InitSigmaScalar = d;
    }
    public double getInitSigmaScalar() {
        return this.m_InitSigmaScalar;
    }
    public String initSigmaScalarTipText() {
        return "Set the initial sigma value, or -1 to use quarter average range of the problem.";
    }
}
