package eva2.server.go.operators.crossover;


import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 03.04.2003
 * Time: 15:03:55
 * To change this template use Options | File Templates.
 */
public class CrossoverGABitSimulated implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem    m_OptimizationProblem;

    public CrossoverGABitSimulated() {

    }
    public CrossoverGABitSimulated(CrossoverGABitSimulated c) {
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverGABitSimulated(this);
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
        for (int i = 0; i < partners.size(); i++) {
            result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        if (partners.size() == 0) return result;
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());
        if ((indy1 instanceof InterfaceGAIndividual) && (partners.get(0) instanceof InterfaceGAIndividual)) {
            int         length          =  ((InterfaceGAIndividual)indy1).getGenotypeLength();
            double      current;
            BitSet[][]  tmpBitSet       = new BitSet[2][partners.size()+1];

            tmpBitSet[0][0]     = ((InterfaceGAIndividual)indy1).getBGenotype();
            tmpBitSet[1][0]     = ((InterfaceGAIndividual)result[0]).getBGenotype();
            for (int i = 0; i < partners.size(); i++) {
                tmpBitSet[0][i+1] = ((InterfaceGAIndividual)partners.get(i)).getBGenotype();
                tmpBitSet[1][i+1] = ((InterfaceGAIndividual)result[i+1]).getBGenotype();
                length = Math.max(length, ((InterfaceGAIndividual)partners.get(i)).getGenotypeLength());
            }

            for (int i = 0; i < length; i++) {
                current = 0;
                for (int j = 0; j < tmpBitSet[0].length; j++) if (tmpBitSet[0][j].get(i)) current++;
                current = current/(double)(tmpBitSet[0].length);
                for (int j = 0; j < tmpBitSet[0].length; j++) {
                    if (RNG.flipCoin(current)) tmpBitSet[1][j].set(i);
                    else tmpBitSet[1][j].clear(i);
                }
            }

            for (int i = 0; i < result.length; i++) ((InterfaceGAIndividual)result[i]).SetBGenotype(tmpBitSet[1][i]);
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
        if (crossover instanceof CrossoverGABitSimulated) return true;
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
        return "GA bit simulated crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a bit simulated crossover between m individuals.";
    }
}
