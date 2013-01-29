package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 10.09.2004
 * Time: 14:23:37
 * To change this template use File | Settings | File Templates.
 */
public class MutateESMutativeStepSizeControl implements InterfaceMutation, java.io.Serializable {
    protected double      m_MutationStepSize    = 0.2;
    protected double      m_Alpha               = 1.2;
    protected double      m_LowerLimitStepSize  = 0.0000005;
    protected double      m_UpperLimitStepSize  = 0.4;

    public MutateESMutativeStepSizeControl() {
    }
    
    public MutateESMutativeStepSizeControl(double initialStepSize, double lowerLimit, double upperLimit) {
    	m_MutationStepSize = initialStepSize;
    	if (m_LowerLimitStepSize > m_UpperLimitStepSize) {
    		System.err.println("Invalid step size bounds, switching upper and lower...");
    		double tmp = upperLimit;
    		upperLimit = lowerLimit;
    		lowerLimit = tmp;
    	}
    	m_LowerLimitStepSize = lowerLimit;
    	m_UpperLimitStepSize = upperLimit;
    	if (initialStepSize < lowerLimit || initialStepSize > upperLimit) {
    		m_MutationStepSize = (upperLimit + lowerLimit) /2.;
    		System.err.println("Invalid initial stepsize, setting it to " + m_MutationStepSize);
    	}
    }
    
    public MutateESMutativeStepSizeControl(MutateESMutativeStepSizeControl mutator) {
        this.m_MutationStepSize     = mutator.m_MutationStepSize;
        this.m_Alpha                = mutator.m_Alpha;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
        this.m_UpperLimitStepSize	= mutator.m_UpperLimitStepSize;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESMutativeStepSizeControl(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESMutativeStepSizeControl) {
            MutateESMutativeStepSizeControl mut = (MutateESMutativeStepSizeControl)mutator;
            if (this.m_MutationStepSize != mut.m_MutationStepSize) return false;
            if (this.m_Alpha != mut.m_Alpha) return false;
            if (this.m_LowerLimitStepSize != mut.m_LowerLimitStepSize) return false;
            if (this.m_UpperLimitStepSize != mut.m_UpperLimitStepSize) return false;
            return true;
        } else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual)individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual)individual).getDoubleRange();
            if (RNG.flipCoin(0.5))
                this.m_MutationStepSize = this.m_MutationStepSize * this.m_Alpha;
            else
                this.m_MutationStepSize = this.m_MutationStepSize / this.m_Alpha;
            if (this.m_MutationStepSize < this.m_LowerLimitStepSize) this.m_MutationStepSize = this.m_LowerLimitStepSize;
            if (this.m_MutationStepSize > this.m_UpperLimitStepSize) this.m_MutationStepSize = this.m_UpperLimitStepSize;
            for (int i = 0; i < x.length; i++) {
                x[i] += ((range[i][1] -range[i][0])/2)*RNG.gaussianDouble(this.m_MutationStepSize);
                if (range[i][0] > x[i]) x[i] = range[i][0];
                if (range[i][1] < x[i]) x[i] = range[i][1];
            }
            ((InterfaceESIndividual)individual).SetDGenotype(x);
            // System.out.println("new step size: " + m_MutationStepSize);
        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
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

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "ES mutative step size control";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "MSR";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The mutative step size control mutation randomly increases/decreases the current step size.";
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
        this.m_LowerLimitStepSize = d;
    }
    public double getLowerLimitStepSize() {
        return this.m_LowerLimitStepSize;
    }
    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }
    
    /** Set the upper limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setUpperLimitStepSize(double d) {
        this.m_UpperLimitStepSize = d;
    }
    public double getUpperLimitStepSize() {
        return this.m_UpperLimitStepSize;
    }
    public String upperLimitStepSizeTipText() {
        return "Set the upper limit for the mutation step size.";
    }

    /** Set the value for Alpha with this method.
     * @param d   The mutation operator.
     */
    public void setAlpha(double d) {
        if (d < 0) d = 0;
        this.m_Alpha = d;
    }
    public double getAlpha() {
        return this.m_Alpha;
    }
    public String alphaTipText() {
        return "Set the value for alpha.";
    }
}
