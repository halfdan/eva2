package eva2.optimization.operator.moso;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.06.2005
 * Time: 13:44:20
 * To change this template use File | Settings | File Templates.
 */
public class MOSORandomWeight implements InterfaceMOSOConverter, java.io.Serializable {

    public MOSORandomWeight() {
    }

    public MOSORandomWeight(MOSORandomWeight b) {
    }

    @Override
    public Object clone() {
        return new MOSORandomWeight(this);
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
        double[] tmpFit, tmpWeight;
        double sum = 0;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        tmpWeight = new double[tmpFit.length];
        for (int i = 0; i < tmpWeight.length; i++) {
            tmpWeight[i] = RNG.randomDouble(0, 1);
            sum += tmpWeight[i];
        }
        if (sum <= 0.0000001) {
            for (int i = 0; i < tmpWeight.length; i++) {
                tmpWeight[i] = 1 / (double) tmpWeight.length;
            }
        } else {
            for (int i = 0; i < tmpWeight.length; i++) {
                tmpWeight[i] /= sum;
            }
        }
        for (int i = 0; (i < tmpWeight.length) && (i < tmpFit.length); i++) {
            resultFit[0] += tmpFit[i] * tmpWeight[i];
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
        return "Randomly Weighted Sum";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This method calcuates a randomly weighted sum over all fitness values [Murata95MOGA].";
    }
}