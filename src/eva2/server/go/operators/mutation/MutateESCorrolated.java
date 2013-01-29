package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 02.04.2003
 * Time: 17:58:30
 * To change this template use Options | File Templates.
 */
public class MutateESCorrolated implements InterfaceMutation, java.io.Serializable {
    protected double        m_MutationStepSize    = 0.2;
    protected double        m_Tau1                = 0.15;
    protected double        m_LowerLimitStepSize  = 0.0000005;
	private static final long serialVersionUID    = 1L;
	private double[]        m_Sigmas = null;
	private double[]		m_Alphas = null;
    protected double        m_Tau2                  = 0.15;

    public MutateESCorrolated() {
        this.m_Sigmas = null;
        this.m_Alphas = null;
    }

    public MutateESCorrolated(MutateESCorrolated mutator) {
        if ((mutator.m_Sigmas != null)) {
            this.m_Sigmas = new double[mutator.m_Sigmas.length];
            for (int i = 0; i < this.m_Sigmas.length; i++) {
                this.m_Sigmas[i] = mutator.m_Sigmas[i];
            }

        }
        if (mutator.m_Alphas != null) {
        	this.m_Alphas = new double[mutator.m_Alphas.length];
        	for (int i = 0; i < this.m_Alphas.length; i++) {
        		this.m_Alphas[i] = mutator.m_Alphas[i];
        	}

        }
        
        this.m_MutationStepSize     = mutator.m_MutationStepSize;
        this.m_Tau1                 = mutator.m_Tau1;
        this.m_Tau2                 = mutator.m_Tau2;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESCorrolated(this);
    }

    /** This method allows you to evaluate whether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
    	if (mutator==this) return true;
        if (mutator instanceof MutateESCorrolated) {
            MutateESCorrolated mut = (MutateESCorrolated)mutator;
            if (this.m_Tau1 != mut.m_Tau1) return false;
            if (this.m_Tau2 != mut.m_Tau2) return false;
            if (this.m_LowerLimitStepSize != mut.m_LowerLimitStepSize) return false;
            if (this.m_Sigmas != null) {
                for (int i = 0; i < this.m_Sigmas.length; i++) {
                    if (this.m_Sigmas[i] != mut.m_Sigmas[i]) return false;
                }
            } else return false;
            if (this.m_Alphas != null) {
                for (int i = 0; i < this.m_Alphas.length; i++) {
                    if (this.m_Alphas[i] != mut.m_Alphas[i]) return false;
                }
            } else return false;
            return true;
        } else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        if (individual instanceof InterfaceESIndividual) {
            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
            if (this.m_Sigmas == null) {
            	// init the Sigmas
            	this.m_Sigmas = new double[x.length];
            	for (int i = 0; i < this.m_Sigmas.length; i++) {
                    this.m_Sigmas[i] = this.m_MutationStepSize;
                }
            }
            if (this.m_Alphas == null) {
            	// init the Alphas
            	this.m_Alphas = new double[(x.length*(x.length-1))/2];
            	for (int i = 0; i < this.m_Alphas.length; i++) {
                    this.m_Alphas[i] = 0.0;
                }
            }
        }
    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
//    public void mutate(AbstractEAIndividual individual) {
//        if (individual instanceof InterfaceESIndividual) {
//            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
//            double[]    xCopy   = ((InterfaceESIndividual)individual).getDGenotype();
//            double[][]  range   = ((InterfaceESIndividual)individual).getDoubleRange();
//            double      tmpR    = RNG.gaussianDouble(1);
//            if (this.m_Sigmas == null) {
//                // init the Sigmas
//                this.m_Sigmas = new double[x.length];
//                for (int i = 0; i < this.m_Sigmas.length; i++) this.m_Sigmas[i] = this.m_MutationStepSize;
//            }
//            
//            //Mutate Sigmas
//            for (int i = 0; i < x.length; i++) {
//                this.m_Sigmas[i] = this.m_Sigmas[i] * Math.exp(this.m_Tau1 * tmpR + this.m_Tau2 * RNG.gaussianDouble(1));
//                if (this.m_Sigmas[i] < this.m_LowerLimitStepSize) this.m_Sigmas[i] = this.m_LowerLimitStepSize;
//            }
//
////            if (this.m_Alphas == null) {
////            	// init the Alphas
////            	this.m_Alphas = new double[(x.length*(x.length-1))/2];
////            	for (int i = 0; i < this.m_Alphas.length; i++) this.m_Alphas[i] = 0.0;
////            }
//
////            //Mutate Alphas
////            for (int i = 0; i < this.m_Alphas.length; i++) {
////            	this.m_Alphas[i] = this.m_Alphas[i] + RNG.gaussianDouble(0.01);
////            	if (this.m_Alphas[i] < -m_PI/2) this.m_Alphas[i] = -m_PI/2;
////            	if (this.m_Alphas[i] > m_PI/2) this.m_Alphas[i] = m_PI/2;
////            }
//
//            //Generate mutationvector in unitspace modified by sigmas
//            for (int i = 0; i < x.length; i++) {
//            	xCopy[i] = RNG.gaussianDouble(this.m_Sigmas[i]);            	
//            }
//            
//            //modify genotype
//            for (int i = 0; i < x.length; i++) {
//            	x[i] += ((range[i][1] -range[i][0])/2)*xCopy[i];
//            	if (range[i][0] > x[i]) x[i] = range[i][0];
//            	if (range[i][1] < x[i]) x[i] = range[i][1];
//            }
// 
//            ((InterfaceESIndividual)individual).SetDGenotype(x);
//            
//      //turn mutationvector with alphas
////      for (int i = 0; i < x.length-1; i++) {
////          for (int j = i+1; j < x.length; j++) {
////          	double alpha=this.getAlpha(i,j, x.length);
////          	double xX=java.lang.Math.cos(alpha)*xCopy[i]-java.lang.Math.sin(alpha)*xCopy[j];
////          	double xY=java.lang.Math.sin(alpha)*xCopy[i]+java.lang.Math.cos(alpha)*xCopy[j];
////          	xCopy[i]=xX;
////          	xCopy[j]=xY;                	
////          }
////      }
//
//        }
//    }

    @Override
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceESIndividual) {
          double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
          double[]    xCopy		= new double[x.length];
          double[][]  range   = ((InterfaceESIndividual)individual).getDoubleRange();
          double      tmpR    = RNG.gaussianDouble(1);

          //Mutate Sigmas
          for (int i = 0; i < x.length; i++) {
              this.m_Sigmas[i] *= Math.exp(this.m_Tau1 * tmpR + this.m_Tau2 * RNG.gaussianDouble(1));
            if (this.m_Sigmas[i] < this.m_LowerLimitStepSize) this.m_Sigmas[i] = this.m_LowerLimitStepSize;
          }

          //Mutate Alphas
          for (int i = 0; i < this.m_Alphas.length; i++) {
          	this.m_Alphas[i] += RNG.gaussianDouble(0.2);
          	if (this.m_Alphas[i] < -Math.PI/2) this.m_Alphas[i] = -Math.PI/2;
          	if (this.m_Alphas[i] > Math.PI/2) this.m_Alphas[i] = Math.PI/2;
          }
          
          //Generate mutationvector in unitspace modified by sigmas
          for (int i = 0; i < x.length; i++) {
        	xCopy[i] = RNG.gaussianDouble(this.m_Sigmas[i]);            	
          }      

          //turn mutationvector with alphas
          for (int i = 0; i < x.length-1; i++) {
          	for (int j = i+1; j < x.length; j++) {
          		double alpha=this.getAlpha(i,j, x.length);
          		double xX=java.lang.Math.cos(alpha)*xCopy[i]-java.lang.Math.sin(alpha)*xCopy[j];
          		double xY=java.lang.Math.sin(alpha)*xCopy[i]+java.lang.Math.cos(alpha)*xCopy[j];
          		xCopy[i]=xX;
          		xCopy[j]=xY;                	
          	}
          }

          //modify genotype
          for (int i = 0; i < x.length; i++) {
        	x[i] += ((range[i][1] -range[i][0])/2)*xCopy[i]; 
        	if (range[i][0] > x[i]) x[i] = range[i][0];
        	if (range[i][1] < x[i]) x[i] = range[i][1];
          }

          ((InterfaceESIndividual)individual).SetDGenotype(x);
        }
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /**
	 * @param i
	 * @param j
	 */
	private double getAlpha(int i, int j, int n) {
		int sum=0;
		if (i<j) {
			for (int count=0; count<i; count++) {
				sum+=n-count-1;
			}
			sum+=j-i; sum--;
			return this.m_Alphas[sum];
		}else{
			System.err.println("Falscher Zugriff auf Alphaliste!");
			return 0.0;
		}
	}

	/** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "ES local correlated mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES local correlated mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The local correlated mutation stores n sigmas for each double attribute and n(n-1) alphas.";
    }
    
    /** Set the value for tau2 with this method.
     * @param d   The mutation operator.
     */
    public void setTau2(double d) {
        if (d < 0) d = 0;
        this.m_Tau2 = d;
    }
    public double getTau2() {
        return this.m_Tau2;
    }
    public String tau2TipText() {
        return "Set the value for tau2.";
    }

    /** Set the initial mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setMutationStepSize(double d) {
        if (d < 0) d = this.m_LowerLimitStepSize;
        this.m_MutationStepSize = d;
    }
    public double getMutationStepSize() {
        return this.m_MutationStepSize;
    }
    public String mutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /** Set the lower limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setLowerLimitStepSize(double d) {
        if (d < 0) d = 0;
        this.m_LowerLimitStepSize = d;
    }
    public double getLowerLimitStepSize() {
        return this.m_LowerLimitStepSize;
    }
    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }

    /** Set the value for tau1 with this method.
     * @param d   The mutation operator.
     */
    public void setTau1(double d) {
        if (d < 0) d = 0;
        this.m_Tau1 = d;
    }
    public double getTau1() {
        return this.m_Tau1;
    }
    public String tau1TipText() {
        return "Set the value for tau1.";
    }
}
