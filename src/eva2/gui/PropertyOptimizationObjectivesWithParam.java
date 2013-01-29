package eva2.gui;

import eva2.server.go.problems.InterfaceOptimizationObjective;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.01.2005
 * Time: 13:35:24
 * To change this template use File | Settings | File Templates.
 */
public class PropertyOptimizationObjectivesWithParam implements java.io.Serializable {

    public InterfaceOptimizationObjective[]    m_AvailableObjectives;
    public InterfaceOptimizationObjective[]    m_SelectedObjectives;
    public double[]                         m_Weights;
    public String                           m_DescriptiveString     = "No Description given.";
    public String                           m_WeightsLabel          = "-";
    public boolean                          m_NormalizationEnabled  = true;

    public PropertyOptimizationObjectivesWithParam(InterfaceOptimizationObjective[] d) {
        this.m_AvailableObjectives = d;
        this.m_SelectedObjectives  = null;
    }
    public PropertyOptimizationObjectivesWithParam(PropertyOptimizationObjectivesWithParam d) {
        this.m_DescriptiveString        = d.m_DescriptiveString;
        this.m_WeightsLabel             = d.m_WeightsLabel;
        this.m_NormalizationEnabled      = d.m_NormalizationEnabled;
        this.m_AvailableObjectives         = new InterfaceOptimizationObjective[d.m_AvailableObjectives.length];
        for (int i = 0; i < this.m_AvailableObjectives.length; i++) {
            this.m_AvailableObjectives[i]  = (InterfaceOptimizationObjective)d.m_AvailableObjectives[i].clone();
        }
        this.m_SelectedObjectives          = new InterfaceOptimizationObjective[d.m_SelectedObjectives.length];
        for (int i = 0; i < this.m_SelectedObjectives.length; i++) {
            this.m_SelectedObjectives[i]   = (InterfaceOptimizationObjective)d.m_SelectedObjectives[i].clone();
        }
        if (d.m_Weights != null) {
            this.m_Weights = new double[d.m_Weights.length];
            System.arraycopy(d.m_Weights, 0, this.m_Weights, 0, this.m_Weights.length);
        }
    }

    @Override
    public Object clone() {
        return (Object) new PropertyOptimizationObjectivesWithParam(this);
    }

    /** This method will allow you to set the value of the InterfaceOptimizationTarget array
     * @param d     The InterfaceOptimizationTarget[]
     */
    public void setSelectedTargets(InterfaceOptimizationObjective[] d) {
        this.m_SelectedObjectives = d;

        if (this.m_Weights == null) {
            this.m_Weights = new double[d.length];
            for (int i = 0; i < this.m_Weights.length; i++) {
                this.m_Weights[i] = 1.0;
            }
            return;
        }

        if (d.length == this.m_Weights.length) return;
        
        if (d.length > this.m_Weights.length) {
            double[] newWeights = new double[d.length];
            for (int i = 0; i < this.m_Weights.length; i++) {
                newWeights[i] = this.m_Weights[i];
            }
            this.m_Weights = newWeights;
        } else {
            double[] newWeights = new double[d.length];
            for (int i = 0; i < d.length; i++) {
                newWeights[i] = this.m_Weights[i];
            }
            this.m_Weights = newWeights;
        }
    }

    /** This method will return the InterfaceOptimizationTarget array
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceOptimizationObjective[] getSelectedTargets() {
        return this.m_SelectedObjectives;
    }

    /** This method will return the InterfaceOptimizationTarget array
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceOptimizationObjective[] getAvailableTargets() {
        return this.m_AvailableObjectives;
    }

    /** This method allows you to read the weights
     * @return the weights
     */
    public double[] getWeights() {
        return this.m_Weights;
    }
    public void setWeights(double[] d) {
        this.m_Weights = d;
    }

    /** This method allows you to set/get the descriptive string
     * @return the string
     */
    public String getDescriptiveString() {
        return this.m_DescriptiveString;
    }
    public void setDescriptiveString(String d) {
        this.m_DescriptiveString = d;
    }

    /** This method allows you to set/get the weights label
     * @return the string
     */
    public String getWeigthsLabel() {
        return this.m_WeightsLabel;
    }
    public void setWeightsLabel(String d) {
        this.m_WeightsLabel = d;
    }

    /** This method allows you to set/get the weights label
     * @return the string
     */
    public boolean isNormalizationEnabled() {
        return this.m_NormalizationEnabled;
    }
    public void enableNormalization(boolean d) {
        this.m_NormalizationEnabled = d;
    }

    /** This method allows you to remove a Target from the list
     * @param index     The index of the target to be removed.
     */
    public void removeTarget(int index) {
        if ((index < 0) || (index >= this.m_SelectedObjectives.length)) return;

        InterfaceOptimizationObjective[]   newList = new InterfaceOptimizationObjective[this.m_SelectedObjectives.length-1];
        double[]                        newWeights = new double[this.m_Weights.length - 1];
        int j = 0;
        for (int i = 0; i < this.m_SelectedObjectives.length; i++) {
            if (index != i) {
                newList[j] = this.m_SelectedObjectives[i];
                newWeights[j] = this.m_Weights[i];
                j++;
            }
        }
        this.m_SelectedObjectives  = newList;
        this.m_Weights          = newWeights;
    }

    /** This method allows you to add a new target to the list
     * @param optTarget
     */
    public void addTarget(InterfaceOptimizationObjective optTarget) {
        InterfaceOptimizationObjective[]   newList = new InterfaceOptimizationObjective[this.m_SelectedObjectives.length+1];
        double[]                        newWeights = new double[this.m_Weights.length + 1];
        for (int i = 0; i < this.m_SelectedObjectives.length; i++) {
            newList[i] = this.m_SelectedObjectives[i];
            newWeights[i] = this.m_Weights[i];
        }
        newList[this.m_SelectedObjectives.length] = optTarget;
        newWeights[this.m_SelectedObjectives.length] = 1.0;
        this.m_SelectedObjectives  = newList;
        this.m_Weights          = newWeights;
    }
}