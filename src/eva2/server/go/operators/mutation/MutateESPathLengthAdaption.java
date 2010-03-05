package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

/**
 * ES mutation with path length control. The step size (single sigma) is
 * adapted using the evolution path length by adapting the real path length
 * to the expected path length in for uncorrelated single steps. 
 * See Hansen&Ostermeier 2001, Eqs. 16,17.
 * 
 */
public class MutateESPathLengthAdaption implements InterfaceMutation, java.io.Serializable  {

  private int           m_dim;
  private double[]      m_randZ;
  private double[]      m_Path;
  private double        m_SigmaGlobal = 1.0;
  private double        m_c;
  private boolean       m_UsePath = true;
  private double		dampening = 1;
  private double 		expectedPathLen = -1;
  private double m_cu;

    public MutateESPathLengthAdaption() {

    }
    public MutateESPathLengthAdaption(MutateESPathLengthAdaption mutator) {
        this.m_UsePath          = true;
        this.m_dim                = mutator.m_dim;
        this.m_SigmaGlobal      = mutator.m_SigmaGlobal;
        this.m_c                = mutator.m_c;
        this.dampening 			= mutator.dampening;
        this.expectedPathLen 	= mutator.expectedPathLen;
        this.m_cu				= mutator.m_cu;
        if (mutator.m_randZ != null)    this.m_randZ        = (double[]) mutator.m_randZ.clone();
        if (mutator.m_Path != null) this.m_Path     = (double[]) mutator.m_Path.clone();
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESPathLengthAdaption(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESPathLengthAdaption) {
            MutateESPathLengthAdaption mut = (MutateESPathLengthAdaption)mutator;
            // i assume if the C Matrix is equal then the mutation operators are equal
            if (this.m_dim != mut.m_dim) return false;
            if (this.m_SigmaGlobal != mut.m_SigmaGlobal) return false;
            if (this.m_c != mut.m_c) return false;
            if ((this.m_randZ != null) && (mut.m_randZ != null))
                for (int i = 0; i < this.m_randZ.length; i++) if (this.m_randZ[i] != mut.m_randZ[i]) return false;
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
        this.m_dim    = x.length;
//        if (this.m_UsePath) this.m_c = Math.sqrt(1.0 / (double) this.m_dim);
        
        this.m_randZ    = new double[this.m_dim];
        this.m_Path = new double[this.m_dim];
        for (int i = 0; i < this.m_dim; i++) {
            this.m_randZ[i] = RNG.gaussianDouble(1.0);
//            this.m_Path[i]=1;
        }

        if (this.m_UsePath) this.m_c = 4./(m_dim+4);
        else this.m_c = 1.0;
        
        expectedPathLen = Math.sqrt(m_dim)*(1-(1./(4*m_dim))+(1./(21*m_dim*m_dim)));
        dampening = (1./m_c)+1;
        m_cu = Math.sqrt(m_c*(2.0-m_c));
        
        mutateX(x, ranges, true);
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
            
            this.adaptStrategy(); // this updates the path using the old step and adapts sigma
            
            this.calculateNewStep();
            
            this.mutateX(x, ranges, true); // this performs new mutation
            
            ((InterfaceESIndividual)individual).SetDGenotype(x);
        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
    }
    
	private void checkRange(double[] x, double[][] ranges) {
		for (int i = 0; i < x.length; i++) {
		    if (x[i]  < ranges[i][0]) x[i] = ranges[i][0];
		    if (x[i]  > ranges[i][1]) x[i] = ranges[i][1];
		}
	}

    private void calculateNewStep() {
   		for (int i = 0; i < m_dim; i++) m_randZ[i] = RNG.gaussianDouble(1.0);            
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
        // remember the path taken. m_randZ is at this time the last step before selection.
        for (int i = 0; i < m_dim; i++) {
			m_Path [i] = (1.0 -m_c) * m_Path[i] + m_cu*m_randZ[i];
        }
        double pathLen = Mathematics.norm(m_Path);
        
//        double expectedPathLen = Math.sqrt(((double)m_dim)+0.5);
//        double kappa_d          = ((double)m_dim)/4.0+1.0;
        
        double exp         = (pathLen - expectedPathLen)/(dampening*expectedPathLen);
        m_SigmaGlobal           = m_SigmaGlobal * Math.exp(exp);
    }

    private void mutateX(double[] x,double[][] range, boolean checkRange) {
        for (int i = 0; i < x.length; i++)
             x[i] = x[i] + m_SigmaGlobal * m_randZ[i];
        if (checkRange) checkRange(x, range);
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "Mutation/Path-Length-Control";
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Mutation/Path-Length-Control";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The single step size is controlled using the evolution path.";
    }

//    /** Use only positive numbers this limits the freedom of effect.
//     * @param bit     The new representation for the inner constants.
//      */
//    public void setUsePath(boolean bit) {
//        this.m_UsePath = bit;
//    }
//    public boolean getUsePath() {
//        return this.m_UsePath;
//    }
//    public String usePathTipText() {
//        return "Use path.";
//    }

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
        return "Set the initial global step size.";
    }
}