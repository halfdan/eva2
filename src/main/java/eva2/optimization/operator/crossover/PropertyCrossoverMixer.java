package eva2.optimization.operator.crossover;

/**
 *
 */
public class PropertyCrossoverMixer implements java.io.Serializable {

    public InterfaceCrossover[] availableTargets;
    public InterfaceCrossover[] selectedTargets;
    public double[] weights;
    public String descriptiveString = "No Description given.";
    public String weightsLabel = "-";
    public boolean normalizationEnabled = true;

    public PropertyCrossoverMixer(InterfaceCrossover[] d) {
        this.weights = new double[d.length];
        for (int i = 0; i < d.length; i++) {
            this.weights[i] = 1 / ((double) d.length);
        }
        this.availableTargets = d;
        this.selectedTargets = null;
    }

    public PropertyCrossoverMixer(PropertyCrossoverMixer d) {
        this.descriptiveString = d.descriptiveString;
        this.weightsLabel = d.weightsLabel;
        this.normalizationEnabled = d.normalizationEnabled;
        this.availableTargets = new InterfaceCrossover[d.availableTargets.length];
        System.arraycopy(d.availableTargets, 0, this.availableTargets, 0, this.availableTargets.length);
        this.selectedTargets = new InterfaceCrossover[d.selectedTargets.length];
        for (int i = 0; i < this.selectedTargets.length; i++) {
            this.selectedTargets[i] = (InterfaceCrossover) d.selectedTargets[i].clone();
        }
        if (d.weights != null) {
            this.weights = new double[d.weights.length];
            System.arraycopy(d.weights, 0, this.weights, 0, this.weights.length);
        }
    }

    @Override
    public Object clone() {
        return new PropertyCrossoverMixer(this);
    }

    /**
     * This method will allow you to set the value of the InterfaceOptimizationTarget array
     *
     * @param d The InterfaceOptimizationTarget[]
     */
    public void setSelectedCrossers(InterfaceCrossover[] d) {
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
            System.arraycopy(this.weights, 0, newWeights, 0, this.weights.length);
            this.weights = newWeights;
        } else {
            double[] newWeights = new double[d.length];
            System.arraycopy(this.weights, 0, newWeights, 0, d.length);
            this.weights = newWeights;
        }
    }

    /**
     * This method will return the InterfaceOptimizationTarget array
     *
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceCrossover[] getSelectedCrossers() {
        return this.selectedTargets;
    }

    /**
     * This method will return the InterfaceOptimizationTarget array
     *
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceCrossover[] getAvailableCrossers() {
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
    public void removeCrosser(int index) {
        if ((index < 0) || (index >= this.selectedTargets.length)) {
            return;
        }

        InterfaceCrossover[] newList = new InterfaceCrossover[this.selectedTargets.length - 1];
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
    public void addCrossers(InterfaceCrossover optTarget) {
        InterfaceCrossover[] newList = new InterfaceCrossover[this.selectedTargets.length + 1];
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