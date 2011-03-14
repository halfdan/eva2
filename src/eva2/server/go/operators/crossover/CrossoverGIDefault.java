package eva2.server.go.operators.crossover;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGIIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * One-point crossover on integer individuals.
 * 
 * User: mkron, streiche
 * Date: 18.05.2005
 * Time: 17:10:28
 * To change this template use File | Settings | File Templates.
 */
public class CrossoverGIDefault implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem    m_OptimizationProblem;

    public CrossoverGIDefault() {

    }
    public CrossoverGIDefault(CrossoverGIDefault c) {
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverGIDefault(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[]  result = null;
        int[][]              parents, children;


        result      = new AbstractEAIndividual[partners.size()+1];
        result[0]   = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1]     = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());

        if ((indy1 instanceof InterfaceGIIndividual) && (partners.get(0) instanceof InterfaceGIIndividual)) {
            if (((InterfaceGIIndividual)indy1).getIGenotype().length <= 1) return result;
            int     crossoverpoint = RNG.randomInt(0,((InterfaceGIIndividual)indy1).getIGenotype().length-1);
            boolean switcher = RNG.randomBoolean();
            parents     = new int[partners.size()+1][];
            children    = new int[partners.size()+1][];
            for (int i = 0; i < result.length; i++) {
                parents[i] = new int[((InterfaceGIIndividual)result[i]).getIGenotype().length];
                children[i] = new int[parents[i].length];
                System.arraycopy(((InterfaceGIIndividual)result[i]).getIGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceGIIndividual)result[i]).getIGenotype(), 0, children[i], 0, parents[i].length);
            }

            for (int i = 0; i < children[0].length; i++) {
                if ((i < crossoverpoint)^(switcher)) {
                    // do nothing
                } else {
                    // exchange
                    for (int j = 0; j < children.length-1; j++) {
                         if ((i < children[j].length) && (i < parents[j+1].length))
                             children[j][i] = parents[j+1][i];
                    }
                    if ((i < children[children.length-1].length) && (i < parents[0].length))
                        children[children.length-1][i] = parents[0][i];
                }
            }
            // write the result back
            for (int i = 0; i < result.length; i++) ((InterfaceGIIndividual)result[i]).SetIGenotype(children[i]);
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
        if (crossover instanceof CrossoverESDefault) return true;
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
        return "GI discrete one-point crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a discrete one-point crossover between m GI individuals.";
    }
}
