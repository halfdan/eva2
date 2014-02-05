package eva2.optimization.operator.moso;

import eva2.gui.PropertyWeightedLPTchebycheff;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 *
 */
public class MOSOWeightedLPTchebycheff implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyWeightedLPTchebycheff weightedLPTchebycheff = null;

    public MOSOWeightedLPTchebycheff() {
        this.weightedLPTchebycheff = new PropertyWeightedLPTchebycheff();
        this.weightedLPTchebycheff.p = 0;
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 0.0;
        }
        this.weightedLPTchebycheff.idealValue = tmpD;
        tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 1.0;
        }
        this.weightedLPTchebycheff.weights = tmpD;
    }

    public MOSOWeightedLPTchebycheff(MOSOWeightedLPTchebycheff b) {
        if (b.weightedLPTchebycheff != null) {
            this.weightedLPTchebycheff = (PropertyWeightedLPTchebycheff) b.weightedLPTchebycheff.clone();
        }
    }

    @Override
    public Object clone() {
        return (Object) new MOSOWeightedLPTchebycheff(this);
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
        double[] tmpFit;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        resultFit[0] = 0;
        for (int i = 0; i < tmpFit.length; i++) {
            if (this.weightedLPTchebycheff.p == 0) {
                resultFit[0] = Math.max(resultFit[0], this.weightedLPTchebycheff.weights[i] * Math.abs(tmpFit[i] - this.weightedLPTchebycheff.idealValue[i]));
            } else {
                resultFit[0] += this.weightedLPTchebycheff.weights[i] * Math.pow(tmpFit[i] - this.weightedLPTchebycheff.idealValue[i], this.weightedLPTchebycheff.p);
            }
        }
        if (this.weightedLPTchebycheff.p > 0) {
            resultFit[0] = Math.pow(resultFit[0], 1 / ((double) this.weightedLPTchebycheff.p));
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
        double[] newTarget = new double[dim];
        double[] newWeights = new double[dim];

        for (int i = 0; i < newTarget.length; i++) {
            newTarget[i] = 0;
            newWeights[i] = 1.0;
        }
        for (int i = 0; (i < this.weightedLPTchebycheff.idealValue.length) && (i < newTarget.length); i++) {
            newTarget[i] = this.weightedLPTchebycheff.idealValue[i];
            newWeights[i] = this.weightedLPTchebycheff.weights[i];
        }
        this.weightedLPTchebycheff.idealValue = newTarget;
        this.weightedLPTchebycheff.weights = newWeights;
    }

    /**
     * This method returns a description of the objective
     *
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        String result = "Lp Metric\n";
        result += " P           = " + this.weightedLPTchebycheff.p + "\n";
        result += " Ref.Fitness = (";
        double[] p = this.weightedLPTchebycheff.idealValue;
        for (int i = 0; i < p.length; i++) {
            result += p[i];
            if (i < (p.length - 1)) {
                result += "; ";
            }
        }
        result += ")\n";
        result += " Weights     = (";
        p = this.weightedLPTchebycheff.weights;
        for (int i = 0; i < p.length; i++) {
            result += p[i];
            if (i < (p.length - 1)) {
                result += "; ";
            }
        }
        result += ")\n";
        return result;
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
        return "Lp/Tchebycheff";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This method implements the Lp-problem and the Tchebycheff metric, the weighted version is also known as compromise programming.";
    }

    /**
     * This method allows you to choose the EpsilonThreshold
     *
     * @param weights The Epsilon Threshhold for the fitness sum.
     */
    public void setIdealPWeights(PropertyWeightedLPTchebycheff weights) {
        this.weightedLPTchebycheff = weights;
    }

    public PropertyWeightedLPTchebycheff getIdealPWeights() {
        return this.weightedLPTchebycheff;
    }

    public String idealPWeightsTipText() {
        return "Set the ideal vector, the weights and p (note: p=0 equals Tchebycheff metric).";
    }

}