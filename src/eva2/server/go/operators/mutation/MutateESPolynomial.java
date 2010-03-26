package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.09.2004
 * Time: 17:05:23
 * To change this template use File | Settings | File Templates.
 */
public class MutateESPolynomial implements InterfaceMutation, java.io.Serializable  {
    private double                          m_Eta = 0.2;

    public MutateESPolynomial() {
    }
    
    public MutateESPolynomial(MutateESPolynomial mutator) {
        this.m_Eta     = mutator.m_Eta;
    }
    
    public MutateESPolynomial(double eta) {
    	m_Eta = eta;
    }
    
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESPolynomial(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESPolynomial) {
        	MutateESPolynomial mut = (MutateESPolynomial)mutator;
            if (this.m_Eta != mut.m_Eta) return false;
            return true;
        } else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
            double[][]  range   = ((InterfaceESIndividual)individual).getDoubleRange();
            for (int i = 0; i < x.length; i++) {
            	
            	  double r = RNG.randomDouble();
            	  double delta=0;
                  if(r < 0.5){
                      delta = Math.pow((2*r),(1/(m_Eta+1))) - 1;
                  }else{
                      delta = 1 - Math.pow((2*(1 - r)),(1/(m_Eta+1)));
                  }

                x[i] += delta;
                if (range[i][0] > x[i]) x[i] = range[i][0];
                if (range[i][1] < x[i]) x[i] = range[i][1];
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

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "ES fixed step size mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES polynomial mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The polynomial mutation alters all elements according to a polynomial distribution";
    }

    /** This method allows you to set the number of crossovers that occur in the
     * genotype.
     * @param a   The number of crossovers.
     */
    public void setEta(double a) {
        if (a < 0) a = 0;
        this.m_Eta = a;
    }
    public double getEta() {
        return this.m_Eta;
    }
    public String etaTipText() {
        return "Set the Eta_c value (the larger the value, the more restricted the search).";
    }
}
