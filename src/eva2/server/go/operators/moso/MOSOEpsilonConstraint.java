package eva2.server.go.operators.moso;

import eva2.gui.PropertyEpsilonConstraint;
import eva2.gui.PropertyEpsilonThreshold;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.07.2005
 * Time: 16:13:17
 * To change this template use File | Settings | File Templates.
 */
public class MOSOEpsilonConstraint implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyEpsilonConstraint   m_EpsilonConstraint   = null;

    public MOSOEpsilonConstraint() {
        this.m_EpsilonConstraint = new PropertyEpsilonConstraint();
        this.m_EpsilonConstraint.m_OptimizeObjective = 0;
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) tmpD[i] = 0.0;
        this.m_EpsilonConstraint.m_TargetValue = tmpD;
    }

    public MOSOEpsilonConstraint(MOSOEpsilonConstraint b) {
        if (b.m_EpsilonConstraint != null)
            this.m_EpsilonConstraint = (PropertyEpsilonConstraint)b.m_EpsilonConstraint.clone();
    }
    public Object clone() {
        return (Object) new MOSOEpsilonConstraint(this);
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
        resultFit[0] = tmpFit[this.m_EpsilonConstraint.m_OptimizeObjective];
        for (int i = 0; i < this.m_EpsilonConstraint.m_TargetValue.length; i++) {
            if (i != this.m_EpsilonConstraint.m_OptimizeObjective) {
                indy.addConstraintViolation(Math.max(0, tmpFit[i] - this.m_EpsilonConstraint.m_TargetValue[i]));
            }
        }
        indy.SetFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    public void setOutputDimension(int dim) {
        double[] newTarget = new double[dim];

        for (int i = 0; i < newTarget.length; i++) {
            newTarget[i] = 0;
        }
        for (int i = 0; (i < this.m_EpsilonConstraint.m_TargetValue.length) && (i < newTarget.length); i++) {
            newTarget[i] = this.m_EpsilonConstraint.m_TargetValue[i];
        }
        if (this.m_EpsilonConstraint.m_OptimizeObjective >= dim) this.m_EpsilonConstraint.m_OptimizeObjective = dim-1;

        this.m_EpsilonConstraint.m_TargetValue = newTarget;
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
        return "Epsilon Constraint";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method uses n-1 objected as hard constraints.";
    }

    /** This method allows you to choose the EpsilonThreshold
     * @param weights     The Epsilon Threshhold for the fitness sum.
     */
    public void setEpsilonThreshhold(PropertyEpsilonConstraint weights) {
        this.m_EpsilonConstraint = weights;
    }
    public PropertyEpsilonConstraint getEpsilonThreshhold() {
        return this.m_EpsilonConstraint;
    }
    public String epsilonThreshholdTipText() {
        return "Choose the epsilon constraints for the fitness sum.";
    }

}