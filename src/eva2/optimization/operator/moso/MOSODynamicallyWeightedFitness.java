package eva2.optimization.operator.moso;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 *
 */
public class MOSODynamicallyWeightedFitness implements InterfaceMOSOConverter, java.io.Serializable {

    private double f = 50;
    private int currentGeneration = 0;
    private int outputDimension = 2;

    public MOSODynamicallyWeightedFitness() {
    }

    public MOSODynamicallyWeightedFitness(MOSODynamicallyWeightedFitness b) {
        this.currentGeneration = b.currentGeneration;
        this.f = b.f;
        this.outputDimension = b.outputDimension;
    }

    @Override
    public Object clone() {
        return (Object) new MOSODynamicallyWeightedFitness(this);
    }

    /**
     * This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     *
     * @param pop The population to process.
     */
    @Override
    public void convertMultiObjective2SingleObjective(Population pop) {
        this.currentGeneration = pop.getGeneration();
        for (int i = 0; i < pop.size(); i++) {
            this.convertSingleIndividual((AbstractEAIndividual) pop.get(i));
        }
    }

    /**
     * This method processes a single individual
     *
     * @param indy The individual to process.
     */
    @Override
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[] resultFit = new double[1];
        double[] tmpFit;
        double[] weights;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        weights = new double[tmpFit.length];

        // calculate the dynamic weights
        weights[0] = Math.pow(Math.sin(2 * Math.PI * (double) this.currentGeneration / this.f), 2);
        weights[1] = 1 - weights[0];

        for (int i = 0; (i < 2) && (i < tmpFit.length); i++) {
            resultFit[0] += tmpFit[i] * weights[i];
        }
        indy.setFitness(resultFit);
    }

    /**
     * This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     *
     * @param dim Outputdimension of the problem
     */
    @Override
    public void setOutputDimension(int dim) {
        this.outputDimension = dim;
        // i think as far as i got not solution for the (n>2) dimensional case
        // i could simply ignore this....
    }

    /**
     * This method returns a description of the objective
     *
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        return this.getName() + "\n";
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
        return "Dynamic Weighted Sum";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This method calcuates a dynamic weighted sum over TWO fitness values depending on the current generation.";
    }

    /**
     * This method allows you to choose the frequency for the change of the weights.
     *
     * @param f The frequency of change.
     */
    public void setF(double f) {
        this.f = f;
    }

    public double getF() {
        return this.f;
    }

    public String fTipText() {
        return "Choose the frequency for the fitness value.";
    }

}
