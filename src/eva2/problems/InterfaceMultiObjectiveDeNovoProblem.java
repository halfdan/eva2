package eva2.problems;

import eva2.optimization.mocco.paretofrontviewer.InterfaceParetoFrontView;
import eva2.optimization.mocco.paretofrontviewer.MOCCOViewer;

/**
 * This is a general interface to access a multi-objective optimization
 * problem which enables de novo programming and thus MOCCO.
 */
public interface InterfaceMultiObjectiveDeNovoProblem {

    /**
     * This method allows you to recieve all the optimization
     * objectives
     *
     * @return A list of optimization objectives
     */
    public InterfaceOptimizationObjective[] getProblemObjectives();

    /**
     * This method will generate a problem specific view on the Pareto
     * front. Nice idea isn't it. This idea was by Jochen, the teamleader
     * of the CombiChem guys at ALTANA Pharma Konstanz.
     *
     * @return the Panel
     */
    public InterfaceParetoFrontView getParetoFrontViewer4MOCCO(MOCCOViewer t);

    /**
     * This method allows MOCCO to deactivate the representation editior
     * if and only if the specific editor reacts to this signal. This signal
     * cannot be deactivated!
     */
    public void deactivateRepresentationEdit();
}
