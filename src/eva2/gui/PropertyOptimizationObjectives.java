package eva2.gui;

import eva2.problems.InterfaceOptimizationObjective;

/**
 *
 */
public class PropertyOptimizationObjectives implements java.io.Serializable {
    public InterfaceOptimizationObjective[] availableObjectives;
    public InterfaceOptimizationObjective[] selectedObjectives;

    public PropertyOptimizationObjectives(InterfaceOptimizationObjective[] d) {
        this.availableObjectives = d;
        this.selectedObjectives = null;
    }

    public PropertyOptimizationObjectives(PropertyOptimizationObjectives d) {
        this.availableObjectives = new InterfaceOptimizationObjective[d.availableObjectives.length];
        for (int i = 0; i < this.availableObjectives.length; i++) {
            this.availableObjectives[i] = (InterfaceOptimizationObjective) d.availableObjectives[i].clone();
        }
        this.selectedObjectives = new InterfaceOptimizationObjective[d.selectedObjectives.length];
        for (int i = 0; i < this.selectedObjectives.length; i++) {
            this.selectedObjectives[i] = (InterfaceOptimizationObjective) d.selectedObjectives[i].clone();
        }
    }

    @Override
    public Object clone() {
        return new PropertyOptimizationObjectives(this);
    }

    /**
     * This method will allow you to set the value of the InterfaceOptimizationTarget array
     *
     * @param d The InterfaceOptimizationTarget[]
     */
    public void setSelectedTargets(InterfaceOptimizationObjective[] d) {
        this.selectedObjectives = d;
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
     * This method allows you to remove a Target from the list
     *
     * @param index The index of the target to be removed.
     */
    public void removeTarget(int index) {
        if ((index < 0) || (index >= this.selectedObjectives.length)) {
            return;
        }

        InterfaceOptimizationObjective[] newList = new InterfaceOptimizationObjective[this.selectedObjectives.length - 1];
        int j = 0;
        for (int i = 0; i < this.selectedObjectives.length; i++) {
            if (index != i) {
                newList[j] = this.selectedObjectives[i];
                j++;
            }
        }
        this.selectedObjectives = newList;
    }

    /**
     * This method allows you to add a new target to the list
     *
     * @param optTarget
     */
    public void addTarget(InterfaceOptimizationObjective optTarget) {
        InterfaceOptimizationObjective[] newList = new InterfaceOptimizationObjective[this.selectedObjectives.length + 1];
        System.arraycopy(this.selectedObjectives, 0, newList, 0, this.selectedObjectives.length);
        newList[this.selectedObjectives.length] = optTarget;
        this.selectedObjectives = newList;
    }
}
