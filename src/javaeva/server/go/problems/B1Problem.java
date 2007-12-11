package javaeva.server.go.problems;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.GAIndividualBinaryData;
import javaeva.server.go.individuals.InterfaceDataTypeBinary;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;


import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 21.03.2003
 * Time: 13:05:33
 * To change this template use Options | File Templates.
 */
public class B1Problem extends AbstractOptimizationProblem implements java.io.Serializable {

    public int          m_ProblemDimension  = 30;

    public B1Problem() {
        this.m_Template         = new GAIndividualBinaryData();
    }

    public B1Problem(B1Problem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //AbstractBinaryOptimizationProblem
        this.m_ProblemDimension = b.m_ProblemDimension;
        // BLOTZ
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new B1Problem(this);
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
        // nothing to init here
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        population.clear();

        ((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(this.m_ProblemDimension);
        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy     = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
    }

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual) {
        BitSet          tmpBitSet;
        double[]        result;
        InterfaceDataTypeBinary    tmpIndy;

        // collect the data
        tmpIndy     = (InterfaceDataTypeBinary) individual;
        tmpBitSet   = tmpIndy.getBinaryData();
        // evalutate the fitness
        result = this.evaluate(tmpBitSet, tmpIndy.size());
        // set the fitness
        individual.SetFitness(result);
    }

    /** This is a simple method that evaluates a given Individual. The fitness
     * values of the individual will be set inside this method.
     * @param b         The BitSet that is to be evaluated.
     * @param l         The length of the BitSet.
     * @return          Double[]
     */
    public double[] evaluate(BitSet b, int l) {
        double[]                result = new double[1];
        int                     fitness = 0;

        for (int i = 0; i < l; i++) if (b.get(i)) fitness++;
        result[0] = fitness;
        return result;
    }

    /** This method allows you to output a string that describes a found solution
     * in a way that is most suiteable for a given problem.
     * @param individual    The individual that is to be shown.
     * @return The description.
     */
    public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
        this.evaluate(individual);
        String result = "Minimize Number of Bits problem:\n";
        result += individual.getStringRepresentation() + "\n";
        result += "Scores " + (this.m_ProblemDimension - individual.getFitness(0)) + " zero bits!";
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        String result = "";

        result += "Minimize Bits Problem:\n";
        result += "The task is to reduce the number of TRUE Bits in the given bit string.\n";
        result += "Parameters:\n";
        result += "Number of Bits: " + this.m_ProblemDimension +"\n";
        result += "Solution representation:\n";
        //result += this.m_Template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Maximize number of bits";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The task in this problem is to maximize the number of false bits in a BitSet.";
    }

    /** This method allows you to set the number of mulitruns that are to be performed,
     * necessary for stochastic optimizers to ensure reliable results.
     * @param multiruns The number of multiruns that are to be performed
     */
    public void setProblemDimension(int multiruns) {
        this.m_ProblemDimension = multiruns;
    }
    public int getProblemDimension() {
        return this.m_ProblemDimension;
    }
    public String multiRunsTipText() {
        return "Length of the BitSet that is to be optimized.";
    }

    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeBinary indy) {
        this.m_Template = (AbstractEAIndividual) indy;
    }
    public InterfaceDataTypeBinary getEAIndividual() {
        return (InterfaceDataTypeBinary) this.m_Template;
    }
    public String eAIndividualTipText() {
        return "Choose the EAIndividual to use.";
    }
}
