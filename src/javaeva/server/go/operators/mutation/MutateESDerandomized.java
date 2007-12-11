package javaeva.server.go.operators.mutation;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceESIndividual;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 31.05.2005
 * Time: 14:22:13
 * To change this template use File | Settings | File Templates.
 */
public class MutateESDerandomized implements InterfaceMutation, java.io.Serializable  {

  private int           m_D;
  private double[]      m_Z;
  private double[]      m_Path;
  private double        m_SigmaGlobal = 1.0;
  private double        m_c;
  private boolean       m_UsePath = true;

    public MutateESDerandomized() {

    }
    public MutateESDerandomized(MutateESDerandomized mutator) {
        this.m_UsePath          = true;
        this.m_D                = mutator.m_D;
        this.m_SigmaGlobal      = mutator.m_SigmaGlobal;
        this.m_c                = mutator.m_c;
        if (mutator.m_Z != null)    this.m_Z        = (double[]) mutator.m_Z.clone();
        if (mutator.m_Path != null) this.m_Path     = (double[]) mutator.m_Path.clone();
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESDerandomized(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESDerandomized) {
            MutateESDerandomized mut = (MutateESDerandomized)mutator;
            // i assume if the C Matrix is equal then the mutation operators are equal
            if (this.m_D != mut.m_D) return false;
            if (this.m_SigmaGlobal != mut.m_SigmaGlobal) return false;
            if (this.m_c != mut.m_c) return false;
            if ((this.m_Z != null) && (mut.m_Z != null))
                for (int i = 0; i < this.m_Z.length; i++) if (this.m_Z[i] != mut.m_Z[i]) return false;
            if ((this.m_Path != null) && (mut.m_Path != null))
                for (int i = 0; i < this.m_Path.length; i++) if (this.m_Path[i] != mut.m_Path[i]) return false;
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
        this.m_D    = x.length;
        if (this.m_UsePath == true) this.m_c = Math.sqrt(1.0 / (double) this.m_D);
        else this.m_c = 1.0;
        this.m_Z    = new double[this.m_D];
        this.m_Path = new double[this.m_D];
        for (int i = 0; i < this.m_D; i++) this.m_Z[i] = RandomNumberGenerator.gaussianDouble(1.0);
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
            for (int i = 0; i < m_D; i++) m_Z[i] = RandomNumberGenerator.gaussianDouble(1.0);            
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

    private void adaptStrategy() {
        double length_of_Z = 0;
        for (int i = 0; i < m_D; i++) {
            m_Path [i] = (1.0 -m_c) * m_Path[i] + Math.sqrt(m_c*(2.0-m_c))*m_Z[i];
            length_of_Z = length_of_Z + m_Path[i] * m_Path[i];
        }
        length_of_Z             = Math.sqrt(length_of_Z);
        double E_of_length_of_Z = Math.sqrt(((double)m_D)+0.5);
        double kappa_d          = ((double)m_D)/4.0+1.0;
        double Exponent         = (length_of_Z - E_of_length_of_Z)/(kappa_d*E_of_length_of_Z);
        m_SigmaGlobal           = m_SigmaGlobal * Math.exp(Exponent);
    }

    private void evaluateNewObjectX(double[] x,double[][] range) {
        for (int i = 0; i < x.length; i++)
             x[i] = x[i] + m_SigmaGlobal * m_Z[i];
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
    public String globalInfo() {
        return "This is the most sophisticated CMA mutation.";
    }

    /** Use only positive numbers this limits the freedom of effect.
     * @param bit     The new representation for the inner constants.
      */
    public void setUsePath(boolean bit) {
        this.m_UsePath = bit;
    }
    public boolean getUsePath() {
        return this.m_UsePath;
    }
    public String usePathTipText() {
        return "Use path.";
    }

    /** This method allows you to set the initial sigma value.
     * @param d     The initial sigma value.
      */
    public void setSigmaGlobal(double d) {
        this.m_SigmaGlobal = d;
    }
    public double getSigmaGlobal() {
        return this.m_SigmaGlobal;
    }
    public String initSigmaGlobalTipText() {
        return "Set the initial global sigma value.";
    }
}