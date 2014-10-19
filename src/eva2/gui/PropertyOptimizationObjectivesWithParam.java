package eva2.gui;

import eva2.problems.InterfaceOptimizationObjective;

/**
 *
 */
public class PropertyOptimizationObjectivesWithParam implements java.io.Serializable {

    public InterfaceOptimizationObjective[] availableObjectives;
    public InterfaceOptimizationObjective[] selectedObjectives;
    public double[] weights;
    public String descriptiveString = "No Description given.";
    public String weightsLabel = "-";
    public boolean normalizationEnabled = true;

    public PropertyOptimizationObjectivesWithParam(InterfaceOptimizationObjective[] d) {
        this.availableObjectives = d;
        this.selectedObjectives = null;
    }

    public PropertyOptimizationObjectivesWithParam(PropertyOptimizationObjectivesWithParam d) {
        this.descriptiveString = d.descriptiveString;
        this.weightsLabel = d.weightsLabel;
        this.normalizationEnabled = d.normalizationEnabled;
        this.availableObjectives = new InterfaceOptimizationObjective[d.availableObjectives.length];
        for (int i = 0; i < this.availableObjectives.length; i++) {
            this.availableObjectives[i] = (InterfaceOptimizationObjective) d.availableObjectives[i].clone();
        }
        this.selectedObjectives = new InterfaceOptimizationObjective[d.selectedObjectives.length];
        for (int i = 0; i < this.selectedObjectives.length; i++) {
            this.selectedObjectives[i] = (InterfaceOptimizationObjective) d.selectedObjectives[i].clone();
        }
        if (d.weights != null) {
            this.weights = new double[d.weights.length];
            System.arraycopy(d.weights, 0, this.weights, 0, this.weights.length);
        }
    }

    @Override
    public Object clone() {
        return new PropertyOptimizationObjectivesWithParam(this);
    }

    /**
     * This method will allow you to set the value of the InterfaceOptimizationTarget array
     *
     * @param d The InterfaceOptimizationTarget[]
     */
    public void setSelectedTargets(InterfaceOptimizationObjective[] d) {
        this.selectedObjectives = d;

        if (this.weights == null) {
            this.weights = new double[d.length];
            for (int i = 0; i < this.weights.length; i++) {
                this.weights[i] = 1.0;
            }
            return;
        }

        if (d.length == this.weights.length) {
            return;
        }

        if (d.length > this.weights.length) {
            double[] newWeights = new double[d.length];
            for (int i = 0; i < this.weights.length; i++) {
                newWeights[i] = this.weights[i];
            }
            this.weights = newWeights;
        } else {
            double[] newWeights = new double[d.length];
            for (int i = 0; i < d.length; i++) {
                newWeights[i] = this.weights[i];
            }
            this.weights = newWeights;
        }
    }

    /**
     * This method will return the InterfaceOptimizationTarget array
     *
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceOptimizationObjective[] getSelectedTargets() {
        return this.selectedObjectives;
    }

    /**
     * This method will return the InterfaceOptimizationTarget array
     *
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceOptimizationObjective[] getAvailableTargets() {
        return this.availableObjectives;
    }

    /**
     * This method allows you to read the weights
     *
     * @return the weights
     */
    public double[] getWeights() {
        return this.weights;
    }

    public void setWeights(double[] d) {
        this.weights = d;
    }

    /**
     * This method allows you to set/get the descriptive string
     *
     * @return the string
     */
    public String getDescriptiveString() {
        return this.descriptiveString;
    }

    public void setDescriptiveString(String d) {
        this.descriptiveString = d;
    }

    /**
     * This method allows you to set/get the weights label
     *
     * @return the string
     */
    public String getWeigthsLabel() {
        return this.weightsLabel;
    }

    public void setWeightsLabel(String d) {
        this.weightsLabel = d;
    }

    /**
     * This method allows you to set/get the weights label
     *
     * @return the string
     */
    public boolean isNormalizationEnabled() {
        return this.normalizationEnabled;
    }

    public void enableNormalization(boolean d) {
        this.normalizationEnabled = d;
    }

    /**
     * This method allows you to remove a Target from the list
     *
     * @param index The index of the target to be removed.
     */
    public void removeTarget(int index) {
        if ((index < 0) || (index >= this.selectedObjectives.length)) {
            return;
        }

        InterfaceOptimizationObjective[] newList = new InterfaceOptimizationObjective[this.selectedObjectives.length - 1];
        double[] newWeights = new double[this.weights.length - 1];
        int j = 0;
        for (int i = 0; i < this.selectedObjectives.length; i++) {
            if (index != i) {
                newList[j] = this.selectedObjectives[i];
                newWeights[j] = this.weights[i];
                j++;
            }
        }
        this.selectedObjectives = newList;
        this.weights = newWeights;
    }

    /**
     * This method allows you to add a new target to the list
     *
     * @param optTarget
     */
    public void addTarget(InterfaceOptimizationObjective optTarget) {
        InterfaceOptimizationObjective[] newList = new InterfaceOptimizationObjective[this.selectedObjectives.length + 1];
        double[] newWeights = new double[this.weights.length + 1];
        for (int i = 0; i < this.selectedObjectives.length; i++) {
            newList[i] = this.selectedObjectives[i];
            newWeights[i] = this.weights[i];
        }
        newList[this.selectedObjectives.length] = optTarget;
        newWeights[this.selectedObjectives.length] = 1.0;
        this.selectedObjectives = newList;
        this.weights = newWeights;
    }
}