package eva2.server.go.operators.crossover;


import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
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
    private InterfaceOptimizationProblem    m_OptimizationProblem;

    public CrossoverGPDefault() {

    }
    public CrossoverGPDefault(CrossoverGPDefault c) {
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverGPDefault(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[partners.size()+1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());
        if (partners.size() == 0) return result;
        if ((indy1 instanceof InterfaceGPIndividual) && (partners.get(0) instanceof InterfaceGPIndividual)) {

            //select node from 0 and memorize parent
            ArrayList allNodes = new ArrayList();
            AbstractGPNode[] nodes = ((InterfaceGPIndividual)result[0]).getPGenotype();
            for (int t = 0; t < nodes.length; t++) {
                allNodes = new ArrayList();
                ((InterfaceGPIndividual)result[0]).getPGenotype()[t].addNodesTo(allNodes);
                AbstractGPNode oldNode     = (AbstractGPNode) allNodes.get(RNG.randomInt(0, allNodes.size()-1));
                AbstractGPNode newNode, memorizingNode = oldNode, tmpNode;
                AbstractGPNode oldParent, newParent;
                oldParent = oldNode.getParent();
                for (int i = 1; i < result.length; i++) {
                    // choose Node from i and add it to i-1
                    allNodes = new ArrayList();
                    ((InterfaceGPIndividual)result[i]).getPGenotype()[t].addNodesTo(allNodes);
                    newNode = (AbstractGPNode) allNodes.get(RNG.randomInt(0, allNodes.size()-1));
                    tmpNode = newNode;
                    newParent = tmpNode.getParent();
                    if (oldParent == null) ((InterfaceGPIndividual)result[i-1]).SetPGenotype(newNode, t);
                    else oldParent.setNode(newNode, oldNode);
                    oldNode = tmpNode;
                    oldParent = newParent;
                }
                // add node from 0 to result.length-1
                if (oldParent == null) {
                    ((InterfaceGPIndividual)result[result.length-1]).SetPGenotype(memorizingNode, t);
                } else {
                    oldParent.setNode(memorizingNode, oldNode);
                }
            }
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }

    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
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
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.m_OptimizationProblem = opt;
    }

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
