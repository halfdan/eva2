package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import java.util.BitSet;

/**
 * An abstract problem based on binary data.
 * 
 * @author mkron
 *
 */
public abstract class AbstractProblemBinary extends AbstractOptimizationProblem {
	
	public AbstractProblemBinary() {
		initTemplate();
	}
	
	protected void initTemplate() {
		if (m_Template == null) {
                this.m_Template         = new GAIndividualBinaryData();
            }
		if (((InterfaceGAIndividual)this.m_Template).getGenotypeLength()!=this.getProblemDimension()) {
			((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(this.getProblemDimension());
		}
	}
	
	public void cloneObjects(AbstractProblemBinary o) {
		if (o.m_Template != null) {
                m_Template = (AbstractEAIndividual)o.m_Template.clone();
            }
	}
	
	@Override
	public void evaluate(AbstractEAIndividual individual) {
        BitSet          tmpBitSet;
        double[]        result;
        
        tmpBitSet   = ((InterfaceDataTypeBinary) individual).getBinaryData();
        // evaluate the fitness
        result = eval(tmpBitSet);
        // set the fitness
        individual.setFitness(result);
	}
	
	/**
	 * Evaluate a BitSet representing a possible solution. This is the target
	 * function implementation.
	 *  
	 * @param x a BitSet representing a possible
	 * @return
	 */
	public abstract double[] eval(BitSet bs);
	
	/**
	 * Get the problem dimension.
	 * 
	 * @return the problem dimension
	 */
	public abstract int getProblemDimension();

//	/**
//	 * Initialize a single individual with index k in the
//	 * initPopulation cycle.
//	 * @param k
//	 * @param indy
//	 */
//    protected void initIndy(int k, AbstractEAIndividual indy) {
//    	indy.init(this);
//    }

	@Override
	public void initPopulation(Population population) {
        ((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(this.getProblemDimension());
        AbstractOptimizationProblem.defaultInitPopulation(population, m_Template, this);
	}
	
	@Override
	public void initProblem() {
		initTemplate();
	}
	    
    /**********************************************************************************************************************
     * These are for GUI
     */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
    	return "AbstractProblemBinary";
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
        sb.append("A binary valued problem:\n");
        sb.append(globalInfo());
        sb.append("Dimension   : "); 
        sb.append(this.getProblemDimension());
        return sb.toString();
    }
}

