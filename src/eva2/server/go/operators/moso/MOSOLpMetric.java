package eva2.server.go.operators.moso;

import eva2.gui.PropertyDoubleArray;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 15:49:16
 * To change this template use File | Settings | File Templates.
 */
public class MOSOLpMetric implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyDoubleArray     m_Reference = null;
    private int                     m_P         = 2;

    public MOSOLpMetric() {
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) tmpD[i] = 0.0;
        this.m_Reference = new PropertyDoubleArray(tmpD);
    }
    public MOSOLpMetric(MOSOLpMetric b) {
        this.m_P = b.m_P;
        if (b.m_Reference != null) {
            this.m_Reference = (PropertyDoubleArray)b.m_Reference.clone();
        }
    }
    @Override
    public Object clone() {
        return (Object) new MOSOLpMetric(this);
    }

    /** This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     * @param pop       The population to process.
     */
    @Override
    public void convertMultiObjective2SingleObjective(Population pop) {
        for (int i = 0; i < pop.size(); i++) {
             this.convertSingleIndividual((AbstractEAIndividual)pop.get(i));
        }
    }

    /** This method processes a single individual
     * @param indy      The individual to process.
     */
    @Override
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[]    resultFit = new double[1];
        double[]    tmpFit;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        if (m_P >= 1) {
            // standard Lp Metric
            resultFit[0] = 0;
            for (int i = 0; (i < this.m_Reference.getNumRows()) && (i < tmpFit.length); i++) {
                 resultFit[0] += Math.pow(Math.abs(tmpFit[i]-this.m_Reference.getValue(i,0)), this.m_P);
            }
            resultFit[0] = Math.pow(resultFit[0], 1/((double)this.m_P));
        } else {
            // Tchebycheff metric
            resultFit[0] = Double.NEGATIVE_INFINITY;
            for (int i = 0; (i < this.m_Reference.getNumRows()) && (i < tmpFit.length); i++) {
                 resultFit[0] += Math.max(Math.abs(tmpFit[i]-this.m_Reference.getValue(i,0)), resultFit[0]);
            }
        }

        indy.setFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    @Override
    public void setOutputDimension(int dim) {
        double[] newWeights = new double[dim];

        for (int i = 0; i < newWeights.length; i++) newWeights[i] = 0.0;
        for (int i = 0; (i < this.m_Reference.getNumRows()) && (i < newWeights.length); i++) newWeights[i] = this.m_Reference.getValue(i,0);

        this.m_Reference.setDoubleArray(newWeights);
    }

    /** This method returns a description of the objective
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        String result = "Lp Metric\n";
        result += " P           = "+this.m_P+"\n";
        result += " Ref.Fitness = (";
        for (int i = 0; i < m_Reference.getNumRows(); i++) {
            result += m_Reference.getValue(i,0);
            if (i < (m_Reference.getNumRows()-1)) result += "; ";
        }
        result += ")\n";
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
        return "Lp Metric";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method minimizes the Lp metric to a given target fitness values, for (p<1) this equals the Tchebycheff metric.";
    }

    /** This method allows you to choose the reference for the Lp Metric
     * @param reference     The reference vector.
     */
    public void setReference(PropertyDoubleArray reference) {
        this.m_Reference = reference;
    }
    public PropertyDoubleArray getReference() {
        return this.m_Reference;
    }
    public String referenceTipText() {
        return "Choose the reference for the fitness values.";
    }

    /** This method allows you to choose the p for the Lp Metric
     * @param p
     */
    public void setP(int p) {
        this.m_P = Math.max(1, p);
    }
    public int getP() {
        return this.m_P;
    }
    public String pTipText() {
        return "Choose the exponent p for the Lp metric, if (p<1) this results in the Tchebycheff metric.";
    }

}
