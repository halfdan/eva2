package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.04.2004
 * Time: 16:23:27
 * To change this template use File | Settings | File Templates.
 */
public class CrossoverESNPointDiscreteDislocation implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem    m_OptimizationProblem;
    private int                             m_NumberOfCrossovers = 3;

    public CrossoverESNPointDiscreteDislocation() {

    }
    public CrossoverESNPointDiscreteDislocation(CrossoverESNPointDiscreteDislocation mutator) {
        this.m_OptimizationProblem    = mutator.m_OptimizationProblem;
        this.m_NumberOfCrossovers     = mutator.m_NumberOfCrossovers;
    }

    /** This method will enable you to clone a given crossover operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverESNPointDiscreteDislocation(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[]  result = null;
        double[][]              parents, children;


        result      = new AbstractEAIndividual[partners.size()+1];
        result[0]   = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1]     = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());

        if ((indy1 instanceof InterfaceESIndividual) && (partners.get(0) instanceof InterfaceESIndividual)) {
            int         length          =  ((InterfaceESIndividual)result[0]).getDGenotype().length;
            int         mixer           = RNG.randomInt(0, partners.size());
            int[]       crossoverPoints = new int[this.m_NumberOfCrossovers+1];
            parents     = new double[partners.size()+1][];
            children    = new double[partners.size()+1][];
            for (int i = 0; i < result.length; i++) {
                parents[i] = new double[((InterfaceESIndividual)result[i]).getDGenotype().length];
                children[i] = new double[parents[i].length];
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, children[i], 0, parents[i].length);
            }
            for (int i = 0; i < crossoverPoints.length; i++) {
                crossoverPoints[i] = RNG.randomInt(0, length-1);
            }
            crossoverPoints[RNG.randomInt(0,this.m_NumberOfCrossovers)] = 0;
            int     parIndex = 0;
            int     chiIndex = 0;
            boolean bol;
            for (int i = 0; i < crossoverPoints.length; i++) {
                parIndex = crossoverPoints[i];
                bol = false;
                while (!bol) {
                    //System.out.println("Chi " + chiIndex + " Par "+ parIndex);
                    for (int j = 0; j < children.length; j++) {
                        children[j][chiIndex] = parents[(j + mixer) % parents.length][parIndex];
                    }
                    parIndex++;
                    chiIndex++;
                    for (int j = 0; j < crossoverPoints.length; j++) {
                        if ((parIndex == crossoverPoints[j]) || (parIndex == length) || (chiIndex == length)) {
                            bol = true;
                        }
                    }
                }
                if (chiIndex == length) {
                    i = crossoverPoints.length;
                }
                mixer++;
            }

            // write the result back
            for (int i = 0; i < result.length; i++) {
                ((InterfaceESIndividual)result[i]).SetDGenotype(children[i]);
            }
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) {
            result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        }
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }

    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverESNPointDiscreteDislocation) {
            CrossoverESNPointDiscreteDislocation cross = (CrossoverESNPointDiscreteDislocation)crossover;
            if (this.m_NumberOfCrossovers != cross.m_NumberOfCrossovers) {
                return false;
            }
            return true;
        } else {
            return false;
        }
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
        return "ES discrete n-point crossover with dislocation";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a discrete n-point crossover between m ES individuals with dislocation.";
    }

    /** This method allows you to set the number of crossovers that occur in the
     * genotype.
     * @param crossovers   The number of crossovers.
     */
    public void setNumberOfCrossovers(int crossovers) {
        if (crossovers < 0) {
            crossovers = 0;
        }
        this.m_NumberOfCrossovers = crossovers;
    }
    public int getNumberOfCrossovers() {
        return this.m_NumberOfCrossovers;
    }
    public String numberOfCrossoversTipText() {
        return "The number of crossoverpoints.";
    }
}