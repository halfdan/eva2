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
public class MOSOGoalProgramming implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyDoubleArray    m_Goals = null;

    public MOSOGoalProgramming() {
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) tmpD[i] = 0.0;
        this.m_Goals = new PropertyDoubleArray(tmpD);
    }
    public MOSOGoalProgramming(MOSOGoalProgramming b) {
        if (b.m_Goals != null) {
            this.m_Goals = (PropertyDoubleArray)b.m_Goals;
        }
    }
    public Object clone() {
        return (Object) new MOSOGoalProgramming(this);
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
        indy.putData("MOFitness", tmpFit);
        resultFit[0] = 0;
        for (int i = 0; (i < this.m_Goals.m_DoubleArray.length) && (i < tmpFit.length) ; i++)
            resultFit[0] += tmpFit[i]-this.m_Goals.m_DoubleArray[i];
        indy.SetFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    public void setOutputDimension(int dim) {
        double[] newWeights = new double[dim];

        for (int i = 0; i < newWeights.length; i++) newWeights[i] = 0.0;
        for (int i = 0; (i < this.m_Goals.m_DoubleArray.length) && (i < newWeights.length); i++) newWeights[i] = this.m_Goals.m_DoubleArray[i];

        this.m_Goals.setDoubleArray(newWeights);
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
        return "Goal Programming";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method minimizes the delta to a given target fitness values.";
    }

    /** This method allows you to choose the goals for goal programming
     * @param goals     The weights for the fitness sum.
     */
    public void setGoals(PropertyDoubleArray goals) {
        this.m_Goals = goals;
    }
    public PropertyDoubleArray getGoals() {
        return this.m_Goals;
    }
    public String goalsTipText() {
        return "Choose the goals for the fitness values.";
    }

}
