package eva2.optimization.operator.moso;

import eva2.gui.PropertyDoubleArray;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This method calcuates the weighted sum over all fitness values.")
public class MOSOWeightedFitness implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyDoubleArray weights = null;

    public MOSOWeightedFitness() {
        double[][] tmpD = new double[2][1];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i][0] = 1.0;
        }
        this.weights = new PropertyDoubleArray(tmpD);
        for (int i = 0; i < this.weights.getNumRows(); i++) {
            this.weights.normalizeColumns();
        }
    }

    public MOSOWeightedFitness(double[][] weights) {
        this();
        setWeights(new PropertyDoubleArray(weights));
    }

    public MOSOWeightedFitness(MOSOWeightedFitness b) {
        if (b.weights != null) {
            this.weights = b.weights;
        }
    }

    @Override
    public Object clone() {
        return new MOSOWeightedFitness(this);
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
        for (int i = 0; (i < this.weights.getNumRows()) && (i < tmpFit.length); i++) {
            resultFit[0] += tmpFit[i] * this.weights.getValue(i, 0);
        }
        indy.setFitness(resultFit);
    }

    private void checkingWeights() {
        String s = "Using Weights: {";
        for (int i = 0; i < this.weights.getNumRows(); i++) {
            s += this.weights.getValue(i, 0);
            if (i < this.weights.getNumRows() - 1) {
                s += "; ";
            }
        }
        System.out.println(s + "}");
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
        double[] newWeights = new double[dim];

        for (int i = 0; i < newWeights.length; i++) {
            newWeights[i] = 1;
        }
        for (int i = 0; (i < this.weights.getNumRows()) && (i < newWeights.length); i++) {
            newWeights[i] = this.weights.getValue(i, 0);
        }

        this.weights.setDoubleArray(newWeights);
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
        return "Weighted Sum";
    }

    /**
     * This method allows you to choose the weights for the weighted
     * fitness sum.
     *
     * @param weights The weights for the fitness sum.
     */
    public void setWeights(PropertyDoubleArray weights) {
        this.weights = weights;
    }

    public PropertyDoubleArray getWeights() {
        return this.weights;
    }

    public String weightsTipText() {
        return "Choose the weights for the fitness values.";
    }

}
