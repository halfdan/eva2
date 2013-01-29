package eva2.server.go.operators.crossover;


import eva2.gui.JParaPanel;
import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 03.12.2003
 * Time: 18:00:14
 * To change this template use Options | File Templates.
 */
public class TestESCrossover implements java.io.Serializable {
    private JFrame          m_Frame;
    private JPanel          m_MainPanel, m_GraphPanel, m_ButtonPanel;
    private JComponent      m_OptionsPanel;
    private JButton         m_InitButton, m_Init2Button, m_Init3Button, m_CrossButton;

    private InterfaceOptimizationProblem m_Problem = new F1Problem();
    private InterfaceCrossover      m_Crossover = new CrossoverESUNDX();
    private Population              m_Partners;
    private AbstractEAIndividual    m_Daddy;
    private AbstractEAIndividual[]  m_OffSprings;

    private int             m_NumberOfCrossovers    = 100;
    private int             m_Dimension             = 2;
    private int             m_NumberOfPartners      = 1;
    double[]                pff;

    private Plot            m_Plot;


    private void initFrame() {
        // init the main frame
        this.m_Frame        = new JFrame();
        this.m_Frame.setTitle("ES Crossover Tester");
        this.m_Frame.setSize(300, 400);
        this.m_Frame.setLocation(530, 50);
        this.m_Frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        // build the main panel
        this.m_MainPanel    = new JPanel();
        this.m_Frame.getContentPane().add(this.m_MainPanel);
        this.m_MainPanel.setLayout(new BorderLayout());
        // build the button panel
        this.m_ButtonPanel = new JPanel();
        this.m_InitButton = new JButton("Init");
        this.m_InitButton.addActionListener(this.initListener);
        this.m_InitButton.setEnabled(true);
        this.m_Init2Button = new JButton("Init 2");
        this.m_Init2Button.addActionListener(this.init2Listener);
        this.m_Init2Button.setEnabled(true);
        this.m_Init3Button = new JButton("Init 3");
        this.m_Init3Button.addActionListener(this.init3Listener);
        this.m_Init3Button.setEnabled(true);
        this.m_CrossButton = new JButton("X");
        this.m_CrossButton.addActionListener(this.XListener);
        this.m_CrossButton.setEnabled(true);
        this.m_ButtonPanel.add(this.m_InitButton);
        this.m_ButtonPanel.add(this.m_Init2Button);
        this.m_ButtonPanel.add(this.m_Init3Button);
        this.m_ButtonPanel.add(this.m_CrossButton);
        this.m_MainPanel.add(this.m_ButtonPanel, BorderLayout.NORTH);
        // build the Options Panel
        this.m_OptionsPanel = (new JParaPanel(this, "MyGUI").makePanel());
        this.m_MainPanel.add(this.m_OptionsPanel, BorderLayout.CENTER);
        // The plot frame
        double[] tmpD = new double[2];
        tmpD[0] = 0;
        tmpD[1] = 0;
        this.m_Plot = new eva2.gui.Plot("ES Crossover Testert", "x", "y", tmpD, tmpD);
        // validate and show
        this.m_Frame.validate();
        this.m_Frame.setVisible(true);
    }

    ActionListener initListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Partners = new Population();
            m_Partners.setTargetSize(m_NumberOfPartners);
            m_Partners.clear();

            InterfaceDataTypeDouble tmpIndyD = new ESIndividualDoubleData();
            AbstractEAIndividual tmpIndyEA;
            double[][] newRange = new double[m_Dimension][2];
            for (int i = 0; i < m_Dimension; i++) {
                newRange[i][0] = -2;
                newRange[i][1] = 2;
            }
            tmpIndyD.setDoubleDataLength(m_Dimension);
            tmpIndyD.SetDoubleRange(newRange);
            for (int i = 0; i < m_Partners.getTargetSize(); i++) {
                tmpIndyEA = (AbstractEAIndividual)((AbstractEAIndividual)tmpIndyD).clone();
                tmpIndyEA.init(m_Problem);
                m_Partners.add(tmpIndyEA);
            }
            m_Partners.init();
            m_Daddy = (AbstractEAIndividual)((AbstractEAIndividual)tmpIndyD).clone();
            m_Daddy.init(m_Problem);
            m_Plot.clearAll();
            m_Plot.setUnconnectedPoint(-2, -2, 0);
            m_Plot.setUnconnectedPoint(2, 2, 0);
            double[]                x;
            x = ((InterfaceDataTypeDouble)m_Daddy).getDoubleData();
            m_Plot.setUnconnectedPoint(x[0], x[1], 0);
            for (int i = 0; i < m_Partners.size(); i++) {
                x = ((InterfaceDataTypeDouble)m_Partners.get(i)).getDoubleData();
                m_Plot.setUnconnectedPoint(x[0], x[1], 0);
                m_Plot.setUnconnectedPoint(x[0], x[1], 0);
                pff = x;
            }
        }
    };

    ActionListener init2Listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Partners = new Population();
            m_Partners.setTargetSize(2);
            m_Partners.clear();

            InterfaceDataTypeDouble tmpIndyD = new ESIndividualDoubleData();
            AbstractEAIndividual tmpIndyEA;
            double[][] newRange = new double[m_Dimension][2];
            for (int i = 0; i < m_Dimension; i++) {
                newRange[i][0] = -2;
                newRange[i][1] = 2;
            }
            tmpIndyD.setDoubleDataLength(m_Dimension);
            tmpIndyD.SetDoubleRange(newRange);

            double[] tmpD = new double[2];
            tmpD[0] = 1;
            tmpD[1] = 1;
            ((AbstractEAIndividual)tmpIndyD).initByValue(tmpD, m_Problem);
            tmpIndyEA = (AbstractEAIndividual)((AbstractEAIndividual)tmpIndyD).clone();
            tmpD = new double[2];
            tmpD[0] = -1;
            tmpD[1] = -1;
            ((AbstractEAIndividual)tmpIndyEA).initByValue(tmpD, m_Problem);
            m_Partners.addIndividual(tmpIndyEA);

            m_Daddy = (AbstractEAIndividual)((AbstractEAIndividual)tmpIndyD).clone();
            m_Plot.clearAll();
            m_Plot.setUnconnectedPoint(-2, -2, 0);
            m_Plot.setUnconnectedPoint(2, 2, 0);
            double[]                x;
            x = ((InterfaceDataTypeDouble)m_Daddy).getDoubleData();
            m_Plot.setUnconnectedPoint(x[0], x[1], 0);
            for (int i = 0; i < m_Partners.size(); i++) {
                x = ((InterfaceDataTypeDouble)m_Partners.get(i)).getDoubleData();
                m_Plot.setUnconnectedPoint(x[0], x[1], 0);
                m_Plot.setUnconnectedPoint(x[0], x[1], 0);
                pff = x;
            }
        }
    };

    ActionListener init3Listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            m_Partners = new Population();
            m_Partners.setTargetSize(3);
            m_Partners.clear();

            InterfaceDataTypeDouble tmpIndyD = new ESIndividualDoubleData();
            AbstractEAIndividual tmpIndyEA;
            double[][] newRange = new double[m_Dimension][2];
            for (int i = 0; i < m_Dimension; i++) {
                newRange[i][0] = -2;
                newRange[i][1] = 2;
            }
            tmpIndyD.setDoubleDataLength(m_Dimension);
            tmpIndyD.SetDoubleRange(newRange);

            double[] tmpD = new double[2];
            tmpD[0] = 0.5;
            tmpD[1] = 1.1;
            ((AbstractEAIndividual)tmpIndyD).initByValue(tmpD, m_Problem);
            tmpIndyEA = (AbstractEAIndividual)((AbstractEAIndividual)tmpIndyD).clone();
            tmpD = new double[2];
            tmpD[0] = 0.1;
            tmpD[1] = -0.65;
            ((AbstractEAIndividual)tmpIndyEA).initByValue(tmpD, m_Problem);
            m_Partners.addIndividual(tmpIndyEA);
            tmpIndyEA = (AbstractEAIndividual)((AbstractEAIndividual)tmpIndyD).clone();
            tmpD = new double[2];
            tmpD[0] = -0.85;
            tmpD[1] = 0.3;
            ((AbstractEAIndividual)tmpIndyEA).initByValue(tmpD, m_Problem);
            m_Partners.addIndividual(tmpIndyEA);

            m_Daddy = (AbstractEAIndividual)((AbstractEAIndividual)tmpIndyD).clone();
            m_Plot.clearAll();
            m_Plot.setUnconnectedPoint(-2, -2, 2);
            m_Plot.setUnconnectedPoint(2, 2, 2);
            double[]                x;
            x = ((InterfaceDataTypeDouble)m_Daddy).getDoubleData();
            m_Plot.setUnconnectedPoint(x[0], x[1], 0);
            for (int i = 0; i < m_Partners.size(); i++) {
                x = ((InterfaceDataTypeDouble)m_Partners.get(i)).getDoubleData();
                m_Plot.setUnconnectedPoint(x[0], x[1], 1);
                m_Plot.setUnconnectedPoint(x[0], x[1], 1);
                pff = x;
            }
        }
    };

    ActionListener XListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            double[]                x;
            AbstractEAIndividual[]  result;
            for (int i = 0; i < m_NumberOfCrossovers; i++) {
                result = m_Crossover.mate(m_Daddy, m_Partners);
                for (int j = 0; j < result.length; j++) {
                    x = ((InterfaceDataTypeDouble)result[j]).getDoubleData();
                    m_Plot.setUnconnectedPoint(x[0], x[1], 0);
                    m_Plot.setUnconnectedPoint(pff[0], pff[1], 1);
                    m_Plot.setUnconnectedPoint(2, 2, 2);
                }
            }
        }
    };

    /** This method will test the crossover operator
     * @param args
     */
    public static void main(String[] args) {
        TestESCrossover t = new TestESCrossover();
        t.initFrame();

    }

    public void setCrossover(InterfaceCrossover NumberOfCrossovers) {
        this.m_Crossover = NumberOfCrossovers;
    }
    public InterfaceCrossover getCrossover() {
        return this.m_Crossover;
    }
    public String crossoverTipText() {
        return "Choose the crossovers operator.";
    }
    public void setNumberOfCrossovers(int NumberOfCrossovers) {
        this.m_NumberOfCrossovers = NumberOfCrossovers;
    }
    public int getNumberOfCrossovers() {
        return this.m_NumberOfCrossovers;
    }
    public String numberOfCrossoversTipText() {
        return "The number of crossovers, that are to be performed.";
    }
    public void setNumberOfPartners(int NumberOfCrossovers) {
        this.m_NumberOfPartners = NumberOfCrossovers;
    }
    public int getNumberOfPartners() {
        return this.m_NumberOfPartners;
    }
    public String numberOfPartnersTipText() {
        return "The number of partners, that are used.";
    }
}


