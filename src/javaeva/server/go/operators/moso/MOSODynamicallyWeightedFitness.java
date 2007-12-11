package javaeva.server.go.operators.moso;

import javaeva.gui.PropertyDoubleArray;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.03.2004
 * Time: 18:59:14
 * To change this template use File | Settings | File Templates.
 */
public class MOSODynamicallyWeightedFitness implements InterfaceMOSOConverter, java.io.Serializable {

    private double          m_F                 = 50;
    private int             m_CurrentGeneration = 0;
    private int             m_OutputDimension   = 2;

    public MOSODynamicallyWeightedFitness() {
    }
    public MOSODynamicallyWeightedFitness(MOSODynamicallyWeightedFitness b) {
        this.m_CurrentGeneration        = b.m_CurrentGeneration;
        this.m_F                        = b.m_F;
        this.m_OutputDimension          = b.m_OutputDimension;
    }
    public Object clone() {
        return (Object) new MOSODynamicallyWeightedFitness(this);
    }

    /** This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     * @param pop       The population to process.
     */
    public void convertMultiObjective2SingleObjective(Population pop) {
        this.m_CurrentGeneration = pop.getGeneration();
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
        double[]    weights;

        tmpFit = indy.getFitness();
        indy.SetData("MOFitness", tmpFit);
        weights = new double[tmpFit.length];

        // calculate the dynamic weights
        weights[0] = Math.pow(Math.sin(2*Math.PI*(double)this.m_CurrentGeneration/this.m_F), 2);
        weights[1] = 1 - weights[0];

        for (int i = 0; (i <2) && (i < tmpFit.length); i++)
            resultFit[0] += tmpFit[i]*weights[i];
        indy.SetFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    public void setOutputDimension(int dim) {
        this.m_OutputDimension = dim;
        // i think as far as i got not solution for the (n>2) dimensional case
        // i could simply ignore this....
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
        return "Dynamic Weighted Sum";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method calcuates a dynamic weighted sum over TWO fitness values depending on the current generation.";
    }

    /** This method allows you to choose the frequency for the change of the weights.
     * @param f     The frequency of change.
     */
    public void setF(double f) {
        this.m_F = f;
    }
    public double getF() {
        return this.m_F;
    }
    public String fTipText() {
        return "Choose the frequency for the fitness value.";
    }

}
