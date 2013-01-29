package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GIIndividualIntegerData;
import eva2.server.go.individuals.InterfaceDataTypeInteger;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * An abstract problem with integer data type.
 * 
 */
public abstract class AbstractProblemInteger extends AbstractOptimizationProblem implements java.io.Serializable {

    protected AbstractEAIndividual      m_OverallBest       = null;
    protected int                       m_ProblemDimension  = 10;

    public AbstractProblemInteger() {
    	initTemplate();
    }

    protected void initTemplate() {
    	if (m_Template==null) {
            m_Template = new GIIndividualIntegerData();
        }
		if (((InterfaceDataTypeInteger)this.m_Template).size()!=this.getProblemDimension()) {
			((InterfaceDataTypeInteger)this.m_Template).setIntegerDataLength(this.getProblemDimension());
		}
    }

	public void cloneObjects(AbstractProblemInteger o) {
		if (o.m_Template != null) {
                m_Template = (AbstractEAIndividual)o.m_Template.clone();
            }
        if (o.m_OverallBest != null) {
                m_OverallBest      = (AbstractEAIndividual)((AbstractEAIndividual)o.m_OverallBest).clone();
            }
        this.m_ProblemDimension = o.m_ProblemDimension;
	}

	@Override
    public void initProblem() {
		initTemplate();
        this.m_OverallBest = null;
    }

	@Override
    public void initPopulation(Population population) {
        this.m_OverallBest = null;
        ((InterfaceDataTypeInteger)this.m_Template).setIntegerDataLength(this.m_ProblemDimension);
        AbstractOptimizationProblem.defaultInitPopulation(population, m_Template, this);
    }

    @Override
    public void evaluate(AbstractEAIndividual individual) {
        int[]           x;
        double[]        fitness;


        x = new int[((InterfaceDataTypeInteger) individual).getIntegerData().length];
        System.arraycopy(((InterfaceDataTypeInteger) individual).getIntegerData(), 0, x, 0, x.length);

        fitness = this.eval(x);
        for (int i = 0; i < fitness.length; i++) {
            // set the fitness of the individual
            individual.SetFitness(i, fitness[i]);
        }
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
        }
    }

    /** 
     * Evaluate a simple integer array to determine the fitness.
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public abstract double[] eval(int[] x);

/**********************************************************************************************************************
 * These are for GUI
 */

    @Override
    public String getName() {
    	return "AbstractProblemInteger";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
    	return "The programmer did not give further details.";
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("An integer valued problem:\n");
        sb.append(globalInfo());
        sb.append("Dimension   : "); 
        sb.append(this.getProblemDimension());
        return sb.toString();
    }


    /** This method allows you to set the problem dimension.
     * @param n     The problem dimension
     */
    public void setProblemDimension(int n) {
        this.m_ProblemDimension = n;
    }
    public int getProblemDimension() {
        return this.m_ProblemDimension;
    }
    public String problemDimensionTipText() {
        return "Length of the integer vector to be optimized";
    }

    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setIndividualTemplate(AbstractEAIndividual indy) {
        this.m_Template = (AbstractEAIndividual)indy;
    }
}