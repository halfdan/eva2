package eva2.optimization.operator.mutation;

/**
 *
 */
public class PropertyMutationMixer implements java.io.Serializable {

    public InterfaceMutation[] availableTargets;
    public InterfaceMutation[] selectedTargets;
    public double[] weights;
    public String descriptiveString = "No Description given.";
    public String weightsLabel = "-";
    public boolean normalizationEnabled = true;

    public PropertyMutationMixer(InterfaceMutation[] d, boolean selectAllOrNone) {
        this.weights = new double[d.length];
        for (int i = 0; i < d.length; i++) {
            this.weights[i] = 1 / ((double) d.length);
        }
        this.availableTargets = d;
        if (selectAllOrNone) {
            this.selectedTargets = d.clone();
        } else {
            this.selectedTargets = null;
        }
    }

    public PropertyMutationMixer(PropertyMutationMixer d) {
        this.descriptiveString = d.descriptiveString;
        this.weightsLabel = d.weightsLabel;
        this.normalizationEnabled = d.normalizationEnabled;
        this.availableTargets = new InterfaceMutation[d.availableTargets.length];
        for (int i = 0; i < this.availableTargets.length; i++) {
            //this.availableTargets[i]  = (InterfaceMutation)d.availableTargets[i].clone();
            this.availableTargets[i] = d.availableTargets[i];
        }
        this.selectedTargets = new InterfaceMutation[d.selectedTargets.length];
        for (int i = 0; i < this.selectedTargets.length; i++) {
            this.selectedTargets[i] = (InterfaceMutation) d.selectedTargets[i].clone();
        }
        if (d.weights != null) {
            this.weights = new double[d.weights.length];
            System.arraycopy(d.weights, 0, this.weights, 0, this.weights.length);
        }
    }

    @Override
    public Object clone() {
        return (Object) new PropertyMutationMixer(this);
    }

    /**
     * This method will allow you to set the value of the InterfaceOptimizationTarget array
     *
     * @param d The InterfaceOptimizationTarget[]
     */
    public void setSelectedMutators(InterfaceMutation[] d) {
        this.selectedTargets = d;

        if (this.weights == null) {
            this.weights = new double[d.length];
            for (int i = 0; i < this.weights.length; i++) {
                this.weights[i] = 1 / ((double) d.length);
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
    public InterfaceMutation[] getSelectedMutators() {
        return this.selectedTargets;
    }

    /**
     * This method will return the InterfaceOptimizationTarget array
     *
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceMutation[] getAvailableMutators() {
        return this.availableTargets;
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
        for (int i = 0; i < this.weights.length; i++) {
            this.weights[i] = Math.abs(this.weights[i]);
        }
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

    public void normalizeWeights() {
        double sum = 0;
        for (int i = 0; i < this.weights.length; i++) {
            sum += this.weights[i];
        }
        if (sum > 0) {
            for (int i = 0; i < this.weights.length; i++) {
                this.weights[i] /= sum;
            }
        }
    }

    /**
     * This method allows you to remove a Target from the list
     *
     * @param index The index of the target to be removed.
     */
    public void removeMutator(int index) {
        if ((index < 0) || (index >= this.selectedTargets.length)) {
            return;
        }

        InterfaceMutation[] newList = new InterfaceMutation[this.selectedTargets.length - 1];
        double[] newWeights = new double[this.weights.length - 1];
        int j = 0;
        for (int i = 0; i < this.selectedTargets.length; i++) {
            if (index != i) {
                newList[j] = this.selectedTargets[i];
                newWeights[j] = this.weights[i];
                j++;
            }
        }
        this.selectedTargets = newList;
        this.weights = newWeights;
    }

    /**
     * This method allows you to add a new target to the list
     *
     * @param optTarget
     */
    public void addMutator(InterfaceMutation optTarget) {
        InterfaceMutation[] newList = new InterfaceMutation[this.selectedTargets.length + 1];
        double[] newWeights = new double[this.weights.length + 1];
        for (int i = 0; i < this.selectedTargets.length; i++) {
            newList[i] = this.selectedTargets[i];
            newWeights[i] = this.weights[i];
        }
        newList[this.selectedTargets.length] = optTarget;
        newWeights[this.selectedTargets.length] = 1.0;
        this.selectedTargets = newList;
        this.weights = newWeights;
    }
}