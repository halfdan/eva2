package eva2.optimization.operator.moso;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This method calculate the maximum of minimum distance over all criterias over all individuals.")
public class MOSOMaxiMin implements InterfaceMOSOConverter, java.io.Serializable {

    private int outputDimension = 2;

    public MOSOMaxiMin() {
    }

    public MOSOMaxiMin(MOSOMaxiMin b) {
        this.outputDimension = b.outputDimension;
    }

    @Override
    public Object clone() {
        return new MOSOMaxiMin(this);
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
        AbstractEAIndividual tmpIndy;
        double[][] fitnessArray, minArray;
        double[] result, tmpFit, resultFit;
        double tmpResult;

        tmpIndy = pop.get(0);
        fitnessArray = new double[pop.size()][tmpIndy.getFitness().length];
        minArray = new double[pop.size()][tmpIndy.getFitness().length];
        result = new double[pop.size()];
        resultFit = new double[1];
        for (int i = 0; i < pop.size(); i++) {
            fitnessArray[i] = pop.get(i).getFitness();
        }
        for (int i = 0; i < fitnessArray.length; i++) {
            result[i] = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < fitnessArray.length; k++) {
                if (i != k) {
                    tmpResult = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < fitnessArray[k].length; j++) {
                        tmpResult = Math.min(tmpResult, fitnessArray[i][j] - fitnessArray[k][j]);
                    }
                    result[i] = Math.max(result[i], tmpResult);
                }
            }
            // write the result to the individuals
            tmpIndy = pop.get(i);
            tmpFit = tmpIndy.getFitness();
            tmpIndy.putData("MOFitness", tmpFit);
            resultFit = new double[1];
            resultFit[0] = result[i];
            tmpIndy.setFitness(resultFit);
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

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        System.err.println("The MaxiMin MOSO can not be applied to single individuals! I default to random criterion.");
        resultFit[0] = tmpFit[RNG.randomInt(0, tmpFit.length)];
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

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "MaxiMin Criterium";
    }
}