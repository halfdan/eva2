package eva2.server.go.operators.crossover;


import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GPIndividualProgramData;
import eva2.server.go.individuals.InterfaceGPIndividual;
import eva2.server.go.individuals.codings.gp.AbstractGPNode;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:37:05
 * To change this template use Options | File Templates.
 */
public class CrossoverGPDefault implements InterfaceCrossover, java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8900427365914281930L;
	private InterfaceOptimizationProblem    m_OptimizationProblem;
    private boolean maintainMaxDepth = true;
	private static final boolean TRACE=false;

    public CrossoverGPDefault() {
    }
    public CrossoverGPDefault(CrossoverGPDefault c) {
    	this.maintainMaxDepth = c.maintainMaxDepth;
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverGPDefault(this);
    }

    /** 
     * Exchange subtrees of two GP nodes. Allows to keep the depth restriction.
     * 
     * @param indy1 The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
    	if (partners.size()>1) System.err.println("Warning, crossover may not work on more than one partner! " + this.getClass());
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[partners.size()+1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        if (TRACE) for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getStringRepresentation());
        if (partners.size() == 0) return result;
        if ((indy1 instanceof InterfaceGPIndividual) && (partners.get(0) instanceof InterfaceGPIndividual)) {
        	int allowedDepth = ((InterfaceGPIndividual)indy1).getMaxAllowedDepth();
        	
            AbstractGPNode[] nodes = ((InterfaceGPIndividual)result[0]).getPGenotype();
            for (int t = 0; t < nodes.length; t++) { // for each of the genotypes (multiploidy??)
                ((InterfaceGPIndividual)result[0]).getPGenotype()[t].getRandomNode();
                AbstractGPNode selNodeThis     = ((InterfaceGPIndividual)result[0]).getPGenotype()[t].getRandomNode();
                AbstractGPNode selNodeOther = ((InterfaceGPIndividual)result[1]).getPGenotype()[t].getRandomNode();
                if (maintainMaxDepth) {//System.err.print(".");
                	int maxTries=10;
                		// if the echange would violate the depth restriction, choose new nodes for a few times...
                		while (maxTries>=0 && ((selNodeOther.getSubtreeDepth()+selNodeThis.getDepth()>allowedDepth) || 
                				(selNodeThis.getSubtreeDepth()+selNodeOther.getDepth()>allowedDepth))) {
                            if (RNG.flipCoin(0.5)) selNodeThis  = ((InterfaceGPIndividual)result[0]).getPGenotype()[t].getRandomNode();
                            else selNodeOther = ((InterfaceGPIndividual)result[1]).getPGenotype()[t].getRandomNode();
                            maxTries--;
                		}
                	if (maxTries<0) { // on a failure, at least exchange two leaves, which always works
//                		System.err.println("Unable to select fitting nodes! Just switch leaves...");
                        selNodeThis     = ((InterfaceGPIndividual)result[0]).getPGenotype()[t].getRandomLeaf();
                        selNodeOther = ((InterfaceGPIndividual)result[1]).getPGenotype()[t].getRandomLeaf();
                	}
                }
                if (TRACE) {
                	System.out.println("Selected t " + selNodeThis.getStringRepresentation());
                	System.out.println("Selected o " + selNodeOther.getStringRepresentation());
                }
                
                AbstractGPNode selNodeThisParent, selNodeOtherParent;
                selNodeThisParent = selNodeThis.getParent();
                selNodeOtherParent = selNodeOther.getParent();
                
                // actually switch individuals!
                if (selNodeThisParent == null) ((InterfaceGPIndividual)result[0]).SetPGenotype((AbstractGPNode)selNodeOther.clone(), t);
                else selNodeThisParent.setNode((AbstractGPNode)selNodeOther.clone(), selNodeThis);
//                for (int i = 0; i < result.length; i++) System.out.println("-- Betw Crossover: " +result[i].getStringRepresentation());
                if (selNodeOtherParent == null) ((InterfaceGPIndividual)result[1]).SetPGenotype((AbstractGPNode)selNodeThis.clone(), t);
                else selNodeOtherParent.setNode((AbstractGPNode)selNodeThis.clone(), selNodeOther);

            }
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) {
        	((GPIndividualProgramData)result[i]).checkDepth();
        	result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        }
        if (TRACE) for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getStringRepresentation());
        return result;
    }

    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverGPDefault) return true;
        else return false;
    }

    /** This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     * @param individual    The individual that will be mutated.
     * @param opt           The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.m_OptimizationProblem = opt;
    }

    @Override
    public String getStringRepresentation() {
        return this.getName();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GP default crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a one-point crossover between two programs.";
    }
}
