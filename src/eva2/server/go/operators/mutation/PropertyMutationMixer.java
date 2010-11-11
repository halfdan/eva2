package eva2.server.go.operators.mutation;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 20.05.2005
 * Time: 13:54:25
 * To change this template use File | Settings | File Templates.
 */
public class PropertyMutationMixer implements java.io.Serializable {

    public InterfaceMutation[]              m_AvailableTargets;
    public InterfaceMutation[]              m_SelectedTargets;
    public double[]                         m_Weights;
    public String                           m_DescriptiveString     = "No Description given.";
    public String                           m_WeightsLabel          = "-";
    public boolean                          m_NormalizationEnabled  = true;

    public PropertyMutationMixer(InterfaceMutation[] d, boolean selectAllOrNone) {
        this.m_Weights = new double[d.length];
        for (int i = 0; i < d.length; i++) this.m_Weights[i] = 1/((double)d.length);
        this.m_AvailableTargets = d;
        if (selectAllOrNone) this.m_SelectedTargets  = d.clone();
        else this.m_SelectedTargets = null;
    }
    public PropertyMutationMixer(PropertyMutationMixer d) {
        this.m_DescriptiveString        = d.m_DescriptiveString;
        this.m_WeightsLabel             = d.m_WeightsLabel;
        this.m_NormalizationEnabled      = d.m_NormalizationEnabled;
        this.m_AvailableTargets         = new InterfaceMutation[d.m_AvailableTargets.length];
        for (int i = 0; i < this.m_AvailableTargets.length; i++) {
            //this.m_AvailableTargets[i]  = (InterfaceMutation)d.m_AvailableTargets[i].clone();
            this.m_AvailableTargets[i]  = d.m_AvailableTargets[i];
        }
        this.m_SelectedTargets          = new InterfaceMutation[d.m_SelectedTargets.length];
        for (int i = 0; i < this.m_SelectedTargets.length; i++) {
            this.m_SelectedTargets[i]   = (InterfaceMutation)d.m_SelectedTargets[i].clone();
        }
        if (d.m_Weights != null) {
            this.m_Weights = new double[d.m_Weights.length];
            System.arraycopy(d.m_Weights, 0, this.m_Weights, 0, this.m_Weights.length);
        }
    }

    public Object clone() {
        return (Object) new PropertyMutationMixer(this);
    }

    /** This method will allow you to set the value of the InterfaceOptimizationTarget array
     * @param d     The InterfaceOptimizationTarget[]
     */
    public void setSelectedMutators(InterfaceMutation[] d) {
        this.m_SelectedTargets = d;

        if (this.m_Weights == null) {
            this.m_Weights = new double[d.length];
            for (int i = 0; i < this.m_Weights.length; i++) this.m_Weights[i] = 1/((double)d.length);
            return;
        }

        if (d.length == this.m_Weights.length) return;

        if (d.length > this.m_Weights.length) {
            double[] newWeights = new double[d.length];
            for (int i = 0; i < this.m_Weights.length; i++) newWeights[i] = this.m_Weights[i];
            this.m_Weights = newWeights;
        } else {
            double[] newWeights = new double[d.length];
            for (int i = 0; i < d.length; i++) newWeights[i] = this.m_Weights[i];
            this.m_Weights = newWeights;
        }
    }

    /** This method will return the InterfaceOptimizationTarget array
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceMutation[] getSelectedMutators() {
        return this.m_SelectedTargets;
    }

    /** This method will return the InterfaceOptimizationTarget array
     * @return The InterfaceOptimizationTarget[].
     */
    public InterfaceMutation[] getAvailableMutators() {
        return this.m_AvailableTargets;
    }

    /** This method allows you to read the weights
     * @return the weights
     */
    public double[] getWeights() {
        return this.m_Weights;
    }
    public void setWeights(double[] d) {
        this.m_Weights = d;
        for (int i = 0; i < this.m_Weights.length; i++) this.m_Weights[i] = Math.abs(this.m_Weights[i]);
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

    public void normalizeWeights() {
        double sum = 0;
        for (int i = 0; i < this.m_Weights.length; i++) {
            sum += this.m_Weights[i];
        }
        if (sum > 0) {
            for (int i = 0; i < this.m_Weights.length; i++) {
                this.m_Weights[i] = this.m_Weights[i]/sum;
            }
        }
    }

    /** This method allows you to remove a Target from the list
     * @param index     The index of the target to be removed.
     */
    public void removeMutator(int index) {
        if ((index < 0) || (index >= this.m_SelectedTargets.length)) return;

        InterfaceMutation[]   newList = new InterfaceMutation[this.m_SelectedTargets.length-1];
        double[]                        newWeights = new double[this.m_Weights.length - 1];
        int j = 0;
        for (int i = 0; i < this.m_SelectedTargets.length; i++) {
            if (index != i) {
                newList[j] = this.m_SelectedTargets[i];
                newWeights[j] = this.m_Weights[i];
                j++;
            }
        }
        this.m_SelectedTargets  = newList;
        this.m_Weights          = newWeights;
    }

    /** This method allows you to add a new target to the list
     * @param optTarget
     */
    public void addMutator(InterfaceMutation optTarget) {
        InterfaceMutation[]   newList = new InterfaceMutation[this.m_SelectedTargets.length+1];
        double[]                        newWeights = new double[this.m_Weights.length + 1];
        for (int i = 0; i < this.m_SelectedTargets.length; i++) {
            newList[i] = this.m_SelectedTargets[i];
            newWeights[i] = this.m_Weights[i];
        }
        newList[this.m_SelectedTargets.length] = optTarget;
        newWeights[this.m_SelectedTargets.length] = 1.0;
        this.m_SelectedTargets  = newList;
        this.m_Weights          = newWeights;
    }
}