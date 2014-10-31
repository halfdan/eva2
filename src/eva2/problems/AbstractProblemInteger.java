package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GIIndividualIntegerData;
import eva2.optimization.individuals.InterfaceDataTypeInteger;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;

/**
 * An abstract problem with integer data type.
 */
public abstract class AbstractProblemInteger extends AbstractOptimizationProblem implements java.io.Serializable {

    protected AbstractEAIndividual bestIndividuum = null;
    protected int problemDimension = 10;

    public AbstractProblemInteger() {
        initTemplate();
    }

    protected void initTemplate() {
        if (template == null) {
            template = new GIIndividualIntegerData();
        }
        if (((InterfaceDataTypeInteger) this.template).size() != this.getProblemDimension()) {
            ((InterfaceDataTypeInteger) this.template).setIntegerDataLength(this.getProblemDimension());
        }
    }

    public void cloneObjects(AbstractProblemInteger o) {
        if (o.template != null) {
            template = (AbstractEAIndividual) o.template.clone();
        }
        if (o.bestIndividuum != null) {
            bestIndividuum = (AbstractEAIndividual) o.bestIndividuum.clone();
        }
        this.problemDimension = o.problemDimension;
    }

    @Override
    public void initializeProblem() {
        initTemplate();
        this.bestIndividuum = null;
    }

    @Override
    public void initializePopulation(Population population) {
        this.bestIndividuum = null;
        ((InterfaceDataTypeInteger) this.template).setIntegerDataLength(this.problemDimension);
        AbstractOptimizationProblem.defaultInitializePopulation(population, template, this);
    }

    @Override
    public void evaluate(AbstractEAIndividual individual) {
        int[] x;
        double[] fitness;


        x = new int[((InterfaceDataTypeInteger) individual).getIntegerData().length];
        System.arraycopy(((InterfaceDataTypeInteger) individual).getIntegerData(), 0, x, 0, x.length);

        fitness = this.evaluate(x);
        for (int i = 0; i < fitness.length; i++) {
            // set the fitness of the individual
            individual.SetFitness(i, fitness[i]);
        }
        if ((this.bestIndividuum == null) || (this.bestIndividuum.getFitness(0) > individual.getFitness(0))) {
            this.bestIndividuum = (AbstractEAIndividual) individual.clone();
        }
    }

    /**
     * Evaluate a eva2.problems.simple integer array to determine the fitness.
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    public abstract double[] evaluate(int[] x);

    @Override
    public String getName() {
        return "AbstractProblemInteger";
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("An integer valued problem:\n");
        sb.append("Dimension   : ");
        sb.append(this.getProblemDimension());
        return sb.toString();
    }


    /**
     * This method allows you to set the problem dimension.
     *
     * @param n The problem dimension
     */
    public void setProblemDimension(int n) {
        this.problemDimension = n;
    }

    /**
     * This method allows you to choose the EA individual
     *
     * @param indy The EAIndividual type
     */
    public void setIndividualTemplate(AbstractEAIndividual indy) {
        this.template = indy;
    }
}