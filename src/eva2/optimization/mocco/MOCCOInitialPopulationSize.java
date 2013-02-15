package eva2.optimization.mocco;

import eva2.optimization.go.MOCCOStandalone;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 25.10.2005
 * Time: 10:07:09
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOInitialPopulationSize extends MOCCOPhase implements InterfaceProcessElement {

    private JTextField          m_JTextField;

    public MOCCOInitialPopulationSize(MOCCOStandalone mocco) {
        this.m_Mocco = mocco;
    }

    /** This method will call the init method and will go to stall
     */
    @Override
    public void initProcessElementParametrization() {
        this.m_Mocco.m_JPanelControl.removeAll();

        // The button panel
        JButton tmpB = new JButton("Continue to problem redefinition/analyzation.");
        tmpB.addActionListener(continue2);
        this.m_Mocco.m_JPanelControl.add(tmpB);
        // the parameter panel
        this.m_Mocco.m_JPanelParameters.removeAll();
        this.m_Mocco.m_JPanelParameters.setLayout(new BorderLayout());
        this.m_Mocco.m_JPanelParameters.add(this.makeHelpText("Please choose a size for the " +
                "initial set of solution alternatives necessary to start the MOCCO optimization " +
                "cycle."), BorderLayout.NORTH);
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill        = GridBagConstraints.HORIZONTAL;
        gbc.anchor      = GridBagConstraints.EAST;
        gbc.weightx     = 100;        
        gbc.gridx       = 0;
        gbc.gridy       = 0;
        tmpP.add(new JLabel("Initial Solution Set Size:"), gbc);
        this.m_JTextField = new JTextField(""+this.m_Mocco.m_State.m_InitialPopulationSize);
        this.m_JTextField.addActionListener(popSizeEdited);
        gbc.gridx       = 1;
        tmpP.add(this.m_JTextField, gbc);
        this.m_Mocco.m_JPanelParameters.add(tmpP, BorderLayout.CENTER);
        this.m_Mocco.m_JPanelParameters.validate();
        this.m_Mocco.m_JPanelControl.validate();
    }


    ActionListener continue2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Mocco.m_JPanelControl.removeAll();
            m_Mocco.m_JPanelControl.validate();
            m_Mocco.m_JPanelParameters.removeAll();
            m_Finished = true;
        }
    };

    ActionListener popSizeEdited = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            String s = m_JTextField.getText();
            try {
                int t = new Integer(s).intValue();
                m_Mocco.m_State.m_InitialPopulationSize = t;
            } catch (java.lang.NumberFormatException e) {

            }
        }
    };
}