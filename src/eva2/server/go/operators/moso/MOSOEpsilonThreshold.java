package eva2.server.go.operators.moso;

import eva2.gui.PropertyDoubleArray;
import eva2.gui.PropertyEpsilonThreshold;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 14:53:47
 * To change this template use File | Settings | File Templates.
 */
public class MOSOEpsilonThreshold implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyEpsilonThreshold   m_EpsilonThreshold   = null;

    public MOSOEpsilonThreshold() {
        this.m_EpsilonThreshold = new PropertyEpsilonThreshold();
        this.m_EpsilonThreshold.m_OptimizeObjective = 0;
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) tmpD[i] = 0.0;
        this.m_EpsilonThreshold.m_TargetValue = tmpD;
        tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) tmpD[i] = 1.0;
        this.m_EpsilonThreshold.m_Punishment = tmpD;
    }

    public MOSOEpsilonThreshold(MOSOEpsilonThreshold b) {
        if (b.m_EpsilonThreshold != null)
            this.m_EpsilonThreshold = (PropertyEpsilonThreshold)b.m_EpsilonThreshold.clone();
    }
    public Object clone() {
        return (Object) new MOSOEpsilonThreshold(this);
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
        for (int i = 0; i < tmpFit.length; i++) {
            if (new Double(tmpFit[i]).isNaN()) System.out.println("Fitness is NaN");
            if (new Double(tmpFit[i]).isInfinite()) System.out.println("Fitness is Infinite");
        }
        resultFit[0] = tmpFit[this.m_EpsilonThreshold.m_OptimizeObjective];

//        System.out.println("Optimize: " + this.m_EpsilonThreshold.m_OptimizeObjective);
//        for (int i = 0; i < tmpFit.length; i++) {
//            System.out.println("Target: " + this.m_EpsilonThreshold.m_TargetValue[i] + " Punish: " + this.m_EpsilonThreshold.m_Punishment[i]);
//        }

        for (int i = 0; i < this.m_EpsilonThreshold.m_Punishment.length; i++) {
            if (i != this.m_EpsilonThreshold.m_OptimizeObjective) {
                resultFit[0] += this.m_EpsilonThreshold.m_Punishment[i] * Math.max(0, tmpFit[i] - this.m_EpsilonThreshold.m_TargetValue[i]);
            }
        }
        tmpFit = (double[]) indy.getData("MOFitness");
        for (int i = 0; i < tmpFit.length; i++) {
            if (new Double(tmpFit[i]).isNaN()) System.out.println("-Fitness is NaN");
            if (new Double(tmpFit[i]).isInfinite()) System.out.println("-Fitness is Infinite");
        }
        indy.SetFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    public void setOutputDimension(int dim) {
        double[] newPunish = new double[dim];
        double[] newTarget = new double[dim];

        for (int i = 0; i < newPunish.length; i++) {
            newPunish[i] = 1;
            newTarget[i] = 0;
        }
        for (int i = 0; (i < this.m_EpsilonThreshold.m_Punishment.length) && (i < newTarget.length); i++) {
            newPunish[i] = this.m_EpsilonThreshold.m_Punishment[i];
            newTarget[i] = this.m_EpsilonThreshold.m_TargetValue[i];
        }
        if (this.m_EpsilonThreshold.m_OptimizeObjective >= dim) this.m_EpsilonThreshold.m_OptimizeObjective = dim-1;

        this.m_EpsilonThreshold.m_Punishment = newPunish;
        this.m_EpsilonThreshold.m_TargetValue = newTarget;
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
        return "Epsilon Threshold";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method uses n-1 objected as soft constraints.";
    }

    /** This method allows you to choose the EpsilonThreshold
     * @param weights     The Epsilon Threshhold for the fitness sum.
     */
    public void setEpsilonThreshhold(PropertyEpsilonThreshold weights) {
        this.m_EpsilonThreshold = weights;
    }
    public PropertyEpsilonThreshold getEpsilonThreshhold() {
        return this.m_EpsilonThreshold;
    }
    public String epsilonThreshholdTipText() {
        return "Choose the epsilon thresholds for the fitness sum.";
    }

}
