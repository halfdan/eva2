package eva2.optimization.operator.crossover;


import eva2.gui.JParaPanel;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.population.Population;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceOptimizationProblem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 */
public class TestESCrossover implements java.io.Serializable {
    private JFrame frame;
    private JPanel mainPanel, buttonPanel;
    private JComponent optionsPanel;
    private JButton initButton, init2Button, init3Button, crossButton;

    private InterfaceOptimizationProblem optimizationProblem = new F1Problem();
    private InterfaceCrossover crossover = new CrossoverESUNDX();
    private Population partners;
    private AbstractEAIndividual daddy;

    private int numberOfCrossovers = 100;
    private int dimension = 2;
    private int numberOfPartners = 1;
    double[] pff;

    private Plot plot;


    private void initFrame() {
        // initialize the main frame
        this.frame = new JFrame();
        this.frame.setTitle("ES Crossover Tester");
        this.frame.setSize(300, 400);
        this.frame.setLocation(530, 50);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        // build the main panel
        this.mainPanel = new JPanel();
        this.frame.getContentPane().add(this.mainPanel);
        this.mainPanel.setLayout(new BorderLayout());
        // build the button panel
        this.buttonPanel = new JPanel();
        this.initButton = new JButton("Init");
        this.initButton.addActionListener(this.initListener);
        this.initButton.setEnabled(true);
        this.init2Button = new JButton("Init 2");
        this.init2Button.addActionListener(this.init2Listener);
        this.init2Button.setEnabled(true);
        this.init3Button = new JButton("Init 3");
        this.init3Button.addActionListener(this.init3Listener);
        this.init3Button.setEnabled(true);
        this.crossButton = new JButton("X");
        this.crossButton.addActionListener(this.XListener);
        this.crossButton.setEnabled(true);
        this.buttonPanel.add(this.initButton);
        this.buttonPanel.add(this.init2Button);
        this.buttonPanel.add(this.init3Button);
        this.buttonPanel.add(this.crossButton);
        this.mainPanel.add(this.buttonPanel, BorderLayout.NORTH);
        // build the Options Panel
        this.optionsPanel = (new JParaPanel(this, "MyGUI").makePanel());
        this.mainPanel.add(this.optionsPanel, BorderLayout.CENTER);
        // The plot frame
        double[] tmpD = new double[2];
        tmpD[0] = 0;
        tmpD[1] = 0;
        // ToDo: Fix plot (it's internal and not showing)
        this.plot = new Plot("ES Crossover Tester", "x", "y", tmpD, tmpD);
        // validate and show
        this.frame.validate();
        this.frame.setVisible(true);
    }

    ActionListener initListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            partners = new Population();
            partners.setTargetSize(numberOfPartners);
            partners.clear();

            InterfaceDataTypeDouble tmpIndyD = new ESIndividualDoubleData();
            AbstractEAIndividual tmpIndyEA;
            double[][] newRange = new double[dimension][2];
            for (int i = 0; i < dimension; i++) {
                newRange[i][0] = -2;
                newRange[i][1] = 2;
            }
            tmpIndyD.setDoubleDataLength(dimension);
            tmpIndyD.setDoubleRange(newRange);
            for (int i = 0; i < partners.getTargetSize(); i++) {
                tmpIndyEA = (AbstractEAIndividual) ((AbstractEAIndividual) tmpIndyD).clone();
                tmpIndyEA.initialize(optimizationProblem);
                partners.add(tmpIndyEA);
            }
            partners.initialize();
            daddy = (AbstractEAIndividual) ((AbstractEAIndividual) tmpIndyD).clone();
            daddy.initialize(optimizationProblem);
            plot.clearAll();
            plot.setUnconnectedPoint(-2, -2, 0);
            plot.setUnconnectedPoint(2, 2, 0);
            double[] x;
            x = ((InterfaceDataTypeDouble) daddy).getDoubleData();
            plot.setUnconnectedPoint(x[0], x[1], 0);
            for (int i = 0; i < partners.size(); i++) {
                x = ((InterfaceDataTypeDouble) partners.get(i)).getDoubleData();
                plot.setUnconnectedPoint(x[0], x[1], 0);
                plot.setUnconnectedPoint(x[0], x[1], 0);
                pff = x;
            }
        }
    };

    ActionListener init2Listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            partners = new Population();
            partners.setTargetSize(2);
            partners.clear();

            InterfaceDataTypeDouble tmpIndyD = new ESIndividualDoubleData();
            AbstractEAIndividual tmpIndyEA;
            double[][] newRange = new double[dimension][2];
            for (int i = 0; i < dimension; i++) {
                newRange[i][0] = -2;
                newRange[i][1] = 2;
            }
            tmpIndyD.setDoubleDataLength(dimension);
            tmpIndyD.setDoubleRange(newRange);

            double[] tmpD = new double[2];
            tmpD[0] = 1;
            tmpD[1] = 1;
            ((AbstractEAIndividual) tmpIndyD).initByValue(tmpD, optimizationProblem);
            tmpIndyEA = (AbstractEAIndividual) ((AbstractEAIndividual) tmpIndyD).clone();
            tmpD = new double[2];
            tmpD[0] = -1;
            tmpD[1] = -1;
            tmpIndyEA.initByValue(tmpD, optimizationProblem);
            partners.addIndividual(tmpIndyEA);

            daddy = (AbstractEAIndividual) ((AbstractEAIndividual) tmpIndyD).clone();
            plot.clearAll();
            plot.setUnconnectedPoint(-2, -2, 0);
            plot.setUnconnectedPoint(2, 2, 0);
            double[] x;
            x = ((InterfaceDataTypeDouble) daddy).getDoubleData();
            plot.setUnconnectedPoint(x[0], x[1], 0);
            for (int i = 0; i < partners.size(); i++) {
                x = ((InterfaceDataTypeDouble) partners.get(i)).getDoubleData();
                plot.setUnconnectedPoint(x[0], x[1], 0);
                plot.setUnconnectedPoint(x[0], x[1], 0);
                pff = x;
            }
        }
    };

    ActionListener init3Listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            partners = new Population();
            partners.setTargetSize(3);
            partners.clear();

            InterfaceDataTypeDouble tmpIndyD = new ESIndividualDoubleData();
            AbstractEAIndividual tmpIndyEA;
            double[][] newRange = new double[dimension][2];
            for (int i = 0; i < dimension; i++) {
                newRange[i][0] = -2;
                newRange[i][1] = 2;
            }
            tmpIndyD.setDoubleDataLength(dimension);
            tmpIndyD.setDoubleRange(newRange);

            double[] tmpD = new double[2];
            tmpD[0] = 0.5;
            tmpD[1] = 1.1;
            ((AbstractEAIndividual) tmpIndyD).initByValue(tmpD, optimizationProblem);
            tmpIndyEA = (AbstractEAIndividual) ((AbstractEAIndividual) tmpIndyD).clone();
            tmpD = new double[2];
            tmpD[0] = 0.1;
            tmpD[1] = -0.65;
            tmpIndyEA.initByValue(tmpD, optimizationProblem);
            partners.addIndividual(tmpIndyEA);
            tmpIndyEA = (AbstractEAIndividual) ((AbstractEAIndividual) tmpIndyD).clone();
            tmpD = new double[2];
            tmpD[0] = -0.85;
            tmpD[1] = 0.3;
            tmpIndyEA.initByValue(tmpD, optimizationProblem);
            partners.addIndividual(tmpIndyEA);

            daddy = (AbstractEAIndividual) ((AbstractEAIndividual) tmpIndyD).clone();
            plot.clearAll();
            plot.setUnconnectedPoint(-2, -2, 2);
            plot.setUnconnectedPoint(2, 2, 2);
            double[] x;
            x = ((InterfaceDataTypeDouble) daddy).getDoubleData();
            plot.setUnconnectedPoint(x[0], x[1], 0);
            for (int i = 0; i < partners.size(); i++) {
                x = ((InterfaceDataTypeDouble) partners.get(i)).getDoubleData();
                plot.setUnconnectedPoint(x[0], x[1], 1);
                plot.setUnconnectedPoint(x[0], x[1], 1);
                pff = x;
            }
        }
    };

    ActionListener XListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            double[] x;
            AbstractEAIndividual[] result;
            for (int i = 0; i < numberOfCrossovers; i++) {
                result = crossover.mate(daddy, partners);
                for (int j = 0; j < result.length; j++) {
                    x = ((InterfaceDataTypeDouble) result[j]).getDoubleData();
                    plot.setUnconnectedPoint(x[0], x[1], 0);
                    plot.setUnconnectedPoint(pff[0], pff[1], 1);
                    plot.setUnconnectedPoint(2, 2, 2);
                }
            }
        }
    };

    /**
     * This method will test the crossover operator
     */
    public static void main(String[] args) {
        TestESCrossover t = new TestESCrossover();
        t.initFrame();
    }

    public void setCrossover(InterfaceCrossover NumberOfCrossovers) {
        this.crossover = NumberOfCrossovers;
    }

    public InterfaceCrossover getCrossover() {
        return this.crossover;
    }

    public String crossoverTipText() {
        return "Choose the crossovers operator.";
    }

    public void setNumberOfCrossovers(int NumberOfCrossovers) {
        this.numberOfCrossovers = NumberOfCrossovers;
    }

    public int getNumberOfCrossovers() {
        return this.numberOfCrossovers;
    }

    public String numberOfCrossoversTipText() {
        return "The number of crossovers, that are to be performed.";
    }

    public void setNumberOfPartners(int NumberOfCrossovers) {
        this.numberOfPartners = NumberOfCrossovers;
    }

    public int getNumberOfPartners() {
        return this.numberOfPartners;
    }

    public String numberOfPartnersTipText() {
        return "The number of partners, that are used.";
    }
}


