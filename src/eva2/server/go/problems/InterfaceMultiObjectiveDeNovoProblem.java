package eva2.server.go.problems;

import eva2.server.go.mocco.paretofrontviewer.InterfaceParetoFrontView;
import eva2.server.go.mocco.paretofrontviewer.MOCCOViewer;

/** This is a general interface to access a multi-objective optimization
 * problem which enables de novo programming and thus MOCCO
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.08.2005
 * Time: 13:53:30
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceMultiObjectiveDeNovoProblem {

    /** This method allows you to recieve all the optimization
     * objectives
     * @return A list of optimization objectives
     */
    public InterfaceOptimizationObjective[] getProblemObjectives();

    /** This method will generate a problem specific view on the Pareto
     * front. Nice idea isn't it. This idea was by Jochen, the teamleader
     * of the CombiChem guys at ALTANA Pharma Konstanz.
     * @return the Panel
     */
    public InterfaceParetoFrontView getParetoFrontViewer4MOCCO(MOCCOViewer t);

    /** This method allows MOCCO to deactivate the representation editior
     * if and only if the specific editor reacts to this signal. This signal
     * cannot be deactivated!
     */
    public void deactivateRepresentationEdit();
}
