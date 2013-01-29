package eva2.gui;

import eva2.server.go.problems.InterfaceOptimizationObjective;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.01.2005
 * Time: 17:11:31
 * To change this template use File | Settings | File Templates.
 */
public class PropertyOptimizationObjectives implements java.io.Serializable {
    public InterfaceOptimizationObjective[]    m_AvailableObjectives;
    public InterfaceOptimizationObjective[]    m_SelectedObjectives;

    public PropertyOptimizationObjectives(InterfaceOptimizationObjective[] d) {
        this.m_AvailableObjectives = d;
        this.m_SelectedObjectives  = null;
    }
    public PropertyOptimizationObjectives(PropertyOptimizationObjectives d) {
        this.m_AvailableObjectives         = new InterfaceOptimizationObjective[d.m_AvailableObjectives.length];
        for (int i = 0; i < this.m_AvailableObjectives.length; i++) {
            this.m_AvailableObjectives[i]  = (InterfaceOptimizationObjective)d.m_AvailableObjectives[i].clone();
        }
        this.m_SelectedObjectives          = new InterfaceOptimizationObjective[d.m_SelectedObjectives.length];
        for (int i = 0; i < this.m_SelectedObjectives.length; i++) {
            this.m_SelectedObjectives[i]   = (InterfaceOptimizationObjective)d.m_SelectedObjectives[i].clone();
        }
    }

    @Override
    public Object clone() {
        return (Object) new PropertyOptimizationObjectives(this);
    }

    /** This method will allow you to set the value of the InterfaceOptimizationTarget array
     * @param d     The InterfaceOptimizationTarget[]
     */
    public void setSelectedTargets(InterfaceOptimizationObjective[] d) {
        this.m_SelectedObjectives = d;
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

    /** This method allows you to remove a Target from the list
     * @param index     The index of the target to be removed.
     */
    public void removeTarget(int index) {
        if ((index < 0) || (index >= this.m_SelectedObjectives.length)) return;

        InterfaceOptimizationObjective[] newList = new InterfaceOptimizationObjective[this.m_SelectedObjectives.length-1];
        int j = 0;
        for (int i = 0; i < this.m_SelectedObjectives.length; i++) {
            if (index != i) {
                newList[j] = this.m_SelectedObjectives[i];
                j++;
            }
        }
        this.m_SelectedObjectives = newList;
    }

    /** This method allows you to add a new target to the list
     * @param optTarget
     */
    public void addTarget(InterfaceOptimizationObjective optTarget) {
        InterfaceOptimizationObjective[] newList = new InterfaceOptimizationObjective[this.m_SelectedObjectives.length+1];
        for (int i = 0; i < this.m_SelectedObjectives.length; i++) {
            newList[i] = this.m_SelectedObjectives[i];
        }
        newList[this.m_SelectedObjectives.length] = optTarget;
        this.m_SelectedObjectives = newList;
    }
}
