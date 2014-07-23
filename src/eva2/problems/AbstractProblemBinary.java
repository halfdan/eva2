package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GAIndividualBinaryData;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;

import java.util.BitSet;

/**
 * An abstract problem based on binary data.
 *
 * @author mkron
 */
public abstract class AbstractProblemBinary extends AbstractOptimizationProblem {

    public AbstractProblemBinary() {
        initTemplate();
    }

    protected void initTemplate() {
        if (template == null) {
            this.template = new GAIndividualBinaryData();
        }
        if (((InterfaceGAIndividual) this.template).getGenotypeLength() != this.getProblemDimension()) {
            ((InterfaceDataTypeBinary) this.template).setBinaryDataLength(this.getProblemDimension());
        }
    }

    public void cloneObjects(AbstractProblemBinary o) {
        if (o.template != null) {
            template = (AbstractEAIndividual) o.template.clone();
        }
    }

    @Override
    public void evaluate(AbstractEAIndividual individual) {
        BitSet tmpBitSet;
        double[] result;

        tmpBitSet = ((InterfaceDataTypeBinary) individual).getBinaryData();
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
//	 * initializePopulation cycle.
//	 * @param k
//	 * @param indy
//	 */
//    protected void initIndy(int k, AbstractEAIndividual indy) {
//    	indy.init(this);
//    }

    @Override
    public void initializePopulation(Population population) {
        ((InterfaceDataTypeBinary) this.template).setBinaryDataLength(this.getProblemDimension());
        AbstractOptimizationProblem.defaultInitPopulation(population, template, this);
    }

    @Override
    public void initializeProblem() {
        initTemplate();
    }

    /**********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "AbstractProblemBinary";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The programmer did not give further details.";
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
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

