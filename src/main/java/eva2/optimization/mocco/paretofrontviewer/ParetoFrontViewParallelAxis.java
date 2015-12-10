package eva2.optimization.mocco.paretofrontviewer;

import javax.swing.*;

/**
 *
 */
public class ParetoFrontViewParallelAxis extends JPanel implements InterfaceParetoFrontView {

    public MOCCOViewer moccoViewer;

    public ParetoFrontViewParallelAxis(MOCCOViewer t) {
        this.moccoViewer = t;
    }

    /**
     * This method notifies the Pareto front view that
     * the data has changed most likely due to changes in
     * the problem definition
     */
    @Override
    public void updateView() {

    }
}