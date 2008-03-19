package javaeva.server.go.problems;

import java.util.BitSet;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.GAIndividualBinaryData;
import javaeva.server.go.individuals.InterfaceDataTypeBinary;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;

public abstract class AbstractProblemBinary extends AbstractOptimizationProblem {
	
	public AbstractProblemBinary() {
		initTemplate();
	}
	
	protected void initTemplate() {
		this.m_Template         = new GAIndividualBinaryData();
        ((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(this.getProblemDimension());
	}
	
	public void cloneObjects(AbstractProblemBinary o) {
		if (o.m_Template != null) m_Template = (AbstractEAIndividual)o.m_Template.clone();
	}
	
	@Override
	public void evaluate(AbstractEAIndividual individual) {
        BitSet          tmpBitSet;
        double[]        result;
        
        tmpBitSet   = ((InterfaceDataTypeBinary) individual).getBinaryData();
        // evaluate the fitness
        result = eval(tmpBitSet);
        // set the fitness
        individual.SetFitness(result);
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
	
	@Override
	public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;
        population.clear();

        ((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(this.getProblemDimension());

        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
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
    public String getName() {
    	return "AbstractProblemBinary";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
    	return "The programmer did not give further details.";
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("A binary valued problem:\n");
        sb.append(globalInfo());
        sb.append("Dimension   : "); 
        sb.append(this.getProblemDimension());
        return sb.toString();
    }
}

