package eva2.server.go.operators.moso;

import eva2.gui.PropertyDoubleArray;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 10:48:39
 * To change this template use File | Settings | File Templates.
 */
public class MOSOWeightedFitness implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyDoubleArray    m_Weights = null;

    public MOSOWeightedFitness() {
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) tmpD[i] = 1.0;
        this.m_Weights = new PropertyDoubleArray(tmpD);
        for (int i = 0; i < this.m_Weights.m_DoubleArray.length; i++) this.m_Weights.m_DoubleArray[i] = 1/((double)this.m_Weights.m_DoubleArray.length);
    }
    public MOSOWeightedFitness(MOSOWeightedFitness b) {
        if (b.m_Weights != null) {
            this.m_Weights = (PropertyDoubleArray)b.m_Weights;
        }
    }
    public Object clone() {
        return (Object) new MOSOWeightedFitness(this);
    }

    /** This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     * @param pop       The population to process.
     */
    public void convertMultiObjective2SingleObjective(Population pop) {
        for (int i = 0; i < pop.size(); i++) {
             this.convertSingleIndividual((AbstractEAIndividual)pop.get(i));
        }
    }

    /** This method processes a single individual
     * @param indy      The individual to process.
     */
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[]    resultFit = new double[1];
        double[]    tmpFit;

        tmpFit = indy.getFitness();
        indy.SetData("MOFitness", tmpFit);
        for (int i = 0; (i < this.m_Weights.m_DoubleArray.length) && (i < tmpFit.length) ; i++)
            resultFit[0] += tmpFit[i]*this.m_Weights.m_DoubleArray[i];
        indy.SetFitness(resultFit);
    }

    private void checkingWeights() {
        String s = "Using Weights: {";
        for (int i = 0; i < this.m_Weights.m_DoubleArray.length; i++) {
            s += this.m_Weights.m_DoubleArray[i];
            if (i < this.m_Weights.m_DoubleArray.length-1) s+= "; ";
        }
        System.out.println(s+"}");
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    public void setOutputDimension(int dim) {
        double[] newWeights = new double[dim];

        for (int i = 0; i < newWeights.length; i++) newWeights[i] = 1;
        for (int i = 0; (i < this.m_Weights.m_DoubleArray.length) && (i < newWeights.length); i++) newWeights[i] = this.m_Weights.m_DoubleArray[i];

        this.m_Weights.setDoubleArray(newWeights);
    }

    /** This method returns a description of the objective
     * @return A String
     */
    public String getStringRepresentation() {
        return this.getName()+"\n";
    }
    

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Weighted Sum";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method calcuates the weighted sum over all fitness values.";
    }

    /** This method allows you to choose the weights for the weighted
     * fitness sum.
     * @param weights     The weights for the fitness sum.
     */
    public void setWeights(PropertyDoubleArray weights) {
        this.m_Weights = weights;
    }
    public PropertyDoubleArray getWeights() {
        return this.m_Weights;
    }
    public String weightsTipText() {
        return "Choose the weights for the fitness values.";
    }

}
