package eva2.optimization.operator.moso;

import eva2.gui.PropertyWeightedLPTchebycheff;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.07.2005
 * Time: 10:12:28
 * To change this template use File | Settings | File Templates.
 */
public class MOSOWeightedLPTchebycheff implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyWeightedLPTchebycheff m_WLPT = null;

    public MOSOWeightedLPTchebycheff() {
        this.m_WLPT = new PropertyWeightedLPTchebycheff();
        this.m_WLPT.m_P = 0;
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 0.0;
        }
        this.m_WLPT.m_IdealValue = tmpD;
        tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 1.0;
        }
        this.m_WLPT.m_Weights = tmpD;
    }

    public MOSOWeightedLPTchebycheff(MOSOWeightedLPTchebycheff b) {
        if (b.m_WLPT != null) {
            this.m_WLPT = (PropertyWeightedLPTchebycheff) b.m_WLPT.clone();
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
            if (this.m_WLPT.m_P == 0) {
                resultFit[0] = Math.max(resultFit[0], this.m_WLPT.m_Weights[i] * Math.abs(tmpFit[i] - this.m_WLPT.m_IdealValue[i]));
            } else {
                resultFit[0] += this.m_WLPT.m_Weights[i] * Math.pow(tmpFit[i] - this.m_WLPT.m_IdealValue[i], this.m_WLPT.m_P);
            }
        }
        if (this.m_WLPT.m_P > 0) {
            resultFit[0] = Math.pow(resultFit[0], 1 / ((double) this.m_WLPT.m_P));
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
        for (int i = 0; (i < this.m_WLPT.m_IdealValue.length) && (i < newTarget.length); i++) {
            newTarget[i] = this.m_WLPT.m_IdealValue[i];
            newWeights[i] = this.m_WLPT.m_Weights[i];
        }
        this.m_WLPT.m_IdealValue = newTarget;
        this.m_WLPT.m_Weights = newWeights;
    }

    /**
     * This method returns a description of the objective
     *
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        String result = "Lp Metric\n";
        result += " P           = " + this.m_WLPT.m_P + "\n";
        result += " Ref.Fitness = (";
        double[] p = this.m_WLPT.m_IdealValue;
        for (int i = 0; i < p.length; i++) {
            result += p[i];
            if (i < (p.length - 1)) {
                result += "; ";
            }
        }
        result += ")\n";
        result += " Weights     = (";
        p = this.m_WLPT.m_Weights;
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
        this.m_WLPT = weights;
    }

    public PropertyWeightedLPTchebycheff getIdealPWeights() {
        return this.m_WLPT;
    }

    public String idealPWeightsTipText() {
        return "Set the ideal vector, the weights and p (note: p=0 equals Tchebycheff metric).";
    }

}