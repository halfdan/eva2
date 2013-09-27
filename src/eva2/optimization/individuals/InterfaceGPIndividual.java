package eva2.optimization.individuals;

import eva2.optimization.individuals.codings.gp.AbstractGPNode;

/**
 * This interface gives access to a tree-based genotype and should
 * only be used by mutation and crossover operators.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 03.04.2003
 * Time: 17:36:46
 * To change this template use Options | File Templates.
 */
public interface InterfaceGPIndividual {

    /**
     * This method will allow the user to read the program 'genotype'
     *
     * @return AbstractGPNode
     */
    public AbstractGPNode[] getPGenotype();

    /**
     * This method will allow the user to set the current program 'genotype'.
     *
     * @param b The new program genotype of the Individual
     */
    public void setPGenotype(AbstractGPNode[] b);

    /**
     * This method will allow the user to set the current program 'genotype'.
     *
     * @param b The new program genotype of the Individual
     * @param i The index where to insert the new program
     */
    public void setPGenotype(AbstractGPNode b, int i);

    /**
     * This method allows you to get the function area
     *
     * @return area  The area contains functions and terminals
     */
    public Object[] getFunctionArea();

    /**
     * Return the maximal allowed depth of a GP tree (or -1 if it does not apply).
     *
     * @return
     */
    public int getMaxAllowedDepth();
}
