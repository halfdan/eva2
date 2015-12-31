package eva2.optimization.mocco;


import eva2.gui.MOCCOStandalone;
import eva2.tools.BasicResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *
 */
public class MOCCOChooseMOStrategy extends MOCCOPhase implements InterfaceProcessElement {

    public final static int STRATEGY_MOEA = 0;
    public final static int STRATEGY_STEP = 1;
    public final static int STRATEGY_REFP = 2;
    public final static int STRATEGY_TBCH = 3;
    public final static int STRATEGY_GDF = 4;
    public int moStrategy = MOCCOChooseMOStrategy.STRATEGY_MOEA;

    public MOCCOChooseMOStrategy(MOCCOStandalone mocco) {
        this.mocco = mocco;
    }

    /**
     * This method will call the initialize method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.mocco.controlPanel.removeAll();
        this.mocco.parameterPanel.removeAll();
        this.mocco.parameterPanel.setLayout(new BorderLayout());

        // The button panel

        // the parameter panel
        this.mocco.parameterPanel.add(this.makeHelpText("Please choose a multi-objective" +
                " optimization strategy for the next optimization iteration. The different optimization approaches" +
                " not only differ in the number of soltuion alternatives generated (more soltuions typicall require" +
                " higher computational effort), but also in the amount of input required by the decision maker (DM)."), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        JButton tmpB;
        BasicResourceLoader loader = BasicResourceLoader.getInstance();
        byte[] bytes;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        tmpB = new JButton();
        bytes = loader.getBytesFromResourceLocation("images/MOCCO/MOCCO_MOEA.gif", true);
        tmpB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        tmpB.setBackground(Color.WHITE);
        gbc.gridy = 0;
        gbc.gridx = 0;
        tmpP.add(tmpB, gbc);
        tmpB.setEnabled(true);
        tmpB.addActionListener(choosenMOEA);
        gbc.gridy = 0;
        gbc.gridx = 1;
        tmpP.add(this.makeInformationText("Multi-Objective Evolutionary Algorithm", "The MOEA is the" +
                " only optimization strategy, which returns an approximated Pareto set in a single run." +
                " Please note that with increasing number of objectives the selection pressure for" +
                " the evolutionary algorithm decreases significantly in case of multi-objective" +
                " optimization. Therefore the MOEA should only be applied in case of less than ten" +
                " objectives."), gbc);

        tmpB = new JButton();
        bytes = loader.getBytesFromResourceLocation("images/MOCCO/MOCCO_GDF.gif", true);
        tmpB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        tmpB.setBackground(Color.WHITE);
        gbc.gridy = 1;
        gbc.gridx = 0;
        tmpP.add(tmpB, gbc);
        tmpB.setEnabled(true);
        tmpB.addActionListener(choosenGDF);
        gbc.gridy = 1;
        gbc.gridx = 1;
        tmpP.add(this.makeInformationText("Geoffrion-Dyer-Feinberg Method", "Here the DM needs to select a reference solution" +
                " from the currently known solution. For this solution the DM has to specify trade-off values for each" +
                " objective. This method assumes a linear utility function by results in a simple weighted aggregation."), gbc);
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);

        tmpB = new JButton();
        bytes = loader.getBytesFromResourceLocation("images/MOCCO/MOCCO_STEP.gif", true);
        tmpB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        tmpB.setBackground(Color.WHITE);
        gbc.gridy = 2;
        gbc.gridx = 0;
        tmpP.add(tmpB, gbc);
        tmpB.setEnabled(true);
        tmpB.addActionListener(choosenSTEP);
        gbc.gridy = 2;
        gbc.gridx = 1;
        tmpP.add(this.makeInformationText("STEP Method", "The STEP method requires the DM to select a" +
                " reference solution from the current solution set. For this solution the obtained objective values are to" +
                " be classified by the DM into satisfactory and unsatisfactory ones. The satifactory ones" +
                " are treaded as constraints, while the unsatisfactory objectives are weight aggregated" +
                " into a single objectives. The individual weights are also to be given by the DM."), gbc);

        tmpB = new JButton();
        bytes = loader.getBytesFromResourceLocation("images/MOCCO/MOCCO_REFP.gif", true);
        tmpB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        tmpB.setBackground(Color.WHITE);
        gbc.gridy = 3;
        gbc.gridx = 0;
        tmpP.add(tmpB, gbc);
        tmpB.setEnabled(true);
        tmpB.addActionListener(choosenREFP);
        gbc.gridy = 3;
        gbc.gridx = 1;
        tmpP.add(this.makeInformationText("Reference Point Method", "Here the DM chooses a reference point of" +
                " aspiration levels (unattainable values are allowed). The optimizier tries to minimze the L-metric (<Inf)" +
                " to that reference point. To increase the number of solution alternatives this method can try to" +
                " minimize the distance to multiple alternative/perturbated versions of the reference point at the" +
                " same time."), gbc);

        tmpB = new JButton();
        bytes = loader.getBytesFromResourceLocation("images/MOCCO/MOCCO_TBCH.gif", true);
        tmpB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        tmpB.setBackground(Color.WHITE);
        gbc.gridy = 4;
        gbc.gridx = 0;
        tmpB.addActionListener(choosenTBCH);
        tmpB.setEnabled(true);
        tmpP.add(tmpB, gbc);
        gbc.gridy = 4;
        gbc.gridx = 1;
        tmpP.add(this.makeInformationText("Tchebycheff Method", "This method also requires a possibly unatainable" +
                " reference point and tries to minimze the L-metric (=Inf). To obtain multiple alternative soltuions" +
                " a weighted L-metric is used with different weigths for a number of optimization runs."), gbc);

        this.mocco.parameterPanel.validate();
        this.mocco.controlPanel.validate();
    }

    /**
     * This method returns the choosen strategy
     *
     * @return the strategy
     */
    public int getMOStrategy() {
        return this.moStrategy;
    }

    ActionListener choosenMOEA = event -> {
        mocco.controlPanel.removeAll();
        mocco.parameterPanel.removeAll();
        moStrategy = STRATEGY_MOEA;
        hasFinished = true;
    };
    ActionListener choosenSTEP = event -> {
        mocco.controlPanel.removeAll();
        mocco.parameterPanel.removeAll();
        moStrategy = STRATEGY_STEP;
        hasFinished = true;
    };
    ActionListener choosenREFP = event -> {
        mocco.controlPanel.removeAll();
        mocco.parameterPanel.removeAll();
        moStrategy = STRATEGY_REFP;
        hasFinished = true;
    };
    ActionListener choosenTBCH = event -> {
        mocco.controlPanel.removeAll();
        mocco.parameterPanel.removeAll();
        moStrategy = STRATEGY_TBCH;
        hasFinished = true;
    };
    ActionListener choosenGDF = event -> {
        mocco.controlPanel.removeAll();
        mocco.parameterPanel.removeAll();
        moStrategy = STRATEGY_GDF;
        hasFinished = true;
    };
}