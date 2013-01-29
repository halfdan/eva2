package eva2.server.go.mocco.paretofrontviewer;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.11.2005
 * Time: 11:16:48
 * To change this template use File | Settings | File Templates.
 */
public class ParetoFrontViewParallelAxsis extends JPanel implements InterfaceParetoFrontView {

    public MOCCOViewer      m_MOCCOViewer;

    public ParetoFrontViewParallelAxsis(MOCCOViewer t) {
        this.m_MOCCOViewer = t;
    }

    /** This method notifies the Pareto front view that
     * the data has changed most likely due to changes in
     * the problem definition
     */
    @Override
    public void updateView() {

    }
}