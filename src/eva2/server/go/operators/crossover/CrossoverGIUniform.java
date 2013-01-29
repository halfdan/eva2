package eva2.server.go.operators.crossover;



import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGIIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.06.2005
 * Time: 14:38:23
 * To change this template use File | Settings | File Templates.
 */
public class CrossoverGIUniform implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem    m_OptimizationProblem;

    public CrossoverGIUniform() {

    }
    public CrossoverGIUniform(CrossoverGIUniform c) {
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverGIUniform(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[partners.size()+1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        if (partners.size() == 0) return result;
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());
        if ((indy1 instanceof InterfaceGIIndividual) && (partners.get(0) instanceof InterfaceGIIndividual)) {
            int         length          =  ((InterfaceGIIndividual)indy1).getGenotypeLength();
            int         mixer           = RNG.randomInt(0, partners.size());
            int[][][]   tmpInts         = new int[2][partners.size()+1][];

            tmpInts[0][0]     = ((InterfaceGIIndividual)indy1).getIGenotype();
            tmpInts[1][0]     = ((InterfaceGIIndividual)result[0]).getIGenotype();
            for (int i = 0; i < partners.size(); i++) {
                length = Math.max(length, ((InterfaceGIIndividual)partners.get(i)).getGenotypeLength());
                tmpInts[0][i+1] = ((InterfaceGIIndividual)partners.get(i)).getIGenotype();
                tmpInts[1][i+1] = ((InterfaceGIIndividual)result[i+1]).getIGenotype();
                length = Math.max(length, ((InterfaceGIIndividual)partners.get(i)).getGenotypeLength());
            }

            for (int i = 0; i < length; i++) {
                mixer = RNG.randomInt(0, partners.size());
                for (int j = 0; j < tmpInts[0].length; j++) {
                    if ((tmpInts[0][(j + mixer) % tmpInts[0].length].length > i) &&
                        (tmpInts[1][j].length > i)) {
                        tmpInts[1][j][i] = tmpInts[0][(j + mixer) % tmpInts[0].length][i];
                    }
                }
            }

            for (int i = 0; i < result.length; i++) {
                ((InterfaceGIIndividual)result[i]).SetIGenotype(tmpInts[1][i]);
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
        if (crossover instanceof CrossoverGIUniform) return true;
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
        return "GI uniform crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a uniform crossover between m individuals.";
    }
}