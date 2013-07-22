package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 02.04.2003
 * Time: 17:58:30
 * To change this template use Options | File Templates.
 */
public class MutateESCorrolated implements InterfaceMutation, java.io.Serializable {
    protected double mutationStepSize = 0.2;
    protected double tau1 = 0.15;
    protected double lowerLimitStepSize = 0.0000005;
    private static final long serialVersionUID = 1L;
    private double[] sigmas = null;
    private double[] alphas = null;
    protected double tau2 = 0.15;

    public MutateESCorrolated() {
        this.sigmas = null;
        this.alphas = null;
    }

    public MutateESCorrolated(MutateESCorrolated mutator) {
        if ((mutator.sigmas != null)) {
            this.sigmas = new double[mutator.sigmas.length];
            for (int i = 0; i < this.sigmas.length; i++) {
                this.sigmas[i] = mutator.sigmas[i];
            }

        }
        if (mutator.alphas != null) {
            this.alphas = new double[mutator.alphas.length];
            for (int i = 0; i < this.alphas.length; i++) {
                this.alphas[i] = mutator.alphas[i];
            }

        }

        this.mutationStepSize = mutator.mutationStepSize;
        this.tau1 = mutator.tau1;
        this.tau2 = mutator.tau2;
        this.lowerLimitStepSize = mutator.lowerLimitStepSize;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESCorrolated(this);
    }

    /**
     * This method allows you to evaluate whether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator == this) {
            return true;
        }
        if (mutator instanceof MutateESCorrolated) {
            MutateESCorrolated mut = (MutateESCorrolated) mutator;
            if (this.tau1 != mut.tau1) {
                return false;
            }
            if (this.tau2 != mut.tau2) {
                return false;
            }
            if (this.lowerLimitStepSize != mut.lowerLimitStepSize) {
                return false;
            }
            if (this.sigmas != null) {
                for (int i = 0; i < this.sigmas.length; i++) {
                    if (this.sigmas[i] != mut.sigmas[i]) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            if (this.alphas != null) {
                for (int i = 0; i < this.alphas.length; i++) {
                    if (this.alphas[i] != mut.alphas[i]) {
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
     * This method allows you to init the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual) individual).getDGenotype();
            if (this.sigmas == null) {
                // init the Sigmas
                this.sigmas = new double[x.length];
                for (int i = 0; i < this.sigmas.length; i++) {
                    this.sigmas[i] = this.mutationStepSize;
                }
            }
            if (this.alphas == null) {
                // init the Alphas
                this.alphas = new double[(x.length * (x.length - 1)) / 2];
                for (int i = 0; i < this.alphas.length; i++) {
                    this.alphas[i] = 0.0;
                }
            }
        }
    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
//    public void mutate(AbstractEAIndividual individual) {
//        if (individual instanceof InterfaceESIndividual) {
//            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
//            double[]    xCopy   = ((InterfaceESIndividual)individual).getDGenotype();
//            double[][]  range   = ((InterfaceESIndividual)individual).getDoubleRange();
//            double      tmpR    = RNG.gaussianDouble(1);
//            if (this.sigmas == null) {
//                // init the Sigmas
//                this.sigmas = new double[x.length];
//                for (int i = 0; i < this.sigmas.length; i++) this.sigmas[i] = this.mutationStepSize;
//            }
//            
//            //Mutate Sigmas
//            for (int i = 0; i < x.length; i++) {
//                this.sigmas[i] = this.sigmas[i] * Math.exp(this.tau1 * tmpR + this.tau2 * RNG.gaussianDouble(1));
//                if (this.sigmas[i] < this.lowerLimitStepSize) this.sigmas[i] = this.lowerLimitStepSize;
//            }
//
////            if (this.alphas == null) {
////            	// init the Alphas
////            	this.alphas = new double[(x.length*(x.length-1))/2];
////            	for (int i = 0; i < this.alphas.length; i++) this.alphas[i] = 0.0;
////            }
//
////            //Mutate Alphas
////            for (int i = 0; i < this.alphas.length; i++) {
////            	this.alphas[i] = this.alphas[i] + RNG.gaussianDouble(0.01);
////            	if (this.alphas[i] < -m_PI/2) this.alphas[i] = -m_PI/2;
////            	if (this.alphas[i] > m_PI/2) this.alphas[i] = m_PI/2;
////            }
//
//            //Generate mutationvector in unitspace modified by sigmas
//            for (int i = 0; i < x.length; i++) {
//            	xCopy[i] = RNG.gaussianDouble(this.sigmas[i]);
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
            double[] x = ((InterfaceESIndividual) individual).getDGenotype();
            double[] xCopy = new double[x.length];
            double[][] range = ((InterfaceESIndividual) individual).getDoubleRange();
            double tmpR = RNG.gaussianDouble(1);

            //Mutate Sigmas
            for (int i = 0; i < x.length; i++) {
                this.sigmas[i] *= Math.exp(this.tau1 * tmpR + this.tau2 * RNG.gaussianDouble(1));
                if (this.sigmas[i] < this.lowerLimitStepSize) {
                    this.sigmas[i] = this.lowerLimitStepSize;
                }
            }

            //Mutate Alphas
            for (int i = 0; i < this.alphas.length; i++) {
                this.alphas[i] += RNG.gaussianDouble(0.2);
                if (this.alphas[i] < -Math.PI / 2) {
                    this.alphas[i] = -Math.PI / 2;
                }
                if (this.alphas[i] > Math.PI / 2) {
                    this.alphas[i] = Math.PI / 2;
                }
            }

            //Generate mutationvector in unitspace modified by sigmas
            for (int i = 0; i < x.length; i++) {
                xCopy[i] = RNG.gaussianDouble(this.sigmas[i]);
            }

            //turn mutationvector with alphas
            for (int i = 0; i < x.length - 1; i++) {
                for (int j = i + 1; j < x.length; j++) {
                    double alpha = this.getAlpha(i, j, x.length);
                    double xX = java.lang.Math.cos(alpha) * xCopy[i] - java.lang.Math.sin(alpha) * xCopy[j];
                    double xY = java.lang.Math.sin(alpha) * xCopy[i] + java.lang.Math.cos(alpha) * xCopy[j];
                    xCopy[i] = xX;
                    xCopy[j] = xY;
                }
            }

            //modify genotype
            for (int i = 0; i < x.length; i++) {
                x[i] += ((range[i][1] - range[i][0]) / 2) * xCopy[i];
                if (range[i][0] > x[i]) {
                    x[i] = range[i][0];
                }
                if (range[i][1] < x[i]) {
                    x[i] = range[i][1];
                }
            }

            ((InterfaceESIndividual) individual).SetDGenotype(x);
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

    /**
     * @param i
     * @param j
     */
    private double getAlpha(int i, int j, int n) {
        int sum = 0;
        if (i < j) {
            for (int count = 0; count < i; count++) {
                sum += n - count - 1;
            }
            sum += j - i;
            sum--;
            return this.alphas[sum];
        } else {
            System.err.println("Falscher Zugriff auf Alphaliste!");
            return 0.0;
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
        return "ES local correlated mutation";
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
        return "ES local correlated mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The local correlated mutation stores n sigmas for each double attribute and n(n-1) alphas.";
    }

    /**
     * Set the value for tau2 with this method.
     *
     * @param d The mutation operator.
     */
    public void setTau2(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau2 = d;
    }

    public double getTau2() {
        return this.tau2;
    }

    public String tau2TipText() {
        return "Set the value for tau2.";
    }

    /**
     * Set the initial mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setMutationStepSize(double d) {
        if (d < 0) {
            d = this.lowerLimitStepSize;
        }
        this.mutationStepSize = d;
    }

    public double getMutationStepSize() {
        return this.mutationStepSize;
    }

    public String mutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setLowerLimitStepSize(double d) {
        if (d < 0) {
            d = 0;
        }
        this.lowerLimitStepSize = d;
    }

    public double getLowerLimitStepSize() {
        return this.lowerLimitStepSize;
    }

    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    public void setTau1(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau1 = d;
    }

    public double getTau1() {
        return this.tau1;
    }

    public String tau1TipText() {
        return "Set the value for tau1.";
    }
}
