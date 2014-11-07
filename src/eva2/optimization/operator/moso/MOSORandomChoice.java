package eva2.optimization.operator.moso;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This method selects a random fitness value, actually this implements VEGA [Schaffer84Experiments].")
public class MOSORandomChoice implements InterfaceMOSOConverter, java.io.Serializable {

    private int outputDimension = 2;

    public MOSORandomChoice() {
    }

    public MOSORandomChoice(MOSORandomChoice b) {
        this.outputDimension = b.outputDimension;
    }

    @Override
    public Object clone() {
        return new MOSORandomChoice(this);
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
            this.convertSingleIndividual(pop.get(i));
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
        resultFit[0] = tmpFit[RNG.randomInt(0, tmpFit.length - 1)];
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

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Random Choice";
    }
}