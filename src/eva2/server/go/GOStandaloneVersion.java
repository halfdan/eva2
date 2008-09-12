package eva2.server.go;

import java.awt.BorderLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import wsi.ra.jproxy.ThreadProxy;
import wsi.ra.math.RNG;
import eva2.client.EvAClient;
import eva2.gui.JParaPanel;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.GAIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.crossover.CrossoverGANPoint;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESFixedStepSize;
import eva2.server.go.operators.mutation.MutateESLocal;
import eva2.server.go.operators.selection.SelectTournament;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.strategies.EvolutionStrategies;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.modules.GOParameters;
import eva2.tools.TokenHolder;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
public class GOStandaloneVersion implements InterfaceGOStandalone, InterfacePopulationChangedEventListener, java.io.Serializable {

    // Interface GUI Stuff
    transient private JFrame          m_Frame;
    transient private JPanel          m_MainPanel;
    transient private JPanel          m_ButtonPanel;
    transient private JButton         m_RunButton, m_StopButton, m_Continue, m_ShowSolution;
    transient private JComponent      m_OptionsPanel, m_O1, m_O2;
    transient private JComponent      m_StatusPanel;
    transient private JLabel          m_StatusField;
    transient private JProgressBar    m_ProgressBar;
    transient private SwingWorker     worker;
    transient private boolean         show = false;
//    transient private InterfaceTest   test = new Test1();

    // Opt. Algorithms and Parameters
    //transient private InterfaceOptimizer              m_Optimizer         = new EvolutionaryMultiObjectiveOptimization();
    //transient private InterfaceOptimizationProblem    m_Problem           = new TF1Problem();
    //transient private int                             m_FunctionCalls     = 1000;
    private GOParameters                              m_GO;
    transient private int                             m_MultiRuns         = 1;
    transient private int                             m_RecentFC;
    transient private int                             currentExperiment   = 0;
    transient private int                             currentRun;
    transient private int                             currentProgress;
    transient private String                          m_ExperimentName;
    transient private String                          m_OutputPath = "";
    transient private String                          m_OutputFileName    = "none";
//    transient private GOStandaloneVersion             m_yself;

    // these parameters are for the continue option
    transient private Population                        m_Backup;
    transient private boolean                           m_ContinueFlag;

    // Plot Panel stuff
    transient private eva2.gui.Plot  m_Plot;
    transient private ArrayList       m_PerformedRuns = new ArrayList();
    transient private ArrayList       m_TmpData;
    transient private BufferedWriter  m_OutputFile;

    // Test
    transient private List    m_List;


    /** Create a new EALectureGUI.
     */
    public GOStandaloneVersion() {
//        this.m_List  = new List();
//        this.m_List.add("Test1");
//        this.m_List.add("Test2");
//        this.m_List.add("Test3");
//        this.m_yself            = this;
        this.m_GO               = GOParameters.getInstance();
        this.m_ExperimentName   = this.m_GO.getOptimizer().getName()+"-"+this.m_PerformedRuns.size();
        this.m_GO.addPopulationChangedEventListener(this);
        RNG.setRandomSeed(m_GO.getSeed());
    }

    /** This method allows you to get the current GO parameters
     *
     */
    public GOParameters getGOParameters() {
        return this.m_GO;
    }

    /** This method will generate a Plot Frame and a main Editing Frame
     */
    public void initFrame() {
        this.m_ProgressBar = new JProgressBar();
        // init the main frame
        this.m_Frame        = new JFrame();
        this.m_Frame.setTitle("Genetic Optimizing");
        this.m_Frame.setSize(500, 400);
        this.m_Frame.setLocation(530, 50);
        this.m_Frame.addWindowListener(new WindowAdapter() {
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
        this.m_RunButton = new JButton("Run");
        this.m_RunButton.addActionListener(this.runListener);
        this.m_RunButton.setEnabled(true);
        this.m_RunButton.setToolTipText("Run the optimization process with the current parameter settings.");
        this.m_StopButton = new JButton("Stop");
        this.m_StopButton.addActionListener(this.stopListener);
        this.m_StopButton.setEnabled(false);
        this.m_StopButton.setToolTipText("Stop the runnig the optimization process.");
        this.m_Continue = new JButton("Continue");
        this.m_Continue.addActionListener(this.continueListener);
        this.m_Continue.setEnabled(false);
        this.m_Continue.setToolTipText("Resume the previous optimization (check termination criteria and multiruns = 1!).");
        this.m_ShowSolution = new JButton("Show Solution");
        this.m_ShowSolution.addActionListener(this.showSolListener);
        this.m_ShowSolution.setEnabled(true);
        this.m_ShowSolution.setToolTipText("Show the current best solution.");
        this.m_ButtonPanel.add(this.m_RunButton);
        this.m_ButtonPanel.add(this.m_Continue);
        this.m_ButtonPanel.add(this.m_StopButton);
        this.m_ButtonPanel.add(this.m_ShowSolution);
        this.m_MainPanel.add(this.m_ButtonPanel, BorderLayout.NORTH);

        // build the Options Panel
        JParaPanel paraPanel = new JParaPanel(this, "MyGUI");
        Class object = null, editor = null;
        String tmp = "eva2.server.oa.go.Tools.InterfaceTest";
        try {
            object = Class.forName(tmp);
        } catch(java.lang.ClassNotFoundException e) {
            System.out.println("No Class found for " + tmp);
        }
        tmp = "eva2.gui.GenericObjectEditor";
        try {
            editor = Class.forName(tmp);
        } catch(java.lang.ClassNotFoundException e) {
            System.out.println("No Class found for " + tmp);
        }
        if ((object != null) && (editor != null)) paraPanel.registerEditor(object, editor);
        this.m_O1 = (paraPanel.installActions());
        EvAClient.setProperty("eva2.server.oa.go.Tools.InterfaceTest", "eva2.server.oa.go.Tools.Test1,eva2.server.oa.go.Tools.Test2");
        this.m_OptionsPanel = new JTabbedPane();
        JParaPanel paraPanel2 = new JParaPanel(this.m_GO, "MyGUI");
        this.m_O2 = (paraPanel2.installActions());
        ((JTabbedPane)this.m_OptionsPanel).addTab("GO Parameters", this.m_O2);
        ((JTabbedPane)this.m_OptionsPanel).addTab("GO Statistics", this.m_O1);
        this.m_MainPanel.add(this.m_OptionsPanel, BorderLayout.CENTER);

        // build the Status Panel
        this.m_StatusPanel = new JPanel();
        this.m_StatusPanel.setLayout(new BorderLayout());
        this.m_StatusField = new JLabel("Click Run to begin...");
        this.m_ProgressBar = new JProgressBar();
        this.m_StatusPanel.add(this.m_StatusField, BorderLayout.NORTH);
        this.m_StatusPanel.add(this.m_ProgressBar, BorderLayout.SOUTH);
        this.m_MainPanel.add(this.m_StatusPanel, BorderLayout.SOUTH);
        // The plot frame
        double[] tmpD = new double[2];
        tmpD[0] = 1;
        tmpD[1] = 1;
        this.m_Plot = new eva2.gui.Plot("EA Lecture Plot", "Function calls", "Fitness", true);
        // validate and show
        this.m_Frame.validate();
        this.m_Frame.setVisible(true);
    }

    /** This action listener, called by the "Run/Restart" button, will init the problem and start the computation.
     */
    ActionListener runListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            worker = new SwingWorker() {
                public Object construct() {
                    return doWork();
                }
                public void finished() {
                    m_RunButton.setEnabled(true);
                    m_Continue.setEnabled(true);
                    m_StopButton.setEnabled(false);
                    m_Backup        = (Population) m_GO.getOptimizer().getPopulation().clone();
                }
            };
            worker.start();
            m_RunButton.setEnabled(false);
            m_Continue.setEnabled(false);
            m_StopButton.setEnabled(true);
        }
    };

    /** This action listener, called by the "Cancel" button, interrupts
     * the worker thread which is running this.doWork().  Note that
     * the doWork() method handles InterruptedExceptions cleanly.
     */
    ActionListener stopListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            m_RunButton.setEnabled(true);
            m_Continue.setEnabled(true);
            m_StopButton.setEnabled(false);
            worker.interrupt();
            for (int i = 0; i < m_MultiRuns; i++) m_Plot.clearGraph(1000 +i);
        }
    };

    /** This action listener, called by the "Cancel" button, interrupts
     * the worker thread which is running this.doWork().  Note that
     * the doWork() method handles InterruptedExceptions cleanly.
     */
    ActionListener continueListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            // todo something need to be done here...
            worker = new SwingWorker() {
                public Object construct() {
                    return doWork();
                }
                public void finished() {
                    m_RunButton.setEnabled(true);
                    m_Continue.setEnabled(true);
                    m_StopButton.setEnabled(false);
                    m_Backup        = (Population) m_GO.getOptimizer().getPopulation().clone();
                    m_ContinueFlag  = false;
                }
            };
            // also mal ganz anders ich gehe davon aus, dass der Benutzer das Ding parametrisiert hat
            // setze einfach die Backup population ein...
            m_ContinueFlag      = true;
            m_MultiRuns         = 1;      // multiruns machen bei continue einfach keinen Sinn...
            worker.start();
            m_RunButton.setEnabled(false);
            m_Continue.setEnabled(false);
            m_StopButton.setEnabled(true);
        }
    };

    /** This action listener, called by the "show" button will show the
     * currently best solution in a frame.
     */
    ActionListener showSolListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            JFrame frame = new JFrame();
            frame.setTitle("The current best solution for "+m_GO.getProblem().getName());
            frame.setSize(400, 300);
            frame.setLocation(450, 250);
            Population pop = m_GO.getOptimizer().getPopulation();
            frame.getContentPane().add(m_GO.getProblem().drawIndividual(pop.getGeneration(), pop.getFunctionCalls(), pop.getBestEAIndividual()));
            frame.validate();
            frame.setVisible(true);
        }
    };

    /** This method gives the experimental settings and starts the work.
     */
    public void startExperiment() {
        // This is for the CBN-TEST RUNS
        this.m_GO.setOptimizer(new EvolutionStrategies());
        this.m_GO.setProblem(new F1Problem());
        EvaluationTerminator terminator = new EvaluationTerminator();
        terminator.setFitnessCalls(50000);
        this.m_GO.setTerminator(terminator);
        this.m_MultiRuns            = 10;
        int experimentType          = 0;
        this.m_ExperimentName       = "InferringGRN";
        this.m_OutputFileName = "Result";
        this.m_OutputPath           = "results/";
        // These are some tmp Variables
        InterfaceDataTypeDouble  tmpIndy = new ESIndividualDoubleData();
        InterfaceMutation        tmpMut  = new MutateESFixedStepSize();

        switch (experimentType) {
            case 0 : {
                // use the Struture Skeletalizing with GA
                this.m_OutputFileName = "Prim4_StructSkelGATESTIT";
                GeneticAlgorithm ga     = new GeneticAlgorithm();
                SelectTournament tour   = new SelectTournament();
                tour.setTournamentSize(10);
                ga.setParentSelection(tour);
                ga.setPartnerSelection(tour);
                this.m_GO.setOptimizer(ga);
                this.m_GO.getOptimizer().getPopulation().setPopulationSize(100);
                F1Problem problem = new F1Problem();
                tmpIndy = new GAIndividualDoubleData();
                ((GAIndividualDoubleData)tmpIndy).setCrossoverOperator(new CrossoverGANPoint());
                ((GAIndividualDoubleData)tmpIndy).setCrossoverProbability(1.0);
                ((GAIndividualDoubleData)tmpIndy).setMutationProbability(1.0);
                ((F1Problem)problem).setEAIndividual(tmpIndy);
                //((FGRNInferringProblem)this.m_Problem).setStructreSkelInterval(1);
                this.m_GO.getOptimizer().SetProblem(problem);
                this.m_GO.getOptimizer().addPopulationChangedEventListener(this);
                this.doWork();
                break;
            }
            case 1 : {
                // use the simple ES Local
            	this.m_OutputFileName = "X360_StandardES";
                EvolutionStrategies es = new EvolutionStrategies();
                this.m_GO.setOptimizer(es);
                this.m_GO.getOptimizer().getPopulation().setPopulationSize(50);
                F1Problem problem = new F1Problem();
                tmpIndy = new ESIndividualDoubleData();
                ((AbstractEAIndividual)tmpIndy).setMutationOperator(new MutateESLocal());
                ((F1Problem)problem).setEAIndividual(tmpIndy);
                //((FGRNInferringProblem)this.m_Problem).setUseHEigenMatrix(true);
                //((FGRNInferringProblem)this.m_Problem).setUseOnlyPositiveNumbers(true);
                this.m_GO.getOptimizer().SetProblem(problem);
                this.m_GO.getOptimizer().addPopulationChangedEventListener(this);
                this.doWork();
                break;
            }
        }
        String m_MyHostName = "_";
        try {
            m_MyHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            System.out.println("ERROR getting HostName (GOStandalone.startExperiment) " + e.getMessage());
        }
//        EVAMail.SendMail("GOTask on "+m_MyHostName+ " finished", "Have a look at the results at the result file", "streiche@informatik.uni-tuebingen.de");

    }

    /**
     * This method represents the application code that we'd like to
     * run on a separate thread.  It simulates slowly computing
     * a value, in this case just a string 'All Done'.  It updates the
     * progress bar every half second to remind the user that
     * we're still busy.
     */
    public Object doWork() {
        try {
        	this.m_GO.saveInstance();
            if (this.show) this.m_StatusField.setText("Optimizing...");

            RNG.setRandomSeed(m_GO.getSeed());
            // opening output file...
            if (!this.m_OutputFileName.equalsIgnoreCase("none")) {
                String name = "";
                SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_'HH.mm.ss");
                String m_StartDate = formatter.format(new Date());
                name = this.m_OutputPath + this.m_OutputFileName +"_"+this.m_ExperimentName+"_"+m_StartDate+".dat";
                try {
                    this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (name)));
                } catch (FileNotFoundException e) {
                    System.out.println("Could not open output file! Filename: " + name);
                }
            } else {
                this.m_OutputFile = null;
            }

            // init problem 
            this.m_GO.getProblem().initProblem();
            this.m_GO.getOptimizer().SetProblem(this.m_GO.getProblem());
            // int optimizer and population
            //this.m_GO.getOptimizer().init();

            // init the log data
            ArrayList tmpMultiRun = new ArrayList();
            this.m_PerformedRuns.add(tmpMultiRun);

            // something to log file
            //if (m_OutputFile != null) this.writeToFile(this.m_GO.getOptimizer().getStringRepresentation());
            //this.writeToFile("Here i'll write something characterizing the algorithm.");

            for (int j = 0; j < this.m_MultiRuns; j++) {
                this.m_GO.getProblem().initProblem(); // in the loop as well, dynamic probs may need that (MK)
            	this.m_TmpData  = new ArrayList();
                this.currentRun = j;
                if (this.show)
                    this.m_StatusField.setText("Optimizing Run " + (j+1) + " of " + this.m_MultiRuns + " Multi Runs...");
                if (Thread.interrupted())
                    throw new InterruptedException();
                // write header to file
                this.writeToFile(" FitnessCalls\t Best\t Mean\t Worst \t" + this.m_GO.getProblem().getAdditionalFileStringHeader(this.m_GO.getOptimizer().getPopulation()));
                if ((this.m_ContinueFlag) && (this.m_Backup != null)) {
                    this.m_RecentFC += this.m_Backup.getFunctionCalls();
                    this.m_GO.getOptimizer().getProblem().initProblem();
                    this.m_GO.getOptimizer().addPopulationChangedEventListener(null);
                    this.m_GO.getOptimizer().setPopulation(this.m_Backup);
                    this.m_GO.getOptimizer().getProblem().evaluate(this.m_GO.getOptimizer().getPopulation());
                    this.m_GO.getOptimizer().getProblem().evaluate(this.m_GO.getOptimizer().getPopulation().getArchive());
                    this.m_GO.getOptimizer().initByPopulation(this.m_Backup, false);
                    this.m_GO.getOptimizer().getPopulation().SetFunctionCalls(0);
                    this.m_GO.addPopulationChangedEventListener(this);
                } else {
                    this.m_RecentFC = 0;
                    this.m_GO.getOptimizer().init();
                }
                //while (this.m_GO.getOptimizer().getPopulation().getFunctionCalls() < this.m_FunctionCalls) {
                while (!this.m_GO.getTerminator().isTerminated(this.m_GO.getOptimizer().getPopulation())) {
                    //System.out.println("Simulated Function calls "+ this.m_Optimizer.getPopulation().getFunctionCalls());
                    if (Thread.interrupted()) throw new InterruptedException();
                    m_GO.getOptimizer().optimize();
                }
                System.gc();
                // @TODO if you want the final report include this
                //this.writeToFile(this.m_GO.getProblem().getStringRepresentationForProblem(this.m_GO.getOptimizer()));
                tmpMultiRun.add(this.m_TmpData);
            }
            if (this.show) this.m_Plot.setInfoString(this.currentExperiment, this.m_ExperimentName, 0.5f);
            if (this.show) this.draw();
            this.m_ExperimentName = this.m_GO.getOptimizer().getName()+"-"+this.m_PerformedRuns.size();
        }
        catch (InterruptedException e) {
            updateStatus(0);
            if (this.show) this.m_StatusField.setText("Interrupted...");
            return "Interrupted";
        }
        if (this.m_OutputFile != null) {
            try {
                this.m_OutputFile.close();
            } catch (IOException e) {
                System.out.println("Failed to close output file!");
            }
        }
        if (this.show) for (int i = 0; i < this.m_MultiRuns; i++) this.m_Plot.clearGraph(1000 +i);
        updateStatus(0);
        if (this.show) this.m_StatusField.setText("Finished...");
        return "All Done";
    }

    /** This method refreshes the plot
     */
    private void draw() {
        ArrayList   multiRuns, singleRun;
        Double[]    tmpD;
        double[][]  data;
//        Color       tmpColor;

        for (int i = this.m_PerformedRuns.size()-1; i < this.m_PerformedRuns.size(); i++) {
            multiRuns = (ArrayList)this.m_PerformedRuns.get(i);
            // determine minimum run length
            int minRunLen = Integer.MAX_VALUE;
            for (int j = 0; j < multiRuns.size(); j++) {
                singleRun = (ArrayList)multiRuns.get(j);
//                tmpD = (Double[])singleRun.get(singleRun.size()-1);
                minRunLen = Math.min(minRunLen, singleRun.size());
            }

            data = new double[minRunLen][3];
            // First run to determine mean
            for (int j = 0; j < multiRuns.size(); j++) {
                singleRun = (ArrayList)multiRuns.get(j);
                for (int p = 0; p < data.length; p++) {
                    tmpD = (Double[])singleRun.get(p);
                    data[p][0] = tmpD[0].doubleValue();
                    data[p][1] += tmpD[1].doubleValue()/multiRuns.size();
                }
            }
            // Second run to determine variance
            for (int j = 0; j < multiRuns.size(); j++) {
                singleRun = (ArrayList)multiRuns.get(j);
                for (int p = 0; p < data.length; p++) {
                    tmpD = (Double[])singleRun.get(p);
                    data[p][2] += Math.pow(data[p][1] - tmpD[1].doubleValue(),2)/multiRuns.size();
                }
            }
            // Now enter this stuff into the graph
            this.m_Plot.clearGraph(this.currentExperiment);
//            tmpColor = Color.darkGray;
//            if (this.m_GO.getOptimizer().getName().equalsIgnoreCase("MCS")) tmpColor = Color.magenta;
//            if (this.m_GO.getOptimizer().getName().equalsIgnoreCase("MS-HC")) tmpColor = Color.green;
//            if (this.m_GO.getOptimizer().getName().equalsIgnoreCase("GA")) tmpColor = Color.blue;
//            if (this.m_GO.getOptimizer().getName().equalsIgnoreCase("PBIL")) tmpColor = Color.CYAN;
//            if (this.m_GO.getOptimizer().getName().equalsIgnoreCase("CHC")) tmpColor = Color.ORANGE;
//            if (this.m_GO.getOptimizer().getName().equalsIgnoreCase("ES")) tmpColor = Color.red;
//            if (this.m_GO.getOptimizer().getName().equalsIgnoreCase("CBN-EA")) tmpColor = Color.black;

            for (int j = 0; j < data.length; j++) {
                if (this.m_ContinueFlag) this.m_Plot.setConnectedPoint(data[j][0]+this.m_RecentFC, data[j][1], this.currentExperiment);
                else this.m_Plot.setConnectedPoint(data[j][0], data[j][1], this.currentExperiment);
            }
            this.currentExperiment++;
        }
    }

    /**
     * When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with
     * SwingUtilities.invokeLater().  In this case we're just
     * changing the progress bars value.
     */
    void updateStatus(final int i) {
        if (this.m_ProgressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {
                public void run() {
                    m_ProgressBar.setValue(i);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
    }

    /** This method will create a new instance of LectureGUI and will call show() to create
     * the necessary frames. This method will ignore all arguments.
     * @param args
     */
    public static void main(String[] args) {
        if (false) {
            InterfaceGOStandalone app = (InterfaceGOStandalone)ThreadProxy.newInstance(new GOStandaloneVersion());
            new TokenHolder("streiche", "");
            app.startExperiment();
            app.setShow(false);
        } else {
            GOStandaloneVersion  program = new GOStandaloneVersion();
            RNG.setRandomSeed(1);
            program.initFrame();
            program.setShow(true);
        }
    }

    public void setShow(boolean t) {
        this.show = t;
    }

    /** This method allows an optimizer to register a change in the optimizer.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name) {
    	if (name.equals(Population.nextGenerationPerformed)) {
    		Population population = ((InterfaceOptimizer)source).getPopulation();
    		double x = 100/this.m_MultiRuns;
    		if (this.m_GO.getTerminator() instanceof EvaluationTerminator) {
    			double y = x/(double)((EvaluationTerminator)this.m_GO.getTerminator()).getFitnessCalls();
    			currentProgress = (int)(this.currentRun * x + population.getFunctionCalls()*y);
    		} else {
    			currentProgress = (int)(this.currentRun * x);
    		}
    		updateStatus(currentProgress);

    		// data to be stored in file
    		double tmpd = 0;
    		StringBuffer  tmpLine = new StringBuffer("");
    		tmpLine.append(population.getFunctionCalls());
    		tmpLine.append("\t");
    		tmpLine.append(population.getBestEAIndividual().getFitness(0));
    		tmpLine.append("\t");
    		for (int i = 0; i < population.size(); i++) tmpd += ((AbstractEAIndividual)population.get(i)).getFitness(0)/(double)population.size();
    		tmpLine.append("\t");
    		tmpLine.append(tmpd);
    		tmpLine.append("\t");
    		tmpLine.append(population.getWorstEAIndividual().getFitness(0));
    		//tmpLine.append("\t");
    		//tmpLine.append(this.m_GO.getProblem().getAdditionalFileStringValue(population));
    		this.writeToFile(tmpLine.toString());

    		Double[] tmpData = new Double[2];
    		tmpData[0] = new Double(population.getFunctionCalls());
    		// instead of adding simply the best fitness value i'll ask the problem what to show
    		tmpData[1] = this.m_GO.getProblem().getDoublePlotValue(population);
    		if (this.m_Plot != null) {
    			if (this.m_ContinueFlag) this.m_Plot.setConnectedPoint(tmpData[0].doubleValue()+this.m_RecentFC, tmpData[1].doubleValue(), 1000+this.currentRun);
    			else this.m_Plot.setConnectedPoint(tmpData[0].doubleValue(), tmpData[1].doubleValue(), 1000+this.currentRun);
    		}
    		this.m_TmpData.add(tmpData);
    	}
    }

    /** This method writes Data to file.
     * @param line      The line that is to be added to the file
     */
    private void writeToFile(String line) {
        String write = line + "\n";
        if (this.m_OutputFile == null) return;
        try {
            this.m_OutputFile.write(write, 0, write.length());
            this.m_OutputFile.flush();
        } catch (IOException e) {
            System.out.println("Problems writing to output file!");
        }
    }

    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "EA Lecture GUI";
    }

/**********************************************************************************************************************
 * These are for GUI
 */

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a simple example framework for Evolutionary Algorithms.";
    }

    /** This method allows you to set the number of mulitruns that are to be performed,
     * necessary for stochastic optimizers to ensure reliable results.
     * @param multiruns The number of multiruns that are to be performed
     */
    public void setMultiRuns(int multiruns) {
        this.m_MultiRuns = multiruns;
    }
    public int getMultiRuns() {
        return this.m_MultiRuns;
    }
    public String multiRunsTipText() {
        return "Multiple runs may be necessary to produce reliable results for stochastic optimizing algorithms.";
    }

    /** This method allows you to set the seed for the random number
     * generator.
     * @param seed The seed for the random number generator
     */
    // MK: These methods have nothing to do with the seed parameter from the m_GO object which is actually used, so I comment them out
//    public void setSeed(long seed) {
//        RNG.setseed(seed);
//    }
//    public long getSeed() {
//        return RNG.getRandomSeed();
//    }
//    public String seedTipText() {
//        return "Choose the seed for the random number generator.";
//    }

    /** This method sets the name of the current experiment as it will occur in the
     * plot legend.
     * @param experimentName The experiment name
     */
    public void setExpName(String experimentName) {
        this.m_ExperimentName = experimentName;
    }
    public String getExpName() {
        return this.m_ExperimentName;
    }
    public String expNameTipText() {
        return "Set the name of the experiment as it will occur in the legend.";
    }

    /** This method will set the output filename
     * @param name
     */
    public void setOutputFileName (String name) {
        this.m_OutputFileName = name;
    }
    public String getOutputFileName () {
        return this.m_OutputFileName;
    }
    public String outputFileNameTipText() {
        return "Set the name for the output file, if 'none' no output file will be created.";
    }

    /** This method will set the output filename
     * @param name
     */
    public void setList (List name) {
        this.m_List = name;
    }
    public List getListe () {
        return this.m_List;
    }
    public String listTipText() {
        return "Set the name for the output file, if 'none' no output file will be created.";
    }

//    /** This method allows you to set the number of functions calls that are to
//     * be evaluated. Note generational optimizers may exceed this number since
//     * they allways evaluate the complete population
//     * @param functionCalls The maximal number of Function calls
//     */
//    public void setFunctionCalls(int functionCalls) {
//        this.m_FunctionCalls = functionCalls;
//    }
//    public int getFunctionCalls() {
//        return this.m_FunctionCalls;
//    }
//    public String functionCallsTipText() {
//        return "The maxiaml number of function(fitness) evaluations that are performed. Mote: Generational algorihtms may be delayed!";
//    }
//
//    /** This method allows you to set the current optimizing algorithm
//     * @param optimizer The new optimizing algorithm
//     */
//    public void setOptimizer(InterfaceOptimizer optimizer) {
//        this.m_Optimizer = optimizer;
//        this.m_Optimizer.addPopulationChangedEventListener(this);
//        this.m_ExperimentName   = this.m_Optimizer.getName()+"-"+this.m_PerformedRuns.size();
//        this.m_Optimizer.SetProblem(this.m_Problem);
//    }
//    public InterfaceOptimizer getOptimizer() {
//        return this.m_Optimizer;
//    }
//    public String optimizerTipText() {
//        return "Choose a optimizing strategies.";
//    }

//    /** This method will set the problem that is to be optimized
//     * @param problem
//     */
//    public void SetProblem (InterfaceOptimizationProblem problem) {
//        this.m_Problem = problem;
//        this.m_Optimizer.SetProblem(this.m_Problem);
//    }
//    public InterfaceOptimizationProblem getProblem () {
//        return this.m_Problem;
//    }
//    public String problemTipText() {
//        return "Choose the problem that is to optimize and the EA individual parameters.";
//    }


//    public void setTest(InterfaceTest v) {
//        this.test = v;
//    }
//    public InterfaceTest getTest() {
//        return this.test;
//    }
//    public String testTipText() {
//        return "Test";
//    }
}
