package eva2.optimization.mocco;

import eva2.optimization.go.MOCCOStandalone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class MOCCOInitialPopulationSize extends MOCCOPhase implements InterfaceProcessElement {

    private JTextField textField;

    public MOCCOInitialPopulationSize(MOCCOStandalone mocco) {
        this.mocco = mocco;
    }

    /**
     * This method will call the initialize method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.mocco.controlPanel.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to problem redefinition/analyzation.");
        tmpB.addActionListener(continue2);
        this.mocco.controlPanel.add(tmpB);
        // the parameter panel
        this.mocco.parameterPanel.removeAll();
        this.mocco.parameterPanel.setLayout(new BorderLayout());
        this.mocco.parameterPanel.add(this.makeHelpText("Please choose a size for the " +
                "initial set of solution alternatives necessary to start the MOCCO optimization " +
                "cycle."), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 100;
        gbc.gridx = 0;
        gbc.gridy = 0;
        tmpP.add(new JLabel("Initial Solution Set Size:"), gbc);
        this.textField = new JTextField("" + this.mocco.state.initialPopulationSize);
        this.textField.addActionListener(popSizeEdited);
        gbc.gridx = 1;
        tmpP.add(this.textField, gbc);
        this.mocco.parameterPanel.add(tmpP, BorderLayout.CENTER);
        this.mocco.parameterPanel.validate();
        this.mocco.controlPanel.validate();
    }


    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            mocco.controlPanel.removeAll();
            mocco.controlPanel.validate();
            mocco.parameterPanel.removeAll();
            hasFinished = true;
        }
    };

    ActionListener popSizeEdited = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            String s = textField.getText();
            try {
                int t = Integer.parseInt(s);
                mocco.state.initialPopulationSize = t;
            } catch (java.lang.NumberFormatException e) {

            }
        }
    };
}